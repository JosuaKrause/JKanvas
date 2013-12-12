package jkanvas.table.bin;

import java.util.Arrays;
import java.util.SortedSet;

import jkanvas.table.Feature;

/**
 * Provides bins for categorical values.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class CategoricalBinner extends ColumnBinner {

  /** The allowed values in a sorted array. */
  private final double[] sortedValues;

  /**
   * Puts the categorical values of the given feature in bins.
   * 
   * @param f The feature.
   */
  public CategoricalBinner(final Feature f) {
    super(f);
    if(!feature.isCategorical()) throw new IllegalArgumentException("must be categorical");
    final SortedSet<Double> set = f.distinctValues();
    sortedValues = new double[set.size()];
    int i = 0;
    for(final double v : set) {
      sortedValues[i++] = v;
    }
  }

  @Override
  public int bins() {
    return sortedValues.length;
  }

  @Override
  protected double minFor(final int bin) {
    return bin;
  }

  @Override
  public double getMaxValue() {
    return sortedValues.length;
  }

  @Override
  public double getWidthOf(final int bin) {
    if(bin >= sortedValues.length || bin < 0) throw new IndexOutOfBoundsException(
        "" + bin);
    return 1.0;
  }

  @Override
  public int binFor(final double value) {
    final int i = Arrays.binarySearch(sortedValues, value);
    if(i < 0) throw new IllegalArgumentException(value + " not a category");
    return i;
  }

}
