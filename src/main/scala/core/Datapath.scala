package rvcpu

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

  import Control._

  // Registers between Fetch/Execute
  val fe_inst = RegInit(Instructions.NOP)
  val fe_pc = RegInit(UInt())

  // Registers between Execute/WriteBack
  val ew_inst = RegInit(Instructions.NOP)
  val ew_pc = Reg(UInt())
  val ew_alu = Reg(UInt())

  // Fetch
  val stall = !io.imem.rvalid || !io.dmem.rvalid
  val pc = Reg(conf.bootAddr)
  val next_pc = Wire(UInt(conf.xlen.W))
  pc := next_pc

  when (stall) {
    next_pc := pc
  } .elsewhen (io.ctrl.pc_sel === PcSel.alu || brCond.io.taken) {
    next_pc := alu.io.out & ~0x1.U
  } .elsewhen (io.ctrl.pc_sel === PcSel.plus0) {
    next_pc := pc
  } .otherwise {
    next_pc := pc + 4.U
  }

  val inst = io.imem.rdata
  when (io.ctrl.inst_kill || brCond.io.taken) {
    inst := Instructions.NOP
  }

  io.imem.addr := next_pc
  io.imem.req := !stall

  when (!stall) {
    fe_pc := pc
    fe_inst := inst
  }

  // Execute
  io.ctrl.inst := fe_inst

  val rd = fe_inst(11, 7)
  val rs1 = fe_inst(19, 15)
  val rs2 = fe_inst(24, 20)

  rf.io.raddr1 := rs1
  rf.io.raddr2 := rs2
}
