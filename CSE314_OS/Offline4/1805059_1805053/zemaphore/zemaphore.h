#include <pthread.h>

typedef struct zemaphore {
    int value;
    pthread_cond_t cond;
    pthread_mutex_t lock;   
    //lock for value. without it two threads could get the same value\
    when the first thread is switched out before it can decrement value.\
    Specially a problem when value=1, since both threads will get 1 and\
    think they are the only thread running.
} zem_t;

void zem_init(zem_t *, int);
void zem_up(zem_t *);
void zem_down(zem_t *);
