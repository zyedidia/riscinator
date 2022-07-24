#include "librtor.h"

void __attribute__((interrupt)) handler() {
    mcause_t cause = read_csr(mcause);
    void* epc = (void*) read_csr(mepc);
    switch (cause) {
    case MCAUSE_ECALL_U:
        printf("%p: ecall from u-mode\n", epc);
    case MCAUSE_ECALL_M:
        printf("%p: ecall from m-mode\n", epc);
    default:
        printf("%p: other cause: %d\n", epc, cause);
    }
}

int main() {
    printf("%p: ecall\n", &handler);
    /* exception_init(handler); */
    /* asm volatile ("ecall"); */
    /* printf("done\n"); */
    return 0;
}
