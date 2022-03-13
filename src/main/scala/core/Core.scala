package rvcpu.core

import chisel3._
import chisel3.util._

case class Config(
  xlen: Int,
  bootAddr: UInt
)

class Core(conf: Config) extends Module {
  val io = IO(new Bundle{
    val imem = new bus.InstIO(conf.xlen, conf.xlen)
    val dmem = new bus.DataIO(conf.xlen, conf.xlen)
  })

  val dpath = Module(new Datapath(conf))
  val ctrl = Module(new Control)

  dpath.io.dmem <> io.dmem
  dpath.io.imem <> io.imem
  dpath.io.ctrl <> ctrl.io
}
