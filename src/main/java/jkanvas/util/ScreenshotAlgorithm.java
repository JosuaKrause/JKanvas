package jkanvas.util;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JComponent;

/**
 * Defines an algorithm for taking screenshots. The actual drawing should be
 * done via {@link Screenshot#paint(JComponent, java.awt.Graphics2D)}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface ScreenshotAlgorithm {

  /**
   * Saves a screenshot.
   * 
   * @param out The output.
   * @param comp The component.
   * @param size The size needed for the screenshot.
   * @throws IOException I/O Exception.
   */
  void save(OutputStream out, JComponent comp, Rectangle size) throws IOException;

  /**
   * Getter.
   * 
   * @return The file extension for this screenshot type. The extension must not
   *         include the dot.
   */
  String extension();

}
