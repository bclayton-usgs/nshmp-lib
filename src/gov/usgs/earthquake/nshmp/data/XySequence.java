package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static gov.usgs.earthquake.nshmp.data.DoubleData.areMonotonic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.google.common.primitives.Doubles;

/**
 * A sequence of xy-value pairs that is iterable ascending in x. Once created,
 * the x-values of a sequence are immutable.
 * 
 * <p>Sequences returned by the factory methods of this interface are guaranteed
 * to also have immutable y-values. Methods and classes in this package will
 * always return the sub-type {@link MutableXySequence} if the instance is, in
 * fact, mutable. Mutable sequences should not be considered thread safe.
 * 
 * <p>All data supplied to the factory methods in this interface
 * {@code MutableXySequence} are defensively copied unless it is not necessary to
 * do so. For instance, {@code *copyOf()} variants should be used where possible
 * as x-values will never be replicated in memory.
 *
 * @author Peter Powers
 * @see MutableXySequence
 */
public interface XySequence extends Iterable<XyPoint> {

  /**
   * Create a new sequence with mutable y-values from the supplied value arrays.
   * If the supplied y-value array is null, all y-values are initialized to 0.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with; may be null
   * @return a mutable, {@code double[]}-backed sequence
   * @throws NullPointerException if {@code xs} are null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  public static XySequence create(double[] xs, double[] ys) {
    return create(xs, ys, true);
  }

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
  public static XySequence createImmutable(double[] xs, double[] ys) {
    return create(xs, checkNotNull(ys), false);
  }

  static XySequence create(double[] xs, double[] ys, boolean mutable) {
    return construct(
        Arrays.copyOf(xs, xs.length),
        (ys == null) ? new double[xs.length] : Arrays.copyOf(ys, ys.length),
        mutable);
  }

  /**
   * Create a new sequence with mutable y-values from the supplied value
   * collections. If the y-value collection is null, all y-values are
   * initialized to 0.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with; may be null
   * @return a mutable, {@code double[]}-backed sequence
   * @throws NullPointerException if {@code xs} are null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  public static XySequence create(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys) {
    return create(xs, ys, true);
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
  public static XySequence createImmutable(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys) {
    return create(xs, checkNotNull(ys), false);
  }

  static XySequence create(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys,
      boolean mutable) {

    return construct(
        Doubles.toArray(xs),
        (ys == null) ? new double[xs.size()] : Doubles.toArray(ys),
        mutable);
  }

  static XySequence construct(double[] xs, double[] ys, boolean mutable) {
    checkArgument(xs.length > 0, "x-values may not be empty");
    checkArgument(xs.length == ys.length, "x- and y-values are different sizes");
    if (xs.length > 1) {
      checkArgument(areMonotonic(true, true, xs), "x-values do not increase monotonically");
    }
    return mutable ? new MutableXySequence(xs, ys) : new ImmutableXySequence(xs, ys);
  }

  /**
   * Create a mutable copy of the supplied {@code sequence}.
   *
   * @param sequence to copy
   * @return a mutable copy of the supplied {@code sequence}
   * @throws NullPointerException if the supplied {@code sequence} is null
   */
  public static XySequence copyOf(XySequence sequence) {
    return new MutableXySequence(checkNotNull(sequence), false);
  }

  /**
   * Create a mutable copy of the supplied {@code sequence} with all y-values
   * reset to zero.
   *
   * @param sequence to copy
   * @return a mutable copy of the supplied {@code sequence}
   * @throws NullPointerException if the supplied {@code sequence} is null
   */
  public static XySequence emptyCopyOf(XySequence sequence) {
    return new MutableXySequence(checkNotNull(sequence), true);
  }

  /**
   * Create an immutable copy of the supplied {@code sequence}.
   *
   * @param sequence to copy
   * @return an immutable copy of the supplied {@code sequence}
   * @throws NullPointerException if the supplied {@code sequence} is null
   */
  public static XySequence immutableCopyOf(XySequence sequence) {
    return (sequence.getClass().equals(ImmutableXySequence.class)) ? sequence
        : new ImmutableXySequence(sequence, false);
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
    return XySequence.create(xs, yResample);
  }

  /**
   * Returns the x-value at {@code index}.
   * @param index to retrieve
   * @return the x-value at {@code index}
   * @throws IndexOutOfBoundsException if the index is out of range (
   *         {@code index < 0 || index >= size()})
   */
  public double x(int index);

  double xUnchecked(int index);

  /**
   * Returns the y-value at {@code index}.
   * @param index to retrieve
   * @return the y-value at {@code index}
   * @throws IndexOutOfBoundsException if the index is out of range (
   *         {@code index < 0 || index >= size()})
   */
  public double y(int index);

  double yUnchecked(int index);

  /**
   * Returns the number or points in this sequence.
   * @return the sequence size
   */
  public int size();

  /**
   * Returns an immutable {@code List} of the sequence x-values.
   * @return the {@code List} of x-values
   */
  public List<Double> xValues();

  /**
   * Returns an immutable {@code List} of the sequence y-values.
   * @return the {@code List} of y-values
   */
  public List<Double> yValues();

  /**
   * Returns an iterator over the {@link XyPoint}s in this sequence. For
   * immutable implementations, the {@link XyPoint#set(double)} method of a
   * returned point throws an {@code UnsupportedOperationException}.
   */
  public Iterator<XyPoint> iterator();

  /**
   * The first {@link XyPoint} in this sequence.
   */
  public XyPoint min();

  /**
   * The last {@link XyPoint} in this sequence.
   */
  public XyPoint max();

  /**
   * Returns {@code true} if all y-values are 0.0; {@code false} otherwise. a
   */
  public boolean isClear();

  /**
   * Returns a new, immutable sequence that has had all leading and trailing
   * zero-valued points ({@code y = 0}) removed. Any zero-valued points in the
   * middle of this sequence are ignored.
   * 
   * @throws IllegalStateException if {@link #isClear() this.isClear()} as empty
   *         sequences are not permitted
   */
  public XySequence trim();

  /**
   * Transforms all y-values in place using the supplied {@link Function}.
   *
   * @param function for transform
   * @return {@code this} sequence, for use inline
   */
  public default XySequence transform(Function<Double, Double> function) {
    throw new UnsupportedOperationException();
  }

}
