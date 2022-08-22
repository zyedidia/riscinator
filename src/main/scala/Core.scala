package rtor

import chisel3._
import chisel3.util._

import rtor.core._

object Core extends App {
  (new chisel3.stage.ChiselStage)
    .emitVerilog(new Core(Config(32, 0x100000.U(32.W))), Array("--target-dir", "generated"))
}
