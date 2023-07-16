package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.util.GeoCoord;
import java.util.HashSet;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * Base implementation of {@link Hull}
 */
public class BaseHull implements Hull{

  protected final GeometryProcessor geometryProcessor;
  protected Set<Long> points = new HashSet<>();
  protected Geometry hull;

  /**
   * Constructor for {@link BaseHull}
   * @param geometryProcessor {@link GeometryProcessor} for processing JTS geometries from H3 ids
   */
  public BaseHull(GeometryProcessor geometryProcessor) {
    this.geometryProcessor = geometryProcessor;
  }

  /**
   * Adds H3 id to point set
   * @param geoCoord {@link GeoCoord} containing latitude and longitude
   */
  @Override
  public void addPoint(GeoCoord geoCoord) {
    points.add(geometryProcessor.geoCoordToH3(geoCoord));
  }

  /**
   * Generates and merges hulls from H3 ids. Empties {@link HashSet<Long>} of points
   */
  @Override
  public void generateHull() {
    Geometry geometryOutlines = geometryProcessor.getGeometry(points);
    points = new HashSet<>();
    hull = geometryProcessor.mergeGeometryOutlines(geometryOutlines, hull);
  }

  /**
   * Gets {@link Geometry} containing hull
   * @return {@link Geometry} containing hull
   */
  @Override
  public Geometry getHullGeometry() {
    return hull;
  }
}
