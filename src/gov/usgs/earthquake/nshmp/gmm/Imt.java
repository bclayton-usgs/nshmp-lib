package gov.usgs.earthquake.nshmp.gmm;

import static com.google.common.math.DoubleMath.fuzzyEquals;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * Intesity measure type (Imt) identifiers. {@code SA0P1} stands for spectal
 * acceleration of 0.1 seconds.
 * @author Peter Powers
 */
@SuppressWarnings("javadoc")
public enum Imt {

  PGA,
  PGV,
  PGD,
  ASI,
  SI,
  DSI,
  CAV,
  DS575,
  DS595,
  AI,
  SA0P01,
  SA0P02,
  SA0P025,
  SA0P03,
  SA0P04,
  SA0P05,
  SA0P06,
  SA0P07,
  SA0P075,
  SA0P08,
  SA0P09,
  SA0P1,
  SA0P12,
  SA0P14,
  SA0P15,
  SA0P16,
  SA0P17,
  SA0P18,
  SA0P2,
  SA0P25,
  SA0P3,
  SA0P35,
  SA0P4,
  SA0P45,
  SA0P5,
  SA0P6,
  SA0P7,
  SA0P75,
  SA0P8,
  SA0P9,
  SA1P0,
  SA1P25,
  SA1P5,
  SA2P0,
  SA2P5,
  SA3P0,
  SA3P5,
  SA4P0,
  SA4P5,
  SA5P0,
  SA6P0,
  SA7P5,
  SA10P0;

  private static final DecimalFormat SA_FORMAT = new DecimalFormat("0.00#");

  @Override
  public String toString() {
    switch (this) {
      case PGA:
        return "Peak Ground Acceleration";
      case PGV:
        return "Peak Ground Velocity";
      case PGD:
        return "Peak Ground Displacement";
      case AI:
        return "Arias Intensity";
      case ASI:
        return "Acceleration Spectrum Intensity";
      case DSI:
        return "Displacement Spectrum Intensity";
      case SI:
        return "Spectrum intensity";
      case CAV:
        return "Cumulative Absolute Velocity";
      case DS575:
        return "Significant Duration 5-75%";
      case DS595:
        return "Significant Duration 5-95%";
      default:
        return SA_FORMAT.format(period()) + " Second Spectral Acceleration";
    }
  }

  /**
   * Return the units in which this {@code Imt} is measured.
   */
  public String units() {
    switch (this) {
      case PGA:
        return "g";
      case PGV:
        return "cm/s";
      case PGD:
        return "cm";
      case AI:
        return "m/s";
      case ASI:
        return "g⋅s";
      case DSI:
        return "cm⋅s";
      case SI:
        return "cm⋅s/s";
      case CAV:
        return "g⋅s";
      case DS575:
        return "s";
      case DS595:
        return "s";
      default:
        return "g";
    }
  }

  /**
   * Returns the corresponding period or frequency for this {@code Imt} if it
   * represents a spectral acceleration.
   * @return the period for this {@code Imt} if it represents a spectral
   *         acceleration, {@code null} otherwise
   */
  public Double period() {
    // TODO should this throw an IAE instead or return null?
    if (ordinal() < 10) {
      return null;
    }
    String valStr = name().substring(2).replace("P", ".");
    return Double.parseDouble(valStr);
  }

  /**
   * Returns the {@code List} of periods for the supplied {@code Imt}s. The
   * result will be sorted according to the iteration order of the supplied
   * {@code Collection}. Any non spectral acceleration {@code Imt}s will have
   * null values in the returned {@code List}.
   *
   * @param imts to list periods for
   * @return a {@code List} of spectral periods
   * @see #saImts()
   */
  public static List<Double> periods(Collection<Imt> imts) {
    List<Double> periodList = Lists.newArrayListWithCapacity(imts.size());
    for (Imt imt : imts) {
      periodList.add(imt.period());
    }
    return periodList;
  }

  /**
   * Returns the spectral acceleration {@code Imt} associated with the supplied
   * period. Due to potential floating point precision problems, this method
   * internally checks values to within a small tolerance.
   * @param period for {@code Imt}
   * @return an {@code Imt}, or {@code null} if no Imt exsists for the supplied
   *         period
   */
  public static Imt fromPeriod(double period) {
    for (Imt imt : Imt.values()) {
      if (imt.name().startsWith("SA")) {
        double saPeriod = imt.period();
        if (fuzzyEquals(saPeriod, period, 0.000001)) {
          return imt;
        }
      }
    }
    throw new IllegalArgumentException("No corresponding Imt for period:" + period);
  }

  /**
   * Returns the frequency (in Hz) for this {@code Imt}. {@code PGA} returns 100
   * Hz, spectral periods return their expected value (1 / period), and
   * {@code PGV} and {@code PGD} throw exceptions.
   * @return the frequency associated with this {@code Imt}
   * @throws UnsupportedOperationException if called on {@code PGV} or
   *         {@code PGD}
   */
  public double frequency() {
    if (this == PGA) {
      return 100;
    }
    if (this.isSA()) {
      return 1.0 / period();
    }
    throw new UnsupportedOperationException("frequncy() not supported for PGD, PGV, etc…");
  }

  /**
   * Returns true if this Imt is some flavor of spectral acceleration.
   * @return {@code true} if this is a spectral period, {@code false} otherwise
   */
  public boolean isSA() {
    return ordinal() > 9;
  }

  /**
   * Returns the {@code Set} of spectal acceleration IMTs.
   * @return the IMTs that represent spectral accelerations
   */
  public static Set<Imt> saImts() {
    return EnumSet.complementOf(EnumSet.range(PGA, AI));
  }

}
