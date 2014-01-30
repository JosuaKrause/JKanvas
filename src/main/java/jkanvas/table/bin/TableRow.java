package jkanvas.table.bin;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import jkanvas.table.DataTable;

/**
 * A row of a table. Tha values are binned.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class TableRow implements Iterable<Integer>, Comparable<TableRow> {

  /** The table. */
  private final DataTable table;
  /** The bins. */
  private final ColumnBinner[] bins;
  /** The row. */
  private final int row;

  /**
   * Creates a table row.
   * 
   * @param t The table.
   * @param row The row.
   * @param bins The bins per column. This array must not be altered from
   *          outside and it must contain all columns in the correct order.
   */
  TableRow(final DataTable t, final int row, final ColumnBinner[] bins) {
    // bins must not be changed afterwards
    this.bins = Objects.requireNonNull(bins);
    table = Objects.requireNonNull(t).cached();
    // bins is assumed to be filled correctly
    if(bins.length != table.cols()) throw new IllegalArgumentException(
        "inconsistent column count: " + bins.length + " != " + table.cols());
    int c = 0;
    for(final ColumnBinner bin : bins) {
      Objects.requireNonNull(bin);
      if(!table.getFeature(c).equals(bin.getFeature())) throw new IllegalArgumentException();
      ++c;
    }
    this.row = row;
  }

  /**
   * Getter.
   * 
   * @return The number of columns.
   */
  public int cols() {
    return bins.length;
  }

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The number of bins in the given column.
   */
  public int binCount(final int col) {
    return bins[col].bins();
  }

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The bin in which the value at the given position is in.
   */
  public int get(final int col) {
    return bins[col].binFor(table.getAt(row, col));
  }

  /**
   * Getter.
   * 
   * @return The row.
   */
  public int getRow() {
    return row;
  }

  /**
   * Getter.
   * 
   * @return The table. This may be a cached version of the original table.
   */
  public DataTable getTable() {
    return table;
  }

  /**
   * The lowest value of the given bin.
   * 
   * @param col The column.
   * @param bin The bin in the column.
   * @return The lowest value of the bin.
   */
  public double fromValue(final int col, final int bin) {
    return bins[col].getMinValueOf(bin);
  }

  /**
   * The highest value of the given bin.
   * 
   * @param col The column.
   * @param bin The bin in the column.
   * @return The highest value of the bin. The value is exclusive unless it is
   *         the highest bin of the column.
   */
  public double toValue(final int col, final int bin) {
    return bins[col].getMinValueOf(bin + 1);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {

      private int c = 0;

      @Override
      public boolean hasNext() {
        return c < cols();
      }

      @Override
      public Integer next() {
        if(!hasNext()) throw new NoSuchElementException();
        return get(c++);
      }

      @Override
      // TODO #43 -- Java 8 simplification
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  /** The cached hash. */
  private int hash = 0;

  @Override
  public int hashCode() {
    if(hash == 0) {
      int h = 1;
      for(final int b : this) {
        h += 31 * b;
      }
      if(h == 0) {
        h = 1;
      }
      hash = h;
    }
    return hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof TableRow)) return false;
    final TableRow tr = ((TableRow) obj);
    if(row != tr.row) return false;
    // shortcut only when the tables are completely identical
    if(row == tr.row && table == tr.table) return true;
    return equalContent(this, tr);
  }

  @Override
  public int compareTo(final TableRow o) {
    final Iterator<Integer> itA = iterator();
    final Iterator<Integer> itB = o.iterator();
    while(itA.hasNext()) {
      if(!itB.hasNext()) return 1;
      final int a = itA.next();
      final int b = itB.next();
      if(a != b) return Integer.compare(a, b);
    }
    if(itB.hasNext()) return -1;
    return Integer.compare(row, o.row);
  }

  /**
   * Whether both rows have the same content. This is independent from the row
   * number.
   * 
   * @param a The first row.
   * @param b The second row.
   * @return Whether both rows have equal content.
   */
  public static final boolean equalContent(final TableRow a, final TableRow b) {
    final Iterator<Integer> itA = a.iterator();
    final Iterator<Integer> itB = b.iterator();
    while(itA.hasNext()) {
      if(!itB.hasNext()) return false;
      if(itA.next() != itB.next()) return false;
    }
    return !itB.hasNext();
  }

}
