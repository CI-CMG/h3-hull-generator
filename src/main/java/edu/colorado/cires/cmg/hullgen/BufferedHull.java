package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.util.GeoCoord;

/**
 * Implementation of {@link BaseHull} which generates and merges hulls according to the size of its point buffer
 */
public class BufferedHull extends BaseHull{

  private final int pointBufferSize;

  /**
   * Constructor for {@link BufferedHull}
   * @param geometryProcessor {@link GeometryProcessor} for processing JTS geometries from H3 ids
   * @param pointBufferSize the size of the point buffer
   */
  public BufferedHull(GeometryProcessor geometryProcessor, int pointBufferSize) {
    super(geometryProcessor);
    this.pointBufferSize = pointBufferSize;
  }

  /**
   * Adds a point to the point buffer, then checks buffer size. If buffer limit is exceeded, hulls are computed while emptying the point buffer
   * @param geoCoord {@link GeoCoord} containing latitude and longitude
   */
  @Override
  public void addPoint(GeoCoord geoCoord) {
    super.addPoint(geoCoord);
    if (points.size() == pointBufferSize) {
      generateHull();
    }
  }
}
