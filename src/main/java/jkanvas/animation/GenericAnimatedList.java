package jkanvas.animation;

import java.util.Arrays;
import java.util.BitSet;

import jkanvas.util.Interpolator;

/**
 * A list of animated values.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class GenericAnimatedList {

  private final double[][] cur;

  private final double[][] from;

  private final double[][] to;

  /** The start times. */
  private long[] startTimes;

  /** The end times. */
  private long[] endTimes;

  /** Actions that are executed when an animation ends. */
  private AnimationAction[] onFinish;

  private final BitSet inAnimation;

  private final BitSet actives;

  /** The interpolation method or <code>null</code> if no animation is active. */
  private Interpolator pol;

  public GenericAnimatedList(final int numberOfDimensions, final int initialSize) {
    final int is = Math.max(128, initialSize);
    cur = new double[numberOfDimensions][is];
    from = new double[numberOfDimensions][is];
    to = new double[numberOfDimensions][is];
    startTimes = new long[is];
    endTimes = new long[is];
    onFinish = new AnimationAction[is];
    inAnimation = new BitSet();
    actives = new BitSet();
    pol = null;
  }

  protected void enlarge() {
    synchronized(actives) {
      final int curSize = Math.max(2, actives.length());
      final int newSize = curSize + curSize / 2;
      for(int d = 0; d < cur.length; ++d) {
        cur[d] = Arrays.copyOf(cur[d], newSize);
        from[d] = Arrays.copyOf(cur[d], newSize);
        to[d] = Arrays.copyOf(cur[d], newSize);
      }
      startTimes = Arrays.copyOf(startTimes, newSize);
      endTimes = Arrays.copyOf(endTimes, newSize);
      onFinish = Arrays.copyOf(onFinish, newSize);
    }
  }

  protected void compress() {
    synchronized(actives) {
      int pos = 0;
      for(;;) {
        final int startBlock = actives.nextSetBit(pos);
        if(startBlock < 0) {
          break;
        }
        final int endBlock = actives.nextClearBit(pos);
        final int blockLength = endBlock - startBlock;
        final int endPos = pos + blockLength;
        if(pos < startBlock) {
          for(int d = 0; d < cur.length; ++d) {
            System.arraycopy(cur[d], startBlock, cur[d], pos, blockLength);
            System.arraycopy(from[d], startBlock, from[d], pos, blockLength);
            System.arraycopy(to[d], startBlock, to[d], pos, blockLength);
          }
          System.arraycopy(startTimes, startBlock, startTimes, pos, blockLength);
          System.arraycopy(endTimes, startBlock, endTimes, pos, blockLength);
          System.arraycopy(onFinish, startBlock, onFinish, pos, blockLength);
          Arrays.fill(onFinish, endPos, endBlock, null);
          // FIXME bit copying may be slow...
          for(int i = 0; i < blockLength; ++i) {
            inAnimation.set(pos + i, inAnimation.get(startBlock + i));
          }
          actives.set(pos, endPos);
          actives.set(endPos, endBlock, false);
        }
        pos = endBlock;
      }
    }
  }

  protected void resetSize() {
    synchronized(actives) {
      compress();
      enlarge();
    }
  }

  protected int addIndex() {
    final int nextIndex;
    synchronized(actives) {
      nextIndex = actives.nextClearBit(0);
      actives.set(nextIndex);
      if(nextIndex >= startTimes.length) {
        enlarge();
      }
    }
    return nextIndex;
  }

  protected void removeIndex(final int index, final boolean execOnFinish) {
    final AnimationAction of;
    synchronized(actives) {
      if(!actives.get(index)) return;
      if(execOnFinish) {
        of = onFinish[index];
      } else {
        of = null;
      }
      onFinish[index] = null;
      actives.set(index, false);
    }
    if(of != null) {
      of.animationFinished();
    }
  }

  protected boolean isActive(final int index) {
    return actives.get(index);
  }

  protected double get(final int dim, final int index) {
    return cur[dim][index];
  }

  protected double getFrom(final int dim, final int index) {
    return from[dim][index];
  }

  protected double getTo(final int dim, final int index) {
    return to[dim][index];
  }

  protected long getStart(final int index) {
    return startTimes[index];
  }

  protected long getEnd(final int index) {
    return endTimes[index];
  }

  protected AnimationAction clearAction(final int index) {
    final AnimationAction res = onFinish[index];
    onFinish[index] = null;
    return res;
  }

  protected boolean inAnimation(final int index) {
    return inAnimation.get(index);
  }

  protected void set(final int dim, final int index, final double val) {
    cur[dim][index] = val;
  }

  protected void setTransition(final int dim, final int index,
      final double from, final double to) {
    this.from[dim][index] = from;
    this.to[dim][index] = to;
  }

  protected AnimationAction setAnimation(
      final int index, final long from, final long to, final AnimationAction of) {
    startTimes[index] = from;
    endTimes[index] = to;
    final AnimationAction res = clearAction(index);
    onFinish[index] = of;
    inAnimation.set(index);
    return res;
  }

  protected void clearAnimation(final int index) {
    inAnimation.set(index, false);
  }

  public void setInterpolation(final Interpolator pol) {
    this.pol = pol;
  }

  public Interpolator getInterpolation() {
    return pol;
  }

}
