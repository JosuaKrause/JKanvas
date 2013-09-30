package jkanvas.present;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.RestrictedArea;
import jkanvas.ViewConfiguration;
import jkanvas.painter.RenderpassPainter;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;

public class PortalRender extends SlideObject implements RestrictedArea {

  private final ViewConfiguration main;

  private final ViewConfiguration cfg;

  private final int width;

  private final int height;

  private double top;

  private double left;

  private Image cache;

  public PortalRender(final Slide slide, final ViewConfiguration main,
      final ViewConfiguration cfg, final HorizontalSlideAlignment hAlign,
      final VerticalSlideAlignment vAlign, final int width, final int height) {
    super(slide, hAlign, vAlign);
    this.main = main;
    this.cfg = cfg;
    this.width = width;
    this.height = height;
    top = Double.NaN;
    left = Double.NaN;
    cache = null;
  }

  public void invalidateCache() {
    if(cache != null) {
      cache.flush();
    }
    cache = null;
  }

  @Override
  public void beforeEntering(final Canvas canvas) {
    // canvas.setViewConfiguration(cfg);
  }

  @Override
  public void beforeLeaving(final Canvas canvas) {
    invalidateCache();
    canvas.setViewConfiguration(main);
  }

  @Override
  public Rectangle2D getTopLevelBounds() {
    final Rectangle2D box = getBoundingBox();
    return RenderpassPainter.getTopLevelBounds(getSlide(),
        new Rectangle2D.Double(box.getX() + getLeft(), box.getY() + getTop(),
            box.getWidth(), box.getHeight()));
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    if(cache == null) {
      final int cw = width * 4;
      final int ch = height * 4;
      final BufferedImage img = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g2 = (Graphics2D) img.getGraphics();
      cfg.paint(g2, cw, ch);
      g2.dispose();
      cache = img;
    }
    g.drawImage(cache, 0, 0, width, height, null);
    g.setColor(Color.BLACK);
    g.draw(new Rectangle2D.Double(0, 0, width, height));
  }

  @Override
  public void beforeDraw(final Graphics2D gfx, final SlideMetrics metric) {
    if(Double.isNaN(top)) {
      final Slide s = getSlide();
      final VerticalSlideAlignment vAlign = getVerticalAlignment();
      top = s.getTotalHeight(vAlign);
      s.addHeight(getHeight(), vAlign);
    }
    if(Double.isNaN(left)) {
      left = getOffset(metric).getX();
    }
  }

  @Override
  public double getTop() throws IllegalStateException {
    if(Double.isNaN(top)) throw new IllegalStateException("top not initialized");
    return top;
  }

  public double getLeft() {
    if(Double.isNaN(left)) throw new IllegalStateException("left not initialized");
    return left;
  }

  @Override
  public double getWidth() throws IllegalStateException {
    return width;
  }

  @Override
  public double getHeight() throws IllegalStateException {
    return height;
  }

  @Override
  public int getIndent() {
    return 0;
  }

}
