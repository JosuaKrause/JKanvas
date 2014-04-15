package jkanvas.optional;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.JComponent;

import jkanvas.util.Screenshot;
import jkanvas.util.ScreenshotAlgorithm;

/**
 * Provides a method to create PDF screenshots. This class depends on <a
 * href="http://itextpdf.com/">iText<sup>&#174;</sup></a> which must be included
 * in the class-path. With Maven you can use the following dependency:
 *
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;com.itextpdf&lt;/groupId&gt;
 *   &lt;artifactId&gt;itextpdf&lt;/artifactId&gt;
 *   &lt;version&gt;5.4.4&lt;/version&gt;
 *   &lt;type&gt;jar&lt;/type&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public final class PDFScreenshot implements ScreenshotAlgorithm {

  /** Rectangle constructor. */
  private final Constructor<?> rectNew;
  /** Document constructor. */
  private final Constructor<?> docNew;
  /** Document open. */
  private final Method docOpen;
  /** Document close. */
  private final Method docClose;
  /** Static PDFWriter factory method. */
  private final Method writerInstance;
  /** PDFWriter byte content method. */
  private final Method writerContent;
  /** PDFWriter close. */
  private final Method writerClose;
  /** PDFGraphics2D constructor. */
  private final Constructor<Graphics2D> pdfGfxNew;

  /**
   * Creates the PDF screenshot instance. All necessary classes and methods are
   * loaded here.
   *
   * @throws ClassNotFoundException When a class could not be found.
   * @throws NoSuchMethodException When a method does not exist.
   * @throws SecurityException Security exception.
   */
  private PDFScreenshot()
      throws ClassNotFoundException, NoSuchMethodException, SecurityException {
    final Class<?> rect = Class.forName("com.itextpdf.text.Rectangle");
    rectNew = rect.getConstructor(Float.TYPE, Float.TYPE);
    final Class<?> document = Class.forName("com.itextpdf.text.Document");
    docNew = document.getConstructor(rect);
    docOpen = document.getMethod("open");
    docClose = document.getMethod("close");
    final Class<?> writer = Class.forName("com.itextpdf.text.pdf.PdfWriter");
    writerInstance = writer.getMethod("getInstance", document, OutputStream.class);
    writerContent = writer.getMethod("getDirectContent");
    writerClose = writer.getMethod("close");
    final Class<?> cb = Class.forName("com.itextpdf.text.pdf.PdfContentByte");
    @SuppressWarnings("unchecked")
    final Class<Graphics2D> pdfGfx = (Class<Graphics2D>) Class.forName("com.itextpdf.awt.PdfGraphics2D");
    pdfGfxNew = pdfGfx.getConstructor(cb, Float.TYPE, Float.TYPE);
  }

  @Override
  public void save(
      final OutputStream out, final JComponent comp, final Rectangle size)
      throws IOException {
    final float w = size.width;
    final float h = size.height;
    try {
      final Object pdfDoc = docNew.newInstance(rectNew.newInstance(w, h));
      final Object writer = writerInstance.invoke(null, pdfDoc, out);
      docOpen.invoke(pdfDoc);
      final Object cb = writerContent.invoke(writer);
      final Graphics2D gfx = pdfGfxNew.newInstance(cb, w, h);
      gfx.setStroke(new BasicStroke(1));
      gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
      gfx.setFont(Font.decode(Font.DIALOG));
      final Graphics2D g = new FlatGraphics2D(gfx);
      g.setColor(Color.WHITE);
      g.drawString("Josua Krause", 15, (int) (h) - 15);
      g.setColor(Color.BLACK);
      Screenshot.paint(comp, g);
      g.dispose();
      gfx.dispose();
      docClose.invoke(pdfDoc);
      writerClose.invoke(writer);
    } catch(final Exception e) {
      throw new IOException(e);
    } finally {
      out.close();
    }
  }

  @Override
  public String extension() {
    return "pdf";
  }

  /** The PDF screenshot instance after initialization. */
  private static PDFScreenshot INSTANCE;
  /** Any exception during class loading. */
  private static Exception EXCEPTION;

  /** Loads the PDF library. */
  private static void loadITextPdf() {
    if(INSTANCE != null || EXCEPTION != null) return;
    EXCEPTION = null;
    try {
      INSTANCE = new PDFScreenshot();
    } catch(final ClassNotFoundException | NoSuchMethodException | SecurityException e) {
      EXCEPTION = e;
    }
  }

  /**
   * Getter.
   *
   * @return Whether this class can be used.
   */
  public static boolean hasITextPdf() {
    loadITextPdf();
    return INSTANCE != null;
  }

  /** Throws an exception when the library could not be loaded. */
  public static void expectITextPdf() {
    loadITextPdf();
    if(INSTANCE != null) return;
    if(EXCEPTION == null) throw new IllegalStateException("could not load iText");
    throw new IllegalStateException("error loading iText", EXCEPTION);
  }

  /**
   * Getter.
   *
   * @return The PDF screenshot algorithm instance if the library could be
   *         loaded.
   */
  public static PDFScreenshot getInstance() {
    expectITextPdf();
    return INSTANCE;
  }

}
