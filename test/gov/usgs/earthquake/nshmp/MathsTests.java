package gov.usgs.earthquake.nshmp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.RoundingMode;
import java.util.stream.DoubleStream;

import org.junit.Test;

import com.google.common.base.Converter;

@SuppressWarnings("javadoc")
public class MathsTests {

  @Test
  public void hypot() {
    /* 2- and 3-arg flavors. */
    assertEquals(Maths.hypot(3, 4), 5.0, 0.0);
    assertEquals(Maths.hypot(3, 4, 0), 5.0, 0.0);
    /* Variadic flavor. */
    assertEquals(Maths.hypot(2), 2.0, 0.0);
    assertEquals(Maths.hypot(3, 4, 0, 0), 5.0, 0.0);
  }

  @Test
  public void epsilon() {
    assertEquals(1.5, Maths.epsilon(3.0, 2.0, 6.0), 0);
  }

  @Test
  public void round() {
    assertEquals(3.14, Maths.round(Math.PI, 2), 0);
    assertEquals(3.14, Maths.round(Math.PI, 2, RoundingMode.HALF_DOWN), 0);
  }

  @Test
  public void converter() {
    Converter<Double, Double> decimalProbConverter = Maths.decimalToProbabilityConverter(2);
    assertEquals(2.45, decimalProbConverter.convert(0.024522), 0.0);
    assertEquals(0.0245, decimalProbConverter.reverse().convert(2.45), 0.0);
  }

  /*
   * Testing erf() against wiki table:
   * https://en.wikipedia.org/wiki/Error_function#Table_of_values
   * 
   * Values are from -2.0 to 2.0 with a 0.2 step.
   */
  private double[] erfExpected = {
      -0.995322265,
      -0.989090502,
      -0.976348383,
      -0.952285120,
      -0.910313978,
      -0.842700793,
      -0.742100965,
      -0.603856091,
      -0.428392355,
      -0.222702589,
      0.0,
      0.222702589,
      0.428392355,
      0.603856091,
      0.742100965,
      0.842700793,
      0.910313978,
      0.952285120,
      0.976348383,
      0.989090502,
      0.995322265 };

  @Test
  public void erf() {
    double[] erfActual = DoubleStream.iterate(-2.0, x -> x + 0.2)
        .limit(21)
        .map(Maths::erf)
        .toArray();
    assertArrayEquals(erfExpected, erfActual, 1e-6);
  }

  private double[] ccdfExpected = {
      0.993790319888562,
      0.9772499371127437,
      0.9331927690234977,
      0.8413447361676363,
      0.6914624627239938,
      0.5000000005,
      0.3085375372760062,
      0.15865526383236372,
      0.06680723097650232,
      0.022750062887256395,
      0.006209680111438021 };

  private double[] pdfExpected = {
      0.00876415024678427,
      0.02699548325659403,
      0.06475879783294587,
      0.12098536225957168,
      0.17603266338214976,
      0.19947114020071635,
      0.17603266338214976,
      0.12098536225957168,
      0.06475879783294587,
      0.02699548325659403,
      0.00876415024678427 };

  private double[] stepExpected = {
      1.0,
      1.0,
      1.0,
      1.0,
      1.0,
      0.0,
      0.0,
      0.0,
      0.0,
      0.0,
      0.0 };

  @Test
  public void distributions() {

    double μ = 0.0;
    double σ = 2.0;

    double[] ccdfActual = DoubleStream.iterate(-5, x -> x + 1)
        .limit(11)
        .map(x -> Maths.normalCcdf(μ, σ, x))
        .toArray();
    assertArrayEquals(ccdfExpected, ccdfActual, 0.0);

    double[] pdfActual = DoubleStream.iterate(-5, x -> x + 1)
        .limit(11)
        .map(x -> Maths.normalPdf(μ, σ, x))
        .toArray();
    assertArrayEquals(pdfExpected, pdfActual, 0.0);

    double[] stepActual = DoubleStream.iterate(-5, x -> x + 1)
        .limit(11)
        .map(x -> Maths.stepFunction(μ, x))
        .toArray();
    assertArrayEquals(stepExpected, stepActual, 0.0);
  }

  public static void main(String[] args) {
    // used to generate distribution test results
    double μ = 0.0;
    double σ = 2.0;
    DoubleStream.iterate(-5, x -> x + 1)
        .limit(11)
        // .map(x -> Maths.normalCcdf(μ, σ, x))
        // .map(x -> Maths.normalPdf(μ, σ, x))
        .map(x -> Maths.stepFunction(μ, x))
        .forEach(System.out::println);
  }
}
