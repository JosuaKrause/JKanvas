package jkanvas.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * A device independent resource identifier. It can either be an URL, a file, or
 * a system resource (located in the jar).
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Resource {

  /** The UTF-8 character set. */
  public static final Charset UTF8 = Charset.forName("UTF-8");
  /** Resource path. */
  public static final String RESOURCES = "src/main/resources/";
  /** ANT resource path. */
  public static final String ANT = "/resources/";

  /**
   * Ensures that the given directory exists.
   * 
   * @param dir The directory.
   * @return The directory.
   */
  private static File ensureDir(final File dir) {
    if(dir == null) return null;
    if(dir.exists()) return dir;
    ensureDir(dir.getParentFile());
    dir.mkdir();
    return dir;
  }

  /**
   * The local resource path or <code>null</code> if a direct path is specified.
   */
  private final String local;

  /** The path part. */
  private final String path;

  /** The file part or <code>null</code> if no file is specified. */
  private final String file;

  /** Dump for caching system resources. */
  private final String dump;

  /** The character set. */
  private final Charset cs;

  /**
   * Automatically decides what type of resource is provided.
   * 
   * @param name The name of the resource.
   * @return The resource.
   */
  public static Resource getFor(final String name) {
    Objects.requireNonNull(name);
    if(name.contains("://")) return new Resource(null, name, UTF8, null);
    if(new File(name).exists()) return new Resource(null, name, UTF8, null);
    return new Resource(name);
  }

  /**
   * Creates a resource for the given file.
   * 
   * @param file The file.
   * @return The resource as direct file.
   * @throws IOException I/O Exception.
   */
  public static Resource getFor(final File file) throws IOException {
    return new Resource(null, file.getCanonicalPath(), UTF8, null);
  }

  /**
   * Creates a system resource (located in the jar).
   * 
   * @param name The name of the system resource.
   */
  public Resource(final String name) {
    this(RESOURCES, name, UTF8, RESOURCES);
  }

  /**
   * Creates a resource.
   * 
   * @param local The local resource path or <code>null</code> if a direct path
   *          is specified.
   * @param resource The resource to get.
   * @param cs The character set or <code>null</code>.
   * @param dump The dump.
   */
  public Resource(final String local, final String resource,
      final String cs, final String dump) {
    this(local, resource, chooseCharset(cs), dump);
  }

  /**
   * Creates a resource.
   * 
   * @param local The local resource path or <code>null</code> if a direct path
   *          is specified.
   * @param resource The resource to get.
   * @param cs The character set.
   * @param dump The dump.
   */
  public Resource(final String local, final String resource,
      final Charset cs, final String dump) {
    this(local, resource, null, cs, dump);
  }

  /**
   * Creates a resource.
   * 
   * @param local resource path or <code>null</code> if a direct path is
   *          specified.
   * @param path The path part.
   * @param file The file part.
   * @param cs The character set.
   * @param dump The dump.
   */
  private Resource(final String local, final String path,
      final String file, final Charset cs, final String dump) {
    this.path = Objects.requireNonNull(path);
    this.cs = Objects.requireNonNull(cs);
    this.file = file;
    final String l;
    if(local != null) {
      l = endsWithDelim(local) ? local : local + "/";
    } else {
      l = null;
    }
    this.local = l;
    final String d;
    if(dump != null) {
      d = endsWithDelim(dump) ? dump : dump + "/";
    } else {
      d = null;
    }
    this.dump = d;
  }

  /**
   * Gets the {@link URL} of a resource.
   * 
   * @return the URL
   * @throws IOException if the resource can't be found
   */
  public URL getURL() throws IOException {
    final String resource = getResource();
    if(resource.contains("://")) return new URL(resource);
    if(hasDirectFile()) return new File(resource).toURI().toURL();
    final URL url = Resource.class.getResource('/' + resource);
    if(url != null) return url;
    final URL antUrl = Resource.class.getResource(ANT + resource);
    if(antUrl != null) return antUrl;
    final File f = new File(local, resource);
    if(!f.canRead()) throw new IOException(f.getName() + " doesn't exist");
    return f.toURI().toURL();
  }

  /**
   * Getter.
   * 
   * @return The combined path and file field.
   */
  private String getResource() {
    if(file == null) return path;
    return endsWithDelim(path) ? path + file : path + "/" + file;
  }

  /**
   * Computes the parent of a path or removes the trailing delimiter.
   * 
   * @param path The path.
   * @return The parent or the path without trailing delimiter.
   */
  private static String parent(final String path) {
    final int i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
    return path.substring(0, i + 1);
  }

  /**
   * Checks whether the path ends with a delimiter.
   * 
   * @param path The path.
   * @return <code>true</code> if the path ends with a delimiter.
   */
  private static boolean endsWithDelim(final String path) {
    final char end = path.charAt(path.length() - 1);
    return end == '/' || end == '\\';
  }

  /**
   * Computes the parent of a path.
   * 
   * @param path The path.
   * @return The parent.
   */
  private static String getParent(final String path) {
    String f = path;
    while(endsWithDelim(f)) {
      f = parent(f);
    }
    return parent(f);
  }

  /**
   * Getter.
   * 
   * @return The parent of the resource.
   */
  public Resource getParent() {
    return new Resource(local, getParent(getResource()), cs, dump);
  }

  /**
   * Changes the extension of a file.
   * 
   * @param file The file as string.
   * @param ext The extension.
   * @return The file with substituted extension.
   */
  private static String changeExtensionTo(final String file, final String ext) {
    final String dext = ext.charAt(0) == '.' ? ext : "." + ext;
    final int dot = file.lastIndexOf('.');
    return dot < 0 ? file + dext : file.substring(0, dot) + dext;
  }

  /**
   * Returns a resource with exchanged extension. If the resource denotes a
   * folder a file with the name of the folder and the given extension in the
   * parent directory is returned.
   * 
   * @param ext The new extension.
   * @return The resource with substituted extension.
   */
  public Resource changeExtensionTo(final String ext) {
    if(file != null) return new Resource(
        local, path, changeExtensionTo(file, ext), cs, dump);
    final String p = endsWithDelim(path) ? parent(path) : path;
    return new Resource(local, changeExtensionTo(p, ext), cs, dump);
  }

  /**
   * Adds a file part to the resource.
   * 
   * @param name The name of the file.
   * @return The resource pointing to the file.
   */
  public Resource getFile(final String name) {
    return new Resource(local, getResource(), name, cs, dump);
  }

  /**
   * Getter.
   * 
   * @return Whether there is no local part.
   */
  public boolean hasDirectFile() {
    return local == null;
  }

  /**
   * Creates a direct file and ensures that the folder for the file exists.
   * 
   * @return The file object.
   */
  public File directFile() {
    if(!hasDirectFile()) throw new IllegalStateException("has local part: " + local);
    final File res = new File(getResource());
    ensureDir(res.getParentFile());
    return res;
  }

  /**
   * Getter.
   * 
   * @return Converts this resource to a dump resource if possible or needed.
   */
  public Resource toDump() {
    if(dump == null && local == null) return this;
    return new Resource(null, dump + getResource(), cs, null);
  }

  /**
   * Checks whether the resource has content. This method does not guarantee
   * whether the resource has content the next time the stream to it is opened,
   * though.
   * 
   * @return If non empty content could be obtained.
   */
  public boolean hasContent() {
    final URL url;
    try {
      url = getURL();
    } catch(final IOException e) {
      return false;
    }
    try (InputStream in = url.openStream()) {
      final boolean content = in.read() >= 0;
      return content;
    } catch(final IOException e) {
      return false;
    }
  }

  /**
   * Opens a stream for the resource.
   * 
   * @return The stream.
   * @throws IOException I/O exception.
   */
  public InputStream stream() throws IOException {
    return getURL().openStream();
  }

  /**
   * Creates a {@link BufferedReader}.
   * 
   * @return reader or <code>null</code> if not found.
   * @throws IOException I/O exception
   */
  public BufferedReader reader() throws IOException {
    if(!hasContent()) return null;
    final URL url = getURL();
    return charsetReader(url.openStream());
  }

  /**
   * Getter.
   * 
   * @return Returns the whole content as {@link String}.
   * @throws IOException I/O Exception.
   */
  public String asString() throws IOException {
    try (final Reader r = reader()) {
      final char[] buff = new char[1024];
      final StringBuilder sb = new StringBuilder();
      for(;;) {
        final int read = r.read(buff, 0, buff.length);
        if(read < 0) return sb.toString();
        sb.append(buff, 0, read);
      }
    }
  }

  /**
   * Creates an output stream if possible.
   * 
   * @return output stream or <code>null</code> if not possible.
   * @throws FileNotFoundException I/O Exception.
   */
  public OutputStream outStream() throws FileNotFoundException {
    if(!hasDirectFile()) return null;
    return new FileOutputStream(directFile());
  }

  /**
   * Creates a {@link Writer} if possible.
   * 
   * @return writer or <code>null</code> if not possible.
   * @throws IOException I/O exception
   */
  public Writer writer() throws IOException {
    if(!hasDirectFile()) return null;
    return new OutputStreamWriter(new FileOutputStream(directFile()), cs);
  }

  /**
   * Creates a {@link BufferedReader} from the given {@link InputStream} that
   * interprets the incoming bytes using the given character set.
   * 
   * @param in input stream
   * @return reader
   */
  private BufferedReader charsetReader(final InputStream in) {
    return new BufferedReader(new InputStreamReader(in, cs));
  }

  /**
   * Whether the given resource is a ZIP file.
   * 
   * @return Whether the resource is a ZIP file.
   */
  public boolean isZip() {
    return isZip(getResource());
  }

  /**
   * Getter.
   * 
   * @return The character set.
   */
  public Charset getCharset() {
    return cs;
  }

  /**
   * Whether the given path points to a ZIP file.
   * 
   * @param path The path.
   * @return Whether it is a ZIP file.
   */
  private static boolean isZip(final String path) {
    return path.endsWith(".zip");
  }

  /**
   * Uses the given character set or the default character set if it is not
   * given.
   * 
   * @param cs The wanted character set or <code>null</code>.
   * @return The character set.
   */
  private static Charset chooseCharset(final String cs) {
    return cs != null ? Charset.forName(cs) : UTF8;
  }

  @Override
  public String toString() {
    return (local != null ? local : "") + getResource() + " [local: " + local + " path: "
        + path + " file: " + file + " dump: " + dump + " cs: " + cs.name() + "]";
  }

  /**
   * Calculates the relative path from <code>base</code> to <code>dest</code>.
   * If both paths have different roots e.g. in windows on different drives. The
   * absolute path of <code>dest</code> is returned.
   * 
   * @param base The starting point of the relative path.
   * @param dest The destination point of the relative path.
   * @return A relative path from <code>base</code> to <code>dest</code> as
   *         string.
   */
  public final static String relativePath(final File base, final File dest) {
    final String[] absoluteDirectories = base.getAbsolutePath().split("[/\\\\]");
    final String[] relativeDirectories = dest.getAbsolutePath().split("[/\\\\]");
    final int length = absoluteDirectories.length < relativeDirectories.length ?
        absoluteDirectories.length : relativeDirectories.length;

    int lastCommonRoot = -1;
    int index;
    for(index = 0; index < length; ++index) {
      if(!absoluteDirectories[index].equals(relativeDirectories[index])) {
        break;
      }
      lastCommonRoot = index;
    }
    if(lastCommonRoot == -1) return dest.getAbsolutePath();

    final StringBuilder relativePath = new StringBuilder();
    for(index = lastCommonRoot + 1; index < absoluteDirectories.length; ++index) {
      if(!absoluteDirectories[index].isEmpty()) {
        relativePath.append("../");
      }
    }
    for(index = lastCommonRoot + 1; index < relativeDirectories.length - 1; ++index) {
      relativePath.append(relativeDirectories[index]);
      relativePath.append("/");
    }
    relativePath.append(relativeDirectories[relativeDirectories.length - 1]);
    return relativePath.toString();
  }

}
