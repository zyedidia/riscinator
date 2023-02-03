package rtor

import chisel3._
import chisel3.util._

import rtor.core._

object CoreSingle extends App {
  (new chisel3.stage.ChiselStage)
    .emitVerilog(new CoreSingle(Config(32, 0x100000.U(32.W))), Array("--target-dir", "generated"))
}
