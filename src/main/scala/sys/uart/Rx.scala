package rtor.sys.uart

import chisel3._
import chisel3.util._

// Uart receiver parameterized by oversampling rate and data width
class Rx[T <: Bits](gen: T, oversample: Int) extends Module {
  val io = IO(new Bundle{
    val ctrl = new UartCtrlIO()
    val data = new UartDataIO(gen)
  })

  val width = gen.getWidth

  val state = RegInit(State.idle)
  val data = RegInit(0.U(width.W))
  val tickC = RegInit(0.U(log2Ceil(oversample).W))
  val dataC = RegInit(0.U(log2Ceil(width).W))

  io.data.bits := data

  io.ctrl.done := false.B
  switch (state) {
    is (State.idle) {
      when (io.data.serial === 0.U) {
        state := State.start
        tickC := 0.U
      }
    }
    is (State.start) {
      when (io.ctrl.tick) {
        when (tickC === (oversample / 2 - 1).U) {
          tickC := 0.U
          state := State.data
        } .otherwise {
          tickC := tickC + 1.U
        }
      }
    }
    is (State.data) {
      when (io.ctrl.tick) {
        when (tickC === (oversample - 1).U) {
          tickC := 0.U
          data := io.data.serial ## data(7, 1)

          when (dataC === (width - 1).U) {
            state := State.stop
          } .otherwise {
            dataC := dataC + 1.U
          }
        } .otherwise {
          tickC := tickC + 1.U
        }
      }
    }
    is (State.stop) {
      when (io.ctrl.tick) {
        when (tickC === (oversample - 1).U) {
          state := State.idle
          io.ctrl.done := true.B
        } .otherwise {
          tickC := tickC + 1.U
        }
      }
    }
  }
}
