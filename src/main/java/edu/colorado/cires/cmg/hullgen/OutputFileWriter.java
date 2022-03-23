package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.IOException;
import org.locationtech.jts.geom.Geometry;

/**
 * Writes {@link Geometry} to output {@link File}
 */
public interface OutputFileWriter {

  /**
   * Writes {@link Geometry} to output {@link File}
   * @param geometry {@link Geometry}
   * @param outputFile output {@link File}
   * @throws IOException if output {@link File} cannot be created or already exists
   */
  void write(Geometry geometry, File outputFile) throws IOException;

}
