package rvcpu.core

import chisel3._
import chisel3.util._

case class Config(
  xlen:     Int,
  bootAddr: UInt)

class ImemIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val addr = Output(UInt(addrw.W))
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val err = Input(Bool())
  val rdata = Input(UInt(dataw.W))
}

class DmemIO(addrw: Int, dataw: Int) extends Bundle {
  val req = Output(Bool())
  val addr = Output(UInt(addrw.W))
  val we = Output(Bool())
  val be = Output(UInt((dataw / 8).W))
  val wdata = Output(UInt(dataw.W))
  val gnt = Input(Bool())
  val rvalid = Input(Bool())
  val err = Input(Bool())
  val rdata = Input(UInt(dataw.W))
}

class CoreIO(xlen: Int) extends Bundle {
  val imem = new ImemIO(xlen, xlen)
  val dmem = new DmemIO(xlen, xlen)
}

class Core(conf: Config) extends Module {
  val io = IO(new CoreIO(conf.xlen))

  val rfsz = 32
  val rlen = log2Ceil(rfsz)
  val rf = Module(new RegFile(rfsz, conf.xlen))

  val fetch = Module(new Fetch(conf.xlen, conf.bootAddr))
  val execute = Module(new Execute(conf.xlen, rlen))
  val writeback = Module(new Writeback(conf.xlen, rlen))

  val control = Module(new Control())

  val started = RegNext(reset.asBool)
  val stall = Wire(Bool())
  val flush = Wire(Bool())

  val prev_imem_rd_req = RegNext(io.imem.req)
  val prev_dmem_rd_req = RegNext(io.dmem.req && !io.dmem.we)
  val prev_dmem_wr_req = RegNext(io.dmem.req && io.dmem.we)

  stall := (prev_imem_rd_req && !io.imem.rvalid) ||
           (prev_dmem_rd_req && !io.dmem.rvalid) ||
           (prev_dmem_wr_req && !io.dmem.gnt) ||
           started

  io.imem.req := fetch.io.imem.req
  io.imem.addr := fetch.io.imem.addr

  fetch.io.br_taken := execute.io.data.br_taken
  val inst = Wire(UInt(conf.xlen.W))
  inst := Mux(flush, Instructions.NOP, io.imem.rdata)

  val fe_pc = RegEnable(fetch.io.pc, !stall)
  val fe_inst = RegEnable(inst, Instructions.NOP, !stall)

  control.io.inst := fe_inst
  execute.io.data.inst := fe_inst
  execute.io.ctrl.imm_sel := control.io.imm_sel
  execute.io.ctrl.ld_type := control.io.ld_type
  execute.io.ctrl.st_type := control.io.st_type
  execute.io.ctrl.alu_op := control.io.alu_op
  execute.io.ctrl.a_sel := control.io.a_sel
  execute.io.ctrl.b_sel := control.io.b_sel
  execute.io.ctrl.br_type := control.io.br_type

  execute.io.data.pc := fe_pc

  rf.io.raddr1 := execute.io.rf.rs1
  rf.io.raddr2 := execute.io.rf.rs2
  execute.io.rf.rs1r := rf.io.rdata1
  execute.io.rf.rs2r := rf.io.rdata2

  io.dmem <> execute.io.dmem

  val ew_wb_sel = RegEnable(control.io.wb_sel, !stall)
  val ew_wb_en = RegEnable(control.io.wb_en, !stall)
  val ew_ld_type = RegEnable(control.io.ld_type, !stall)

  val ew_rd = RegEnable(execute.io.data.rd, !stall)
  val ew_ld = io.dmem.rdata
  val ew_pc = RegEnable(fe_pc, !stall)
  val ew_alu_out = RegEnable(execute.io.data.alu_out, !stall)

  writeback.io.ctrl.wb_sel := ew_wb_sel
  writeback.io.ctrl.wb_en := ew_wb_en
  writeback.io.ctrl.ld_type := ew_ld_type

  writeback.io.data.ld := ew_ld
  writeback.io.data.pc := ew_pc
  writeback.io.data.alu_out := ew_alu_out
  writeback.io.data.rd := ew_rd

  rf.io.wen := writeback.io.rf.wen
  rf.io.waddr := writeback.io.rf.waddr
  rf.io.wdata := writeback.io.rf.wdata

  fetch.io.ctrl.pc_sel := control.io.pc_sel
  fetch.io.alu_out := execute.io.data.alu_out
  fetch.io.ctrl.stall := stall

  // if an instruction tries to read from an rs1/rs2 while the previous
  // instruction is still writing it back, forward it from the ALU
  val rs1hzd = ew_wb_en && execute.io.rf.rs1 =/= 0.U && execute.io.rf.rs1 === ew_rd
  when (ew_wb_sel === WbSel.alu && rs1hzd) {
    execute.io.rf.rs1r := ew_alu_out
  }
  val rs2hzd = ew_wb_en && execute.io.rf.rs2 =/= 0.U && execute.io.rf.rs2 === ew_rd
  when (ew_wb_sel === WbSel.alu && rs2hzd) {
    execute.io.rf.rs2r := ew_alu_out
  }

  flush := control.io.inst_kill || execute.io.data.br_taken || started
}
