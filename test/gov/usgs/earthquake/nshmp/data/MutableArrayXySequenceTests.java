package gov.usgs.earthquake.nshmp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("javadoc")
public class MutableArrayXySequenceTests {

  static double[] xs = new double[] { 0, 1, 2, 3 };
  static double[] ys = new double[] { -1, 10.5, 5.25, 2.5 };
  static MutableXySequence xy = MutableXySequence.create(xs, ys);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public final void clearTest() {
    assertFalse(xy.isClear());

    XySequence xyEmpty = new MutableArrayXySequence(xy, true);
    xyEmpty.stream().forEach(p -> assertEquals(p.y(), 0, 0));

    MutableXySequence.copyOf(xy)
        .clear()
        .stream()
        .forEach(p -> assertEquals(p.y(), 0, 0));
  }

  @Test
  public final void trimTest() {
    double[] xs = { 0, 1, 2, 3, 4, 5 };
    double[] ys = { 0, 0, 1, 2, 0, 0 };

    MutableXySequence xy = MutableXySequence.create(xs, ys);
    MutableXySequence xyExpected = xy.trim();

    double[] xsTrim = Arrays.copyOfRange(xs, 2, 4);
    double[] ysTrim = Arrays.copyOfRange(ys, 2, 4);
    MutableXySequence xyActual = MutableXySequence.create(xsTrim, ysTrim);

    assertTrue(xyActual.equals(xyExpected));
  }

  @Test
  public final void equalsTest() {
    double[] xsExpected = xy.xValues().toArray();
    double[] ysExpected = xy.yValues().toArray();

    for (int index = 0; index < xy.size(); index++) {
      assertEquals(xy.x(index), xs[index], 0);
      assertEquals(xy.y(index), ys[index], 0);

      assertEquals(xsExpected[index], xs[index], 0);
      assertEquals(ysExpected[index], ys[index], 0);
    }

    XyPoint first = xy.min();
    assertEquals(first.x(), xs[0], 0);
    assertEquals(first.y(), ys[0], 0);

    XyPoint last = xy.max();
    assertEquals(last.x(), xs[xs.length - 1], 0);
    assertEquals(last.y(), ys[ys.length - 1], 0);

    int size = xy.size();
    assertEquals(size, xs.length, 0);
    assertEquals(size, ys.length, 0);
    assertEquals(xsExpected.length, xs.length, 0);
    assertEquals(ysExpected.length, ys.length, 0);

    XySequence xyCopy = XySequence.create(xs, ys);
    assertTrue(xy.equals(xyCopy));
  }

  @Test
  public final void mutatorTest() {
    /* add */
    double term = 5.5;
    MutableXySequence xyAddTerm = MutableXySequence.copyOf(xy).add(term);
    MutableXySequence xyAddArray = MutableXySequence.copyOf(xy).add(ys);
    MutableXySequence xyAddSequence = MutableXySequence.copyOf(xy).add(xy);

    /* transform */
    Function<Double, Double> function = (data) -> {
      return Math.exp(data * 2);
    };

    MutableXySequence xyTransform = MutableXySequence.copyOf(xy).transform(function);

    /* multiply */
    double scale = 2.5;
    MutableXySequence xyMultiplyScale = MutableXySequence.copyOf(xy).multiply(scale);
    MutableXySequence xyMultiplySequence = MutableXySequence.copyOf(xy).multiply(xy);

    /* complement */
    MutableXySequence xyComplement = MutableXySequence.copyOf(xy).complement();
    MutableXySequence xyFlip = MutableXySequence.create(xs, DoubleData.flip(ys.clone()));

    for (int index = 0; index < xy.size(); index++) {
      /* add */
      assertEquals(xyAddTerm.y(index), xy.y(index) + term, 0);
      assertEquals(xyAddArray.y(index), xy.y(index) + ys[index], 0);
      assertEquals(xyAddSequence.y(index), xy.y(index) * 2, 0);

      /* transform */
      assertEquals(xyTransform.y(index), function.apply(xy.y(index)), 0);

      /* multiply */
      assertEquals(xyMultiplyScale.y(index), xy.y(index) * scale, 0);
      assertEquals(xyMultiplySequence.y(index), Math.pow(xy.y(index), 2), 0);

      /* complement */
      assertEquals(xyComplement.y(index), xyFlip.y(index) + 1, 0);
    }

    /* set */
    MutableXySequence xySet = MutableXySequence.copyOf(xy).set(0, term);
    assertEquals(xySet.y(0), term, 0);
  }

  @Test
  public final void pointTest() {
    MutableXySequence xyCopy = MutableXySequence.copyOf(xy);
    double setTerm = 5.5;

    xyCopy.stream().forEach(p -> {
      p.set(setTerm);
      assertEquals(p.y(), setTerm, 0);
    });
  }

}
