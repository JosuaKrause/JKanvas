package jkanvas.table;

import java.util.Objects;

/**
 * A table representing a collection of features.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class FeatureTable extends DataTable {

  /** The features. */
  private final Feature[] features;
  /** The number of rows. */
  private final int rows;

  /**
   * Creates a table from given features.
   * 
   * @param features The features. All features must have the same number of
   *          rows.
   */
  public FeatureTable(final Feature[] features) {
    Objects.requireNonNull(features);
    int rows = -1;
    for(int r = 0; r < features.length; ++r) {
      final int l = features[r].rows();
      if(rows < 0) {
        rows = l;
      } else if(rows != l) throw new IllegalArgumentException(
          "inconsistent row count: " + rows + " " + l);
    }
    this.rows = rows >= 0 ? rows : 0;
    this.features = features.clone();
  }

  @Override
  public int rows() {
    return rows;
  }

  @Override
  public int cols() {
    return features.length;
  }

  @Override
  public double getAt(final int row, final int col) {
    return features[col].getElement(row);
  }

}
