package jkanvas.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Provides a resource loading system without specifying how resources are
 * stored.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class ResourceLoader {

  /**
   * Loads the resource associated with the given identifier.
   * 
   * @param resource The resource identifier.
   * @return The input stream of the resource.
   * @throws IOException I/O Exception.
   */
  public abstract InputStream loadResource(String resource) throws IOException;

  /** The currently installed resource loader. */
  private static ResourceLoader loader = new ResourceLoader() {

    @Override
    public InputStream loadResource(final String resource) throws IOException {
      return new FileInputStream(resource);
    }

  };

  /**
   * Setter.
   * 
   * @param loader Sets the currently active resource loader.
   */
  public static final void setResourceLoader(final ResourceLoader loader) {
    ResourceLoader.loader = Objects.requireNonNull(loader);
  }

  /**
   * Getter.
   * 
   * @return The currently active resource loader.
   */
  public static final ResourceLoader getResourceLoader() {
    return loader;
  }

}
