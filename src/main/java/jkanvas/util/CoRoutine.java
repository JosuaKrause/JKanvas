package jkanvas.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Iterates over items that are generated on the fly. Generated items are freed
 * as soon as possible to enable iteration over a large number of items.
 * However, for performance reasons produced items are stored temporarily until
 * they are consumed.
 * <p>
 * The iterator is not thread-safe.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type to iterate.
 */
public abstract class CoRoutine<T> implements Iterator<T> {

  /** A queue to temporarily store items. */
  private final BlockingQueue<T> queue;
  /** The lock to communicate between consumer and producer. */
  private final Object lock;
  /** Whether the producer has finished. */
  private volatile boolean finish;
  /** A reference to the producer before it is started. */
  private Runnable runner;

  /** Creates a coroutine with default settings. */
  public CoRoutine() {
    this(2000);
  }

  /**
   * Creates a coroutine.
   * 
   * @param maxCapacity The maximal capacity of the temporary storage.
   */
  public CoRoutine(final int maxCapacity) {
    queue = new LinkedBlockingQueue<T>(maxCapacity);
    lock = new Object();
    runner = new Runnable() {

      @Override
      public void run() {
        try {
          compute();
        } finally {
          endRoutine();
        }
      }

    };
  }

  /**
   * Computes the elements to iterate over. Use {@link #yield(Object)} to add
   * items to the iteration. As soon as all items are produced the method
   * {@link #endRoutine()} may be called. However, this is not necessary.
   */
  protected abstract void compute();

  /**
   * Enqueues an item to be iterated over. This method should not be called from
   * outside of the {@link #compute()} method.
   * 
   * @param obj The item which must not be <code>null</code>.
   * @throws IllegalStateException If the coroutine has already finished.
   */
  protected void yield(final T obj) {
    if(finish) throw new IllegalStateException("cannot add items after end of coroutine");
    try {
      queue.put(Objects.requireNonNull(obj));
      synchronized(lock) {
        lock.notifyAll();
      }
    } catch(final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Ends the coroutine. Note that after this method is called no new elements
   * may be added. It is optional to call this method to signal the end of
   * elements. This method should not be called from outside of the
   * {@link #compute()} method.
   */
  protected void endRoutine() {
    finish = true;
    synchronized(lock) {
      lock.notifyAll();
    }
  }

  /**
   * The next element that will be returned by {@link #next()} or
   * <code>null</code> if the coroutine has finished.
   */
  private T cur;

  /** Fetches the next element. */
  private void fetchNext() {
    while((cur = queue.poll()) == null) {
      if(finish) return;
      try {
        synchronized(lock) {
          lock.wait(100);
        }
      } catch(final InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  /** Ensures that the coroutine is started. */
  private void ensureStarted() {
    if(runner == null) return;
    final Thread t = new Thread(runner);
    t.setDaemon(true);
    t.start();
    fetchNext();
    runner = null;
  }

  @Override
  public boolean hasNext() {
    ensureStarted();
    return cur != null;
  }

  @Override
  public T next() {
    ensureStarted();
    if(cur == null) throw new NoSuchElementException();
    final T row = cur;
    fetchNext();
    return row;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
