package jkanvas.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Arrays;
import java.util.BitSet;

/**
 * A list of paint-able objects.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The modifiable shape object.
 */
public abstract class GenericPaintList<T extends Shape> {

  /** The values. */
  private final double[][] cur;
  /** The colors. */
  private final Color[][] colors;
  /** The active elements. */
  private final BitSet actives;

  /**
   * Creates an empty list.
   * 
   * @param numberOfDimensions The number of dimensions.
   * @param numberOfColors The number of colors.
   * @param initialSize The initial capacity.
   */
  public GenericPaintList(final int numberOfDimensions,
      final int numberOfColors, final int initialSize) {
    if(numberOfDimensions <= 0) throw new IllegalArgumentException(
        "must be larger than 0: " + numberOfDimensions);
    final int is = Math.max(128, initialSize);
    cur = new double[numberOfDimensions][is];
    colors = new Color[numberOfColors][is];
    actives = new BitSet();
  }

  /** Reduces the capacity of the arrays to the highest active index. */
  public void trimToSize() {
    setCapacity(length());
  }

  /**
   * Sets the capacity of all arrays.
   * 
   * @param newSize The new capacity.
   */
  private void setCapacity(final int newSize) {
    if(newSize == capacity()) return;
    synchronized(actives) {
      for(int d = 0; d < cur.length; ++d) {
        cur[d] = Arrays.copyOf(cur[d], newSize);
      }
      for(int c = 0; c < colors.length; ++c) {
        colors[c] = Arrays.copyOf(colors[c], newSize);
      }
    }
  }

  /** Adds more capacity. */
  protected void enlarge() {
    synchronized(actives) {
      final int curSize = Math.max(2, actives.length());
      final int newSize = curSize + curSize / 2;
      setCapacity(newSize);
    }
  }

  /**
   * Makes room for a new object. The capacity is increased if necessary.
   * 
   * @return The index to the new object. Note that the values must be manually
   *         initialized.
   */
  protected int addIndex() {
    final int nextIndex;
    synchronized(actives) {
      nextIndex = actives.nextClearBit(0);
      actives.set(nextIndex);
      if(nextIndex >= capacity()) {
        enlarge();
      }
    }
    return nextIndex;
  }

  /**
   * Getter.
   * 
   * @return The capacity of the arrays.
   */
  protected int capacity() {
    return cur[0].length;
  }

  /**
   * Removes the index at the given position.
   * 
   * @param index The index to remove.
   */
  public void removeIndex(final int index) {
    synchronized(actives) {
      actives.set(index, false);
    }
  }

  /**
   * Removes a range of indices.
   * 
   * @param fromIndex The inclusive lowest index.
   * @param toIndex The exclusive highest index.
   */
  public void removeRange(final int fromIndex, final int toIndex) {
    synchronized(actives) {
      actives.set(fromIndex, toIndex, false);
    }
  }

  /**
   * Whether the object at the given index is active.
   * 
   * @param index The index.
   * @return Whether the object is active.
   */
  public boolean isActive(final int index) {
    return actives.get(index);
  }

  /**
   * Getter.
   * 
   * @param dim The dimension.
   * @param index The index.
   * @return The value at the given index for the dimension.
   */
  protected double get(final int dim, final int index) {
    return cur[dim][index];
  }

  /**
   * Setter.
   * 
   * @param dim The dimension.
   * @param index The index.
   * @param val The value at the given index for the dimension.
   */
  protected void set(final int dim, final int index, final double val) {
    cur[dim][index] = val;
  }

  /**
   * Getter.
   * 
   * @param col The color column.
   * @param index The index.
   * @return The color of the given index in the column.
   */
  protected Color getColor(final int col, final int index) {
    return colors[col][index];
  }

  /**
   * Setter.
   * 
   * @param col The color column.
   * @param index The index.
   * @param color The color of the given index in the column.
   */
  protected void setColor(final int col, final int index, final Color color) {
    colors[col][index] = color;
  }

  /**
   * Creates the modifiable shape.
   * 
   * @return The modifiable shape.
   */
  protected abstract T createDrawObject();

  /**
   * Paints all objects.
   * 
   * @param gfx The graphics context.
   */
  public void paintAll(final Graphics2D gfx) {
    final T drawObject = createDrawObject();
    for(int i = actives.nextSetBit(0); i >= 0; i = actives.nextSetBit(i + 1)) {
      paint(gfx, drawObject, i);
    }
  }

  /**
   * Paints an object.
   * 
   * @param gfx The graphics context. The context must not be altered.
   * @param obj The draw shape.
   * @param index The index to draw.
   */
  protected abstract void paint(Graphics2D gfx, T obj, int index);

  /**
   * Getter.
   * 
   * @return The number of active objects.
   */
  public int length() {
    return actives.cardinality();
  }

}
