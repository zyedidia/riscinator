package rvcpu.core

import chisel3._
import chisel3.util._

class ImmExtractIO(xlen: Int) extends Bundle {
  val inst = Input(UInt(32.W))
  val sel = Input(ImmSel())
  val out = Output(UInt(xlen.W))
}

class ImmExtract(xlen: Int) extends Module {
  val io = IO(new ImmExtractIO(xlen))

  val sint = Wire(SInt(xlen.W))
  io.out := sint.asUInt

  sint := 0.S
  switch (io.sel) {
    is (ImmSel.i) { sint := io.inst(31, 20).asSInt }
    is (ImmSel.s) { sint := Cat(io.inst(31, 25), io.inst(11, 7)).asSInt }
    is (ImmSel.b) { sint := Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)).asSInt }
    is (ImmSel.u) { sint := Cat(io.inst(31, 12), 0.U(12.W)).asSInt }
    is (ImmSel.j) { sint := Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 25), io.inst(24, 21), 0.U(1.W)).asSInt }
    is (ImmSel.z) { sint := io.inst(19, 15).zext }
  }
}
