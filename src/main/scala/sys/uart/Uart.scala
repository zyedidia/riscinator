package rtor.sys.uart

import chisel3._
import chisel3.util._

import rtor.bus._

object RegMap {
  val txdata = 0
  val rxdata = 4
  val dvsr = 8
  val clear = 12
}

class Uart(offset: Int, fifow: Int, addrw: Int, dataw: Int) extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new RwIO(addrw, dataw))
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  io.bus.gnt := io.bus.req

  val uart = Module(new UartBlock(UInt(8.W), 11, fifow, 16))
  uart.io.rx.serial := io.rx
  io.tx := uart.io.tx.serial

  val we = io.bus.req && io.bus.we

  def wReg(addr: UInt, width: Int): UInt = {
    val reg = RegInit(0.U(width.W))
    when(we && (io.bus.addr(offset, 0) === addr)) {
      reg := io.bus.wdata
    }
    return reg
  }

  val dvsr = wReg(RegMap.dvsr.U, 11)
  val addr = io.bus.addr(offset, 0)

  val rdata = WireDefault(0.U(dataw.W))
  switch(addr) {
    is(RegMap.rxdata.U) {
      rdata := Cat(0.U(22.W), uart.io.tx_full, uart.io.rx_empty, uart.io.rx.bits)
    }
    is(RegMap.dvsr.U) {
      rdata := dvsr
    }
  }

  io.bus.rvalid := RegNext(io.bus.req)
  io.bus.rdata := RegNext(rdata)
  io.bus.err := false.B

  uart.io.rd := we && (io.bus.addr(offset, 0) === RegMap.clear.U)
  uart.io.wr := we && (io.bus.addr(offset, 0) === RegMap.txdata.U)
  uart.io.tx.bits := io.bus.wdata
  uart.io.dvsr := dvsr
}
