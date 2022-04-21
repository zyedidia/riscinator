package rvcpu.core

import chisel3._
import chisel3.util._

class DecodeCtrlIO extends Bundle {
  val imm_sel = Input(ImmSel())
}

class DecodeDataIO(xlen: Int, rlen: Int) extends Bundle {
  val inst = Input(UInt(xlen.W))

  val rs1 = Output(UInt(rlen.W))
  val rs2 = Output(UInt(rlen.W))
  val rd = Output(UInt(rlen.W))
  val imm = Output(UInt(xlen.W))
}

class DecodeIO(xlen: Int, rlen: Int) extends Bundle {
  val ctrl = new DecodeCtrlIO()
  val data = new DecodeDataIO(xlen, rlen: Int)
}

class Decode(xlen: Int, rlen: Int) extends Module {
  val io = IO(new DecodeIO(xlen, rlen))

  io.data.rs1 := io.data.inst(19, 15)
  io.data.rs2 := io.data.inst(24, 20)
  io.data.rd := io.data.inst(11, 7)

  val sint = Wire(SInt(xlen.W))
  io.data.imm := sint.asUInt

  val inst = io.data.inst
  sint := 0.S
  switch (io.ctrl.imm_sel) {
    is (ImmSel.i) { sint := inst(31, 20).asSInt }
    is (ImmSel.s) { sint := Cat(inst(31, 25), inst(11, 7)).asSInt }
    is (ImmSel.b) { sint := Cat(inst(31), inst(7), inst(30, 25), inst(11, 8), 0.U(1.W)).asSInt }
    is (ImmSel.u) { sint := Cat(inst(31, 12), 0.U(12.W)).asSInt }
    is (ImmSel.j) { sint := Cat(inst(31), inst(19, 12), inst(20), inst(30, 25), inst(24, 21), 0.U(1.W)).asSInt }
    is (ImmSel.z) { sint := inst(19, 15).zext }
  }
}
