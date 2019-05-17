package org.opensha.util.geo;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.opensha.util.Text.WHITESPACE_SPLITTER;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

/**
 * A list of {@link Location}s.
 * 
 * <p>Implementations provided in this package are guaranteed to be immutable.
 * All calls to methods or iterator methods that would cause structural
 * modifications throw an {@code UnsupportedOperationException}.
 *
 * <p>A {@code LocationList} must contain at least 1 {@code Location} and does
 * not permit {@code null} elements.
 * 
 * <p>Consider using a {@link LocationList#builder() builder} if a list is being
 * compiled from numerous {@code Location}s that are not known in advance.
 * Otherwise, use static factory methods. A variety of additional methods exist
 * to create modified views or transforms of a {@code LocationList} (e.g.
 * {@link #resample(double)} , {@link #reverse()}, and
 * {@link #translate(LocationVector)}.
 *
 * @author Peter Powers
 */
public interface LocationList extends List<Location> {

  /**
   * Return the first location in the list.
   */
  default Location first() {
    return get(0);
  }

  /**
   * Return the last location in the list.
   */
  default Location last() {
    return get(size() - 1);
  }

  /**
   * Lazily compute the horizontal length of the list in km. Method uses the
   * {@link Locations#horzDistanceFast(Location, Location)} algorithm and
   * ignores depth variation between locations, computing length as though all
   * locations have a depth of 0.0 km. Repeat calls to this method will
   * recalculate the length each time.
   *
   * @return the length of the line connecting all {@code Location}s in this
   *         list, ignoring depth variations, or 0.0 if list only contains 1
   *         location
   */
  default double length() {
    double sum = 0.0;
    if (size() == 1) {
      return sum;
    }
    Location prev = first();
    for (Location loc : Iterables.skip(this, 1)) {
      sum += Locations.horzDistanceFast(prev, loc);
      prev = loc;
    }
    return sum;
  }

  /**
   * Lazily compute the average depth of the locations in the list.
   */
  default double depth() {
    double depth = 0.0;
    for (Location loc : this) {
      depth += loc.depth;
    }
    return depth / size();
  }

  /**
   * Compute the distances between each location in the list. The length of the
   * returned array is {@code size() - 1} and may be empty if this list contains
   * only one single location.
   * @return
   */
  default double[] distances() {
    double[] distances = new double[size() - 1];
    for (int i = 0; i < size() - 1; i++) {
      distances[i] = Locations.horzDistanceFast(get(i), get(i + 1));
    }
    return distances;
  }

  /**
   * Lazily compute the bounds of the locations in the list. Method delegates to
   * {@link Locations#bounds(Iterable)}.
   */
  default Bounds bounds() {
    return Locations.bounds(this);
  }

  /**
   * Partition this list into sub-lists of desired {@code length}. The actual
   * length of the returned lists will likely differ, as the lengths of the
   * sub-lists are adjusted up or down to a value closest to the target length
   * that yields sub-lists of equal length.
   *
   * <p>If {@code this.length() < length}, this list is returned.
   *
   * @param length target length of sub-lists
   */
  default List<LocationList> partition(double length) {
    checkArgument(
        Doubles.isFinite(length) && length > 0.0,
        "Length must be positive, real number");

    double totalLength = this.length();
    if (totalLength <= length) {
      return ImmutableList.of(this);
    }

    ImmutableList.Builder<LocationList> partitions = ImmutableList.builder();
    double partitionLength = totalLength / Math.rint(totalLength / length);

    double[] distances = distances();
    LocationList.Builder partition = LocationList.builder();
    double residual = 0.0;
    for (int i = 0; i < distances.length; i++) {
      Location start = get(i);
      partition.add(start);
      double distance = distances[i] + residual;
      while (partitionLength < distance) {
        /*
         * Catch the edge case where, on the last segment of a trace that needs
         * to be partitioned we just undershoot the last point. In the absence
         * of this we can end up with a final section of length ≈ 0.
         * 
         */
        if (i == distances.length - 1 && distance < 1.5 * partitionLength) {
          break;
        }
        LocationVector v = LocationVector.create(start, get(i + 1));
        Location end = Locations.location(start, v.azimuth, partitionLength - residual);
        partition.add(end);
        partitions.add(partition.build());
        partition = LocationList.builder();
        partition.add(end);
        start = end;
        distance -= partitionLength;
        residual = 0.0;
      }
      residual = distance;
    }
    partition.add(last());
    partitions.add(partition.build());
    return partitions.build();
  }

  /**
   * Return a new list resampled to the desired maximum spacing. The actual
   * spacing of the returned list will likely differ, as spacing is adjusted
   * down to maintain uniform divisions. The original vertices are also not
   * preserved such that some corners might be clipped if {@code spacing} is
   * large. Buyer beware.
   *
   * <p>If {@code this.length() < spacing}, this list is returned.
   *
   * @param spacing resample interval
   */
  default LocationList resample(double spacing) {
    checkArgument(
        Doubles.isFinite(spacing) && spacing > 0.0,
        "Spacing must be positive, real number");

    double length = this.length();
    if (length <= spacing) {
      return this;
    }

    /*
     * TODO Consider using rint() and/or providing a maxFlag which will keep the
     * actual spacing closer to the target spacing, albeit sometimes larger.
     * 
     * TODO consder using round() instead of ceil() which will in some cases be
     * closer to the target spacing, albeit greater.
     * 
     * TODO consider immutList.Builder for resampled below
     */

    spacing = length / Math.ceil(length / spacing);
    List<Location> resampled = Lists.newArrayList();
    Location start = this.first();
    resampled.add(start);
    double walker = spacing;
    for (Location loc : Iterables.skip(this, 1)) {
      LocationVector v = LocationVector.create(start, loc);
      double distance = Locations.horzDistanceFast(start, loc);
      while (walker <= distance) {
        resampled.add(Locations.location(start, v.azimuth, walker));
        walker += spacing;
      }
      start = loc;
      walker -= distance;
    }
    // replace last point to be exact
    resampled.set(resampled.size() - 1, this.last());
    return LocationList.create(resampled);
  }

