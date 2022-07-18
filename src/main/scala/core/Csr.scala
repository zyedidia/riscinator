package rtor.core

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

object Priv extends ChiselEnum {
  val u = 0.U(2.W)
  val m = 3.U(2.W)
}

object Csr {
  // user-level
  val cycle = 0xc00.U(12.W)
  val time = 0xc01.U(12.W)
  val instret = 0xc02.U(12.W)
  val cycleh = 0xc80.U(12.W)
  val timeh = 0xc81.U(12.W)
  val instreth = 0xc82.U(12.W)

  // machine information registers
  val mvendorid = 0xf11.U(12.W)
  val marchid = 0xf12.U(12.W)
  val mimpid = 0xf13.U(12.W)
  val mhartid = 0xf14.U(12.W)

  // machine trap setup
  val mstatus = 0x300.U(12.W)
  val misa = 0x301.U(12.W)
  val medeleg = 0x302.U(12.W)
  val mideleg = 0x303.U(12.W)
  val mie = 0x304.U(12.W)
  val mtvec = 0x305.U(12.W)
  val mcounteren = 0x306.U(12.W)

  // machine trap handling
  val mscratch = 0x340.U(12.W)
  val mepc = 0x341.U(12.W)
  val mcause = 0x342.U(12.W)
  val mtval = 0x343.U(12.W)
  val mip = 0x344.U(12.W)
}

object Cause {
  val instAddrMisaligned = 0.U
  val instAccessFault = 1.U
  val illegalInsn = 2.U
  val breakpoint = 3.U
  val loadAddrMisaligned = 4.U
  val loadAccessFault = 5.U
  val ecallU = 8.U
  val ecallM = 11.U
}

class CsrCtrlIO extends Bundle {
  val csr_type = Input(CsrType())
  val st_type = Input(StType())
  val ld_type = Input(LdType())
  val pc_sel = Input(PcSel())
}

class CsrIO(xlen: Int) extends Bundle {
  val ctrl = new CsrCtrlIO()

  val pc = Input(UInt(xlen.W))
  val csr = Input(UInt(12.W))
  val rs1 = Input(UInt(5.W))
  val wdata = Input(UInt(xlen.W))

  // val exception = Output(Bool())
  // val epc = Output(UInt(xlen.W))
  val rdata = Output(UInt(xlen.W))
}

class Csr(xlen: Int) extends Module {
  val io = IO(new CsrIO(xlen))

  val cycle = RegInit(0.U(xlen.W))
  val time = RegInit(0.U(xlen.W))
  val instret = RegInit(0.U(xlen.W))
  val cycleh = RegInit(0.U(xlen.W))
  val timeh = RegInit(0.U(xlen.W))
  val instreth = RegInit(0.U(xlen.W))

  val mvendorid = 0.U(xlen.W)
  val marchid = 0.U(xlen.W)
  val mimpid = 0.U(xlen.W)
  val mhartid = 0.U(xlen.W)

  val mstatus = 0.U(xlen.W)
  val misa = 0.U(xlen.W)
  val medeleg = 0.U(xlen.W)
  val mideleg = 0.U(xlen.W)
  val mie = 0.U(xlen.W)
  val mtvec = 0.U(xlen.W)
  val mcounteren = 0.U(xlen.W)

  val mscratch = RegInit(0.U(xlen.W))
  val mepc = 0.U(xlen.W)
  val mcause = 0.U(xlen.W)
  val mtval = 0.U(xlen.W)
  val mip = 0.U(xlen.W)

  val regs = Array(
    Csr.cycle -> cycle,
    Csr.time -> time,
    Csr.instret -> instret,
    Csr.cycleh -> cycleh,
    Csr.timeh -> timeh,
    Csr.instreth -> instreth,
    Csr.mvendorid -> 0.U(xlen.W),
    Csr.marchid -> 0.U(xlen.W),
    Csr.mimpid -> 0.U(xlen.W),
    Csr.mhartid -> 0.U(xlen.W),
    Csr.mstatus -> 0.U(xlen.W),
    Csr.misa -> 0.U(xlen.W),
    Csr.medeleg -> 0.U(xlen.W),
    Csr.mideleg -> 0.U(xlen.W),
    Csr.mie -> 0.U(xlen.W),
    Csr.mtvec -> 0.U(xlen.W),
    Csr.mcounteren -> 0.U(xlen.W),
    Csr.mscratch -> mscratch,
    Csr.mepc -> 0.U(xlen.W),
    Csr.mcause -> 0.U(xlen.W),
    Csr.mtval -> 0.U(xlen.W),
    Csr.mip -> 0.U(xlen.W)
  )

  io.rdata := MuxLookup(io.csr, 0.U, regs)
  val valid = regs.map(_._1 === io.csr).reduce(_ || _)
  val wen = io.ctrl.csr_type === CsrType.w
  val wdata = MuxCase(0.U, Array((io.ctrl.csr_type === CsrType.w) -> io.wdata))

  when(wen) {
    switch(io.csr) {
      is(Csr.mscratch) { mscratch := wdata }
    }
  }
}
