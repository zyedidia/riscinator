#include <stddef.h>

void* memchr(const void* s, int c, size_t n) {
    const unsigned char* sp = s;

    while (n--) {
        if (*sp == (unsigned char) c)
            return (void*) sp;
        sp++;
    }

    return NULL;
}

int memcmp(const void* s1, const void* s2, size_t n) {
    const unsigned char *c1 = s1, *c2 = s2;
    int d = 0;

    while (n--) {
        d = (int) *c1++ - (int) *c2++;
        if (d)
            break;
    }

    return d;
}

void* memcpy(void* dst, const void* src, size_t n) {
    const char* p = src;
    char* q = dst;
    while (n--) {
        *q++ = *p++;
    }

    return dst;
}

void* memmove(void* dst, const void* src, size_t count) {
    char* a = dst;
    const char* b = src;

    if (src == dst)
        return dst;

    if (src > dst) {
        while (count--)
            *a++ = *b++;
    } else {
        a += count - 1;
        b += count - 1;
        while (count--)
            *a-- = *b--;
    }

    return dst;
}

void* memset(void* dst, int c, size_t n) {
    char* q = dst;

    while (n--) {
        *q++ = c;
    }

    return dst;
}

void memswap(void* m1, void* m2, size_t n) {
    char* p = m1;
    char* q = m2;
    char tmp;

    while (n--) {
        tmp = *p;
        *p = *q;
        *q = tmp;

        p++;
        q++;
    }
}

static inline size_t newgap(size_t gap) {
    gap = (gap * 10) / 13;
    if (gap == 9 || gap == 10)
        gap = 11;

    if (gap < 1)
        gap = 1;
    return gap;
}

int strcmp(const char* a, const char* b) {
    while (1) {
        unsigned char ac = *a, bc = *b;
        if (ac == 0 || bc == 0 || ac != bc) {
            return (ac > bc) - (ac < bc);
        }
        ++a, ++b;
    }
}

size_t strlen(const char* p) {
    size_t ret;
    for (ret = 0; p[ret]; ++ret)
        ;
    return ret;
}
