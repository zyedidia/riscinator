package rvcpu.core

import chisel3._
import chisel3.util._

class DatapathIO(xlen: Int) extends Bundle {
  val imem = new ImemIO(xlen, xlen)
  val dmem = new DmemIO(xlen, xlen)
  val ctrl = Flipped(new ControlIO)
}

class Datapath(conf: Config) extends Module {
  val io = IO(new DatapathIO(conf.xlen))

  val alu = Module(new Alu(conf.xlen))
  val rf = Module(new RegFile(32, conf.xlen))
  val brCond = Module(new BrCond(conf.xlen))
  val immExt = Module(new ImmExtract(conf.xlen))

  // Pipeline registers

  // Registers between Fetch/Execute
  val fe_inst = RegInit(Instructions.NOP)
  val fe_pc = Reg(UInt())

  // Registers between Execute/WriteBack
  val ew_inst = RegInit(Instructions.NOP)
  val ew_pc = Reg(UInt())
  val ew_alu = Reg(UInt())

  // Registers to track control signals
  val st_type = Reg(StType())
  val ld_type = Reg(LdType())
  val wb_sel = Reg(WbSel())
  val wb_en = Reg(Bool())
  val illegal = Reg(Bool())
  val pc_check = Reg(Bool())

  import Control._

  // Fetch stage

  val started = RegNext(reset.asBool)
  val stall = !io.imem.rvalid || !io.dmem.rvalid
  val pc = RegInit(conf.bootAddr - 4.U(conf.xlen.W))
  val next_pc = Wire(UInt(conf.xlen.W))
  pc := next_pc

  when (stall) {
    next_pc := pc
  } .elsewhen (io.ctrl.pc_sel === PcSel.alu || brCond.io.taken) {
    next_pc := alu.io.sum & ~0x1.U
  } .elsewhen (io.ctrl.pc_sel === PcSel.plus0) {
    next_pc := pc
  } .otherwise {
    next_pc := pc + 4.U
  }

  val inst = Wire(UInt(32.W))
  inst := io.imem.rdata
  when (started || io.ctrl.inst_kill || brCond.io.taken) {
    inst := Instructions.NOP
  }

  io.imem.addr := next_pc
  io.imem.req := !stall

  when (!stall) {
    fe_pc := pc
    fe_inst := inst
  }

  // Execute stage

  io.ctrl.inst := fe_inst

  val rd = fe_inst(11, 7)
  val rs1_addr = fe_inst(19, 15)
  val rs2_addr = fe_inst(24, 20)

  rf.io.raddr1 := rs1_addr
  rf.io.raddr2 := rs2_addr

  immExt.io.inst := fe_inst
  immExt.io.sel := io.ctrl.imm_sel

  // hazard forwarding
  val wb_rd_addr = ew_inst(11, 7)
  // if an instruction tries to read from rs1/rs2 before write back, forward it directly
  val rs1hzd = wb_en && rs1_addr =/= 0.U && rs1_addr === wb_rd_addr
  val rs2hzd = wb_en && rs2_addr =/= 0.U && rs2_addr === wb_rd_addr
  // if hazard, grab the value to be written from the alu, otherwise just read from rf
  val rs1 = Mux(wb_sel === WbSel.alu && rs1hzd, ew_alu, rf.io.rdata1)
  val rs2 = Mux(wb_sel === WbSel.alu && rs2hzd, ew_alu, rf.io.rdata2)

  alu.io.a := Mux(io.ctrl.a_sel === ASel.rs1, rs1, fe_pc)
  alu.io.b := Mux(io.ctrl.b_sel === BSel.rs2, rs2, immExt.io.out)
  alu.io.op := io.ctrl.alu_op

  brCond.io.rs1 := rs1
  brCond.io.rs2 := rs2
  brCond.io.br_type := io.ctrl.br_type

  val daddr = Mux(stall, ew_alu, alu.io.sum) & ~0x3.U
  val woffset = (alu.io.sum(1) << 4.U).asUInt | (alu.io.sum(0) << 3.U).asUInt
  io.dmem.req := !stall && (io.ctrl.st_type =/= StType.none || io.ctrl.ld_type =/= LdType.none)
  io.dmem.we := !stall && io.ctrl.st_type =/= StType.none
  io.dmem.addr := daddr
  io.dmem.wdata := rs2 << woffset

  io.dmem.be := 0.U
  switch (Mux(stall, st_type, io.ctrl.st_type)) {
    is (StType.sw) { io.dmem.be := "b1111".U }
    is (StType.sh) { io.dmem.be := "b11".U << alu.io.sum(1, 0) }
    is (StType.sb) { io.dmem.be := "b1".U << alu.io.sum(1, 0)  }
  }

  when (!stall) {
    ew_pc := fe_pc
    ew_inst := fe_inst
    ew_alu := alu.io.out
    st_type := io.ctrl.st_type
    ld_type := io.ctrl.ld_type
    wb_sel := io.ctrl.wb_sel
    wb_en := io.ctrl.wb_en
    illegal := io.ctrl.illegal
    pc_check := io.ctrl.pc_sel === PcSel.alu
  }

  val ldoff = (ew_alu(1) << 4.U).asUInt | (ew_alu(0) << 3.U).asUInt
  val ldshift = io.dmem.rdata >> ldoff
  val ld = Wire(SInt(conf.xlen.W))

  ld := io.dmem.rdata.zext
  switch (ld_type) {
    is (LdType.lh)  { ld := ldshift(15, 0).asSInt }
    is (LdType.lb)  { ld := ldshift(7, 0).asSInt }
    is (LdType.lhu) { ld := ldshift(15, 0).zext }
    is (LdType.lbu) { ld := ldshift(7, 0).zext }
  }

  // Writeback stage
  val regwr = Wire(UInt(conf.xlen.W))

  regwr := ew_alu.zext.asUInt
  switch (wb_sel) {
    is (WbSel.mem) { regwr := ld.asUInt }
    is (WbSel.pc4) { regwr := ew_pc + 4.U }
  }

  rf.io.wen := wb_en && !stall
  rf.io.waddr := wb_rd_addr
  rf.io.wdata := regwr
}
