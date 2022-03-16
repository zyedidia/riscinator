#include "gpio.h"
#include "bits.h"

typedef struct {
    unsigned gpi_val;
    unsigned gpo_val;
} gpio_reg_t;

static volatile gpio_reg_t* const gpio = (gpio_reg_t*) 0x20000;

void gpo_write(gpo_pin_t pin, unsigned val) {
    gpio->gpo_val = bit_assign(gpio->gpo_val, pin, val);
}

unsigned gpi_read(gpi_pin_t pin) {
    return bit_get(gpio->gpi_val, pin);
}
