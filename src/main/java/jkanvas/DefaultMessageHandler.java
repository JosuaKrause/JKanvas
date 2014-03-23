package jkanvas;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.swing.JFrame;

import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.Animator;
import jkanvas.optional.PDFScreenshot;
import jkanvas.painter.FrameRateHUD;
import jkanvas.painter.HUDRenderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.TextHUD;
import jkanvas.util.Screenshot;
import jkanvas.util.ScreenshotAlgorithm;

/**
 * Processes some messages for a canvas.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DefaultMessageHandler implements CanvasMessageHandler {

  /** The canvas id. */
  public static final String CANVAS_ID = "canvas";

  /** The frame or <code>null</code>. */
  private final JFrame frame;

  /**
   * Creates a default message handler.
   * 
   * @param frame The frame or <code>null</code>.
   */
  public DefaultMessageHandler(final JFrame frame) {
    this.frame = frame;
    ids = " " + CANVAS_ID + " ";
  }

  /** The inactive frame rate displayer. */
  private FrameRateDisplayer frdTmp;

  /**
   * Toggle the frame rate displayer.
   * 
   * @param canvas The canvas.
   */
  protected void toggleShowFps(final Canvas canvas) {
    final FrameRateDisplayer cur = canvas.getFrameRateDisplayer();
    if(cur == null && frdTmp == null) {
      frdTmp = createFrameRateDisplayer();
    }
    canvas.setFrameRateDisplayer(frdTmp);
    frdTmp = cur;
  }

  /**
   * Getter.
   * 
   * @return Creates a frame rate displayer.
   */
  protected FrameRateDisplayer createFrameRateDisplayer() {
    return new FrameRateHUD();
  }

  /**
   * Makes a screenshot.
   * 
   * @param canvas The canvas.
   * @param window Whether the complete window should be printed.
   */
  protected void makeScreenshot(final Canvas canvas, final boolean window) {
    try {
      final boolean before;
      final Animator a = canvas.getAnimator();
      final AnimatedPainter ap;
      if(a instanceof AnimatedPainter) {
        ap = (AnimatedPainter) a;
      } else {
        ap = null;
      }
      if(ap != null) {
        before = ap.isStopped();
        ap.setStopped(true);
      } else {
        before = false;
      }
      final File png = Screenshot.save(
          photoFolder(), photoPrefix(),
          (window && frame != null) ? frame.getRootPane() : canvas);
      photoSuccess(png);
      if(PDFScreenshot.hasITextPdf()) {
        final ScreenshotAlgorithm algo = Screenshot.getAlgorithm();
        Screenshot.setAlgorithm(PDFScreenshot.getInstance());
        final File pdf = Screenshot.save(photoFolder(), photoPrefix(), canvas);
        photoSuccess(pdf);
        Screenshot.setAlgorithm(algo);
      }
      if(ap != null) {
        ap.setStopped(before);
      }
    } catch(final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Getter.
   * 
   * @return The folder to save photos.
   */
  protected File photoFolder() {
    return new File("pics");
  }

  /**
   * Getter.
   * 
   * @return The photo prefix.
   */
  protected String photoPrefix() {
    if(frame == null) return "pic";
    final String title = frame.getTitle().trim().replace(' ', '_');
    if(title.isEmpty()) return "pic";
    return title;
  }

  /**
   * Is called when a photo was created.
   * 
   * @param png The photo file.
   */
  protected void photoSuccess(final File png) {
    System.out.println("Saved screenshot in " + png);
  }

  /** The pause HUD. */
  private HUDRenderpass pauseHUD;

  /**
   * Toggles pause.
   * 
   * @param canvas The canvas.
   */
  protected void togglePause(final Canvas canvas) {
    final Animator a = canvas.getAnimator();
    if(!(a instanceof AnimatedPainter)) throw new IllegalStateException(
        "Need animated painter to stop animation!");
    final AnimatedPainter ap = (AnimatedPainter) a;
    if(pauseHUD == null) {
      pauseHUD = createPauseHUD();
      pauseHUD.setVisible(false);
      final KanvasPainter p = canvas.getPainter();
      if(p instanceof RenderpassPainter) {
        ((RenderpassPainter) p).addHUDPass(pauseHUD);
      } else throw new IllegalStateException("Cannot add HUD render pass!");
    }
    final boolean b = !ap.isStopped();
    pauseHUD.setVisible(b);
    ap.setStopped(b);
  }

  /**
   * Getter.
   * 
   * @param canvas The canvas.
   * @return Whether the canvas is paused.
   */
  protected boolean isPaused(final Canvas canvas) {
    final Animator a = canvas.getAnimator();
    if(!(a instanceof AnimatedPainter)) throw new IllegalStateException(
        "Need animated painter to stop animation!");
    final AnimatedPainter ap = (AnimatedPainter) a;
    return ap.isStopped();
  }

  /**
   * Getter.
   * 
   * @return Creates a pause HUD.
   */
  protected HUDRenderpass createPauseHUD() {
    final SimpleTextHUD hud = new SimpleTextHUD(TextHUD.LEFT, TextHUD.TOP);
    hud.addLine("paused");
    return hud;
  }

  @Override
  public void processMessage(final Canvas canvas, final String msg) {
    switch(msg) {
      case "fps:true":
        if(canvas.getFrameRateDisplayer() == null) {
          toggleShowFps(canvas);
        }
        break;
      case "fps:false":
        if(canvas.getFrameRateDisplayer() != null) {
          toggleShowFps(canvas);
        }
        break;
      case "fps:toggle":
        toggleShowFps(canvas);
        break;
      case "photo":
        makeScreenshot(canvas, false);
        break;
      case "photo:window":
        makeScreenshot(canvas, true);
        break;
      case "pause:true":
        if(!isPaused(canvas)) {
          togglePause(canvas);
        }
        break;
      case "pause:false":
        if(isPaused(canvas)) {
          togglePause(canvas);
        }
        break;
      case "pause:toggle":
        togglePause(canvas);
        break;
      case "reset":
        canvas.reset();
        break;
      case "quit":
        canvas.dispose();
        if(frame != null) {
          frame.dispose();
        }
        break;
    }
  }

  /** The ids associated with the canvas. */
  private String ids;

  /**
   * Setter.
   * 
   * @param ids The ids associated with this render pass. Multiple ids may be
   *          separated with space '<code> </code>'.
   */
  public void setIds(final String ids) {
    this.ids = " " + Objects.requireNonNull(ids).trim() + " ";
  }

  @Override
  public String getCanvasIds() {
    return ids;
  }

}
