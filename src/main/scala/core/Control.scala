package rvcpu.core

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

object PcSel extends ChiselEnum {
  val plus4, alu, plus0 = Value
}

object ASel extends ChiselEnum {
  val none, pc, rs1 = Value
}

object BSel extends ChiselEnum {
  val none, imm, rs2 = Value
}

object ImmSel extends ChiselEnum {
  val x, i, s, u, j, b, z = Value
}

object BrType extends ChiselEnum {
  val none, ltu, lt, eq, geu, ge, ne = Value
}

object StType extends ChiselEnum {
  val none, sw, sh, sb = Value
}

object LdType extends ChiselEnum {
  val none, lw, lh, lb, lhu, lbu = Value
}

object WbSel extends ChiselEnum {
  val alu, mem, pc4 = Value
}

import Instructions._

object Control {
  val Y = true.B
  val N = false.B
  //                                                                                       kill                                  wb_en illegal
  //                                                                                        |                                       |  |
  val default = List(PcSel.plus4, ASel.none,  BSel.none, ImmSel.x, AluOp.none, BrType.none, N, StType.none, LdType.none, WbSel.alu, N, Y)
  // format: off
  val map = Array(
    LUI   -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.u, AluOp.copyB, BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    AUIPC -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.u, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    JAL   -> List(PcSel.alu,   ASel.pc,    BSel.imm,  ImmSel.j, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N),
    JALR  -> List(PcSel.alu,   ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N),
    BEQ   -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.b, AluOp.add,   BrType.eq,   N, StType.none, LdType.none, WbSel.alu, N, N),
    BNE   -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.b, AluOp.add,   BrType.ne,   N, StType.none, LdType.none, WbSel.alu, N, N),
    BLT   -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.b, AluOp.add,   BrType.lt,   N, StType.none, LdType.none, WbSel.alu, N, N),
    BGE   -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.b, AluOp.add,   BrType.ge,   N, StType.none, LdType.none, WbSel.alu, N, N),
    BLTU  -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.b, AluOp.add,   BrType.ltu,  N, StType.none, LdType.none, WbSel.alu, N, N),
    BGEU  -> List(PcSel.plus4, ASel.pc,    BSel.imm,  ImmSel.b, AluOp.add,   BrType.geu,  N, StType.none, LdType.none, WbSel.alu, N, N),
    LB    -> List(PcSel.plus0, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lb,   WbSel.mem, Y, N),
    LH    -> List(PcSel.plus0, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lh,   WbSel.mem, Y, N),
    LW    -> List(PcSel.plus0, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lw,   WbSel.mem, Y, N),
    LBU   -> List(PcSel.plus0, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lbu,  WbSel.mem, Y, N),
    LHU   -> List(PcSel.plus0, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lhu,  WbSel.mem, Y, N),
    SB    -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sb,   LdType.none, WbSel.alu, N, N),
    SH    -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sh,   LdType.none, WbSel.alu, N, N),
    SW    -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sw,   LdType.none, WbSel.alu, N, N),
    ADDI  -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SLTI  -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SLTIU -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    XORI  -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    ORI   -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    ANDI  -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SLLI  -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SRLI  -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SRAI  -> List(PcSel.plus4, ASel.rs1,   BSel.imm,  ImmSel.i, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    ADD   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SUB   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.sub,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SLL   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SLT   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SLTU  -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    XOR   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SRL   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    SRA   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    OR    -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    AND   -> List(PcSel.plus4, ASel.rs1,   BSel.rs2,  ImmSel.x, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N),
    FENCE -> List(PcSel.plus4, ASel.none,  BSel.none, ImmSel.x, AluOp.none,  BrType.none, N, StType.none, LdType.none, WbSel.alu, N, N),
    FENCEI-> List(PcSel.plus0, ASel.none,  BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.alu, N, N))
  // format: on
}

class ControlIO extends Bundle {
  val inst = Input(UInt(32.W))
  val pc_sel = Output(PcSel())
  val inst_kill = Output(Bool())
  val a_sel = Output(ASel())
  val b_sel = Output(BSel())
  val imm_sel = Output(ImmSel())
  val alu_op = Output(AluOp())
  val br_type = Output(BrType())
  val st_type = Output(StType())
  val ld_type = Output(LdType())
  val wb_sel = Output(WbSel())
  val wb_en = Output(Bool())
  val illegal = Output(Bool())
}

class Control extends Module {
  val io = IO(new ControlIO())

  val signals = ListLookup(io.inst, Control.default, Control.map)

  // Control signals for Fetch
  io.pc_sel := signals(0)
  io.inst_kill := signals(6)

  // Control signals for Execute
  io.a_sel := signals(1)
  io.b_sel := signals(2)
  io.imm_sel := signals(3)
  io.alu_op := signals(4)
  io.br_type := signals(5)
  io.st_type := signals(7)

  // Control signals for Write Back
  io.ld_type := signals(8)
  io.wb_sel := signals(9)
  io.wb_en := signals(10)
  io.illegal := signals(11)
}
