package rtor.sys.uart

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

import rtor.lib.fifo.Fifo

class UartDataIO[T <: Bits](gen: T) extends Bundle {
  val serial = Input(UInt(1.W))
  val bits = Output(gen)
}

class UartCtrlIO extends Bundle {
  val tick = Input(Bool())
  val done = Output(Bool())
}

object State extends ChiselEnum {
  val idle, start, data, stop = Value
}

class UartBlock[T <: Bits](gen: T, dvsrw: Int, fifow: Int, oversample: Int) extends Module {
  val io = IO(new Bundle{
    val dvsr = Input(UInt(dvsrw.W))
    val tx_full = Output(Bool())
    val rx_empty = Output(Bool())

    val rd = Input(Bool())
    val wr = Input(Bool())

    val rx = new UartDataIO(gen)
    val tx = Flipped(new UartDataIO(gen))
  })

  val baud = Module(new Baud(dvsrw))
  baud.io.dvsr := io.dvsr

  val rxer = Module(new Rx(gen, oversample))
  rxer.io.data.serial := io.rx.serial
  rxer.io.ctrl.tick := baud.io.tick

  val txer = Module(new Tx(gen, oversample))
  io.tx.serial := txer.io.data.serial
  txer.io.ctrl.tick := baud.io.tick

  val fifo_rx = Module(new Fifo(gen, fifow))
  fifo_rx.io.rd := io.rd
  fifo_rx.io.wr := rxer.io.ctrl.done
  fifo_rx.io.wdata := rxer.io.data.bits
  io.rx_empty := fifo_rx.io.empty
  io.rx.bits := fifo_rx.io.rdata

  val fifo_tx = Module(new Fifo(gen, fifow))
  fifo_tx.io.rd := txer.io.ctrl.done
  fifo_tx.io.wr := io.wr
  fifo_tx.io.wdata := io.tx.bits
  io.tx_full := fifo_tx.io.full

  txer.io.start := ~fifo_tx.io.empty
  txer.io.data.bits := fifo_tx.io.rdata
}
