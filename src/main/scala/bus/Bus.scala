package rtor.bus

import chisel3._
import chisel3.util._

class RoIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val addr = Output(UInt(addrw.W))
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val err = Input(Bool())
  val rdata = Input(UInt(dataw.W))
}

class RwIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val addr = Output(UInt(addrw.W))
  val we = Output(Bool())
  val be = Output(UInt((dataw / 8).W))
  val wdata = Output(UInt(dataw.W))
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val err = Input(Bool())
  val rdata = Input(UInt(dataw.W))
}

case class BaseMask(base: UInt, mask: UInt)

// Simple bus that doesn't handle the full bus protocol:
// - All devices must respond in the next cycle after the request.
// - Host arbitration is strictly priority based.
//
// Adapted from the lowRISC example bus.
class SimpleBus(
  nHost: Int,
  addrw: Int,
  dataw: Int,
  devs: List[BaseMask]
) extends Module {
  val nDev = devs.length

  val io = IO(new Bundle{
    val host = Vec(nHost, Flipped(new RwIO(addrw, dataw)))
    val dev = Vec(nDev, new RwIO(addrw, dataw))
  })

  val hostSelBits = if (nHost > 1) log2Ceil(nHost) else 1
  val devSelBits = if (nDev > 1) log2Ceil(nDev) else 1

  val hostSelReq = Wire(UInt(hostSelBits.W))

  // Host select
  hostSelReq := 0.U
  for (host <- (0 until nHost).reverse) {
    when (io.host(host).req) {
      hostSelReq := host.U
    }
  }

  val devSelReq = Wire(UInt(devSelBits.W))

  // Device select
  devSelReq := 0.U
  for (dev <- 0 until nDev) {
    when ((io.host(hostSelReq).addr & ~devs(dev).mask) === devs(dev).base) {
      devSelReq := dev.U
    }
  }

  // Responses are expected 1 cycle after request
  val hostSelResp = RegNext(hostSelReq, 0.U)
  val devSelResp = RegNext(devSelReq, 0.U)

  for (dev <- 0 until nDev) {
    when (dev.U === devSelReq) {
      io.dev(dev).req := io.host(hostSelReq).req
      io.dev(dev).we := io.host(hostSelReq).we
      io.dev(dev).addr := io.host(hostSelReq).addr
      io.dev(dev).wdata := io.host(hostSelReq).wdata
      io.dev(dev).be := io.host(hostSelReq).be
    } .otherwise {
      io.dev(dev).req := false.B
      io.dev(dev).we := false.B
      io.dev(dev).addr := 0.U
      io.dev(dev).wdata := 0.U
      io.dev(dev).be := 0.U
    }
  }

  for (host <- 0 until nHost) {
    io.host(host).gnt := false.B
    when (host.U === hostSelResp) {
      io.host(host).rvalid := io.dev(devSelResp).rvalid
      io.host(host).err := io.dev(devSelResp).err
      io.host(host).rdata := io.dev(devSelResp).rdata
    } .otherwise {
      io.host(host).rvalid := false.B
      io.host(host).err := false.B
      io.host(host).rdata := 0.U
    }
  }

  io.host(hostSelReq).gnt := io.host(hostSelReq).req
}
