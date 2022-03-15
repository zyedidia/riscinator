package rvcpu

import chisel3._
import chisel3.util._

import rvcpu.core._
import rvcpu.bus._
import rvcpu.sys.ram._
import rvcpu.sys.gpio._

case class Dev(idx: Int, base: Int, size: Int) {
  def mask: Int = {
    return size - 1
  }
}

object DevMap {
  val ram = Dev(0, 0x100000, 16 * 1024)
  val gpio = Dev(1, 0x10000, 1024)
}

class Soc extends Module {
  val io = IO(new Bundle{
    val gpi = Vec(1, Input(Bool()))
    val gpo = Vec(1, Output(Bool()))
  })

  val xlen = 32
  val bootAddr = DevMap.ram.base.U(xlen.W)

  val core = Module(new Core(Config(xlen, bootAddr)))

  val bus = Module(
    new SimpleBus(
      1, xlen, xlen,
      List(DevMap.ram.base.U(xlen.W), DevMap.gpio.base.U(xlen.W)),
      List(DevMap.ram.mask.U(xlen.W), DevMap.gpio.mask.U(xlen.W))
    )
  )

  val ram = Module(new Ram(log2Ceil(DevMap.ram.base), DevMap.ram.size / 4, xlen, xlen, "./mem/riscvtest.hex"))
  val gpio = Module(new Gpio(1, 1, xlen, xlen))
  gpio.io.gpo <> io.gpo
  gpio.io.gpi <> io.gpi

  core.io.imem <> ram.io.imem
  core.io.dmem <> bus.io.host(0)
  bus.io.dev(DevMap.ram.idx) <> ram.io.dmem
  bus.io.dev(DevMap.gpio.idx) <> gpio.io.bus
}

object Soc extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Soc, Array("--target-dir", "generated"))
}
