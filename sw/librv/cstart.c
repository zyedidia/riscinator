#include <stdlib.h>

extern int main();

extern int __bss_start__, __bss_end__;

void _cstart() {
    int* bss = &__bss_start__;
    int* bss_end = &__bss_end__;

    while (bss < bss_end) {
        *bss++ = 0;
    }

    main();
}
