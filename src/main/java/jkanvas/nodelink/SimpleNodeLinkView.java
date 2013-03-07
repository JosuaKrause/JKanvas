package jkanvas.nodelink;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jkanvas.animation.Position2D;

/**
 * Creates a simple view on a graph. Nodes can only be added.
 * 
 * @author Joschi <josua.krause@googlemail.com>
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
   * Creates a simple node link view.
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
  }

  @Override
  public String getName(final int index) {
    return "Node " + index;
  }

  /**
   * Iterates over the edges given by a bit set.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static final class EdgeIterator implements Iterator<Integer> {

    /** The bit set. */
    private final BitSet edges;

    /** The current edge position. */
    private int pos;

    /**
     * Creates an edge iterator.
     * 
     * @param start The source id or exactly <code>-1</code> if the graph is
     *          directed.
     * @param edges The edge bit set.
     */
    public EdgeIterator(final int start, final BitSet edges) {
      this.edges = edges;
      pos = edges.nextSetBit(start + 1);
    }

    @Override
    public boolean hasNext() {
      return pos >= 0;
    }

    @Override
    public Integer next() {
      if(pos < 0) throw new NoSuchElementException();
      final int ret = pos;
      pos = edges.nextSetBit(pos + 1);
      return ret;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  @Override
  public Iterable<Integer> edgesFrom(final int node) {
    final int start = isDirected() ? -1 : node;
    final BitSet es = edges.get(node);
    return new Iterable<Integer>() {

      @Override
      public Iterator<Integer> iterator() {
        return new EdgeIterator(start, es);
      }

    };
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
  }

  @Override
  public boolean isDirected() {
    return isDirected;
  }

}
