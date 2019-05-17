package org.opensha.util.geo;

import java.util.Arrays;
import java.util.Objects;

/**
 * A rectangular (in Mercator projection) bounding box specified by a lower-left
 * coordinate ({@link #min}) and an upper-right coordinate ({@link #max}).
 *
 * <p>Bounds are 2-dimensional in that the depth component of the corners will
 * always be 0. The bounds of any {@code Iterable<Location>} may be computed
 * using {@link Locations#bounds(Iterable)}.
 *
 * @author Peter Powers
 */
public final class Bounds {

  /** The lower left coordinate (minimum longitude and latatide). */
  public final Location min;

  /** The upper right coordinate (maximum longitude and latitude). */
  public final Location max;

  /*
   * Bounds creation is restricted to the geo package so the burden of checking
   * that min and max are actually min and max lies with classes that create
   * bounds objects.
   */

  Bounds(Location min, Location max) {
    this.min = min;
    this.max = max;
  }

  Bounds(double minLon, double minLat, double maxLon, double maxLat) {
    this(Location.create(minLat, minLon), Location.create(maxLat, maxLon));
  }

  /**
   * Return this {@code Bounds} as a {@code LocationList} of five vertices,
   * starting with {@link #min} and winding counter-clockwise.
   */
  public LocationList toList() {
    return LocationList.create(
        min,
        Location.create(min.latitude, max.longitude),
        max,
        Location.create(max.latitude, min.longitude),
        min);
  }

  /**
   * Return the values of this {@code Bounds} object in the form
   * {@code [min.longitude, min.latitude, max.longitude, max.latitude]}.
   */
  public double[] toArray() {
    return new double[] {
        min.longitude,
        min.latitude,
        max.longitude,
        max.latitude
    };
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Bounds)) {
      return false;
    }
    Bounds other = (Bounds) obj;
    if (min == other.min && max == other.max) {
      return true;
    }
    return min.equals(other.min) && max.equals(other.max);
  }

  @Override
  public int hashCode() {
    return Objects.hash(min, max);
  }

  /**
   * Returns the string representation of {@link #toArray()}.
   */
  @Override
  public String toString() {
    return Arrays.toString(toArray());
  }

}
