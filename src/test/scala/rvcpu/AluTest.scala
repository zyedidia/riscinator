package rvcpu

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AluTester extends AnyFlatSpec with ChiselScalatestTester {
  "ALU" should "pass" in {
    test(new Alu(32)) { alu => 
      val a = 42
      val b = 42
      val op = alu.OpAdd
      alu.io.a.poke(a)
      alu.io.b.poke(b)
      alu.io.shift_arith.poke(false.B)
      alu.io.op.poke(op)

      alu.io.out.expect(a + b)
    }
  }
}
