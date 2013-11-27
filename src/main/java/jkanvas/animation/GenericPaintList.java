package jkanvas.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.BitSet;

/**
 * A list of paint-able objects. The class is not fully guaranteed to be thread
 * safe.
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
  /** The visible elements. An element can only be visible when it is active. */
  private final BitSet visibles;

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
    visibles = new BitSet();
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
      visibles.set(nextIndex);
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
      visibles.set(index, false);
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
      visibles.set(fromIndex, toIndex, false);
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
   * Ensures that the given index is active.
   * 
   * @param index The index.
   */
  private void ensureActive(final int index) {
    if(!isActive(index)) throw new IllegalArgumentException(index + " not active");
  }

  /**
   * Getter.
   * 
   * @param dim The dimension.
   * @param index The index.
   * @return The value at the given index for the dimension.
   */
  protected double get(final int dim, final int index) {
    ensureActive(index);
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
    ensureActive(index);
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
    ensureActive(index);
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
    ensureActive(index);
    colors[col][index] = color;
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return Whether the index is visible.
   */
  public boolean isVisible(final int index) {
    // no need to ensure being active here since it
    // can only be visible when active
    return visibles.get(index);
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param isVisible Whether the index is visible.
   */
  public void setVisible(final int index, final boolean isVisible) {
    ensureActive(index);
    visibles.set(index, isVisible);
  }

  /**
   * Creates the modifiable shape.
   * 
   * @return The modifiable shape.
   */
  protected abstract T createDrawObject();

  /**
   * Paints all visible objects.
   * 
   * @param gfx The graphics context.
   */
  public void paintAll(final Graphics2D gfx) {
    final T drawObject = createDrawObject();
    for(int i = visibles.nextSetBit(0); i >= 0; i = visibles.nextSetBit(i + 1)) {
      final int endOfRun = visibles.nextClearBit(i);
      do {
        paint(gfx, drawObject, i);
      } while(++i < endOfRun);
    }
  }

  /**
   * Paints an object.
   * 
   * @param gfx The graphics context. The context must not be altered.
   * @param obj The draw shape. The shape must be set to the correct values.
   * @param index The index to draw.
   */
  protected abstract void paint(Graphics2D gfx, T obj, int index);

  /**
   * Returns the first element that contains the given point.
   * 
   * @param point The point.
   * @return The index of the first element that contains the given point or
   *         <code>-1</code> if no element is hit.
   */
  public int hit(final Point2D point) {
    final T drawObject = createDrawObject();
    for(int i = visibles.nextSetBit(0); i >= 0; i = visibles.nextSetBit(i + 1)) {
      final int endOfRun = visibles.nextClearBit(i);
      do {
        if(contains(point, drawObject, i)) return i;
      } while(++i < endOfRun);
    }
    return -1;
  }

  /**
   * Checks whether the given point is contained in the given element.
   * 
   * @param point The point.
   * @param obj The element. The shape must be set to the correct values.
   * @param index The index of the element.
   * @return Whether the element contains the point.
   */
  protected abstract boolean contains(Point2D point, T obj, int index);

  /**
   * Getter.
   * 
   * @return The number of active objects.
   */
  public int length() {
    return actives.cardinality();
  }

}
