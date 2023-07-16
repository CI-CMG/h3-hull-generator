package edu.colorado.cires.cmg.hullgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;

public class MultiFileHullMergerTest {

  final Path SOURCE_DIR = Paths.get("src/test/resources/");
  final Path TEST_DIR = Paths.get("target/test-dir");
  final int H3_RESOLUTION = 8;

  @BeforeEach
  public void beforeEach() throws IOException {
    FileUtils.deleteQuietly(TEST_DIR.toAbsolutePath().toFile());
    Files.createDirectory(TEST_DIR.toAbsolutePath());
  }

  @AfterEach
  public void afterEach() throws IOException {
    FileUtils.deleteDirectory(TEST_DIR.toAbsolutePath().toFile());
  }

  @Test
  public void testMultiFileHullMergerGeoJsonToWkt() throws IOException, ParseException {

    final File TEST_FILE = TEST_DIR.resolve("test-output.wkt").toFile();

    writeHullsCSVtoGeoJson();

    InputFileProcessor inputFileProcessor = new MultiFileHullMerger(
        new GeoJSONReader(), new CompleteGeometryProcessor(null, new GeometryFactory())
    );
    OutputFileWriter outputFileWriter = new WktWriter(null);
    HullGenerator hullGenerator = new HullGenerator(inputFileProcessor, outputFileWriter);
    hullGenerator.generate(new File(TEST_DIR.toString()), TEST_FILE);

    OutputFileReader outputFileReader = new WktReader();
    Geometry outputGeometry = outputFileReader.read(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }

  }

  @Test
  public void testMultiFileHullMergerWktToGeoJson() throws IOException, ParseException {

    final File TEST_FILE = TEST_DIR.resolve("test-output.geojson").toFile();

    writeHullsGeoTiffToWkt();

    InputFileProcessor inputFileProcessor = new MultiFileHullMerger(
        new WktReader(), new CompleteGeometryProcessor(null, new GeometryFactory())
    );
    OutputFileWriter outputFileWriter = new GeoJSONWriter(null);
    HullGenerator hullGenerator = new HullGenerator(inputFileProcessor, outputFileWriter);
    hullGenerator.generate(new File(TEST_DIR.toString()), TEST_FILE);

    OutputFileReader outputFileReader = new GeoJSONReader();
    Geometry outputGeometry = outputFileReader.read(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }

  }

  @Test
  public void testMultiFileHullMergerSimplifying() throws IOException, ParseException {

    final File TEST_FILE = TEST_DIR.resolve("test-output.wkt").toFile();

    writeHullsCSVtoGeoJson();

    int maxHullPointsAllowed = 1000;
    GeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
        null, new GeometryFactory(), 0.007, 0.001, maxHullPointsAllowed
    );

    InputFileProcessor inputFileProcessor = new MultiFileHullMerger(
        new GeoJSONReader(), geometryProcessor
    );
    OutputFileWriter outputFileWriter = new WktWriter(null);
    HullGenerator hullGenerator = new HullGenerator(inputFileProcessor, outputFileWriter);
    hullGenerator.generate(new File(TEST_DIR.toString()), TEST_FILE);

    OutputFileReader outputFileReader = new WktReader();
    Geometry outputGeometry = outputFileReader.read(TEST_FILE);

    assertEquals("MultiPolygon", outputGeometry.getGeometryType());
    assertTrue(outputGeometry.getNumPoints() < maxHullPointsAllowed);

    for (int i = 0; i < outputGeometry.getNumGeometries(); i++) {
      assertEquals("Polygon", outputGeometry.getGeometryN(i).getGeometryType());
      assertTrue(outputGeometry.getGeometryN(i).isValid());
      assertTrue(outputGeometry.getGeometryN(i).isSimple());
    }

  }

  private void writeHullsCSVtoGeoJson() throws IOException {
    Hull hull = new CompleteHull(
        new CompleteGeometryProcessor(H3_RESOLUTION, new GeometryFactory())
    );
    InputFileProcessor inputFileProcessor = new CSVProcessor("[, ]", hull);
    OutputFileWriter outputFileWriter = new GeoJSONWriter();

    HullGenerator hullGenerator = new HullGenerator(inputFileProcessor, outputFileWriter);
    Files.walk(SOURCE_DIR).forEach(file ->
        {
          if (file.getFileName().toString().endsWith(".csv") && !file.getFileName().toString().equals("polar_dateline.csv")) {
            String outputFileName = file.getFileName().toString().split("\\.")[0] + ".geojson";
            File outputFile = new File(TEST_DIR .resolve(outputFileName).toString());
            try {
              hullGenerator.generate(file.toFile(), outputFile);
            } catch (IOException e) {
              throw new IllegalStateException("Error generating hull for file: " + file.getFileName());
            }
          }
        }
    );
  }

  private void writeHullsGeoTiffToWkt() throws IOException {
    Hull hull = new CompleteHull(
        new CompleteGeometryProcessor(H3_RESOLUTION, new GeometryFactory())
    );
    InputFileProcessor inputFileProcessor = new GeoTiffProcessor(10000, hull);
    OutputFileWriter outputFileWriter = new WktWriter();

    HullGenerator hullGenerator = new HullGenerator(inputFileProcessor, outputFileWriter);
    Files.walk(SOURCE_DIR).forEach(file ->
        {
          if (file.getFileName().toString().endsWith(".tif")) {
            String outputFileName = file.getFileName().toString().split("\\.")[0] + ".wkt";
            File outputFile = new File(TEST_DIR .resolve(outputFileName).toString());
            try {
              hullGenerator.generate(file.toFile(), outputFile);
            } catch (IOException e) {
              throw new IllegalStateException("Error generating hull for file: " + file.getFileName());
            }
          }
        }
    );
  }

}
