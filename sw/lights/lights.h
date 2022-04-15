#pragma once

#include "librv.h"

// these functions must always be inlined because the timing is exactly
// calculated using only the number of nops
static inline void __attribute__((always_inline)) delay_350ns() {
    // each nop is roughly 60ns
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
}

static inline void __attribute__((always_inline)) delay_900ns() {
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");

    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");

    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
    asm volatile ("nop");
}

static inline void write_0(unsigned pin) {
    gpo_write(pin, 1);
    delay_350ns();
    gpo_write(pin, 0);
    delay_900ns();
}

static inline void write_1(unsigned pin) {
    gpo_write(pin, 1);
    delay_900ns();
    gpo_write(pin, 0);
    delay_350ns();
}

static inline void flush(unsigned pin) {
    gpo_write(pin, 0);
    delay_us(50);
}

static inline void write_bit(unsigned pin, uint8_t bit) {
    if (bit) {
        write_1(pin);
    } else {
        write_0(pin);
    }
}

static inline void write_byte(unsigned pin, uint8_t byte) {
    // big-endian
    for (int i = 7; i >= 0; i--) {
        write_bit(pin, (byte >> i) & 0x1);
    }
}

static inline void write_rgb(unsigned pin, uint8_t r, uint8_t g, uint8_t b) {
    write_byte(pin, g);
    write_byte(pin, r);
    write_byte(pin, b);
}

