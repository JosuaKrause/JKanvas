package jkanvas.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Finding dependecies between objects.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ObjectDependencies {

  /** Classes that will never be included. */
  public static final String[] NEVER = {
      "java.io.ObjectStreamField",
      "java.lang.Boolean",
      "java.lang.Class",
      "java.lang.Double",
      "java.lang.Float",
      "java.lang.Integer",
      "java.lang.Long",
      "java.lang.String",
      "jkanvas.nodelink.Dependency",
      "jkanvas.util.ObjectDependencies",
  };

  /** The standard set of allowed classes. */
  public static final String[] STD_CLASSES = {
      "java.awt.BasicStroke",
      "java.awt.Color",
      "java.awt.Dimension",
      "java.lang.ref.WeakReference",
      "java.util.ArrayList",
      "java.util.BitSet",
      "java.util.concurrent",
      "java.util.WeakHashMap",
      "jkanvas",
  };

  /** All allowed packages. */
  private final String[] allowedClasses;

  /** Creates a dependency container. */
  public ObjectDependencies() {
    this(STD_CLASSES);
  }

  /**
   * Creates a dependency container.
   * 
   * @param allowedClasses The allowed class prefixes.
   */
  public ObjectDependencies(final String[] allowedClasses) {
    this.allowedClasses = Objects.requireNonNull(allowedClasses);
    for(final String s : allowedClasses) {
      Objects.requireNonNull(s);
    }
  }

  /**
   * Whether the given class is a non primitive array.
   * 
   * @param clazz The class.
   * @return Whether the class is a non primitive array.
   */
  private static boolean isValidArray(final Class<?> clazz) {
    return clazz.isArray() && !clazz.getComponentType().isPrimitive();
  }

  /**
   * Getter.
   * 
   * @param clazz The class.
   * @return Whether the class is allowed.
   */
  private boolean isAllowed(final Class<?> clazz) {
    if(clazz.isArray() && clazz.getComponentType().isPrimitive()) return false;
    final String name = clazz.getName();
    for(final String a : NEVER) {
      if(name.startsWith(a)) return false;
    }
    for(final String a : allowedClasses) {
      if(name.startsWith(a)) return true;
    }
    System.out.println(name);
    return false;
  }

  /**
   * Adds the given reference directly to the collection.
   * 
   * @param others The collection.
   * @param ref The reference.
   * @return Whether the collection changed.
   */
  private static boolean addDirect(final Collection<Object> others, final Object ref) {
    if(ref == null) return false;
    if(others.contains(ref)) return false;
    return others.add(ref);
  }

  /**
   * Adds a reference to the collection.
   * 
   * @param others The collection.
   * @param ref The reference.
   * @return Whether the collection changed.
   */
  public boolean addRef(final Collection<Object> others, final Object ref) {
    if(ref == null) return false;
    if(others.size() > 1000) return false; // do not add more than 1000 objects
    if(others.contains(ref)) return false;
    final Class<? extends Object> clazz = ref.getClass();
    if(isValidArray(clazz) || isAllowed(clazz)) return addDirect(others, ref);
    return false;
  }

  /** Whether to exclude static fields. */
  public static boolean avoidStatic = true;

  /**
   * Adds all fields of an object.
   * 
   * @param clazz The class of the reference.
   * @param others The set of objects.
   * @param o The reference to get the field from.
   * @return Whether the collection changed.
   */
  public boolean fields(final Class<? extends Object> clazz,
      final Set<Object> others, final Object o) {
    boolean chg = false;
    final Field[] fields = clazz.getDeclaredFields();
    for(final Field f : fields) {
      try {
        if(f.isEnumConstant()) {
          continue;
        }
        if(avoidStatic && Modifier.isStatic(f.getModifiers())) {
          continue;
        }
        final boolean acc = f.isAccessible();
        f.setAccessible(true);
        final Object ref = f.get(o);
        chg = addRef(others, ref) || chg;
        f.setAccessible(acc);
      } catch(IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    final Class<?> superClazz = clazz.getSuperclass();
    if(superClazz != null) {
      chg |= fields(superClazz, others, o);
    }
    return chg;
  }

  /**
   * Getter.
   * 
   * @param o The reference.
   * @return The neighbors of this reference.
   */
  public Set<Object> neighbors(final Object o) {
    if(o == null) return Collections.emptySet();
    final Set<Object> others = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
    Class<? extends Object> clazz = o.getClass();
    if(isValidArray(clazz)) {
      final Object[] arr = (Object[]) o;
      for(final Object r : arr) {
        addRef(others, r);
      }
      return others;
    }
    do {
      fields(clazz, others, o);
      clazz = clazz.getSuperclass();
    } while(clazz != null);
    return others;
  }

}
