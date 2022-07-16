package rtor.sys.ram

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.experimental.{annotate, ChiselAnnotation}
import firrtl.annotations.MemorySynthInit

import rtor.bus._

class Ram(offset: Int, size: Int, addrw: Int, dataw: Int, memfile: String = "") extends Module {
  val io = IO(new Bundle{
    val imem = Flipped(new RoIO(addrw, dataw))
    val dmem = Flipped(new RwIO(addrw, dataw))
  })

  annotate(new ChiselAnnotation {
    override def toFirrtl =
      MemorySynthInit
  })

  io.imem.err := false.B
  io.dmem.err := false.B
  io.imem.gnt := io.imem.req
  io.dmem.gnt := io.dmem.req

  val mem = SyncReadMem(size, UInt(dataw.W))
  if (memfile.trim().nonEmpty) {
    loadMemoryFromFileInline(mem, memfile)
  }

  val irvalid = RegNext(io.imem.req)
  io.imem.rvalid := irvalid

  val drvalid = RegNext(io.dmem.req)
  io.dmem.rvalid := drvalid

  val iaddr = io.imem.addr(offset-1, log2Ceil(dataw / 8))
  val daddr = io.dmem.addr(offset-1, log2Ceil(dataw / 8))
  io.imem.rdata := mem.read(iaddr, io.imem.req)
  io.dmem.rdata := mem.read(daddr, io.dmem.req)

  val write = (0 until (dataw / 8)).foldLeft(0.U(dataw.W)) { (write, i) =>
    write |
    (Mux(
      io.dmem.req && io.dmem.be(i),
      io.dmem.wdata,
      mem(daddr)
    )(8 * (i + 1) - 1, 8 * i) << (8 * i).U).asUInt
  }

  when (io.dmem.req && io.dmem.we) {
    mem.write(daddr, write)
  }
}
