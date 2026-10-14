import java.util.concurrent.locks.*;
import  java.util.concurrent.atomic.*;

public class LockFreeQueue<T> implements MyQueue<T>
{
    public class Node
    {
        public T value;
        public AtomicReference<Node> next;
        public Node(T x)
        {
            value = x;
            next = new AtomicReference<>(null);
        }
    }
    

    //Members
    volatile AtomicReference<Node> head;
    volatile AtomicReference<Node> tail;

    int capacity;

    public LockFreeQueue()
    {
        Node sentinel = new Node(null);
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);

    }

    public void enq(T value)
    {
        Node node = new Node(value);
        while(true)
        {
            Node last = tail.get();
            Node next = last.next.get();
            if(last == tail.get())
            {
                if(next == null)
                {
                    if(last.next.compareAndSet(next, node))
                    {
                        tail.compareAndSet(last, node);
                        return;
                    }
                }
                else
                {
                    tail.compareAndSet(last, next);
                }
            }
        }
    }
    public T deq()
    {
        while(true)
        {
            Node first = head.get();
            Node last = tail.get();
            Node next = first.next.get();
            if(first == head.get())
            {
                if(first == last)
                {
                    if(next == null)
                    {
                        return null;
                    }
                    tail.compareAndSet(last, next);
                }
                else
                {
                    T value = next.value;
                    if(head.compareAndSet(first, next))
                    {
                        return value;
                    }
                }
            }
        }
    }

}
