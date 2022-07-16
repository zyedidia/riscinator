package rvcpu.sys.timer

import chisel3._
import chisel3.util._

import rvcpu.bus._

object RegMap {
  val mtime_low = 0
  val mtime_high = 4
  val mtimecmp_low = 8
  val mtimecmp_high = 12
  val cycle_timer = 16
}

class Timer(offset: Int, addrw: Int, dataw: Int) extends Module {
  val io = IO(new Bundle{
    val bus = Flipped(new RwIO(addrw, dataw))

    val intr = Output(Bool())
  })

  val cyc_timer = RegInit(0.U(32.W))
  cyc_timer := cyc_timer + 1.U
  val addr = RegNext(io.bus.addr(offset, 0))

  io.bus.err := true.B
  io.bus.rdata := 0.U
  switch (addr) {
    is (RegMap.cycle_timer.U) {
      io.bus.err := false.B
      io.bus.rdata := cyc_timer
    }
  }

  io.bus.rvalid := RegNext(io.bus.req)
  io.bus.gnt := io.bus.req
  io.intr := false.B
}
