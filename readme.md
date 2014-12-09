JKanvas
=======

An easy-to-use ZUI (Zoomable User Interface) implementation for Java.

This project is build with Maven. Use `mvn install` to generate it as a jar
in the *target/* directory. Alternatively the project can be used as dependency
in other Maven projects (see [below] (#maven-integration)).

In order to create a ZUI component you can use the following snippet:

```java
Renderpass renderpass = new Renderpass() {

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    // draw here
    // ctx can be used to convert between canvas and component coordinates
  }

  @Override
  public boolean click(final Camera cam, final Point2D p, final MouseEvent e) {
    // react to events
  }

  @Override
  public void getBoundingBox(final RectangularShape rectangularShape) {
    // set bounding box for the render pass
  }

};
RenderpassPainter painter = new RenderpassPainter();
painter.addPass(renderpass);
Canvas canvas = new Canvas(painter, width, height);
// ...
parent.add(canvas); // adds the canvas to the parent component
canvas.reset(); // scrolls the bounding-box of the painter into view
```

In the package `jkanvas.examples` are examples that show
how to use the canvas for various tasks.
Furthermore the [wiki] (https://github.com/JosuaKrause/Kanvas/wiki) is meant to explain how to
use various classes and interfaces.

### Maven Integration

In order to use JKanvas within a Maven project you can use the following dependency
(in the `<dependencies>` section -- note that the *X.X.X* in the version tag
must be replaced with the current version):

```xml
<dependency>
  <groupId>joschi-mvn</groupId>
  <artifactId>JKanvas</artifactId>
  <version>X.X.X</version>
</dependency>
```

However, this requires an additional repository in the repositories section (`<repositories>`) of the pom.xml file:

```xml
<repository>
  <id>joschi</id>
  <url>http://josuakrause.github.io/info/mvn-repo/releases</url>
</repository>
```

When using [Eclipse] (http://www.eclipse.org/) the current snapshot can be used
by having the JKanvas project open and altering the version of the dependency
to *X.X.X-SNAPSHOT* where *X.X.X* is the upcoming version.
