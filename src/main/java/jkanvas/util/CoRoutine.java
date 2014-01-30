package jkanvas.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

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
  /** The exception if encountered. */
  private Exception except;
  /** The worker thread during the run. */
  private Thread worker;

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
    queue = new LinkedBlockingQueue<>(maxCapacity);
    lock = new Object();
    // TODO #43 -- Java 8 simplification
    runner = new Runnable() {

      @Override
      public void run() {
        setThread(Thread.currentThread());
        try {
          try {
            compute();
          } finally {
            endRoutine();
          }
        } catch(final Exception e) {
          setException(e);
        } finally {
          setThread(null);
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
   * Setter.
   * 
   * @param worker The worker thread.
   */
  void setThread(final Thread worker) {
    this.worker = worker;
  }

  /** Ensures that the calling method is accessed from the worker thread. */
  protected final void ensureCorrectThread() {
    if(worker != Thread.currentThread()) throw new IllegalStateException(
        "attempt to access coroutine only method");
  }

  /**
   * Enqueues an item to be iterated over. This method should not be called from
   * outside of the {@link #compute()} method.
   * 
   * @param obj The item which must not be <code>null</code>.
   * @throws IllegalStateException If the coroutine has already finished.
   */
  public final void yield(final T obj) {
    ensureCorrectThread();
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
   * Sets the exception that aborted the element creation. All elements in the
   * buffer will be returned before the exception is actually thrown. After a
   * call to this method no new elements can be added.
   * 
   * @param e The exception.
   */
  public final void setException(final Exception e) {
    ensureCorrectThread();
    except = e;
    endRoutine();
  }

  /**
   * Ends the coroutine. Note that after this method is called no new elements
   * may be added. It is optional to call this method to signal the end of
   * elements. This method should not be called from outside of the
   * {@link #compute()} method.
   */
  public final void endRoutine() {
    ensureCorrectThread();
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
      if(except != null) throw new IllegalStateException(
          "exception during element creation", except);
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

  /** The thread pool. */
  private static final ExecutorService POOL = Executors.newCachedThreadPool();

  static {
    // make threads daemons so they won't prevent the program from terminating
    if(POOL instanceof ThreadPoolExecutor) {
      final ThreadPoolExecutor p = ((ThreadPoolExecutor) POOL);
      final ThreadFactory factory = p.getThreadFactory();
      p.setThreadFactory(new ThreadFactory() {

        @Override
        public Thread newThread(final Runnable r) {
          final Thread t = factory.newThread(r);
          t.setDaemon(true);
          return t;
        }

      });
    }
  }

  /** Ensures that the coroutine is started. */
  private void ensureStarted() {
    if(runner == null) return;
    POOL.execute(runner);
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
  // TODO #43 -- Java 8 simplification
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
