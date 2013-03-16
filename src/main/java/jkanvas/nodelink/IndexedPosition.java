package jkanvas.nodelink;

import java.awt.geom.Point2D;

import jkanvas.animation.AnimatedPosition;

/**
 * A position with an additional index.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class IndexedPosition extends AnimatedPosition {

  /** The index of the corresponding node. */
  protected final int index;

  /**
   * Creates an indexed position.
   * 
   * @param x The x position.
   * @param y The y position.
   * @param index The index.
   */
  public IndexedPosition(final double x, final double y, final int index) {
    super(x, y);
    this.index = index;
  }

  /**
   * Creates an indexed position.
   * 
   * @param pos The position.
   * @param index The index.
   */
  public IndexedPosition(final Point2D pos, final int index) {
    this(pos.getX(), pos.getY(), index);
  }

  /**
   * Getter.
   * 
   * @return The index of the node.
   */
  public int getIndex() {
    return index;
  }

}
