package jkanvas.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Utility functions for arrays.
 * 
 * @author Leo Woerteler
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

}
