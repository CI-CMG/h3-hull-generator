package edu.colorado.cires.cmg.hullgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class CSVProcessorTest {

  final Path TEST_DIR = Paths.get("src/test/resources");
  final String delimiters = "[, ]";
  final int H3_RESOLUTION = 8;
  final GeometryFactory geometryFactory = new GeometryFactory();
  final double distanceTolerance = 0.07;
  final double deltaDistanceTolerance = 0.001;
  final int maxHullPointsAllowed = 10000;

  @Test
  public void testSmallFileCompleteHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("small_file.csv").toFile();

    CompleteGeometryProcessor geometryProcessor = new CompleteGeometryProcessor(H3_RESOLUTION, geometryFactory);
    CompleteHull hull = new CompleteHull(geometryProcessor);
    CSVProcessor csvProcessor = new CSVProcessor(delimiters, hull);

    Geometry outputGeometry = csvProcessor.process(TEST_FILE);

    assertEquals("Polygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.isValid());
    assertTrue(outputGeometry.isSimple());
  }

  @Test
  public void testSmallFileSimplifiedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("small_file.csv").toFile();

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, distanceTolerance, deltaDistanceTolerance, maxHullPointsAllowed
    );
    CompleteHull hull = new CompleteHull(geometryProcessor);
    CSVProcessor csvProcessor = new CSVProcessor(delimiters, hull);

    Geometry outputGeometry = csvProcessor.process(TEST_FILE);

    assertEquals("Polygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.isValid());
    assertTrue(outputGeometry.isSimple());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);
  }

  @Test
  public void testSmallFileSimplifiedBufferedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("small_file.csv").toFile();
    final int pointBufferSize = 100;

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, 0.01, deltaDistanceTolerance, maxHullPointsAllowed
    );
    BufferedHull hull = new BufferedHull(geometryProcessor, pointBufferSize);
    CSVProcessor csvProcessor = new CSVProcessor(delimiters, hull);

    Geometry outputGeometry = csvProcessor.process(TEST_FILE);

    assertEquals("Polygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.isValid());
    assertTrue(outputGeometry.isSimple());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);
  }

  @Test
  public void testLargeFileCompleteHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("large_file.csv").toFile();

    CompleteGeometryProcessor geometryProcessor = new CompleteGeometryProcessor(H3_RESOLUTION, geometryFactory);
    CompleteHull hull = new CompleteHull(geometryProcessor);
    CSVProcessor csvProcessor = new CSVProcessor(delimiters, hull);

    Geometry outputGeometry = csvProcessor.process(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }
  }

  @Test
  public void testLargeFileSimplifiedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("large_file.csv").toFile();

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, distanceTolerance, deltaDistanceTolerance, maxHullPointsAllowed
    );
    CompleteHull hull = new CompleteHull(geometryProcessor);
    CSVProcessor csvProcessor = new CSVProcessor(delimiters, hull);

    Geometry outputGeometry = csvProcessor.process(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }
  }

  @Test
  public void testLargeFileSimplifiedBufferedHull() throws IOException {
    final File TEST_FILE = TEST_DIR.resolve("large_file.csv").toFile();
    final int pointBufferSize = 10000;

    SimplifyingGeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        H3_RESOLUTION, geometryFactory, 0.01, deltaDistanceTolerance, maxHullPointsAllowed
    );
    BufferedHull hull = new BufferedHull(geometryProcessor, pointBufferSize);
    CSVProcessor csvProcessor = new CSVProcessor(delimiters, hull);

    Geometry outputGeometry = csvProcessor.process(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }
  }

}
