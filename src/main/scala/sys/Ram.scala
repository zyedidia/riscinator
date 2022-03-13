package rvcpu.sys

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline

// class Ram(size: Int, addrw: Int, dataw: Int, memfile: String = "") extends Module {
//   val io = IO(new Bundle{
//     val imem = Flipped(new ImemIO(addrw, dataw))
//     val dmem = Flipped(new DmemIO(addrw, dataw))
//   })
//
//   val MemType = Vec(dataw / 8, UInt(8.W))
//
//   val mem = SyncReadMem(size, MemType)
//   if (memfile.trim().nonEmpty) {
//     loadMemoryFromFileInline(mem, memfile)
//   }
//
//   val irvalid = RegNext(io.imem.req)
//   io.imem.rvalid := irvalid
//
//   val drvalid = RegNext(io.imem.req)
//   io.dmem.rvalid := drvalid
//
//   val iaddr = io.imem.addr >> 2
//   val daddr = io.imem.addr >> 2
//   io.imem.rdata := mem.read(iaddr, io.imem.req).asTypeOf(UInt(dataw.W))
//   io.dmem.rdata := mem.read(daddr, io.dmem.req).asTypeOf(UInt(dataw.W))
//
//   when (io.dmem.req && io.dmem.we) {
//     mem.write(daddr, io.dmem.wdata.asTypeOf(MemType), io.dmem.be.asBools)
//   }
// }

class Ram(size: Int, addrw: Int, dataw: Int, memfile: String = "") extends Module {
  val io = IO(new Bundle{
    val imem = Flipped(new bus.InstIO(addrw, dataw))
    val dmem = Flipped(new bus.DataIO(addrw, dataw))
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

  val iaddr = io.imem.addr >> 2
  val daddr = io.dmem.addr >> 2
  io.imem.rdata := mem.read(iaddr, io.imem.req)
  io.dmem.rdata := mem.read(daddr, io.dmem.req)

  when (io.dmem.req && io.dmem.we) {
    mem.write(daddr, io.dmem.wdata)
  }
}
