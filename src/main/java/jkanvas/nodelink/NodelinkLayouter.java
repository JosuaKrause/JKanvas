package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedLayouter;
import jkanvas.animation.AnimatedPosition;
import jkanvas.painter.AbstractRenderpass;
import jkanvas.painter.Renderpass;

/**
 * Paints a node link diagram.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The type of nodes.
 */
public class NodelinkLayouter<T extends AnimatedPosition> implements AnimatedLayouter {

  /** Reverse map from nodes to ids. */
  private final Map<T, Integer> idMap = new HashMap<>();

  /** Dense list of nodes. */
  private final List<T> nodes = new ArrayList<>();

  /**
   * List containing edges. Edges always go from lower to higher indices. Nodes
   * are stored as integers.
   */
  private final List<List<Integer>> edges = new ArrayList<>();

  /** The node realizer. */
  private NodeRealizer<T> nodeRealizer;

  /** The edge realizer. */
  private EdgeRealizer<T> edgeRealizer;

  /** The node render pass. */
  private final AbstractRenderpass nodePass;

  /** The edge render pass. */
  private final AbstractRenderpass edgePass;

  /** Creates a node link painter. */
  public NodelinkLayouter() {
    nodePass = new AbstractRenderpass(false) {

      @Override
      public void render(final Graphics2D gfx, final KanvasContext ctx) {
        renderEdges(gfx, ctx);
      }

    };
    edgePass = new AbstractRenderpass(false) {

      @Override
      public void render(final Graphics2D gfx, final KanvasContext ctx) {
        renderNodes(gfx, ctx);
      }

    };
  }

  /**
   * Getter.
   * 
   * @return The node render pass.
   */
  public Renderpass getNodePass() {
    return nodePass;
  }

  /**
   * Getter.
   * 
   * @return The edge render pass.
   */
  public Renderpass getEdgePass() {
    return edgePass;
  }

  /**
   * Getter.
   * 
   * @return The list of nodes.
   */
  protected List<T> getNodes() {
    return nodes;
  }

  /**
   * Getter.
   * 
   * @param id The id of the node.
   * @return The corresponding node.
   */
  public T getNode(final int id) {
    return nodes.get(id);
  }

  @Override
  public Iterable<? extends AnimatedPosition> getPositions() {
    return nodes;
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The corresponding id.
   */
  public int getId(final T node) {
    return idMap.get(node);
  }

  /**
   * Getter.
   * 
   * @return The number of nodes.
   */
  public int nodeCount() {
    return nodes.size();
  }

  /** The empty list. */
  private final List<Integer> EMPTY_LIST = Collections.emptyList();

  /**
   * Returns all edges from the given node. Note that this contains only nodes
   * with a higher id.
   * 
   * @param node The starting id.
   * @return All connected edges with a higher id.
   */
  protected List<Integer> getEdges(final int node) {
    if(node >= edges.size() || edges.get(node) == null) return EMPTY_LIST;
    return edges.get(node);
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

  /**
   * Adds an edge between two nodes.
   * 
   * @param from The first node.
   * @param to The second node.
   */
  public void addEdge(final T from, final T to) {
    addEdge(getId(from), getId(to));
  }

  /**
   * Adds an edge between two nodes.
   * 
   * @param from The id of the first node.
   * @param to The id of the second node.
   */
  public void addEdge(final int from, final int to) {
    if(from == to) return;
    if(from > to) {
      addEdge(to, from);
      return;
    }
    assert from < to;
    while(edges.size() <= from) {
      edges.add(null);
    }
    if(edges.get(from) == null) {
      edges.set(from, new ArrayList<Integer>());
    }
    final List<Integer> fromList = edges.get(from);
    if(!fromList.contains(to)) {
      fromList.add(to);
    }
  }

  /**
   * Adds a node.
   * 
   * @param node The node.
   */
  public void addNode(final T node) {
    if(idMap.containsKey(node)) throw new IllegalArgumentException("node "+node+" already added");
    idMap.put(node, nodes.size());
    nodes.add(node);
  }

  /**
   * Renders all edges.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  protected void renderEdges(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final EdgeRealizer<T> edgeRealizer = getEdgeRealizer();
    for(int i = 0; i < nodes.size(); ++i) {
      final T from = getNode(i);
      for(final int toId : getEdges(i)) {
        final T to = getNode(toId);
        final Shape edgeShape = edgeRealizer.createLineShape(from, to);
        if(!edgeShape.intersects(visible)) {
          continue;
        }
        final Graphics2D g = (Graphics2D) gfx.create();
        edgeRealizer.drawLines(g, from, to);
        g.dispose();
      }
    }
  }

  /**
   * Renders all nodes.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  protected void renderNodes(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    for(final T node : getNodes()) {
      final Shape nodeShape = nodeRealizer.createNodeShape(node);
      if(!nodeShape.intersects(visible)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      nodeRealizer.drawNode(g, node);
      g.dispose();
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
    for(final T node : getNodes()) {
      final Shape shape = nodeRealizer.createNodeShape(node);
      if(shape.contains(pos)) return node;
    }
    return null;
  }

}
