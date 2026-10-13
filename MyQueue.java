public interface MyQueue<T> {
    void enq(T item) throws InterruptedException;
    T deq();
}