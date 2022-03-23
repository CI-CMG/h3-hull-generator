package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.H3Core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

/**
 * Implementation of {@link BaseGeometryProcessor} which simplifies output geometry
 */
public class SimplifyingGeometryProcessor extends BaseGeometryProcessor{

  private final double distanceTolerance;
  private final double deltaDistanceTolerance;
  private final double maxGeometryPointsAllowed;

  /**
   *
   * @param h3Resolution integer from 0 (the lowest resolution) to 15 (the highest resolution) specifying size of H3 hexagons
   * @param geometryFactory {@link GeometryFactory} for generating and merging JTS geometries
   * @param distanceTolerance Douglas-Peucker algorithm distance tolerance
   * @param deltaDistanceTolerance Granularity of change in distance tolerance for successive attempts of Douglas-Peucker algorithm
   * @param maxGeometryPointsAllowed maximum allowed points allowed in output {@link Geometry}
   * @throws IOException if {@link H3Core} cannot create a new instance
   */
  public SimplifyingGeometryProcessor(Integer h3Resolution, GeometryFactory geometryFactory, double distanceTolerance, double deltaDistanceTolerance,
      double maxGeometryPointsAllowed) throws IOException {
    super(h3Resolution, geometryFactory);
    this.distanceTolerance = distanceTolerance;
    this.deltaDistanceTolerance = deltaDistanceTolerance;
    this.maxGeometryPointsAllowed = maxGeometryPointsAllowed;
  }

  /**
   * Transforms H3 ids into a simplified {@link List<Geometry>} containing outer rings. Applies Douglas-Peucker algorithm to outer rings
   * @param points {@link Collection<Long>} containing H3 ids
   * @return {@link List<Geometry>} containing outer rings of neighboring hexagons
   */
  @Override
  public List<Geometry> getGeometryOutlines(Collection<Long> points) {
    List<Long> pointsWithMissingNeighbors = getPointsWithMissingNeighbors(points);
    return super.getGeometryOutlines(pointsWithMissingNeighbors).parallelStream()
        .map(geometry -> DouglasPeuckerSimplifier.simplify(geometry, distanceTolerance))
        .collect(Collectors.toList());
  }

  /**
   * Unions {@link List<Geometry>} into a single {@link Geometry}. Successively applies Douglas-Peucker algorithm until output geometry is at or below the specified point count
   * @param geometries {@link List<Geometry>} separated geometries
   * @param existingGeometry {@link Geometry} existing geometry
   * @return {@link Geometry} containing unions of all input geometry
   */
  @Override
  public Geometry mergeGeometryOutlines(List<Geometry> geometries, Geometry existingGeometry) {
   Geometry mergedGeometryOutlines = super.mergeGeometryOutlines(geometries, existingGeometry);
   double mergedGeometryOutlineDistanceTolerance = distanceTolerance;
   while (mergedGeometryOutlines.getNumPoints() >= maxGeometryPointsAllowed) {
     mergedGeometryOutlines = DouglasPeuckerSimplifier.simplify(
         mergedGeometryOutlines, mergedGeometryOutlineDistanceTolerance
     );
     mergedGeometryOutlineDistanceTolerance += deltaDistanceTolerance;
   }
   return super.processAnteMeridian(mergedGeometryOutlines);
  }

  private List<Long> getPointsWithMissingNeighbors(Collection<Long> points) {
    List<Long> pointsWithMissingNeighbors = new ArrayList<>();
    for (long point : points) {
      if (!points.containsAll(h3Core.kRing(point, 1))) {
        pointsWithMissingNeighbors.add(point);
      }
    }
    return pointsWithMissingNeighbors;
  }

}
