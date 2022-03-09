package rvcpu

import chisel3._
import chisel3.util._

class Alu(width: Int) extends Module {
  private val opw = 3

  val OpAdd  = "b000".U(opw.W)
  val OpShl  = "b001".U(opw.W)
  val OpSlt  = "b010".U(opw.W)
  val OpSltu = "b011".U(opw.W)
  val OpXor  = "b100".U(opw.W)
  val OpShr  = "b101".U(opw.W)
  val OpOr   = "b110".U(opw.W)
  val OpAnd  = "b111".U(opw.W)

  val io = IO(new Bundle {
    val a = Input(UInt(width.W))
    val b = Input(UInt(width.W))
    val op = Input(UInt(opw.W))
    val shift_arith = Input(Bool())
    val out = Output(UInt(width.W))
  })

  val shamt = io.b(4, 0)

  io.out := 0.U(width.W)

  switch (io.op) {
    is (OpAdd) {
      io.out := io.a + io.b
    }
    is (OpShl) {
      io.out := io.a << shamt
    }
    is (OpShr) {
      when (io.shift_arith) {
        io.out := (io.a.asSInt >> shamt).asUInt
      } .otherwise {
        io.out := io.a >> shamt
      }
    }
    is (OpSlt) {
      io.out := io.a.asSInt < io.b.asSInt
    }
    is (OpSltu) {
      io.out := io.a < io.b
    }
    is (OpXor) {
      io.out := io.a ^ io.b
    }
    is (OpOr) {
      io.out := io.a | io.b
    }
    is (OpAnd) {
      io.out := io.a & io.b
    }
  }
}
