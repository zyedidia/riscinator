        addi x26,   x0,    63  // x26 = 63
        andi x2,   x26,    67  // x2 = 3
        ori  x3,   x2,    64  // x3 = 67
        xori x4,   x3,    64  // x4 = 3
        ori  x5,   x0,    -1  // x5 = -1
        slti x6,   x0,    0   // x6 = 0
        slti x7,   x0,    10  // x7 = 1
        addi x8,   x0,    4   // x8 = 4
        sw   x26,   0(x8)      // 0x4 = 63
        sw   x2,   -4(x8)     // 0x0 = 3
        sw   x3,   4(x8)      // 0x8 = 67
        lw   x9,   0(x8)      // x9 = 63
        lw   x10,  -4(x8)     // x10 = 3
        lw   x11,  4(x8)      // x11 = 67
        j    A
        nop
        addi x12,  x0,    1
B:      j    C
        nop
        addi x12, x0,    1
A:      j    B
        nop
        addi x12,  x0,    1   // x12 = 0
C:      slli  x13,  x7,    0   // x13 = 0
        addi x13,  x13,   112 // x13 = 112
        jr   x13
        nop
        addi x14,  x0,    1   // x14 = 0
        addi x15,  x0,    1   // x15 = 1
        jal  D                  // x31 = 4194408
        nop
        addi x16,  x0,    1   // x16 = 0
D:      addi x17,  x0,    1   // x17 = 1
        beq  x0,   x0,    E
        nop
        addi x18,  x0,    1   // x18 = 0
E:      addi x19,  x0,    1   // x19 = 1
        beq  x26,   x0,    F
        nop
        addi x20,  x0,    1   // x20 = 1
F:      addi x21,  x0,    1   // x21 = 1
        bne  x0,   x0,    G
        nop
        addi x22,  x0,    1   // x22 = 1
G:      addi x23,  x0,    1   // x23 = 1
        bne  x26,   x0,    H
        nop
        addi x24,  x0,    1   // x24 = 0
H:      addi x25,  x0,    1   // x25 = 1
        nop