package rtor.core

import chisel3._
import chisel3.util._

class RegFileIO(size: Int, width: Int) extends Bundle {
  val addrw = log2Ceil(size)

  val wen = Input(Bool())
  val raddr1 = Input(UInt(addrw.W))
  val raddr2 = Input(UInt(addrw.W))
  val waddr = Input(UInt(addrw.W))
  val wdata = Input(UInt(width.W))
  val rdata1 = Output(UInt(width.W))
  val rdata2 = Output(UInt(width.W))
}

class RegFile(size: Int, width: Int) extends Module {
  val io = IO(new RegFileIO(size, width))

  val regs = Mem(size, UInt(width.W))

  io.rdata1 := regs(io.raddr1)
  io.rdata2 := regs(io.raddr2)

  when(io.wen && io.waddr =/= 0.U(width.W)) {
    regs(io.waddr) := io.wdata
  }
}
