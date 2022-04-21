package rvcpu.core

import chisel3._
import chisel3.util._

class FetchIO(xlen: Int) extends Bundle {
  val imem = new ImemIO(xlen, xlen)
}

class Fetch(xlen: Int) extends Module {
  val io = IO(new FetchIO(xlen))
}
