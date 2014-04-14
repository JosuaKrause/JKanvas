package jkanvas.io.csv;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A CSV row.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public final class CSVRow {

  /** The title map. */
  private final Map<String, String> map = new HashMap<>();
  /** The index map. */
  private String[] indexed;
  /** The column titles. */
  private String[] titles;
  /** The current max index. */
  private int maxIndex;

  /**
   * Creates a row with a predefined number of columns. The number may but
   * should not be different.
   *
   * @param numCols The estimated number of columns.
   */
  public CSVRow(final int numCols) {
    indexed = new String[numCols];
    titles = new String[numCols];
  }

  /**
   * Adds a cell to the row.
   *
   * @param index The index of the cell.
   * @param name The name/title of the column.
   * @param value The content of the cell.
   */
  protected void addCell(final int index, final String name, final String value) {
    final String n = name != null ? name : "" + index;
    if(index >= indexed.length) {
      indexed = Arrays.copyOf(indexed, index + 1);
      titles = Arrays.copyOf(titles, index + 1);
    }
    maxIndex = Math.max(index, maxIndex);
    indexed[index] = value;
    titles[index] = n;
    map.put(n, value);
  }

  /**
   * Whether the given title is present.
   *
   * @param name The title.
   * @return Whether the title is present.
   */
  public boolean has(final String name) {
    return map.containsKey(name);
  }

  /**
   * Checks whether the given index exists.
   *
   * @param i The index.
   * @return Whether the index exists.
   */
  public boolean hasIndex(final int i) {
    return i >= 0 && i <= maxIndex;
  }

  /**
   * Getter.
   *
   * @return The highest used index.
   */
  public int highestIndex() {
    return maxIndex;
  }

  /**
   * Getter.
   *
   * @param index The index.
   * @return The content at the given index.
   */
  public String get(final int index) {
    if(!hasIndex(index)) throw new IndexOutOfBoundsException(
        "max index: " + maxIndex + " index: " + index);
    return indexed[index];
  }

  /**
   * Getter.
   *
   * @param name The title.
   * @return The content of the cell with the given column name.
   */
  public String get(final String name) {
    return map.get(name);
  }

  /**
   * Getter.
   *
   * @param index The index.
   * @return The title of the given index.
   */
  public String getTitle(final int index) {
    return titles[index];
  }

  /**
   * Getter.
   *
   * @return A set of available names.
   */
  public Set<String> names() {
    return Collections.unmodifiableSet(map.keySet());
  }

  @Override
  public String toString() {
    return Arrays.toString(indexed);
  }

}
