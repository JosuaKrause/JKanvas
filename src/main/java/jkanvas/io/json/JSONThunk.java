package jkanvas.io.json;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class JSONThunk {

  private final IdManager mng;

  private final Map<String, JSONThunk> setters = new HashMap<>();

  private final Map<String, JSONThunk> constructorLookup = new HashMap<>();

  private final List<String> constructor = new ArrayList<>();

  private Class<?> type;

  private Object obj;

  private boolean eval;

  public JSONThunk(final IdManager mng) {
    this.mng = Objects.requireNonNull(mng);
  }

  public JSONThunk(final IdManager mng, final Object obj) {
    this.obj = Objects.requireNonNull(obj);
    this.mng = Objects.requireNonNull(mng);
    type = obj.getClass();
  }

  public void setConstructor(final String construct) {
    final String[] fields = construct.split(",");
    if(fields.length == 0) throw new IllegalArgumentException(
        "default constructor is implicit");
    if(!constructor.isEmpty()) throw new IllegalStateException("constructor already set");
    for(final String f : fields) {
      constructor.add(f);
      final JSONThunk thunk = setters.remove(f);
      if(thunk != null) {
        constructorLookup.put(f, thunk);
      }
    }
  }

  public void setType(final String typeName) throws IOException {
    Objects.requireNonNull(typeName);
    if(type != null) throw new IllegalStateException(
        "type already set: " + type);
    try {
      type = Class.forName(typeName);
    } catch(final ClassNotFoundException e) {
      throw new IOException("unknown type: " + typeName, e);
    }
  }

  public Class<?> getType() {
    return Objects.requireNonNull(type);
  }

  public void addField(final String name, final String id) {
    final JSONThunk thunk = mng.getForId(id);
    addField(name, thunk);
  }

  public void addField(final String name, final JSONThunk thunk) {
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

  public Object get() throws IOException {
    if(obj != null) {
      if(eval) throw new IOException("cyclic dependency!");
      eval = true;
      obj = eval();
      Objects.requireNonNull(obj);
      if(obj instanceof JSONFinished) {
        ((JSONFinished) obj).onSetupFinish();
      }
      eval = false;
    }
    return obj;
  }

  private Object create() throws IOException {
    try {
      if(constructor.isEmpty()) return type.newInstance();
      final Class<?>[] classes = new Class[constructor.size()];
      final Object[] args = new Object[classes.length];
      for(int i = 0; i < classes.length; ++i) {
        final JSONThunk thunk = constructorLookup.get(constructor.get(i));
        classes[i] = thunk.getType();
        args[i] = thunk.get();
      }
      final Constructor<?> c = type.getConstructor(classes);
      return c.newInstance(args);
    } catch(IllegalAccessException | InstantiationException | NoSuchMethodException
        | SecurityException | IllegalArgumentException | InvocationTargetException e) {
      throw new IOException(e);
    }
  }

  private String getSetter(final String name) {
    return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  private void callSetters(final Object o) throws IOException {
    for(final Entry<String, JSONThunk> el : setters.entrySet()) {
      final String setter = getSetter(el.getKey());
      final JSONThunk thunk = el.getValue();
      try {
        final Method method = type.getMethod(setter, thunk.getType());
        method.invoke(o, thunk.get());
      } catch(NoSuchMethodException | SecurityException | IllegalAccessException
          | IllegalArgumentException | InvocationTargetException e) {
        throw new IOException(e);
      }
    }
  }

  private Object eval() throws IOException {
    final Object res = create();
    callSetters(res);
    return res;
  }

  public boolean canCastTo(final Class<?> c) {
    return c.isAssignableFrom(getType());
  }

  public static final JSONThunk readJSON(final JSONElement el, final IdManager mng)
      throws IOException {
    if(el.isString()) return new JSONThunk(mng, el.string());
    el.expectObject();
    final JSONThunk thunk;
    if(el.hasValue("id")) {
      thunk = mng.getForId(el.getString("id", null));
    } else {
      thunk = new JSONThunk(mng);
    }
    loop: for(final String k : el.getKeys()) {
      switch(k) {
        case "id":
          continue loop;
        case "type":
          thunk.setType(el.getString(k, null));
          continue loop;
        case "args":
          thunk.setConstructor(el.getString(k, null));
          continue loop;
      }
      final JSONElement v = el.getValue(k);
      // TODO find out whether it is an id or direct object
      // TODO also which type
      thunk.addField(k, readJSON(v, mng));
    }
    return thunk;
  }

}
