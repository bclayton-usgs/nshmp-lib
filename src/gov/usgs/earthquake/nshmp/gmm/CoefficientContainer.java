package gov.usgs.earthquake.nshmp.gmm;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Resources;
import com.google.common.primitives.Doubles;

import gov.usgs.earthquake.nshmp.Parsing;
import gov.usgs.earthquake.nshmp.Parsing.Delimiter;

/**
 * Class loads and manages {@code GroundMotionModel} coefficients.
 *
 * <p>Coefficients are loaded from CSV files. When such files are updated, it
 * may be necessary to edit certain {@code Imt} designations that are commonly
 * coded as integers (e.g. -1 = PGV, usually) or coefficient IDs that contain
 * illegal characters (e.g those with units labels in parentheses).
 *
 * @author Peter Powers
 */
final class CoefficientContainer {

  private static final String C_DIR = "coeffs/";
  private Table<Imt, String, Double> table;

  /**
   * Create a new coefficent container from a comma-delimited coefficient
   * resource.
   *
   * @param resource coefficent csv text resource
   */
  CoefficientContainer(String resource) {
    try {
      table = ImmutableTable.copyOf(load(resource));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Return the value of the coefficient for the supplied name and intensity
   * measure type.
   *
   * @param imt intensity measure type
   * @param name of the coefficient to look up
   */
  double get(Imt imt, String name) {
    return table.get(imt, name);
  }

  /**
   * Return a {@code Map} of all coefficient for the supplied intensity measure
   * type.
   *
   * @param imt intensity measure type
   */
  Map<String, Double> get(Imt imt) {
    return table.row(imt);
  }

  /**
   * Returns the {@code Set} of intensity measure types (IMTs) for which
   * coefficients are supplied.
   * @return the {@code Set} of supported IMTs
   */
  Set<Imt> imts() {
    return Sets.immutableEnumSet(table.rowKeySet());
  }

  private static Table<Imt, String, Double> load(String resource) throws IOException {
    URL url = Resources.getResource(CoefficientContainer.class, C_DIR + resource);
    List<String> lines = Resources.readLines(url, UTF_8);
    // build coeff name list
    Iterable<String> names = FluentIterable
        .from(Parsing.split(lines.get(0), Delimiter.COMMA))
        .skip(1);
    // build Imt-value map
    Map<Imt, Double[]> valueMap = Maps.newHashMap();

    Iterable<String> imtLines = FluentIterable.from(lines)
        .skip(1)
        .filter(new Predicate<String>() {
          @Override
          public boolean test(String s) {
            return !s.startsWith("#");
          }
        }::test);
    for (String line : imtLines) {
      Iterable<String> entries = Parsing.split(line, Delimiter.COMMA);
      String imtStr = Iterables.get(entries, 0);
      Imt imt = parseImt(imtStr);
      Iterable<String> valStrs = Iterables.skip(entries, 1);
      Iterable<Double> values = Iterables.transform(valStrs, Doubles.stringConverter());
      valueMap.put(imt, Iterables.toArray(values, Double.class));
    }
    // create and load table
    Table<Imt, String, Double> table = ArrayTable.create(valueMap.keySet(), names);
    for (Imt imt : valueMap.keySet()) {
      Double[] values = valueMap.get(imt);
      int i = 0;
      for (String name : names) {
        table.put(imt, name, values[i++]);
      }
    }
    return table;
  }

  /*
   * Parses IMT strings from coefficient files. Method expects Imt.name() for
   * specifically named intensity measure types, e.g. "PGA", and double value
   * strings for spectral periods, e.g. "0.2". This method is NOT the same as
   * {@link Imt#valueOf(String)}. Method will throw a NumberFormatException or
   * IllegalArgumentException if the supplied string is not parseable into a
   * known IMT.
   */
  static Imt parseImt(String s) {
    s = s.trim().toUpperCase();
    if (s.equals("PGA") || s.equals("PGV") || s.equals("PGD")) {
      return Imt.valueOf(s);
    }
    double period = Double.parseDouble(s);
    return Imt.fromPeriod(period);
  }
}
