package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

/**
 * Writes from input {@link Geometry} to output GeoJson {@link File}
 */
public class GeoJSONWriter implements OutputFileWriter{

  private final CoordinateFilter coordinateFilter;

    /**
     * Constructor for {@link GeoJSONWriter}
     * @param coordinateFilter {@link CoordinateFilter} to apply to input {@link Geometry}
     */
  public GeoJSONWriter(CoordinateFilter coordinateFilter) {
    this.coordinateFilter = coordinateFilter;
  }

    /**
     * Constructor for {@link GeoJSONWriter}
     */
  public GeoJSONWriter() {
    this.coordinateFilter = null;
  }

  /**
   * Writes GeoJson string from input {@link Geometry} to output {@link File}
   * @param geometry input {@link Geometry}
   * @param outputFile output {@link File}
   * @throws IOException if output {@link File} already exists or cannot be created
   */
  @Override
  public void write(Geometry geometry, File outputFile) throws IOException {
    if (coordinateFilter != null) {
      geometry.apply(coordinateFilter);
    }

    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
    String geoJsonString =  geoJsonWriter.write(geometry);

    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
      outputStream.write(geoJsonString.getBytes(StandardCharsets.UTF_8));
    }
  }
}
