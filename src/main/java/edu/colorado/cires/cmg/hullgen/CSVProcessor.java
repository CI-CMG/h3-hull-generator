package edu.colorado.cires.cmg.hullgen;

import com.uber.h3core.util.GeoCoord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.locationtech.jts.geom.Geometry;

/**
 * Implementation of {@link InputFileProcessor} which computes hulls from CSV coordinates (longitude/latitude column order)
 */
public class CSVProcessor implements InputFileProcessor{

  private final String delimiters;
  private final Hull hull;

  /**
   * Constructor for {@link CSVProcessor}
   * @param delimiters regex {@link String} specifying pattern which divides CSV columns
   * @param hull {@link Hull} for generating hulls from H3 ids
   */
  public CSVProcessor(String delimiters, Hull hull) {
    this.delimiters = delimiters;
    this.hull = hull;
  }

  /**
   * Computes hulls from CSV coordinates
   * @param file {@link File} containing CSV coordinates
   * @return {@link Geometry} containing hull
   * @throws IOException if input {@link File} cannot be found
   */
  @Override
  public Geometry process(File file) throws IOException {
    try (
        InputStream inputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        GeoCoord geoCoord = csvLineToGeoCoord(line);
        hull.addPoint(geoCoord);
      }
    }
    hull.generateHull();
    return hull.getHullGeometry();
  }

  private GeoCoord csvLineToGeoCoord(String csvLine) {
    String[] yx = csvLine.split(delimiters);
    return new GeoCoord(Double.parseDouble(yx[1]), Double.parseDouble(yx[0]));
  }
}
