package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.util.GeoCoord;
import java.util.Collection;
import org.locationtech.jts.geom.Geometry;

/**
 * Processes geometries and coordinates with Uber's H3 library
 */
public interface GeometryProcessor {

  /**
   * Converts {@link GeoCoord} to H3 id
   * @param geoCoord {@link GeoCoord} containing latitude and longitude
   * @return H3 id
   */
  long geoCoordToH3(GeoCoord geoCoord);

  /**
   * Transforms H3 ids into {@link Geometry}
   * @param points {@link Collection<Long>} containing H3 ids
   * @return {@link Geometry} from H3 ids
   */
  Geometry getGeometry(Collection<Long> points);

  /**
   * Unions {@link Geometry} and another geometry into a single {@link Geometry}
   * @param geometry {@link Geometry} to add
   * @param existingGeometry existing {@link Geometry}
   * @return {@link Geometry} containing unions of all input geometry
   */
  Geometry mergeGeometryOutlines(Geometry geometry, Geometry existingGeometry);

}