  /**
   * Return a new list with locations in reverse order. If possible,
   * implementations avoid copying the list.
   */
  default LocationList reverse() {
    return create(ImmutableList.copyOf(this).reverse());
  }

  /**
   * Return a new {@code LocationList} with each {@code Location} translated
   * according to the supplied vector.
   *
   * @param vector to translate list by
   */
  default LocationList translate(LocationVector vector) {
    // TODO consider adding translate to LocationVector
    // --> .map(vector::translate)
    return create(this.stream()
        .map(loc -> Locations.location(loc, vector))
        .collect(ImmutableList.toImmutableList()));

  }

  // TODO need toString equals hashcode
  // @Override
  // public String toString() {
  // return NEWLINE + Joiner.on(NEWLINE).join(this) + NEWLINE;
  // }

  // TODO Test IAE passed back from RegLocList

  /**
   * Create a new {@code LocationList} containing the supplied
   * {@code locations}.
   *
   * @param locations to populate list with
   * @throws IllegalArgumentException if {@code locations} is empty
   */
  static LocationList create(Location... locations) {
    return builder().add(locations).build();
  }

  /**
   * Create a new {@code LocationList} containing the supplied
   * {@code locations}. If possible, this method tries to avoid copying the
   * supplied data.
   *
   * @param locations to populate list with
   * @throws IllegalArgumentException if {@code locations} is empty
   */
  static LocationList create(Iterable<Location> locations) {
    checkArgument(locations.iterator().hasNext(), "locations may not be empty");
    if (locations instanceof LocationList) {
      return (LocationList) locations;
    }
    if (locations instanceof ImmutableList) {
      return new RegularLocationList((ImmutableList<Location>) locations);
    }
    return builder().addAll(locations).build();
  }

  /**
   * Create a new {@code LocationList} from the supplied {@code String}. This
   * method assumes that {@code s} is formatted as one or more space-delimited
   * {@code [longitude, latitude, depth]} tuples (comma-delimited); see
   * {@link Location#fromString(String)}.
   *
   * @param s {@code String} to read
   * @return a new {@code LocationList}
   * @see Location#fromString(String)
   */
  static LocationList fromString(String s) {
    return create(Iterables.transform(
        WHITESPACE_SPLITTER.split(checkNotNull(s)),
        Location.stringConverter().reverse()));
  }

  // TODO check tuple order lon,lat,depth in tests

  /**
   * Return a new builder.
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * A reusable builder of {@code LocationList}s. Repeat calls to
   * {@code build()} will return multiple lists in series with each new list
   * containing all the elements of the one before it. Builder does not permit
   * the addition of {@code null} elements.
   *
   * <p>Use {@link LocationList#builder()} to create new builder instances.
   */
  public static final class Builder {

    // TODO check if @VisibleForTesting is needed
    @VisibleForTesting
    ImmutableList.Builder<Location> builder;

    /* No instantiation. */
    private Builder() {
      builder = ImmutableList.builder();
    }

    /**
     * Add a {@code Location}.
     *
     * @param location to add
     * @return this {@code Builder}
     */
    public Builder add(Location location) {
      builder.add(location);
      return this;
    }

    /**
     * Add a new {@code Location} with the specified latitude and longitude, and
     * a depth of 0 km.
     *
     * @param latitude in decimal degrees
     * @param longitude in decimal degrees
     * @return this {@code Builder}
     * @throws IllegalArgumentException if any values are out of range
     * @see Coordinates
     */
    public Builder add(double latitude, double longitude) {
      builder.add(Location.create(latitude, longitude));
      return this;
    }

    /**
     * Add a new {@code Location} with the specified latitude, longitude, and
     * depth.
     *
     * @param latitude in decimal degrees
     * @param longitude in decimal degrees
     * @param depth in km (positive down)
     * @return this {@code Builder}
     * @throws IllegalArgumentException if any values are out of range
     * @see Coordinates
     */
    public Builder add(double latitude, double longitude, double depth) {
      builder.add(Location.create(latitude, longitude, depth));
      return this;
    }

    /**
     * Add each of the supplied {@code Location}s.
     *
     * @param locations to add
     * @return this {@code Builder}
     */
    public Builder add(Location... locations) {
      builder.add(locations);
      return this;
    }

    /**
     * Add each of the supplied {@code Location}s.
     *
     * @param locations to add
     * @return this {@code Builder}
     */
    public Builder addAll(Iterable<Location> locations) {
      builder.addAll(locations);
      return this;
    }

    /**
     * Return a newly created {@code LocationList}.
     *
     * @return a new {@code LocationList}
     * @throws IllegalStateException if the list is empty
     */
    public LocationList build() {
      ImmutableList<Location> locs = builder.build();
      return new RegularLocationList(locs);
    }
  }

}
