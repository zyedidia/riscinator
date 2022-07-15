package rvcpu.core

import chisel3._
import chisel3.util._

class BranchIO(xlen: Int) extends Bundle {
  val rs1 = Input(UInt(xlen.W))
  val rs2 = Input(UInt(xlen.W))
  val br_type = Input(BrType())
  val taken = Output(Bool())
}

class Branch(xlen: Int) extends Module {
  val io = IO(new BranchIO(xlen))

  val eq = io.rs1 === io.rs2
  val lt = io.rs1.asSInt < io.rs2.asSInt
  val ltu = io.rs1 < io.rs2

  io.taken := false.B

  switch (io.br_type) {
    is (BrType.eq)  { io.taken := eq   }
    is (BrType.ne)  { io.taken := !eq  }
    is (BrType.lt)  { io.taken := lt   }
    is (BrType.ge)  { io.taken := !lt  }
    is (BrType.ltu) { io.taken := ltu  }
    is (BrType.geu) { io.taken := !ltu }
  }
}
