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

  val stall = Wire(Bool())
  val flush = Wire(Bool())

  val prev_imem_rd_req = RegNext(io.imem.req)
  val prev_dmem_rd_req = RegNext(io.dmem.req && !io.dmem.we)
  val prev_dmem_wr_req = RegNext(io.dmem.req && io.dmem.we)

  stall := (prev_imem_rd_req && !io.imem.rvalid) ||
           (prev_dmem_rd_req && !io.dmem.rvalid) ||
           (prev_dmem_wr_req && !io.dmem.gnt)

  io.imem.req := fetch.io.imem.req
  io.imem.addr := fetch.io.imem.addr

  fetch.io.br_taken := execute.io.data.br_taken
  val inst = WireInit(Mux(flush, Instructions.NOP, io.imem.rdata))

  val pc_e = RegEnable(fetch.io.pc, !stall)
  val inst_e = RegEnable(inst, !stall)

  control.io.inst := inst_e
  execute.io.data.inst := inst_e
  execute.io.ctrl.imm_sel := control.io.imm_sel
  execute.io.ctrl.ld_type := control.io.ld_type
  execute.io.ctrl.st_type := control.io.st_type
  execute.io.ctrl.alu_op := control.io.alu_op
  execute.io.ctrl.a_sel := control.io.a_sel
  execute.io.ctrl.b_sel := control.io.b_sel
  execute.io.ctrl.br_type := control.io.br_type

  execute.io.data.pc := pc_e

  rf.io.raddr1 := execute.io.rf.rs1
  rf.io.raddr2 := execute.io.rf.rs2
  execute.io.rf.rs1r := rf.io.rdata1
  execute.io.rf.rs2r := rf.io.rdata2

  io.dmem <> execute.io.dmem

  val wb_sel_w = RegEnable(control.io.wb_sel, !stall)
  val wb_en_w = RegEnable(control.io.wb_en, !stall)
  val ld_type_w = RegEnable(control.io.ld_type, !stall)

  val rd_w = RegEnable(execute.io.data.rd, !stall)
  val ld_w = io.dmem.rdata
  val pc_w = RegEnable(pc_e, !stall)
  val alu_out_w = RegEnable(execute.io.data.alu_out, !stall)

  writeback.io.ctrl.wb_sel := wb_sel_w
  writeback.io.ctrl.wb_en := wb_en_w
  writeback.io.ctrl.ld_type := ld_type_w

  writeback.io.data.ld := ld_w
  writeback.io.data.pc := pc_w
  writeback.io.data.alu_out := alu_out_w
  writeback.io.data.rd := rd_w

  rf.io.wen := writeback.io.rf.wen
  rf.io.waddr := writeback.io.rf.waddr
  rf.io.wdata := writeback.io.rf.wdata

  fetch.io.ctrl.pc_sel := control.io.pc_sel
  fetch.io.alu_out := execute.io.data.alu_out
  fetch.io.ctrl.stall := stall

  // if an instruction tries to read from an rs1/rs2 while the previous
  // instruction is still writing it back, forward it from the ALU
  val rs1hzd = wb_en_w && execute.io.rf.rs1 =/= 0.U && execute.io.rf.rs1 === rd_w
  when (wb_sel_w === WbSel.alu && rs1hzd) {
    execute.io.rf.rs1r := alu_out_w
  }
  val rs2hzd = wb_en_w && execute.io.rf.rs2 =/= 0.U && execute.io.rf.rs2 === rd_w
  when (wb_sel_w === WbSel.alu && rs2hzd) {
    execute.io.rf.rs2r := alu_out_w
  }

  flush := control.io.inst_kill || execute.io.data.br_taken

  // when (control.io.ld_type =/= LdType.none && (execute.io.data.rd === execute.io.rf.rs1 || execute.io.data.rd === execute.io.rf.rs2)) {
  //   flush := true.B
  //   stall := true.B
  // }
}
