package jkanvas.table;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * A table where every value is computed once.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class CachedTable extends DataTable {

  /** The names. */
  private final String[] names;
  /** The number of rows. */
  protected final int rows;
  /** The number of columns. */
  protected final int cols;
  /**
   * The content. Classes sub-classing {@link CachedTable} may only alter the
   * content after in the constructor.
   */
  protected final double[][] content;
  /**
   * A bit set of categorical columns or <code>null</code> if no column is
   * categorical.
   */
  private final BitSet categorical;

  /**
   * Creates a cached version of the given table.
   * 
   * @param table The table.
   */
  CachedTable(final DataTable table) {
    Objects.requireNonNull(table);
    rows = table.rows();
    cols = table.cols();
    content = new double[rows][cols];
    for(int r = 0; r < rows; ++r) {
      for(int c = 0; c < cols; ++c) {
        content[r][c] = table.getAt(r, c);
      }
    }
    if(table instanceof CachedTable) {
      final CachedTable ct = (CachedTable) table;
      categorical = ct.categorical;
      names = ct.names;
    } else {
      categorical = categoricalBitSet(table);
      names = new String[cols];
      for(int c = 0; c < cols; ++c) {
        names[c] = table.getName(c);
      }
    }
    for(final ColumnAggregation agg : ColumnAggregation.values()) {
      final double[] arr = getCachedArray(table, agg);
      if(arr == null) {
        continue;
      }
      setCachedArray(agg, arr);
    }
    if(table.transposed != null && table.transposed.isCaching()) {
      transposed = table.transposed;
      // overwrite back-link when we are the better alternative
      final DataTable tt = transposed.transposed;
      if(tt == null || !tt.isCaching()) {
        table.transposed.transposed = this;
      }
    }
  }

  /**
   * Creates a cached table from an array.
   * 
   * @param table The table with <code>table[row][col]</code>.
   */
  CachedTable(final double[][] table) {
    Objects.requireNonNull(table);
    categorical = null;
    content = new double[table.length][];
    int cols = -1;
    for(int r = 0; r < table.length; ++r) {
      final double[] row = table[r];
      if(cols < 0) {
        cols = row.length;
      } else if(cols != row.length) throw new IllegalArgumentException(
          "inconsistent column count: " + cols + " " + row.length);
      content[r] = row.clone();
    }
    this.cols = cols >= 0 ? cols : 0;
    rows = content.length;
    final String prefix = generateName();
    names = new String[cols];
    for(int c = 0; c < cols; ++c) {
      names[c] = prefix + c;
    }
  }

  @Override
  public int rows() {
    return rows;
  }

  @Override
  public int cols() {
    return cols;
  }

  @Override
  public double getAt(final int row, final int col) {
    return content[row][col];
  }

  @Override
  public String getName(final int col) {
    return names[col];
  }

  @Override
  public String[] getNames() {
    return Arrays.copyOf(names, names.length);
  }

  @Override
  public boolean isCategorical(final int col) {
    return categorical != null && categorical.get(col);
  }

  @Override
  public boolean isCaching() {
    return true;
  }

  /** The feature cache. This value is computed lazily. */
  private Feature[] features;

  @Override
  public Feature getFeature(final int col) {
    return features()[col];
  }

  @Override
  protected Feature[] features() {
    if(features == null) {
      final Feature[] res = new Feature[cols()];
      for(int i = 0; i < res.length; ++i) {
        res[i] = new Feature(this, i);
      }
      features = res;
    }
    return features;
  }

  @Override
  public boolean hasCachedFeatures() {
    return features != null;
  }

  /** The cache for aggregated values. */
  private double[][] aggs;

  /**
   * Setter.
   * 
   * @param agg The column aggregation function.
   * @param arr The array.
   */
  private void setCachedArray(final ColumnAggregation agg, final double[] arr) {
    final int ord = agg.ordinal();
    if(aggs == null) {
      aggs = new double[ColumnAggregation.aggregationTypeCount()][];
    } else if(ord >= aggs.length) {
      aggs = Arrays.copyOf(aggs, ColumnAggregation.aggregationTypeCount());
    }
    aggs[ord] = arr;
  }

  /**
   * Getter.
   * 
   * @param agg The column aggregation function.
   * @return The array.
   */
  private double[] getCacheArray(final ColumnAggregation agg) {
    final int ord = agg.ordinal();
    if(aggs != null && ord < aggs.length) {
      final double[] res = aggs[ord];
      if(res != null) return res;
    }
    final double[] arr = new double[cols];
    Arrays.fill(arr, Double.NaN);
    setCachedArray(agg, arr);
    return arr;
  }

  @Override
  protected double getCachedValue(final ColumnAggregation agg, final int col) {
    if(aggs == null) return Double.NaN;
    final int ord = agg.ordinal();
    if(ord >= aggs.length) return Double.NaN;
    final double[] arr = aggs[ord];
    if(arr == null) return Double.NaN;
    return arr[col];
  }

  @Override
  protected void setCachedValue(final ColumnAggregation agg, final int col, final double v) {
    getCacheArray(agg)[col] = v;
  }

  /**
   * Creates a bit set from the given table.
   * 
   * @param table The table.
   * @return The bit set representing all categorical columns or
   *         <code>null</code> if no column is categorical.
   */
  private static BitSet categoricalBitSet(final DataTable table) {
    final int cols = table.cols();
    final BitSet set = new BitSet();
    for(int c = 0; c < cols; ++c) {
      set.set(c, table.isCategorical(c));
    }
    return set.isEmpty() ? null : set;
  }

}
