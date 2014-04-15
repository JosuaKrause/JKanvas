package jkanvas.io.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * A lazy evaluated JSON object.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class JSONThunk implements ObjectCreator {

  /** The id manager. */
  private final JSONManager mng;
  /** All used setters. */
  private final Map<String, JSONThunk> setters = new HashMap<>();
  /** The thunks for the constructor arguments. */
  private final Map<String, JSONThunk> constructorLookup = new HashMap<>();
  /** Hints to find the right constructor. */
  private final Map<String, Class<?>> constructorHints = new HashMap<>();
  /** The constructor arguments. */
  private final List<String> constructor = new ArrayList<>();
  /** The creation string or <code>null</code>. */
  private final String str;
  /** The thunk array or <code>null</code>. */
  private final JSONThunk[] arr;
  /** The id if any. */
  private final String id;
  /** The creation type of the thunk if it is not a simple string. */
  private Class<?> type;
  /** The actual object. */
  private Object obj;
  /**
   * Whether the thunk is currently evaluating. This is used to detect circular
   * dependencies.
   */
  private boolean eval;

  /**
   * Creates a complex thunk.
   *
   * @param mng The manager.
   * @param id The id of the thunk if any. Must be non-<code>null</code> if
   *          <code>hasId</code> is <code>true</code>.
   * @param hasId Whether the thunk has an id.
   */
  public JSONThunk(final JSONManager mng, final String id, final boolean hasId) {
    this.mng = Objects.requireNonNull(mng);
    this.id = hasId ? Objects.requireNonNull(id) : null;
    str = null;
    arr = null;
  }

  /**
   * Creates a simple thunk.
   *
   * @param mng The manager.
   * @param str The creation string.
   */
  public JSONThunk(final JSONManager mng, final String str) {
    this.mng = Objects.requireNonNull(mng);
    this.str = Objects.requireNonNull(str);
    arr = null;
    id = null;
  }

  /**
   * Creates an array thunk.
   *
   * @param mng The manager.
   * @param arr The array.
   */
  public JSONThunk(final JSONManager mng, final JSONThunk[] arr) {
    this.mng = Objects.requireNonNull(mng);
    this.arr = Objects.requireNonNull(arr);
    str = null;
    id = null;
  }

  /**
   * Creates an already evaluated thunk.
   *
   * @param obj The evaluated object.
   * @param mng The manager.
   * @param id The id of the thunk. Must be non-<code>null</code>.
   */
  JSONThunk(final Object obj, final JSONManager mng, final String id) {
    this.mng = Objects.requireNonNull(mng);
    this.obj = Objects.requireNonNull(obj);
    this.id = Objects.requireNonNull(id);
    str = null;
    arr = null;
  }

  /**
   * Getter.
   *
   * @return The id of the thunk or <code>null</code> if it has none.
   */
  public String getId() {
    return id;
  }

  @Override
  public void setType(final String type) throws IOException {
    try {
      setType(findClass(type));
    } catch(IllegalStateException | ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  /**
   * Sets the creation type of the object. The type cannot be set when the
   * creation string is present.
   *
   * @param type The type.
   */
  public void setType(final Class<?> type) {
    if(obj != null) throw new IllegalStateException("object already evaluated");
    if(str != null) throw new IllegalStateException("cannot define type for primitives");
    if(arr != null) throw new IllegalStateException("cannot define type for arrays");
    if(this.type != null) throw new IllegalStateException("type already defined");
    Objects.requireNonNull(type);
    this.type = type;
  }

  @Override
  public boolean hasType() {
    return str != null || arr != null || type != null;
  }

  @Override
  public void setConstructor(final String construct) {
    if(obj != null) throw new IllegalStateException("object already evaluated");
    if(str != null) throw new IllegalStateException(
        "cannot define constructor for primitives");
    if(arr != null) throw new IllegalStateException(
        "cannot define constructor for arrays");
    if(!constructor.isEmpty()) throw new IllegalStateException("constructor already set");
    final String[] fields = construct.split(",");
    if(fields.length == 0) throw new IllegalArgumentException(
        "default constructor is implicit");
    for(final String f : fields) {
      final String field;
      if(f.endsWith(")")) {
        final int begin = f.lastIndexOf('(');
        final int end = f.lastIndexOf(')', f.length() - 2);
        if(begin >= 0 && end < begin) {
          final String hint = f.substring(begin + 1, f.length() - 1);
          field = f.substring(0, begin);
          try {
            constructorHints.put(field, findClass(hint));
          } catch(final ClassNotFoundException e) {
            throw new IllegalArgumentException(
                "could not interpret hint \"" + hint + "\" in " + construct, e);
          }
        } else {
          field = f;
        }
      } else {
        field = f;
      }
      if(field.isEmpty()) throw new IllegalArgumentException(
          "empty field in constructor definition: " + construct);
      constructor.add(field);
      final JSONThunk thunk = setters.remove(field);
      if(thunk != null) {
        constructorLookup.put(field, thunk);
      }
    }
  }

  @Override
  public boolean hasConstructor() {
    return !constructor.isEmpty();
  }

  /**
   * Adds a field to the thunk.
   *
   * @param name The name of the field.
   * @param id The id reference.
   */
  public void addField(final String name, final String id) {
    final JSONThunk thunk = mng.getForId(id);
    addField(name, thunk);
  }

  @Override
  public void addField(final String name, final JSONThunk thunk) {
    if(obj != null) throw new IllegalStateException("object already evaluated");
    if(str != null) throw new IllegalStateException("cannot add field for primitives");
    if(arr != null) throw new IllegalStateException("cannot add field type for arrays");
    if(constructor.contains(name)) {
      if(constructorLookup.containsKey(name)) throw new IllegalArgumentException(
          name + " already in use");
      constructorLookup.put(name, thunk);
    } else {
      if(setters.containsKey(name)) throw new IllegalArgumentException(
          name + " already in use");
      setters.put(name, thunk);
    }
  }

  @Override
  public boolean hasField(final String name) {
    return constructorLookup.containsKey(name) || setters.containsKey(name);
  }

  /**
   * Finds and loads the class for the given name. This also finds inner
   * classes.
   *
   * @param name The name of the class.
   * @return The class.
   * @throws ClassNotFoundException If the class cannot be found.
   */
  private static Class<?> findClass(final String name) throws ClassNotFoundException {
    String n = name;
    for(;;) {
      try {
        return Class.forName(n);
      } catch(final ClassNotFoundException e) {
        final int i = n.lastIndexOf('.');
        if(i < 0) throw new ClassNotFoundException("class not found: " + name, e);
        n = n.substring(0, i) + "$" + n.substring(i + 1);
      }
    }
  }

  /**
   * Casts the object to the given type.
   *
   * @param <T> The type.
   * @param o The object.
   * @param type The type to cast.
   * @return The cast object.
   * @throws IOException I/O Exception.
   */
  @SuppressWarnings("unchecked")
  private static <T> T cast(final Object o, final Class<T> type) throws IOException {
    final Class<?> oc = o.getClass();
    if(type.isPrimitive()) {
      try {
        final Object pt = oc.getField("TYPE").get(null);
        if(pt != type) throw new IllegalArgumentException();
        return (T) o;
      } catch(final IllegalArgumentException | IllegalAccessException
          | NoSuchFieldException | SecurityException e) {
        throw new IOException(
            "invalid type: " + type.getSimpleName() + " <=!= " + oc.getSimpleName(), e);
      }
    }
    if(!type.isAssignableFrom(oc)) throw new IOException(
        "invalid type: " + type.getSimpleName() + " <=!= " + oc.getSimpleName());
    return (T) o;
  }

  /**
   * Evaluates the thunk and returns the content. If the thunk is already
   * evaluated the content is returned directly.
   *
   * @param <T> The expected type.
   * @param <C> The component type if it is an array.
   * @param type The expected type of the content.
   * @return The content.
   * @throws IOException I/O Exception.
   */
  public <T, C> T get(final Class<T> type) throws IOException {
    if(eval) throw new IOException("cyclic dependency!");
    if(obj != null) return cast(obj, type);
    eval = true;
    if(arr != null) {
      if(!type.isArray()) throw new IOException(
          "expected array type: " + type.getSimpleName());
      @SuppressWarnings("unchecked")
      final Class<C> comp = (Class<C>) type.getComponentType();
      @SuppressWarnings("unchecked")
      final C[] res = (C[]) Array.newInstance(comp, arr.length);
      for(int i = 0; i < res.length; ++i) {
        final C cur = arr[i].get(comp);
        res[i] = cast(cur, comp);
      }
      obj = res;
    } else if(str != null) {
      try {
        if(type.isAssignableFrom(Long.class)
            || type.isAssignableFrom(Long.TYPE)) {
          obj = Long.parseLong(str);
        } else if(type.isAssignableFrom(Integer.class)
            || type.isAssignableFrom(Integer.TYPE)) {
          obj = Integer.parseInt(str);
        } else if(type.isAssignableFrom(Short.class)
            || type.isAssignableFrom(Short.TYPE)) {
          obj = Short.parseShort(str);
        } else if(type.isAssignableFrom(Byte.class)
            || type.isAssignableFrom(Byte.TYPE)) {
          obj = Byte.parseByte(str);
        } else if(type.isAssignableFrom(Double.class)
            || type.isAssignableFrom(Double.TYPE)) {
          obj = Double.parseDouble(str);
        } else if(type.isAssignableFrom(Float.class)
            || type.isAssignableFrom(Float.TYPE)) {
          obj = Float.parseFloat(str);
        } else if(type.isAssignableFrom(String.class)) {
          obj = str;
        } else if(str.startsWith("#")) {
          // string is ID
          obj = mng.getForId(str.substring(1)).get(type);
        } else {
          // string is constant
          obj = getConstant(str, type);
        }
      } catch(final NumberFormatException e) {
        try {
          obj = getConstant(str, type);
        } catch(final IOException io) {
          throw new IOException(
              "could not interpret \"" + str + "\" as " + type.getSimpleName(), io);
        }
      }
    } else {
      obj = eval(type);
    }
    eval = false;
    return cast(obj, type);
  }

  /**
   * Interpret string as constant.
   *
   * @param str The string to interpret as constant.
   * @param type The type.
   * @return The constant.
   * @throws IOException I/O Exception.
   */
  private static Object getConstant(
      final String str, final Class<?> type) throws IOException {
    final int constant = str.lastIndexOf('.');
    if(constant < 0) throw new IOException(
        "expected '.' for constant: " + str + " for type: " + type.getSimpleName());
    try {
      final Class<?> cl = findClass(str.substring(0, constant));
      final Field f = cl.getDeclaredField(str.substring(constant + 1));
      return f.get(null);
    } catch(ClassNotFoundException | IllegalArgumentException | IllegalAccessException
        | NoSuchFieldException | SecurityException e) {
      throw new IOException("could not interpret \"" + str
          + "\" as constant for type: " + type.getSimpleName(), e);
    }
  }

  /**
   * Finds a constructor for the given type with the given number of arguments.
   *
   * @param type The type.
   * @param args The names of the arguments.
   * @param hints The constructor hints.
   * @return The first matching constructor.
   * @throws IOException I/O Exception.
   */
  private static Constructor<?> findConstructor(final Class<?> type,
      final List<String> args, final Map<String, Class<?>> hints)
      throws IOException {
    outer: for(final Constructor<?> c : type.getConstructors()) {
      final Class<?>[] params = c.getParameterTypes();
      if(params.length != args.size()) {
        continue outer;
      }
      for(int i = 0; i < params.length; ++i) {
        final Class<?> hint = hints.get(args.get(i));
        if(hint == null) {
          continue;
        }
        if(!params[i].equals(hint)) {
          continue outer;
        }
      }
      return c;
    }
    throw new IOException("could not find constructor: " + type.getSimpleName());
  }

  /**
   * Creates an object of the given type. The specified constructor is used.
   *
   * @param type The type.
   * @return The object.
   * @throws IOException I/O Exception.
   */
  private Object create(final Class<?> type) throws IOException {
    try {
      if(constructor.isEmpty()) return type.newInstance();
      final Constructor<?> c = findConstructor(type, constructor, constructorHints);
      final Class<?>[] classes = c.getParameterTypes();
      final Object[] args = new Object[classes.length];
      for(int i = 0; i < classes.length; ++i) {
        final JSONThunk thunk = constructorLookup.get(constructor.get(i));
        args[i] = thunk.get(classes[i]);
      }
      return c.newInstance(args);
    } catch(IllegalAccessException | InstantiationException | SecurityException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new IOException(e);
    }
  }

  /**
   * Constructs the setter name for the given field.
   *
   * @param name The field name.
   * @return The name of the setter method.
   */
  private static String getSetter(final String name) {
    return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * Finds a method with one argument and a setter typical name for the given
   * field.
   *
   * @param type The type.
   * @param name The field name.
   * @return The setter method.
   * @throws IOException I/O Exception.
   */
  private static Method findSetter(
      final Class<?> type, final String name) throws IOException {
    final String setter = getSetter(name);
    for(final Method m : type.getMethods()) {
      if(!setter.equals(m.getName())) {
        continue;
      }
      if(m.getParameterTypes().length == 1) return m;
    }
    throw new IOException("could not find setter (" + setter
        + ") in " + type.getSimpleName() + " for: " + name);
  }

  @Override
  public void callSetters(final Object o) throws IOException {
    callSetters(o, setters);
  }

  /**
   * Calls setters on an object.
   *
   * @param o The object.
   * @param setters The setters.
   * @throws IOException I/O Exception.
   */
  public static final void callSetters(
      final Object o, final Map<String, JSONThunk> setters) throws IOException {
    final Class<?> type = o.getClass();
    for(final Entry<String, JSONThunk> el : setters.entrySet()) {
      try {
        final Method method = findSetter(type, el.getKey());
        method.invoke(o, el.getValue().get(method.getParameterTypes()[0]));
      } catch(SecurityException | IllegalAccessException
          | IllegalArgumentException | InvocationTargetException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Evaluates the thunk.
   *
   * @param type The expected type.
   * @return The object.
   * @throws IOException I/O Exception.
   */
  private Object eval(final Class<?> type) throws IOException {
    final Class<?> t;
    if(this.type != null) {
      if(!type.isAssignableFrom(this.type)) throw new IOException(
          "cannot convert " + this.type.getSimpleName() + " to " + type.getSimpleName());
      t = this.type;
    } else {
      t = type;
    }
    final Object res = create(t);
    callSetters(res);
    if(res instanceof JSONFinished) {
      ((JSONFinished) res).onSetupFinish();
    }
    return res;
  }

  /**
   * Reads a thunk from JSON input.
   *
   * @param el The root element.
   * @param mng The manager.
   * @return The thunk.
   * @throws IOException I/O Exception.
   */
  public static final JSONThunk readJSON(final JSONElement el, final JSONManager mng)
      throws IOException {
    if(el.isString()) return new JSONThunk(mng, el.string());
    if(el.isArray()) {
      final JSONThunk[] arr = new JSONThunk[el.size()];
      for(int i = 0; i < arr.length; ++i) {
        arr[i] = readJSON(el.getAt(i), mng);
      }
      return new JSONThunk(mng, arr);
    }
    el.expectObject();
    final JSONThunk thunk;
    if(el.hasValue("id")) {
      thunk = mng.getForId(el.getString("id", null));
    } else {
      thunk = new JSONThunk(mng, null, false);
    }
    addFields(thunk, el, mng, null);
    return thunk;
  }

  /**
   * Adds all fields from the given object to the thunk.
   *
   * @param thunk The object creator.
   * @param el The object.
   * @param mng The manager.
   * @param ignore The fields to ignore.
   * @throws IOException I/O Exception.
   */
  public static void addFields(final ObjectCreator thunk, final JSONElement el,
      final JSONManager mng, final Set<String> ignore) throws IOException {
    JSONElement cur = el;
    while(cur != null) {
      cur.expectObject();
      JSONElement tmpl = null;
      loop: for(final String k : cur.getKeys()) {
        if(ignore != null && ignore.contains(k)) {
          continue loop;
        }
        switch(k) {
          case "id":
            if(cur != el) throw new IOException("id in template not allowed");
            continue loop;
          case "type":
            if(cur != el && thunk.hasType()) {
              continue loop;
            }
            thunk.setType(cur.getString(k, null));
            continue loop;
          case "args":
            if(cur != el && thunk.hasConstructor()) {
              continue loop;
            }
            thunk.setConstructor(cur.getString(k, null));
            continue loop;
          case "template":
            if(tmpl != null) throw new IOException("double template definition");
            tmpl = mng.getTemplate(cur.getString(k, null));
            continue loop;
        }
        if(cur != el && thunk.hasField(k)) {
          continue loop;
        }
        final JSONElement v = cur.getValue(k);
        thunk.addField(k, readJSON(v, mng));
      }
      cur = tmpl;
      tmpl = null;
    }
  }

}
