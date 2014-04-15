package jkanvas.util;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
    try {
      assertEquals(list.length, s.size());
      for(int i = 0; i < list.length; ++i) {
        assertEquals(list[i], s.get(i));
      }
    } catch(final AssertionError e) {
      // print content on failure
      for(final String item : s) {
        System.out.println(item);
      }
      throw e;
    }
  }

  /** Tests changing elements during the snapshot. */
  @Test
  public void addingDuringSnapshot() {
    final String a = "a", b = "b", c = "c", d = "d", e = "e";
    final String[] first = { a, b, c};
    final String[] then = { a, b, c, d, e};
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
      sl.add(e);
      sl.add(d);
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
    sl.add(a);
    sl.add(a);
    try (Snapshot<String> s = sl.getSnapshot()) {
      assertEquals(1, s.size());
      assertEquals(a, s.get(0));
      final Iterator<String> it = s.iterator();
      assertTrue(it.hasNext());
      assertEquals(a, it.next());
      assertFalse(it.hasNext());
      try {
        it.next();
        fail();
      } catch(final NoSuchElementException e) {
        // expected behavior
      }
    }
  }

  /** Needs failure when snapshots closes during iteration. */
  @Test
  public void prematureClose() {
    final String a = "a";
    final SnapshotList<String> sl = new SnapshotList<>();
    sl.add(a);
    @SuppressWarnings("resource")
    final Snapshot<String> s = sl.getSnapshot();
    final Iterator<String> it = s.iterator();
    assertTrue(it.hasNext());
    s.close();
    try {
      it.next();
      fail();
    } catch(final IllegalStateException e) {
      // expected behavior
    }
  }

  /**
   * Tests whether automatic removal via garbage collection works fine.
   *
   * @param numVolatile The number of volatile objects that will be removed.
   * @param numConst The number of constant objects that must no be removed.
   * @param add Whether to add during the snapshot.
   * @param volatileBefore Whether the volatile elements are added before the
   *          objects.
   */
  private static void testGC(final int numVolatile, final int numConst,
      final boolean add, final boolean volatileBefore) {
    final SnapshotList<Object> list = new SnapshotList<>();
    final Object[] objConst = new Object[numConst];
    if(volatileBefore) {
      for(int i = 0; i < numVolatile; ++i) {
        list.add(new Object());
      }
    }
    for(int i = 0; i < objConst.length; ++i) {
      objConst[i] = new Object();
      list.add(objConst[i]);
    }
    if(!volatileBefore) {
      for(int i = 0; i < numVolatile; ++i) {
        list.add(new Object());
      }
    }
    // when test fails increase to generate more garbage for the GC
    final int tmp = 100;
    int turns = 10;
    int curNum;
    do {
      Object[] objTmp = new Object[tmp];
      for(int k = 0; k < objTmp.length; ++k) {
        objTmp[k] = new Object();
      }
      for(int k = 0; k < objTmp.length; ++k) {
        objTmp[k] = null;
      }
      objTmp = null;
      curNum = 0;
      try (Snapshot<Object> s = list.getSnapshot()) {
        for(int k = 0; k < 10; ++k) {
          System.gc();
        }
        for(final Object o : s) {
          if(o != null) {
            ++curNum;
          }
        }
        if(add) {
          for(int i = 0; i < objConst.length; ++i) {
            list.add(objConst[i]);
          }
        }
      }
    } while(--turns > 0 && curNum != numConst);
    if(turns <= 0) {
      fail();
    }
    try (Snapshot<Object> s = list.getSnapshot()) {
      assertEquals(numConst, s.size());
      for(int i = 0; i < s.size(); ++i) {
        assertEquals(objConst[i], s.get(i));
      }
    }
  }

  /** Tests whether the automatic removal of elements works correctly. */
  @Test
  public void testGC() {
    // !!! very slow test !!!
    testGC(0, 10000, true, true);
    testGC(10000, 10000, false, true);
    testGC(10000, 10000, true, true);
    testGC(10000, 10000, false, false);
    testGC(10000, 10000, true, false);
  }

}
