package gov.usgs.earthquake.nshmp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("javadoc")
public class ArrayXySequenceTests {

  private static double[] xs = new double[] { 0, 1, 2, 3 };
  private static double[] ys = new double[] { -1, 10.5, 5.25, 2.5 };
  private static XySequence xy = XySequence.create(xs, ys);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public final void clearTest() {
    assertFalse(xy.isClear());

    XySequence xyEmpty = new ArrayXySequence(xy, true);
    xyEmpty.stream().forEach(p -> assertEquals(p.y(), 0, 0));
  }

  @Test
  public final void trimTest() {
    double[] xs = { 0, 1, 2, 3, 4, 5 };
    double[] ys = { 0, 0, 1, 2, 0, 0 };

    XySequence xy = XySequence.create(xs, ys);
    XySequence xyExpected = xy.trim();

    double[] xsTrim = Arrays.copyOfRange(xs, 2, 4);
    double[] ysTrim = Arrays.copyOfRange(ys, 2, 4);
    XySequence xyActual = XySequence.create(xsTrim, ysTrim);

    assertTrue(xyActual.equals(xyExpected));
  }

  @Test
  public final void pointTest() {
    thrown.expect(UnsupportedOperationException.class);
    xy.stream().forEach(p -> p.set(0));
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

}
