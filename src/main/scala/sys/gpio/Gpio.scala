package rvcpu.sys

import chisel3._
import chisel3.util._

import rvcpu.bus._

object Mmio {
  val inVal = 0x0
  val outVal = 0x4
}

class Gpio(nIn: Int, nOut: Int, addrw: Int, dataw: Int) extends Module {
  val io = IO(new Bundle{
    val bus = Flipped(new RwIO(addrw, dataw))

    val gpi = Vec(nIn, Input(Bool()))
    val gpo = Vec(nOut, Output(Bool()))
  })

  io.bus.err := false.B
  io.bus.gnt := io.bus.req

  val we = io.bus.req && io.bus.we

  def wReg(addr: UInt, width: Int): UInt = {
    val reg = RegInit(0.U(width.W))
    val rwe = we && (io.bus.addr === addr)

    when (rwe) {
      reg := io.bus.wdata
    }

    return reg
  }

  val inVal = RegNext(io.gpi.asUInt, 0.U)
  val outVal = wReg(Mmio.outVal.U, nOut)

  io.bus.err := true.B
  io.bus.rdata := 0.U
  switch (io.bus.addr) {
    is (Mmio.inVal.U) {
      io.bus.err := false.B
      io.bus.rdata := inVal
    }
    is (Mmio.inVal.U) {
      io.bus.err := false.B
      io.bus.rdata := outVal
    }
  }

  io.gpo := outVal.asBools

  io.bus.rvalid := RegNext(io.bus.req)
}
