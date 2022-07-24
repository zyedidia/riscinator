#include "librtor.h"

void __attribute__((interrupt)) handler() {
    mcause_t cause = read_csr(mcause);
    void* epc = (void*) read_csr(mepc);
    switch (cause) {
    case MCAUSE_ECALL_U:
        printf("%p: ecall from u-mode\n", epc);
        break;
    case MCAUSE_ECALL_M:
        printf("%p: ecall from m-mode\n", epc);
        break;
    default:
        printf("%p: other cause: %d\n", epc, cause);
        break;
    }
    write_csr(mepc, epc+4);
}

int main() {
    exception_init(handler);
    while (1) {
        asm volatile ("ecall");
        printf("ecall returned\n");
        delay_ms(1000);
    }
}
