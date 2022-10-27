#include <stdint.h>
#include <stdlib.h>
#include <vector>
#include "VSoc.h"
#include "verilated.h"
#include "verilated_vcd_c.h"

static void clock(VSoc* soc) {
    soc->clock = 0;
    soc->eval();
    soc->clock = 1;
    soc->eval();
}

static void simulate(VSoc* soc, unsigned ncyc) {
    VerilatedVcdC* tfp = new VerilatedVcdC;
    soc->trace(tfp, 99);
    tfp->open("sim.vcd");
    auto time = 0;

    soc->reset = 1;
    clock(soc);
    soc->reset = 0;

    uint32_t next_imem_rdata = 0;
    uint8_t next_imem_rvalid = 0;
    uint32_t next_dmem_rdata = 0;
    uint8_t next_dmem_rvalid = 0;

    for (unsigned i = 0; i < ncyc; i++) {
        clock(soc);
        tfp->dump(time++);
    }
    tfp->close();
}

int main(int argc, char **argv) {
    Verilated::commandArgs(argc, argv);
    Verilated::traceEverOn(true);

    VSoc* soc = new VSoc;
    simulate(soc, 10000);

    return 0;
}
