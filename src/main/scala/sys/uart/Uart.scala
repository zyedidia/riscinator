package rtor.sys.uart

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

class UartIO(width: Int) extends Bundle {
  val serial = Input(UInt(1.W))
  val channel = DecoupledIO(UInt(width.W))
}

object State extends ChiselEnum {
  val idle, start, data, stop = Value
}

class Uart(nfifo: Int, oversample: Int) extends Module {
  val t = UInt(8.W)
  val dvsrw = 11

  val io = IO(new Bundle{
    val rd = Input(Bool())
    val wr = Input(Bool())
    val wdata = Input(t)
    val dvsr = Input(UInt(dvsrw.W))
    val tx_full = Output(Bool())
    val rx_empty = Output(Bool())
    val rdata = Output(t)

    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val baud = Module(new Baud(dvsrw))
  baud.io.dvsr := io.dvsr
}
