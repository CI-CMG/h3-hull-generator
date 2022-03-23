package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.IOException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

/**
 * Reads {@link org.locationtech.jts.geom.Geometry} from input {@link java.io.File}
 */
public interface OutputFileReader {

  /**
   * Reads {@link Geometry} from input {@link File}
   * @param file input {@link File}
   * @return {@link Geometry} from input {@link File}
   * @throws IOException if input {@link File} cannot be found
   * @throws ParseException if input {@link File} cannot be properly parsed
   */
  Geometry read(File file) throws IOException, ParseException;

  /**
   * Get file extension
   * @return file extension
   */
  String getExt();

}
