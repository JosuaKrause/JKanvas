package jkanvas.nodelink;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import jkanvas.animation.Animated;
import jkanvas.util.BitSetIterable;
import jkanvas.util.ObjectDependencies;

/**
 * Creates a graph based on fields of objects referencing each other.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DependencyNodeLinkView implements NodeLinkView<IndexedPosition>, Animated {

  // TODO better array handling #16

  /** The base object. */
  private final Object base;

  /** The mapping from objects to indices. */
  private final Map<Object, Integer> backMap;

  /** The objects. */
  private final List<Object> objects;

  /** The positions of the objects. */
  private final List<IndexedPosition> pos;

  /** The edges. */
  private final BitSet edges;

  /** The object dependency. */
  private final ObjectDependencies dependency;

  /**
   * Creates a node-link view of dependencies.
   * 
   * @param base The base object.
   */
  public DependencyNodeLinkView(final Object base) {
    this(base, new ObjectDependencies());
  }

  /**
   * Creates a node-link view of dependencies.
   * 
   * @param base The base object.
   * @param dependency The object dependency handler.
   */
  public DependencyNodeLinkView(final Object base, final ObjectDependencies dependency) {
    this.dependency = Objects.requireNonNull(dependency);
    this.base = Objects.requireNonNull(base);
    backMap = new IdentityHashMap<>();
    objects = new ArrayList<>();
    pos = new ArrayList<>();
    edges = new BitSet();
  }

  /**
   * Fills all objects in.
   * 
   * @param o The base object.
   */
  private void fillObjects(final Object o) {
    backMap.clear();
    objects.clear();
    final Queue<Object> objs = new LinkedList<>();
    dependency.addRef(objs, o);
    while(!objs.isEmpty()) {
      if(objects.size() > 1000) return;
      final Object cur = objs.poll();
      if(backMap.containsKey(cur)) {
        continue;
      }
      final int pos = objects.size();
      objects.add(cur);
      backMap.put(cur, pos);
      final Set<Object> set = dependency.neighbors(cur);
      objs.addAll(set);
    }
  }

  /** Fills all edges in. */
  private void fillEdges() {
    edges.clear();
    for(final Object f : objects) {
      final Integer from = backMap.get(f);
      if(from == null) {
        continue;
      }
      final Set<Object> neighbors = dependency.neighbors(f);
      for(final Object n : neighbors) {
        final Integer to = backMap.get(n);
        if(to != null) {
          edges.set(edgePos(from, to));
        }
      }
    }
  }

  /** The last time of animation. */
  private long lastTime;

  @Override
  public boolean animate(final long currentTime) {
    if(lastTime != 0 && currentTime - lastTime < 10000) return false;
    pos.clear();
    fillObjects(base);
    fillEdges();
    // TODO use layout algorithm #16
    final double maxX = 1000;
    double x = 0;
    double y = 0;
    for(int i = 0; i < objects.size(); ++i) {
      pos.add(new IndexedPosition(x, y, i));
      x += 100;
      if(x > maxX) {
        x = 0;
        y += 100;
      }
    }
    lastTime = currentTime;
    return true;
  }

  @Override
  public int nodeCount() {
    return pos.size();
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The short name.
   */
  public String shortName(final int index) {
    final Object o = objects.get(index);
    final Class<? extends Object> clazz = o.getClass();
    return clazz.isAnonymousClass() ? clazz.getName() : clazz.getSimpleName();
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The info.
   */
  public String info(final int index) {
    final Object o = objects.get(index);
    final Class<? extends Object> clazz = o.getClass();
    final String arr;
    if(clazz.isArray() && !clazz.getComponentType().isPrimitive()) {
      final StringBuilder a = new StringBuilder("<br>[");
      final Object[] array = (Object[]) o;
      boolean first = true;
      for(final Object el : array) {
        if(first) {
          first = false;
        } else {
          a.append(", ");
        }
        a.append(System.identityHashCode(el));
      }
      a.append("]");
      arr = a.toString();
    } else {
      arr = "";
    }
    return "<html>" + clazz.getName() + "<br>("
        + System.identityHashCode(o) + ")<br>" + o.toString() + arr;
  }

  @Override
  public String getName(final int index) {
    return shortName(index);
  }

  /**
   * Computes the position of an edge.
   * 
   * @param from The from index.
   * @param to The to index.
   * @return The position in the bit set.
   */
  private int edgePos(final int from, final int to) {
    return from * objects.size() + to;
  }

  @Override
  public boolean areConnected(final int a, final int b) {
    return edges.get(edgePos(a, b));
  }

  @Override
  public Iterable<Integer> edgesFrom(final int from) {
    final BitSet set = edges;
    final int start = edgePos(from, 0);
    final int end = edgePos(from + 1, 0);
    final Iterable<Integer> iter = new Iterable<Integer>() {

      private final Iterable<Integer> iterable = new BitSetIterable(set, start, end);

      @Override
      public Iterator<Integer> iterator() {
        final Iterable<Integer> iterable = this.iterable;
        return new Iterator<Integer>() {

          private final Iterator<Integer> it = iterable.iterator();

          @Override
          public boolean hasNext() {
            return it.hasNext();
          }

          @Override
          public Integer next() {
            return it.next() - start;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
    return iter;
  }

  @Override
  public IndexedPosition getNode(final int index) {
    return pos.get(index);
  }

  @Override
  public Iterable<IndexedPosition> nodes() {
    return pos;
  }

  @Override
  public boolean isDirected() {
    return true;
  }

}
