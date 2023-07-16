package edu.colorado.cires.cmg.hullgen;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class GeoTiffProcessorTest {

  final Path TEST_DIR = Paths.get("src/test/resources");
  final int H3_RESOLUTION = 8;
  final int pixelArea = 10000;
  final GeometryFactory geometryFactory = new GeometryFactory();
  final double distanceTolerance = 0.007;
  final double deltaDistanceTolerance = 0.001;
  final int maxHullPointsAllowed = 10000;

  @Test
  public void testSmallFileCompleteHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("small_file.tif").toFile();
    CompleteGeometryProcessor geometryProcessor = new CompleteGeometryProcessor(H3_RESOLUTION, geometryFactory);
    CompleteHull hull = new CompleteHull(geometryProcessor);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(pixelArea, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    assertEquals("Polygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.isValid());
    assertTrue(outputGeometry.isSimple());
  }

  @Test
  public void testSmallFileSimplifiedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("small_file.tif").toFile();

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, distanceTolerance, deltaDistanceTolerance, maxHullPointsAllowed
    );
    CompleteHull hull = new CompleteHull(geometryProcessor);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(pixelArea, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    assertEquals("Polygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.isValid());
    assertTrue(outputGeometry.isSimple());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);
  }

  @Test
  public void testSmallFileSimplifiedBufferedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("small_file.tif").toFile();
    final int pointBufferSize = 100;

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, 0.01, deltaDistanceTolerance, maxHullPointsAllowed
    );
    BufferedHull hull = new BufferedHull(geometryProcessor, pointBufferSize);
    GeoTiffProcessor geoTiffProcessor= new GeoTiffProcessor(pixelArea, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    assertEquals("Polygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.isValid());
    assertTrue(outputGeometry.isSimple());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);
  }

  @Test
  public void testLargeFileCompleteHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("large_file.tif").toFile();

    CompleteGeometryProcessor geometryProcessor = new CompleteGeometryProcessor(H3_RESOLUTION, geometryFactory);
    CompleteHull hull = new CompleteHull(geometryProcessor);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(pixelArea, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }
  }

  @Test
  public void testKeepHolesCompleteHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("hole.tif").toFile();

    CompleteGeometryProcessor geometryProcessor = new CompleteGeometryProcessor(H3_RESOLUTION, geometryFactory, true);
    CompleteHull hull = new CompleteHull(geometryProcessor);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(100, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    Coordinate coordinate = new Coordinate();
    coordinate.setX(0);
    coordinate.setY(0);
    Point point = geometryFactory.createPoint(coordinate);

    assertFalse(outputGeometry.contains(point));
  }

  @Test
  public void testLargeFileSimplifiedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("large_file.tif").toFile();

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, distanceTolerance, deltaDistanceTolerance, maxHullPointsAllowed
    );
    CompleteHull hull = new CompleteHull(geometryProcessor);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(pixelArea, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }
  }

  @Test
  public void testKeepHolesSimplifiedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("hole.tif").toFile();

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, 0.00001, 0.000001, maxHullPointsAllowed, true
    );
    CompleteHull hull = new CompleteHull(geometryProcessor);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(100, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    Coordinate coordinate = new Coordinate();
    coordinate.setX(0);
    coordinate.setY(0);
    Point point = geometryFactory.createPoint(coordinate);

    assertFalse(outputGeometry.contains(point));
  }

  @Test
  public void testLargeFileSimplifiedBufferedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("large_file.tif").toFile();
    final int pointBufferSize = 10000;

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, distanceTolerance, deltaDistanceTolerance, maxHullPointsAllowed
    );
    BufferedHull hull = new BufferedHull(geometryProcessor, pointBufferSize);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(pixelArea, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }
  }

  @Test
  public void testKeepHolesSimplifiedBufferedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("hole.tif").toFile();
    final int pointBufferSize = 10000;

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, 0.00001, 0.000001, maxHullPointsAllowed, true
    );
    BufferedHull hull = new BufferedHull(geometryProcessor, pointBufferSize);
    GeoTiffProcessor geoTiffProcessor = new GeoTiffProcessor(100, hull);

    Geometry outputGeometry = geoTiffProcessor.process(TEST_FILE);

    Coordinate coordinate = new Coordinate();
    coordinate.setX(0);
    coordinate.setY(0);
    Point point = geometryFactory.createPoint(coordinate);

    assertFalse(outputGeometry.contains(point));
  }

}
