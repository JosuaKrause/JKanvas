package jkanvas.painter.groups;

import java.util.List;

import jkanvas.painter.Renderpass;
import jkanvas.painter.groups.RenderGroup.RenderpassPosition;

public interface RenderpassLayout<T extends Renderpass> {

  void doLayout(List<RenderpassPosition<T>> members);

}
