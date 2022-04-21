#include "librv.h"

int main() {
    const gpo_pin_t led = GPO_LED_R;

    unsigned val = 0;
    while (1) {
        gpo_write(led, val);
        val = !val;
        delay_ms(500);
    }
}