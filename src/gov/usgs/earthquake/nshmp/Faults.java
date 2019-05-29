package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static gov.usgs.earthquake.nshmp.data.DoubleData.checkInRange;
import static gov.usgs.earthquake.nshmp.geo.Locations.azimuth;
import static gov.usgs.earthquake.nshmp.geo.Locations.azimuthRad;
import static gov.usgs.earthquake.nshmp.geo.Locations.horzDistance;
import static gov.usgs.earthquake.nshmp.geo.Locations.linearDistanceFast;
import static gov.usgs.earthquake.nshmp.geo.Locations.location;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.geo.LocationList;
import gov.usgs.earthquake.nshmp.geo.LocationVector;
import gov.usgs.earthquake.nshmp.geo.Locations;
import gov.usgs.earthquake.nshmp.Maths;

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
   * This subdivides the given fault trace into sub-traces that have the length
   * as given (or less). This assumes all fault trace points are at the same
   * depth.
   * @param faultTrace
   * @param maxSubSectionLen Maximum length of each subsection
   * @return a {@code List} of subsection traces
   */
  public static List<LocationList> getEqualLengthSubsectionTraces(LocationList faultTrace,
      double maxSubSectionLen) {
    return getEqualLengthSubsectionTraces(faultTrace, maxSubSectionLen, 1);
  }

  /**
   * This subdivides the given fault trace into sub-traces that have the length
   * as given (or less). This assumes all fault trace points are at the same
   * depth.
   * @param faultTrace
   * @param maxSubSectionLen Maximum length of each subsection
   * @param minSubSections minimum number of sub sections to generate
   * @return a {@code List} of subsection traces
   */
  public static List<LocationList> getEqualLengthSubsectionTraces(LocationList faultTrace,
      double maxSubSectionLen, int minSubSections) {

    int numSubSections;
    List<LocationList> subSectionTraceList;

    // find the number of sub sections
    double numSubSec = faultTrace.length() / maxSubSectionLen;
    if (Math.floor(numSubSec) != numSubSec) {
      numSubSections = (int) Math.floor(numSubSec) + 1;
    } else {
      numSubSections = (int) numSubSec;
    }
    if (numSubSections < minSubSections) {
      numSubSections = minSubSections;
    }
    // find the length of each sub section
    double subSecLength = faultTrace.length() / numSubSections;
    double distance = 0, distLocs = 0;
    int numLocs = faultTrace.size();
    int index = 0;
    subSectionTraceList = Lists.newArrayList();
    Location prevLoc = faultTrace.get(index);
    while (index < numLocs && subSectionTraceList.size() < numSubSections) {
      // FaultTrace subSectionTrace = new
      // FaultTrace(faultTrace.name()+" "+(subSectionTraceList.size()+1));
      List<Location> subSectionLocs = Lists.newArrayList();
      subSectionLocs.add(prevLoc); // the first location
      ++index;
      distance = 0;
      while (true && index < faultTrace.size()) {
        Location nextLoc = faultTrace.get(index);
        distLocs = horzDistance(prevLoc, nextLoc);
        distance += distLocs;
        if (distance < subSecLength) { // if sub section length is
          // greater than distance, then
          // get next point on trace
          prevLoc = nextLoc;
          subSectionLocs.add(prevLoc);
          ++index;
        } else {
          // LocationVector direction = vector(prevLoc, nextLoc);
          // direction.setHorzDistance(subSecLength -
          // (distance - distLocs));
          LocationVector dirSrc = LocationVector.create(prevLoc, nextLoc);
          double hDist = subSecLength - (distance - distLocs);
          LocationVector direction = LocationVector.create(dirSrc.azimuth, hDist,
              dirSrc.Δv);
          prevLoc = location(prevLoc, direction);
          subSectionLocs.add(prevLoc);
          --index;
          break;
        }
      }
      // TODO is name used in subTraces? Can we name traces after
      // returning list
      // String subsectionName = faultTrace.name() + " " +
      // (subSectionTraceList.size() + 1);
      // LocationList subSectionTrace =
      // LocationList.create(subsectionName,
      // LocationList.create(subSectionLocs));
      LocationList subSectionTrace = LocationList.copyOf(subSectionLocs);
      subSectionTraceList.add(subSectionTrace);
    }
    return subSectionTraceList;
  }

  /**
   * This resamples the trace into num subsections of equal length (final number
   * of points in trace is num+1). However, note that these subsections of are
   * equal length on the original trace, and that the final subsections will be
   * less than that if there is curvature in the original between the points
   * (e.g., corners getting cut).
   * @param trace
   * @param num - number of subsections
   */
  public static LocationList resampleTrace(LocationList trace, int num) {
    double resampInt = trace.length() / num;
    // FaultTrace resampTrace = new FaultTrace("resampled "+trace.name());
    List<Location> resampLocs = Lists.newArrayList();
    resampLocs.add(trace.first()); // add the first location
    double remainingLength = resampInt;
    Location lastLoc = trace.first();
    int NextLocIndex = 1;
    while (NextLocIndex < trace.size()) {
      Location nextLoc = trace.get(NextLocIndex);
      double length = linearDistanceFast(lastLoc, nextLoc);
      if (length > remainingLength) {
        // set the point
        // LocationVector dir = vector(lastLoc, nextLoc);
        // dir.setHorzDistance(dir.getHorzDistance() * remainingLength /
        // length);
        // dir.setVertDistance(dir.getVertDistance() * remainingLength /
        // length);
        LocationVector dirSrc = LocationVector.create(lastLoc, nextLoc);
        double hDist = dirSrc.Δh * remainingLength / length;
        double vDist = dirSrc.Δv * remainingLength / length;

        LocationVector dir = LocationVector.create(dirSrc.azimuth, hDist, vDist);
        Location loc = location(lastLoc, dir);
        resampLocs.add(loc);
        lastLoc = loc;
        remainingLength = resampInt;
        // Next location stays the same
      } else {
        lastLoc = nextLoc;
        NextLocIndex += 1;
        remainingLength -= length;
      }
    }

    // make sure we got the last one (might be missed because of numerical
    // precision issues?)
    double dist = linearDistanceFast(trace.last(), resampLocs.get(resampLocs.size() - 1));
    if (dist > resampInt / 2) {
      resampLocs.add(trace.last());
    }

    /* Debugging Stuff **************** */
    /*
     * // write out each to check System.out.println("RESAMPLED"); for(int i=0;
     * i<resampTrace.size(); i++) { Location l = resampTrace.getLocationAt(i);
     * System.out.println(l.getLatitude()+"\t"+
     * l.getLongitude()+"\t"+l.getDepth()); }
     *
     * System.out.println("ORIGINAL"); for(int i=0; i<trace.size(); i++) {
     * Location l = trace.getLocationAt(i); System.out.println(l.getLatitude(
     * )+"\t"+l.getLongitude()+"\t"+l.getDepth()); }
     *
     * // write out each to check System.out.println("target resampInt="
     * +resampInt+"\tnum sect="+num); System.out.println("RESAMPLED"); double
     * ave=0, min=Double.MAX_VALUE, max=Double.MIN_VALUE; for(int i=1;
     * i<resampTrace.size(); i++) { double d =
     * Locations.getTotalDistance(resampTrace.getLocationAt(i-1),
     * resampTrace.getLocationAt(i)); ave +=d; if(d<min) min=d; if(d>max) max=d;
     * } ave /= resampTrace.size()-1; System.out.println("ave="+ave+"\tmin="
     * +min+"\tmax="+max+"\tnum pts=" +resampTrace.size());
     *
     *
     * System.out.println("ORIGINAL"); ave=0; min=Double.MAX_VALUE;
     * max=Double.MIN_VALUE; for(int i=1; i<trace.size(); i++) { double d =
     * Locations.getTotalDistance(trace.getLocationAt(i-1),
     * trace.getLocationAt(i)); ave +=d; if(d<min) min=d; if(d>max) max=d; } ave
     * /= trace.size()-1; System.out.println("ave="+ave+"\tmin="+min+"\tmax="
     * +max+"\tnum pts=" +trace.size());
     *
     * /* End of debugging stuff *******************
     */

    // TODO is resampled trace name used? can't it be acquired from a
    // wrapping source?
    // return FaultTrace.create("resampled " + trace.name(),
    // LocationList.create(resampLocs));
    return LocationList.copyOf(resampLocs);
  }

  /**
   * Returns an average of the given angles scaled by the distances between the
   * corresponding locations. Note that this expects angles in degrees, and will
   * return angles from 0 to 360 degrees.
   *
   * @param locs locations for distance scaling
   * @param angles angles in degrees corresponding to each pair of locations
   */
  public static double getLengthBasedAngleAverage(LocationList locs, List<Double> angles) {
    Preconditions.checkArgument(locs.size() >= 2, "must have at least 2 locations!");
    Preconditions.checkArgument(angles.size() == locs.size() - 1,
        "must have exactly one fewer angles than location");

    ArrayList<Double> lengths = new ArrayList<Double>();

    for (int i = 1; i < locs.size(); i++) {
      lengths.add(linearDistanceFast(locs.get(i), locs.get(i - 1)));
    }

    return getScaledAngleAverage(lengths, angles);
  }

  /**
   * Returns an average of the given angles scaled by the given scalars. Note
   * that this expects angles in degrees, and will return angles from 0 to 360
   * degrees.
   *
   * @param scalars scalar weights for each angle (does not need to be
   *        normalized)
   * @param angles angles in degrees corresponding to each pair of locations
   */
  public static double getScaledAngleAverage(List<Double> scalars, List<Double> angles) {
    Preconditions.checkArgument(scalars.size() >= 1, "must have at least 1 lengths!");
    Preconditions.checkArgument(angles.size() == scalars.size(),
        "must have exactly the same amount of lengths as angles");

    // see if we have an easy case, or a NaN
    if (angles.size() == 1) {
      return angles.get(0);
    }
    if (Double.isNaN(angles.get(0))) {
      return Double.NaN;
    }
    boolean equal = true;
    for (int i = 1; i < angles.size(); i++) {
      if (Double.isNaN(angles.get(i))) {
        return Double.NaN;
      }
      if (angles.get(i) != angles.get(0)) {
        equal = false;
      }
    }
    if (equal) {
      return angles.get(0);
    }

    double xdir = 0;
    double ydir = 0;
    for (int i = 0; i < scalars.size(); i++) {
      double scalar = scalars.get(i);
      double angle = angles.get(i);
      xdir += scalar * Math.cos(Math.toRadians(angle));
      ydir += scalar * Math.sin(Math.toRadians(angle));
    }

    double avg;

    if (xdir > 0 & ydir >= 0) {
      avg = Math.toDegrees(Math.atan(ydir / xdir));
    } else if (xdir > 0 & ydir < 0) {
      avg = Math.toDegrees(Math.atan(ydir / xdir)) + 360;
    } else if (xdir < 0) {
      avg = Math.toDegrees(Math.atan(ydir / xdir)) + 180;
    } else if (xdir == 0 & ydir > 0) {
      avg = 90;
    } else if (xdir == 0 & ydir < 0) {
      avg = 270;
    } else {
      avg = 0; // if both xdir==0 & ydir=0
    }

    while (avg > 360) {
      avg -= 360;
    }
    while (avg < 0) {
      avg += 360;
    }

    return avg;
  }

  /**
   * Averages angles dealing with any -180/180 or 0/360 cut issues. Note that
   * this expects angles in degrees, and will return angles from 0 to 360
   * degrees.
   *
   * @param angles
   */
  public static double getAngleAverage(List<Double> angles) {
    ArrayList<Double> scalars = new ArrayList<Double>();
    for (int i = 0; i < angles.size(); i++) {
      scalars.add(1d);
    }
    return getScaledAngleAverage(scalars, angles);
  }

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
   * averaging and is significantly faster; see <a
   * href="https://opensha.org/trac/wiki/StrikeDirectionMethods"
   * target="_top">StrikeDirectionMethods</a> for more information.
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
   * averaging and is significantly faster; see <a
   * href="https://opensha.org/trac/wiki/StrikeDirectionMethods"
   * target="_top">StrikeDirectionMethods</a> for more information.
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

  /*
   * This returns the average strike (weight average by length). public double
   * getAveStrike() { ArrayList<Double> azimuths = new ArrayList<Double>(); for
   * (int i = 1; i < size(); i++) { azimuths.add(Locations.azimuth(get(i - 1),
   * get(i))); } return Faults.getLengthBasedAngleAverage(this, azimuths); }
   */

  /**
   * Returns the dip direction for the supplied line/trace assuming the
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

  /* <b>x</b>-axis unit normal vector [1,0,0] */
  private static final double[] VX_UNIT_NORMAL = { 1.0, 0.0, 0.0 };
  /* <b>y</b>-axis unit normal vector [0,1,0] */
  private static final double[] VY_UNIT_NORMAL = { 0.0, 1.0, 0.0 };
  /* <b>z</b>-axis unit normal vector [0,0,1] */
  private static final double[] VZ_UNIT_NORMAL = { 0.0, 0.0, 1.0 };

  /**
   * Calculates a slip vector from strike, dip, and rake information provided.
   * @param strikeDipRake array
   * @return double[x,y,z] array for slip vector.
   */
  public static double[] getSlipVector(double[] strikeDipRake) {
    // start with y-axis unit normal on a horizontal plane
    double[] startVector = VY_UNIT_NORMAL;
    // rotate rake amount about z-axis (negative axial rotation)
    double[] rakeRotVector = vectorMatrixMultiply(zAxisRotMatrix(-strikeDipRake[2]),
        startVector);
    // rotate dip amount about y-axis (negative axial rotation)
    double[] dipRotVector = vectorMatrixMultiply(yAxisRotMatrix(-strikeDipRake[1]),
        rakeRotVector);
    // rotate strike amount about z-axis (positive axial rotation)
    double[] strikeRotVector = vectorMatrixMultiply(zAxisRotMatrix(strikeDipRake[0]),
        dipRotVector);
    return strikeRotVector;
  }

  /*
   * Multiplies the vector provided with a matrix. Useful for rotations.
   *
   * @param matrix double[][] matrix (likely one of the rotation matrices from
   * this class).
   *
   * @param vector double[x,y,z] to be modified.
   */
  private static double[] vectorMatrixMultiply(double[][] matrix, double[] vector) {
    double[] rotatedVector = new double[3];
    for (int i = 0; i < 3; i++) {
      rotatedVector[i] = vector[0] * matrix[i][0] + vector[1] * matrix[i][1] + vector[2] *
          matrix[i][2];
    }
    return rotatedVector;
  }

  /*
   * Returns a rotation matrix about the x axis in a right-handed coordinate
   * system for a given theta. Note that these are coordinate transformations
   * and that a positive (anticlockwise) rotation of a vector is the same as a
   * negative rotation of the reference frame.
   *
   * @param theta axial rotation in degrees.
   *
   * @return double[][] rotation matrix.
   */
  private static double[][] xAxisRotMatrix(double theta) {
    // @formatter:off
    double thetaRad = Math.toRadians(theta);
    double[][] rotMatrix= {{ 1.0 ,                 0.0 ,                0.0 },
        { 0.0 ,  Math.cos(thetaRad) , Math.sin(thetaRad) },
        { 0.0 , -Math.sin(thetaRad) , Math.cos(thetaRad) }};
    return rotMatrix;
    // @formatter:on
  }

  /*
   * Returns a rotation matrix about the y axis in a right-handed coordinate
   * system for a given theta. Note that these are coordinate transformations
   * and that a positive (anticlockwise) rotation of a vector is the same as a
   * negative rotation of the reference frame.
   *
   * @param theta axial rotation in degrees.
   *
   * @return double[][] rotation matrix.
   */
  private static double[][] yAxisRotMatrix(double theta) {
    // @formatter:off
    double thetaRad = Math.toRadians(theta);
    double[][] rotMatrix= {{ Math.cos(thetaRad) , 0.0 , -Math.sin(thetaRad) },
        {                0.0 , 1.0 ,                 0.0 },
        { Math.sin(thetaRad) , 0.0 ,  Math.cos(thetaRad) }};
    return rotMatrix;
    // @formatter:on
  }

  /*
   * Returns a rotation matrix about the z axis in a right-handed coordinate
   * system for a given theta. Note that these are coordinate transformations
   * and that a positive (anticlockwise) rotation of a vector is the same as a
   * negative rotation of the reference frame.
   *
   * @param theta axial rotation in degrees.
   *
   * @return double[][] rotation matrix.
   */
  private static double[][] zAxisRotMatrix(double theta) {
    // @formatter:off
    double thetaRad = Math.toRadians(theta);
    double[][] rotMatrix= {{  Math.cos(thetaRad) , Math.sin(thetaRad) , 0.0 },
        { -Math.sin(thetaRad) , Math.cos(thetaRad) , 0.0 },
        {                 0.0 ,                0.0 , 1.0 }};
    return rotMatrix;
    // @formatter:on
  }

}
