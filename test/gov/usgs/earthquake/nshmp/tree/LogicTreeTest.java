package gov.usgs.earthquake.nshmp.tree;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import gov.usgs.earthquake.nshmp.tree.Branch;
import gov.usgs.earthquake.nshmp.tree.LogicTree.Builder;

/**
 * JUnit tests for LogicTree
 */
@SuppressWarnings("javadoc")
public class LogicTreeTest {

  private static final String[] KEYS = new String[] {
      "Branch1",
      "Branch2",
      "Branch3",
      "Branch4" };

  private static final double[] WEIGHTS = new double[] { 0.40, 0.30, 0.20, 0.10 };

  private static final double[] CUML_WEIGHTS = new double[] { 0.39, 0.69, 0.89, 0.99 };

  private static final GroundMotion GM = new GroundMotion(1.0, 0.5);

  private static final LogicTree<GroundMotion> TREE = LogicTree
      .<GroundMotion> builder()
      .add(KEYS[0], GM, WEIGHTS[0])
      .add(KEYS[1], GM, WEIGHTS[1])
      .add(KEYS[2], GM, WEIGHTS[2])
      .add(KEYS[3], GM, WEIGHTS[3])
      .build();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public final void builderEmpty() {
    thrown.expect(IllegalStateException.class);

    LogicTree.<GroundMotion> builder().build();
  }

  @Test
  public final void builderAlreadyBuild() {
    thrown.expect(IllegalStateException.class);

    Builder<GroundMotion> builder = LogicTree
        .<GroundMotion> builder()
        .add(KEYS[0], GM, 1.0);

    builder.build();
    builder.build();
  }

  @Test
  public final void builderNullKey() {
    thrown.expect(NullPointerException.class);

    LogicTree.<GroundMotion> builder()
        .add(null, GM, 1.0)
        .build();
  }

  @Test
  public final void builderNullValue() {
    thrown.expect(NullPointerException.class);

    LogicTree.<GroundMotion> builder()
        .add(KEYS[0], null, 1.0)
        .build();
  }

  @Test
  public final void builderBadWeight() {
    thrown.expect(IllegalArgumentException.class);

    LogicTree.<GroundMotion> builder()
        .add(KEYS[0], GM, 2.0);
  }

  @Test
  public final void builderBuildBadWeights() {
    thrown.expect(IllegalArgumentException.class);

    LogicTree.<GroundMotion> builder()
        .add(KEYS[0], GM, 1.0)
        .add(KEYS[1], GM, 1.0)
        .build();
  }

  @Test
  public final void equals() {
    int index = 0;

    List<Branch<GroundMotion>> sampleBranches = TREE.sample(CUML_WEIGHTS);
    assertEquals(CUML_WEIGHTS.length, sampleBranches.size(), 0);

    for (Branch<GroundMotion> branch : TREE) {
      String key = KEYS[index];
      double weight = WEIGHTS[index];
      Branch<GroundMotion> sampleBranch = TREE.sample(CUML_WEIGHTS[index]);

      checkBranch(key, weight, GM, branch);
      checkBranch(key, weight, GM, sampleBranch);
      checkBranch(key, weight, GM, sampleBranches.get(index));

      index++;
    }

    Branch<GroundMotion> sampleBranch = TREE.sample(1.1);
    checkBranch(KEYS[3], WEIGHTS[3], GM, sampleBranch);
  }

  @Test
  public final void singleBranchEquals() {
    String key = KEYS[0];
    double weight = 1.0;

    SingleBranchTree<GroundMotion> tree = LogicTree.singleBranch(key, GM);
    tree.forEach((branch) -> checkBranch(key, weight, GM, branch));

    Branch<GroundMotion> sampleBranch = tree.sample(2.0);
    checkBranch(key, weight, GM, sampleBranch);

    List<Branch<GroundMotion>> sampleBranches = tree.sample(WEIGHTS);
    sampleBranches.forEach((branch) -> checkBranch(key, weight, GM, branch));
    assertEquals(WEIGHTS.length, sampleBranches.size());
  }

  private static void checkBranch(
      String key,
      double weight,
      GroundMotion value,
      Branch<GroundMotion> branch) {
    assertEquals(key, branch.id());
    assertEquals(weight, branch.weight(), 0);
    assertEquals(value.mean(), branch.value().mean(), 0);
    assertEquals(value.sigma(), branch.value().sigma(), 0);
  }
  
  private static class GroundMotion {
    double mean;
    double sigma;
    
    GroundMotion(double mean, double sigma) {
      this.mean = mean;
      this.sigma = sigma;
    }
    
    double mean() {
      return mean;
    }
    
    double sigma() {
      return sigma;
    }

  }

}
