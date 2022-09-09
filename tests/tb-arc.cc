#include "Core.h"
#include <iostream>
#include <fstream>
#include <stdint.h>
#include <stdlib.h>
#include <cassert>
#include <cstring>
#include <vector>

#include "rvsym.h"

#include "itype_mem.h"
#include "jmps_mem.h"

extern "C" {
unsigned Core_clock(void* state);
unsigned Core_passthrough(void* state);
}

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

static unsigned addr2idx(uint32_t addr, size_t mem_base) {
    return (addr-mem_base) / sizeof(uint32_t);
}

static void simulate(Core &core, uint32_t* mem, size_t len, size_t mem_base) {
    core.reset = 1;
    core.passthrough();
    core.clock();
    core.passthrough();
    core.reset = 0;

    uint32_t next_imem_rdata = 0;
    uint8_t next_imem_rvalid = 0;
    uint32_t next_dmem_rdata = 0;
    uint8_t next_dmem_rvalid = 0;

    unsigned ncyc = 4;
    for (unsigned i = 0; i < ncyc; i++) {
        core.passthrough();

        core.io_imem_rvalid = next_imem_rvalid;
        core.io_imem_rdata = next_imem_rdata;
        core.io_dmem_rvalid = next_dmem_rvalid;
        core.io_dmem_rdata = next_dmem_rdata;

        next_imem_rvalid = core.io_imem_req;
        next_dmem_rvalid = core.io_dmem_req;
        core.io_dmem_gnt = core.io_dmem_req;

        if (core.io_imem_req) {
            assert(addr2idx(core.io_imem_addr, mem_base) < len);
            next_imem_rdata = mem[addr2idx(core.io_imem_addr, mem_base)];
        }

        if (core.io_dmem_req && core.io_dmem_we) {
            uint32_t write = core.io_dmem_wdata;
            uint32_t mask = be2mask(core.io_dmem_be);
            assert(addr2idx(core.io_dmem_addr, mem_base) < len);
            mem[addr2idx(core.io_dmem_addr, mem_base)] = write & mask;
        } else if (core.io_dmem_req) {
            assert(addr2idx(core.io_dmem_addr, mem_base) < len);
            next_dmem_rdata = mem[addr2idx(core.io_dmem_addr, mem_base)];
        }

        core.clock();
    }
}

#define MEMSIZE 4096
#define MEMBASE 0x100000
static uint32_t mem[MEMSIZE];

int main(int argc, char **argv) {
    Core core;

    memset(mem, 0, sizeof(mem));
    uint32_t insn;
    rvsym_mark_bytes(&insn, sizeof(insn), "insn");
    // rvsym_assume(insn == 0x02a00093);
    rvsym_assume((insn & 0b1111111) == 0b0010011);
    mem[0] = insn;
    mem[1] = 0x0000006f;
    simulate(core, mem, MEMSIZE, MEMBASE);
    if (core.internal.rf.regs_ext.words[1].data == 42) {
        rvsym_exit();
    }

    return 0;
}
