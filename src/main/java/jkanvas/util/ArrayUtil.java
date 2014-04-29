package jkanvas.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Utility functions for arrays.
 *
 * @author Leonard Woerteler <leo@woerteler.de>
 * @author Joschi <josua.krause@gmail.com>
 */
public final class ArrayUtil {

  /** Hidden default constructor. */
  private ArrayUtil() {
    throw new AssertionError();
  }

  /**
   * Fills the given two-dimensional {@code double} array with the value
   * {@code val}.
   *
   * @param arr array to fill
   * @param val value to fill the array with
   * @return filled array
   */
  public static double[][] fill(final double[][] arr, final double val) {
    for(final double[] line : arr) {
      Arrays.fill(line, val);
    }
    return arr;
  }

  /**
   * Getter.
   *
   * @param arr The array.
   * @return The maximal value of the array.
   */
  public static int max(final int[] arr) {
    if(arr.length == 0) throw new IllegalArgumentException("must not be empty");
    int max = Integer.MIN_VALUE;
    for(final int v : arr) {
      if(v > max) {
        max = v;
      }
    }
    return max;
  }

  /**
   * Getter.
   *
   * @param arr The array.
   * @return The maximal value of the array.
   */
  public static double max(final double[] arr) {
    if(arr.length == 0) throw new IllegalArgumentException("must not be empty");
    double max = Double.NaN;
    for(final double v : arr) {
      if(v > max || Double.isNaN(max)) {
        max = v;
      }
    }
    return max;
  }

  /**
   * Getter.
   *
   * @param arr The array.
   * @return The minimal value of the array.
   */
  public static int min(final int[] arr) {
    if(arr.length == 0) throw new IllegalArgumentException("must not be empty");
    int min = Integer.MAX_VALUE;
    for(final int v : arr) {
      if(v < min) {
        min = v;
      }
    }
    return min;
  }

  /**
   * Getter.
   *
   * @param arr The array.
   * @return The minimal value of the array.
   */
  public static double min(final double[] arr) {
    if(arr.length == 0) throw new IllegalArgumentException("must not be empty");
    double min = Double.NaN;
    for(final double v : arr) {
      if(v < min || Double.isNaN(min)) {
        min = v;
      }
    }
    return min;
  }

  /**
   * Copies the given two-dimensional {@code double} array.
   *
   * @param arr array to copy
   * @return deep-copy of the array
   */
  public static double[][] copy(final double[][] arr) {
    final double[][] copy = arr.clone();
    for(int i = 0; i < copy.length; i++) {
      copy[i] = copy[i].clone();
    }
    return copy;
  }

  /**
   * Converts a primitive <code>int</code> array to a boxed {@link Integer}
   * array.
   *
   * @param arr The array to convert.
   * @return The converted array.
   */
  public static Integer[] copyToBox(final int[] arr) {
    final Integer[] res = new Integer[arr.length];
    for(int i = 0; i < arr.length; ++i) {
      res[i] = arr[i];
    }
    return res;
  }

  /**
   * Converts a boxed {@link Integer} array to a primitive <code>int</code>
   * array.
   *
   * @param arr The array to convert.
   * @return The converted array.
   */
  public static int[] copyFromBox(final Integer[] arr) {
    final int[] res = new int[arr.length];
    for(int i = 0; i < arr.length; ++i) {
      res[i] = arr[i];
    }
    return res;
  }

