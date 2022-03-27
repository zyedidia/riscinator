package rvcpu.core

import chisel3._
import chisel3.util._

// RISC-V Privileged ISA Version 1.11

object Mxl {
  val rv32 = 0x1.U(2.W)
  val rv64 = 0x2.U(2.W)
  val rv128 = 0x3.U(2.W)
}

// Privilege modes
object Prv {
  val U = 0x0.U(2.W)
  val M = 0x3.U(2.W)
}

object Csr {
  // User-level CSRs
  val cycle = 0xc00.U(12.W)
  val time = 0xc01.U(12.W)
  val instret = 0xc02.U(12.W)
  val cycleh = 0xc80.U(12.W)
  val timeh = 0xc81.U(12.W)
  val instreth = 0xc82.U(12.W)

  // Machine-level CSRs
  // Machine information registers
  val mvendorid = 0xf11.U(12.W)
  val marchid = 0xf12.U(12.W)
  val mimpid = 0xf13.U(12.W)
  val mhartid = 0xf14.U(12.W)
  // Machine trap setup
  val mstatus = 0x300.U(12.W)
  val misa = 0x301.U(12.W)
  val medeleg = 0x302.U(12.W)
  val mideleg = 0x303.U(12.W)
  val mie = 0x304.U(12.W)
  val mtvec = 0x305.U(12.W)
  val mcounteren = 0x306.U(12.W)
  // Machine trap handling
  val mscratch = 0x340.U(12.W)
  val mepc = 0x341.U(12.W)
  val mcause = 0x342.U(12.W)
  val mtval = 0x343.U(12.W)
  val mip = 0x344.U(12.W)
  // Machine counter/timers
  val mcycle = 0xb00.U(12.W)
  val minstret = 0xb02.U(12.W)
  val mcycleh = 0xb80.U(12.W)
  val minstreth = 0xb82.U(12.W)

  // medeleg and mideleg do not exist because we do not support U-mode traps
  val regs = List(
    cycle,
    time,
    instret,
    cycleh,
    timeh,
    instreth,
    mvendorid,
    marchid,
    mimpid,
    mhartid,
    mstatus,
    misa,
    mie,
    mtvec,
    mcounteren,
    mscratch,
    mepc,
    mcause,
    mtval,
    mip
  )
}

object TrapCause {
  val InstAddrMisaligned = 0x0.U
  val InstAccessFault = 0x1.U
  val IllegalInst = 0x2.U
  val Breakpoint = 0x3.U
  val LoadAddrMisaligned = 0x4.U
  val LoadAccessFault = 0x5.U
  val StoreAddrMisaligned = 0x6.U
  val StoreAccessFault = 0x7.U
  val EcallU = 0x8.U
  val EcallM = 0x11.U
}

class CsrIO(xlen: Int) extends Bundle {
  val cmd = Input(CsrType())
  val csr_addr = Input(UInt(12.W))
  val rdata = Output(UInt(xlen.W))
  val pc = Input(UInt(xlen.W))
  val st_type = Input(StType())
  val ld_type = Input(LdType())
  val epc = Output(UInt(xlen.W))
  val exc = Output(Bool())
}

class McauseDat(xlen: Int) extends Bundle {
  val interrupt = Bool()
  val code = UInt((xlen - 1).W)
}

class Csr(xlen: Int) extends Module {
  val io = IO(new CsrIO(xlen))

  val time = RegInit(0.U(xlen.W))
  val timeh = RegInit(0.U(xlen.W))
  val cycle = RegInit(0.U(xlen.W))
  val cycleh = RegInit(0.U(xlen.W))
  val instret = RegInit(0.U(xlen.W))
  val instreth = RegInit(0.U(xlen.W))

  val misa = Cat(
    Mxl.rv32,
    0.U((xlen - 28).W),
    (1 << ('I' - 'A') | 1 << ('U' - 'A')).U(26.W) // RV32I and U-mode
  )

  val mvendorid = 0.U(xlen.W)
  val marchid = 0.U(xlen.W)
  val mimpid = 0.U(xlen.W)
  val mhartid = 0.U(xlen.W)

  // mstatus
  val MIE = RegInit(false.B)
  val MPIE = RegInit(false.B)
  val MPP = RegInit(Prv.M)
  val PRV = RegInit(Prv.M)

  val mstatus = Cat(
    0.U(14.W),
    0.U(2.W),
    0.U(4.W),
    MPP,
    0.U(3.W),
    MPIE,
    0.U(3.W),
    MIE,
    0.U(3.W)
  )

  // mtvec
  val mtvec = RegInit(0.U(32.W))

  // mip and mie
  val MEIP = RegInit(false.B)
  val MTIP = RegInit(false.B)
  val MSIP = RegInit(false.B)

  val mip = Cat(MEIP, 0.U(3.W), MTIP, 0.U(3.W), MSIP, 0.U(3.W))

  val MEIE = RegInit(false.B)
  val MTIE = RegInit(false.B)
  val MSIE = RegInit(false.B)

  val mie = Cat(MEIE, 0.U(3.W), MTIE, 0.U(3.W), MSIE, 0.U(3.W))

  // mscratch
  val mscratch = Reg(UInt(xlen.W))
  // mepc
  val mepc = Reg(UInt(xlen.W))
  // mcause
  val mcause = Reg(new McauseDat(xlen))
  // mtval
  val mtval = Reg(UInt(xlen.W))

  val regs = List(
    Csr.mvendorid -> mvendorid,
    Csr.marchid -> marchid,
    Csr.mimpid -> mimpid,
    Csr.mhartid -> mhartid,
    Csr.mstatus -> mstatus,
    Csr.misa -> misa,
    Csr.mie -> mie,
    Csr.mtvec -> mtvec,
    Csr.mscratch -> mscratch,
    Csr.mepc -> mepc,
    Csr.mcause -> mcause,
    Csr.mtval -> mtval,
    Csr.mip -> mip
  )

  io.rdata := MuxLookup(io.csr_addr, 0.U, regs)
}
