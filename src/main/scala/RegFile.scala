package rvcpu

import chisel3._
import chisel3.util._

class RegFile(size: Int, width: Int) extends Module {
  private val addrw = log2Ceil(size)

  val io = IO(new Bundle {
    val wen = Input(Bool())
    val raddr0 = Input(UInt(addrw.W))
    val raddr1 = Input(UInt(addrw.W))
    val waddr = Input(UInt(addrw.W))
    val wdata = Input(UInt(width.W))
    val rdata0 = Output(UInt(width.W))
    val rdata1 = Output(UInt(width.W))
  })

  val regs = Mem(size, UInt(width.W))

  when (io.raddr0 === 0.U(width.W)) {
    io.rdata0 := 0.U(width.W)
  } .otherwise {
    io.rdata0 := regs(io.raddr0)
  }

  when (io.raddr1 === 0.U(width.W)) {
    io.rdata1 := 0.U(width.W)
  } .otherwise {
    io.rdata1 := regs(io.raddr1)
  }

  when (io.wen && io.waddr =/= 0.U(width.W)) {
    regs(io.waddr) := io.wdata
  }
}
