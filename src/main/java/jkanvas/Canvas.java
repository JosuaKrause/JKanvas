package jkanvas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationBarrier;
import jkanvas.animation.AnimationBarrier.CloseBlock;
import jkanvas.animation.AnimationList;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.util.Stopwatch;

/**
 * A simple class adding panning and zooming functionality to a
 * {@link JComponent}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class Canvas extends JComponent implements Refreshable {

  /**
   * A debug flag to activate bounding box rendering. This flag is optional for
   * a {@link KanvasPainter} or a {@link jkanvas.painter.Renderpass} to
   * interpret.
   */
  public static boolean DEBUG_BBOX;

  /**
   * A debug flag to show when a render pass is cached. This flag is optional to
   * interpret.
   */
  public static boolean DEBUG_CACHE;

  /** Disables render pass caching. */
  public static boolean DISABLE_CACHING;

  /** The current view configuration for the canvas. */
  protected ViewConfiguration cfg;

  /** The focused component. */
  private JComponent focus;

  /**
   * Creates an unrestricted canvas for the given painter.
   * 
   * @param p The painter.
   * @param width The initial width of the component.
   * @param height The initial height of the component.
   */
  public Canvas(final KanvasPainter p, final int width, final int height) {
    this(p, false, width, height);
  }

  /**
   * Creates a canvas for the given painter. The component is opaque by default.
   * 
   * @param p The painter.
   * @param restricted Whether the canvas should be restricted.
   * @param width The initial width of the component.
   * @param height The initial height of the component.
   */
  public Canvas(final KanvasPainter p, final boolean restricted,
      final int width, final int height) {
    setPreferredSize(new Dimension(width, height));
    cfg = new ViewConfiguration(this, p, restricted);
    final MouseAdapter mouse = new MouseInteraction() {

      /** Whether the drag is on the HUD. */
      private boolean hudDrag;

      @Override
      public void mousePressed(final MouseEvent e) {
        getFocusComponent().grabFocus();
        final Camera cam = getCamera();
        final KanvasPainter painter = cfg.getPainter();
        final Point2D p = e.getPoint();
        try {
          if(painter.clickHUD(cam, p, e)) {
            refresh();
            return;
          }
        } catch(final IgnoreInteractionException i) {
          // nothing to do
        }
        try {
          if(painter.acceptDragHUD(p, e)) {
            hudDrag = true;
            startDragging(p);
            refresh();
            return;
          }
        } catch(final IgnoreInteractionException i) {
          // nothing to do
        }
        final CameraZUI zui = cfg.getZUI();
        final Point2D c = zui.getForScreen(p);
        try {
          if(painter.click(cam, c, e)) {
            refresh();
            return;
          }
        } catch(final IgnoreInteractionException i) {
          // nothing to do
        }
        try {
          if(painter.acceptDrag(c, e)) {
            hudDrag = false;
            startDragging(c);
            refresh();
            return;
          }
        } catch(final IgnoreInteractionException i) {
          // nothing to do
        }
        if(isMoveable() && painter.isAllowingPan(c, e)) {
          startDragging(e, zui.getOffsetX(), zui.getOffsetY());
        }
      }

      @Override
      public void mouseClicked(final MouseEvent e) {
        if(e.getClickCount() < 2) return;
        final Camera cam = getCamera();
        getFocusComponent().grabFocus();
        final KanvasPainter painter = cfg.getPainter();
        final Point2D p = e.getPoint();
        try {
          if(painter.doubleClickHUD(cam, p, e)) {
            refresh();
            return;
          }
        } catch(final IgnoreInteractionException i) {
          // nothing to do
        }
        final CameraZUI zui = cfg.getZUI();
        final Point2D c = zui.getForScreen(p);
        try {
          if(painter.doubleClick(cam, c, e)) {
            refresh();
            return;
          }
        } catch(final IgnoreInteractionException i) {
          // nothing to do
        }
      }

      @Override
      public void mouseDragged(final MouseEvent e) {
        if(!isDragging()) return;
        if(!isPointDrag()) {
          move(e.getX(), e.getY());
          return;
        }
        final KanvasPainter painter = cfg.getPainter();
        final Point2D start = getStartPoint();
        final Point2D p = e.getPoint();
        if(hudDrag) {
          painter.dragHUD(start, p, p.getX() - start.getX(), p.getY() - start.getY());
        } else {
          final CameraZUI zui = cfg.getZUI();
          final Point2D cur = zui.getForScreen(p);
          painter.drag(start, cur, cur.getX() - start.getX(), cur.getY() - start.getY());
        }
        refresh();
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if(!isDragging()) return;
        if(!isPointDrag()) {
          move(e.getX(), e.getY());
          stopDragging();
          return;
        }
        final KanvasPainter painter = cfg.getPainter();
        final Point2D p = e.getPoint();
        final Point2D start = stopPointDrag();
        if(hudDrag) {
          painter.endDragHUD(start, p, p.getX() - start.getX(), p.getY() - start.getY());
        } else {
          final CameraZUI zui = cfg.getZUI();
          final Point2D c = zui.getForScreen(p);
          painter.endDrag(start, c, c.getX() - start.getX(), c.getY() - start.getY());
        }
        refresh();
      }

      /**
       * Sets the offset according to the mouse position.
       * 
       * @param x The mouse x position.
       * @param y The mouse y position.
       */
      protected void move(final int x, final int y) {
        cfg.getZUI().setOffset(getMoveX(x), getMoveY(y));
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if(isDragging() || !isMoveable()) return;
        cfg.getZUI().zoomTicks(e.getX(), e.getY(), e.getWheelRotation());
      }

      @Override
      public void mouseMoved(final MouseEvent e) {
        final KanvasPainter painter = cfg.getPainter();
        final CameraZUI zui = cfg.getZUI();
        try {
          if(!painter.moveMouse(zui.getForScreen(e.getPoint()))) return;
        } catch(final IgnoreInteractionException i) {
          // better refresh also in this case --
          // some method may have returned true
        }
        refresh();
      }

    };
    addMouseListener(mouse);
    addMouseMotionListener(mouse);
    addMouseWheelListener(mouse);
    setToolTipText("");
    setFocusable(true);
    grabFocus();
    setOpaque(true);
    focus = this;
  }

  /**
   * Prevents peers from processing the given interaction. When a peer has
   * already processed an interaction this method has no influence. This means
   * that when an earlier peer consumes the interaction this method will not be
   * called. The difference to consuming an interaction is that other
   * interaction types are still called afterwards. So when a
   * {@link KanvasInteraction#click(Camera,Point2D, MouseEvent)} interaction
   * calls this method for example
   * {@link KanvasInteraction#acceptDrag(Point2D, MouseEvent)} will still be
   * called.
   * 
   * @see HUDInteraction#clickHUD(Camera,Point2D, MouseEvent)
   * @see HUDInteraction#acceptDragHUD(Point2D, MouseEvent)
   * @see KanvasInteraction#click(Camera,Point2D, MouseEvent)
   * @see KanvasInteraction#acceptDrag(Point2D, MouseEvent)
   * @see HUDInteraction#doubleClickHUD(Camera,Point2D, MouseEvent)
   * @see KanvasInteraction#doubleClick(Camera,Point2D, MouseEvent)
   * @see KanvasInteraction#moveMouse(Point2D)
   */
  public static final void preventPeerInteraction() {
    throw IgnoreInteractionException.INSTANCE;
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
    return cfg.getZUI().getForScreen(e.getPoint());
  }

  @Override
  public String getToolTipText(final MouseEvent e) {
    final KanvasPainter painter = cfg.getPainter();
    final Point2D p = e.getPoint();
    String strHUD;
    if((strHUD = painter.getTooltipHUD(p)) != null) return strHUD;
    final Point2D c = cfg.getZUI().getForScreen(p);
    String str;
    return ((str = painter.getTooltip(c)) != null) ? str : null;
  }

  /**
   * Adds a keyboard action event.
   * 
   * @param key The key id, given by {@link java.awt.event.KeyEvent}. (Constants
   *          beginning with <code>VK</code>)
   * @param a The action that is performed.
   */
  public void addAction(final int key, final Action a) {
    final Object token = new Object();
    final InputMap input = getInputMap();
    input.put(KeyStroke.getKeyStroke(key, 0), token);
    final ActionMap action = getActionMap();
    action.put(token, a);
  }

  /** The map containing all keyboard messages. */
  private final Map<String, List<String>> messageActions = new HashMap<>();

  /** The action to handle all keyboard messages. */
  private Action messageDispatcher;

  /**
   * Adds a keyboard message.
   * 
   * @param key The key id, given by {@link java.awt.event.KeyEvent}. (Constants
   *          beginning with <code>VK</code>)
   * @param message The message that is posted.
   */
  public void addMessageAction(final int key, final String message) {
    // almost all printable keys
    final boolean printKey = (key >= 0x2C && key <= 0x39) || (key >= 0x41 && key <= 0x5A);
    final String cmd;
    if(printKey) {
      cmd = "" + (char) key;
    } else {
      cmd = "undef:" + key;
    }
    addMessage(cmd, message);
    if(!printKey) {
      addAction(key, new AbstractAction() {

        @Override
        public void actionPerformed(final ActionEvent e) {
          processForActionCommand(cmd);
        }

      });
    } else {
      final String lc = cmd.toLowerCase();
      if(!lc.equals(cmd)) {
        addMessage(lc, message);
      }
      if(messageDispatcher == null) {
        messageDispatcher = new AbstractAction() {

          @Override
          public void actionPerformed(final ActionEvent e) {
            final String cmd = e.getActionCommand();
            processForActionCommand(cmd);
          }

        };
      }
      addAction(key, messageDispatcher);
    }
  }

  /**
   * Processes messages for an action command.
   * 
   * @param cmd The action command.
   */
  void processForActionCommand(final String cmd) {
    final List<String> msgs = messageActions.get(cmd);
    if(msgs == null) throw new IllegalStateException("cmd must have list: " + cmd);
    for(final String m : msgs) {
      postMessage(m);
    }
    refresh();
  }

  /**
   * Adds the message for the given command.
   * 
   * @param cmd The command.
   * @param message The message.
   */
  private void addMessage(final String cmd, final String message) {
    if(!messageActions.containsKey(cmd)) {
      messageActions.put(cmd, new ArrayList<String>());
    }
    messageActions.get(cmd).add(message);
  }

  /** The frame rate displayer. */
  private FrameRateDisplayer frameRateDisplayer;

  /**
   * Setter.
   * 
   * @param frameRateDisplayer Sets the frame rate displayer. <code>null</code>
   *          stops time measuring.
   */
  public void setFrameRateDisplayer(final FrameRateDisplayer frameRateDisplayer) {
    this.frameRateDisplayer = frameRateDisplayer;
    refresh();
  }

  /**
   * Getter.
   * 
   * @return The current frame rate displayer or <code>null</code>.
   */
  public FrameRateDisplayer getFrameRateDisplayer() {
    return frameRateDisplayer;
  }

  /**
   * Getter.
   * 
   * @return Whether frame time is measured.
   */
  public boolean isMeasuringFrameTime() {
    return frameRateDisplayer != null && frameRateDisplayer.isActive();
  }

  @Override
  protected void paintComponent(final Graphics gfx) {
    final boolean mft = isMeasuringFrameTime();
    final AnimationBarrier barrier = this.barrier;
    // only use own stop-watch when no barrier is installed
    final Stopwatch watch = (mft && barrier == null) ? new Stopwatch() : null;
    final Graphics2D g = (Graphics2D) gfx.create();
    // honor opaqueness
    if(isOpaque()) {
      gfx.setColor(getBackground());
      gfx.fillRect(0, 0, getWidth(), getHeight());
    }
    // clip the visible area
    g.clip(getVisibleRect());
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if(barrier == null) {
      cfg.paint(g);
    } else {
      try (CloseBlock b = barrier.openDrawBlock()) {
        cfg.paint(g);
      }
    }
    if(mft) {
      final long nano = watch != null ? watch.currentNano() : barrier.lastCycle();
      frameRateDisplayer.setLastFrameTime(nano);
      frameRateDisplayer.drawFrameRate(g, getVisibleRect());
    }
    g.dispose();
  }

  /**
   * Returns the current canvas context. Note that it is not guaranteed that the
   * context returns correct values if the viewport changes after a call to this
   * method.
   * 
   * @return The current canvas context.
   */
  public KanvasContext getContext() {
    return cfg.getContext();
  }

  /**
   * Returns the current head-up display context. Note that it is not guaranteed
   * that the context returns correct values if the viewport changes after a
   * call to this method.
   * 
   * @return The current head-up display context.
   */
  public KanvasContext getHUDContext() {
    return cfg.getHUDContext();
  }

  /** The animator. */
  private Animator animator;

  /** The animation barrier. */
  private AnimationBarrier barrier;

  /**
   * Setter.
   * 
   * @param animator The animator or <code>null</code> if no animation is used.
   *          If the animator is non-<code>null</code> a new barrier is
   *          installed.
   */
  public void setAnimator(final Animator animator) {
    if(this.animator != null) {
      this.animator.setAnimationBarrier(null, this);
      if(barrier != null) {
        barrier.dispose();
      }
      barrier = null;
    }
    this.animator = animator;
    if(animator != null) {
      final AnimationList al = animator.getAnimationList();
      final CameraZUI zui = cfg.getZUI();
      al.addAnimated(zui);
      barrier = new AnimationBarrier(this);
      animator.setAnimationBarrier(barrier, this);
    }
  }

  /**
   * Getter.
   * 
   * @return The animator or <code>null</code> if no animation is used.
   */
  public Animator getAnimator() {
    return animator;
  }

  /**
   * Schedules the given action to be executed after the specified time in
   * milliseconds.
   * 
   * @param action The action to be executed. May be <code>null</code> when no
   *          action needs to be executed.
   * @param timing The timing to infer the duration.
   * @see jkanvas.animation.AnimationList#scheduleAction(AnimationAction,
   *      AnimationTiming)
   */
  public void scheduleAction(final AnimationAction action, final AnimationTiming timing) {
    scheduleAction(action, timing.duration);
  }

  /**
   * Schedules the given action to be executed after the specified time in
   * milliseconds.
   * 
   * @param action The action to be executed. May be <code>null</code> when no
   *          action needs to be executed.
   * @param delay The time to wait in milliseconds.
   * @see jkanvas.animation.AnimationList#scheduleAction(AnimationAction, long)
   */
  public void scheduleAction(final AnimationAction action, final long delay) {
    if(animator == null) throw new IllegalStateException("no animator installed");
    animator.getAnimationList().scheduleAction(action, delay);
  }

  /**
   * Getter.
   * 
   * @return The associated camera.
   */
  public Camera getCamera() {
    return cfg.getCamera();
  }

  /**
   * Setter.
   * 
   * @param cfg Sets the active view configuration.
   */
  public void setViewConfiguration(final ViewConfiguration cfg) {
    this.cfg = Objects.requireNonNull(cfg);
    if(animator != null) {
      final AnimationList al = animator.getAnimationList();
      final CameraZUI zui = cfg.getZUI();
      al.addAnimated(zui);
    }
  }

  /**
   * Getter.
   * 
   * @return The active view configuration.
   */
  public ViewConfiguration getViewConfiguration() {
    return cfg;
  }

  /** Zooms to fit the bounding box. */
  public void reset() {
    reset(cfg.getPainter().getBoundingBox());
  }

  /**
   * Resets the viewport to fit the bounding box.
   * 
   * @param timing The timing.
   * @param onFinish The action to perform when the viewport was set or when the
   *          method returns <code>false</code>. This method may be
   *          <code>null</code> when no action needs to be performed.
   * @return Whether the viewport is set.
   */
  public boolean reset(final AnimationTiming timing, final AnimationAction onFinish) {
    final Rectangle2D bbox = cfg.getPainter().getBoundingBox();
    cfg.getZUI().toView(bbox, timing, onFinish, true);
    return true;
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
  public void reset(final RectangularShape bbox) {
    reset(bbox, getMargin());
  }

  /**
   * Resets the viewport to show exactly the given rectangle expanded by the
   * margin.
   * 
   * @param bbox The rectangle that is visible.
   * @param margin The margin.
   */
  public void reset(final RectangularShape bbox, final double margin) {
    Objects.requireNonNull(bbox);
    final Rectangle2D rect = getVisibleRect();
    cfg.getZUI().showRectangle(bbox, rect, margin, true);
  }

  /**
   * Shows only the given rectangle. This may lead to parts of the rectangle
   * that are not shown.
   * 
   * @param bbox The rectangle.
   */
  public void showOnly(final RectangularShape bbox) {
    Objects.requireNonNull(bbox);
    cfg.getZUI().showRectangle(bbox, getVisibleRect(), getMargin(), false);
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
    return cfg.getZUI().toCanvas(getVisibleRect());
  }

  /**
   * Setter.
   * 
   * @param restriction Sets the restriction rectangle.
   * @param timing How the transition to the restriction rectangle should be
   *          performed.
   * @throws IllegalStateException When the canvas is not restricted. The canvas
   *           can be restricted only with the constructor.
   * @see #isRestricted()
   */
  public void setRestriction(final Rectangle2D restriction, final AnimationTiming timing) {
    cfg.setRestriction(restriction, timing, getMargin());
  }

  /**
   * Getter.
   * 
   * @return Whether the canvas is restricted.
   */
  public boolean isRestricted() {
    return cfg.isRestricted();
  }

  /**
   * Returns the minimal zoom value.
   * 
   * @return The minimal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  public double getMinZoom() {
    return cfg.getZUI().getMinZoom();
  }

  /**
   * Getter.
   * 
   * @return Whether zoom has a minimum.
   */
  public boolean hasMinZoom() {
    return cfg.getZUI().hasMinZoom();
  }

  /**
   * Sets the current minimal zoom value.
   * 
   * @param zoom The new minimal zoom value. Non-positive values indicate no
   *          restriction.
   */
  public void setMinZoom(final double zoom) {
    final CameraZUI zui = cfg.getZUI();
    zui.setMinZoom(zoom);
    zui.zoom(1, getVisibleCanvas());
  }

  /**
   * Returns the maximal zoom value.
   * 
   * @return The maximal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  public double getMaxZoom() {
    return cfg.getZUI().getMaxZoom();
  }

  /**
   * Getter.
   * 
   * @return Whether zoom has a maximum.
   */
  public boolean hasMaxZoom() {
    return cfg.getZUI().hasMaxZoom();
  }

  /**
   * Sets the current maximal zoom value.
   * 
   * @param zoom The new maximal zoom value. Non-positive values indicate no
   *          restriction.
   */
  public void setMaxZoom(final double zoom) {
    final CameraZUI zui = cfg.getZUI();
    zui.setMaxZoom(zoom);
    zui.zoom(1, getVisibleCanvas());
  }

  /**
   * Posts a message to be processed by {@link KanvasInteraction}. A message
   * contains of two parts: The optional id part and the actual message. The id
   * is separated from the message via the character '<code>#</code>'. Multiple
   * ids may be passed by separating them with space '<code>&#20;</code>'
   * 
   * @param msg The message to post.
   * @throws IllegalArgumentException If the message part is empty.
   */
  public void postMessage(final String msg) {
    final int idEnd = msg.indexOf('#');
    if(msg.isEmpty() || idEnd >= msg.length() - 1) throw new IllegalArgumentException(
        "empty message not allowed: '" + msg + "'");
    final String[] ids = idEnd < 0 ? new String[0] : msg.substring(0, idEnd).split(" ");
    int emptyCount = 0;
    for(final String id : ids) {
      if(id.isEmpty()) {
        ++emptyCount;
      }
    }
    String[] realIds;
    if(emptyCount > 0) {
      // handling empty strings
      realIds = new String[ids.length - emptyCount];
      int i = 0;
      for(final String id : ids) {
        if(id.isEmpty()) {
          continue;
        }
        realIds[i++] = id;
      }
    } else {
      realIds = ids;
    }
    final String m = msg.substring(idEnd + 1);
    cfg.getPainter().processMessage(realIds, m);
  }

  /** Disposes the painter. */
  public void dispose() {
    cfg.getPainter().dispose();
    setAnimator(null);
  }

  @Override
  public void refresh() {
    repaint();
  }

}
