package rtor.core

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._
import chisel3.util.experimental.decode
import chisel3.experimental.ChiselEnum
import chisel3.experimental.BundleLiterals._

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

  def B(sig: List[Element]): ControlSignals = {
    (new ControlSignals).Lit(
      _.pc_sel -> sig(0),
      _.a_sel -> sig(1),
      _.b_sel -> sig(2),
      _.imm_sel -> sig(3),
      _.alu_op -> sig(4),
      _.br_type -> sig(5),
      _.inst_kill -> sig(6),
      _.st_type -> sig(7),
      _.ld_type -> sig(8),
      _.wb_sel -> sig(9),
      _.wb_en -> sig(10),
      _.illegal -> sig(11),
      _.csr_type -> sig(12)
    )
  }

  // format: off
  //                                                                                       inst_kill                              wb_en illegal
  //                                                                                          |                                       |  |
  val default = B(List(PcSel.plus4, ASel.none,  BSel.none, ImmSel.x, AluOp.none, BrType.none, N, StType.none, LdType.none, WbSel.alu, N, Y, CsrType.n))
  val insts = Array(
    LUI   -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.u, AluOp.copyB, BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    AUIPC -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.u, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    JAL   -> B(List(PcSel.alu,   ASel.pc,   BSel.imm,  ImmSel.j, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N, CsrType.n)),
    JALR  -> B(List(PcSel.alu,   ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.none, WbSel.pc4, Y, N, CsrType.n)),
    BEQ   -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.eq,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BNE   -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ne,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BLT   -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.lt,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BGE   -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ge,   N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BLTU  -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.ltu,  N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    BGEU  -> B(List(PcSel.plus4, ASel.pc,   BSel.imm,  ImmSel.b, AluOp.add,   BrType.geu,  N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    LB    -> B(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lb,   WbSel.mem, Y, N, CsrType.n)),
    LH    -> B(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lh,   WbSel.mem, Y, N, CsrType.n)),
    LW    -> B(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lw,   WbSel.mem, Y, N, CsrType.n)),
    LBU   -> B(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lbu,  WbSel.mem, Y, N, CsrType.n)),
    LHU   -> B(List(PcSel.plus0, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, Y, StType.none, LdType.lhu,  WbSel.mem, Y, N, CsrType.n)),
    SB    -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sb,   LdType.none, WbSel.alu, N, N, CsrType.n)),
    SH    -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sh,   LdType.none, WbSel.alu, N, N, CsrType.n)),
    SW    -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.s, AluOp.add,   BrType.none, N, StType.sw,   LdType.none, WbSel.alu, N, N, CsrType.n)),
    ADDI  -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLTI  -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLTIU -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    XORI  -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    ORI   -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    ANDI  -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLLI  -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRLI  -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRAI  -> B(List(PcSel.plus4, ASel.rs1,  BSel.imm,  ImmSel.i, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    ADD   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.add,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SUB   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sub,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLL   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sll,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLT   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.slt,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SLTU  -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sltu,  BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    XOR   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.xor,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRL   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.srl,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    SRA   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.sra,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    OR    -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.or,    BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    AND   -> B(List(PcSel.plus4, ASel.rs1,  BSel.rs2,  ImmSel.x, AluOp.and,   BrType.none, N, StType.none, LdType.none, WbSel.alu, Y, N, CsrType.n)),
    FENCE -> B(List(PcSel.plus4, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    FENCEI-> B(List(PcSel.plus4, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, N, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n)),
    CSRRW -> B(List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.w)),
    CSRRS -> B(List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.s)),
    CSRRC -> B(List(PcSel.plus0, ASel.rs1,  BSel.none, ImmSel.x, AluOp.copyA, BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.c)),
    CSRRWI-> B(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.w)),
    CSRRSI-> B(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.s)),
    CSRRCI-> B(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.z, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, Y, N, CsrType.c)),
    ECALL -> B(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.ec)),
    EBREAK-> B(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.eb)),
    ERET  -> B(List(PcSel.epc,   ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.csr, N, N, CsrType.er)),
    WFI   -> B(List(PcSel.plus0, ASel.none, BSel.none, ImmSel.x, AluOp.none,  BrType.none, Y, StType.none, LdType.none, WbSel.alu, N, N, CsrType.n))
  )
  // format: on
}

class ControlIO extends Bundle {
  val inst = Input(UInt(32.W))
  val sig = Output(new ControlSignals())
}

object DecodeLookup {
  def apply[T <: Bundle](b: T, addr: UInt, default: T, mapping: Array[(BitPat, T)]): T = {
    val mapBits = mapping.map(x => (x._1 -> BitPat(x._2.litValue.U(x._2.getWidth.W))))
    val defBits = BitPat(default.litValue.U(default.getWidth.W))
    decoder(addr, decode.TruthTable(mapBits, defBits, false)).asTypeOf(b)
  }
}

class ControlSignals extends Bundle {
  val pc_sel = PcSel()
  val a_sel = ASel()
  val b_sel = BSel()
  val imm_sel = ImmSel()
  val alu_op = AluOp()
  val br_type = BrType()
  val inst_kill = Bool()
  val st_type = StType()
  val ld_type = LdType()
  val wb_sel = WbSel()
  val wb_en = Bool()
  val illegal = Bool()
  val csr_type = CsrType()
}

class Control extends Module {
  val io = IO(new ControlIO())

  io.sig := DecodeLookup((new ControlSignals), io.inst, Control.default, Control.insts)
}
