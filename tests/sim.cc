#include <stdint.h>
#include <stdlib.h>
#include <vector>
#include "VCore.h"
#include "VCore___024root.h"
#include "verilated.h"
#include "verilated_vcd_c.h"

#include "rtor_mem.h"

#define UART_TX 0x30000

typedef struct {
    uint32_t idx;
    uint32_t value;
} val_t;

typedef struct {
    std::vector<val_t> regs;
    std::vector<val_t> mem;
} check_t;

static uint32_t be2mask(uint8_t be) {
    uint32_t mask = 0;
    for (unsigned i = 0; i < 4; i++) {
        if (be & (1 << i)) {
            mask |= 0xff << i*8;
        }
    }
    return mask;
}

static void clock(VCore* core) {
    core->clock = 0;
    core->eval();
    core->clock = 1;
    core->eval();
}

static unsigned addr2idx(uint32_t addr, size_t mem_base) {
    return (addr-mem_base) / sizeof(uint32_t);
}

static void simulate(VCore* core, uint32_t* mem, size_t len, size_t mem_base, unsigned ncyc) {
    VerilatedVcdC* tfp = new VerilatedVcdC;
    core->trace(tfp, 99);
    tfp->open("sim.vcd");
    auto time = 0;

    core->reset = 1;
    clock(core);
    core->reset = 0;

    uint32_t next_imem_rdata = 0;
    uint8_t next_imem_rvalid = 0;
    uint32_t next_dmem_rdata = 0;
    uint8_t next_dmem_rvalid = 0;

    for (unsigned i = 0; i < ncyc; i++) {
        core->eval();

        core->io_imem_rvalid = next_imem_rvalid;
        core->io_imem_rdata = next_imem_rdata;
        core->io_dmem_rvalid = next_dmem_rvalid;
        core->io_dmem_rdata = next_dmem_rdata;

        next_imem_rvalid = core->io_imem_req;
        next_dmem_rvalid = core->io_dmem_req;
        core->io_dmem_gnt = core->io_dmem_req;
        core->eval();

        if (core->io_imem_req) {
            assert(addr2idx(core->io_imem_addr, mem_base) < len);
            next_imem_rdata = mem[addr2idx(core->io_imem_addr, mem_base)];
        }

        if (core->io_dmem_req && core->io_dmem_we) {
            uint32_t write = core->io_dmem_wdata;
            uint32_t mask = be2mask(core->io_dmem_be);
            if (core->io_dmem_addr == UART_TX) {
                printf("%c", write & mask);
            } else if (addr2idx(core->io_dmem_addr, mem_base) < len) {
                uint32_t val = mem[addr2idx(core->io_dmem_addr, mem_base)];
                mem[addr2idx(core->io_dmem_addr, mem_base)] = (val & ~mask) | (write & mask);
            }
        } else if (core->io_dmem_req) {
            if (addr2idx(core->io_dmem_addr, mem_base) < len) {
                next_dmem_rdata = mem[addr2idx(core->io_dmem_addr, mem_base)];
            } else {
                next_dmem_rdata = 0;
            }
        }

        clock(core);
        tfp->dump(time++);
    }
    tfp->close();
}

#define MEMSIZE 16 * 1024
#define MEMBASE 0x100000
static uint32_t mem[MEMSIZE];

int main(int argc, char **argv) {
    Verilated::commandArgs(argc, argv);
    Verilated::traceEverOn(true);

    VCore* core = new VCore;
    memset(mem, 0, sizeof(mem));
    memcpy(mem, rtor_bin, rtor_bin_len);
    simulate(core, (uint32_t*) mem, MEMSIZE, MEMBASE, 10000000);

    return 0;
}
