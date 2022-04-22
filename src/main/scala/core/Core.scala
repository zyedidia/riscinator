package rvcpu.core

import chisel3._
import chisel3.util._

case class Config(
  xlen: Int,
  bootAddr: UInt
)

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
  // number of registers in register file
  private val rfn = 32
  private val rlen = log2Ceil(rfn)

  val io = IO(new CoreIO(conf.xlen))

  // TODO: branches
  val rf = Module(new RegFile(rfn, conf.xlen))
  val fetch = Module(new Fetch(conf.xlen, conf.bootAddr))
  val decode = Module(new Decode(conf.xlen, rlen))
  val execute = Module(new Execute(conf.xlen))
  val memory = Module(new Memory(conf.xlen))
  memory.io.dmem <> io.dmem
  val writeback = Module(new Writeback(conf.xlen, rlen))
  val control = Module(new Control())

  val stall = Wire(Bool())
  val stall_all = Wire(Bool())
  val flush = Wire(Bool())
  stall_all := false.B
  stall := false.B
  flush := false.B

  io.imem.req := fetch.io.imem.req
  io.imem.addr := fetch.io.imem.addr

  val pc_d = RegEnable(fetch.io.pc, !stall && !stall_all)
  val inst_d = RegEnable(io.imem.rdata, !stall && !stall_all)

  control.io.inst := inst_d
  decode.io.data.inst := inst_d
  decode.io.ctrl.imm_sel := control.io.imm_sel

  rf.io.raddr1 := decode.io.data.rs1
  rf.io.raddr2 := decode.io.data.rs2

  val pc_sel_e = RegEnable(control.io.pc_sel, !stall_all)
  val wb_sel_e = RegEnable(control.io.wb_sel, !stall_all)
  val wb_en_e = RegEnable(control.io.wb_en, !stall_all)
  val ld_type_e = RegEnable(control.io.ld_type, !stall_all)
  val st_type_e = RegEnable(control.io.st_type, !stall_all)
  val alu_op_e = RegEnable(control.io.alu_op, !stall_all)
  val a_sel_e = RegEnable(control.io.a_sel, !stall_all)
  val b_sel_e = RegEnable(control.io.b_sel, !stall_all)

  execute.io.ctrl.alu_op := alu_op_e
  execute.io.ctrl.a_sel := a_sel_e
  execute.io.ctrl.b_sel := b_sel_e

  val pc_e = RegEnable(pc_d, !stall_all)
  val rs1r_e = RegEnable(rf.io.rdata1, !stall_all)
  val rs2r_e = RegEnable(rf.io.rdata2, !stall_all)
  val rs1_e = RegEnable(decode.io.data.rs1, !stall_all)
  val rs2_e = RegEnable(decode.io.data.rs2, !stall_all)
  val rd_e = RegEnable(decode.io.data.rd, !stall_all)
  val imm_e = RegEnable(decode.io.data.imm, !stall_all)

  execute.io.data.pc := pc_e
  execute.io.data.rs1r := rs1r_e
  execute.io.data.rs2r := rs2r_e
  execute.io.data.imm := imm_e

  when (flush) {
    pc_sel_e := PcSel.plus0
    wb_sel_e := WbSel.alu
    wb_en_e := false.B
    ld_type_e := LdType.none
    st_type_e := StType.none
    alu_op_e := AluOp.none
    a_sel_e := ASel.none
    b_sel_e := BSel.none
    pc_e := 0.U
    rs1r_e := 0.U
    rs2r_e := 0.U
    rs1_e := 0.U
    rs2_e := 0.U
    rd_e := 0.U
    imm_e := 0.U
  }

  val pc_sel_m = RegEnable(pc_sel_e, !stall_all)
  val wb_sel_m = RegEnable(wb_sel_e, !stall_all)
  val wb_en_m = RegEnable(wb_en_e, !stall_all)
  val ld_type_m = RegEnable(ld_type_e, !stall_all)
  val st_type_m = RegEnable(st_type_e, !stall_all)

  memory.io.ctrl.ld_type := ld_type_m
  memory.io.ctrl.st_type := st_type_m

  val rd_m = RegEnable(rd_e, !stall_all)
  val pc_m = RegEnable(pc_e, !stall_all)
  val alu_out_m = RegEnable(execute.io.data.alu_out, !stall_all)
  val rs2r_m = RegEnable(rs2r_e, !stall_all)

  memory.io.data.alu_out := alu_out_m
  memory.io.data.rs2 := rs2r_m

  val ld_type_w = RegEnable(ld_type_m, !stall_all)
  val wb_sel_w = RegEnable(wb_sel_m, !stall_all)
  val wb_en_w = RegEnable(wb_en_m, !stall_all)

  writeback.io.ctrl.wb_sel := wb_sel_w
  writeback.io.ctrl.wb_en := wb_en_w
  writeback.io.ctrl.ld_type := ld_type_w

  val ld_w = io.dmem.rdata
  val pc_w = RegEnable(pc_m, !stall_all)
  val alu_out_w = RegEnable(alu_out_m, !stall_all)
  val rd_w = RegEnable(rd_m, !stall_all)

  writeback.io.data.ld := ld_w
  writeback.io.data.pc := pc_w
  writeback.io.data.alu_out := alu_out_w
  writeback.io.data.rd := rd_w

  rf.io.wen := writeback.io.rf.wen
  rf.io.waddr := writeback.io.rf.waddr
  rf.io.wdata := writeback.io.rf.wdata

  fetch.io.ctrl.pc_sel := pc_sel_m
  fetch.io.alu_out := alu_out_m
  fetch.io.ctrl.stall := stall || stall_all

  when (rs1_e =/= 0.U && rs1_e === rd_m && wb_en_m) {
    execute.io.data.rs1r := alu_out_m
  } .elsewhen (rs1_e =/= 0.U && rs1_e === rd_w && wb_en_w) {
    when (ld_type_w === LdType.none) {
      execute.io.data.rs1r := alu_out_w
    } .otherwise {
      execute.io.data.rs1r := ld_w
    }
  }
  when (rs2_e =/= 0.U && rs2_e === rd_m && wb_en_m) {
    execute.io.data.rs2r := alu_out_m
  } .elsewhen (rs2_e =/= 0.U && rs2_e === rd_w && wb_en_w) {
    when (ld_type_w === LdType.none) {
      execute.io.data.rs2r := alu_out_w
    } .otherwise {
      execute.io.data.rs2r := ld_w
    }
  }

  when (ld_type_e =/= LdType.none && (rd_e === decode.io.data.rs1 || rd_e === decode.io.data.rs2)) {
    stall := true.B
    flush := true.B
  }

  when (memory.io.data.stall || io.imem.req && !io.imem.rvalid) {
    stall_all := true.B
  }
}
