package rtor.core

import chisel3._
import chisel3.util._

class ExecuteCtrlIO extends Bundle {
  val imm_sel = Input(ImmSel())
  val ld_type = Input(LdType())
  val st_type = Input(StType())
  val alu_op = Input(AluOp())
  val a_sel = Input(ASel())
  val b_sel = Input(BSel())
  val br_type = Input(BrType())
}

class ExecuteDataIO(xlen: Int, rlen: Int) extends Bundle {
  val inst = Input(UInt(xlen.W))
  val pc = Input(UInt(xlen.W))

  val rd = Output(UInt(rlen.W))
  val imm = Output(UInt(xlen.W))
  val csr = Output(UInt(12.W))
  val alu_out = Output(UInt(xlen.W))
  val br_taken = Output(Bool())
}

class ExecuteRfIO(xlen: Int, rlen: Int) extends Bundle {
  val rs1 = Output(UInt(rlen.W))
  val rs2 = Output(UInt(rlen.W))
  val rs1r = Input(UInt(xlen.W))
  val rs2r = Input(UInt(xlen.W))
}

class ExecuteIO(xlen: Int, rlen: Int) extends Bundle {
  val ctrl = new ExecuteCtrlIO()
  val data = new ExecuteDataIO(xlen, rlen)
  val dmem = new DmemIO(xlen, xlen)
  val rf = new ExecuteRfIO(xlen, rlen)
}

class Execute(xlen: Int, rlen: Int) extends Module {
  val io = IO(new ExecuteIO(xlen, rlen))

  io.rf.rs1 := io.data.inst(19, 15)
  io.rf.rs2 := io.data.inst(24, 20)
  io.data.rd := io.data.inst(11, 7)
  io.data.csr := io.data.inst(31, 20)

  val sint = Wire(SInt(xlen.W))
  io.data.imm := sint.asUInt

  val inst = io.data.inst
  sint := DontCare
  switch(io.ctrl.imm_sel) {
    is(ImmSel.i) { sint := inst(31, 20).asSInt }
    is(ImmSel.s) { sint := Cat(inst(31, 25), inst(11, 7)).asSInt }
    is(ImmSel.b) { sint := Cat(inst(31), inst(7), inst(30, 25), inst(11, 8), 0.U(1.W)).asSInt }
    is(ImmSel.u) { sint := Cat(inst(31, 12), 0.U(12.W)).asSInt }
    is(ImmSel.j) { sint := Cat(inst(31), inst(19, 12), inst(20), inst(30, 25), inst(24, 21), 0.U(1.W)).asSInt }
    is(ImmSel.z) { sint := inst(19, 15).zext }
  }

  val alu = Module(new Alu(xlen))
  io.data.alu_out := alu.io.out
  alu.io.op := io.ctrl.alu_op

  alu.io.a := Mux(io.ctrl.a_sel === ASel.rs1, io.rf.rs1r, io.data.pc)
  alu.io.b := Mux(io.ctrl.b_sel === BSel.rs2, io.rf.rs2r, io.data.imm)

  val daddr = alu.io.out & ~("b11".U(xlen.W))
  val woffset = Wire(UInt(5.W))
  woffset := (alu.io.out(1) << 4.U) | (alu.io.out(0) << 3.U)

  val dmem_rd_req = io.ctrl.ld_type =/= LdType.none
  val dmem_wr_req = io.ctrl.st_type =/= StType.none

  io.dmem.req := dmem_rd_req || dmem_wr_req
  io.dmem.we := dmem_wr_req
  io.dmem.addr := daddr
  io.dmem.wdata := io.rf.rs2r << woffset

  io.dmem.be := "b1111".U
  switch(io.ctrl.st_type) {
    is(StType.sh) { io.dmem.be := "b11".U << alu.io.out(1, 0) }
    is(StType.sb) { io.dmem.be := "b1".U << alu.io.out(1, 0) }
  }

  val br = Module(new Branch(xlen))
  br.io.rs1 := io.rf.rs1r
  br.io.rs2 := io.rf.rs2r
  br.io.br_type := io.ctrl.br_type
  io.data.br_taken := br.io.taken
}
