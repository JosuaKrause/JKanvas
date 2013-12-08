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
import jkanvas.nodelink.layout.BouncingLayouter;
import jkanvas.nodelink.layout.CircleLayouter;
import jkanvas.nodelink.layout.ForceDirectedLayouter;
import jkanvas.nodelink.layout.RandomLayouter;
import jkanvas.nodelink.layout.SimpleLayoutedView;
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
  private final SimpleLayoutedView<AnimatedPosition> simpleView;

  /**
   * Creates a node-link diagram.
   * 
   * @param view The view on the graph.
   */
  public NodeLinkMain(final SimpleLayoutedView<AnimatedPosition> view) {
    super(view);
    simpleView = view;
    setIds("nl");
  }

  @Override
  protected void processMessage(final String msg) {
    switch(msg) {
      case "random":
        simpleView.setLayouter(new RandomLayouter<>());
        break;
      case "circle":
        simpleView.setLayouter(new CircleLayouter<>());
        break;
      case "force":
        simpleView.setLayouter(new ForceDirectedLayouter<>());
        break;
      case "bounce":
        simpleView.setLayouter(new BouncingLayouter<>());
        break;
    }
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
  public void getBoundingBox(final Rectangle2D bbox) {
    final NodeRealizer<AnimatedPosition> n = getNodeRealizer();
    bbox.setFrame(0, 0, 0, 0);
    for(final AnimatedPosition p : view.nodes()) {
      final double x = p.getX();
      final double y = p.getY();
      final Shape shape = n.createNodeShape(p, x, y);
      final Rectangle2D b = shape.getBounds2D();
      if(bbox.isEmpty()) {
        bbox.setFrame(b);
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
  }

  /**
   * Fills the given graph.
   * 
   * @param view The view on the graph.
   * @param nodes The number of nodes.
   * @param edges The number of edges.
   */
  private static void fillGraph(final SimpleNodeLinkView<AnimatedPosition> view,
      final int nodes, final int edges) {
    for(int i = 0; i < nodes; ++i) {
      view.addNode(new AnimatedPosition(0, 0));
    }
    final int numMain = 5;
    final int factorMore = 10000;
    final Random rnd = new Random();
    for(int i = 0; i < edges; ++i) {
      final int start = rnd.nextInt(nodes);
      int end = rnd.nextInt(nodes + factorMore * numMain);
      if(end >= nodes) {
        end = end % numMain;
      }
      view.addEdge(start, end);
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
    final AnimatedPainter p = new AnimatedPainter();
    final Canvas c = new Canvas(p, w, h);
    final SimpleLayoutedView<AnimatedPosition> view = new SimpleLayoutedView<>(c, false);
    fillGraph(view, nodes, edges);
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
    c.setBackground(Color.WHITE);
    c.setFrameRateDisplayer(new FrameRateHUD());
    p.addRefreshable(c);
    c.setAnimator(p);
    final JFrame frame = new JFrame("Node-Link") {

      @Override
      public void dispose() {
        // The Canvas also disposes the animator,
        // which terminates the animation daemon
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
    c.addMessageAction(KeyEvent.VK_1, "nl#random");
    c.addMessageAction(KeyEvent.VK_2, "nl#circle");
    c.addMessageAction(KeyEvent.VK_3, "nl#force");
    c.addMessageAction(KeyEvent.VK_4, "nl#bounce");
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
    info.setIds("info");
    c.addMessageAction(KeyEvent.VK_H, "info#visible:toggle");
    info.addLine("T: Pause animation");
    info.addLine("F: Toggle Framerate Display");
    info.addLine("P: Take Photo");
    info.addLine("H: Toggle Help");
    info.addLine("1: Lay out nodes randomly once");
    info.addLine("2: Lay out nodes in a circle");
    info.addLine("3: Force directed layout");
    info.addLine("4: Bounce layout");
    info.addLine("Q/ESC: Quit");
    p.addHUDPass(info);
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    // TODO start with MDS layout #24
    final RandomLayouter<AnimatedPosition> rl = new RandomLayouter<>();
    rl.setTiming(AnimationTiming.NO_ANIMATION);
    view.setLayouter(rl);
  }

}
