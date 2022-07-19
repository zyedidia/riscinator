package rtor.core

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

object CsrType extends ChiselEnum {
  val n, w, s, c, ec, eb, er = Value
}

object PcSel extends ChiselEnum {
  val plus4, alu, plus0, epc = Value
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
  val alu, mem, pc4, csr = Value
}

import Instructions._

object Control {
  val Y = true.B
  val N = false.B
  // format: off
  //                                                                                     inst_kill                              wb_en illegal
  //                                                                                        |                                       |  |
  val default = List(PcSel.plus4, ASel.none,  BSel.none, ImmSel.x, AluOp.none, BrType.none, N, StType.none, LdType.none, WbSel.alu, N, Y, CsrType.n)
  val map = Array(
    LUI   -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.u, AluOp.copyB, BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    AUIPC -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.u, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    JAL   -> List(PcSel.alu,   ASel.pc,   BSel.imm,  ImmSel.j, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N, CsrType.n),
    JALR  -> List(PcSel.alu,   ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N, CsrType.n),
    BEQ   -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.eq,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    BNE   -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ne,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    BLT   -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.lt,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    BGE   -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ge,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    BLTU  -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ltu,  N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    BGEU  -> List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.geu,  N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    LB    -> List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lb,   WbSel.mem, Y, N, CsrType.n),
    LH    -> List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lh,   WbSel.mem, Y, N, CsrType.n),
    LW    -> List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lw,   WbSel.mem, Y, N, CsrType.n),
    LBU   -> List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lbu,  WbSel.mem, Y, N, CsrType.n),
    LHU   -> List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lhu,  WbSel.mem, Y, N, CsrType.n),
    SB    -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sb,   LdType.none, WbSel.alu, N, N, CsrType.n),
    SH    -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sh,   LdType.none, WbSel.alu, N, N, CsrType.n),
    SW    -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sw,   LdType.none, WbSel.alu, N, N, CsrType.n),
    ADDI  -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SLTI  -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SLTIU -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    XORI  -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    ORI   -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    ANDI  -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SLLI  -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SRLI  -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SRAI  -> List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    ADD   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SUB   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sub,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SLL   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SLT   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SLTU  -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    XOR   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SRL   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    SRA   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    OR    -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    AND   -> List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n),
    FENCE -> List(PcSel.plus4, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    FENCEI-> List(PcSel.plus4, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n),
    CSRRW -> List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.w),
    CSRRS -> List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.s),
    CSRRC -> List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.c),
    CSRRWI-> List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.w),
    CSRRSI-> List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.s),
    CSRRCI-> List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.c),
    ECALL -> List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.ec),
    EBREAK-> List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.eb),
    ERET  -> List(PcSel.epc,   ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.er),
    WFI   -> List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)
  )
  // format: on
}

class ControlIO extends Bundle {
  val inst = Input(UInt(32.W))
  val pc_sel = Output(PcSel())
  val a_sel = Output(ASel())
  val b_sel = Output(BSel())
  val imm_sel = Output(ImmSel())
  val alu_op = Output(AluOp())
  val br_type = Output(BrType())
  val inst_kill = Output(Bool())
  val st_type = Output(StType())
  val ld_type = Output(LdType())
  val wb_sel = Output(WbSel())
  val wb_en = Output(Bool())
  val illegal = Output(Bool())
  val csr_type = Output(CsrType())
}

class Control extends Module {
  val io = IO(new ControlIO())

  val signals = ListLookup(io.inst, Control.default, Control.map)

  io.pc_sel := signals(0)
  io.a_sel := signals(1)
  io.b_sel := signals(2)
  io.imm_sel := signals(3)
  io.alu_op := signals(4)
  io.br_type := signals(5)
  io.inst_kill := signals(6)
  io.st_type := signals(7)
  io.ld_type := signals(8)
  io.wb_sel := signals(9)
  io.wb_en := signals(10)
  io.illegal := signals(11)
  io.csr_type := signals(12)
}
