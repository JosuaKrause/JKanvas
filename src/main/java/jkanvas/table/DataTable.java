package jkanvas.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Access to tabular data. The size of the table must not change. The contents
 * of the table may change. Use {@link #cached()} for a non-mutable
 * representation of the current snapshot of the table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class DataTable {

  /** Whether to force the use of caching in aggregation functions. */
  public static boolean FORCE_CACHE_ON_AGGREGATES = true;

  /**
   * Getter.
   * 
   * @return The number of rows.
   */
  public abstract int rows();

  /**
   * Getter.
   * 
   * @return The number of columns.
   */
  public abstract int cols();

  /**
   * Getter.
   * 
   * @param row The row.
   * @param col The column.
   * @return The value at the given column.
   */
  public abstract double getAt(final int row, final int col);

  /**
   * Getter.
   * 
   * @param col The column.
   * @return Whether the column is categorical.
   */
  public abstract boolean isCategorical(int col);

  /**
   * The name of the column. The name identifies a column. Columns are
   * considered equal if they have the same name and the same number of rows.
   * <p>
   * Try to avoid giving simple number names because of possible overlaps. When
   * there is no unique name given you can use {@link #generateName()} to create
   * a name or prefix. When reading names from the disk make sure to
   * {@link #sanitizeName(String) sanitize} the names. This will keep the names
   * human readable but will avoid false name sharing.
   * 
   * @param col The column.
   * @return The name of the column. The name must always be the same for a
   *         given column.
   */
  public abstract String getName(int col);

  /**
   * Lists the names of the columns.
   * 
   * @see #getName(int)
   * @return The names.
   */
  public String[] getNames() {
    final String[] res = new String[cols()];
    for(int i = 0; i < res.length; ++i) {
      res[i] = getName(i);
    }
    return res;
  }

  /**
   * Finds the first column that has the given name.
   * 
   * @param name The name.
   * @return The column or <code>-1</code> if the name is not found.
   */
  public int getColumn(final String name) {
    Objects.requireNonNull(name);
    for(int i = 0; i < cols(); ++i) {
      if(name.equals(getName(i))) return i;
    }
    return -1;
  }

  /**
   * Getter.
   * 
   * @param name The feature name.
   * @return The feature for the given name.
   */
  public Feature getFeature(final String name) {
    final int c = getColumn(name);
    if(c < 0) throw new IllegalArgumentException(name + " not found");
    return getFeature(c);
  }

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The feature for the given column.
   */
  public Feature getFeature(final int col) {
    return new Feature(this, col);
  }

  /**
   * Getter.
   * 
   * @return All features.
   */
  protected Feature[] features() {
    final Feature[] res = new Feature[cols()];
    for(int i = 0; i < res.length; ++i) {
      res[i] = getFeature(i);
    }
    return res;
  }

  /**
   * Getter.
   * 
   * @return All features.
   */
  public final Feature[] getFeatures() {
    final Feature[] fs = features();
    if(hasCachedFeatures()) return Arrays.copyOf(fs, fs.length);
    return fs;
  }

  /**
   * Getter.
   * 
   * @return Whether feature objects are cached.
   */
  protected boolean hasCachedFeatures() {
    return false;
  }

  /** The cached transposed table. */
  protected DataTable transposed;

  /**
   * Getter.
   * 
   * @return A transposed version of this table.
   */
  public DataTable transposed() {
    if(transposed == null) {
      transposed = new TransposedTable(this);
    }
    return transposed;
  }

  /**
   * Getter.
   * 
   * @return Whether the content of this table is cached. This also applies to
   *         aggregation functions.
   */
  public boolean isCaching() {
    return false;
  }

  /**
   * Getter.
   * 
   * @return Returns a cached version of the current snapshot of the table.
   *         Aggregations in cached tables are lazily cached.
   */
  public final DataTable cached() {
    if(isCaching()) return this;
    return new CachedTable(this);
  }

  /**
   * Computes the aggregated value for the given column.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @return The value.
   */
  public double aggregated(final ColumnAggregation agg, final int col) {
    return agg.getValue(this, col);
  }

  /**
   * Getter.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @return The cached aggregated value. {@link Double#NaN} signals a not yet
   *         cached value. This method does not compute aggregations.
   */
  protected double getCachedValue(
      @SuppressWarnings("unused") final ColumnAggregation agg,
      @SuppressWarnings("unused") final int col) {
    return Double.NaN;
  }

  /**
   * Setter.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @param v The value. This method is a no-op if {@link #isCaching()} returns
   *          <code>false</code>.
   */
  protected void setCachedValue(
      @SuppressWarnings("unused") final ColumnAggregation agg,
      @SuppressWarnings("unused") final int col,
      @SuppressWarnings("unused") final double v) {
    if(FORCE_CACHE_ON_AGGREGATES) throw new IllegalStateException("must be caching");
  }

  /**
   * Normalize the value at the given position.
   * 
   * @param row The row.
   * @param col The column.
   * @return The value at the given position normalized for the column.
   */
  public double getMinMaxScaled(final int row, final int col) {
    final double min = ColumnAggregation.MINIMUM.getValue(this, col);
    final double max = ColumnAggregation.MAXIMUM.getValue(this, col);
    if(min == max) return 0;
    return (getAt(row, col) - min) / (max - min);
  }

  /**
   * Normalize the value at the given position with taking zero as minimum.
   * 
   * @param row The row.
   * @param col The column.
   * @return The value at the given position normalized for the column taking
   *         zero as minimum.
   */
  public double getZeroMaxScaled(final int row, final int col) {
    // final double min = ColumnAggregation.MINIMUM.getValue(this, col);
    // if(min < 0) return getAt(row, col) / min;
    final double max = ColumnAggregation.MAXIMUM.getValue(this, col);
    if(max == 0) return 0;
    return getAt(row, col) / max;
  }

  /**
   * Converts the given string array into a categorical column.
   * 
   * @param array The array that will be the column. The column is categorical
   *          via string equality.
   * @param name The name of the column or <code>null</code> if the name should
   *          be empty.
   * @return The table.
   */
  public static final DataTable fromArray(final String[] array, final String name) {
    Objects.requireNonNull(array);
    final List<String> v = new ArrayList<>();
    final double[] values = new double[array.length];
    for(int i = 0; i < array.length; ++i) {
      final String s = Objects.requireNonNull(array[i]);
      final int index = v.indexOf(s);
      if(index >= 0) {
        values[i] = index;
        continue;
      }
      values[i] = v.size();
      v.add(s);
    }
    return fromArray(values, name);
  }

  /**
   * A thin wrapper around the given array.
   * 
   * @param array The array that will be the column. Changes to the array are
   *          reflected in the table. The column is numerical.
   * @param name The name of the column or <code>null</code> if the name should
   *          be automatically generated.
   * @return The wrapper table.
   */
  public static final DataTable fromArray(final double[] array, final String name) {
    Objects.requireNonNull(array);
    final String n = name == null ? generateName() : name;
    return new DataTable() {

      @Override
      public int rows() {
        return array.length;
      }

      @Override
      public boolean isCategorical(final int col) {
        return false;
      }

      @Override
      public String getName(final int col) {
        return n;
      }

      @Override
      public double getAt(final int row, final int col) {
        return array[row];
      }

      @Override
      public int cols() {
        return 1;
      }

    };
  }

  /**
   * Creates a data table from the given array.
   * 
   * @param table The table with <code>table[row][col]</code>.
   * @return The data table.
   */
  public static final DataTable fromMatrix(final double[][] table) {
    return new CachedTable(table);
  }

  /**
   * Computes an array of cached values for the given aggregation function.
   * 
   * @param table The table.
   * @param agg The aggregation function.
   * @return The array or <code>null</code> if no value was cached.
   */
  protected static final double[] getCachedArray(
      final DataTable table, final ColumnAggregation agg) {
    double[] res = null;
    final int cols = table.cols();
    for(int c = 0; c < cols; ++c) {
      final double v = table.getCachedValue(agg, c);
      if(Double.isNaN(v)) {
        continue;
      }
      if(res == null) {
        res = new double[cols];
        Arrays.fill(res, Double.NaN);
      }
      res[c] = v;
    }
    return res;
  }

  /** The name counter. */
  private static AtomicInteger nameCounter = new AtomicInteger();

  /**
   * Creates a unique name for a feature. This name can also be used as prefix.
   * Note that every call to this method results in a new name.
   * 
   * @return The newly generated name.
   */
  public static final String generateName() {
    return "$" + nameCounter.getAndIncrement() + "$";
  }

  /**
   * Sanitizes the given column name so that it does not conflict with
   * automatically generated names. When an automatically generated name is
   * detected this part is changed to a new automatically generated name.
   * 
   * @param name The name to sanitize.
   * @return The sanitized name.
   */
  public static final String sanitizeName(final String name) {
    final String n = name.trim();
    if(n.isEmpty()) return generateName();
    // check for number only names
    boolean isNumber = true;
    for(int c = 0; c < n.length(); ++c) {
      if("0123456789".indexOf(n.charAt(c)) < 0) {
        isNumber = false;
        break;
      }
    }
    if(isNumber) return generateName() + n;
    final int start = n.indexOf('$', 0);
    if(start < 0) return n;
    final int end = n.indexOf('$', start + 1);
    if(end < 0) return n;
    // automatically generated name detected
    final String begin = n.substring(0, start);
    final String trail = end + 1 < n.length() ? n.substring(end + 1) : "";
    // we have to generate a new name every time
    // since we do not know whether begin and trail are empty sometimes
    return begin + generateName() + trail;
  }

}
