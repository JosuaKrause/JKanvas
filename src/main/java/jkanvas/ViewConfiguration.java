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
  public void paint(final Graphics2D gfx, final double w, final double h) {
    final Graphics2D g = (Graphics2D) gfx.create();
    zui.transform(g);
    painter.draw(g, getContext(w, h));
    g.dispose();
    painter.drawHUD(gfx, getHUDContext(w, h));
  }

  /**
   * Returns the current canvas context. Note that it is not guaranteed that the
   * context returns correct values if the viewport changes after a call to this
   * method.
   * 
   * @return The current canvas context.
   */
  public KanvasContext getContext(final double width, final double height) {
    return new ViewContext(zui, true, 0, 0, width, height);
  }

  /**
   * Returns the current head-up display context. Note that it is not guaranteed
   * that the context returns correct values if the viewport changes after a
   * call to this method.
   * 
   * @return The current head-up display context.
   */
  public KanvasContext getHUDContext(final double width, final double height) {
    return new ViewContext(zui, false, 0, 0, width, height);
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
  public void setRestriction(final Canvas canvas,
      final RestrictedArea area, final AnimationTiming timing) {
    if(!isRestricted()) throw new IllegalStateException("not restricted");
    restriction = null;
    final Rectangle2D rest = area.getTopLevelBounds();
    if(rest != null) {
      zui.toView(rest, timing, new AnimationAction() {

        @Override
        public void animationFinished() {
          if(!zui.inAnimation()) {
            area.beforeEntering(canvas);
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

    private final double width;

    private final double height;

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
        final double offX, final double offY, final double width, final double height) {
      super(inCanvasSpace, offX, offY);
      this.width = width;
      this.height = height;
      this.zui = Objects.requireNonNull(zui);
    }

    @Override
    protected KanvasContext create(final boolean inCanvasSpace,
        final double offX, final double offY) {
      return new ViewContext(zui, inCanvasSpace, offX, offY, width, height);
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
      return new Rectangle2D.Double(0, 0, width, height);
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
