package rtor

import chisel3._
import chisel3.util._

import rtor.core._
import rtor.bus._
import rtor.sys.ram._
import rtor.sys.gpio._
import rtor.sys.timer._

import scala.collection.mutable.ListBuffer

case class Device(base: Int, size: Int, busIO: () => RwIO) {
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
    val gpo = Vec(7, Output(Bool()))
  })

  val boot = Mmio.RamBase.U(xlen.W)
  val core = Module(new Core(Config(xlen, boot)))

  val ram = Module(new Ram(log2Ceil(Mmio.RamBase)-1, Mmio.RamSize / 4, xlen, xlen, memFile))
  val timer = Module(new Timer(log2Ceil(Mmio.TimerBase)-1, xlen, xlen))
  val gpio = Module(new Gpio(log2Ceil(Mmio.GpioBase)-1, 1, 7, xlen, xlen))

  val devices = List(
    Device(Mmio.RamBase, Mmio.RamSize, () => {
      core.io.imem <> ram.io.imem
      ram.io.dmem
    }),
    Device(Mmio.TimerBase, Mmio.TimerSize, () => {
      timer.io.bus
    }),
    Device(Mmio.GpioBase, Mmio.GpioSize, () => {
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
    bus.io.dev(i) <> dev.busIO()
  }
}

object Soc extends App {
  if (args.length == 0) {
    throw new Exception("no memory file provided")
  }
  (new chisel3.stage.ChiselStage).emitVerilog(new Soc(args(0)), Array("--target-dir", "generated"))
}
