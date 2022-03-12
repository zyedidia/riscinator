package rvcpu

import chisel3._
import chisel3.util._

case class Config(
  xlen: Int,
  bootAddr: UInt
)

class ImemIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val addr = Output(UInt(addrw.W))
  val rdata = Input(UInt(dataw.W))
  val err = Input(Bool())
}

class DmemIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val we = Output(Bool())
  val be = Output(UInt((dataw / 8).W))
  val addr = Output(UInt(addrw.W))
  val wdata = Output(UInt(dataw.W))
  val rdata = Input(UInt(dataw.W))
  val err = Input(Bool())
}

class Core(xlen: Int) extends Module {
  val io = IO(new Bundle{
    val imem = new ImemIO(xlen, xlen)
    val dmem = new DmemIO(xlen, xlen)
  })
}
