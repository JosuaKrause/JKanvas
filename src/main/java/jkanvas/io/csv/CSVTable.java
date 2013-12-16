package jkanvas.io.csv;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jkanvas.table.DataTable;
import jkanvas.util.Resource;

/**
 * Reads a CSV file and represents it as numerical table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class CSVTable extends DataTable {

  /** The rows. */
  private final List<CSVRow> rows;
  /** All columns with string data. */
  private final Map<Integer, List<String>> values;
  /** All numerical columns. */
  private final BitSet numerical;
  /** Identified columns. */
  private final BitSet known;
  /** The string indicating a missing value. */
  private final String missing;
  /** The names of the columns. */
  private final String[] names;
  /** First row. */
  private final CSVRow first;

  /**
   * Creates a CSV reader table.
   * 
   * @param reader The CSV reader.
   * @param r The source.
   * @param missing The missing value string.
   */
  public CSVTable(final CSVReader reader, final Resource r, final String missing) {
    Objects.requireNonNull(reader);
    Objects.requireNonNull(r);
    this.missing = Objects.requireNonNull(missing);
    rows = new ArrayList<>();
    int cols = -1;
    for(final CSVRow row : CSVReader.readRows(r, reader)) {
      if(cols < 0) {
        cols = row.highestIndex();
        if(cols <= 0) throw new IllegalArgumentException("no cols");
      }
      rows.add(row);
      if(cols != row.highestIndex()) throw new IllegalArgumentException(
          "varying cols: " + (cols + 1) + " got " + (row.highestIndex() + 1));
    }
    if(rows.isEmpty()) throw new IllegalArgumentException("no rows");
    first = rows.get(0);
    values = new HashMap<>();
    numerical = new BitSet();
    names = new String[cols + 1];
    for(int c = 0; c < names.length; ++c) {
      names[c] = sanitizeName(first.getTitle(c));
    }
    known = new BitSet(names.length);
  }

  @Override
  public int rows() {
    return rows.size();
  }

  @Override
  public int cols() {
    return first.highestIndex() + 1;
  }

  @Override
  public double getAt(final int row, final int col) {
    final CSVRow r = rows.get(row);
    final String str = r.get(col);
    if(missing.equals(str)) return Double.NaN;
    known.set(col);
    final List<String> v = values.get(col);
    if(v != null) {
      // expect string
      if(numerical.get(col)) throw new IllegalArgumentException(
          "column " + col + " contains numerical and string data");
      final int index = v.indexOf(str);
      if(index >= 0) return index;
      // new string detected
      final int res = v.size();
      v.add(str);
      // check whether it is a number
      // FIXME ***hack***
      try {
        Double.parseDouble(str);
        throw new IllegalArgumentException(
            "column " + col + " contains numerical and string data");
      } catch(final NumberFormatException e) {
        // expected
      }
      // ***hack***
      return res;
    }
    try {
      // numerical value?
      final double d = Double.parseDouble(str);
      numerical.set(col);
      return d;
    } catch(final NumberFormatException e) {
      if(numerical.get(col)) throw new IllegalArgumentException(
          "column " + col + " contains numerical and string data");
      // found new string column
      final List<String> vals = new ArrayList<>();
      vals.add(str);
      values.put(col, vals);
      return 0;
    }
  }

  @Override
  public String getName(final int col) {
    return names[col];
  }

  @Override
  public boolean isCategorical(final int col) {
    if(!known.get(col)) {
      int row = 0;
      do {
        getAt(row++, col);
      } while(!known.get(col) && row < rows());
    }
    return !numerical.get(col);
  }

  /**
   * Writes the contents of the table to the writer.
   * 
   * @param table The table.
   * @param out The writer.
   * @param titles Whether to include titles.
   */
  public static final void writeTable(
      final DataTable table, final CSVWriter out, final boolean titles) {
    final int cols = table.cols();
    if(titles) {
      for(int c = 0; c < cols; ++c) {
        out.writeCell(table.getName(c));
      }
    }
    out.writeRow();
    final int rows = table.rows();
    for(int r = 0; r < rows; ++r) {
      for(int c = 0; c < cols; ++c) {
        final double v = table.getAt(r, c);
        if(table.isCategorical(c)) {
          out.writeCell("cat" + v);
        } else {
          out.writeCell(v);
        }
      }
      out.writeRow();
    }
  }

}
