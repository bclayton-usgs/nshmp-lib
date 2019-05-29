package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A sequence of xy-value pairs that is iterable ascending in x. Once created,
 * the x-values of a sequence are immutable.
 * 
 * <p>Sequences returned by the factory methods of this interface are guaranteed
 * to also have immutable y-values. Methods and classes in this package will
 * always return the sub-type {@link MutableXySequence} if the instance is, in
 * fact, mutable. Mutable sequences should not be considered thread safe.
 * 
 * <p>All data supplied to the factory methods in this interface are defensively
 * copied unless it is not necessary to do so. For instance, {@code *copyOf()}
 * variants should be used where possible as x-values will never be replicated
 * in memory.
 *
 * @author Peter Powers
 * @see MutableXySequence
 */
public interface XySequence extends Iterable<XyPoint> {

  /**
   * Create a new, immutable sequence from the supplied value arrays. Unlike
   * {@link #create(double[], double[])}, the supplied y-value array may not be
   * null.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with
   * @return an immutable, {@code double[]}-backed sequence
   * @throws NullPointerException if {@code xs} or {@code ys} are null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  public static XySequence create(double[] xs, double[] ys) {
    return Sequences.create(xs, checkNotNull(ys), false);
  }

  /**
   * Create a new, immutable sequence from the supplied value collections.
   * Unlike {@link #create(Collection, Collection)} , the supplied y-value
   * collection may not be null.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with
   * @return an immutable, {@code double[]}-backed sequence
   * @throws NullPointerException if {@code xs} or {@code ys} are null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  public static XySequence create(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys) {
    return Sequences.create(xs, checkNotNull(ys), false);
  }

  /**
   * Create an immutable copy of the supplied {@code sequence}.
   *
   * @param sequence to copy
   * @return an immutable copy of the supplied {@code sequence}
   * @throws NullPointerException if the supplied {@code sequence} is null
   */
  public static XySequence copyOf(XySequence sequence) {
    return (sequence.getClass().equals(ArrayXySequence.class)) ? sequence
        : new ArrayXySequence(sequence, false);
  }

  /**
   * Create a resampled version of the supplied {@code sequence}. Method
   * resamples via linear interpolation and does not extrapolate beyond the
   * domain of the source {@code sequence}; y-values with x-values outside the
   * domain of the source sequence are set to 0.
   *
   * @param sequence to resample
   * @param xs resample values
   * @return a resampled sequence
   */
  @Deprecated
  public static XySequence resampleTo(XySequence sequence, double[] xs) {
    // NOTE TODO this will support mfd combining
    checkNotNull(sequence);
    checkArgument(checkNotNull(xs).length > 0);

    Interpolator interpolator = Interpolator.builder()
        .build();

    double[] yResample = interpolator.findY(sequence.xValues(), sequence.yValues(), xs);

    // TODO disable extrapolation
    if (true) {
      throw new UnsupportedOperationException();
    }
    return MutableXySequence.create(xs, yResample);
  }

  /**
   * Returns the x-value at {@code index}.
   * @param index to retrieve
   * @return the x-value at {@code index}
   * @throws IndexOutOfBoundsException if the index is out of range (
   *         {@code index < 0 || index >= size()})
   */
  double x(int index);

  double xUnchecked(int index);

  /**
   * Returns the y-value at {@code index}.
   * @param index to retrieve
   * @return the y-value at {@code index}
   * @throws IndexOutOfBoundsException if the index is out of range (
   *         {@code index < 0 || index >= size()})
   */
  double y(int index);

  double yUnchecked(int index);

  /**
   * Returns the number or points in this sequence.
   * @return the sequence size
   */
  int size();

  /**
   * Returns an immutable {@code List} of the sequence x-values.
   * @return the {@code List} of x-values
   */
  List<Double> xValues();

  /**
   * Returns an immutable {@code List} of the sequence y-values.
   * @return the {@code List} of y-values
   */
  List<Double> yValues();

  /**
   * Returns an iterator over the {@link XyPoint}s in this sequence. For
   * immutable implementations, the {@link XyPoint#set(double)} method of a
   * returned point throws an {@code UnsupportedOperationException}.
   */
  Iterator<XyPoint> iterator();

  /**
   * The first {@link XyPoint} in this sequence.
   */
  XyPoint min();

  /**
   * The last {@link XyPoint} in this sequence.
   */
  XyPoint max();

  /**
   * Returns {@code true} if all y-values are 0.0; {@code false} otherwise. a
   */
  boolean isClear();

  /**
   * Returns a new, immutable sequence that has had all leading and trailing
   * zero-valued points ({@code y = 0}) removed. Any zero-valued points in the
   * middle of this sequence are ignored.
   * 
   * @throws IllegalStateException if {@link #isClear() this.isClear()} as empty
   *         sequences are not permitted
   */
  XySequence trim();

  /**
   * Transforms all y-values in place using the supplied {@link Function}.
   *
   * @param function for transform
   * @return {@code this} sequence, for use inline
   */
  default XySequence transform(Function<Double, Double> function) {
    throw new UnsupportedOperationException();
  }

}
