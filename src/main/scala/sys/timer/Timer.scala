package rvcpu.sys

import chisel3._
import chisel3.util._

import rvcpu.bus._

class Timer(addrw: Int, dataw: Int) extends Module {
  val io = IO(new Bundle{
    val bus = Flipped(new RwIO(addrw, dataw))

    val intr = Output(Bool())
  })
}
