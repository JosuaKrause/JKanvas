package jkanvas.nodelink;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jkanvas.animation.Position2D;
import jkanvas.util.BitSetIterable;

/**
 * Creates a simple view on a graph. Nodes can only be added.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The position type.
 */
public class SimpleNodeLinkView<T extends Position2D> implements NodeLinkView<T> {

  /** Reverse map from nodes to ids. */
  private final Map<T, Integer> idMap = new HashMap<>();

  /** Dense list of nodes. */
  private final List<T> nodes = new ArrayList<>();

  /** List containing edges. */
  private final List<BitSet> edges = new ArrayList<>();

  /** Whether the graph is directed. */
  private final boolean isDirected;

  /**
   * Creates a simple node-link view.
   * 
   * @param isDirected Whether the graph is directed.
   */
  public SimpleNodeLinkView(final boolean isDirected) {
    this.isDirected = isDirected;
  }

  @Override
  public Iterable<T> nodes() {
    return nodes;
  }

  @Override
  public T getNode(final int id) {
    return nodes.get(id);
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The corresponding id.
   */
  private int getId(final T node) {
    return idMap.get(node);
  }

  @Override
  public int nodeCount() {
    return nodes.size();
  }

  /** This method is called whenever the graph changes. */
  protected void onChange() {
    // nothing to do
  }

  /**
   * Adds a node.
   * 
   * @param node The node.
   */
  public void addNode(final T node) {
    if(idMap.containsKey(node)) throw new IllegalArgumentException(
        "node " + node + " already added");
    idMap.put(node, nodes.size());
    nodes.add(node);
    edges.add(new BitSet());
    onChange();
  }

  @Override
  public String getName(final int index) {
    return "Node " + index;
  }

  @Override
  public Iterable<Integer> edgesFrom(final int node) {
    final int start = isDirected() ? 0 : node + 1;
    final BitSet es = edges.get(node);
    return new BitSetIterable(es, start);
  }

  /**
   * Checks whether the given indices are within bounds.
   * 
   * @param from The source index.
   * @param to The destination index.
   */
  private void checkIndex(final int from, final int to) {
    final int count = nodeCount();
    if(from < 0 || from >= count) throw new IndexOutOfBoundsException(
        "from out of bounds: " + from + " count: " + count);
    if(to < 0 || to >= count) throw new IndexOutOfBoundsException(
        "to out of bounds: " + to + " count: " + count);
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
    checkIndex(from, to);
    edges.get(from).set(to);
    if(!isDirected()) {
      edges.get(to).set(from);
    }
    onChange();
  }

  /**
   * Removes the edge given by the nodes.
   * 
   * @param from The source node.
   * @param to The destination node.
   */
  public void removeEdge(final T from, final T to) {
    removeEdge(getId(from), getId(to));
  }

  /**
   * Removes the edge given by the nodes.
   * 
   * @param from The source node index.
   * @param to The destination node index.
   */
  public void removeEdge(final int from, final int to) {
    checkIndex(from, to);
    edges.get(from).clear(to);
    if(!isDirected()) {
      edges.get(to).clear(from);
    }
    onChange();
  }

  @Override
  public boolean areConnected(final int from, final int to) {
    checkIndex(from, to);
    return edges.get(from).get(to);
  }

  /** Clears all edges. */
  public void clearEdges() {
    for(final BitSet es : edges) {
      es.clear();
    }
    onChange();
  }

  @Override
  public boolean isDirected() {
    return isDirected;
  }

}
