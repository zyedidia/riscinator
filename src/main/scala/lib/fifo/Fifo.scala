package rvcpu.lib.fifo

import chisel3._
import chisel3.util._

import scala.math.pow

class FifoIO[T <: Data](t: T) extends Bundle {
  val rd = Input(Bool())
  val wr = Input(Bool())
  val wdata = Input(t)
  val empty = Output(Bool())
  val full = Output(Bool())
  val rdata = Output(t)
}

class Fifo[T <: Data](t: T, addrw: Int) extends Module {
  class FifoRegFile[T <: Data](t: T, addrw: Int) extends Module {
    val io = IO(new Bundle{
      val wen = Input(Bool())
      val waddr = Input(UInt(addrw.W))
      val raddr = Input(UInt(addrw.W))
      val wdata = Input(t)
      val rdata = Output(t)
    })

    val regs = Mem(pow(2, addrw).toInt, t)
    io.rdata := regs(io.raddr)
    when (io.wen) {
      regs(io.waddr) := io.wdata
    }
  }

  class FifoCtrl(addrw: Int) extends Module {
    val io = IO(new Bundle{
      val rd = Input(Bool())
      val wr = Input(Bool())
      val empty = Output(Bool())
      val full = Output(Bool())
      val waddr = Output(UInt(addrw.W))
      val raddr = Output(UInt(addrw.W))
    })

    val w_ptr = RegInit(0.U)
    val r_ptr = RegInit(0.U)
    val full = RegInit(false.B)
    val empty = RegInit(true.B)

    switch (io.wr ## io.rd) {
      is ("b01".U) {
        when (!empty) {
          when (r_ptr + 1.U === w_ptr) {
            empty := true.B
          }
          r_ptr := r_ptr + 1.U
          full := false.B
        }
      }
      is ("b10".U) {
        when (!full) {
          when (w_ptr === r_ptr) {
            full := true.B
          }
          w_ptr := w_ptr + 1.U
          empty := false.B
        }
      }
      is ("b11".U) {
        w_ptr := w_ptr + 1.U
        r_ptr := r_ptr + 1.U
      }
    }

    io.waddr := w_ptr
    io.raddr := r_ptr
    io.full := full
    io.empty := empty
  }

  val io = IO(new FifoIO(t))

  val ctrl = Module(new FifoCtrl(addrw))
  ctrl.io.rd := io.rd
  ctrl.io.wr := io.wr
  io.empty := ctrl.io.empty
  io.full := ctrl.io.full

  val rf = Module(new FifoRegFile(t, addrw))
  rf.io.wdata := io.wdata
  io.rdata := rf.io.rdata

  rf.io.waddr := ctrl.io.waddr
  rf.io.raddr := ctrl.io.raddr
  rf.io.wen := io.wr && !ctrl.io.full
}
