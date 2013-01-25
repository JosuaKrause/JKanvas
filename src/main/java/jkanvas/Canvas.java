package jkanvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * A simple class adding panning and zooming functionality to a
 * {@link JComponent}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class Canvas extends JComponent implements Refreshable {

  /** The underlying zoomable user interface. */
  protected final ZoomableUI zui;

  /** The painter. */
  protected KanvasPainter painter;

  /** The focused component. */
  private JComponent focus;

  /**
   * Creates a canvas for the given painter.
   * 
   * @param p The painter.
   * @param width The initial width of the component.
   * @param height The initial height of the component.
   */
  public Canvas(final KanvasPainter p, final int width, final int height) {
    setPreferredSize(new Dimension(width, height));
    painter = Objects.requireNonNull(p);
    zui = new ZoomableUI(this, null);
    final MouseAdapter mouse = new MouseInteraction() {

      @Override
      public void mousePressed(final MouseEvent e) {
        getFocusComponent().grabFocus();
        final Point2D p = e.getPoint();
        if(painter.clickHUD(p)) {
          refresh();
          return;
        }
        final Point2D c = zui.getForScreen(p);
        if(painter.click(c, e)) {
          refresh();
          return;
        }
        if(painter.acceptDrag(c, e)) {
          startDragging(c);
          refresh();
          return;
        }
        if(isMoveable() && painter.isAllowingPan(c, e)) {
          startDragging(e, zui.getOffsetX(), zui.getOffsetY());
        }
      }

      @Override
      public void mouseDragged(final MouseEvent e) {
        if(isDragging()) {
          if(!isPointDrag()) {
            move(e.getX(), e.getY());
          } else {
            final Point2D cur = zui.getForScreen(e.getPoint());
            final Point2D start = getPoint();
            painter.drag(start, cur, cur.getX() - start.getX(), cur.getY() - start.getY());
            refresh();
          }
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if(isDragging()) {
          if(!isPointDrag()) {
            move(e.getX(), e.getY());
            stopDragging();
          } else {
            final Point2D cur = zui.getForScreen(e.getPoint());
            final Point2D start = stopPointDrag();
            painter.endDrag(start, cur, cur.getX() - start.getX(),
                cur.getY() - start.getY());
            refresh();
          }
        }
      }

      /**
       * Sets the offset according to the mouse position.
       * 
       * @param x The mouse x position.
       * @param y The mouse y position.
       */
      protected void move(final int x, final int y) {
        zui.setOffset(getMoveX(x), getMoveY(y));
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if(!isDragging() && isMoveable()) {
          zui.zoomTo(e.getX(), e.getY(), e.getWheelRotation());
        }
      }

      @Override
      public void mouseMoved(final MouseEvent e) {
        if(painter.moveMouse(zui.getForScreen(e.getPoint()))) {
          refresh();
        }
      }

    };
    addMouseListener(mouse);
    addMouseMotionListener(mouse);
    addMouseWheelListener(mouse);
    setToolTipText("");
    setFocusable(true);
    grabFocus();
    focus = this;
  }

  /**
   * Setter.
   * 
   * @param focus The component to focus when clicked.
   */
  public void setFocusComponent(final JComponent focus) {
    this.focus = Objects.requireNonNull(focus);
  }

  /**
   * Getter.
   * 
   * @return The component to focus when clicked.
   */
  public JComponent getFocusComponent() {
    return focus;
  }

  /**
   * Computes the position of a {@link MouseEvent} on the canvas.
   * 
   * @param e The mouse event.
   * @return The position of the event on the canvas.
   */
  public Point2D getPositionOnCanvas(final MouseEvent e) {
    return zui.getForScreen(e.getPoint());
  }

  @Override
  public String getToolTipText(final MouseEvent e) {
    final Point2D p = e.getPoint();
    String strHUD;
    if((strHUD = painter.getTooltipHUD(p)) != null) return strHUD;
    final Point2D c = zui.getForScreen(p);
    String str;
    return ((str = painter.getTooltip(c)) != null) ? str : null;
  }

  /**
   * Adds a keyboard action event.
   * 
   * @param key The key id, given by {@link KeyEvent}. (Constants beginning with
   *          <code>VK</code>)
   * @param a The action that is performed.
   */
  public void addAction(final int key, final Action a) {
    final Object token = new Object();
    final InputMap input = getInputMap();
    input.put(KeyStroke.getKeyStroke(key, 0), token);
    final ActionMap action = getActionMap();
    action.put(token, a);
  }

  /**
   * The back ground color of the component or <code>null</code> if it is
   * transparent.
   */
  private Color back;

  @Override
  public void setBackground(final Color bg) {
    back = bg;
    super.setBackground(bg);
  }

  /**
   * A context for this canvas.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private final class CanvasContext implements KanvasContext {

    /** Whether this context is in canvas space. */
    private final boolean inCanvasSpace;
    /** The x offset of the context. */
    private final double offX;
    /** The y offset of the context. */
    private final double offY;

    /**
     * Creates a context for this canvas.
     * 
     * @param inCanvasSpace Whether the normal
     *          {@link KanvasPainter#draw(Graphics2D, KanvasContext)} is called.
     * @param offX The x offset in canvas coordinates.
     * @param offY The y offset in canvas coordinates.
     */
    public CanvasContext(final boolean inCanvasSpace, final double offX, final double offY) {
      this.inCanvasSpace = inCanvasSpace;
      this.offX = offX;
      this.offY = offY;
    }

    @Override
    public Point2D toCanvasCoordinates(final Point2D p) {
      final Point2D pos = zui.getForScreen(p);
      return new Point2D.Double(pos.getX() + offX, pos.getY() + offY);
    }

    @Override
    public double toCanvasLength(final double length) {
      return zui.inReal(length);
    }

    @Override
    public Point2D toComponentCoordinates(final Point2D p) {
      return new Point2D.Double(zui.getXFromCanvas(p.getX() + offX),
          zui.getYFromCanvas(p.getY() + offY));
    }

    @Override
    public double toComponentLength(final double length) {
      return zui.fromReal(length);
    }

    @Override
    public boolean inCanvasCoordinates() {
      return inCanvasSpace;
    }

    /** The cache for the visible rectangle in component coordinates. */
    private Rectangle2D visComp;

    @Override
    public Rectangle2D getVisibleComponent() {
      if(visComp == null) {
        visComp = getVisibleRect();
      }
      return visComp;
    }

    /** The cache for the visible rectangle in canvas coordinates. */
    private Rectangle2D visCanvas;

    @Override
    public Rectangle2D getVisibleCanvas() {
      if(visCanvas == null) {
        visCanvas = zui.toCanvas(getVisibleComponent());
        visCanvas.setRect(offX + visCanvas.getX(), offY + visCanvas.getY(),
            visCanvas.getWidth(), visCanvas.getHeight());
      }
      return visCanvas;
    }

    @Override
    public KanvasContext translate(final double dx, final double dy) {
      if(dx == 0 && dy == 0) return this;
      return new CanvasContext(inCanvasSpace, offX - dx, offY - dy);
    }

    @Override
    public AffineTransform toCanvasTransformation() {
      final AffineTransform at = new AffineTransform();
      zui.transform(at);
      at.translate(offX, offY);
      return at;
    }

  } // CanvasContext

  @Override
  protected void paintComponent(final Graphics g) {
    final Graphics2D g2 = (Graphics2D) g.create();
    final Rectangle2D rect = getVisibleRect();
    g2.clip(rect);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    final Color c = back;
    if(c != null) {
      g2.setColor(c);
      g2.fill(rect);
    }
    if(paintLock == null) {
      doPaint(g2);
    } else {
      synchronized(paintLock) {
        doPaint(g2);
      }
    }
    g2.dispose();
  }

  /**
   * Returns the current canvas context. Note that it is not guaranteed that the
   * context returns correct values if the viewport changes after a call to this
   * method.
   * 
   * @return The current canvas context.
   */
  public CanvasContext getContext() {
    return new CanvasContext(true, 0, 0);
  }

  /**
   * Returns the current head-up display context. Note that it is not guaranteed
   * that the context returns correct values if the viewport changes after a
   * call to this method.
   * 
   * @return The current head-up display context.
   */
  public CanvasContext getHUDContext() {
    return new CanvasContext(false, 0, 0);
  }

  /**
   * Does the actual painting.
   * 
   * @param g The graphics context.
   */
  private void doPaint(final Graphics2D g) {
    final Graphics2D gfx = (Graphics2D) g.create();
    zui.transform(gfx);
    painter.draw(gfx, getContext());
    gfx.dispose();
    painter.drawHUD(g, getHUDContext());
  }

  /** The paint lock. */
  private Object paintLock;

  /**
   * Setter.
   * 
   * @param paintLock The paint lock or <code>null</code> if nothing should be
   *          locked during painting.
   */
  public void setPaintLock(final Object paintLock) {
    this.paintLock = paintLock;
  }

  /**
   * Getter.
   * 
   * @return The paint lock or <code>null</code> if nothing is locked during
   *         painting.
   */
  public Object getPaintLock() {
    return paintLock;
  }


  /**
   * Sets the painter.
   * 
   * @param p The new painter.
   */
  public void setPainter(final KanvasPainter p) {
    painter = Objects.requireNonNull(p);
  }

  /**
   * Resets the viewport to a scaling of <code>1.0</code> and
   * <code>(0, 0)</code> being in the center of the component when
   * {@link KanvasPainter#getBoundingBox()} returns <code>null</code> and zooms to fit
   * the bounding box if {@link KanvasPainter#getBoundingBox()} returns a proper
   * bounding box.
   */
  public void reset() {
    final Rectangle2D bbox = painter.getBoundingBox();
    if(bbox == null) {
      zui.resetView(getVisibleRect());
    } else {
      reset(bbox);
    }
  }

  /** The margin for the viewport reset. The default is <code>10.0</code>. */
  private double margin = 10.0;

  /**
   * Getter.
   * 
   * @return The margin for viewport resets.
   */
  public double getMargin() {
    return margin;
  }

  /**
   * Setter.
   * 
   * @param margin Sets the margin for viewport resets.
   */
  public void setMargin(final double margin) {
    this.margin = margin;
  }

  /**
   * Resets the viewport to show exactly the given rectangle expanded by the
   * margin given by {@link #getMargin()}.
   * 
   * @param bbox The rectangle that is visible.
   */
  public void reset(final Rectangle2D bbox) {
    if(bbox == null) {
      reset();
    } else {
      final double margin = getMargin();
      final Rectangle2D rect = getVisibleRect();
      zui.showRectangle(bbox, rect, margin, false);
    }
  }

  /** Whether the canvas is moveable, ie it can be panned and zoomed. */
  private boolean isMoveable = true;

  /**
   * Sets whether the canvas is moveable, ie whether it can be panned or zoomed.
   * 
   * @param isMoveable If it is moveable.
   */
  public void setMoveable(final boolean isMoveable) {
    this.isMoveable = isMoveable;
  }

  /**
   * Getter.
   * 
   * @return Is <code>true</code>, when the canvas can be panned and zoomed.
   */
  public boolean isMoveable() {
    return isMoveable;
  }

  /**
   * Getter.
   * 
   * @return The currently visible portion of the canvas.
   */
  public Rectangle2D getVisibleCanvas() {
    return zui.toCanvas(getVisibleRect());
  }

  /** Disposes the painter. */
  public void dispose() {
    painter.dispose();
  }

  @Override
  public void refresh() {
    repaint();
  }

}
