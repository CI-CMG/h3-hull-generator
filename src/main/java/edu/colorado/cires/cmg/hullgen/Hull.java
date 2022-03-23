package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.util.GeoCoord;
import org.locationtech.jts.geom.Geometry;

/**
 * Processes H3 ids into {@link Geometry} containing hulls
 */
public interface Hull {

  /**
   * Adds point to point set
   * @param geoCoord {@link GeoCoord} containing latitude and longitude
   */
  void addPoint(GeoCoord geoCoord);

  /**
   * Generates and merges hulls
   */
  void generateHull();

  /**
   * Gets {@link Geometry} containing hull
   * @return {@link Geometry} containing hull
   */
  Geometry getHullGeometry();

}
