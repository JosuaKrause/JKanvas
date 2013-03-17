package jkanvas.util;

import static org.junit.Assert.*;

import java.util.Iterator;

import jkanvas.util.SnapshotList.Snapshot;

import org.junit.Test;

/**
 * Tests for the {@link SnapshotList}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class SnapshotListTest {

  /**
   * Whether the snapshot and the given list are equal.
   * 
   * @param s The snapshot.
   * @param list The list.
   */
  private static void equal(final Snapshot<String> s, final String[] list) {
    assertEquals(list.length, s.size());
    for(int i = 0; i < list.length; ++i) {
      assertEquals(list[i], s.get(i));
    }
  }

  /** Tests changing elements during the snapshot. */
  @Test
  public void addingDuringSnapshot() {
    final String a = "a", b = "b", c = "c", d = "d", e = "e";
    final String[] first = { a, b, c};
    final String[] then = { b, c, d, a};
    final SnapshotList<String> sl = new SnapshotList<>();
    sl.add(a);
    sl.add(b);
    sl.add(c);
    try (Snapshot<String> s = sl.getSnapshot()) {
      equal(s, first);
    }
    try (Snapshot<String> s = sl.getSnapshot()) {
      final Iterator<String> it = s.iterator();
      equal(s, first);
      sl.add(d);
      sl.remove(a);
      sl.add(e);
      sl.remove(d);
      sl.add(d);
      sl.remove(e);
      sl.add(a);
      for(int i = 0; i < first.length; ++i) {
        assertTrue(it.hasNext());
        assertEquals(first[i], it.next());
      }
      assertFalse(it.hasNext());
      assertEquals(1, sl.activeSnapshots());
      equal(s, first);
      try (Snapshot<String> inner = sl.getSnapshot()) {
        equal(inner, first);
        assertEquals(2, sl.activeSnapshots());
        s.close();
        equal(inner, first);
        assertEquals(1, sl.activeSnapshots());
        try {
          s.get(0);
          fail();
        } catch(final IllegalStateException ise) {
          // expected
        }
        try {
          s.size();
          fail();
        } catch(final IllegalStateException ise) {
          // expected
        }
        try {
          s.iterator();
          fail();
        } catch(final IllegalStateException ise) {
          // expected
        }
      }
      assertEquals(0, sl.activeSnapshots());
    }
    assertEquals(0, sl.activeSnapshots());
    try (Snapshot<String> s = sl.getSnapshot()) {
      equal(s, then);
    }
  }

  /** Ensures that elements can be added only once. */
  @Test
  public void addingOnlyOnce() {
    final String a = "a";
    final SnapshotList<String> sl = new SnapshotList<>();
    assertFalse(sl.has(a));
    sl.add(a);
    assertTrue(sl.has(a));
    try {
      sl.add(a);
      fail("should throw illegal argument exception");
    } catch(final IllegalArgumentException e) {
      // everything is fine
    }
  }

  /** Ensures that elements are directly removed when no snapshot is present. */
  @Test
  public void directRemoving() {
    final String a = "a";
    final SnapshotList<String> sl = new SnapshotList<>();
    sl.add(a);
    sl.remove(a);
    try (Snapshot<String> s = sl.getSnapshot()) {
      equal(s, new String[] { });
    }
    try {
      sl.remove(a);
      fail("should throw illegal argument exception");
    } catch(final IllegalArgumentException e) {
      // everything is fine
    }
  }
}
