#pragma once

typedef enum {
    GPO_LED_R = 0,
    GPO_LED_G = 1,
    GPO_LED_B = 2,
} gpo_pin_t;

typedef enum {
    GPI_BTN = 0,
} gpi_pin_t;

void gpo_write(gpo_pin_t pin, unsigned val);
unsigned gpi_read(gpi_pin_t pin);
