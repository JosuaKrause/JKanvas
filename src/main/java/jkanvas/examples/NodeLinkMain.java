package jkanvas.examples;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.FrameRateDisplayer;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationTiming;
import jkanvas.nodelink.DefaultEdgeRealizer;
import jkanvas.nodelink.DefaultNodeRealizer;
import jkanvas.nodelink.NodeLinkRenderpass;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.SimpleNodeLinkView;
import jkanvas.painter.FrameRateHUD;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.TextHUD;
import jkanvas.util.Screenshot;

/**
 * A small node-link example application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class NodeLinkMain extends NodeLinkRenderpass<AnimatedPosition> {

  /** The color for no selection. */
  private static final Color NO_SEL = new Color(247, 247, 247);

  /** The color for primary selection. */
  private static final Color PRIM_SEL = new Color(202, 0, 32);

  /** The color for secondary selection. */
  private static final Color SEC_SEL = new Color(5, 113, 176);

  /** The simple view in order to add nodes dynamically. */
  private final SimpleNodeLinkView<AnimatedPosition> simpleView;

  /**
   * Creates a node link diagram.
   * 
   * @param view The view on the graph.
   */
  public NodeLinkMain(final SimpleNodeLinkView<AnimatedPosition> view) {
    super(view);
    simpleView = view;
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The color of the node.
   */
  public Color getNodeColor(final AnimatedPosition node) {
    return primSel == node ? PRIM_SEL : (secSel == node ? SEC_SEL : NO_SEL);
  }

  /** The primary selection. */
  private AnimatedPosition primSel;

  /** The start x position of the current drag. */
  private double startX;

  /** The start y position of the current drag. */
  private double startY;

  /** The secondary selection. */
  private AnimatedPosition secSel;

  @Override
  public boolean click(final Point2D pos, final MouseEvent e) {
    if(!SwingUtilities.isRightMouseButton(e)) return false;
    final AnimatedPosition n = pick(pos);
    if(n == null) {
      // no node selected -- add new node
      simpleView.addNode(new AnimatedPosition(pos));
      return true;
    }
    if(secSel != null) {
      // when selecting a second node create edge
      simpleView.addEdge(secSel, n);
      secSel = null;
    } else {
      // select node
      secSel = n;
    }
    return true;
  }

  @Override
  public String getTooltip(final Point2D p) {
    final AnimatedPosition n = pick(p);
    if(n == null) return null;
    return "x: " + n.getX() + " y: " + n.getY();
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    final AnimatedPosition n = pick(p);
    if(n == null) return false;
    // initialize node dragging
    primSel = n;
    primSel.clearAnimation();
    startX = primSel.getX();
    startY = primSel.getY();
    return true;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    primSel.setPosition(startX + dx, startY + dy);
  }

  @Override
  public void endDrag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    drag(start, cur, dx, dy);
    primSel = null;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    final NodeRealizer<AnimatedPosition> n = getNodeRealizer();
    Rectangle2D bbox = null;
    for(final AnimatedPosition p : view.nodes()) {
      final double x = p.getX();
      final double y = p.getY();
      final Shape shape = n.createNodeShape(p, x, y);
      final Rectangle2D b = shape.getBounds2D();
      if(bbox == null) {
        bbox = b;
      } else {
        bbox.add(b);
      }
      // include the end of the animation in the bounding box
      if(p.inAnimation()) {
        final double px = p.getPredictX();
        final double py = p.getPredictY();
        final Shape endShape = n.createNodeShape(p, px, py);
        bbox.add(endShape.getBounds2D());
      }
    }
    return bbox;
  }

  /**
   * Fills the given graph.
   * 
   * @param view The view on the graph.
   * @param width The allowed width.
   * @param height The allowed height.
   * @param nodes The number of nodes.
   * @param edges The number of edges.
   */
  private static void fillGraph(final SimpleNodeLinkView<AnimatedPosition> view,
      final int width, final int height, final int nodes, final int edges) {
    final double rad = DefaultNodeRealizer.RADIUS;
    final Random rnd = new Random();
    for(int i = 0; i < nodes; ++i) {
      view.addNode(new AnimatedPosition(rad + rnd.nextDouble() * (width - 2 * rad),
          rad + rnd.nextDouble() * (height - 2 * rad)));
    }
    for(int i = 0; i < edges; ++i) {
      view.addEdge(rnd.nextInt(nodes), rnd.nextInt(nodes));
    }
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    // Canvas.DEBUG_BBOX = true;
    final int w = 800;
    final int h = 600;
    final int nodes = 20;
    final int edges = 100;
    final SimpleNodeLinkView<AnimatedPosition> view = new SimpleNodeLinkView<>(false);
    fillGraph(view, w, h, nodes, edges);
    final AnimatedPainter p = new AnimatedPainter();
    final NodeLinkMain r = new NodeLinkMain(view);
    r.setEdgeRealizer(new DefaultEdgeRealizer<>());
    r.setNodeRealizer(new DefaultNodeRealizer<AnimatedPosition>() {

      @Override
      public Color getColor(final AnimatedPosition node) {
        return r.getNodeColor(node);
      }

    });
    p.addPass(r);
    // configure Canvas
    final Canvas c = new Canvas(p, w, h);
    c.setBackground(Color.WHITE);
    c.setFrameRateDisplayer(new FrameRateHUD());
    p.addRefreshable(c);
    c.setAnimator(p);
    final JFrame frame = new JFrame("Node-Link") {

      @Override
      public void dispose() {
        // The Canvas also disposes the animator, which terminates the animation
        // daemon
        c.dispose();
        super.dispose();
      }

    };
    // add actions
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    c.addAction(KeyEvent.VK_F, new AbstractAction() {

      private FrameRateDisplayer frd;

      @Override
      public void actionPerformed(final ActionEvent e) {
        final FrameRateDisplayer tmp = frd;
        frd = c.getFrameRateDisplayer();
        c.setFrameRateDisplayer(tmp);
      }

    });
    final SimpleTextHUD pause = new SimpleTextHUD(TextHUD.LEFT, TextHUD.TOP);
    pause.addLine("paused");
    pause.setVisible(false);
    p.addHUDPass(pause);
    c.addAction(KeyEvent.VK_T, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final boolean b = !p.isStopped();
        pause.setVisible(b);
        p.setStopped(b);
      }

    });
    c.addAction(KeyEvent.VK_R, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final Rectangle2D rect = c.getVisibleCanvas();
        final double w = rect.getWidth();
        final double h = rect.getHeight();
        final double r = Math.min(w, h) / 2 - DefaultNodeRealizer.RADIUS;
        final int count = view.nodeCount();
        final double step = 2 * Math.PI / count;
        double angle = 0;
        for(int i = 0; i < count; ++i) {
          final double x = rect.getCenterX() + Math.sin(angle) * r;
          final double y = rect.getCenterY() + Math.cos(angle) * r;
          view.getNode(i).startAnimationTo(new Point2D.Double(x, y),
              AnimationTiming.SMOOTH);
          angle += step;
        }
      }

    });
    c.addAction(KeyEvent.VK_P, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        try {
          final File png = Screenshot.savePNG(new File("pics"), "nodelink", c);
          System.out.println("Saved screenshot in " + png);
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }

    });
    final SimpleTextHUD info = new SimpleTextHUD(TextHUD.RIGHT, TextHUD.BOTTOM);
    c.addAction(KeyEvent.VK_H, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        info.setVisible(!info.isVisible());
        c.refresh();
      }

    });
    info.addLine("T: Pause animation");
    info.addLine("F: Toggle Framerate Display");
    info.addLine("P: Take Photo");
    info.addLine("H: Toggle Help");
    info.addLine("R: Lay out nodes in a circle");
    info.addLine("Q/ESC: Quit");
    p.addHUDPass(info);
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

}
