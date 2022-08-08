package rtor.core

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode
import chisel3.util.experimental.decode._
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
  def L(list: List[Element]): List[Element] = {
    return list.map(x => x.litValue.U(x.getWidth.W))
  }
  // format: off
  //                                                                                     inst_kill                              wb_en illegal
  //                                                                                        |                                       |  |
  val default = L(List(PcSel.plus4, ASel.none,  BSel.none, ImmSel.x, AluOp.none, BrType.none, N, StType.none, LdType.none, WbSel.alu, N, Y, CsrType.n))
  val insts = Array(
    LUI   -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.u, AluOp.copyB, BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    AUIPC -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.u, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    JAL   -> L(List(PcSel.alu,   ASel.pc,   BSel.imm,  ImmSel.j, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N, CsrType.n)),
    JALR  -> L(List(PcSel.alu,   ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N, CsrType.n)),
    BEQ   -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.eq,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BNE   -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ne,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BLT   -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.lt,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BGE   -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ge,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BLTU  -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ltu,  N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BGEU  -> L(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.geu,  N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    LB    -> L(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lb,   WbSel.mem, Y, N, CsrType.n)),
    LH    -> L(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lh,   WbSel.mem, Y, N, CsrType.n)),
    LW    -> L(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lw,   WbSel.mem, Y, N, CsrType.n)),
    LBU   -> L(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lbu,  WbSel.mem, Y, N, CsrType.n)),
    LHU   -> L(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lhu,  WbSel.mem, Y, N, CsrType.n)),
    SB    -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sb,   LdType.none, WbSel.alu, N, N, CsrType.n)),
    SH    -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sh,   LdType.none, WbSel.alu, N, N, CsrType.n)),
    SW    -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sw,   LdType.none, WbSel.alu, N, N, CsrType.n)),
    ADDI  -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLTI  -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLTIU -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    XORI  -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    ORI   -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    ANDI  -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLLI  -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRLI  -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRAI  -> L(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    ADD   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SUB   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sub,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLL   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLT   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLTU  -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    XOR   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRL   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRA   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    OR    -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    AND   -> L(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    FENCE -> L(List(PcSel.plus4, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    FENCEI-> L(List(PcSel.plus4, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    CSRRW -> L(List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.w)),
    CSRRS -> L(List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.s)),
    CSRRC -> L(List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.c)),
    CSRRWI-> L(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.w)),
    CSRRSI-> L(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.s)),
    CSRRCI-> L(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.c)),
    ECALL -> L(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.ec)),
    EBREAK-> L(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.eb)),
    ERET  -> L(List(PcSel.epc,   ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.er)),
    WFI   -> L(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n))
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

  def toBitPat(l: List[Element]): BitPat = {
    l.map(y => BitPat(y.asUInt)).reduce((a, b) => a ## b)
  }

  val bitpats = Control.insts.map(x => (x._1 -> toBitPat(x._2)))
  val default = toBitPat(Control.default)

  val signals = decoder(io.inst, decode.TruthTable(bitpats, default, false))

  def get[T](gen: UInt => (T, Bool), v: UInt): T = {
    val (x, valid) = gen(v)
    assert(valid)
    x
  }

  io.pc_sel := get(PcSel.safe, signals(0))
  io.a_sel := get(ASel.safe, signals(1))
  io.b_sel := get(BSel.safe, signals(2))
  io.imm_sel := get(ImmSel.safe, signals(3))
  io.alu_op := get(AluOp.safe, signals(4))
  io.br_type := get(BrType.safe, signals(5))
  io.inst_kill := signals(6)
  io.st_type := get(StType.safe, signals(7))
  io.ld_type := get(LdType.safe, signals(8))
  io.wb_sel := get(WbSel.safe, signals(9))
  io.wb_en := signals(10)
  io.illegal := signals(11)
  io.csr_type := get(CsrType.safe, signals(12))
}
