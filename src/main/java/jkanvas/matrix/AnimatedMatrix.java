package jkanvas.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import jkanvas.RefreshManager;
import jkanvas.animation.AnimatedDouble;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationList;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.util.ArrayUtil;
import jkanvas.util.Swapable;

/**
 * A matrix that has a mutable number of rows and columns. Rows and columns can
 * be added, remove, and replaced with optional animations.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public class AnimatedMatrix<T>
    extends AbstractMatrix<T> implements MutableMatrix<T>, PermutableMatrix<T> {

  /** The matrix content. */
  private final List<List<T>> matrix;
  /** The widths of the columns. */
  private final List<AnimatedDouble> widths;
  /** The heights of the rows. */
  private final List<AnimatedDouble> heights;
  /** The names of the rows. */
  private final List<String> rowNames;
  /** The names of the columns. */
  private final List<String> colNames;
  /** The number of columns. */
  private int cols;
  /**
   * Whether further change is currently allowed. Further change is only not
   * allowed when rows or columns are removed with animation.
   */
  protected boolean noChange;

  /**
   * Creates an animated matrix with one cell.
   * 
   * @param init The content of the initial cell.
   * @param w The width of the initial cell.
   * @param h The height of the initial cell.
   * @param row The row name of the initial cell.
   * @param col The column name of the initial cell.
   */
  public AnimatedMatrix(final T init, final double w, final double h,
      final String row, final String col) {
    matrix = new ArrayList<>();
    final ArrayList<T> tmp = new ArrayList<>();
    tmp.add(init);
    matrix.add(tmp);
    widths = new ArrayList<>();
    widths.add(new AnimatedDouble(w));
    heights = new ArrayList<>();
    heights.add(new AnimatedDouble(h));
    rowNames = new ArrayList<>();
    rowNames.add(Objects.requireNonNull(row));
    colNames = new ArrayList<>();
    colNames.add(Objects.requireNonNull(col));
    cols = 1;
  }

  /**
   * Creates an animated matrix.
   * 
   * @param m The initial matrix with rows and then columns.
   * @param widths The widths of the columns.
   * @param heights The heights of the rows.
   * @param rowNames The names of the rows.
   * @param colNames The names of the columns.
   */
  public AnimatedMatrix(final T[][] m, final double[] widths, final double[] heights,
      final String[] rowNames, final String[] colNames) {
    final int rows = m.length;
    cols = m[0].length;
    matrix = new ArrayList<>(rows);
    for(final T[] row : m) {
      if(row.length != cols) throw new IllegalArgumentException(
          "inconsistent row length: " + cols + " != " + row.length);
      matrix.add(new ArrayList<>(Arrays.asList(row)));
    }
    this.widths = new ArrayList<>(widths.length);
    for(final double w : widths) {
      this.widths.add(new AnimatedDouble(w));
    }
    if(this.widths.size() != cols) throw new IllegalArgumentException(
        this.widths.size() + " != " + cols);
    this.heights = new ArrayList<>(heights.length);
    for(final double h : heights) {
      this.heights.add(new AnimatedDouble(h));
    }
    if(this.heights.size() != rows) throw new IllegalArgumentException(
        this.heights.size() + " != " + rows);
    this.rowNames = new ArrayList<>(Arrays.asList(rowNames));
    if(this.rowNames.size() != rows) throw new IllegalArgumentException(
        this.rowNames.size() + " != " + rows);
    this.colNames = new ArrayList<>(Arrays.asList(colNames));
    if(this.colNames.size() != cols) throw new IllegalArgumentException(
        this.colNames.size() + " != " + cols);
  }

  /** Ensures that no further changes happen during animated removal. */
  private void ensureChangeAllowed() {
    // TODO find alternative to animated removal... -- maybe treat the rows and
    // columns already as deleted...
    if(noChange) throw new IllegalStateException("no change allowed");
  }

  /**
   * Adds rows.
   * 
   * @param index The index to insert the rows.
   * @param rows A list of all rows to add.
   * @param names The names of the new rows.
   * @param initHeight The initial height for the rows.
   * @param heights The actual heights of the rows.
   * @param timing The animation timing.
   */
  public synchronized void addRows(final int index, final List<List<T>> rows,
      final List<String> names, final double initHeight, final List<Double> heights,
      final AnimationTiming timing) {
    ensureChangeAllowed();
    Objects.requireNonNull(timing);
    // add rows
    final List<List<T>> toAdd = new ArrayList<>(rows.size());
    for(final List<T> l : rows) {
      final ArrayList<T> add = new ArrayList<>(l);
      if(add.size() != cols) throw new IllegalArgumentException(
          add.size() + " != " + cols);
      toAdd.add(add);
    }
    // add heights
    final AnimationList al = animator != null ? animator.getAnimationList() : null;
    final List<AnimatedDouble> newHeights = new ArrayList<>(heights.size());
    for(final Double h : heights) {
      final AnimatedDouble d = new AnimatedDouble(initHeight);
      if(al != null) {
        al.addAnimated(d);
      }
      d.startAnimationTo(Objects.requireNonNull(h), timing);
      newHeights.add(d);
    }
    if(newHeights.size() != toAdd.size()) throw new IllegalArgumentException(
        newHeights.size() + " != " + toAdd.size());
    // add names
    final List<String> newNames = new ArrayList<>(names.size());
    for(final String n : names) {
      newNames.add(Objects.requireNonNull(n));
    }
    if(newNames.size() != toAdd.size()) throw new IllegalArgumentException(
        newNames.size() + " != " + toAdd.size());
    // add on end
    matrix.addAll(index, toAdd);
    this.heights.addAll(index, newHeights);
    rowNames.addAll(index, names);
    refreshAll();
  }

  /**
   * Adds columns.
   * 
   * @param index The index to insert the columns.
   * @param cols A list of all columns to add.
   * @param names The names of the new columns.
   * @param initWidth The initial width for the columns.
   * @param widths The actual widths of the columns.
   * @param timing The animation timing.
   */
  public synchronized void addColumns(final int index, final List<List<T>> cols,
      final List<String> names, final double initWidth, final List<Double> widths,
      final AnimationTiming timing) {
    ensureChangeAllowed();
    Objects.requireNonNull(timing);
    // add columns
    final int rows = rows();
    final int colsToAdd = cols.size();
    final List<List<T>> toAdd = new ArrayList<>(rows);
    for(int i = 0; i < rows; ++i) {
      toAdd.add(new ArrayList<T>(colsToAdd));
    }
    for(final List<T> l : cols) {
      int row = 0;
      for(final T el : l) {
        final List<T> list = toAdd.get(row);
        list.add(el);
        ++row;
      }
      if(row != rows) throw new IllegalArgumentException(row + " != " + rows);
    }
    for(final List<T> l : toAdd) {
      if(l.size() != colsToAdd) throw new IllegalArgumentException(
          l.size() + " != " + colsToAdd);
    }
    // add widths
    final AnimationList al = animator != null ? animator.getAnimationList() : null;
    final List<AnimatedDouble> newWidths = new ArrayList<>(widths.size());
    for(final Double w : widths) {
      final AnimatedDouble d = new AnimatedDouble(initWidth);
      if(al != null) {
        al.addAnimated(d);
      }
      d.startAnimationTo(Objects.requireNonNull(w), timing);
      newWidths.add(d);
    }
    if(newWidths.size() != colsToAdd) throw new IllegalArgumentException(
        newWidths.size() + " != " + colsToAdd);
    // add names
    final List<String> newNames = new ArrayList<>(names.size());
    for(final String n : names) {
      newNames.add(Objects.requireNonNull(n));
    }
    if(newNames.size() != colsToAdd) throw new IllegalArgumentException(
        newNames.size() + " != " + colsToAdd);
    // add on end
    int i = 0;
    for(final List<T> m : matrix) {
      m.addAll(index, toAdd.get(i));
      ++i;
    }
    this.cols += colsToAdd;
    this.widths.addAll(index, newWidths);
    colNames.addAll(index, names);
    refreshAll();
  }

  /**
   * Removes rows. Note that it is not allowed to remove all rows.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   */
  public synchronized void removeRows(final int from, final int to) {
    if(to - from >= rows()) throw new IllegalArgumentException("cannot remove all rows");
    ensureChangeAllowed();
    matrix.subList(from, to).clear();
    heights.subList(from, to).clear();
    rowNames.subList(from, to).clear();
    // refresh all
    refreshAll();
  }

  /**
   * Removes rows. Note that it is not allowed to remove all rows.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   * @param timing The animation timing.
   */
  public synchronized void removeRows(
      final int from, final int to, final AnimationTiming timing) {
    if(to - from >= rows()) throw new IllegalArgumentException("cannot remove all rows");
    ensureChangeAllowed();
    Objects.requireNonNull(timing);
    noChange = true;
    for(final AnimatedDouble d : heights.subList(from, to)) {
      d.startAnimationTo(0.0, timing);
    }
    // TODO #43 -- Java 8 simplification
    animator.getAnimationList().scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        noChange = false;
        removeRows(from, to);
      }

    }, timing);
  }

  /**
   * Removes columns. Note that it is not allowed to remove all columns.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   */
  public synchronized void removeColumns(final int from, final int to) {
    if(to - from >= cols()) throw new IllegalArgumentException(
        "cannot remove all columns");
    ensureChangeAllowed();
    for(final List<T> m : matrix) {
      m.subList(from, to).clear();
    }
    widths.subList(from, to).clear();
    colNames.subList(from, to).clear();
    cols -= to - from;
    // refresh all
    refreshAll();
  }

  /**
   * Removes columns. Note that it is not allowed to remove all columns.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   * @param timing The animation timing.
   */
  public synchronized void removeColumns(
      final int from, final int to, final AnimationTiming timing) {
    if(to - from >= cols()) throw new IllegalArgumentException(
        "cannot remove all columns");
    ensureChangeAllowed();
    Objects.requireNonNull(timing);
    noChange = true;
    for(final AnimatedDouble d : widths.subList(from, to)) {
      d.startAnimationTo(0.0, timing);
    }
    // TODO #43 -- Java 8 simplification
    animator.getAnimationList().scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        noChange = false;
        removeColumns(from, to);
      }

    }, timing);
  }

  /**
   * Replaces a range of rows with a new set of rows.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   * @param rows A list of rows to add.
   * @param names The names of the new rows.
   * @param heights The heights of the new rows.
   * @param timing The animation timing.
   */
  public synchronized void replaceRows(final int from, final int to,
      final List<List<T>> rows, final List<String> names, final List<Double> heights,
      final AnimationTiming timing) {
    final double h = getHeight(from, to);
    addRows(to, rows, names, h / rows.size(), heights, timing);
    removeRows(from, to);
  }

  /**
   * Replaces a range of columns with a new set of columns.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   * @param cols A list of columns to add.
   * @param names The names of the new columns.
   * @param widths The widths of the new columns.
   * @param timing The animation timing.
   */
  public synchronized void replaceColumns(final int from, final int to,
      final List<List<T>> cols, final List<String> names, final List<Double> widths,
      final AnimationTiming timing) {
    final double w = getWidth(from, to);
    addColumns(to, cols, names, w / cols.size(), widths, timing);
    removeColumns(from, to);
  }

  @Override
  public synchronized void swapRows(final int a, final int b) {
    ensureChangeAllowed();
    Collections.swap(matrix, a, b);
    Collections.swap(heights, a, b);
    Collections.swap(rowNames, a, b);
  }

  /**
   * Fills the given list with the content of the column.
   * 
   * @param column The list to store the column in. If <code>null</code> a new
   *          list is allocated.
   * @param col The column index.
   * @return The list holding the result.
   */
  protected synchronized ArrayList<T> getColumn(final ArrayList<T> column, final int col) {
    final int rows = rows();
    final ArrayList<T> l;
    if(column == null) {
      l = new ArrayList<>(rows);
    } else {
      column.ensureCapacity(rows);
      l = column;
      if(l.size() > rows) {
        l.subList(rows, l.size()).clear();
      }
    }
    assert l.size() <= rows;
    for(int pos = 0; pos < l.size(); ++pos) {
      l.set(pos, matrix.get(pos).get(col));
    }
    for(int pos = l.size(); pos < matrix.size(); ++pos) {
      l.add(matrix.get(pos).get(col));
    }
    return l;
  }

  /**
   * Sets the specified column to the values given in the list. The list
   * contains the previous values afterwards.
   * 
   * @param column The new elements. After this method this list contains the
   *          previous values.
   * @param dest The destination column index.
   * @return The list holding all previous values.
   */
  protected synchronized List<T> setColumn(final List<T> column, final int dest) {
    int r = 0;
    for(final List<T> row : matrix) {
      column.set(r, row.set(dest, column.get(r)));
      ++r;
    }
    return column;
  }

  @Override
  public synchronized void swapColumns(final int a, final int b) {
    ensureChangeAllowed();
    setColumn(setColumn(getColumn(null, a), b), a).clear();
    Collections.swap(widths, a, b);
    Collections.swap(colNames, a, b);
  }

  @Override
  public synchronized void sortRows(final Comparator<Integer> cmp) {
    ensureChangeAllowed();
    final int[] perm = ArrayUtil.createPermutation(cmp, rows());
    ArrayUtil.applyPermutation(matrix, perm);
    ArrayUtil.applyPermutation(heights, perm);
    ArrayUtil.applyPermutation(rowNames, perm);
  }

  @Override
  public synchronized void sortColumns(final Comparator<Integer> cmp) {
    ensureChangeAllowed();
    final ArrayList<T> column = new ArrayList<>();
    final int[] perm = ArrayUtil.createPermutation(cmp, cols());
    ArrayUtil.applyPermutation(new Swapable() {

      @Override
      public void swap(final int col1, final int col2) {
        setColumn(setColumn(getColumn(column, col1), col2), col1);
      }

    }, perm);
    column.clear();
    ArrayUtil.applyPermutation(widths, perm);
    ArrayUtil.applyPermutation(colNames, perm);
  }

  @Override
  public T get(final int row, final int col) {
    return matrix.get(row).get(col);
  }

  @Override
  public synchronized void set(final int row, final int col, final T value) {
    matrix.get(row).set(col, value);
    refreshAll();
  }

  @Override
  public int rows() {
    return matrix.size();
  }

  @Override
  public int cols() {
    return cols;
  }

  @Override
  public synchronized void setRowName(final int row, final String name) {
    rowNames.set(row, Objects.requireNonNull(name));
    refreshAll();
  }

  @Override
  public String getRowName(final int row) {
    return rowNames.get(row);
  }

  @Override
  public synchronized void setColumnName(final int col, final String name) {
    colNames.set(col, Objects.requireNonNull(name));
    refreshAll();
  }

  @Override
  public String getColumnName(final int col) {
    return colNames.get(col);
  }

  @Override
  public synchronized void setWidth(final int col, final double value) {
    widths.get(col).set(value);
    refreshAll();
  }

  /**
   * Setter.
   * 
   * @param col The column.
   * @param value The width.
   * @param timing The animation timing.
   */
  public synchronized void setWidth(
      final int col, final double value, final AnimationTiming timing) {
    widths.get(col).startAnimationTo(value, timing);
    refreshAll();
  }

  @Override
  public double getWidth(final int col) {
    return widths.get(col).get();
  }

  /**
   * Computes the total width of a range.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   * @return The total width of the range.
   */
  public double getWidth(final int from, final int to) {
    double totalWidth = 0;
    for(int i = from; i < to; ++i) {
      totalWidth += getWidth(i);
    }
    return totalWidth;
  }

  @Override
  public synchronized void setHeight(final int row, final double value) {
    heights.get(row).set(value);
    refreshAll();
  }

  /**
   * Setter.
   * 
   * @param row The row.
   * @param value The height.
   * @param timing The animation timing.
   */
  public synchronized void setHeight(
      final int row, final double value, final AnimationTiming timing) {
    heights.get(row).startAnimationTo(value, timing);
    refreshAll();
  }

  @Override
  public double getHeight(final int row) {
    return heights.get(row).get();
  }

  /**
   * Computes the total height of a range.
   * 
   * @param from The starting index inclusive.
   * @param to The end index exclusive.
   * @return The total height of the range.
   */
  public double getHeight(final int from, final int to) {
    double totalHeight = 0;
    for(int i = from; i < to; ++i) {
      totalHeight += getHeight(i);
    }
    return totalHeight;
  }

  /** The animator. */
  private Animator animator;

  @Override
  public void setRefreshManager(final RefreshManager manager) {
    if(!(manager instanceof Animator)) throw new IllegalArgumentException(
        "argument must be " + Animator.class + " got " + manager.getClass());
    setAnimator((Animator) manager);
  }

  /**
   * Setter.
   * 
   * @param animator The animator.
   */
  public void setAnimator(final Animator animator) {
    super.setRefreshManager(animator);
    this.animator = animator;
    if(this.animator != null) {
      final AnimationList al = this.animator.getAnimationList();
      for(final AnimatedDouble d : widths) {
        al.addAnimated(d);
      }
      for(final AnimatedDouble d : heights) {
        al.addAnimated(d);
      }
    }
  }

}
