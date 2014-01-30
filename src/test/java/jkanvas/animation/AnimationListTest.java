package jkanvas.animation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests {@link AnimationList}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimationListTest {

  /**
   * A test class for animated objects.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class TestAnimated implements Animated {

    /** The time when this animated object changes. */
    private final long trigger;
    /** The last animation time. */
    private long time;

    /**
     * Creates a test animated object.
     * 
     * @param trigger The trigger time.
     */
    public TestAnimated(final long trigger) {
      this.trigger = trigger;
      time = 0;
    }

    @Override
    public boolean animate(final long currentTime) {
      time = currentTime;
      return time == trigger;
    }

    /**
     * Getter.
     * 
     * @return The last animation time.
     */
    public long getTime() {
      return time;
    }

  } // TestAnimated

  /**
   * Checks whether the animated objects have the given time.
   * 
   * @param animated The animated objects.
   * @param time The expected time.
   */
  protected static void checkAnimated(
      final Iterable<TestAnimated> animated, final long time) {
    for(final TestAnimated a : animated) {
      assertEquals(time, a.getTime());
    }
  }

  /**
   * Creates animated objects.
   * 
   * @param trigger The trigger time.
   * @param count The number of objects.
   * @return The objects.
   */
  private static Iterable<TestAnimated> createAnimated(final long trigger, final int count) {
    final List<TestAnimated> list = new ArrayList<>();
    for(int i = 0; i < count; ++i) {
      list.add(new TestAnimated(trigger));
    }
    return list;
  }

  /**
   * Tests whether all objects are animated.
   * 
   * @param size The number of objects to animate.
   */
  private static void testRun(final int size) {
    final Iterable<TestAnimated> it = createAnimated(2, size);
    final AnimationList list = new AnimationList();
    for(final TestAnimated a : it) {
      list.addAnimated(a);
    }
    assertFalse(list.doAnimate(1));
    checkAnimated(it, 1);
    if(size > 0) {
      assertTrue(list.doAnimate(2));
      checkAnimated(it, 2);
    }
    assertFalse(list.doAnimate(3));
    checkAnimated(it, 3);
  }

  /**
   * Tests when not all objects are changed at the same time and scheduling
   * animations.
   * 
   * @param size The number of objects.
   */
  private static void testRunNotAll(final int size) {
    final Iterable<TestAnimated> it1 = createAnimated(2, size);
    final Iterable<TestAnimated> it2 = createAnimated(3, size);
    final AnimationList list = new AnimationList();
    list.scheduleAction(null, 1);
    // TODO #43 -- Java 8 simplification
    list.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        checkAnimated(it1, 0);
        checkAnimated(it2, 0);
      }

    }, 0);
    // TODO #43 -- Java 8 simplification
    list.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        checkAnimated(it1, 1);
        checkAnimated(it2, 1);
      }

    }, 1);
    // TODO #43 -- Java 8 simplification
    list.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        checkAnimated(it1, 2);
        checkAnimated(it2, 2);
      }

    }, 2);
    // TODO #43 -- Java 8 simplification
    list.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        fail();
      }

    }, 4);
    for(final TestAnimated a : it1) {
      list.addAnimated(a);
    }
    for(final TestAnimated a : it2) {
      list.addAnimated(a);
    }
    assertFalse(list.doAnimate(0));
    checkAnimated(it1, 0);
    checkAnimated(it2, 0);
    assertFalse(list.doAnimate(1));
    checkAnimated(it1, 1);
    checkAnimated(it2, 1);
    if(size > 0) {
      // TODO #43 -- Java 8 simplification
      list.scheduleAction(new AnimationAction() {

        @Override
        public void animationFinished() {
          checkAnimated(it1, 3);
          checkAnimated(it2, 3);
        }

      }, 1);
      assertTrue(list.doAnimate(2));
      checkAnimated(it1, 2);
      checkAnimated(it2, 2);
      assertTrue(list.doAnimate(3));
      checkAnimated(it1, 3);
      checkAnimated(it2, 3);
    }
  }

  /** Tests whether all objects are animated for various number of objects. */
  @Test
  public void completeness() {
    testRun(0);
    testRun(1);
    testRun(100);
    testRun(1000);
    testRun(10000);
  }

  /**
   * Tests when not all objects change at the same time and whether actions are
   * scheduled properly.
   */
  @Test
  public void notAllAnimated() {
    testRunNotAll(0);
    testRunNotAll(1);
    testRunNotAll(100);
    testRunNotAll(1000);
    testRunNotAll(10000);
  }

}
