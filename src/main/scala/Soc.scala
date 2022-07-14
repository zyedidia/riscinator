package rvcpu

import chisel3._

import rvcpu.core._

class Soc() extends Module {}

object Soc extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Core(Config(32, 0.U)), Array("--target-dir", "generated"))
}
