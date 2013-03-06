package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.Animated;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.GroupAnimator;
import jkanvas.painter.AbstractRenderpass;

/**
 * Paints a layouted node link diagram.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The type of nodes.
 */
public class NodeLinkRenderpass<T extends AnimatedPosition> extends AbstractRenderpass {

  /** The node realizer. */
  private NodeRealizer<T> nodeRealizer;

  /** The edge realizer. */
  private EdgeRealizer<T> edgeRealizer;

  /** The view on the graph. */
  protected final NodeLinkView<T> view;

  /**
   * Creates a node link painter.
   * 
   * @param view The view on the graph.
   */
  public NodeLinkRenderpass(final NodeLinkView<T> view) {
    this.view = view;
  }

  /**
   * Setter.
   * 
   * @param edgeRealizer The edge realizer.
   */
  public void setEdgeRealizer(final EdgeRealizer<T> edgeRealizer) {
    this.edgeRealizer = Objects.requireNonNull(edgeRealizer);
  }

  /**
   * Getter.
   * 
   * @return The current edge realizer.
   */
  public EdgeRealizer<T> getEdgeRealizer() {
    return edgeRealizer;
  }

  /**
   * Setter.
   * 
   * @param nodeRealizer The node realizer.
   */
  public void setNodeRealizer(final NodeRealizer<T> nodeRealizer) {
    this.nodeRealizer = Objects.requireNonNull(nodeRealizer);
  }

  /**
   * Getter.
   * 
   * @return The current node realizer.
   */
  public NodeRealizer<T> getNodeRealizer() {
    return nodeRealizer;
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    renderEdges(gfx, ctx);
    renderNodes(gfx, ctx);
  }

  /**
   * Renders all nodes.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  private void renderNodes(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    for(final T node : view.nodes()) {
      final double x = node.getX();
      final double y = node.getY();
      final Shape nodeShape = nodeRealizer.createNodeShape(node, x, y);
      if(!nodeShape.intersects(visible)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      nodeRealizer.drawNode(g, node);
      g.dispose();
    }
  }

  /**
   * Renders all edges.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  private void renderEdges(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final EdgeRealizer<T> edgeRealizer = getEdgeRealizer();
    for(int i = 0; i < view.nodeCount(); ++i) {
      final T from = view.getNode(i);
      for(final int toId : view.edgesFrom(i)) {
        final T to = view.getNode(toId);
        final Shape edgeShape = edgeRealizer.createLineShape(from, to);
        if(!edgeShape.intersects(visible)) {
          continue;
        }
        final Graphics2D g = (Graphics2D) gfx.create();
        edgeRealizer.drawLines(g, edgeShape, from, to);
        g.dispose();
      }
    }
  }

  /**
   * Finds a node at the given position.
   * 
   * @param pos The position.
   * @return The node or <code>null</code> if there is no node at the given
   *         position.
   */
  public T pick(final Point2D pos) {
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    T cur = null;
    for(final T node : view.nodes()) {
      final double x = node.getX();
      final double y = node.getY();
      final Shape shape = nodeRealizer.createNodeShape(node, x, y);
      if(shape.contains(pos)) {
        // return the last match -- ie topmost node
        cur = node;
      }
    }
    return cur;
  }

  /** The group animator for the nodes. */
  private final GroupAnimator<T> animator = new GroupAnimator<T>() {

    @Override
    protected Iterable<T> members() {
      return view.nodes();
    }

    @Override
    protected Animated animated(final T member) {
      return member;
    }

  };

  @Override
  public Animated getAnimated() {
    return animator;
  }

}
