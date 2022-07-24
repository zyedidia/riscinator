#pragma once

#include "csr.h"

typedef enum {
    MCAUSE_INST_ADDR_MISALIGNED = 0,
    MCAUSE_INST_ACCESS_FAULT = 1,
    MCAUSE_ILLEGAL_INST = 2,
    MCAUSE_BREAKPOINT = 3,
    MCAUSE_LOAD_ADDR_MISALIGNED = 4,
    MCAUSE_LOAD_ACCESS_FAULT = 5,
    MCAUSE_ECALL_U = 8,
    MCAUSE_ECALL_M = 11,
} mcause_t;

void exception_init(void (*handler)());
