package jkanvas.animation;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import jkanvas.util.BitSetIterable;

/**
 * A list of paint-able objects. The class is not fully guaranteed to be thread
 * safe.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The modifiable shape object.
 */
public abstract class GenericPaintList<T extends Shape> {

  /** The number of dimensions. */
  private final int dims;
  /** The number of colors. */
  private final int cols;
  /** The active elements. */
  private final BitSet actives;
  /** The visible elements. An element can only be visible when it is active. */
  private final BitSet visibles;
  /** The capacity. */
  private int capacity;
  /** The values. */
  private double[] cur;
  /** The colors. */
  private Color[] colors;

  /**
   * Creates an empty list.
   * 
   * @param numberOfDimensions The number of dimensions.
   * @param numberOfColors The number of colors.
   * @param initialSize The initial capacity.
   */
  public GenericPaintList(final int numberOfDimensions,
      final int numberOfColors, final int initialSize) {
    if(numberOfColors < 0) throw new IllegalArgumentException(
        "must be larger or equal to 0: " + numberOfColors);
    if(numberOfDimensions <= 0) throw new IllegalArgumentException(
        "must be larger than 0: " + numberOfDimensions);
    dims = numberOfDimensions;
    cols = numberOfColors;
    final int is = Math.max(128, initialSize);
    capacity = is;
    cur = new double[numberOfDimensions * is];
    colors = new Color[numberOfColors * is];
    actives = new BitSet();
    visibles = new BitSet();
  }

  /** Reduces the capacity of the arrays to the highest active index. */
  public void trimToSize() {
    setCapacity(actives.length());
  }

  /**
   * Sets the capacity of all arrays.
   * 
   * @param newSize The new capacity.
   */
  private void setCapacity(final int newSize) {
    if(newSize == capacity()) return;
    synchronized(actives) {
      capacity = newSize;
      cur = Arrays.copyOf(cur, newSize * dims);
      colors = Arrays.copyOf(colors, newSize * cols);
    }
  }

