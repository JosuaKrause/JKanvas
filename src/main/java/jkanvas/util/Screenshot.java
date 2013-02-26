package jkanvas.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * Saves screen-shots.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class Screenshot {

  /** The scaling factor for raster screen-shots. */
  private static final int SCALE = 3;

  /** No constructor. */
  private Screenshot() {
    throw new AssertionError();
  }

  /**
   * Ensures a directory.
   * 
   * @param dir The directory.
   */
  private static void ensureDir(final File dir) {
    if(dir == null) return;
    if(dir.isFile()) return;
    if(dir.exists()) return;
    ensureDir(dir.getParentFile());
    dir.mkdir();
  }

  /**
   * Finds an unused file in the given directory.
   * 
   * @param dir The directory.
   * @param prefix The name prefix.
   * @param postfix The name postfix.
   * @return The file that does not yet exist.
   */
  private static File nextFile(final File dir, final String prefix, final String postfix) {
    ensureDir(dir);
    final File out;
    int i = 0;
    for(;;) {
      final File f = new File(dir, prefix + (i++) + postfix);
      if(!f.exists()) {
        out = f;
        break;
      }
    }
    return out;
  }

  /**
   * Saves a screen-shot as PNG.
   * 
   * @param dir The directory.
   * @param prefix The name prefix.
   * @param comp The component to draw.
   * @return The file that was used to store the image.
   * @throws IOException I/O Exception.
   */
  public static File savePNG(final File dir, final String prefix, final JComponent comp)
      throws IOException {
    final File out = nextFile(dir, prefix, ".png");
    savePNG(new FileOutputStream(out), comp);
    return out;
  }

  /**
   * Saves a screen-shot as PNG.
   * 
   * @param out The output.
   * @param comp The component.
   * @throws IOException I/O Exception.
   */
  public static void savePNG(final OutputStream out, final JComponent comp)
      throws IOException {
    final Rectangle rect = comp.getVisibleRect();
    final BufferedImage img = new BufferedImage(
        rect.width * SCALE, rect.height * SCALE, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = img.createGraphics();
    g.scale(SCALE, SCALE);
    comp.paintAll(g);
    g.dispose();
    ImageIO.write(img, "png", out);
    out.close();
  }

}
