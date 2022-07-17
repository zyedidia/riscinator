package rtor.sys.uart

import chisel3._
import chisel3.util._

class Baud(width: Int) extends Module {
  val io = IO(new Bundle{
    val dvsr = Input(UInt(width.W))
    val tick = Output(Bool())
  })
  val count = RegInit(0.U(width.W))
  count := Mux(count === io.dvsr, 0.U, count + 1.U)
  io.tick := count === 1.U
}
