package jkanvas.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * Saves screenshots.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public final class Screenshot {

  /** The scaling factor for raster screenshots. */
  public static int SCALE = 3;

  /** No constructor. */
  private Screenshot() {
    throw new AssertionError();
  }

  /**
   * Ensures that a directory exists.
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
   * @param postfix The name postfix. The dot before the extension is needed.
   * @return The file that does not yet exist.
   */
  public static File findFile(final File dir, final String prefix, final String postfix) {
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
   * Saves a screenshot.
   *
   * @param dir The directory.
   * @param prefix The name prefix.
   * @param comp The component to draw.
   * @return The file that was used to store the image.
   * @throws IOException I/O Exception.
   */
  public static File save(
      final File dir, final String prefix, final JComponent comp) throws IOException {
    return save(dir, prefix, "." + ALGO.extension(), comp);
  }

  /**
   * Saves a screenshot.
   *
   * @param dir The directory.
   * @param prefix The name prefix.
   * @param postfix The name extension.
   * @param comp The component to draw.
   * @return The file that was used to store the image.
   * @throws IOException I/O Exception.
   */
  public static File save(final File dir, final String prefix,
      final String postfix, final JComponent comp) throws IOException {
    final File out = findFile(dir, prefix, postfix);
    try (OutputStream os = new FileOutputStream(out)) {
      save(os, comp);
    }
    return out;
  }

  /** Saves screenshots as PNG. This is the default screenshot algorithm. */
  public static final ScreenshotAlgorithm PNG = new ScreenshotAlgorithm() {

    @Override
    public void save(final OutputStream out, final JComponent comp, final Rectangle rect)
        throws IOException {
      final BufferedImage img = new BufferedImage(
          rect.width * SCALE, rect.height * SCALE, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g = img.createGraphics();
      g.scale(SCALE, SCALE);
      paint(comp, g);
      g.dispose();
      ImageIO.write(img, "png", out);
      out.close();
    }

    @Override
    public String extension() {
      return "png";
    }

  };

  /** The current screenshot algorithm. */
  private static ScreenshotAlgorithm ALGO = PNG;

  /**
   * Saves a screenshot.
   *
   * @param out The output.
   * @param comp The component.
   * @throws IOException I/O Exception.
   */
  public static void save(
      final OutputStream out, final JComponent comp) throws IOException {
    final Rectangle rect = outputSize(comp);
    ALGO.save(out, comp, rect);
  }

  /**
   * Setter.
   *
   * @param algo The screenshot algorithm to use.
   */
  public static void setAlgorithm(final ScreenshotAlgorithm algo) {
    ALGO = Objects.requireNonNull(algo);
  }

  /**
   * Getter.
   *
   * @return The currently used screenshot algorithm.
   */
  public static ScreenshotAlgorithm getAlgorithm() {
    return ALGO;
  }

  /**
   * Getter.
   *
   * @param comp The component to take a screenshot of.
   * @return The size of the screenshot.
   */
  public static Rectangle outputSize(final JComponent comp) {
    return comp.getVisibleRect();
  }

  /**
   * Paints a screenshot of the given component to the graphics context.
   *
   * @param comp The component.
   * @param g The graphics context.
   */
  public static void paint(final JComponent comp, final Graphics2D g) {
    final boolean isCacheDisabled = jkanvas.Canvas.DISABLE_CACHING;
    jkanvas.Canvas.DISABLE_CACHING = true;
    try {
      comp.paintAll(g);
    } finally {
      jkanvas.Canvas.DISABLE_CACHING = isCacheDisabled;
    }
  }

  /**
   * Pads the given positive number with zeros.
   *
   * @param number The number to pad.
   * @param figures The number of figures allowed.
   * @return The padded number.
   */
  public static String padNumber(final long number, final int figures) {
    if(number < 0) throw new IllegalArgumentException("number is negative: " + number);
    final String num = "" + number;
    final int len = num.length();
    if(len > figures) throw new IllegalArgumentException(
        "number " + number + " has too many figures -- expected " + figures);
    final char[] digits = new char[figures];
    Arrays.fill(digits, '0');
    for(int pos = 0; pos < figures; ++pos) {
      final int cur = len - pos - 1;
      if(cur < 0) {
        break;
      }
      digits[figures - pos - 1] = num.charAt(cur);
    }
    return String.valueOf(digits);
  }

}
