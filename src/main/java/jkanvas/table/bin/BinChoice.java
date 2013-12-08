package jkanvas.table.bin;

import jkanvas.table.Feature;

/**
 * Determines the number of bins for an equal width histogram.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface BinChoice {

  /**
   * Determines the number of bins for an equal width histogram.
   * 
   * @param feature The feature.
   * @return The number of bins.
   */
  int numberOfBins(Feature feature);

  /** The square root choice. Used for example by Excel. */
  BinChoice SQRT = new BinChoice() {

    @Override
    public int numberOfBins(final Feature feature) {
      return (int) Math.ceil(Math.sqrt(feature.rows()));
    }

  };

  /** Sturge's rule assumes a normal distribution. */
  BinChoice STURGES = new BinChoice() {

    @Override
    public int numberOfBins(final Feature feature) {
      return (int) Math.ceil(Math.log(feature.rows()) + 1);
    }

  };

  /** A simple alternative to {@link #STURGES Sturge's} rule. */
  BinChoice RICE = new BinChoice() {

    @Override
    public int numberOfBins(final Feature feature) {
      return (int) Math.ceil(2 * Math.cbrt(feature.rows()));
    }

  };

}
