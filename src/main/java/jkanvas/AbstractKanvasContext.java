package jkanvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * Defines methods of a {@link KanvasContext} that can easily be derived from
 * other methods or can be done without implementation dependencies.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class AbstractKanvasContext implements KanvasContext {

  /** Whether this context is in canvas space. */
  private final boolean inCanvasSpace;
  /** The x offset of the context. */
  private final double offX;
  /** The y offset of the context. */
  private final double offY;

  /**
   * Creates an abstract context.
   * 
   * @param inCanvasSpace Whether the context is in canvas coordinates.
   * @param offX The x offset in canvas coordinates.
   * @param offY The y offset in canvas coordinates.
   */
  public AbstractKanvasContext(final boolean inCanvasSpace,
      final double offX, final double offY) {
    this.inCanvasSpace = inCanvasSpace;
    this.offX = offX;
    this.offY = offY;
  }

  /**
   * Getter.
   * 
   * @return The offset in x direction.
   */
  protected double getOffsetX() {
    return offX;
  }

  /**
   * Getter.
   * 
   * @return The offset in y direction.
   */
  protected double getOffsetY() {
    return offY;
  }

  /**
   * Creates a copy of this context with the given state.
   * 
   * @param inCanvasSpace Whether the context is in canvas coordinates.
   * @param offX The offset in x direction.
   * @param offY The offset in y direction.
   * @return The copied context.
   */
  protected abstract KanvasContext create(boolean inCanvasSpace, double offX, double offY);

  /**
   * Getter.
   * 
   * @return Computes the visible component.
   */
  protected abstract Rectangle2D createVisibleComponent();

  /** The cache for the visible rectangle in component coordinates. */
  private Rectangle2D visComp;

  @Override
  public Rectangle2D getVisibleComponent() {
    if(visComp == null) {
      visComp = createVisibleComponent();
    }
    return visComp;
  }

  /** The cache for the visible rectangle in canvas coordinates. */
  private Rectangle2D visCanvas;

  @Override
  public Rectangle2D getVisibleCanvas() {
    if(visCanvas == null) {
      visCanvas = toCanvasCoordinates(getVisibleComponent());
      visCanvas.setRect(getOffsetX() + visCanvas.getX(), getOffsetY() + visCanvas.getY(),
          visCanvas.getWidth(), visCanvas.getHeight());
    }
    return visCanvas;
  }

  @Override
  public boolean inCanvasCoordinates() {
    return inCanvasSpace;
  }

  @Override
  public KanvasContext translate(final double dx, final double dy) {
    if(dx == 0 && dy == 0) return this;
    if(inCanvasSpace) return create(true, offX - dx, offY - dy);
    return create(false, offX + dx, offY + dy);
  }

  /**
   * Transforms the given affine transformation from canvas to component
   * coordinates.
   * 
   * @param at The affine transformation.
   */
  protected abstract void transform(AffineTransform at);

  /**
   * Transforms the given affine transformation from component to canvas
   * coordinates.
   * 
   * @param at The affine transformation.
   */
  protected abstract void transformBack(AffineTransform at);

  @Override
  public AffineTransform toComponentTransformation() {
    final AffineTransform at = new AffineTransform();
    transform(at);
    at.translate(getOffsetX(), getOffsetY());
    return at;
  }

  @Override
  public AffineTransform toCanvasTransformation() {
    final AffineTransform at = new AffineTransform();
    at.translate(-getOffsetX(), -getOffsetY());
    transformBack(at);
    return at;
  }

  @Override
  public Point2D toComponentCoordinates(final Point2D p) {
    return toComponentTransformation().transform(p, null);
  }

  @Override
  public double toComponentLength(final double length) {
    return toComponentTransformation().getScaleX() * length;
  }

  @Override
  public Point2D toCanvasCoordinates(final Point2D p) {
    return toCanvasTransformation().transform(p, null);
  }

  @Override
  public double toCanvasLength(final double length) {
    return toCanvasTransformation().getScaleX() * length;
  }

  @Override
  public Rectangle2D toCanvasCoordinates(final RectangularShape r) {
    final Point2D topLeft = toCanvasCoordinates(new Point2D.Double(r.getX(), r.getY()));
    return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(),
        toCanvasLength(r.getWidth()), toCanvasLength(r.getHeight()));
  }

  @Override
  public Rectangle2D toComponentCoordinates(final RectangularShape r) {
    final Point2D topLeft = toComponentCoordinates(
        new Point2D.Double(r.getX(), r.getY()));
    return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(),
        toComponentLength(r.getWidth()), toComponentLength(r.getHeight()));
  }

}
