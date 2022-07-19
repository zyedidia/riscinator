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

class Csr(xlen: Int, bootAddr: UInt) extends Module {
  val io = IO(new CsrIO(xlen))

  def mtreg() = new {
    val p = RegInit(false.B)
    val e = RegInit(false.B)
  }
  // bits for mip and mie
  val mei = mtreg()
  val mti = mtreg()
  val msi = mtreg()

  // bits for mstatus
  val mpp = RegInit(Priv.m)
  val mpie = RegInit(false.B)
  val mie = RegInit(false.B)

  val priv = RegInit(Priv.m)

  val regs = Map(
    Csr.cycle -> RegInit(0.U(xlen.W)),
    Csr.time -> RegInit(0.U(xlen.W)),
    Csr.instret -> RegInit(0.U(xlen.W)),
    Csr.cycleh -> RegInit(0.U(xlen.W)),
    Csr.timeh -> RegInit(0.U(xlen.W)),
    Csr.instreth -> RegInit(0.U(xlen.W)),
    Csr.mvendorid -> 0.U(xlen.W),
    Csr.marchid -> 0.U(xlen.W),
    Csr.mimpid -> 5000.U(xlen.W),
    Csr.mhartid -> 0.U(xlen.W),
    Csr.mstatus -> Cat(0.U(19.W), mpp, 0.U(3.W) mpie, 0.U(3.W), mie, 0.U(3.W)),
    Csr.misa -> Cat(1.U(2.W), 0.U((xlen - 28).W), ((1 << 'I' - 'A') | (1 << 'U' - 'A')).U(26.W)),
    // medeleg and mideleg don't exist because we don't have user mode traps
    Csr.mip -> Cat(0.U((xlen - 12).W), mei.p, 0.U(3.W), mti.p, 0.U(3.W), msi.p, 0.U(3.W)),
    Csr.mie -> Cat(0.U((xlen - 12).W), mei.e, 0.U(3.W), mti.e, 0.U(3.W), msi.e, 0.U(3.W)),
    Csr.mtvec -> RegInit(bootAddr),
    Csr.mcounteren -> 0.U(xlen.W),
    Csr.mscratch -> RegInit(0.U(xlen.W)),
    Csr.mepc -> Reg(xlen.W),
    Csr.mcause -> Reg(xlen.W),
    Csr.mtval -> Reg(xlen.W),
  )

  io.rdata := MuxLookup(io.csr, 0.U, regs.toSeq)
  val valid = regs.map(_._1 === io.csr).reduce(_ || _)
  val wen = io.ctrl.csr_type === CsrType.w
  val wdata = MuxCase(0.U, Array((io.ctrl.csr_type === CsrType.w) -> io.wdata))

  when(wen) {
    switch(io.csr) {
      is(Csr.mstatus) {
        mpp := wdata(12, 11)
        mpie := wdata(7)
        mie := wdata(3)
      }
      is(Csr.mip) {
        mei.p := wdata(11)
        mti.p := wdata(7)
        msi.p := wdata(3)
      }
      is(Csr.mie) {
        mei.e := wdata(11)
        mti.e := wdata(7)
        msi.e := wdata(3)
      }
      is(Csr.mscratch) { regs(Csr.mscratch) := wdata }
      is(Csr.mtvec) { regs(Csr.mtvec) := wdata }
      is(Csr.mepc) { regs(Csr.mepc) := wdata >> 2.U << 2.U }
      // XXX: can anything be written to mcause?
      is(Csr.mcause) { regs(Csr.mcause) := wdata }
    }
  }
}
