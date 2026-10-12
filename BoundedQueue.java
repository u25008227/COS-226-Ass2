import java.util.concurrent.locks.*;
import  java.util.concurrent.atomic.*;


public class BoundedQueue<T>
{
    //locks for enquing and dequeing
    ReentrantLock enqLock;
    ReentrantLock deqLock;

    //conditions
    Condition notEmpty;
    Condition notFull;

    //size
    AtomicInteger size;

    //Members
    volatile Node<T> head;
    volatile Node<T> tail;

    int capacity;

    //constructor?
    public BoundedQueue(int _capacity)
    {
        this.capacity = _capacity;
        head = new Node<>(null);
        tail = head;
        size = new AtomicInteger(0);
        enqLock = new ReentrantLock();
        notFull = enqLock.newCondition();
        deqLock = new ReentrantLock();
        notEmpty = deqLock.newCondition();
    }
    //enqueue method
    public void enq(T x)
    {
        boolean mustWakeDeqrs = false;
        enqLock.lock();
        try
        {
            while(size.get() == capacity)
            {
                notFull.await();
            }
            Node<T> e = new Node<>(x);
            tail.next = e;
            tail = e;
            if(size.getAndIncrement() == 0)
            {                
                mustWakeDeqrs = true;
            }
        }
            catch (InterruptedException f)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                enqLock.unlock();
            }   
            if(mustWakeDeqrs)
            {
                deqLock.lock();
                try
                {
                    notEmpty.signalAll();
                }
                finally
                {
                    deqLock.unlock();
                }
            }
        }
        public T deq()
        {
            T result = null;
            boolean mustWakeEnqrs = false;
            deqLock.lock();
            try
            {
                while (size.get() == 0)
                {
                    notEmpty.await();
                }
                result = head.next.value;
                head = head.next;
                if(size.getAndDecrement() == capacity)
                {
                    mustWakeEnqrs = true;
                }
            }
            catch (InterruptedException f)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                deqLock.unlock();
            }
            if(mustWakeEnqrs)
            {
                enqLock.lock();
                try
                {
                    notFull.signalAll();
                }
                finally
                {
                    enqLock.unlock();
                }

            }
            return result;
        }
    }

