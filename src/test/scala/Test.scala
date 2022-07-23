package test

import chisel3._
import chiseltest._
import chiseltest.iotesters._
import org.scalatest.flatspec.AnyFlatSpec

class SocTest extends AnyFlatSpec with ChiselScalatestTester {
  "riscvtest" should "pass" in {
    test(new rtor.Soc("./tests/riscvtest.mem")).runPeekPoke(new PeekPokeTester(_) {
      for (i <- 1 to 1000) {
        step(1)
      }

      var regs = Array.fill[Int](32)(0)
      regs(2) = 25
      regs(3) = 1048644
      regs(4) = 1
      regs(5) = 11
      regs(7) = 7
      regs(9) = 18

      for (i <- 0 until 32) {
        assert(regs(i) == peekAt(dut.core.rf.regs, i), s"error: reg $i expected ${regs(i)}")
      }

      assert(peekAt(dut.ram.mem, 24) == 7)
      assert(peekAt(dut.ram.mem, 25) == 25)
    })
  }

  "itype" should "pass" in {
    test(new rtor.Soc("./tests/itype.mem")).runPeekPoke(new PeekPokeTester(_) {
      for (i <- 1 to 1000) {
        step(1)
      }

      var regs = Array.fill[Long](32)(0)
      regs(1) = 63
      regs(2) = 3
      regs(3) = 67
      regs(4) = 3
      regs(5) = 4294967295L
      regs(7) = 1
      regs(8) = 0x100004
      regs(9) = 63
      regs(10) = 3
      regs(11) = 67

      for (i <- 0 until 32) {
        assert(regs(i) == peekAt(dut.core.rf.regs, i), s"error: reg $i expected ${regs(i)}")
      }

      assert(peekAt(dut.ram.mem, 0) == 3)
      assert(peekAt(dut.ram.mem, 1) == 63)
      assert(peekAt(dut.ram.mem, 2) == 67)
    })
  }

  "jmps" should "pass" in {
    test(new rtor.Soc("./tests/jmps.mem")).runPeekPoke(new PeekPokeTester(_) {
      for (i <- 1 to 1000) {
        step(1)
      }

      var regs = Array.fill[Long](32)(0)
      regs(1) = 0
      regs(2) = 3
      regs(3) = 67
      regs(4) = 3
      regs(5) = 4294967295L
      regs(7) = 1
      regs(8) = 0x100004
      regs(9) = 63
      regs(10) = 3
      regs(11) = 67
      regs(13) = 1048688
      regs(17) = 1
      regs(19) = 1
      regs(20) = 1
      regs(21) = 1
      regs(22) = 1
      regs(23) = 1
      regs(25) = 1
      regs(26) = 63

      for (i <- 0 until 32) {
        assert(regs(i) == peekAt(dut.core.rf.regs, i), s"error: reg $i expected ${regs(i)}")
      }

      assert(peekAt(dut.ram.mem, 0) == 3)
      assert(peekAt(dut.ram.mem, 1) == 63)
      assert(peekAt(dut.ram.mem, 2) == 67)
    })
  }
}

class SocSim extends AnyFlatSpec with ChiselScalatestTester {
  "Soc" should "simulate" in {
    test(
      new rtor.Soc("./sw/hello/hello.mem")
    ).withAnnotations(
      Seq(
        treadle.WriteVcdAnnotation,
        treadle.MemoryToVCD("all")
      )
    ) { dut =>
      for (i <- 1 to 5) {
        dut.io.gpi(0).poke(i % 2 == 0)
        for (j <- 1 to 1000) {
          dut.clock.step()
        }
      }
    }
  }
}
