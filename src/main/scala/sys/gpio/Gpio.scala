package rtor.sys.gpio

import chisel3._
import chisel3.util._

import rtor.bus._

object RegMap {
  val inVal = 0x0
  val outVal = 0x4
}

class Gpio(offset: Int, nIn: Int, nOut: Int, addrw: Int, dataw: Int) extends Module {
  val io = IO(new Bundle{
    val bus = Flipped(new RwIO(addrw, dataw))

    val gpi = Vec(nIn, Input(Bool()))
    val gpo = Vec(nOut, Output(Bool()))
  })

  io.bus.gnt := io.bus.req

  val we = io.bus.req && io.bus.we

  def wReg(addr: UInt, width: Int): UInt = {
    val reg = RegInit(0.U(width.W))
    val rwe = we && (io.bus.addr(offset, 0) === addr)

    when (rwe) {
      reg := io.bus.wdata
    }

    return reg
  }

  val inVal = RegNext(io.gpi.asUInt, 0.U)
  val outVal = wReg(RegMap.outVal.U, nOut)
  val addr = RegNext(io.bus.addr(offset, 0))

  io.bus.err := true.B
  io.bus.rdata := 0.U
  switch (addr) {
    is (RegMap.inVal.U) {
      io.bus.err := false.B
      io.bus.rdata := inVal
    }
    is (RegMap.outVal.U) {
      io.bus.err := false.B
      io.bus.rdata := outVal
    }
  }

  io.gpo := outVal.asBools
  io.bus.rvalid := RegNext(io.bus.req)
}
