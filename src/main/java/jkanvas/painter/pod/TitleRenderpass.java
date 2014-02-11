package jkanvas.painter.pod;

import java.util.Objects;

import jkanvas.painter.Renderpass;

/**
 * Adds a title to the given render pass.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The innermost wrapped type.
 */
public class TitleRenderpass<T extends Renderpass> extends AbstractTitleRenderpass<T> {

  /** An array for initializing with no title. */
  private static final String[] NO_TITLE = { ""};

  /** The space between title texts. */
  private double titleSpace;
  /** The title texts. */
  private String[] titles;

  /**
   * Creates an empty title at the top for the given render pass.
   * 
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   */
  public TitleRenderpass(final T pass, final double textHeight, final double space) {
    this(pass, textHeight, space, NO_TITLE);
  }

  /**
   * Creates a title at the top for the given render pass.
   * 
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   * @param titles The initial titles.
   */
  public TitleRenderpass(final T pass, final double textHeight,
      final double space, final String... titles) {
    super(pass, textHeight, space);
    if(titles.length == 0) throw new IllegalArgumentException("empty titles");
    for(final String s : titles) {
      Objects.requireNonNull(s);
    }
    this.titles = titles.clone();
    titleSpace = 0;
    setChildOffset(0, space + textHeight);
  }

  /**
   * Creates an empty title at the top for the given render pass.
   * 
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   */
  public TitleRenderpass(
      final Renderpod<T> pass, final double textHeight, final double space) {
    this(pass, textHeight, space, NO_TITLE);
  }

  /**
   * Creates a title at the top for the given render pass.
   * 
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   * @param titles The initial titles.
   */
  public TitleRenderpass(final Renderpod<T> pass, final double textHeight,
      final double space, final String... titles) {
    super(pass, textHeight, space);
    if(titles.length == 0) throw new IllegalArgumentException("empty titles");
    for(final String s : titles) {
      Objects.requireNonNull(s);
    }
    this.titles = titles.clone();
    titleSpace = 0;
    setChildOffset(0, space + textHeight);
  }

  /**
   * Setter.
   * 
   * @param title The title.
   */
  public void setTitle(final String title) {
    titles = new String[] { Objects.requireNonNull(title)};
  }

  /**
   * Setter.
   * 
   * @param titles The titles.
   */
  public void setTitles(final String... titles) {
    Objects.requireNonNull(titles);
    if(titles.length == 0) throw new IllegalArgumentException("empty titles");
    for(final String s : titles) {
      Objects.requireNonNull(s);
    }
    this.titles = titles.clone();
  }

  @Override
  public String getTitle(final int num) {
    return titles[num];
  }

  /**
   * Getter.
   * 
   * @return All titles.
   */
  public String[] getTitles() {
    return titles.clone();
  }

  @Override
  public int getTitleCount() {
    return titles.length;
  }

  /**
   * Setter.
   * 
   * @param titleSpace The space between titles.
   */
  public void setTitleSpace(final double titleSpace) {
    this.titleSpace = titleSpace;
  }

  /**
   * Getter.
   * 
   * @return The space between titles.
   */
  public double getTitleSpace() {
    return titleSpace;
  }

  @Override
  protected double getTitleSpace(final int i) {
    return titleSpace;
  }

  @Override
  protected double getWidth(final double totalWidth, final int index) {
    return (totalWidth - titleSpace * (titles.length - 1)) / titles.length;
  }

}
