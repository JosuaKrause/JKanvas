package jkanvas.io.json;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Accesses the static <code>loadFromJSON</code> method from a class to create
 * objects via JSON description.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public final class JSONLoader {

  /** Nothing to construct. */
  private JSONLoader() {
    throw new AssertionError();
  }

  /**
   * Prepends the JSON element to the front of the argument list.
   *
   * @param el The JSON element.
   * @param argValues The argument list.
   * @return The new argument list.
   */
  private static Object[] prepareArgValues(final JSONElement el, final Object[] argValues) {
    final Object[] res = new Object[argValues.length + 1];
    System.arraycopy(argValues, 0, res, 1, argValues.length);
    res[0] = el;
    return res;
  }

  /**
   * Computes the types of the argument list.
   *
   * @param argValues The arguments.
   * @return The types of the arguments.
   */
  private static Class<?>[] argTypes(final Object[] argValues) {
    final Class<?>[] res = new Class<?>[argValues.length];
    for(int i = 0; i < argValues.length; ++i) {
      res[i] = argValues[i].getClass();
    }
    return res;
  }

  /**
   * Constructs an object via the static <code>loadFromJSON</code> method in the
   * type given by the field <code>type</code> of the JSON element. The static
   * method must accept the JSON element as first argument and the other
   * arguments in the given order. The result of the method must be of the
   * result type.
   *
   * @param <T> The type of the resulting object.
   * @param el The JSON element to construct the object.
   * @param result The result type.
   * @param argValues The arguments after the JSON element.
   * @return The constructed object.
   */
  public static <T> T fromJSONloader(final JSONElement el,
      final Class<T> result, final Object[] argValues) {
    el.expectObject();
    final String type = Objects.requireNonNull(el.getString("type", null));
    final Object[] args = prepareArgValues(el, argValues);
    try {
      final Class<?> clz = Class.forName(type);
      if(!result.isAssignableFrom(clz)) throw new IllegalArgumentException(
          "class " + type + " must be a " + result.getName());
      final Method m = clz.getDeclaredMethod("loadFromJSON", argTypes(args));
      if(!result.isAssignableFrom(m.getReturnType())) throw new IllegalArgumentException(
          "return type of method must be a " + clz.getName());
      @SuppressWarnings("unchecked")
      final T res = (T) m.invoke(null, args);
      return res;
    } catch(final ClassNotFoundException | NoSuchMethodException | SecurityException
        | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Reads a rectangle from JSON.
   *
   * @param el The element.
   * @return The rectangle.
   */
  public static Rectangle2D getRectFromJSON(final JSONElement el) {
    el.expectObject();
    final double x = el.getDouble("x", 0.0);
    final double y = el.getDouble("y", 0.0);
    final double w = el.getDouble("width", 0.0);
    final double h = el.getDouble("height", 0.0);
    return new Rectangle2D.Double(x, y, w, h);
  }

  /**
   * Reads a point from JSON.
   *
   * @param el The element.
   * @return The point.
   */
  public static Point2D getPointFromJSON(final JSONElement el) {
    el.expectObject();
    final double x = el.getDouble("x", 0.0);
    final double y = el.getDouble("y", 0.0);
    return new Point2D.Double(x, y);
  }

}
