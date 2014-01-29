package jkanvas.io.json;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;

import jkanvas.Canvas;
import jkanvas.painter.SimpleTextHUD;

/**
 * Interprets key bindings from a JSON element.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class JSONKeyBindings {

  /** No instances. */
  private JSONKeyBindings() {
    throw new AssertionError();
  }

  /**
   * Loads key bindings from a JSON element. The key bindings must be in the
   * array "keys" and are objects with a "key" string, a "msg" string, and
   * optionally an "help" string.
   * 
   * @param json The JSON element.
   * @param c The canvas.
   * @param info The optional info HUD. May be <code>null</code>.
   */
  public static void load(final JSONElement json, final Canvas c, final SimpleTextHUD info) {
    if(!json.hasValue("keys")) return;
    final JSONElement arr = json.getValue("keys");
    arr.expectArray();
    for(final JSONElement el : arr) {
      el.expectObject();
      final String k = Objects.requireNonNull(el.getString("key", null));
      final String k_big = k.toUpperCase(Locale.ENGLISH);
      final String msg = Objects.requireNonNull(el.getString("msg", null));
      final String key = "VK_" + k_big;
      try {
        final Field f = KeyEvent.class.getDeclaredField(key);
        final int vk = f.getInt(null);
        c.addMessageAction(vk, msg);
        if(info != null && el.hasValue("help")) {
          info.addLine(k_big + ": " + el.getString("help", msg));
        }
      } catch(NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

}
