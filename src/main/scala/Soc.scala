package rvcpu

import chisel3._
import chisel3.util._

import rvcpu.core._
import rvcpu.sys._

import bus._

class Soc extends Module {
  val io = IO(new Bundle{
    val led = Output(Bool())
  })

  val xlen = 32
  val ramStart = 0x100000
  val ramSize = 16 * 1024;
  val ramMask = (ramSize - 1).U(xlen.W);
  val bootAddr = ramStart.U(xlen.W)

  val core = Module(new Core(Config(xlen, bootAddr)))

  val devRam = 0
  val devBase = List(ramStart.U(xlen.W))
  val devMask = List(ramMask)

  val bus = Module(new SimpleBus(1, xlen, xlen, devBase, devMask))

  val ram = Module(new Ram(log2Ceil(ramStart), ramSize, xlen, xlen, "./mem/riscvtest.hex"))

  core.io.imem <> ram.io.imem
  core.io.dmem <> bus.io.host(0)
  ram.io.dmem <> bus.io.dev(devRam)

  io.led := core.io.dmem.req
}

object Soc extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Soc, Array("--target-dir", "generated"))
}
