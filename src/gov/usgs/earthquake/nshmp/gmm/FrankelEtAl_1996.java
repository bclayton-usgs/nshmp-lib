package gov.usgs.earthquake.nshmp.gmm;

import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.MW;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.RRUP;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.VS30;
import static gov.usgs.earthquake.nshmp.gmm.GmmUtils.BASE_10_TO_E;
import static gov.usgs.earthquake.nshmp.gmm.GmmUtils.CeusSiteClass.HARD_ROCK;
import static gov.usgs.earthquake.nshmp.gmm.GmmUtils.CeusSiteClass.SOFT_ROCK;
import static gov.usgs.earthquake.nshmp.gmm.MagConverter.NONE;

import com.google.common.collect.Range;

import gov.usgs.earthquake.nshmp.gmm.GmmInput.Constraints;
import gov.usgs.earthquake.nshmp.gmm.GmmUtils.CeusSiteClass;
import gov.usgs.earthquake.nshmp.gmm.GroundMotionTables.GroundMotionTable;

/**
 * Implementation of the Frankel et al. (1996) ground motion model for stable
 * continental regions. This implementation matches that used in the 2008 USGS
 * NSHMP and comes in two additional magnitude converting (mb to Mw) flavors to
 * support the 2008 central and eastern US model.
 *
 * <p><b>Note:</b> Direct instantiation of {@code GroundMotionModel}s is
 * prohibited. Use {@link Gmm#instance(Imt)} to retrieve an instance for a
 * desired {@link Imt}.
 *
 * <p><b>Implementation note:</b> Mean values are clamped per
 * {@link GmmUtils#ceusMeanClip(Imt, double)}.
 *
 * <p><b>Reference:</b> Frankel, A., Mueller, C., Barnhard, T., Perkins, D.,
 * Leyendecker, E., Dickman, N., Hanson, S., and Hopper, M., 1996, National
 * Seismic Hazard Maps—Documentation June 1996: U.S. Geological Survey Open-File
 * Report 96–532, 110 p.
 *
 * <p><b>Component:</b> not specified
 *
 * @author Peter Powers
 * @see Gmm#FRANKEL_96
 * @see Gmm#FRANKEL_96_AB
 * @see Gmm#FRANKEL_96_J
 */
public class FrankelEtAl_1996 implements GroundMotionModel, ConvertsMag {

  static final String NAME = "Frankel et al. (1996)";

  static final Constraints CONSTRAINTS = Constraints.builder()
      .set(MW, Range.closed(4.0, 8.0))
      .set(RRUP, Range.closed(0.0, 1000.0))
      .set(VS30, Range.closed(760.0, 2000.0))
      .build();

  static final CoefficientContainer COEFFS = new CoefficientContainer("Frankel96.csv");

  private final double bσ;
  private final Imt imt;
  private final GroundMotionTable bcTable;
  private final GroundMotionTable aTable;

  FrankelEtAl_1996(Imt imt) {
    this.imt = imt;
    bσ = COEFFS.get(imt, "bsigma");
    bcTable = GroundMotionTables.getFrankel96(imt, SOFT_ROCK);
    aTable = GroundMotionTables.getFrankel96(imt, HARD_ROCK);
  }

  @Override
  public final ScalarGroundMotion calc(GmmInput in) {
    CeusSiteClass siteClass = GmmUtils.ceusSiteClass(in.vs30);
    double Mw = converter().convert(in.Mw);
    double μ = (siteClass == SOFT_ROCK) ? bcTable.get(in.rRup, Mw) : aTable.get(in.rRup, Mw);
    μ = GmmUtils.ceusMeanClip(imt, μ * BASE_10_TO_E);
    double σ = bσ * BASE_10_TO_E;
    return DefaultScalarGroundMotion.create(μ, σ);
  }

  @Override
  public MagConverter converter() {
    return NONE;
  }

}
