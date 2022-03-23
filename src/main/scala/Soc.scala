package rvcpu

import chisel3._
import chisel3.util._

import rvcpu.core._
import rvcpu.bus._
import rvcpu.sys.ram._
import rvcpu.sys.gpio._
import rvcpu.sys.timer._

import scala.collection.mutable.ListBuffer

case class Device(base: Int, size: Int, busIO: (Int, Int) => RwIO) {
  def mask: Int = {
    return size - 1
  }
}

object Mmio {
  val RamBase = 0x100000
  val RamSize = 16 * 1024
  val TimerBase = 0x10000
  val TimerSize = 1024
  val GpioBase = 0x20000
  val GpioSize = 1024
}

class Soc(memFile: String) extends Module {
  val xlen = 32

  val io = IO(new Bundle{
    val gpi = Vec(1, Input(Bool()))
    val gpo = Vec(3, Output(Bool()))
  })

  val boot = Mmio.RamBase.U(xlen.W)
  val core = Module(new Core(Config(xlen, boot)))

  val devices = List(
    Device(Mmio.RamBase, Mmio.RamSize, (base: Int, size: Int) => {
      val ram = Module(new Ram(log2Ceil(base)-1, size / 4, xlen, xlen, memFile))
      core.io.imem <> ram.io.imem

      ram.io.dmem
    }),
    Device(Mmio.TimerBase, Mmio.TimerSize, (base: Int, size: Int) => {
      val timer = Module(new Timer(log2Ceil(base)-1, xlen, xlen))
      timer.io.bus
    }),
    Device(Mmio.GpioBase, Mmio.GpioSize, (base: Int, size: Int) => {
      val gpio = Module(new Gpio(log2Ceil(base)-1, 1, 3, xlen, xlen))
      gpio.io.gpo <> io.gpo
      gpio.io.gpi <> io.gpi

      gpio.io.bus
    })
  )

  var devs = new ListBuffer[BaseMask]()
  for (dev <- devices) {
    devs += BaseMask(dev.base.U(xlen.W), dev.mask.U(xlen.W))
  }

  val bus = Module(new SimpleBus(1, xlen, xlen, devs.toList))
  core.io.dmem <> bus.io.host(0)

  for (i <- devices.indices) {
    val dev = devices(i)
    bus.io.dev(i) <> dev.busIO(dev.base, dev.size)
  }
}

object Soc extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Soc("mem/blink.hex"), Array("--target-dir", "generated"))
}
