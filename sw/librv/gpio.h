#pragma once

typedef enum {
    GPO_LED_R = 0,
} gpo_pin_t;

typedef enum {
    GPI_BTN = 0,
} gpi_pin_t;

enum {
    GPIO_0  = 0,
    GPIO_1  = 1,
    GPIO_5  = 2,
    GPIO_6  = 3,
    GPIO_9  = 4,
    GPIO_10 = 5,
    GPIO_11 = 6,
    GPIO_12 = 7,
    GPIO_13 = 8,
    GPIO_A0 = 9,
    GPIO_A1 = 10,
    GPIO_A2 = 11,
    GPIO_A3 = 12,
    GPIO_BTN = 31,
    GPIO_LED_R = 14,
    GPIO_LED_G = 15,
    GPIO_LED_B = 16,
};

void gpo_write(gpo_pin_t pin, unsigned val);
unsigned gpi_read(gpi_pin_t pin);
