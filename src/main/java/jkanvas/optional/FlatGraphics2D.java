package jkanvas.optional;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.Objects;

/**
 * Keeps the state of a graphics context that does not keep it by itself. This
 * makes drawing very slow. Be sure to use it only for screenshots. Accessing
 * after disposing results in undefined behavior.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class FlatGraphics2D extends Graphics2D {

  /**
   * Contains the current active {@link FlatGraphics2D} painting on the
   * {@link Graphics2D}.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class GraphicsPod {

    /** The graphics context. */
    private final Graphics2D g;
    /** The current active flat graphics context. */
    private FlatGraphics2D cur;

    /**
     * Creates a new pod.
     * 
     * @param g The graphics context.
     */
    public GraphicsPod(final Graphics2D g) {
      this.g = Objects.requireNonNull(g);
    }

    /**
     * Initializes the pod.
     * 
     * @param fg The first flat graphics context.
     */
    public void init(final FlatGraphics2D fg) {
      fg.store(g);
      cur = fg;
    }

    /**
     * Getter.
     * 
     * @param fg The flat graphics context.
     * @return Prepares the graphics context to be used. This method may return
     *         <code>null</code> for already disposed graphic contexts.
     */
    public Graphics2D get(final FlatGraphics2D fg) {
      if(cur != fg) {
        if(!cur.isDisposed()) {
          cur.store(g);
        }
        cur = fg;
        if(fg.isDisposed()) return null;
        fg.restore(g);
      }
      return g;
    }

    /**
     * Getter.
     * 
     * @param fg The flat graphics context.
     * @return Prepares the graphics context to be used. This method always
     *         returns the context. If the flat graphic context is already
     *         disposed the state of the returned graphics context is undefined.
     */
    public Graphics2D expect(final FlatGraphics2D fg) {
      get(fg);
      return g;
    }

  } // GraphicsPod

  /** The pod. */
  private final GraphicsPod pod;
  /** The rendering hints. */
  private RenderingHints renderingHints;
  /** The transformation. */
  private AffineTransform transform;
  /** The paint. */
  private Paint paint;
  /** The composite. */
  private Composite composite;
  /** The background. */
  private Color background;
  /** The stroke. */
  private Stroke stroke;
  /** The color. */
  private Color color;
  /** The font. */
  private Font font;
  /** The clip. */
  private Shape clip;

  /**
   * Creates a flat graphics object.
   * 
   * @param g The graphics object whose {@link #create()} function will never be
   *          called.
   */
  public FlatGraphics2D(final Graphics2D g) {
    this(new GraphicsPod(g));
  }

  /**
   * Creates a new flat graphics context.
   * 
   * @param pod The pod.
   */
  private FlatGraphics2D(final GraphicsPod pod) {
    this.pod = Objects.requireNonNull(pod);
    pod.init(this);
  }

  /**
   * Stores the current state of the graphics context in this flat graphic
   * context.
   * 
   * @param g The graphic context to store.
   */
  void store(final Graphics2D g) {
    renderingHints = (RenderingHints) g.getRenderingHints().clone();
    color = Objects.requireNonNull(g.getColor());
    background = Objects.requireNonNull(g.getBackground());
    paint = Objects.requireNonNull(g.getPaint());
    transform = new AffineTransform(Objects.requireNonNull(g.getTransform()));
    composite = Objects.requireNonNull(g.getComposite());
    stroke = Objects.requireNonNull(g.getStroke());
    font = Objects.requireNonNull(g.getFont());
    clip = g.getClip();
  }

  /**
   * Sets the current state of the graphics context from this flat graphics
   * context.
   * 
   * @param g The graphic context to set.
   */
  void restore(final Graphics2D g) {
    g.setRenderingHints(renderingHints);
    g.setColor(color);
    g.setBackground(background);
    g.setPaint(paint);
    g.setTransform(transform);
    g.setComposite(composite);
    g.setStroke(stroke);
    g.setClip(clip);
    g.setFont(font);
  }

  @Override
  public Graphics create() {
    return new FlatGraphics2D(pod);
  }

  /**
   * Getter.
   * 
   * @return Whether this context is already disposed.
   */
  public boolean isDisposed() {
    return color == null;
  }

  @Override
  public void dispose() {
    renderingHints = null;
    color = null;
    background = null;
    paint = null;
    transform = null;
    composite = null;
    stroke = null;
    font = null;
    clip = null;
  }

  @Override
  public GraphicsConfiguration getDeviceConfiguration() {
    return pod.expect(this).getDeviceConfiguration();
  }

  @Override
  public Object getRenderingHint(final Key hintKey) {
    return pod.expect(this).getRenderingHint(hintKey);
  }

  @Override
  public RenderingHints getRenderingHints() {
    return pod.expect(this).getRenderingHints();
  }

  @Override
  public AffineTransform getTransform() {
    return pod.expect(this).getTransform();
  }

  @Override
  public Paint getPaint() {
    return pod.expect(this).getPaint();
  }

  @Override
  public Composite getComposite() {
    return pod.expect(this).getComposite();
  }

  @Override
  public Color getBackground() {
    return pod.expect(this).getBackground();
  }

  @Override
  public Stroke getStroke() {
    return pod.expect(this).getStroke();
  }

  @Override
  public FontRenderContext getFontRenderContext() {
    return pod.expect(this).getFontRenderContext();
  }

  @Override
  public Color getColor() {
    return pod.expect(this).getColor();
  }

  @Override
  public Font getFont() {
    return pod.expect(this).getFont();
  }

  @Override
  public FontMetrics getFontMetrics(final Font f) {
    return pod.expect(this).getFontMetrics(f);
  }

  @Override
  public Rectangle getClipBounds() {
    return pod.expect(this).getClipBounds();
  }

  @Override
  public Shape getClip() {
    return pod.expect(this).getClip();
  }

  @Override
  public void draw(final Shape s) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    // ensure only non-NaN values
    final double[] vals = new double[6];
    final PathIterator iter = s.getPathIterator(g.getTransform());
    while(!iter.isDone()) {
      final int segType = iter.currentSegment(vals);
      iter.next();
      final int num;
      switch(segType) {
        case PathIterator.SEG_MOVETO:
        case PathIterator.SEG_LINETO:
          num = 2;
          break;
        case PathIterator.SEG_CLOSE:
          continue;
        case PathIterator.SEG_CUBICTO:
          num = 6;
          break;
        case PathIterator.SEG_QUADTO:
          num = 4;
          break;
        default:
          // bad shape
          return;
      }
      for(int i = 0; i < num; ++i) {
        // check for bad value
        if(Double.isNaN(vals[i])) return;
      }
    }
    g.fill(g.getStroke().createStrokedShape(s));
  }

  @Override
  public boolean drawImage(
      final Image img, final AffineTransform xform, final ImageObserver obs) {
    final Graphics2D g = pod.get(this);
    if(g == null) return true;
    return g.drawImage(img, xform, obs);
  }

  @Override
  public void drawImage(final BufferedImage img, final BufferedImageOp op,
      final int x, final int y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawImage(img, op, x, y);
  }

  @Override
  public void drawRenderedImage(final RenderedImage img, final AffineTransform xform) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawRenderedImage(img, xform);
  }

  @Override
  public void drawRenderableImage(final RenderableImage img, final AffineTransform xform) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawRenderableImage(img, xform);
  }

  @Override
  public void drawString(final String str, final int x, final int y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawString(str, x, y);
  }

  @Override
  public void drawString(final String str, final float x, final float y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawString(str, x, y);
  }

  @Override
  public void drawString(final AttributedCharacterIterator iterator,
      final int x, final int y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawString(iterator, x, y);
  }

  @Override
  public void drawString(final AttributedCharacterIterator iterator,
      final float x, final float y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawString(iterator, x, y);
  }

  @Override
  public void drawGlyphVector(final GlyphVector gv, final float x, final float y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawGlyphVector(gv, x, y);
  }

  @Override
  public void fill(final Shape s) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.fill(s);
  }

  @Override
  public boolean hit(final Rectangle rect, final Shape s, final boolean onStroke) {
    final Graphics2D g = pod.get(this);
    if(g == null) return false;
    return g.hit(rect, s, onStroke);
  }

  @Override
  public void setComposite(final Composite comp) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setComposite(comp);
  }

  @Override
  public void setPaint(final Paint paint) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setPaint(paint);
  }

  @Override
  public void setStroke(final Stroke s) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setStroke(s);
  }

  @Override
  public void setRenderingHint(final Key hintKey, final Object hintValue) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setRenderingHint(hintKey, hintValue);
  }

  @Override
  public void setRenderingHints(final Map<?, ?> hints) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setRenderingHints(hints);
  }

  @Override
  public void addRenderingHints(final Map<?, ?> hints) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.addRenderingHints(hints);
  }

  @Override
  public void translate(final int x, final int y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.translate(x, y);
  }

  @Override
  public void translate(final double tx, final double ty) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.translate(tx, ty);
  }

  @Override
  public void rotate(final double theta) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.rotate(theta);
  }

  @Override
  public void rotate(final double theta, final double x, final double y) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.rotate(theta, x, y);
  }

  @Override
  public void scale(final double sx, final double sy) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.scale(sx, sy);
  }

  @Override
  public void shear(final double shx, final double shy) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.shear(shx, shy);
  }

  @Override
  public void transform(final AffineTransform Tx) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.transform(Tx);
  }

  @Override
  public void setTransform(final AffineTransform Tx) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setTransform(Tx);
  }

  @Override
  public void setBackground(final Color color) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setBackground(color);
  }

  @Override
  public void clip(final Shape s) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.clip(s);
  }

  @Override
  public void setColor(final Color c) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setColor(c);
  }

  @Override
  public void setPaintMode() {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setPaintMode();
  }

  @Override
  public void setXORMode(final Color c1) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setXORMode(c1);
  }

  @Override
  public void setFont(final Font font) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setFont(font);
  }

  @Override
  public void clipRect(final int x, final int y, final int width, final int height) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.clipRect(x, y, width, height);
  }

  @Override
  public void setClip(final int x, final int y, final int width, final int height) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setClip(x, y, width, height);
  }

  @Override
  public void setClip(final Shape clip) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.setClip(clip);
  }

  @Override
  public void copyArea(final int x, final int y, final int width, final int height,
      final int dx, final int dy) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.copyArea(x, y, width, height, dx, dy);
  }

  @Override
  public void drawLine(final int x1, final int y1, final int x2, final int y2) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawLine(x1, y1, x2, y2);
  }

  @Override
  public void fillRect(final int x, final int y, final int width, final int height) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.fillRect(x, y, width, height);
  }

  @Override
  public void clearRect(final int x, final int y, final int width, final int height) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.clearRect(x, y, width, height);
  }

  @Override
  public void drawRoundRect(final int x, final int y, final int width, final int height,
      final int arcWidth, final int arcHeight) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
  }

  @Override
  public void fillRoundRect(final int x, final int y, final int width, final int height,
      final int arcWidth, final int arcHeight) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
  }

  @Override
  public void drawOval(final int x, final int y, final int width, final int height) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawOval(x, y, width, height);
  }

  @Override
  public void fillOval(final int x, final int y, final int width, final int height) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.fillOval(x, y, width, height);
  }

  @Override
  public void drawArc(final int x, final int y, final int width, final int height,
      final int startAngle, final int arcAngle) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawArc(x, y, width, height, startAngle, arcAngle);
  }

  @Override
  public void fillArc(final int x, final int y, final int width, final int height,
      final int startAngle, final int arcAngle) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.fillArc(x, y, width, height, startAngle, arcAngle);
  }

  @Override
  public void drawPolyline(final int[] xPoints, final int[] yPoints, final int nPoints) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawPolyline(xPoints, yPoints, nPoints);
  }

  @Override
  public void drawPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.drawPolygon(xPoints, yPoints, nPoints);
  }

  @Override
  public void fillPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
    final Graphics2D g = pod.get(this);
    if(g == null) return;
    g.fillPolygon(xPoints, yPoints, nPoints);
  }

  @Override
  public boolean drawImage(final Image img, final int x, final int y,
      final ImageObserver observer) {
    final Graphics2D g = pod.get(this);
    if(g == null) return true;
    return g.drawImage(img, x, y, observer);
  }

  @Override
  public boolean drawImage(final Image img, final int x, final int y,
      final int width, final int height, final ImageObserver observer) {
    final Graphics2D g = pod.get(this);
    if(g == null) return true;
    return g.drawImage(img, x, y, width, height, observer);
  }

  @Override
  public boolean drawImage(final Image img, final int x, final int y,
      final Color bgcolor, final ImageObserver observer) {
    final Graphics2D g = pod.get(this);
    if(g == null) return true;
    return g.drawImage(img, x, y, bgcolor, observer);
  }

  @Override
  public boolean drawImage(final Image img, final int x, final int y, final int width,
      final int height, final Color bgcolor, final ImageObserver observer) {
    final Graphics2D g = pod.get(this);
    if(g == null) return true;
    return g.drawImage(img, x, y, width, height, bgcolor, observer);
  }

  @Override
  public boolean drawImage(final Image img, final int dx1, final int dy1,
      final int dx2, final int dy2, final int sx1, final int sy1,
      final int sx2, final int sy2, final ImageObserver observer) {
    final Graphics2D g = pod.get(this);
    if(g == null) return true;
    return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
  }

  @Override
  public boolean drawImage(final Image img, final int dx1, final int dy1,
      final int dx2, final int dy2, final int sx1, final int sy1,
      final int sx2, final int sy2, final Color bgcolor, final ImageObserver observer) {
    final Graphics2D g = pod.get(this);
    if(g == null) return true;
    return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
  }

}
