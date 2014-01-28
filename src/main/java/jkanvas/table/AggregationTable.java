package jkanvas.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A table that has aggregation values as cells. The columns correspond to
 * {@link ColumnAggregation aggregators} and the rows to features of other
 * tables.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AggregationTable extends DataTable {

  /** The included features. */
  private final List<Feature> features;
  /** The name seed for column names. */
  private final String nameSeed;

  /** Creates an empty aggregation table. */
  public AggregationTable() {
    this(generateName());
  }

  /**
   * Creates an empty aggregation table.
   * 
   * @param nameSeed The name seed for column names.
   */
  public AggregationTable(final String nameSeed) {
    this.nameSeed = Objects.requireNonNull(nameSeed);
    features = new ArrayList<>();
  }

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The aggregator corresponding to the column.
   */
  private static ColumnAggregation get(final int col) {
    return ColumnAggregation.values().get(col);
  }

  @Override
  public int rows() {
    return features.size();
  }

  /**
   * Adds a feature.
   * 
   * @param f The feature.
   * @return The index of the new row.
   */
  public int addRow(final Feature f) {
    final int row = features.size();
    features.add(Objects.requireNonNull(f));
    return row;
  }

  @Override
  public int cols() {
    return ColumnAggregation.aggregationTypeCount();
  }

  @Override
  public double getAt(final int row, final int col) {
    return features.get(row).aggregate(get(col));
  }

  @Override
  public boolean isCategorical(final int col) {
    return false;
  }

  @Override
  public String getName(final int col) {
    return nameSeed + "-" + get(col).name();
  }

}
