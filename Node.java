public class Node<T>
{
    public T value;
    public volatile Node<T> next;
    public Node(T x)
    {
        value = x;
        next = null;
    }
    
}
