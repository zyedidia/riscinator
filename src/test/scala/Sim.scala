package test

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SocSim extends AnyFlatSpec with ChiselScalatestTester {
  "Soc" should "simulate" in {
    test(new rvcpu.Soc).withAnnotations(Seq(treadle.WriteVcdAnnotation, treadle.MemoryToVCD("all"))) { dut =>
      for (i <- 1 to 100) {
        dut.clock.step()
      }
    }
  }
}
