package jkanvas.table.bin;

import jkanvas.table.ColumnAggregation;
import jkanvas.table.Feature;

/**
 * Divides the feature in bins of equal size. The number of bins must be
 * specified beforehand.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class SimpleBinner extends ColumnBinner {

  /** The number of bins. */
  private final int bins;
  /** The minimal value. */
  private final double min;
  /** The width of a bin. */
  private final double width;

  /**
   * Puts values in equally sized bins.
   * 
   * @param f The feature.
   * @param bins The number of bins.
   */
  public SimpleBinner(final Feature f, final int bins) {
    super(f);
    if(bins <= 0) throw new IllegalArgumentException("" + bins);
    final double min = feature.aggregate(ColumnAggregation.MINIMUM);
    final double max = getMaxValue();
    if(max == min) {
      this.min = min - 1;
      this.bins = 1;
      width = 1;
      return;
    }
    this.min = min;
    this.bins = bins;
    width = (max - min) / bins;
    if(width < 0) throw new IllegalArgumentException("min > max");
  }

  @Override
  public int bins() {
    return bins;
  }

  @Override
  protected double minFor(final int bin) {
    return min + bin * width;
  }

  @Override
  public double getWidthOf(final int bin) {
    if(bin < 0 || bin > bins) throw new IndexOutOfBoundsException("" + bin);
    return width;
  }

  @Override
  public int binFor(final double value) {
    final double max = getMaxValue();
    if(value > max) throw new IllegalArgumentException(
        "value > max: " + value + " > " + max);
    if(value < min) throw new IllegalArgumentException(
        "value < min: " + value + " < " + min);
    if(value == max) return bins - 1;
    return (int) ((value - min) / width);
  }

}
