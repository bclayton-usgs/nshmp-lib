package org.opensha.util.geo;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.opensha.util.Maths.TO_DEGREES;
import static org.opensha.util.Maths.TWO_PI;

import com.google.common.base.MoreObjects;

/**
 * This utility class encapsulates information describing a vector between two
 * {@code Location}s. Internally, the vector is defined by the azimuth (bearing)
 * and the horizontal and vertical separation between the points. Note that a
 * {@code LocationVector} from point A to point B is not the complement of that
 * from point B to A. Although the horizontal and vertical components will be
 * the same, the azimuth will likely change by some value other than 180°.
 *
 * <p>Although a {@code LocationVector} will function in any reference frame,
 * the convention in seismology and that adopted here is for depth to be
 * positive down.
 *
 * @author Peter Powers
 */
public final class LocationVector {

  /** The azimuth of this vector in <i>radians</i>. */
  public final double azimuth;

  /** The horizontal component of this vector. */
  public final double Δh;

  /** The vertical component of this vector. */
  public final double Δv;

  /*
   * TODO: add error checking and perhaps unckecked package static initializers
   */

  private LocationVector(double azimuth, double Δh, double Δv) {
    this.azimuth = azimuth;
    this.Δh = Δh;
    this.Δv = Δv;
  }

  /**
   * Initializes a new {@code LocationVector} with the supplied values. Note
   * that {@code azimuth} is expected in <i>radians</i>.
   *
   * @param azimuth to set in <i>radians</i>
   * @param horizontal component to set in km
   * @param vertical component to set in km
   * @return a new {@code LocationVector}
   */
  public static LocationVector create(double azimuth, double horizontal, double vertical) {
    return new LocationVector(azimuth, horizontal, vertical);
  }

  /**
   * Creates a new {@code LocationVector} with horizontal and vertical
   * components derived from the supplied {@code plunge} and {@code length}.
   * Note that {@code azimuth} and {@code plunge} are expected in
   * <i>radians</i>.
   *
   * @param azimuth to set in <i>radians</i>
   * @param plunge to set in <i>radians</i>
   * @param length of vector in km
   * @return a new {@code LocationVector}
   */
  public static LocationVector createWithPlunge(double azimuth, double plunge, double length) {
    return create(azimuth, length * cos(plunge), length * sin(plunge));
  }

  /**
   * Returns the {@code LocationVector} describing the move from one
   * {@code Location} to another.
   *
   * @param p1 the first {@code Location}
   * @param p2 the second {@code Location}
   * @return a new {@code LocationVector}
   */
  public static LocationVector create(Location p1, Location p2) {
    // NOTE A 'fast' implementation of this method was tested
    // but no performance gain was realized P.Powers 3-5-2010
    return create(
        Locations.azimuthRad(p1, p2),
        Locations.horzDistance(p1, p2),
        Locations.vertDistance(p1, p2));
  }

  /**
   * The angle (in radians) between this vector and a horizontal plane. This
   * method is intended for use at relatively short separations (e.g. ≤200km);
   * it degrades at large distances because curvature is not considered. Note
   * that positive angles are down, negative angles are up.
   * @return the plunge of this vector
   */
  public double plunge() {
    return atan(Δv / Δh);
  }

  /**
   * Returns a copy of the supplied vector with azimuth and vertical components
   * reversed.
   *
   * <p><b>Note</b>: create(p1, p2) is not equivalent to create
   * reverseOf(create(p2, p1)). Although the horizontal and vertical components
   * will likley be the same but the azimuths will potentially be different.
   *
   * @param v {@code LocationVector} to copy and flip
   * @return the flipped copy
   */
  public static LocationVector reverseOf(LocationVector v) {
    return create((v.azimuth + PI) % TWO_PI, v.Δh, -v.Δv);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("az", azimuth * TO_DEGREES)
        .add("Δh", Δh)
        .add("Δv", Δv)
        .toString();
  }

}
