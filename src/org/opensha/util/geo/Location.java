package org.opensha.util.geo;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.opensha.util.Earthquakes.checkDepth;
import static org.opensha.util.geo.Coordinates.checkLatitude;
import static org.opensha.util.geo.Coordinates.checkLongitude;

import java.util.List;
import java.util.Objects;

import org.opensha.util.Maths;

import com.google.common.base.Converter;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.primitives.Doubles;

/**
 * A {@code Location} represents a point with reference to the earth's
 * ellipsoid. It is expressed in terms of longitude and latitude in decimal
 * degrees, and depth in km. As in seismology, the convention adopted here is
 * for depth to be positive-down, always. Locations may be defined using
 * longitude values in the range: [-180°, 360°]. Location instances are
 * immutable.
 *
 * <p>Note that constructors and static factory methods take arguments in the
 * order: {@code [lat, lon, depth]}, which is inconsistent with {@code String}
 * representations used in KML, GeoJSON, and other digital coordinate formats
 * that match standard plotting coordinate order: {@code [x, y, z]}.
 *
 * @author Peter Powers
 */
public final class Location implements Comparable<Location> {

  /*
   * Developer notes:
   * 
   * Historically, this class has variably stored lon-lat values in decimal
   * degrees or radians and then converted as needed for math operations and
   * string representations. However, such conversions can be lossy due to
   * double-precision operations, so it is prefereable to preserve both the
   * original decimal degrees values used to initialize the location along with
   * radian formatted values for computational efficiency.
   */

  /** The latitude of this {@code Location} in decimal degrees. */
  public final double latitude;

  /** The longitude of this {@code Location} in decimal degrees. */
  public final double longitude;

  /** The depth of this {@code Location} in kilometers. */
  public final double depth;

  final double latRad;
  final double lonRad;

  /**
   * Create a new {@code Location} with the supplied longitude, latitude, and
   * depth. This constructor is provided for backward compatibility but may be
   * removed in a future release. Please use {@link #create(double, double)}
   * instead.
   * 
   * @param latitude
   * @param longitude
   */
  public Location(double latitude, double longitude) {
    this(latitude, longitude, 0.0);
  }

  /**
   * Create a new {@code Location} with the supplied longitude, latitude, and
   * depth. This constructor is provided for backward compatibility but may be
   * removed in a future release. Please use
   * {@link #create(double, double, double)} instead.
   * 
   * @param latitude
   * @param longitude
   * @param depth
   */
  public Location(double latitude, double longitude, double depth) {
    this.latitude = checkLatitude(latitude);
    this.longitude = checkLongitude(longitude);
    this.depth = checkDepth(depth);
    this.lonRad = longitude * Maths.TO_RADIANS;
    this.latRad = latitude * Maths.TO_RADIANS;
  }

  /**
   * Create a new {@code Location} with the supplied longitude and latitude and
   * a depth of 0 km.
   *
   * @param latitude in decimal degrees
   * @param longitude in decimal degrees
   * @throws IllegalArgumentException if any supplied values are out of range
   * @see Coordinates
   */
  public static Location create(double latitude, double longitude) {
    return create(latitude, longitude, 0);
  }

  /**
   * Create a new {@code Location} with the supplied longitude, latitude, and
   * depth.
   *
   * @param latitude in decimal degrees
   * @param longitude in decimal degrees
   * @param depth in km (positive down)
   * @throws IllegalArgumentException if any supplied values are out of range
   * @see Coordinates
   */
  public static Location create(double latitude, double longitude, double depth) {
    return new Location(latitude, longitude, depth);
  }

  /**
   * Generate a new {@code Location} by parsing the supplied {@code String}.
   * Method is intended for use with the result of {@link #toString()}.
   *
   * @param s string to parse
   * @throws NumberFormatException if {@code s} is unparseable
   * @throws IndexOutOfBoundsException if {@code s} contains fewer than 3
   *         comma-separated values; any additional values in the suppied string
   *         are ignored
   * @see #toString()
   * @see #stringConverter()
   */
  public static Location fromString(String s) {
    return StringConverter.INSTANCE.reverse().convert(s);
  }

  /**
   * Return a KML and GeoJSON compatible tuple: {@code longitude,latitude,depth}
   * (no spaces).
   * 
   * @see #fromString(String)
   * @see #stringConverter()
   */
  @Override
  public String toString() {
    return StringConverter.INSTANCE.convert(this);
  }

  /**
   * Return a {@link Converter} that converts between {@code Location}s and
   * {@code String}s, preserving 5 decimal place precision.
   *
   * <p>Calls to {@code converter.reverse().convert(String)} will throw a
   * {@code NumberFormatException} if the values in the supplied string are
   * unparseable; or an {@code IndexOutOfBoundsException} if the supplied string
   * contains fewer than 3 comma-separated values; any additional values in the
   * supplied string are ignored.
   */
  public static Converter<Location, String> stringConverter() {
    return StringConverter.INSTANCE;
  }

  private static final class StringConverter extends Converter<Location, String> {

    static final StringConverter INSTANCE = new StringConverter();
    static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    static final String FORMAT = "%.5f,%.5f,%.5f";

    @Override
    protected String doForward(Location loc) {
      return String.format(FORMAT, loc.longitude, loc.latitude, loc.depth);
    }

    @Override
    protected Location doBackward(String s) {
      List<Double> values = FluentIterable
          .from(SPLITTER.split(checkNotNull(s)))
          .transform(Doubles.stringConverter())
          .toList();
      return create(values.get(1), values.get(0), values.get(2));
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Location)) {
      return false;
    }
    Location loc = (Location) obj;
    return this.longitude == loc.longitude &&
        this.latitude == loc.latitude &&
        this.depth == loc.depth;
  }

  @Override
  public int hashCode() {
    return Objects.hash(longitude, latitude, depth);
  }

  /**
   * Compare this {@code Location} to another and sort first by latitude, then
   * by longitude. When sorting a list of {@code Location}s, the resultant
   * ordering is left-to-right, bottom-to-top.
   *
   * @param loc {@code Location} to compare {@code this} to
   * @return a negative integer, zero, or a positive integer if this
   *         {@code Location} is less than, equal to, or greater than the
   *         specified {@code Location}.
   */
  @Override
  public int compareTo(Location loc) {
    double d = (latitude == loc.latitude)
        ? longitude - loc.longitude
        : latitude - loc.latitude;
    return (d != 0) ? (d < 0) ? -1 : 1 : 0;
  }

}
