package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static gov.usgs.earthquake.nshmp.data.DoubleData.checkInRange;
import static gov.usgs.earthquake.nshmp.geo.Locations.azimuth;
import static gov.usgs.earthquake.nshmp.geo.Locations.azimuthRad;

import com.google.common.collect.Range;

import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.geo.LocationList;
import gov.usgs.earthquake.nshmp.geo.Locations;

/**
 * Constants and utility methods pertaining to faults.
 *
 * @author Peter Powers
 */
public final class Faults {

  private Faults() {}

  /** Supported fault dips: {@code [0..90]°}. */
  public static final Range<Double> DIP_RANGE = Range.closed(0.0, 90.0);

  /** Supported fault rakes: {@code [-180..180]°}. */
  public static final Range<Double> RAKE_RANGE = Range.closed(-180.0, 180.0);

  /** Supported fault strikes: {@code [0..360)°}. */
  public static final Range<Double> STRIKE_RANGE = Range.closedOpen(0.0, 360.0);

  /**
   * Ensure {@code 0° ≤ dip ≤ 90°}.
   *
   * @param dip to validate
   * @return the validated dip
   * @throws IllegalArgumentException if {@code dip} is outside the range
   *         {@code [0..90]°}
   */
  public static double checkDip(double dip) {
    return checkInRange(DIP_RANGE, "Dip", dip);
  }

  /**
   * Ensure {@code 0° ≤ strike < 360°}.
   *
   * @param strike to validate
   * @return the validated strike
   * @throws IllegalArgumentException if {@code strike} is outside the range
   *         {@code [0..360)°}
   */
  public static double checkStrike(double strike) {
    return checkInRange(STRIKE_RANGE, "Strike", strike);
  }

  /**
   * Ensure {@code -180° ≤ rake ≤ 180°}.
   *
   * @param rake to validate
   * @return the validated rake
   * @throws IllegalArgumentException if {@code rake} is outside the range
   *         {@code [-180..180]°}
   */
  public static double checkRake(double rake) {
    return checkInRange(RAKE_RANGE, "Rake", rake);
  }

  /**
   * Ensure {@code trace} contains at least two points.
   *
   * @param trace to validate
   * @return the validated trace
   * @throws IllegalArgumentException if {@code trace.size() < 2}
   */
  public static LocationList checkTrace(LocationList trace) {
    checkArgument(trace.size() > 1, "Fault trace must have at least 2 points");
    return trace;
  }

  /*
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * TODO Everything below needs review
   */



  /**
   * Generic model for hypocentral depth returns a value that is halfway between
   * the top and bottom of a fault, parameterized by its dip, width, and depth.
   * This method performs no input validation.
   *
   * @param dip of the fault plane
   * @param width of the fault plane
   * @param zTop depth to the fault plane
   */
  public static double hypocentralDepth(double dip, double width, double zTop) {
    return zTop + Math.sin(dip * Maths.TO_RADIANS) * width / 2.0;
  }

  /**
   * Compute the strike in degrees of the supplied line, or trace, by connecting
   * the first and last points in {@code locs}. Method forwards to
   * {@link Locations#azimuth(Location, Location)}.
   *
   * <p>This approach has been shown to be as accurate as length-weighted angle
   * averaging and is significantly faster.
   *
   * @param locs line for which to compute strike
   * @return strike direction in the range [0°, 360°)
   * @see #strikeRad(LocationList)
   */
  public static double strike(LocationList locs) {
    return strike(locs.first(), locs.last());
  }

  /**
   * Compute the strike in degrees of the line connecting {@code p1} to
   * {@code p2}.
   * @param p1 starting {@code Location}
   * @param p2 ending {@code Location}
   * @return strike direction in the range [0°, 360°)
   * @see #strikeRad(Location, Location)
   */
  public static double strike(Location p1, Location p2) {
    return azimuth(p1, p2);
  }

  /**
   * Compute the strike in radians of the supplied line, or trace, by connecting
   * the first and last points in {@code locs}. Method forwards to
   * {@link Locations#azimuth(Location, Location)}.
   *
   * <p>This approach has been shown to be as accurate as length-weighted angle
   * averaging and is significantly faster.
   *
   * @param locs line for which to compute strike
   * @return strike direction in the range [0, 2π)
   * @see #strike(LocationList)
   */
  public static double strikeRad(LocationList locs) {
    return strikeRad(locs.first(), locs.last());
  }

  /**
   * Compute the strike in degrees of the line connecting {@code p1} to
   * {@code p2}.
   * @param p1 starting {@code Location}
   * @param p2 ending {@code Location}
   * @return strike direction in the range [0, 2π)
   * @see #strike(Location, Location)
   */
  public static double strikeRad(Location p1, Location p2) {
    return azimuthRad(p1, p2);
  }

  /**
   * Compute the dip direction for the supplied line/trace assuming the
   * right-hand rule (strike + 90°).
   *
   * @param locs line for which to compute dip direction
   * @return dip direction in the range 0° and 360°)
   */
  public static double dipDirection(LocationList locs) {
    return dipDirection(strike(locs));
  }

  public static double dipDirectionRad(LocationList locs) {
    return dipDirectionRad(strikeRad(locs));
  }

  public static double dipDirection(Location p1, Location p2) {
    return dipDirection(strike(p1, p2));
  }

  public static double dipDirectionRad(Location p1, Location p2) {
    return dipDirectionRad(strikeRad(p1, p2));
  }

  public static double dipDirection(double strike) {
    return (strike + 90.0) % 360.0;
  }

  public static double dipDirectionRad(double strikeRad) {
    return (strikeRad + Maths.PI_BY_2) % Maths.TWO_PI;
  }

}
