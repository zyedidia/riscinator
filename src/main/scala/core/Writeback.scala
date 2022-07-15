package rvcpu.core

import chisel3._
import chisel3.util._

class WritebackCtrlIO extends Bundle {
  val wb_sel = Input(WbSel())
  val wb_en = Input(Bool())
  val ld_type = Input(LdType())
}

class WritebackDataIO(xlen: Int) extends Bundle {
  val ld = Input(UInt(xlen.W))
  val pc = Input(UInt(xlen.W))
  val alu_out = Input(UInt(xlen.W))
  val rd = Input(UInt(xlen.W))
}

class WritebackRfIO(xlen: Int, rlen: Int) extends Bundle {
  val wen = Output(Bool())
  val waddr = Output(UInt(rlen.W))
  val wdata = Output(UInt(xlen.W))
}

class WritebackIO(xlen: Int, rlen: Int) extends Bundle {
  val ctrl = new WritebackCtrlIO()
  val data = new WritebackDataIO(xlen)
  val rf = new WritebackRfIO(xlen, rlen)
}

class Writeback(xlen: Int, rlen: Int) extends Module {
  val io = IO(new WritebackIO(xlen, rlen))

  val ldoff = (io.data.alu_out(1) << 4.U) | (io.data.alu_out(0) << 3.U)
  val ldshift = io.data.ld >> ldoff
  val ld = Wire(SInt(xlen.W))

  ld := io.data.ld.zext
  switch (io.ctrl.ld_type) {
    is (LdType.lh)  { ld := ldshift(15, 0).asSInt }
    is (LdType.lb)  { ld := ldshift(7, 0).asSInt }
    is (LdType.lhu) { ld := ldshift(15, 0).zext }
    is (LdType.lbu) { ld := ldshift(7, 0).zext }
  }

  io.rf.wen := io.ctrl.wb_en
  io.rf.waddr := io.data.rd
  io.rf.wdata := 0.U
  switch (io.ctrl.wb_sel) {
    is (WbSel.alu) { io.rf.wdata := io.data.alu_out }
    is (WbSel.mem) { io.rf.wdata := ld.asUInt }
    is (WbSel.pc4) { io.rf.wdata := io.data.pc + 4.U }
  }
}
