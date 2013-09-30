package jkanvas;

import java.awt.geom.Rectangle2D;

public interface RestrictedArea {

  Rectangle2D getTopLevelBounds();

  void beforeEntering(Canvas canvas);

  void beforeLeaving(Canvas canvas);

}
