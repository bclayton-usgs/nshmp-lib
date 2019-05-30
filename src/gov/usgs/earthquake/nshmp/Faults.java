package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static gov.usgs.earthquake.nshmp.data.DoubleData.checkInRange;

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

  /**
   * Generic model for hypocentral depth returns a value that is halfway between
   * the top and bottom of a fault, parameterized by its dip, width, and depth.
   * This method performs no input validation.
   *
   * @param dip of the fault plane in decimal degrees
   * @param width of the fault plane
   * @param zTop depth to the fault plane (positive down)
   */
  public static double hypocentralDepth(double dip, double width, double zTop) {
    return zTop + Math.sin(dip * Maths.TO_RADIANS) * width / 2.0;
  }

  /**
   * Compute the strike in decimal degrees of the supplied fault trace by
   * connecting the endpoints. This approach has been shown to be as accurate as
   * length-weighted angle averaging and is significantly faster.
   *
   * @param trace for which to compute strike
   * @return strike direction in the range [0°..360°)
   * @see #strikeRad(LocationList)
   */
  public static double strike(LocationList trace) {
    return strike(trace.first(), trace.last());
  }

  /**
   * Compute the strike in decimal degrees of the line connecting the supplied
   * locations.
   * 
   * @param start location
   * @param end location
   * @return strike direction in the range [0°..360°)
   * @see #strikeRad(Location, Location)
   */
  public static double strike(Location start, Location end) {
    return Locations.azimuth(start, end);
  }

  /**
   * Compute the strike in radians of the supplied fault trace by connecting the
   * endpoints. This approach has been shown to be as accurate as
   * length-weighted angle averaging and is significantly faster.
   *
   * @param trace for which to compute strike
   * @return strike direction in the range [0..2π)
   * @see #strike(LocationList)
   */
  public static double strikeRad(LocationList trace) {
    return strikeRad(trace.first(), trace.last());
  }

  /**
   * Compute the strike in radians of the line connecting the supplied
   * locations.
   * 
   * @param start location
   * @param end location
   * @return strike direction in the range [0..2π)
   * @see #strike(Location, Location)
   */
  public static double strikeRad(Location start, Location end) {
    return Locations.azimuthRad(start, end);
  }

  /**
   * Compute the dip direction in decimal degrees for the supplied fault trace
   * assuming the right-hand rule (strike + 90°).
   *
   * @param trace for which to compute dip direction
   * @return dip direction in the range [0°..360°)
   * @see #dipDirectionRad(LocationList)
   */
  public static double dipDirection(LocationList trace) {
    return dipDirection(strike(trace));
  }

  /**
   * Compute the dip direction in decimal degrees of the line connecting the supplied
   * locations assuming the right-hand rule (strike + 90°).
   * 
   * @param start location
   * @param end location
   * @return dip direction in the range [0°..360°)
   * @see #dipDirectionRad(Location, Location)
   */
  public static double dipDirection(Location start, Location end) {
    return dipDirection(strike(start, end));
  }

  /**
   * Compute the dip direction in radians for the supplied fault trace assuming
   * the right-hand rule (strike + π/2).
   *
   * @param trace for which to compute dip direction
   * @return dip direction in the range [0..2π)
   * @see #dipDirection(LocationList)
   */
  public static double dipDirectionRad(LocationList trace) {
    return dipDirectionRad(strikeRad(trace));
  }

  /**
   * Compute the dip direction in radians of the line connecting the supplied
   * locations assuming the right-hand rule (strike + π/2).
   * 
   * @param start location
   * @param end location
   * @return dip direction in the range [0..2π)
   * @see #dipDirection(Location, Location)
   */
  public static double dipDirectionRad(Location start, Location end) {
    return dipDirectionRad(strikeRad(start, end));
  }

  /**
   * Compute the dip direction for the supplied strike (strike + 90°).
   * 
   * @param strike (in decimal degrees) for which to compute dip direction
   * @return dip direction in the range [0°..360°)
   */
  public static double dipDirection(double strike) {
    return (strike + 90.0) % 360.0;
  }

  /**
   * Compute the dip direction for the supplied strike (strike + π/2).
   * 
   * @param strike (in radians) for which to compute dip direction
   * @return dip direction in the range [0°..360°)
   */
  public static double dipDirectionRad(double strike) {
    return (strike + Maths.PI_BY_2) % Maths.TWO_PI;
  }

}
