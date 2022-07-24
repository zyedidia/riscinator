package rtor.core

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
  val epc = Input(Valid(UInt(xlen.W)))
  val br_taken = Input(Bool())
  val alu_out = Input(UInt(xlen.W))
  val pc = Output(UInt(xlen.W))
}

class Fetch(xlen: Int, bootAddr: UInt) extends Module {
  val io = IO(new FetchIO(xlen))

  val pc = RegInit(bootAddr)
  val next = Wire(UInt(xlen.W))
  pc := next
  io.pc := pc

  when(io.epc.valid) {
    next := io.epc.bits
  }.elsewhen(io.ctrl.stall || io.ctrl.pc_sel === PcSel.plus0) {
    next := pc
  }.elsewhen(io.ctrl.pc_sel === PcSel.alu || io.br_taken) {
    next := io.alu_out & ~(1.U(xlen.W))
  }.otherwise {
    next := pc + 4.U
  }

  io.imem.addr := next
  io.imem.req := true.B
}
