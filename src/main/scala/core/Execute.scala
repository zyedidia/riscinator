package rvcpu.core

import chisel3._
import chisel3.util._

class ExecuteCtrlIO extends Bundle {
  val alu_op = Input(AluOp())
  val a_sel = Input(ASel())
  val b_sel = Input(BSel())
}

class ExecuteDataIO(xlen: Int) extends Bundle {
  val pc = Input(UInt(xlen.W))
  val rs1r = Input(UInt(xlen.W))
  val rs2r = Input(UInt(xlen.W))
  val imm = Input(UInt(xlen.W))

  val alu_out = Output(UInt(xlen.W))
}

class ExecuteIO(xlen: Int) extends Bundle {
  val ctrl = new ExecuteCtrlIO()
  val data = new ExecuteDataIO(xlen)
}

class Execute(xlen: Int) extends Module {
  val io = IO(new ExecuteIO(xlen))

  val alu = Module(new Alu(xlen))
  io.data.alu_out := alu.io.out

  alu.io.op := io.ctrl.alu_op

  alu.io.a := 0.U
  switch (io.ctrl.a_sel) {
    is (ASel.pc)  { alu.io.a := io.data.pc  }
    is (ASel.rs1) { alu.io.a := io.data.rs1r }
  }

  alu.io.b := 0.U
  switch (io.ctrl.b_sel) {
    is (BSel.imm) { alu.io.b := io.data.imm }
    is (BSel.rs2) { alu.io.b := io.data.rs2r }
  }
}
