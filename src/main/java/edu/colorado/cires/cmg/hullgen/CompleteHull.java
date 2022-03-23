package edu.colorado.cires.cmg.hullgen;

/**
 * Implementation of {@link BaseHull} which processes all H3 ids at once
 */
public class CompleteHull extends BaseHull{

  /**
   * Constructor for {@link BaseHull}
   * @param geometryProcessor {@link GeometryProcessor} for processing JTS geometries from H3 ids
   */
  public CompleteHull(GeometryProcessor geometryProcessor) {
    super(geometryProcessor);
  }
}
