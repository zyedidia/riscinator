package rtor.sys.ram

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline
import chisel3.experimental.{annotate, ChiselAnnotation}
import chisel3.util.HasBlackBoxInline

import rtor.bus._

class Ram(offset: Int, size: Int, addrw: Int, dataw: Int, memfile: String = "") extends Module {
  class Memory(offset: Int, size: Int, addrw: Int, dataw: Int, memfile: String = "")
      extends BlackBox
      with HasBlackBoxInline {
    val io = IO(new Bundle {
      val clock = Input(Clock())
      val we = Input(Bool())
      val iaddr = Input(UInt(addrw.W))
      val daddr = Input(UInt(addrw.W))
      val be = Input(UInt((dataw / 8).W))
      val wdata = Input(UInt(dataw.W))
      val irdata = Output(UInt(dataw.W))
      val drdata = Output(UInt(dataw.W))
    })

    setInline(
      "Memory.sv",
      s"""
      module Memory(
        input logic clock,
        input logic we,
        input logic [${addrw - 1}:0] iaddr,
        input logic [${addrw - 1}:0] daddr,
        input logic [${dataw / 8 - 1}:0] be,
        input logic [${dataw - 1}:0] wdata,
        output logic [${dataw - 1}:0] irdata,
        output logic [${dataw - 1}:0] drdata
      );

        logic [${dataw - 1}:0] mem [0:$size-1];

        initial begin
          $$readmemh("$memfile", mem);
        end

        always_ff @(posedge clock) begin
            if (we) begin
                // write the appropriate bytes according to the write mask
                if (be[3])
                    mem[daddr >> 2][31:24] <= wdata[31:24];
                if (be[2])
                    mem[daddr >> 2][23:16] <= wdata[23:16];
                if (be[1])
                    mem[daddr >> 2][15:8] <= wdata[15:8];
                if (be[0])
                    mem[daddr >> 2][7:0] <= wdata[7:0];
            end

            irdata <= mem[iaddr >> 2];
            drdata <= mem[daddr >> 2];
        end
      endmodule
      """.stripMargin
    )
  }

  val io = IO(new Bundle {
    val imem = Flipped(new RoIO(addrw, dataw))
    val dmem = Flipped(new RwIO(addrw, dataw))
  })

  // TODO: set err for imem when it is out of bounds (for dmem, the bus only
  // selects this device when in range and handles out-of-bounds faults in the
  // arbiter)
  io.imem.err := false.B
  io.dmem.err := false.B
  io.imem.gnt := io.imem.req
  io.dmem.gnt := io.dmem.req

  val mem = Module(new Memory(offset, size, addrw, dataw, memfile))
  mem.io.clock := clock
  mem.io.we := io.dmem.we && io.dmem.req
  mem.io.iaddr := io.imem.addr
  mem.io.daddr := io.dmem.addr
  mem.io.be := io.dmem.be
  mem.io.wdata := io.dmem.wdata
  io.imem.rdata := mem.io.irdata
  io.dmem.rdata := mem.io.drdata

  io.imem.rvalid := RegNext(io.imem.req)
  io.dmem.rvalid := RegNext(io.dmem.req)
}
