package rvcpu.core

import chisel3._
import chisel3.util._

class FetchCtrlIO extends Bundle {
  val pc_sel = Input(PcSel())
  val stall = Input(Bool())
}

class FetchMemIO(addrw: Int) extends Bundle {
  val req = Output(Bool())
  val addr = Output(UInt(addrw.W))
}

class FetchIO(xlen: Int) extends Bundle {
  val imem = new FetchMemIO(xlen)
  val ctrl = new FetchCtrlIO()
  val alu_out = Input(UInt(xlen.W))
  val pc = Output(UInt(xlen.W))
}

class Fetch(xlen: Int, bootAddr: UInt) extends Module {
  val io = IO(new FetchIO(xlen))

  val pc = RegInit(bootAddr)
  io.pc := pc

  when (io.ctrl.stall) {
    pc := pc
  } .elsewhen (io.ctrl.pc_sel === PcSel.alu) {
    pc := io.alu_out & ~(0x1.U(xlen.W))
  } .elsewhen (io.ctrl.pc_sel === PcSel.plus4) {
    pc := pc + 4.U
  }

  io.imem.addr := pc
  io.imem.req := true.B
}
