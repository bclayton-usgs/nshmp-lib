package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkArgument;
import static gov.usgs.earthquake.nshmp.data.DoubleData.areMonotonic;

import java.util.Map;

public class Sequences {

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
//  @Deprecated
//  public static XySequence resampleTo(XySequence sequence, double[] xs) {
//    // NOTE TODO this will support mfd combining
//    checkNotNull(sequence);
//    checkArgument(checkNotNull(xs).length > 0);
//
//    Interpolator interpolator = Interpolator.builder()
//        .build();
//
//    double[] yResample = interpolator.findY(sequence.xValues(), sequence.yValues(), xs);
//
//    // TODO disable extrapolation
//    if (true) {
//      throw new UnsupportedOperationException();
//    }
//    return MutableXySequence.create(xs, yResample);
//  }

  /**
   * Adds {@code this} sequence to any exisiting sequence for {@code key} in the
   * supplied {@code map}. If {@code key} does not exist in the {@code map},
   * method puts a mutable copy of {@code this} in the map.
   * 
   * @param key for sequence to add
   * @param map of sequences to add to
   * @throws IllegalArgumentException if the x-values of added sequences to not
   *         match those of existing sequences
   */
  public static <E extends Enum<E>> void addToMap(
      E key,
      Map<E, MutableXySequence> map,
      XySequence sequence) {

    if (map.containsKey(key)) {
      map.get(key).add(sequence);
    } else {
      map.put(key, (MutableXySequence) XySequence.copyOf(sequence));
    }
  }

//  static XySequence create(
//      Collection<? extends Number> xs,
//      Collection<? extends Number> ys) {
//
//    double[] xArray = Doubles.toArray(xs);
//    double[] yArray = Doubles.toArray(ys);
//    validateSequenceArrays(xArray, yArray);
//    return new ArrayXySequence(xArray, yArray);
//  }
//
//  static MutableXySequence createMutable(
//      Collection<? extends Number> xs,
//      Collection<? extends Number> ys) {
//
//    double[] xArray = Doubles.toArray(xs);
//    double[] yArray = Doubles.toArray(ys);
//    return createMutable(
//        Doubles.toArray(xs),
//        (ys == null) ? null : Doubles.toArray(ys));
//  }

//  static XySequence create(
//      double[] xs,
//      double[] ys) {
//
//    double[] xArray = Arrays.copyOf(xs, xs.length);
//    double[] yArray = Arrays.copyOf(ys, ys.length);
//    validateSequenceArrays(xArray, yArray);
//    return new ArrayXySequence(xArray, yArray);
//  }
  

  /* Assumes array copies have already been created. */
  static XySequence construct(
      double[] xs,
      double[] ys) {
    validateArrays(xs, ys);
    return new ArrayXySequence(xs, ys);
  }

  /* Assumes array copies have already been created. */
  static MutableArrayXySequence constructMutable(
      double[] xs,
      double[] ys) {
    validateArrays(xs, ys);
    return new MutableArrayXySequence(xs, ys);
  }

//  static MutableXySequence createMutable(
//      double[] xs,
//      double[] ys) {
//
//    double[] xArray = Arrays.copyOf(xs, xs.length);
//    double[] yArray = (ys == null)
//        ? new double[xs.length]
//        : Arrays.copyOf(ys, ys.length);
//    validate(xArray, yArray);
//    return new MutableArrayXySequence(xArray, yArray);
//  }

  static void validateArrays(double[] xs, double[] ys) {
    checkArgument(xs.length > 0, "x-values may not be empty");
    checkArgument(xs.length == ys.length, "x- and y-values are different sizes");
    if (xs.length > 1) {
      checkArgument(areMonotonic(true, true, xs), "x-values do not increase monotonically");
    }
  }

  static XySequence construct(double[] xs, double[] ys, boolean mutable) {
    return mutable ? new MutableArrayXySequence(xs, ys) : new ArrayXySequence(xs, ys);
  }

}
