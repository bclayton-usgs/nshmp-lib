package gov.usgs.earthquake.nshmp.gmm;

import static gov.usgs.earthquake.nshmp.gmm.Gmm.AB_03_CASCADIA_SLAB;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.AB_03_CASCADIA_SLAB_LOW_SAT;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.AB_03_GLOBAL_SLAB;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.AB_03_GLOBAL_SLAB_LOW_SAT;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.BCHYDRO_12_SLAB;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.YOUNGS_97_SLAB;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.ZHAO_06_SLAB;
import static gov.usgs.earthquake.nshmp.gmm.Imt.PGA;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P2;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA1P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA3P0;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class SubSlab extends GmmTest {

  private static String GMM_INPUTS = "slab-inputs.csv";
  private static String GMM_RESULTS = "slab-results.csv";

  @Parameters(name = "{index}: {0} {2} {1}")
  public static Collection<Object[]> data() throws IOException {
    return loadResults(GMM_RESULTS);
  }

  public SubSlab(int index, Gmm gmm, Imt imt, double exMedian, double exSigma) {
    super(index, gmm, imt, exMedian, exSigma, GMM_INPUTS);
  }

  /* Result generation sets */
  private static Set<Gmm> gmms = EnumSet.of(
      AB_03_GLOBAL_SLAB,
      AB_03_GLOBAL_SLAB_LOW_SAT,
      AB_03_CASCADIA_SLAB,
      AB_03_CASCADIA_SLAB_LOW_SAT,
      BCHYDRO_12_SLAB,
      YOUNGS_97_SLAB,
      ZHAO_06_SLAB);

  private static Set<Imt> imts = EnumSet.of(
      PGA,
      SA0P2,
      SA1P0,
      SA3P0);

  public static void main(String[] args) throws IOException {
    GmmTest.generateResults(gmms, imts, GMM_INPUTS, GMM_RESULTS);
  }

}
