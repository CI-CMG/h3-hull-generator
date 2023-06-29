package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.util.GeoCoord;
import java.util.Collection;
import java.util.List;
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
   * Transforms H3 ids into {@link List<Geometry>}
   * @param points {@link Collection<Long>} containing H3 ids
   * @return {@link List<Geometry>} from H3 ids
   */
  List<Geometry> getGeometry(Collection<Long> points);

  /**
   * Unions {@link List<Geometry>} into a single {@link Geometry}
   * @param geometries separated {@link List<Geometry>}
   * @param existingGeometry existing {@link Geometry}
   * @return {@link Geometry} containing unions of all input geometry
   */
  Geometry mergeGeometryOutlines(List<Geometry> geometries, Geometry existingGeometry);

}
