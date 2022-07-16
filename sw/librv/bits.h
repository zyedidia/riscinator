#pragma once

#include <stdint.h>

static inline uint32_t bit_clr(uint32_t x, unsigned bit) {
    return x & ~(1 << bit);
}

static inline uint32_t bit_set(uint32_t x, unsigned bit) {
    return x | (1 << bit);
}

static inline uint32_t bit_assign(uint32_t x, unsigned bit, unsigned val) {
    x = bit_clr(x, bit);
    return x | (val << bit);
}

#define bit_isset bit_is_on
#define bit_get bit_is_on

static inline unsigned bit_is_on(uint32_t x, unsigned bit) {
    return (x >> bit) & 1;
}
static inline unsigned bit_is_off(uint32_t x, unsigned bit) {
    return bit_is_on(x, bit) == 0;
}

static inline uint32_t bits_mask(unsigned nbits) {
    if (nbits == 32)
        return ~0;
    return (1 << nbits) - 1;
}

static inline uint32_t bits_get(uint32_t x, unsigned lb, unsigned ub) {
    return (x >> lb) & bits_mask(ub - lb + 1);
}

static inline uint32_t bits_clr(uint32_t x, unsigned lb, unsigned ub) {
    uint32_t mask = bits_mask(ub - lb + 1);

    // XXX: check that gcc handles shift by more bit-width as expected.
    return x & ~(mask << lb);
}

static inline uint32_t bits_set(uint32_t x,
                                unsigned lb,
                                unsigned ub,
                                uint32_t v) {
    return bits_clr(x, lb, ub) | (v << lb);
}

static inline unsigned bits_eq(uint32_t x,
                               unsigned lb,
                               unsigned ub,
                               uint32_t val) {
    return bits_get(x, lb, ub) == val;
}

static inline unsigned bit_count(uint32_t x) {
    unsigned cnt = 0;
    for (unsigned i = 0; i < 32; i++)
        if (bit_is_on(x, i))
            cnt++;
    return cnt;
}

static inline uint32_t bits_union(uint32_t x, uint32_t y) {
    return x | y;
}

static inline uint32_t bits_intersect(uint32_t x, uint32_t y) {
    return x & y;
}
static inline uint32_t bits_not(uint32_t x) {
    return ~x;
}

static inline uint32_t bits_diff(uint32_t A, uint32_t B) {
    return bits_intersect(A, bits_not(B));
}
