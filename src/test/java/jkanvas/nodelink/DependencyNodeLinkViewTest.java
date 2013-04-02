package jkanvas.nodelink;

import static junit.framework.Assert.*;

import org.junit.Test;

public class DependencyNodeLinkViewTest {

  @Test
  public void normalObjects() {
    final Object o = new Object();
    final DependencyNodeLinkView view = new DependencyNodeLinkView(o);
    assertEquals(0, view.nodeCount());
  }

}
