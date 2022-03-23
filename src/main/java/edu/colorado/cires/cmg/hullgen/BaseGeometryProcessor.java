package edu.colorado.cires.cmg.hullgen;
;
import com.uber.h3core.H3Core;
import com.uber.h3core.util.GeoCoord;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

/**
 * Base implementation of {@link GeometryProcessor}
 */
public class BaseGeometryProcessor implements GeometryProcessor{

  protected final H3Core h3Core;
  private final Integer h3Resolution;
  private final GeometryFactory geometryFactory;

  /**
   * Constructor for {@link BaseGeometryProcessor}
   * @param h3Resolution integer from 0 (the lowest resolution) to 15 (the highest resolution) specifying size of H3 hexagons
   * @param geometryFactory {@link GeometryFactory} for generating and merging JTS geometries
   * @throws IOException if {@link H3Core} cannot create a new instance
   */
  public BaseGeometryProcessor(Integer h3Resolution, GeometryFactory geometryFactory) throws IOException {
    this.h3Resolution = h3Resolution;
    this.geometryFactory = geometryFactory;
    h3Core = H3Core.newInstance();
  }

  /**
   * Converts {@link GeoCoord} to H3 id
   * @param geoCoord {@link GeoCoord} containing latitude and longitude
   * @return H3 id
   */
  @Override
  public long geoCoordToH3(GeoCoord geoCoord) {
    return h3Core.geoToH3(geoCoord.lat, geoCoord.lng, h3Resolution);
  }

  /**
   * Transforms H3 ids into {@link List<Geometry>} containing outer rings
   * @param points {@link Collection<Long>} containing H3 ids
   * @return {@link List<Geometry>} containing outer rings of neighboring hexagons
   */
  @Override
  public List<Geometry> getGeometryOutlines(Collection<Long> points) {
    return h3Core.h3SetToMultiPolygon(points, true).parallelStream()
        .map(geoCoordsList -> geoCoordsList.get(0))
        .map(geoCoords ->
            geoCoords.stream()
                .map(geoCoord -> new Coordinate(geoCoord.lat, geoCoord.lng))
                .collect(Collectors.toList())
                .toArray(new Coordinate[] {})
            )
        .map(geometryFactory::createPolygon)
        .collect(Collectors.toList());
  }

  /**
   * Unions {@link List<Geometry>} into a single {@link Geometry}
   * @param geometries separated {@link List<Geometry>}
   * @param existingGeometry existing {@link Geometry}
   * @return {@link Geometry} containing unions of all input geometry
   */
  @Override
  public Geometry mergeGeometryOutlines(List<Geometry> geometries, Geometry existingGeometry) {
    if (existingGeometry != null) {
      geometries.add(existingGeometry);
    }
    Geometry geometry = removeHoles(UnaryUnionOp.union(geometries, geometryFactory));
    return processAnteMeridian(geometry);
  }

  @Override
  public Geometry processAnteMeridian(Geometry geometry) {
    return new JtsGeometry(geometry, JtsSpatialContext.GEO, true, false).getGeom();
  }

  private Geometry removeHoles(Geometry geometry) {
    if (geometry.getGeometryType().equals("Polygon")) {
      Polygon polygon = (Polygon) geometry;
      return geometryFactory.createPolygon(polygon.getExteriorRing().getCoordinateSequence());
    } else if (geometry.getGeometryType().equals("MultiPolygon")) {
      Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        Polygon polygon = (Polygon) geometry.getGeometryN(i);
        polygons[i] = geometryFactory.createPolygon(polygon.getExteriorRing().getCoordinateSequence());
      }
      return geometryFactory.createMultiPolygon(polygons);
    } else {
      throw new IllegalStateException("Invalid merged geometry type: " + geometry.getGeometryType());
    }
  }
}
