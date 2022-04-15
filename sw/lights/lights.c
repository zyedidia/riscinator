#include "lights.h"

void two_lights(unsigned lights_pin) {
    unsigned npix = 30;
    unsigned iter = 0;

    uint8_t r1, g1, b1;
    uint8_t r2, g2, b2;
    r1 = g1 = b1 = 0;
    r2 = g2 = b2 = 0;

    while (1) {
        for (int i = 0; i < npix; i++) {
            unsigned led1 = iter % 2 == 0 ? i : npix - i - 1;
            unsigned led2 = iter % 2 == 0 ? npix - i - 1 : i;

            for (int p = 0; p < npix; p++) {
                if (p == led1) {
                    write_rgb(lights_pin, r1, g1, b1);
                } else if (p == led2) {
                    write_rgb(lights_pin, r2, g2, b2);
                } else {
                    write_rgb(lights_pin, 0, 0, 0);
                }
            }
            flush(lights_pin);
            delay_ms(20);

            r1 = iter % 2 == 0 ? 8*i : 255 - 8*i;
            g1 = iter % 2 == 0 ? 255 - 8*i : 8*i;
            b1 = 0;

            r2 = 0;
            g2 = iter % 2 == 0 ? 8*i : 255 - 8*i;
            b2 = iter % 2 == 0 ? 255 - 8*i : 8*i;
        }
        iter++;
    }
}

void alternating(unsigned lights_pin) {
    unsigned iter = 0;
    unsigned npix = 30;
    while (1) {
        for (int i = 0; i < npix; i += 3) {
            if (iter == 0) {
                write_rgb(lights_pin, 148, 0, 211);
                write_rgb(lights_pin, 255, 165, 0);
                write_rgb(lights_pin, 0, 0, 0);
            } else if (iter == 1) {
                write_rgb(lights_pin, 0, 0, 0);
                write_rgb(lights_pin, 148, 0, 211);
                write_rgb(lights_pin, 255, 165, 0);
            } else {
                write_rgb(lights_pin, 255, 165, 0);
                write_rgb(lights_pin, 0, 0, 0);
                write_rgb(lights_pin, 148, 0, 211);
            }
        }

        delay_ms(100);

        iter++;
        if (iter == 3) {
            iter = 0;
        }
    }
}

void main() {
    two_lights(GPO_PIN_0);
}
