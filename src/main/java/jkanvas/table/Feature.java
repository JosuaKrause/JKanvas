package jkanvas.table;

import static jkanvas.table.ColumnAggregation.*;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import jkanvas.util.BitSetIterable;

/**
 * A feature is a column of a {@link DataTable}. Equality of columns is based on
 * their name and number of rows.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Feature implements Iterable<Double> {

  /** The table. */
  private final DataTable table;
  /** The column. */
  private final int col;

  /**
   * Creates a feature.
   * 
   * @param table The table.
   * @param col The column.
   */
  public Feature(final DataTable table, final int col) {
    this.table = Objects.requireNonNull(table);
    if(col < 0 || col >= table.cols()) throw new IndexOutOfBoundsException("" + col);
    this.col = col;
  }

  /**
   * Getter.
   * 
   * @return The name of the column.
   */
  public String getName() {
    return table.getName(col);
  }

  /**
   * This method renames the current feature. When renaming multiple features
   * make sure to create a {@link FeatureTable} and cache it to avoid
   * unnecessary memory usage:
   * 
   * <pre>
   * {@link Feature}[] renamedFeatures = ...;
   * {@link FeatureTable} ft = new {@link FeatureTable#FeatureTable(Feature[]) FeatureTable(renamedFeatures)};
   * {@link DataTable} table = {@link DataTable#cached() ft.cached()};
   * // use only the features provided by table and let
   * // renamedFeatures get out of scope as fast as possible
   * </pre>
   * 
   * @param newName The new name of the feature.
   * @return The feature with the new name.
   */
  public Feature renamed(final String newName) {
    Objects.requireNonNull(newName);
    final DataTable old = table;
    final int column = col;
    final DataTable t = new WrappedTable(old) {

      @Override
      public int rows() {
        return table.rows();
      }

      @Override
      public int cols() {
        return table.cols();
      }

      @Override
      public boolean isCategorical(final int col) {
        return table.isCategorical(col);
      }

      @Override
      public String getName(final int col) {
        return col == column ? newName : table.getName(col);
      }

      @Override
      public double getAt(final int row, final int col) {
        return table.getAt(row, col);
      }

    };
    return t.getFeature(column);
  }

  /**
   * Getter.
   * 
   * @return Whether the column has categorical values.
   */
  public boolean isCategorical() {
    return table.isCategorical(col);
  }

  /**
   * Getter.
   * 
   * @return The column this feature represents.
   */
  public int getColumn() {
    return col;
  }

  /**
   * Getter.
   * 
   * @return The table of this feature.
   */
  public DataTable getTable() {
    return table;
  }

  /**
   * Getter.
   * 
   * @param row The row.
   * @return The element in the given row.
   */
  public double getElement(final int row) {
    return table.getAt(row, col);
  }

  /**
   * Getter.
   * 
   * @return The number of rows.
   */
  public int rows() {
    return table.rows();
  }

  /**
   * Getter.
   * 
   * @param agg The aggregation function.
   * @return The aggregated value.
   */
  public double aggregate(final ColumnAggregation agg) {
    return agg.getValue(table, col);
  }

  /**
   * Getter.
   * 
   * @param agg The aggregation function.
   * @return The cached value.
   */
  double getCachedValue(final ColumnAggregation agg) {
    return table.getCachedValue(agg, col);
  }

  /**
   * Setter.
   * 
   * @param agg The aggregation function.
   * @param v The cached value.
   */
  void setCachedValue(final ColumnAggregation agg, final double v) {
    table.setCachedValue(agg, col, v);
  }

  /**
   * Getter.
   * 
   * @return Whether the feature is caching.
   */
  public boolean isCaching() {
    return table.isCaching();
  }

  /**
   * Getter.
   * 
   * @return A caching version of the feature.
   */
  public Feature cached() {
    if(isCaching()) return this;
    return table.cached().getFeature(col);
  }

  @Override
  public Iterator<Double> iterator() {
    return new Iterator<Double>() {

      /** The current row. */
      private int row = 0;

      @Override
      public boolean hasNext() {
        return row < rows();
      }

      @Override
      public Double next() {
        if(!hasNext()) throw new NoSuchElementException();
        return getElement(row++);
      }

      @Override
      // TODO #43 -- Java 8 simplification
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  /**
   * Getter.
   * 
   * @return The distinct values of this feature.
   */
  public SortedSet<Double> distinctValues() {
    final SortedSet<Double> set = new TreeSet<>();
    for(final double v : this) {
      set.add(v);
    }
    return set;
  }

  /**
   * Sets the indices of rows with the given value.
   * 
   * @param set The set where the rows will be set. The previous content of the
   *          set is or'd.
   * @param value The value to find.
   */
  public void rowsWith(final BitSet set, final double value) {
    for(int r = 0; r < rows(); ++r) {
      final double v = getElement(r);
      if(v == value) {
        set.set(r);
      }
    }
  }

  /**
   * Iterates over all rows with the given value.
   * 
   * @param value The reference value.
   * @return The row indices.
   */
  public Iterable<Integer> rowsWith(final double value) {
    final BitSet set = new BitSet();
    rowsWith(set, value);
    return new BitSetIterable(set);
  }

  /**
   * Creates a feature that contains only rows with the given class.
   * 
   * @param classes The classifying feature.
   * @param label The label of the class. The value must be present in the
   *          classifying feature.
   * @return The feature.
   * @throws IllegalArgumentException When the classes have a different number
   *           of rows than this feature.
   */
  public Feature conditional(final Feature classes, final double label) {
    if(classes.rows() != rows()) throw new IllegalArgumentException(
        "must have same number of rows: " + classes.rows() + " != " + rows());
    final BitSet selection = new BitSet();
    classes.rowsWith(selection, label);
    // TODO produce less garbage -- now a single column table is created to
    // avoid creating copies of the whole table when caching
    final DataTable t = new RowSelectionTable(
        new FeatureTable(new Feature[] { this}), selection);
    return t.getFeature(0);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + rows();
    result = prime * result + getName().hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Feature)) return false;
    final Feature other = (Feature) obj;
    if(rows() != other.rows()) return false;
    return getName().equals(other.getName());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "(" + getName() + "[" + rows() + "], " + table.toString() + ")";
  }

  /**
   * An abstract metric of features.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static abstract class FeatureMetric implements Metric<Feature> {

    /** Creates a metric. */
    public FeatureMetric() {
      // nothing to do
    }

    @Override
    public double distance(final Feature a, final Feature b) {
      final int rows = a.rows();
      if(rows != b.rows()) throw new IllegalArgumentException(
          "different number of rows: " + rows + " != " + b.rows());
      double sum = 0.0;
      for(int r = 0; r < rows; ++r) {
        sum += dist(a.getElement(r), b.getElement(r), a, b);
      }
      return endSum(sum, a, b);
    }

    /**
     * The operation after the sum is computed.
     * 
     * @param sum The sum.
     * @param fa The first feature.
     * @param fb The second feature.
     * @return The result.
     */
    protected double endSum(final double sum,
        @SuppressWarnings("unused") final Feature fa,
        @SuppressWarnings("unused") final Feature fb) {
      return Math.sqrt(sum);
    }

    /**
     * The distance of the elements of one row.
     * 
     * @param a The first element.
     * @param b The second element.
     * @param fa The first feature.
     * @param fb The second feature.
     * @return The distance.
     */
    protected abstract double dist(double a, double b, Feature fa, Feature fb);

  } // FeatureMetric

  /** The euclidean metric. */
  // TODO #43 -- Java 8 simplification
  public static final Metric<Feature> EUCLID = new FeatureMetric() {

    @Override
    protected double dist(final double a, final double b, final Feature fa,
        final Feature fb) {
      return (a - b) * (a - b);
    }

  }; // EUCLID

  /** The scaled metric. */
  // TODO #43 -- Java 8 simplification
  public static final Metric<Feature> SCALED = new FeatureMetric() {

    @Override
    protected double dist(final double a, final double b,
        final Feature fa, final Feature fb) {
      final double ra = fa.aggregate(MAXIMUM) - fa.aggregate(MINIMUM);
      final double rb = fb.aggregate(MAXIMUM) - fb.aggregate(MINIMUM);
      if(ra == 0 || rb == 0) return Double.POSITIVE_INFINITY;
      final double sa = (a - fa.aggregate(MINIMUM)) / ra;
      final double sb = (b - fb.aggregate(MINIMUM)) / rb;
      return (sa - sb) * (sa - sb);
    }

  }; // SCALED

  /**
   * A metric based on the Pearson coefficient. It is modified so that a perfect
   * correlation (-1 or 1) is 0 distance and no correlation (0) is the maximum
   * distance of 1.
   */
  // TODO #43 -- Java 8 simplification
  public static final Metric<Feature> PEARSON = new FeatureMetric() {

    @Override
    protected double dist(final double a, final double b,
        final Feature fa, final Feature fb) {
      return (a - fa.aggregate(MEAN)) * (b - fb.aggregate(MEAN));
    }

    @Override
    protected double endSum(final double sum, final Feature fa, final Feature fb) {
      final double stdA = fa.aggregate(STD_DEVIATION);
      final double stdB = fb.aggregate(STD_DEVIATION);
      if(stdA == 0.0 || stdB == 0.0) return (stdA == stdB) ? 1.0 : 0.0;
      return 1 - Math.abs(sum / stdA / stdB / fa.rows());
    }

  }; // PEARSON

}
