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

  val prev_imem_rd_req = RegNext(io.imem.req, false.B)
  val prev_dmem_rd_req = RegNext(io.dmem.req && !io.dmem.we, false.B)
  val prev_dmem_wr_req = RegNext(io.dmem.req && io.dmem.we, false.B)

  io.imem.req := fetch.io.imem.req
  io.imem.addr := fetch.io.imem.addr

  fetch.io.br_taken := execute.io.data.br_taken

  val fe = new {
    val pc = fetch.io.pc
    val inst = io.imem.rdata
    val imem_req = io.imem.req
    val imem_addr = io.imem.addr
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
    val dmem_req = io.dmem.req
    val dmem_addr = io.dmem.addr
    val imem_req = fe.imem_req
    val imem_addr = fe.imem_addr
    val imem_rvalid = io.imem.rvalid
    val imem_err = io.imem.err

    val wb_sel = control.io.sig.wb_sel
    val wb_en = control.io.sig.wb_en
    val ld_type = control.io.sig.ld_type
    val csr_type = control.io.sig.csr_type
    val st_type = control.io.sig.st_type
    val pc_sel = control.io.sig.pc_sel
    val illegal = control.io.sig.illegal

    val rd = execute.io.data.rd
    val ld = io.dmem.rdata
    val pc = fe.pc
    val alu_out = execute.io.data.alu_out
    val csr = execute.io.data.csr
    val rs1 = execute.io.rf.rs1
    val rs1r = execute.io.rf.rs1r
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
  fetch.io.epc := csr.io.epc
}
