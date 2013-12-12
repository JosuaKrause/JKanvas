package jkanvas.table;

/**
 * A column normalized representation of a table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class NormalizedTable extends WrappedTable {

  /**
   * Creates a column normalized version of the given table.
   * 
   * @param table The table.
   */
  public NormalizedTable(final DataTable table) {
    super(table.cached());
  }

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
    return table.getName(col);
  }

  @Override
  public double getAt(final int row, final int col) {
    return table.getMinMaxScaled(row, col);
  }

  @Override
  public double getMinMaxScaled(final int row, final int col) {
    return getAt(row, col);
  }

  @Override
  protected double getCachedValue(final ColumnAggregation agg, final int col) {
    if(ColumnAggregation.MAXIMUM == agg || ColumnAggregation.MINIMUM == agg) return (agg == ColumnAggregation.MAXIMUM)
        ? 1.0
        : 0.0;
    return super.getCachedValue(agg, col);
  }

  @Override
  protected void setCachedValue(final ColumnAggregation agg, final int col, final double v) {
    if(ColumnAggregation.MAXIMUM == agg || ColumnAggregation.MINIMUM == agg) throw new IllegalStateException(
        "min and max cannot be set: " + agg);
    super.setCachedValue(agg, col, v);
  }

}
