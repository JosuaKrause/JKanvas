package jkanvas.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Utility functions for arrays.
 * 
 * @author Leonard Woerteler <leo@woerteler.de>
 * @author Joschi <josua.krause@googlemail.com>
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
   * Swaps two entries in a <code>T</code> array.
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
   */
  public static void swap(final double[] arr, final int i, final int j) {
    final double temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
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
      final Class<T> clazz = (Class<T>) e.getClass();
      if(curClass == null) {
        curClass = clazz;
      } else if(clazz != curClass && clazz.isAssignableFrom(curClass)) {
        curClass = clazz;
      }
      list.add(e);
    }
    if(curClass == null) return (T[]) list.toArray(); // list is empty
    return list.toArray((T[]) Array.newInstance(curClass, list.size()));
  }

  /**
   * Reverses the order of the array in place.
   * 
   * @param <T> The array type.
   * @param arr The array to reverse.
   */
  public static <T> void reverse(final T[] arr) {
    final int mid = arr.length / 2;
    for(int i = 0; i < mid; ++i) {
      swap(arr, i, arr.length - i - 1);
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
