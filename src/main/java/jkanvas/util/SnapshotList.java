package jkanvas.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A list that is readable only through snapshots. The list can always be
 * modified but when a snapshot is obtained all changes afterwards are not
 * present in this snapshot. It is advised to only use one snapshot at a time.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The element type.
 */
public final class SnapshotList<T> {

  /** All registered objects. */
  private final List<T> list = new ArrayList<>();

  /**
   * A quick check to see whether an object is in the list. This set is also
   * used as monitor for all list changing operations and setting
   * {@link #snapshots}.
   */
  private final Set<T> quickCheck = new HashSet<>();

  /**
   * The list that is filled instead of {@link #list} when there are
   * {@link #snapshots}. The elements are added when no {@link #snapshots} are
   * active any more.
   */
  private final List<T> toBeAdded = new ArrayList<>();

  /**
   * The list that schedules removal when there are {@link #snapshots} and
   * {@link #list} cannot be changed. The elements are removed when no
   * {@link #snapshots} are active any more.
   */
  private final List<T> toBeRemoved = new ArrayList<>();

  /**
   * The number of active snapshots. When there are snapshots the {@link #list}
   * is guaranteed not to change.
   */
  private int snapshots;

  /**
   * Adds an object.
   * 
   * @param elem The object.
   */
  public void add(final T elem) {
    synchronized(quickCheck) {
      if(quickCheck.contains(elem)) throw new IllegalArgumentException(
          "object already added: " + elem);
      quickCheck.add(elem);
      if(snapshots > 0) {
        toBeAdded.add(elem);
      } else {
        list.add(elem);
      }
    }
  }

  /**
   * Removes an object.
   * 
   * @param elem The object.
   */
  public void remove(final T elem) {
    synchronized(quickCheck) {
      if(!quickCheck.remove(elem)) throw new IllegalArgumentException(
          "object not in list: " + elem);
      if(snapshots > 0) {
        if(!toBeAdded.contains(elem)) {
          // object must be in animated
          toBeRemoved.add(elem);
        } else {
          toBeAdded.remove(elem);
        }
      } else {
        list.remove(elem);
      }
    }
  }

  /**
   * Getter.
   * 
   * @param elem The object.
   * @return Whether the object is contained in the list.
   */
  public boolean has(final T elem) {
    final boolean has;
    synchronized(quickCheck) {
      has = quickCheck.contains(elem);
    }
    return has;
  }

  /**
   * A snapshot of the given list.
   * 
   * @author Joschi <josua.krause@gmail.com>
   * @param <T> The element type.
   */
  public static final class Snapshot<T> implements AutoCloseable, Iterable<T> {

    /** The list. */
    private final List<T> content;
    /** The snapshot list. */
    private SnapshotList<T> list;

    /**
     * Creates a snapshot.
     * 
     * @param list The snapshot list.
     * @param content The snapshot content.
     */
    public Snapshot(final SnapshotList<T> list, final List<T> content) {
      this.list = list;
      this.content = content;
      list.startSnapshot();
    }

    /** Ensures that the snapshot is still open. */
    private void ensureOpen() {
      if(list == null) throw new IllegalStateException("snapshot already closed");
    }

    /**
     * Getter.
     * 
     * @param index The index.
     * @return Returns the element at the given position.
     */
    public T get(final int index) {
      ensureOpen();
      return content.get(index);
    }

    /**
     * Getter.
     * 
     * @return The size of the list.
     */
    public int size() {
      ensureOpen();
      return content.size();
    }

    @Override
    public Iterator<T> iterator() {
      ensureOpen();
      return Collections.unmodifiableList(content).iterator();
    }

    @Override
    public void close() {
      if(list == null) return;
      list.endSnapshot();
      list = null;
    }

  }

  /** Starts a snapshot. */
  protected void startSnapshot() {
    synchronized(quickCheck) {
      ++snapshots;
    }
  }

  /** Ends a snapshot. */
  protected void endSnapshot() {
    synchronized(quickCheck) {
      --snapshots;
      if(snapshots > 0) return;
      // remove first because objects could be re-added
      list.removeAll(toBeRemoved);
      toBeRemoved.clear();
      list.addAll(toBeAdded);
      toBeAdded.clear();
    }
  }

  /**
   * Creates a snapshot. To best use the snapshot use the following snippet:
   * 
   * <pre>
   * try (Snapshot<...> s = snapshotList.getSnapshot()) {
   *   // do something with the snapshot
   * }
   * </pre>
   * 
   * @return Creates a snapshot.
   */
  public Snapshot<T> getSnapshot() {
    return new Snapshot<>(this, list);
  }

  /**
   * Getter.
   * 
   * @return The number of currently active snapshots.
   */
  public int activeSnapshots() {
    return snapshots;
  }

}
