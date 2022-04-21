package rvcpu.core

import chisel3._
import chisel3.util._

class MemoryCtrlIO extends Bundle {
  val ld_type = Input(LdType())
  val st_type = Input(StType())
}

class MemoryDataIO(xlen: Int) extends Bundle {
  val alu_out = Input(UInt(xlen.W))
  val rs2 = Input(UInt(xlen.W))
  
  val stall = Output(Bool())
}

class MemoryIO(xlen: Int) extends Bundle {
  val ctrl = new MemoryCtrlIO()
  val data = new MemoryDataIO(xlen)
  val dmem = new DmemIO(xlen, xlen)
}

class Memory(xlen: Int) extends Module {
  val io = IO(new MemoryIO(xlen))

  val daddr = io.data.alu_out & ~(0x3.U(xlen.W))
  val woffset = (io.data.alu_out(1) << 4.U).asUInt | (io.data.alu_out(0) << 3.U).asUInt

  val dmem_rd_req = io.ctrl.ld_type =/= LdType.none
  val dmem_wr_req = io.ctrl.st_type =/= StType.none

  io.dmem.req := dmem_rd_req || dmem_wr_req
  io.dmem.we := dmem_wr_req
  io.dmem.addr := daddr
  io.dmem.wdata := io.data.rs2 << woffset
  // TODO: stall if not rvalid
  io.data.stall := dmem_wr_req && !io.dmem.gnt || dmem_rd_req && !io.dmem.gnt

  io.dmem.be := "b1111".U
  switch (io.ctrl.st_type) {
    is (StType.sh) { io.dmem.be := "b11".U << io.data.alu_out(1, 0) }
    is (StType.sb) { io.dmem.be := "b1".U << io.data.alu_out(1, 0) }
  }
}
