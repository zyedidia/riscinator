#pragma once

#include "bits.h"

typedef enum {
    GPO_LED_R = 0,
    GPO_LED_G = 1,
    GPO_LED_B = 2,
    GPO_PIN_0 = 3,
    GPO_PIN_1 = 4,
    GPO_PIN_2 = 5,
    GPO_PIN_3 = 6,
} gpo_pin_t;

typedef enum {
    GPI_BTN = 0,
} gpi_pin_t;

typedef struct {
    unsigned gpi_val;
    unsigned gpo_val;
} gpio_reg_t;

static volatile gpio_reg_t* const gpio = (gpio_reg_t*) 0x20000;

static inline void __attribute__((always_inline)) gpo_write(gpo_pin_t pin, unsigned val) {
    gpio->gpo_val = bit_assign(gpio->gpo_val, pin, val);
}

static inline unsigned __attribute__((always_inline)) gpi_read(gpi_pin_t pin) {
    return bit_get(gpio->gpi_val, pin);
}