  /**
   * Swaps two entries in a <code>T</code> array. For lists use
   * {@link java.util.Collections#swap(List, int, int)}.
   *
   * @param <T> The type of the array.
   * @param arr array
   * @param i position of first element
   * @param j position of second element
   */
  public static <T> void swap(final T[] arr, final int i, final int j) {
    final T temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

  /**
   * Swaps two entries in a <code>double</code> array.
   *
   * @param arr array
   * @param i position of first element
   * @param j position of second element
   * @see #swap(Object[], int, int)
   */
  public static void swap(final double[] arr, final int i, final int j) {
    final double temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

  /**
   * Swaps two entries in an <code>int</code> array.
   *
   * @param arr array
   * @param i position of first element
   * @param j position of second element
   * @see #swap(Object[], int, int)
   */
  public static void swap(final int[] arr, final int i, final int j) {
    final int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

  /**
   * Sort a given number of elements via their index.
   *
   * @param cmp The comparator to sort the elements. The index of the elements
   *          are handed in.
   * @param size The number of elements.
   * @return The permutation of the indices.
   */
  public static int[] createPermutation(final Comparator<Integer> cmp, final int size) {
    final Integer[] perm = new Integer[size];
    for(int i = 0; i < perm.length; ++i) {
      perm[i] = i;
    }
    Arrays.sort(perm, cmp);
    return copyFromBox(perm);
  }

  /**
   * Applies a permutation to a list. The running time is
   * <code>&theta;(2n)</code> and a copy of the list is created.
   *
   * @param <T> The element type.
   * @param list The list to permute.
   * @param perm The permutation.
   * @see #createPermutation(Comparator, int)
   */
  public static <T> void applyPermutation(final List<T> list, final int[] perm) {
    final List<T> tmp = new ArrayList<>(list);
    if(tmp.size() != perm.length) throw new IllegalArgumentException(
        tmp.size() + " != " + perm.length);
    for(int i = 0; i < perm.length; ++i) {
      list.set(i, tmp.get(perm[i]));
    }
  }

  /**
   * Applies a permutation to a swap-able data structure. The running time is
   * <code>&theta;(2n)</code> with <code>O(n)</code> swap operations. There is
   * no additional space used. No runtime checks regarding the integrity of the
   * permutation array are made and the behavior of the method is undefined for
   * an incorrect array.
   * <p>
   * The permutation array gets modified internally. This means the array cannot
   * be used concurrently during a call to this method (the method is not
   * thread-safe) and if an exception is thrown the array is likely to be
   * corrupted. To fix a possibly corrupted array use the
   * {@link #repairPermutationArray(int[])} method (This is only necessary after
   * an exception was thrown).
   *
   * @param <T> The element type.
   * @param list A data structure allowing only swap operations to permute.
   * @param pos The permutation array. This array is modified internally. This
   *          means that it cannot be used concurrently and that it is possibly
   *          corrupted if an exception occurs during execution.
   *          {@link #repairPermutationArray(int[])} can be used to recover from
   *          a failure.
   * @see #createPermutation(Comparator, int)
   */
  public static <T> void applyPermutation(final Swapable list, final int[] pos) {
    // look for next start of a cycle (ie a value >= 0 in pos)
    for(int start = 0; start < pos.length; ++start) {
      int to = pos[start];
      if(to >= 0) {
        // swap along a cycle until reaching the beginning again
        int from = start;
        while(to != start) {
          list.swap(from, to);
          from = to;
          to = pos[from];
          pos[from] = ~to;
        }
      } else {
        pos[start] = ~to;
      }
    }
  }

  /**
   * Repairs a permutation array after it was corrupted by an exception during a
   * call to {@link #applyPermutation(Swapable, int[])}. Note that the necessity
   * of calling this method implies that either the {@link Swapable}
   * implementation is incorrect or the permutation array was inconsistent. This
   * method is only provided to prevent cascading exceptions from happening.
   *
   * @param pos The possibly corrupted permutation array.
   */
  public static void repairPermutationArray(final int[] pos) {
    for(int i = 0; i < pos.length; ++i) {
      if(pos[i] < 0) {
        pos[i] = ~pos[i];
      }
    }
  }

  /**
   * Shuffles a swapable.
   * 
   * @param s The swapable.
   * @param length The length of the swapable.
   */
  public static void shuffle(final Swapable s, final int length) {
    final java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
    int i = length;
    while(--i >= 0) {
      s.swap(i, rnd.nextInt(i + 1));
    }
  }

  /**
   * Converts an {@link Iterable} to an array.
   *
   * @param <T> The type.
   * @param it The {@link Iterable}.
   * @return The array.
   */
  public static <T> T[] toArray(final Iterable<T> it) {
    Class<T> curClass = null;
    final List<T> list = new ArrayList<>();
    for(final T e : it) {
      @SuppressWarnings("unchecked")
      final Class<T> clazz = (Class<T>) e.getClass();
      if(curClass == null) {
        curClass = clazz;
      } else if(clazz != curClass && clazz.isAssignableFrom(curClass)) {
        curClass = clazz;
      }
      list.add(e);
    }
    if(curClass == null) {
      @SuppressWarnings("unchecked")
      final T[] res = (T[]) list.toArray();
      return res;
    } // list is empty
    @SuppressWarnings("unchecked")
    final T[] res = (T[]) Array.newInstance(curClass, list.size());
    return list.toArray(res);
  }

  /**
   * Reverses the order of the array in place.
   *
   * @param <T> The array type.
   * @param arr The array to reverse.
   */
  public static <T> void reverse(final T[] arr) {
    reverseRange(arr, 0, arr.length);
  }

  /**
   * Reverses the order of a range of the array in place.
   *
   * @param <T> The array type.
   * @param arr The array.
   * @param from The lowest included index.
   * @param to The highest excluded index.
   */
  public static <T> void reverseRange(final T[] arr, final int from, final int to) {
    final int mid = (to - from) / 2 + from;
    for(int i = from; i < mid; ++i) {
      swap(arr, i, to - i - 1);
    }
  }

  /**
   * Reverses the given list for iteration. The actual list is not modified.
   *
   * @param <T> The content type.
   * @param list The list to reverse.
   * @return The reversed list.
   */
  public static <T> Iterable<T> reverseList(final List<T> list) {
    return new Iterable<T>() {

      @Override
      public Iterator<T> iterator() {
        final ListIterator<T> li = list.listIterator(list.size());
        return new Iterator<T>() {

          @Override
          public boolean hasNext() {
            return li.hasPrevious();
          }

          @Override
          public T next() {
            return li.previous();
          }

          @Override
          public void remove() {
            li.remove();
          }

        };
      }

    };
  }

  /**
   * Reverses the given array for iteration. The actual array is not modified.
   *
   * @param <T> The content type.
   * @param arr The array to reverse.
   * @return The reversed array.
   */
  public static <T> Iterable<T> reverseArray(final T[] arr) {
    return new Iterable<T>() {

      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {

          private int pos = arr.length;

          @Override
          public boolean hasNext() {
            return pos > 0;
          }

          @Override
          public T next() {
            return arr[--pos];
          }

          @Override
          // TODO #43 -- Java 8 simplification
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
  }

  /**
   * Rotates the content of an iterable.
   *
   * @param <T> The content type.
   * @param c The iterable to rotate.
   * @param by The number of items to skip and reappend to the end. Only
   *          non-negative values are allowed. Values that are larger than the
   *          content of the iterable are allowed and rotate modulo to the size,
   *          but may consume more resources than smaller values. If you know
   *          the size beforehand use modulo for this argument.
   * @return A rotated iterable.
   */
  public static <T> Iterable<T> rotate(final Iterable<T> c, final int by) {
    if(by < 0) throw new IllegalArgumentException("not supported for iterables");
    if(by == 0) return c;
    return new Iterable<T>() {

      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {

          private Iterator<T> it = c.iterator();

          private List<T> remain = new ArrayList<>();

          {
            int i = 0;
            while(it.hasNext()) {
              remain.add(it.next());
              ++i;
              if(i >= by) {
                break;
              }
            }
            if(remain.size() < by) {
              it = rotate(remain, by).iterator();
              remain = null;
            }
          }

          @Override
          public boolean hasNext() {
            return it.hasNext() || remain != null;
          }

          @Override
          public T next() {
            if(!it.hasNext()) {
              if(remain == null) throw new NoSuchElementException();
              it = remain.iterator();
              remain = null;
            }
            return it.next();
          }

          @Override
          // TODO #43 -- Java 8 simplification
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
  }

  /**
   * Rotates a collection. This means the first part of the collection will be
   * skipped and appended to the end of the collection.
   *
   * @param <T> The content type.
   * @param c The collection.
   * @param by The number of items to skip and append. Negative values are
   *          allowed and rotate in the other direction.
   * @return The resulting iterable.
   */
  public static <T> Iterable<T> rotate(final Collection<T> c, final int by) {
    final int size = c.size();
    final int skip = ((by < 0) ? size + by % size : by) % size;
    if(skip == 0) return c;
    return new Iterable<T>() {

      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {

          private Iterator<T> it = c.iterator();

          private List<T> remain = new ArrayList<>(skip);

          {
            int i = 0;
            while(it.hasNext()) {
              remain.add(it.next());
              ++i;
              if(i >= skip) {
                break;
              }
            }
          }

          @Override
          public boolean hasNext() {
            return it.hasNext() || remain != null;
          }

          @Override
          public T next() {
            if(!it.hasNext()) {
              if(remain == null) throw new NoSuchElementException();
              it = remain.iterator();
              remain = null;
            }
            return it.next();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
  }

  /**
   * Draws <code>k</code> distinct random samples from an iterator. The
   * algorithm used is called Reservoir Sampling and runs in <code>O(n)</code>
   * time and <code>O(k)</code> space.
   *
   * @param <T> The list content type.
   * @param reservoir The reservoir.
   * @param k The number of samples.
   * @return The sample list.
   */
  public static <T> List<T> sample(final Iterator<T> reservoir, final int k) {
    final java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
    final List<T> list = new java.util.ArrayList<>(k);
    for(int i = 0; i < k && reservoir.hasNext(); i++) {
      list.add(reservoir.next());
    }
    if(list.size() == k) {
      for(int j = k; reservoir.hasNext(); j++) {
        final T e = reservoir.next();
        final int pos = rnd.nextInt(j + 1);
        if(pos < k) {
          list.set(pos, e);
        }
      }
    }
    return list;
  }

  /**
   * Draws <code>k</code> distinct random samples from an {@link Iterable}. The
   * algorithm used is called Reservoir Sampling and runs in <code>O(n)</code>
   * time and <code>O(k)</code> space.
   *
   * @param <T> The list content type.
   * @param reservoir The reservoir.
   * @param k The number of samples.
   * @return The sample list.
   */
  public static <T> List<T> sample(final Iterable<T> reservoir, final int k) {
    return sample(reservoir.iterator(), k);
  }

  /**
   * Draws as much distinct random samples from an iterator as fitting into the
   * given array. The array may contain less items when the iterator has fewer
   * items. The algorithm used is called Reservoir Sampling and runs in
   * <code>O(n)</code> time and no additional space.
   *
   * @param <T> The list content type.
   * @param reservoir The reservoir.
   * @param array The array to store the samples in. The number of samples is
   *          limited by the size of the array but may be smaller. Be sure to
   *          check the return value. If the number of samples is smaller the
   *          rest of the array is not modified.
   * @return The number of samples written into the array.
   */
  public static <T> int sample(final Iterator<T> reservoir, final T[] array) {
    final java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
    for(int i = 0; i < array.length; i++) {
      if(!reservoir.hasNext()) return i;
      array[i] = reservoir.next();
    }
    for(int j = array.length; reservoir.hasNext(); j++) {
      final T e = reservoir.next();
      final int pos = rnd.nextInt(j + 1);
      if(pos < array.length) {
        array[pos] = e;
      }
    }
    return array.length;
  }

  /**
   * Draws as much distinct random samples from an {@link Iterable} as fitting
   * into the given array. The array may contain less items when the
   * {@link Iterable} has fewer items. The algorithm used is called Reservoir
   * Sampling and runs in <code>O(n)</code> time and no additional space.
   *
   * @param <T> The list content type.
   * @param reservoir The reservoir.
   * @param array The array to store the samples in. The number of samples is
   *          limited by the size of the array but may be smaller. Be sure to
   *          check the return value. If the number of samples is smaller the
   *          rest of the array is not modified.
   * @return The number of samples written into the array.
   */
  public static <T> int sample(final Iterable<T> reservoir, final T[] array) {
    return sample(reservoir.iterator(), array);
  }

}
