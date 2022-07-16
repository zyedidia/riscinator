package rvcpu.core

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

object AluOp extends ChiselEnum {
  val add, sub, and, or, xor, slt, sll, sltu, srl, sra, copyA, copyB, none = Value
}

class AluIO(width: Int) extends Bundle {
  val a = Input(UInt(width.W))
  val b = Input(UInt(width.W))
  val op = Input(AluOp())
  val out = Output(UInt(width.W))
}

// TODO: Alu can be optimized
class Alu(width: Int) extends Module {
  val io = IO(new AluIO(width))

  val shamt = io.b(4, 0)

  io.out := io.a

  val b = WireInit(io.b)
  when(io.op === AluOp.sub) {
    b := -io.b
  }
  val sum = WireInit(io.a + b)
  switch(io.op) {
    is(AluOp.add)   { io.out := sum }
    is(AluOp.sub)   { io.out := sum }
    is(AluOp.and)   { io.out := io.a & io.b }
    is(AluOp.or)    { io.out := io.a | io.b }
    is(AluOp.xor)   { io.out := io.a ^ io.b }
    is(AluOp.slt)   { io.out := io.a.asSInt < io.b.asSInt }
    is(AluOp.sll)   { io.out := io.a << shamt }
    is(AluOp.sltu)  { io.out := io.a < io.b }
    is(AluOp.srl)   { io.out := io.a >> shamt }
    is(AluOp.sra)   { io.out := (io.a.asSInt >> shamt).asUInt }
    is(AluOp.copyB) { io.out := io.b }
  }
}
