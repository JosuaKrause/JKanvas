package jkanvas.io.json;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.animation.AnimationAction;
import jkanvas.util.Resource;

/**
 * Sets up a canvas with a frame from a given JSON resource.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class JSONSetup {

  /** No constructor. */
  private JSONSetup() {
    throw new AssertionError();
  }

  /**
   * Sets up the canvas by creating a frame. The frame is shown directly.
   * 
   * @param name The name of the frame.
   * @param mng The JSON manager.
   * @param json The JSON resource.
   * @param reset Whether to reset the view.
   * @throws IOException I/O Exception.
   */
  public static void setupCanvas(final String name, final JSONManager mng,
      final Resource json, final boolean reset) throws IOException {
    final JFrame frame = new JFrame(name);
    setupCanvas(frame, mng, json, true, reset);
  }

  /**
   * Sets up the canvas.
   * 
   * @param frame The frame.
   * @param mng The JSON manager.
   * @param json The JSON resource.
   * @param show Whether to make the frame visible.
   * @param reset Whether to reset the view.
   * @throws IOException I/O Exception.
   */
  public static void setupCanvas(final JFrame frame, final JSONManager mng,
      final Resource json, final boolean show, final boolean reset) throws IOException {
    mng.addRawId("frame", frame);
    final Canvas canvas = JSONReader.loadCanvas(new JSONReader(json.reader()).get(), mng);
    // pack and show window
    frame.add(canvas);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    if(show) {
      frame.setVisible(true);
    }
    if(reset) {
      canvas.scheduleAction(new AnimationAction() {

        @Override
        public void animationFinished() {
          canvas.reset();
        }

      }, 0);
    }
  }

}
