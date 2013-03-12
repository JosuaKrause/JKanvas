package jkanvas;

import java.awt.geom.Rectangle2D;

import jkanvas.animation.AnimationTiming;

public interface Camera {

  void toView(Rectangle2D rect, AnimationTiming timing);

  Rectangle2D getView();

  Rectangle2D getPredictView();

}
