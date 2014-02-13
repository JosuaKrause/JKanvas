package jkanvas.io.json;

import java.awt.event.WindowAdapter;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.CanvasMessageHandler;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.painter.HUDRenderpass;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.util.CoRoutine;
import jkanvas.util.Resource;

/**
 * Sets up a canvas with a frame from a given JSON resource.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class JSONSetup {

  /** No constructor. */
  private JSONSetup() {
    throw new AssertionError();
  }

  /**
   * Sets up the canvas by creating a frame. The frame is shown directly.
   * 
   * @param name The name of the frame.
   * @param mng The JSON manager.
   * @param json The JSON resource.
   * @param reset Whether to reset the view.
   * @throws IOException I/O Exception.
   */
  public static void setupCanvas(final String name, final JSONManager mng,
      final Resource json, final boolean reset) throws IOException {
    final JFrame frame = new JFrame(name);
    setupCanvas(frame, mng, json, true, reset);
  }

  /**
   * Sets up the canvas.
   * 
   * @param frame The frame.
   * @param mng The JSON manager.
   * @param json The JSON resource.
   * @param show Whether to make the frame visible.
   * @param reset Whether to reset the view.
   * @throws IOException I/O Exception.
   */
  public static void setupCanvas(final JFrame frame, final JSONManager mng,
      final Resource json, final boolean show, final boolean reset) throws IOException {
    mng.addRawId("frame", frame);
    final Canvas canvas = loadCanvas(new JSONReader(json.reader()).get(), mng);
    // pack and show window
    frame.add(canvas);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    final WindowAdapter wnd = Canvas.getWindowAdapter(frame, canvas);
    frame.addWindowListener(wnd);
    frame.addWindowStateListener(wnd);
    if(show) {
      frame.setVisible(true);
    }
    if(reset) {
      // TODO #43 -- Java 8 simplification
      canvas.scheduleAction(new AnimationAction() {

        @Override
        public void animationFinished() {
          canvas.reset();
        }

      }, 0);
    }
  }

  /**
   * Loads a canvas from JSON.
   * 
   * @param el The element.
   * @param m The JSON manager. If <code>null</code> a manager is created.
   * @return The canvas.
   * @throws IOException I/O Exception.
   */
  public static final Canvas loadCanvas(
      final JSONElement el, final JSONManager m) throws IOException {
    el.expectObject();
    final Set<String> fields = new HashSet<>();
    fields.add("keys");
    final JSONManager mng = m != null ? m : new JSONManager();
    fields.add("templates");
    if(el.hasValue("templates")) {
      final JSONElement tmpls = el.getValue("templates");
      addTemplates(mng, tmpls);
    }
    fields.add("import");
    readImports(new HashSet<String>(), el, mng);
    // ### recursive can be used after here ###
    final RenderpassPainter rp;
    final AnimatedPainter ap;
    {
      if(getRecursive(el, "animated", mng, fields, true)) {
        ap = new AnimatedPainter();
        rp = ap;
      } else {
        rp = new RenderpassPainter();
        ap = null;
      }
    }
    mng.addRawId("painter", rp);
    final boolean autoRest;
    final Rectangle2D rest;
    {
      final JSONElement restr = getRecursive(el, "restriction", mng, fields);
      if(restr != null) {
        if(restr.isString()) {
          switch(restr.string()) {
            case "auto":
              rest = new Rectangle2D.Double();
              autoRest = true;
              break;
            case "none":
              rest = null;
              autoRest = false;
              break;
            default:
              throw new IOException(
                  "expect \"auto\", \"none\", or rectangle as restriction");
          }
        } else {
          rest = JSONLoader.getRectFromJSON(restr);
          autoRest = false;
        }
      } else {
        rest = null;
        autoRest = false;
      }
    }
    final int width = getRecursive(el, "width", mng, fields, 800);
    final int height = getRecursive(el, "height", mng, fields, 600);
    final Canvas c = new Canvas(rp, rest != null, width, height);
    mng.addRawId("canvas", c);
    if(ap != null) {
      c.setAnimator(ap);
      ap.addRefreshable(c);
    }
    if(rest != null && !autoRest) {
      c.setRestriction(rest, AnimationTiming.NO_ANIMATION, null);
    }
    final List<JSONThunk> passes = new ArrayList<>();
    {
      for(final JSONElement cnt : getRecursiveArray(el, "content", mng, fields)) {
        cnt.expectObject();
        passes.add(JSONThunk.readJSON(cnt, mng));
      }
    }
    final List<JSONThunk> huds = new ArrayList<>();
    {
      for(final JSONElement cnt : getRecursiveArray(el, "huds", mng, fields)) {
        cnt.expectObject();
        huds.add(JSONThunk.readJSON(cnt, mng));
      }
    }
    final JSONThunk msgHnd;
    {
      final JSONElement h = getRecursive(el, "handler", mng, fields);
      if(h != null) {
        h.expectObject();
        msgHnd = JSONThunk.readJSON(h, mng);
      } else {
        msgHnd = null;
      }
    }
    final JSONThunk help;
    {
      final JSONElement h = getRecursive(el, "help", mng, fields);
      if(h != null) {
        h.expectObject();
        help = JSONThunk.readJSON(h, mng);
      } else {
        help = null;
      }
    }
    {
      for(final JSONElement cnt : getRecursiveArray(el, "objects", mng, fields)) {
        cnt.expectObject();
        final JSONThunk thunk = JSONThunk.readJSON(cnt, mng);
        if(thunk.getId() == null) throw new IOException(
            "all objects in 'objects' must have an id");
      }
    }
    // ### interpret remaining fields ###
    final ObjectCreator oc = new SimpleObjectCreator();
    JSONThunk.addFields(oc, el, mng, fields);
    // ### evaluating ###
    for(final JSONThunk p : passes) {
      rp.addPass(p.get(Renderpass.class));
    }
    for(final JSONThunk p : huds) {
      rp.addHUDPass(p.get(HUDRenderpass.class));
    }
    if(msgHnd != null) {
      c.setMessageHandler(msgHnd.get(CanvasMessageHandler.class));
    }
    final SimpleTextHUD helpHUD;
    if(help != null) {
      helpHUD = help.get(SimpleTextHUD.class);
    } else {
      helpHUD = null;
    }
    oc.callSetters(c);
    // ### messages ###
    {
      JSONElement cur = el;
      while(cur != null) {
        final JSONElement keys = getRecursiveParent(cur, "keys", mng);
        if(keys == null) {
          break;
        }
        JSONKeyBindings.load(keys, c, helpHUD);
        cur = mng.getTemplateOf(keys);
      }
      if(helpHUD != null) {
        rp.addHUDPass(helpHUD);
      }
    }
    // ### auto restriction ###
    if(rest != null && autoRest) {
      rp.getBoundingBox(rest);
      c.setRestriction(rest, AnimationTiming.NO_ANIMATION, null);
    }
    return c;
  }

  /**
   * Get the element that contains the given value obeying templates.
   * 
   * @param el The element.
   * @param name The name of the value.
   * @param mng The manager.
   * @return The element containing the given value or <code>null</code>.
   */
  public static JSONElement getRecursiveParent(
      final JSONElement el, final String name, final JSONManager mng) {
    if(el.hasValue(name)) return el;
    final JSONElement tmpl = mng.getTemplateOf(el);
    if(tmpl == null) return null;
    return getRecursiveParent(tmpl, name, mng);
  }

  /**
   * Get the given value obeying templates.
   * 
   * @param el The element.
   * @param name The name of the value.
   * @param mng The manager.
   * @param fields The used fields set.
   * @return The given value or <code>null</code>.
   */
  public static JSONElement getRecursive(final JSONElement el, final String name,
      final JSONManager mng, final Set<String> fields) {
    fields.add(name);
    final JSONElement p = getRecursiveParent(el, name, mng);
    if(p == null) return null;
    return p.getValue(name);
  }

  /**
   * Get the given value obeying templates.
   * 
   * @param el The element.
   * @param name The name of the value.
   * @param mng The manager.
   * @param fields The used fields set.
   * @param defaultValue The default value if the field does not exist.
   * @return The given value or the default value.
   */
  public static boolean getRecursive(final JSONElement el, final String name,
      final JSONManager mng, final Set<String> fields, final boolean defaultValue) {
    fields.add(name);
    final JSONElement p = getRecursiveParent(el, name, mng);
    if(p == null) return defaultValue;
    return p.getBool(name, defaultValue);
  }

  /**
   * Get the given value obeying templates.
   * 
   * @param el The element.
   * @param name The name of the value.
   * @param mng The manager.
   * @param fields The used fields set.
   * @param defaultValue The default value if the field does not exist.
   * @return The given value or the default value.
   */
  public static int getRecursive(final JSONElement el, final String name,
      final JSONManager mng, final Set<String> fields, final int defaultValue) {
    fields.add(name);
    final JSONElement p = getRecursiveParent(el, name, mng);
    if(p == null) return defaultValue;
    return p.getInt(name, defaultValue);
  }

  /**
   * Get the given value obeying templates.
   * 
   * @param el The element.
   * @param name The name of the value.
   * @param mng The manager.
   * @param fields The used fields set.
   * @param defaultValue The default value if the field does not exist.
   * @return The given value or the default value.
   */
  public static double getRecursive(final JSONElement el, final String name,
      final JSONManager mng, final Set<String> fields, final double defaultValue) {
    fields.add(name);
    final JSONElement p = getRecursiveParent(el, name, mng);
    if(p == null) return defaultValue;
    return p.getDouble(name, defaultValue);
  }

  /**
   * Recursively computes an array.
   * 
   * @param el The root element.
   * @param name The name of the field.
   * @param mng The manager.
   * @param fields The set of used fields.
   * @return The array elements.
   */
  public static Iterable<JSONElement> getRecursiveArray(final JSONElement el,
      final String name, final JSONManager mng, final Set<String> fields) {
    fields.add(name);
    // TODO #43 -- Java 8 simplification
    return new Iterable<JSONElement>() {

      @Override
      public Iterator<JSONElement> iterator() {
        return new CoRoutine<JSONElement>() {

          @Override
          protected void compute() {
            addArray(el);
          }

          public void addArray(final JSONElement el) {
            if(el.hasValue(name)) {
              final JSONElement content = el.getValue(name);
              if(content.isArray()) {
                for(int i = 0; i < content.size(); ++i) {
                  yield(content.getAt(i));
                }
              } else {
                content.expectObject();
                yield(content);
              }
            }
            final JSONElement tmpl = mng.getTemplateOf(el);
            if(tmpl == null) return;
            addArray(tmpl);
          }

        };
      }

    };
  }

  /**
   * Reads imports of an element.
   * 
   * @param alreadyImported The filenames that are already imported.
   * @param el The element that has the import field.
   * @param mng The JSON manager.
   * @throws IOException I/O Exception.
   */
  private static void readImports(final Set<String> alreadyImported,
      final JSONElement el, final JSONManager mng) throws IOException {
    if(!el.hasValue("import")) return;
    final JSONElement imp = el.getValue("import");
    imp.expectArray();
    for(int i = 0; i < imp.size(); ++i) {
      final JSONElement file = imp.getAt(i);
      file.expectString();
      final String rName = file.string();
      if(alreadyImported.contains(rName)) {
        continue;
      }
      alreadyImported.add(rName);
      final Resource r = Resource.getFor(rName);
      final JSONReader in = new JSONReader(r.reader());
      final JSONElement imported = in.get();
      addTemplates(mng, imported);
      readImports(alreadyImported, imported, mng);
    }
  }

  /**
   * Adds all elements as templates.
   * 
   * @param mng The manager.
   * @param tmpls The template dictionary.
   */
  public static void addTemplates(final JSONManager mng, final JSONElement tmpls) {
    tmpls.expectObject();
    loop: for(final String k : tmpls.getKeys()) {
      switch(k) {
        case "import":
          continue loop;
        case "templates":
          throw new IllegalArgumentException("cannot define template called 'templates'");
      }
      mng.addTemplate(k, tmpls.getValue(k));
    }
  }

}
