#pragma once

#include "mem.h"

typedef struct {
    uint64_t mtime;
    uint64_t mtimecmp;
    uint32_t cycles;
} timer_reg_t;

static volatile timer_reg_t* const _timer = (timer_reg_t*) 0x10000;

static inline unsigned timer_get_cycles() {
    return _timer->cycles;
}

static inline void delay_cyc(unsigned cyc) {
    unsigned rb = timer_get_cycles();
    while (1) {
        unsigned ra = timer_get_cycles();
        if ((ra - rb) >= cyc) {
            break;
        }
    }
}

static inline void delay_us(unsigned us) {
    delay_cyc(us * CLK_FREQ_MHZ);
}

static inline void delay_ms(unsigned ms) {
    delay_us(1000 * ms);
}

static inline void timer_clear_irq() {
    _timer->mtime = 0;
    _timer->mtimecmp = _timer->mtimecmp;
}

static inline void timer_init_irq(uint64_t irq_freq_us) {
    _timer->mtimecmp = CLK_FREQ_MHZ * irq_freq_us;
    _timer->mtime = 0;
}
