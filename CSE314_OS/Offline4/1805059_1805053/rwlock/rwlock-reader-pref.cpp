#include "rwlock.h"

void InitalizeReadWriteLock(struct read_write_lock * rw)
{

  sem_init(&rw->lock, 0, 1);
  sem_init(&rw->write_lock, 0, 1);
  rw->read_count = 0;
  
}


void ReaderLock(struct read_write_lock * rw)
{
  //	Write the code for aquiring read-write lock by the reader.
  sem_wait(&rw->lock);

  rw->read_count++;
  if (rw->read_count == 1) 
      sem_wait(&rw->write_lock); //reads will be stuck here if a writer is reading

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
  //	Write the code for aquiring read-write lock by the writer.
   sem_wait(&rw->write_lock);


}

void WriterUnlock(struct read_write_lock * rw)
{
  //	Write the code for releasing read-write lock by the writer.
  sem_post(&rw->write_lock);
 
}
