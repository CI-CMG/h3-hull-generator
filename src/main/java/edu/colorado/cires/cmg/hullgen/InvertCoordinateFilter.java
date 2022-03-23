package edu.colorado.cires.cmg.hullgen;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;

/**
 * Inverts coordinates belonging to a {@link org.locationtech.jts.geom.Geometry}
 */
public class InvertCoordinateFilter implements CoordinateFilter {

  /**
   *
   * @param coordinate {@link Coordinate} containing latitude and longitude
   */
  @Override
  public void filter(Coordinate coordinate) {
    double oldX = coordinate.getX();
    coordinate.setX(coordinate.getY());
    coordinate.setY(oldX);
  }
}
