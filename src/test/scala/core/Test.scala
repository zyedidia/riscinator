package test.core

import chisel3._
import chisel3.testers._
import chiseltest._
import chiseltest.iotesters._
import org.scalatest.flatspec.AnyFlatSpec

import rvcpu.core._
import rvcpu.sys.ram._

class BasicCore(memFile: String) extends BasicTester {
  val xlen = 32
  val ramsz = 1024
  val core = Module(new Core(Config(xlen, 0.U(xlen.W))))
  val ram = Module(new Ram(xlen, ramsz / 4, xlen, xlen, memFile))

  // Haven't found a way to inspect the memory inside Ram, so instead
  // we make another memory here that records all the writes
  val mem = SyncReadMem(ramsz / 4, UInt(xlen.W))

  val daddr = ram.io.dmem.addr(xlen-1, 2)

  // if I don't have this it gives an error
  val rd = IO(Output(UInt(xlen.W)))
  rd := mem(daddr)

  val write = (0 until (xlen / 8)).foldLeft(0.U(xlen.W)) { (write, i) =>
    write |
    (Mux(
      ram.io.dmem.req && ram.io.dmem.be(i),
      ram.io.dmem.wdata,
      mem(daddr)
    )(8 * (i + 1) - 1, 8 * i) << (8 * i).U).asUInt
  }

  when (ram.io.dmem.req && ram.io.dmem.we) {
    mem.write(daddr, write)
  }

  core.io.imem <> ram.io.imem
  core.io.dmem <> ram.io.dmem
}

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  "riscvtest" should "pass" in {
    test(
      new BasicCore("./mem/riscvtest.hex")
    ).runPeekPoke(new PeekPokeTester(_) {
      for (i <- 1 to 100) {
        step(1)
      }
      assert(peekAt(dut.mem, 24) == 7)
      assert(peekAt(dut.mem, 25) == 25)
    })
  }
}
