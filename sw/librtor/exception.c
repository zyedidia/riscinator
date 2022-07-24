#include "exception.h"

void exception_init(void (*handler)()) {
    write_csr(mtvec, handler);
}
