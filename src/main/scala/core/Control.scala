package rvcpu.core

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

object CsrType extends ChiselEnum {
  val n, w, s, c, p = Value
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
