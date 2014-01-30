package jkanvas.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An enumeration of aggregation functions.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class ColumnAggregation {

  /** A list of all registered column aggregators. */
  private static final List<ColumnAggregation> VALUES = new ArrayList<>();

  /** The minimum of the column. */
  // TODO #43 -- Java 8 simplification
  public static final ColumnAggregation MINIMUM = new ColumnAggregation("min") {

    @Override
    protected double aggregate(final DataTable table, final int col) {
      double min = Double.POSITIVE_INFINITY;
      for(int i = 0; i < table.rows(); ++i) {
        final double v = table.getAt(i, col);
        if(min > v) {
          min = v;
        }
      }
      return min;
    }

  };

  /** The maximum of the column. */
  // TODO #43 -- Java 8 simplification
  public static final ColumnAggregation MAXIMUM = new ColumnAggregation("max") {

    @Override
    protected double aggregate(final DataTable table, final int col) {
      double max = Double.NEGATIVE_INFINITY;
      for(int i = 0; i < table.rows(); ++i) {
        final double v = table.getAt(i, col);
        if(max < v) {
          max = v;
        }
      }
      return max;
    }

  };

  /** The mean value of the column. */
  // TODO #43 -- Java 8 simplification
  public static final ColumnAggregation MEAN = new ColumnAggregation("mean") {

    @Override
    protected double aggregate(final DataTable table, final int col) {
      final int rows = table.rows();
      double sum = 0;
      for(int i = 0; i < rows; ++i) {
        sum += table.getAt(i, col);
      }
      return sum / rows;
    }

  };

  /** The standard deviation. */
  // TODO #43 -- Java 8 simplification
  public static final ColumnAggregation STD_DEVIATION = new ColumnAggregation("std") {

    @Override
    protected double aggregate(final DataTable table, final int col) {
      final double mean = MEAN.getValue(table, col);
      final int rows = table.rows();
      double sum = 0;
      for(int i = 0; i < rows; ++i) {
        final double v = table.getAt(i, col);
        sum += (v - mean) * (v - mean);
      }
      return Math.sqrt(sum / rows);
    }

  };

  /**
   * Getter.
   * 
   * @return The number of registered aggregators.
   */
  public static final int aggregationTypeCount() {
    return VALUES.size();
  }

  /**
   * Getter.
   * 
   * @return A list of all registered aggregators.
   */
  public static final List<ColumnAggregation> values() {
    return Collections.unmodifiableList(VALUES);
  }

  /** The name of this aggregator. */
  private final String name;
  /** The ordinal value of this aggregator. */
  private int ord;

  /**
   * Creates an aggregator. An aggregator must not have a state and should be
   * unique.
   * 
   * @param name The name of the aggregator.
   */
  public ColumnAggregation(final String name) {
    this.name = Objects.requireNonNull(name);
    synchronized(ColumnAggregation.class) {
      ord = VALUES.size();
      VALUES.add(this);
    }
  }

  /**
   * Computes the actual aggregation.
   * 
   * @param table The table.
   * @param col The column.
   * @return The aggregated value.
   */
  protected abstract double aggregate(DataTable table, int col);

  /**
   * Computes the aggregation for the given column.
   * 
   * @param table The table.
   * @param col The column.
   * @return The value.
   */
  public final double getValue(final DataTable table, final int col) {
    final double c = table.getCachedValue(this, col);
    if(!Double.isNaN(c)) return c;
    final double v = aggregate(table, col);
    table.setCachedValue(this, col, v);
    return v;
  }

  /**
   * Getter.
   * 
   * @return The name of this aggregator.
   */
  public final String name() {
    return name;
  }

  /**
   * Getter.
   * 
   * @return The ordinal value of this aggregator.
   */
  public final int ordinal() {
    return ord;
  }

}
