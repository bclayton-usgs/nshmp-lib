package gov.usgs.earthquake.nshmp.eq.model;

import static gov.usgs.earthquake.nshmp.Faults.checkRake;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import gov.usgs.earthquake.nshmp.eq.fault.surface.RuptureSurface;
import gov.usgs.earthquake.nshmp.geo.Location;

/**
 * A {@code Rupture} is a proxy for an actual earthquake and encapsulates all
 * the source information required by a ground motion model (Gmm).
 *
 * @author Peter Powers
 */
public class Rupture {

  double mag;
  double rake;
  double rate;
  Location hypocenter; // TODO needed??
  RuptureSurface surface;

  /* for internal use only */
  Rupture() {}

  private Rupture(double mag, double rate, double rake, RuptureSurface surface,
      Location hypocenter) {
    this.mag = mag;
    // TODO validate mag?
    // where are mags coming from? if MFD then no need to validate
    this.rate = rate;
    this.rake = checkRake(rake);
    this.surface = surface;
    this.hypocenter = hypocenter;
    // TODO checkNotNull?
  }

  /**
   *
   * @param mag moment magnitude
   * @param rate of occurrence (annual)
   * @param rake slip direction on rupture surface
   * @param surface of the rupture
   * @return a new {@code Rupture}
   */
  public static Rupture create(double mag, double rate, double rake, RuptureSurface surface) {
    return new Rupture(mag, rate, rake, surface, null);
  }

  /**
   * Creates a new {@code Rupture}.
   *
   * @param mag moment magnitude
   * @param rate of occurrence (annual)
   * @param rake slip direction on rupture surface
   * @param surface of the rupture
   * @param hypocenter of the rupture
   * @return a new {@code Rupture}
   */
  @Deprecated // until proven useful
  static Rupture create(double mag, double rate, double rake, RuptureSurface surface,
      Location hypocenter) {
    return new Rupture(mag, rate, rake, surface, hypocenter);
  }

  /**
   * The {@code Rupture} magnitude.
   */
  public double mag() {
    return mag;
  }

  /**
   * The {@code Rupture} rake.
   */
  public double rake() {
    return rake;
  }

  /**
   * The {@code Rupture} surface.
   */
  public RuptureSurface surface() {
    return surface;
  }

  // public Location hypocenter() { return hypocenter; }

  /**
   * The {@code Rupture} rate.
   */
  public double rate() {
    return rate;
  }

  @Override
  public String toString() {
    Map<Object, Object> data = ImmutableMap.builder()
        .put("mag", mag)
        .put("rake", rake)
        .put("rate", rate)
        .put("hypo", hypocenter == null ? "null" : hypocenter)
        .put("surface", surface)
        .build();
    return getClass().getSimpleName() + " " + data;
  }

}
