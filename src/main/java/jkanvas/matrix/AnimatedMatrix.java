package jkanvas.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.RefreshManager;
import jkanvas.animation.AnimatedDouble;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationList;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;

public class AnimatedMatrix<T>
    extends AbstractMatrix<T> implements MutableMatrix<T> {

  private final List<List<T>> matrix;

  private final List<AnimatedDouble> widths;

  private final List<AnimatedDouble> heights;

  private final List<String> rowNames;

  private final List<String> colNames;

  private int cols;

  protected boolean noChange;

  public AnimatedMatrix(final T init, final double w, final double h, final String row,
      final String col) {
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

  private void ensureChangeAllowed() {
    if(noChange) throw new IllegalStateException("no change allowed");
  }

  public synchronized void addRows(final int index, final List<List<T>> rows,
      final List<String> names, final double initHeight, final List<Double> heights,
      final AnimationTiming timing) {
    ensureChangeAllowed();
    // TODO make error robust
    Objects.requireNonNull(timing);
    // add rows
    final List<List<T>> toAdd = new ArrayList<>(rows.size());
    for(final List<T> l : rows) {
      final ArrayList<T> add = new ArrayList<>(l);
      if(add.size() != cols) throw new IllegalArgumentException(
          add.size() + " != " + cols);
      toAdd.add(add);
    }
    matrix.addAll(index, toAdd);
    // add heights
    final AnimationList al = animator != null ? animator.getAnimationList() : null;
    final List<AnimatedDouble> newHeights = new ArrayList<>(heights.size());
    for(final Double h : heights) {
      final AnimatedDouble d = new AnimatedDouble(initHeight);
      if(al != null) {
        al.addAnimated(d);
      }
      d.startAnimationTo(Objects.requireNonNull(h), timing);
    }
    if(newHeights.size() != toAdd.size()) throw new IllegalArgumentException(
        newHeights.size() + " != " + toAdd.size());
    this.heights.addAll(index, newHeights);
    // add names
    final List<String> newNames = new ArrayList<>(names.size());
    for(final String n : names) {
      newNames.add(Objects.requireNonNull(n));
    }
    if(newNames.size() != toAdd.size()) throw new IllegalArgumentException(
        newNames.size() + " != " + toAdd.size());
    rowNames.addAll(index, names);
    // refresh all
    refreshAll();
  }

  public synchronized void addColumns(final int index, final List<List<T>> cols,
      final List<String> names, final double initWidth, final List<Double> widths,
      final AnimationTiming timing) {
    ensureChangeAllowed();
    // TODO make error robust
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
    int i = 0;
    for(final List<T> m : matrix) {
      final List<T> l = toAdd.get(i);
      if(l.size() != colsToAdd) throw new IllegalArgumentException(
          l.size() + " != " + colsToAdd);
      m.addAll(index, l);
      ++i;
    }
    this.cols += colsToAdd;
    // add widths
    final AnimationList al = animator != null ? animator.getAnimationList() : null;
    final List<AnimatedDouble> newWidths = new ArrayList<>(widths.size());
    for(final Double w : widths) {
      final AnimatedDouble d = new AnimatedDouble(initWidth);
      if(al != null) {
        al.addAnimated(d);
      }
      d.startAnimationTo(Objects.requireNonNull(w), timing);
    }
    if(newWidths.size() != colsToAdd) throw new IllegalArgumentException(
        newWidths.size() + " != " + colsToAdd);
    this.widths.addAll(index, newWidths);
    // add names
    final List<String> newNames = new ArrayList<>(names.size());
    for(final String n : names) {
      newNames.add(Objects.requireNonNull(n));
    }
    if(newNames.size() != colsToAdd) throw new IllegalArgumentException(
        newNames.size() + " != " + colsToAdd);
    colNames.addAll(index, names);
    // refresh all
    refreshAll();
  }

  public synchronized void removeRows(final int from, final int to) {
    if(to - from >= rows()) throw new IllegalArgumentException("cannot remove all rows");
    ensureChangeAllowed();
    matrix.subList(from, to).clear();
    heights.subList(from, to).clear();
    rowNames.subList(from, to).clear();
    // refresh all
    refreshAll();
  }

  public void removeRows(final int from, final int to, final AnimationTiming timing) {
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

  public void removeColumns(final int from, final int to, final AnimationTiming timing) {
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

  public void replaceRows(final int from, final int to, final List<List<T>> rows,
      final List<String> names, final List<Double> heights, final AnimationTiming timing) {
    final double h = getHeight(from, to);
    addRows(to, rows, names, h / rows.size(), heights, timing);
    removeRows(from, to);
  }

  public void replaceColumns(final int from, final int to, final List<List<T>> cols,
      final List<String> names, final List<Double> widths, final AnimationTiming timing) {
    final double w = getWidth(from, to);
    addColumns(to, cols, names, w / cols.size(), widths, timing);
    removeColumns(from, to);
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

  public synchronized void setWidth(
      final int col, final double value, final AnimationTiming timing) {
    widths.get(col).startAnimationTo(value, timing);
    refreshAll();
  }

  @Override
  public double getWidth(final int col) {
    return widths.get(col).get();
  }

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

  public synchronized void setHeight(
      final int row, final double value, final AnimationTiming timing) {
    heights.get(row).startAnimationTo(value, timing);
    refreshAll();
  }

  @Override
  public double getHeight(final int row) {
    return heights.get(row).get();
  }

  public double getHeight(final int from, final int to) {
    double totalHeight = 0;
    for(int i = from; i < to; ++i) {
      totalHeight += getHeight(i);
    }
    return totalHeight;
  }

  private Animator animator;

  @Override
  public void setRefreshManager(final RefreshManager manager) {
    if(!(manager instanceof Animator)) throw new IllegalArgumentException(
        "argument must be " + Animator.class + " got " + manager.getClass());
    setAnimator((Animator) manager);
  }

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
