#include "rwlock.h"

void InitalizeReadWriteLock(struct read_write_lock * rw)
{

  sem_init(&rw->lock, 0, 1);
  sem_init(&rw->write_lock, 0, 1);
  sem_init(&rw->wait_for_writers, 0, 1);
  rw->read_count = 0;
  
}

/*
readers check if any writers are waiting, if so, they wait on semaphore "waiting_for_writers"
writers put themselves in a virtual queue and the first one holds the "waiting_for_writers"
once all writers done, the last one out wakes up readers waiting on "waiting_for_writers"

*/




void ReaderLock(struct read_write_lock * rw)
{
  
  int flag = 0;
  sem_wait(&rw->lock);

  while(rw->writer_count > 0) {   //check if there are waiting writers
    sem_post(&rw->lock);
    sem_wait(&rw->wait_for_writers); //fancy spin lock
    sem_wait(&rw->lock);
    flag = 1;
  }
  
  rw->read_count++;

  if (rw->read_count == 1) 
    sem_wait(&rw->write_lock); //reads will be stuck here if a writer is reading
  
  if (flag == 1) //if every reader posts, the semaphore would take on a high +ve value and the logic will fail
    sem_post(&rw->wait_for_writers); //  let some reader wait but not post. subsequent writers would be stuck

  sem_post(&rw->lock);
   
  
}

void ReaderUnlock(struct read_write_lock * rw)
{ 
  sem_wait(&rw->lock);
  rw->read_count--;

  if (rw->read_count == 0) 
      sem_post(&rw->write_lock);

  sem_post(&rw->lock);
  
}

void WriterLock(struct read_write_lock * rw)
{
  
  //put itself in a queue of sorts. signals to subsequent readers that they should wait
  sem_wait(&rw->lock);
  rw->writer_count++;

  if (rw->writer_count == 1) {
    sem_wait(&rw->wait_for_writers);
  }
  sem_post(&rw->lock);

  sem_wait(&rw->write_lock);

}

void WriterUnlock(struct read_write_lock * rw)
{
  sem_wait(&rw->lock);
  rw->writer_count--;
  if (rw->writer_count == 0){  //wake readers when it's the last writer
    sem_post(&rw->wait_for_writers);
  }
  sem_post(&rw->write_lock);
  sem_post(&rw->lock);
   
}
