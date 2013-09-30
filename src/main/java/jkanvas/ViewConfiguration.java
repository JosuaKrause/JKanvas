package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;

/**
 * A view configuration provides a view on a {@link KanvasPainter}. The view is
 * tied to the painter.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class ViewConfiguration {

  /** The painter. */
  private final KanvasPainter painter;
  /** The ZUI camera. */
  protected final CameraZUI zui;
  /** The restriction rectangle. */
  protected Rectangle2D restriction;

  /**
   * Creates a view configuration for the given painter.
   * 
   * @param canvas The canvas.
   * @param painter The painter.
   * @param restricted Whether the configuration is restricted.
   */
  public ViewConfiguration(final Canvas canvas,
      final KanvasPainter painter, final boolean restricted) {
    Objects.requireNonNull(canvas);
    this.painter = Objects.requireNonNull(painter);
    zui = new CameraZUI(canvas, restricted ? new RestrictedCanvas() {

      @Override
      public Rectangle2D getComponentView() {
        return canvas.getVisibleRect();
      }

      @Override
      public Rectangle2D getBoundingRect() {
        return restriction;
      }

    } : null);
  }

  /**
   * Getter.
   * 
   * @return The camera.
   */
  public Camera getCamera() {
    return zui;
  }

  /**
   * Getter.
   * 
   * @return The ZUI camera.
   */
  CameraZUI getZUI() {
    return zui;
  }

  /**
   * Getter.
   * 
   * @return The painter.
   */
  public KanvasPainter getPainter() {
    return painter;
  }

  /**
   * Paints this configuration.
   * 
   * @param gfx The graphics context.
   */
  public void paint(final Graphics2D gfx) {
    final Graphics2D g = (Graphics2D) gfx.create();
    zui.transform(g);
    painter.draw(g, getContext());
    g.dispose();
    painter.drawHUD(gfx, getHUDContext());
  }

  /**
   * Returns the current canvas context. Note that it is not guaranteed that the
   * context returns correct values if the viewport changes after a call to this
   * method.
   * 
   * @return The current canvas context.
   */
  public ViewContext getContext() {
    return new ViewContext(zui, true, 0, 0);
  }

  /**
   * Returns the current head-up display context. Note that it is not guaranteed
   * that the context returns correct values if the viewport changes after a
   * call to this method.
   * 
   * @return The current head-up display context.
   */
  public ViewContext getHUDContext() {
    return new ViewContext(zui, false, 0, 0);
  }

  /**
   * Setter.
   * 
   * @param restriction Sets the restriction rectangle.
   * @param timing How the transition to the restriction rectangle should be
   *          performed.
   * @param margin The margin added to the rectangle.
   * @throws IllegalStateException When the canvas is not restricted. The canvas
   *           can be restricted only with the constructor.
   * @see #isRestricted()
   */
  public void setRestriction(final Rectangle2D restriction,
      final AnimationTiming timing, final double margin) {
    if(!isRestricted()) throw new IllegalStateException("not restricted");
    this.restriction = null;
    if(restriction != null) {
      final Rectangle2D rest = jkanvas.util.PaintUtil.addPadding(restriction, margin);
      zui.toView(rest, timing, new AnimationAction() {

        @Override
        public void animationFinished() {
          if(!zui.inAnimation()) {
            setRestrictionDirectly(rest);
          }
        }

      }, false);
    }
  }

  /**
   * Getter.
   * 
   * @return Whether the canvas is restricted.
   */
  public boolean isRestricted() {
    return zui.isRestricted();
  }

  /**
   * Setter.
   * 
   * @param restriction Directly sets the restriction. This method should only
   *          be used internally.
   */
  void setRestrictionDirectly(final Rectangle2D restriction) {
    this.restriction = restriction;
  }

  /**
   * A context for this configuration. The ZUI camera should not change during
   * its usage.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static final class ViewContext extends AbstractKanvasContext {

    /** The ZUI camera. */
    private final CameraZUI zui;

    /**
     * Creates a context for this configuration.
     * 
     * @param zui The ZUI camera.
     * @param inCanvasSpace Whether the normal
     *          {@link KanvasPainter#draw(Graphics2D, KanvasContext)} is called.
     * @param offX The x offset in canvas coordinates.
     * @param offY The y offset in canvas coordinates.
     */
    public ViewContext(final CameraZUI zui, final boolean inCanvasSpace,
        final double offX, final double offY) {
      super(inCanvasSpace, offX, offY);
      this.zui = Objects.requireNonNull(zui);
    }

    @Override
    protected KanvasContext create(final boolean inCanvasSpace,
        final double offX, final double offY) {
      return new ViewContext(zui, inCanvasSpace, offX, offY);
    }

    @Override
    public Rectangle2D toCanvasCoordinates(final RectangularShape r) {
      return zui.toCanvas(r);
    }

    @Override
    public Point2D toCanvasCoordinates(final Point2D p) {
      final Point2D pos = zui.getForScreen(p);
      return new Point2D.Double(pos.getX() + getOffsetX(), pos.getY() + getOffsetY());
    }

    @Override
    public double toCanvasLength(final double length) {
      return zui.inReal(length);
    }

    @Override
    public Point2D toComponentCoordinates(final Point2D p) {
      return new Point2D.Double(zui.getXFromCanvas(p.getX() + getOffsetX()),
          zui.getYFromCanvas(p.getY() + getOffsetY()));
    }

    @Override
    public double toComponentLength(final double length) {
      return zui.fromReal(length);
    }

    @Override
    protected Rectangle2D createVisibleComponent() {
      return zui.getVisibleRect();
    }

    @Override
    protected void transform(final AffineTransform at) {
      zui.transform(at);
    }

    @Override
    protected void transformBack(final AffineTransform at) {
      zui.transformBack(at);
    }

  } // ViewContext

}
