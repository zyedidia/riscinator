package test

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SocTest extends AnyFlatSpec with ChiselScalatestTester {
  "Soc test" should "pass" in {
    test(new rvcpu.Soc) { dut =>
      // ...
    }
  }
}
