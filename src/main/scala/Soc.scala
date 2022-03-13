package rvcpu

import chisel3._
import chisel3.util._

import rvcpu.core._
import rvcpu.sys._

class Soc extends Module {
  val xlen = 32

  val io = IO(new Bundle{
    val led = Output(Bool())
  })

  val core = Module(new Core(Config(xlen, 0x0000.U)))
  val ram = Module(new Ram(1024, xlen, xlen, "./mem/riscvtest.hex"))

  core.io.imem <> ram.io.imem
  core.io.dmem <> ram.io.dmem

  io.led := core.io.dmem.req
}

object Soc extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Soc, Array("--target-dir", "generated"))
}
