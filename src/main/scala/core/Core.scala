package rtor.core

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
  val csr = Module(new Csr(conf.xlen, conf.bootAddr))

  val control = Module(new Control())

  val started = RegNext(reset.asBool, false.B)
  val stall = Wire(Bool())
  val flush = Wire(Bool())

  val prev_imem_rd_req = RegNext(io.imem.req, false.B)
  val prev_dmem_rd_req = RegNext(io.dmem.req && !io.dmem.we, false.B)
  val prev_dmem_wr_req = RegNext(io.dmem.req && io.dmem.we, false.B)

  stall := started

  io.imem.req := fetch.io.imem.req
  io.imem.addr := fetch.io.imem.addr

  fetch.io.br_taken := execute.io.data.br_taken
  val finst = WireInit(Mux(flush, Instructions.NOP, io.imem.rdata))

  val fe = new {
    val pc = RegEnable(fetch.io.pc, !stall)
    val inst = RegEnable(finst, Instructions.NOP, !stall)
    val imem_req = RegEnable(io.imem.req, false.B, !stall)
    val imem_addr = RegEnable(io.imem.addr, !stall)
  }

  control.io.inst := fe.inst
  execute.io.data.inst := fe.inst

  execute.io.ctrl := control.io.sig

  execute.io.data.pc := fe.pc

  rf.io.raddr1 := execute.io.rf.rs1
  rf.io.raddr2 := execute.io.rf.rs2
  execute.io.rf.rs1r := rf.io.rdata1
  execute.io.rf.rs2r := rf.io.rdata2

  io.dmem <> execute.io.dmem

  val ew = new {
    val dmem_req = RegEnable(io.dmem.req, false.B, !stall)
    val dmem_addr = RegEnable(io.dmem.addr, !stall)
    val imem_req = RegEnable(fe.imem_req, false.B, !stall)
    val imem_addr = RegEnable(fe.imem_addr, !stall)
    val imem_rvalid = RegEnable(io.imem.rvalid, false.B, !stall)
    val imem_err = RegEnable(io.imem.err, false.B, !stall)

    val wb_sel = RegEnable(control.io.sig.wb_sel, WbSel.alu, !stall)
    val wb_en = RegEnable(control.io.sig.wb_en, false.B, !stall)
    val ld_type = RegEnable(control.io.sig.ld_type, LdType.none, !stall)
    val csr_type = RegEnable(control.io.sig.csr_type, CsrType.n, !stall)
    val st_type = RegEnable(control.io.sig.st_type, StType.none, !stall)
    val pc_sel = RegEnable(control.io.sig.pc_sel, PcSel.plus4, !stall)
    val illegal = RegEnable(control.io.sig.illegal, false.B, !stall)

    val rd = RegEnable(execute.io.data.rd, !stall)
    val ld = io.dmem.rdata
    val pc = RegEnable(fe.pc, !stall)
    val alu_out = RegEnable(execute.io.data.alu_out, !stall)
    val csr = RegEnable(execute.io.data.csr, !stall)
    val rs1 = RegEnable(execute.io.rf.rs1, !stall)
    val rs1r = RegEnable(execute.io.rf.rs1r, !stall)
  }

  csr.io.ctrl.csr_type := ew.csr_type
  csr.io.ctrl.st_type := ew.st_type
  csr.io.ctrl.ld_type := ew.ld_type
  csr.io.ctrl.pc_sel := ew.pc_sel
  csr.io.ctrl.illegal := ew.illegal
  csr.io.pc := ew.pc
  csr.io.csr := ew.csr
  csr.io.rs1 := ew.rs1
  csr.io.wdata := ew.rs1r

  csr.io.imem.req := ew.imem_req
  csr.io.imem.addr := ew.imem_addr
  csr.io.imem.rvalid := ew.imem_rvalid
  csr.io.imem.err := ew.imem_err
  csr.io.dmem.req := ew.dmem_req
  csr.io.dmem.addr := ew.dmem_addr
  csr.io.dmem.rvalid := io.dmem.rvalid
  csr.io.dmem.err := io.dmem.err

  writeback.io.ctrl.wb_sel := ew.wb_sel
  writeback.io.ctrl.wb_en := ew.wb_en
  writeback.io.ctrl.ld_type := ew.ld_type

  writeback.io.data.ld := ew.ld
  writeback.io.data.pc := ew.pc
  writeback.io.data.alu_out := ew.alu_out
  writeback.io.data.csr_rdata := csr.io.rdata
  writeback.io.data.rd := ew.rd

  rf.io.wen := writeback.io.rf.wen
  rf.io.waddr := writeback.io.rf.waddr
  rf.io.wdata := writeback.io.rf.wdata

  fetch.io.ctrl.pc_sel := control.io.sig.pc_sel
  fetch.io.alu_out := execute.io.data.alu_out
  fetch.io.ctrl.stall := stall
  fetch.io.epc := csr.io.epc

  def regeq(r1: UInt, r2: UInt) = r1 =/= 0.U && r1 === r2

  def fwd[T <: Data](cond: Bool, to: T, from: T) =
    when(cond) {
      to := from
    }

  // if an instruction tries to read from an rs1/rs2 while the previous
  // instruction is still writing it back from the ALU, forward it from the ALU
  fwd(ew.wb_en && ew.wb_sel === WbSel.alu && regeq(execute.io.rf.rs1, ew.rd), execute.io.rf.rs1r, ew.alu_out)
  fwd(ew.wb_en && ew.wb_sel === WbSel.alu && regeq(execute.io.rf.rs2, ew.rd), execute.io.rf.rs2r, ew.alu_out)

  // flush the pipeline for slow instructions (loads) and when branches are taken
  flush := control.io.sig.inst_kill || execute.io.data.br_taken || csr.io.epc.valid || started
}
