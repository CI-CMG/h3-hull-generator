package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.GeoCoord;
import edu.colorado.cires.cmg.polarprocessor.PolarProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

/**
 * Base implementation of {@link GeometryProcessor}
 */
public class BaseGeometryProcessor implements GeometryProcessor {

  protected final H3Core h3Core;
  private final Integer h3Resolution;
  private final GeometryFactory geometryFactory;

  private final boolean keepHoles;

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
    this.keepHoles = false;
  }

  /**
   * Constructor for {@link BaseGeometryProcessor}
   * @param h3Resolution integer from 0 (the lowest resolution) to 15 (the highest resolution) specifying size of H3 hexagons
   * @param geometryFactory {@link GeometryFactory} for generating and merging JTS geometries
   * @param keepHoles boolean specifying whether to keep holes in the output geometry
   * @throws IOException if {@link H3Core} cannot create a new instance
   */
  public BaseGeometryProcessor(Integer h3Resolution, GeometryFactory geometryFactory, boolean keepHoles) throws IOException {
    this.h3Resolution = h3Resolution;
    this.geometryFactory = geometryFactory;
    h3Core = H3Core.newInstance();
    this.keepHoles = keepHoles;
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
   * Transforms H3 ids into {@link Geometry}
   * @param points {@link Collection<Long>} containing H3 ids
   * @return {@link Geometry} from H3 ids
   */
  @Override
  public Geometry getGeometry(Collection<Long> points) {
    List<Geometry> geometries = points.stream().map(h3Core::h3ToGeoBoundary).parallel()
        .map(geoCoordsList -> {
          List<Coordinate> coordinates = geoCoordsList.stream().map(geoCoord -> new Coordinate(geoCoord.lng, geoCoord.lat)).collect(Collectors.toList());
          coordinates.add(coordinates.get(0));
          LinearRing linearRing = geometryFactory.createLinearRing(coordinates.toArray(new Coordinate[] {}));
          return processPolar(geometryFactory.createPolygon(linearRing, null));
        })
        .collect(Collectors.toList());
    List<Polygon> polygons = new ArrayList<>();
    geometries.stream().filter(Objects::nonNull).forEach(g -> {
      if (g instanceof Polygon) {
        polygons.add((Polygon) g);
      } else if (g instanceof MultiPolygon) {
        for (int i = 0; i < g.getNumGeometries(); i++) {
          polygons.add((Polygon) g.getGeometryN(i));
        }
      } else {
        throw new IllegalStateException("Unexpected geometry type: " + g.getGeometryType());
      }
    });
    return geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[] {})).union();
  }

  /**
   * Unions {@link Geometry} and another geometry into a single {@link Geometry}
   * @param geometry {@link Geometry} to add
   * @param existingGeometry existing {@link Geometry}
   * @return {@link Geometry} containing unions of all input geometry
   */
  @Override
  public Geometry mergeGeometryOutlines(Geometry geometry, Geometry existingGeometry) {
    Geometry merged = existingGeometry != null ? existingGeometry.union(geometry) : geometry;
    return keepHoles ? merged : removeHoles(merged);
  }

  protected Geometry processPolar(Geometry geometry) {

    if (geometry.getNumGeometries() == 0) {
      throw new IllegalStateException("Geometry is empty");
    }

    if (geometry.getGeometryType().equals("Polygon")) {
      Polygon polygon = (Polygon) geometry;
      try {
        return PolarProcessor.splitPolar(polygon, geometryFactory).orElse(
            new JtsGeometry(polygon, JtsSpatialContext.GEO, true, false).getGeom()
        );
      } catch (AssertionError e) {
        System.out.println("Skipping unprocessable geometry: " + polygon);
        return null;
      }
    } else if (geometry.getGeometryType().equals("MultiPolygon")) {
      List<Polygon> polarPolygons = new ArrayList<>();
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        Polygon polygon = (Polygon) geometry.getGeometryN(i);
        Geometry result = null;
        try {
          result = PolarProcessor.splitPolar(polygon, geometryFactory).orElse(
              new JtsGeometry(polygon, JtsSpatialContext.GEO, true, false).getGeom()
          );
        } catch (AssertionError e) {
          System.out.println("Skipping unprocessable geometry: " + polygon);
        }
        if (result instanceof Polygon) {
          polarPolygons.add((Polygon) result);
        } else if (result instanceof MultiPolygon) {
          for (int j = 0; j < result.getNumGeometries(); j++) {
            polarPolygons.add((Polygon) result.getGeometryN(j));
          }
        } else {
          if (result != null) {
            throw new IllegalStateException("Unexpected geometry type: " + result.getGeometryType());
          }
          throw new IllegalStateException("Encountered null geometry");
        }
      }
      return geometryFactory.createMultiPolygon(polarPolygons.toArray(new Polygon[] {}));
    }
    throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
  }

  private Geometry removeHoles(Geometry geometry) {

    if (geometry.getNumGeometries() == 0) {
      throw new IllegalStateException("Geometry is empty");
    }

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
