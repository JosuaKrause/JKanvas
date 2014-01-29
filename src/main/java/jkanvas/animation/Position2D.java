package jkanvas.animation;

import java.awt.geom.Point2D;

/**
 * A position in two dimensions.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Position2D {

  /** The current x position. */
  private double x;

  /** The current y position. */
  private double y;

  /**
   * Creates a position.
   * 
   * @param x The initial x position.
   * @param y The initial y position.
   */
  public Position2D(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Creates a position.
   * 
   * @param pos The initial position.
   */
  public Position2D(final Point2D pos) {
    this(pos.getX(), pos.getY());
  }

  /**
   * Getter.
   * 
   * @return The current x position.
   */
  public double getX() {
    return x;
  }

  /**
   * Getter.
   * 
   * @return The current y position.
   */
  public double getY() {
    return y;
  }

  /**
   * Getter.
   * 
   * @return The current position.
   */
  public Point2D getPos() {
    return new Point2D.Double(getX(), getY());
  }

  /**
   * Setter.
   * 
   * @param x The new x position.
   * @param y The new y position.
   */
  public void setPosition(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Setter.
   * 
   * @param pos The new position.
   */
  public void setPosition(final Point2D pos) {
    setPosition(pos.getX(), pos.getY());
  }

}
