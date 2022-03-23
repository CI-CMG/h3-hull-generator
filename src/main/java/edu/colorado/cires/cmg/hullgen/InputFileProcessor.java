package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.IOException;
import org.locationtech.jts.geom.Geometry;

/**
 * Generates hull from input {@link File}
 */
public interface InputFileProcessor {

  /**
   * Generates hull from input {@link File}
   * @param file input {@link File}
   * @return {@link Geometry} containing hull
   * @throws IOException if input {@link File} cannot be found
   */
  Geometry process(File file) throws IOException;

}
