#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <wait.h>
#include <pthread.h>

int item_to_produce, item_to_consume, next_fill_loc, next_consume_loc;
int empty, full;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t cond_curr_buf_full = PTHREAD_COND_INITIALIZER;
pthread_cond_t cond_curr_buf_empty = PTHREAD_COND_INITIALIZER;

int total_items, max_buf_size, num_workers, num_masters;

int *buffer;

void print_produced(int num, int master) {

  printf("Produced %d by master %d\n", num, master);
}

void print_consumed(int num, int worker) {
    
  printf("Consumed %d by worker %d\n", num, worker);
  
}

void *generate_requests_loop(void *data)
{
  int thread_id = *((int *)data);

  while(1)
    {  
      
      pthread_mutex_lock(&mutex);
    
     
      while (full) { //no risk of going to permanent sleep since when the last consumer exits, the buffer is empty and this loop will break
        pthread_cond_wait(&cond_curr_buf_full, &mutex);
        
      }
      if (item_to_produce>=total_items) //shouldn't create more even if not full
          break;


      buffer[next_fill_loc] = item_to_produce;
      empty = 0;
      pthread_cond_broadcast(&cond_curr_buf_empty); //possible for 2 consumers to be sleeping. wanna wake them all

      print_produced(item_to_produce, thread_id);
      item_to_produce++;

     

      
      next_fill_loc=(next_fill_loc+1)%max_buf_size;

      if (next_fill_loc == next_consume_loc) {
        full = 1;
      }
      
      
      // printf("master %d gave lock up\n", thread_id);
      pthread_mutex_unlock(&mutex);

    }
  // printf("broke out of master %d\n", thread_id);
  pthread_cond_broadcast(&cond_curr_buf_empty);
  pthread_mutex_unlock(&mutex);
  return 0;
}


void *consume_requests_loop(void *data)
{
  int thread_id = *((int *)data);
  int item;
  while(1)
    { 
     
      pthread_mutex_lock(&mutex);

      while (empty) {
        
        if (item_to_consume<=0)  //let both consumer0 and consumer1 be waken up by the very last producer which dies immediately after. consumer0 wakes first and takes the last item and dies. consumer1 wakes up, finds the buffer empty. it shouldn't go back to sleep because no producer remains to wake it up
          break;
        pthread_cond_wait(&cond_curr_buf_empty, &mutex); 
       

      }
      if (item_to_consume<=0)  //double_break
          break;
    
      item = buffer[next_consume_loc];
      buffer[next_consume_loc] = -1;
      full = 0;
      item_to_consume--;


      print_consumed(item, thread_id);
      pthread_cond_broadcast(&cond_curr_buf_full);

    
      
      next_consume_loc= (next_consume_loc+1)%max_buf_size;

      if (next_consume_loc == next_fill_loc) {
        empty = 1;
      }
      
      // printf("slave %d gave lock up\n", thread_id);
      pthread_mutex_unlock(&mutex);
    }
  // printf("broke out of slave %d\n", thread_id);
       
  pthread_mutex_unlock(&mutex);
  return 0;
}



int main(int argc, char *argv[])
{
  int *master_thread_id;
  pthread_t *master_thread;
  pthread_t *worker_thread;
  int *worker_thread_id;
  item_to_produce = 0;
  empty = 1;
  full = 0;
  next_consume_loc = 0;
  next_fill_loc = 0;
  
  int i;
  
   if (argc < 5) {
    printf("./master-worker #total_items #max_buf_size #num_workers #masters e.g. ./exe 10000 1000 4 3\n");
    exit(1);
  }
  else { 
    total_items = atoi(argv[1]);
    max_buf_size = atoi(argv[2]);
    num_workers = atoi(argv[3]);
    num_masters = atoi(argv[4]);
  }
  

  item_to_consume = total_items;

   buffer = (int *)malloc (sizeof(int) * max_buf_size);
   for (int b=0; b<max_buf_size;b++)
      buffer[b]=-1;
  
  master_thread_id = (int *)malloc(sizeof(int) * num_masters);
  master_thread = (pthread_t *)malloc(sizeof(pthread_t) * num_masters);


  for (i = 0; i < num_masters; i++)
    master_thread_id[i] = i;

  for (i = 0; i < num_masters; i++)
    pthread_create(&master_thread[i], NULL, generate_requests_loop, (void *)&master_thread_id[i]);
  





  //create worker consumer threads

  worker_thread_id = (int *)malloc(sizeof(int) * num_workers);
  worker_thread = (pthread_t *)malloc(sizeof(pthread_t) * num_workers);

  for (i = 0; i < num_workers; i++)
    worker_thread_id[i] = i;
  
  for (i = 0; i < num_workers; i++)
    pthread_create(&worker_thread[i], NULL, consume_requests_loop, (void *)&worker_thread_id[i]);


  //wait for all threads to complete
  for (i = 0; i < num_masters; i++)
    {
      pthread_join(master_thread[i], NULL);
      printf("master %d joined\n", i);
    }
  
  for (i = 0; i < num_workers; i++)
    {
      pthread_join(worker_thread[i], NULL);
      printf("worker %d joined\n", i);
    }
  
  
  /*----Deallocating Buffers---------------------*/
  free(buffer);
  free(master_thread_id);
  free(master_thread);
  
  return 0;
}