  /** Adds more capacity. */
  protected void enlarge() {
    synchronized(actives) {
      final int curSize = Math.max(2, capacity);
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
    return capacity;
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

  /** Removes all active indices. */
  public void clear() {
    synchronized(actives) {
      actives.clear();
      visibles.clear();
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
   * Ensures that the given index is active. This check must be made before
   * accessing elements when they are not guaranteed to be active.
   * 
   * @param index The index.
   */
  protected void ensureActive(final int index) {
    if(!isActive(index)) throw new IllegalArgumentException(index + " not active");
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The position in the array.
   */
  protected int getPosition(final int index) {
    return index * dims;
  }

  /**
   * Getter. This method does no checks.
   * 
   * @param dim The dimension.
   * @param pos The position in the array. Use {@link #getPosition(int)}.
   * @return The value at the given index for the dimension.
   * @see #ensureActive(int)
   */
  protected double get(final int dim, final int pos) {
    return cur[pos + dim];
  }

  /**
   * Setter. This method does no checks.
   * 
   * @param dim The dimension.
   * @param pos The position in the array. Use {@link #getPosition(int)}.
   * @param val The value at the given index for the dimension.
   * @see #ensureActive(int)
   */
  protected void set(final int dim, final int pos, final double val) {
    cur[pos + dim] = val;
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The position in the color array.
   */
  protected int getColorPosition(final int index) {
    return index * cols;
  }

  /**
   * Getter. This method does no checks.
   * 
   * @param col The color column.
   * @param pos The position in the array. Use {@link #getColorPosition(int)}.
   * @return The color of the given index in the column.
   * @see #ensureActive(int)
   */
  protected Color getColor(final int col, final int pos) {
    return colors[pos + col];
  }

  /**
   * Setter. This method does no checks.
   * 
   * @param col The color column.
   * @param pos The position in the array. Use {@link #getColorPosition(int)}.
   * @param color The color of the given index in the column.
   * @see #ensureActive(int)
   */
  protected void setColor(final int col, final int pos, final Color color) {
    colors[pos + col] = color;
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
   * Getter.
   * 
   * @return An iteration over all active indices.
   */
  public Iterable<Integer> actives() {
    return new BitSetIterable(actives);
  }

  /**
   * Getter.
   * 
   * @return An iteration over all visible indices.
   */
  public Iterable<Integer> visibles() {
    return new BitSetIterable(visibles);
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
    final Composite defaultComposite = gfx.getComposite();
    final T drawObject = createDrawObject();
    for(int i = visibles.nextSetBit(0); i >= 0; i = visibles.nextSetBit(i + 1)) {
      int pos = getPosition(i);
      int cpos = getColorPosition(i);
      // we know the current index is set so we can start at the next position
      final int endOfRun = visibles.nextClearBit(i + 1);
      do {
        paint(gfx, drawObject, i, pos, cpos, defaultComposite);
        pos += dims;
        cpos += cols;
      } while(++i < endOfRun);
    }
  }

  /**
   * Paints an object. No bounds need to be checked and the index is guaranteed
   * to be active.
   * 
   * @param gfx The graphics context. The context must not be altered.
   * @param obj The draw shape. The shape must be set to the correct values.
   * @param index The index to draw.
   * @param pos The position in the array.
   * @param cpos The position in the color array.
   * @param defaultComposite The initial composite. When wanting to use alpha
   *          blending this composite can be used to reset the state of the
   *          graphics context.
   */
  protected abstract void paint(Graphics2D gfx, T obj, int index,
      int pos, int cpos, Composite defaultComposite);

  /**
   * Returns the first element that contains the given point.
   * 
   * @param point The point.
   * @return The index of the first element that contains the given point or
   *         <code>-1</code> if no element is hit.
   */
  public int hit(final Point2D point) {
    final T drawObject = createDrawObject();
    for(int i = visibles.length() - 1; i >= 0; i = visibles.previousSetBit(i - 1)) {
      int pos = getPosition(i);
      // we know the current index is set, so we can start looking one below
      final int endOfRun = visibles.previousClearBit(i - 1);
      do {
        if(contains(point, drawObject, i, pos)) return i;
        pos -= dims;
      } while(--i > endOfRun);
      if(i < 1) {
        break;
      }
    }
    return -1;
  }

  /**
   * Fills the given list with elements hit by the given area.
   * 
   * @param area The area.
   * @param elements The element indices.
   */
  public void hit(final Shape area, final List<Integer> elements) {
    final T drawObject = createDrawObject();
    final Area a = new Area(area);
    for(int i = visibles.length() - 1; i >= 0; i = visibles.previousSetBit(i - 1)) {
      int pos = getPosition(i);
      // we know the current index is set, so we can start looking one below
      final int endOfRun = visibles.previousClearBit(i - 1);
      do {
        if(intersects(a, drawObject, i, pos)) {
          elements.add(i);
        }
        pos -= dims;
      } while(--i > endOfRun);
      if(i < 1) {
        break;
      }
    }
  }

  /**
   * Checks whether the given point is contained in the given element. No bounds
   * need to be checked and the index is guaranteed to be active.
   * 
   * @param point The point.
   * @param obj The element. The shape must be set to the correct values.
   * @param index The index of the element.
   * @param pos The position in the array.
   * @return Whether the element contains the point.
   */
  protected abstract boolean contains(Point2D point, T obj, int index, int pos);

  /**
   * Checks whether the given area intersects with the given element. No bounds
   * need to be checked and the index is guaranteed to be active.
   * 
   * @param area The area.
   * @param obj The element. The shape must be set to the correct values.
   * @param index The index of the element.
   * @param pos The position in the array.
   * @return Whether the element intersects the area.
   */
  protected abstract boolean intersects(Area area, T obj, int index, int pos);

  /**
   * Getter.
   * 
   * @return The number of active objects.
   */
  public int cardinality() {
    return actives.cardinality();
  }

  /**
   * Getter.
   * 
   * @return The number of visible objects.
   */
  public int visibleCardinality() {
    return visibles.cardinality();
  }

  /**
   * Getter.
   * 
   * @return The highest active index plus one.
   */
  public int length() {
    return actives.length();
  }

}
