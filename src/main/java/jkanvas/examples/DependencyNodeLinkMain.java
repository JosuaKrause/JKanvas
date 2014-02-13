package jkanvas.examples;

import jkanvas.Canvas;
import jkanvas.CanvasSetup;
import jkanvas.animation.AnimatedPainter;
import jkanvas.nodelink.DependencyNodeLinkRenderpass;
import jkanvas.nodelink.IndexedPosition;
import jkanvas.nodelink.layout.ForceDirectedLayouter;
import jkanvas.util.ObjectDependencies;

/**
 * A small example showing dependencies between objects.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class DependencyNodeLinkMain {

  /**
   * Creates a window with the node-link diagram.
   * 
   * @param start The start object or <code>null</code> if the own canvas should
   *          be used.
   * @param pkgs The allowed packages.
   */
  public static void createFrame(final Object start, final String[] pkgs) {
    final int w = 800;
    final int h = 600;
    // configure Canvas
    final AnimatedPainter p = new AnimatedPainter();
    final Canvas c = new Canvas(p, w, h);
    CanvasSetup.setupCanvas("Node-Link", c, p, true, true, false, false);
    final DependencyNodeLinkRenderpass pass =
        new DependencyNodeLinkRenderpass(c, start == null ? c : start, pkgs);
    p.addPass(pass);
    pass.getView().setLayouter(new ForceDirectedLayouter<IndexedPosition>());
  }

  /**
   * Starts a small example.
   * 
   * @param args The ignored arguments.
   */
  public static void main(final String[] args) {
    // Canvas.DEBUG_BBOX = true;
    createFrame(null, ObjectDependencies.STD_CLASSES);
  }

}
