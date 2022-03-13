package rvcpu.core

import chisel3._
import chisel3.util._

case class Config(
  xlen: Int,
  bootAddr: UInt
)

class ImemIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val addr = Output(UInt(addrw.W))
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val err = Input(Bool())
  val rdata = Input(UInt(dataw.W))
}

class DmemIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val addr = Output(UInt(addrw.W))
  val we = Output(Bool())
  val be = Output(UInt((dataw / 8).W))
  val wdata = Output(UInt(dataw.W))
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val err = Input(Bool())
  val rdata = Input(UInt(dataw.W))
}

class Core(conf: Config) extends Module {
  val io = IO(new Bundle{
    val imem = new ImemIO(conf.xlen, conf.xlen)
    val dmem = new DmemIO(conf.xlen, conf.xlen)
  })

  val dpath = Module(new Datapath(conf))
  val ctrl = Module(new Control)

  dpath.io.dmem <> io.dmem
  dpath.io.imem <> io.imem
  dpath.io.ctrl <> ctrl.io
}
