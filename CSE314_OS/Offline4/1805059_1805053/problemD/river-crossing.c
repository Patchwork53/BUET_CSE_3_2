#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#define N 100
#define CAPACITY 4
#define HACKER 1
#define SERF 0

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t wait_for_next = PTHREAD_COND_INITIALIZER;

/*
5.7 River crossing problem
Pg 148 Little Book of Semaphores
This is from a problem set written by Anthony Joseph at U.C. Berkeley, but
I don’t know if he is the original author. It is similar to the H2O problem in
the sense that it is a peculiar sort of barrier that only allows threads to pass in
certain combinations.
Somewhere near Redmond, Washington there is a rowboat that is used by
both Linux hackers and Microsoft employees (serfs) to cross a river. The ferry
can hold exactly four people; it won’t leave the shore with more or fewer. To
guarantee the safety of the passengers, it is not permissible to put one hacker
in the boat with three serfs, or to put one serf with three hackers. Any other
combination is safe.
As each thread boards the boat it should invoke a function called board. You
must guarantee that all four threads from each boatload invoke board before
any of the threads from the next boatload do.
After all four threads have invoked board, exactly one of them should call
a function named rowBoat, indicating that that thread will take the oars. It
doesn’t matter which thread calls the function, as long as one does.
Don’t worry about the direction of travel. Assume we are only interested in
traffic going in one of the directions.
*/

struct boat
{
    int num_passengers;
    int hacker_or_serf[CAPACITY];
    int passengers[CAPACITY];
    int num_hackers;
    int num_serfs;
};

struct boat boat;
void rowBoat(int thread_id)
{
    if (boat.hacker_or_serf[CAPACITY - 1] == HACKER)
        printf("thread h%d is rowing boat with passengers: ", thread_id);
    else
        printf("thread s%d is rowing boat with passengers: ", thread_id);

    for (int i = 0; i < CAPACITY; i++)
    {
        if (boat.hacker_or_serf[i] == HACKER)
            printf("h%d ", boat.passengers[i]);
        else
            printf("s%d ", boat.passengers[i]);
    }
    printf("\n");
}

void board(int thread_id, int hacker)
{
    pthread_mutex_lock(&lock);

    if (hacker)
    {
        while (boat.num_serfs == 3 || (boat.num_hackers == 2 && boat.num_serfs == 1))
        {
            pthread_cond_wait(&wait_for_next, &lock);
        }
        printf("hacker %d boarded\n", thread_id);
    }
    else
    {
        while (boat.num_hackers == 3 || (boat.num_serfs == 2 && boat.num_hackers == 1))
        {
            pthread_cond_wait(&wait_for_next, &lock);
        }
        printf("serf %d boarded\n", thread_id);
    }

    boat.passengers[boat.num_passengers] = thread_id;
    boat.hacker_or_serf[boat.num_passengers] = hacker;

    boat.num_passengers++;
    if (hacker)
        boat.num_hackers++;
    else
        boat.num_serfs++;
    if (boat.num_passengers == CAPACITY)
    {
        rowBoat(thread_id);
        boat.num_passengers = 0;
        boat.num_hackers = 0;
        boat.num_serfs = 0;
        pthread_cond_broadcast(&wait_for_next);
        // why broadcast instead of signal? It's possible for 4 serfs to be sleeping. If signal, only 1 may wake up, the rest would be sleeping and hence fail to make a crew
    }
    pthread_mutex_unlock(&lock);
}

void *hacker(void *data)
{
    int thread_id = *((int *)data);
    // printf("hacker %d created\n", thread_id);
    board(thread_id, HACKER);
}

void *serf(void *data)
{
    int thread_id = *((int *)data);
    // printf("serf %d created\n", thread_id);
    board(thread_id, SERF);
}

int main()
{
    pthread_t *hacker_threads;
    pthread_t *serf_threads;

    int *hacker_thread_id;
    int *serf_thread_id;

    hacker_thread_id = (int *)malloc(sizeof(int) * N);
    serf_thread_id = (int *)malloc(sizeof(int) * N);

    for (int i = 0; i < N; i++)
    {
        hacker_thread_id[i] = i;
        serf_thread_id[i] = i;
    }

    hacker_threads = (pthread_t *)malloc(sizeof(pthread_t) * N);
    serf_threads = (pthread_t *)malloc(sizeof(pthread_t) * N);
    for (int i = 0; i < N; i++)
    {
        pthread_create(&hacker_threads[i], NULL, hacker, (void *)&hacker_thread_id[i]);
        pthread_create(&serf_threads[i], NULL, serf, (void *)&serf_thread_id[i]);
    }
    //   for (int i = 0; i < N; i++){
    //         // pthread_create(&serf_threads[i], NULL, serf, (void *)&serf_thread_id[i]);
    //         // pthread_create(&hacker_threads[i], NULL, hacker, (void *)&hacker_thread_id[i]);
    //   }

    for (int i = 0; i < N; i++)
    {
        pthread_join(hacker_threads[i], NULL);
        pthread_join(serf_threads[i], NULL);
        //   printf("hacker %d joined\n", i);
    }
}