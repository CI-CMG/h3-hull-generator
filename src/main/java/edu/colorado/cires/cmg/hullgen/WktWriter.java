package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

/**
 * Writes from input {@link Geometry} to output WKT {@link File}
 */
public class WktWriter implements OutputFileWriter{

  private final CoordinateFilter coordinateFilter;

  public WktWriter(CoordinateFilter coordinateFilter) {
    this.coordinateFilter = coordinateFilter;
  }

  /**
   * Writes WKT string from input {@link Geometry} to output {@link File}
   * @param geometry input {@link Geometry}
   * @param outputFile output {@link File}
   * @throws IOException if output {@link File} already exists or cannot be created
   */
  @Override
  public void write(Geometry geometry, File outputFile) throws IOException {
    if (coordinateFilter != null) {
      geometry.apply(coordinateFilter);
    }

    WKTWriter wktWriter = new WKTWriter();
    String wktString = wktWriter.write(geometry);

    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
      outputStream.write(wktString.getBytes(StandardCharsets.UTF_8));
    }
  }
}
