package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.H3Core;
import java.io.IOException;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Implementation of {@link BaseGeometryProcessor} which processes H3 ids without simplification
 */
public class CompleteGeometryProcessor extends BaseGeometryProcessor{

  /**
   * Constructor for {@link CompleteGeometryProcessor}
   * @param h3Resolution integer from 0 (the lowest resolution) to 15 (the highest resolution) specifying size of H3 hexagons
   * @param geometryFactory {@link GeometryFactory} for generating and merging JTS geometries
   * @throws IOException if {@link H3Core} cannot create a new instance
   */
  public CompleteGeometryProcessor(Integer h3Resolution, GeometryFactory geometryFactory) throws IOException {
    super(h3Resolution, geometryFactory);
  }
}
