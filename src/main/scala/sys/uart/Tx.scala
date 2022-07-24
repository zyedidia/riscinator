package rtor.sys.uart

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// Uart transmitter parameterized by oversampling rate and data width
class Tx[T <: Bits](gen: T, oversample: Int) extends Module {
  val io = IO(new Bundle {
    val start = Input(Bool())
    val ctrl = new UartCtrlIO()
    val data = Flipped(new UartDataIO(gen))
  })

  val width = gen.getWidth

  val state = RegInit(State.idle)
  val data = RegInit(0.U(width.W))
  val tickC = RegInit(0.U(log2Ceil(oversample).W))
  val dataC = RegInit(0.U(log2Ceil(width).W))
  val tx = RegInit(0.U(1.W))

  io.data.serial := tx

  io.ctrl.done := false.B
  switch(state) {
    is(State.idle) {
      tx := 1.U
      when(io.start) {
        state := State.start
        tickC := 0.U
        data := io.data.bits
      }
    }
    is(State.start) {
      tx := 0.U
      when(io.ctrl.tick) {
        when(tickC === (oversample - 1).U) {
          state := State.data
          tickC := 0.U
          dataC := 0.U
        }.otherwise {
          tickC := tickC + 1.U
        }
      }
    }
    is(State.data) {
      tx := data(0)
      when(io.ctrl.tick) {
        when(tickC === (oversample - 1).U) {
          tickC := 0.U
          data := data >> 1.U
          when(dataC === (width - 1).U) {
            state := State.stop
          }.otherwise {
            dataC := dataC + 1.U
          }
        }.otherwise {
          tickC := tickC + 1.U
        }
      }
    }
    is(State.stop) {
      tx := 1.U
      when(io.ctrl.tick) {
        when(tickC === (oversample - 1).U) {
          state := State.idle
          io.ctrl.done := true.B
        }.otherwise {
          tickC := tickC + 1.U
        }
      }
    }
  }
}
