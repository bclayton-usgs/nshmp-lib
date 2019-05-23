package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkElementIndex;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Mutable variant of {@code XySequence}.
 *
 * @author Peter Powers
 */
final class MutableXySequence extends ImmutableXySequence {

  MutableXySequence(double[] xs, double[] ys) {
    super(xs, ys);
  }

  MutableXySequence(XySequence sequence, boolean clear) {
    super(sequence, clear);
  }

  @Override
  public XyPoint min() {
    return new MutablePoint(0);
  }

  @Override
  public XyPoint max() {
    return new MutablePoint(size() - 1);
  }

  @Override
  public XySequence transform(Function<Double, Double> function) {
    DoubleData.transform(function, ys);
    return this;
  }

  @Override
  public Iterator<XyPoint> iterator() {
    return new MutableXyIterator();
  }

  public void set(int index, double value) {
    checkElementIndex(index, xs.length);
    ys[index] = value;
  }

  public XySequence add(double term) {
    DoubleData.add(term, ys);
    return this;
  }

  public XySequence add(double[] ys) {
    DoubleData.add(this.ys, ys);
    return this;
  }

  public XySequence add(XySequence sequence) {
    // safe covariant cast
    DoubleData.uncheckedAdd(ys, validateSequence((ImmutableXySequence) sequence).ys);
    return this;
  }

  public XySequence multiply(double scale) {
    DoubleData.multiply(scale, ys);
    return this;
  }

  public XySequence multiply(XySequence sequence) {
    // safe covariant cast
    DoubleData.uncheckedMultiply(ys, validateSequence((ImmutableXySequence) sequence).ys);
    return this;
  }

  public XySequence complement() {
    DoubleData.add(1, DoubleData.flip(ys));
    return this;
  }

  public XySequence clear() {
    Arrays.fill(ys, 0.0);
    return this;
  }

  class MutablePoint extends Point implements XyPoint {

    MutablePoint(int index) {
      super(index);
    }

    @Override
    public void set(double y) {
      MutableXySequence.this.set(index, y);
    }
  }

  class MutableXyIterator implements Iterator<XyPoint> {
    private final int size = size();
    private int caret = 0;

    @Override
    public boolean hasNext() {
      return caret < size;
    }

    @Override
    public XyPoint next() {
      return new MutablePoint(caret++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}