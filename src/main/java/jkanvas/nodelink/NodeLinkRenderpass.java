package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationList;
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

  /** The animation list. */
  private AnimationList list;

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
   * Signals that a garbage collection on animated nodes is needed. This flag is
   * set when the drawing routine detects that more nodes are in the node set
   * than in the node list. A garbage collection works as follows. At first all
   * nodes from the node set are removed from the {@link AnimationList}. Then
   * the list is automatically filled again during drawing.
   */
  private boolean gc;

  /**
   * The node set. This set is used to detect whether a garbage collection is
   * necessary.
   */
  private final Set<T> lastNodes = Collections.newSetFromMap(new IdentityHashMap<T, Boolean>());

  /**
   * Renders all nodes.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  private void renderNodes(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    if(gc) {
      // clear all nodes from the animated list
      // it will be reconstructed when drawing
      // removing and adding has to be made on the
      // same draw run because otherwise the animation
      // lock would be released in between which
      // may lead to missed node animation calls
      for(final T node : lastNodes) {
        list.removeAnimated(node);
      }
      lastNodes.clear();
      gc = false;
    }
    int count = 0;
    for(final T node : view.nodes()) {
      ++count;
      // automatically adds new nodes to the animation list
      // this needs only to be done in the draw method
      if(!lastNodes.contains(node)) {
        list.addAnimated(node);
        lastNodes.add(node);
      }
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
    if(count != lastNodes.size()) {
      // we got more nodes than drawn
      // next draw is a garbage collection
      gc = true;
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

  @Override
  public void setAnimationList(final AnimationList list) {
    this.list = list;
  }

}
