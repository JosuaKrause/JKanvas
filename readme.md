JKanvas
=======

An easy-to-use ZUI (Zoomable User Interface) implementation for Java.

This project is build with Maven. Use `mvn install` to generate it as a jar
in the *target/* directory. Alternatively the project can be used as dependency
in other Maven projects (see [below] (#maven-integration)).

In order to create a ZUI component you can use the following snippet:

```
Renderpass renderpass = new RenderpassAdapter() {

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    // draw here
    // ctx can be used to convert between canvas and component coordinates
  }

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    // react to events
  }

  @Override
  public Rectangle2D getBoundingBox() {
    // optionally return a bounding-box
  }

};
RenderpassPainter painter = new RenderpassPainter();
painter.addPass(renderpass);
Canvas canvas = new Canvas(painter, width, height);
// ...
parent.add(canvas); // adds the canvas to the parent component
canvas.reset(); // scrolls the bounding-box of the painter into view
```

In the package `jkanvas.examples` are examples that further show
how to use the canvas for various tasks.

### Maven Integration

In order to use JKanvas within a Maven project you can use the following dependency
(in the `<dependencies>` section):

    <dependency>
      <groupId>joschi-mvn</groupId>
      <artifactId>JKanvas</artifactId>
      <version>0.0.1</version>
    </dependency>

However, this requires an additional repository in the repositories section (`<repositories>`) of the pom.xml file:

    <repository>
      <id>joschi</id>
      <url>http://merkur57.inf.uni-konstanz.de/~krause/mvn/releases</url>
    </repository>
