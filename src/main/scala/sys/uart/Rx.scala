package rtor.sys.uart

import chisel3._
import chisel3.util._

// Uart receiver parameterized by oversampling rate and data width
class Rx(oversample: Int, width: Int) extends Module {
  val io = IO(new Bundle{
    val tick = Input(Bool())
    val uart = new UartIO(width)
  })

  val state = RegInit(State.idle)
  val data = RegInit(0.U(width.W))
  val tickC = RegInit(0.U(log2Ceil(oversample).W))
  val dataC = RegInit(0.U(log2Ceil(width).W))

  io.uart.channel.valid := false.B
  io.uart.channel.bits := data

  switch (state) {
    is (State.idle) {
      when (io.uart.serial === 0.U) {
        state := State.start
        tickC := 0.U
      }
    }
    is (State.start) {
      when (io.tick) {
        when (tickC === (oversample / 2 - 1).U) {
          tickC := 0.U
          state := State.data
        } .otherwise {
          tickC := tickC + 1.U
        }
      }
    }
    is (State.data) {
      when (io.tick) {
        when (tickC === (oversample - 1).U) {
          tickC := 0.U
          data := io.uart.serial ## data(7, 1)

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
      io.uart.channel.valid := true.B
      when (io.tick) {
        when (tickC === (oversample - 1).U) {
          state := State.idle
        } .otherwise {
          tickC := tickC + 1.U
        }
      }
    }
  }
}
