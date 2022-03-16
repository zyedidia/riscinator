#include "librv.h"

int main() {
    const int led = GPO_LED_R;
    const int btn = GPI_BTN;

    while (1) {
        gpo_write(led, gpi_read(btn));
    }
}
