package org.opensha.util.geo;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;

/**
 * Default LocationList implementation backed by an ImmutableList.
 * 
 * @author Peter Powers
 */
final class RegularLocationList extends ForwardingList<Location> implements LocationList {

  final ImmutableList<Location> locs;

  RegularLocationList(ImmutableList<Location> locs) {
    checkArgument(locs.size() > 0, "Location lists may not by empty");
    this.locs = locs;
  }

  @Override
  protected List<Location> delegate() {
    return locs;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && (obj instanceof LocationList);
  }

  @Override
  public LocationList reverse() {
    return new RegularLocationList(locs.reverse());
  }

  @Override
  public String toString() {
    return Locations.toStringHelper(this);
  }

}
