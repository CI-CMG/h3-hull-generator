package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * Reads {@link Geometry} from WKT format
 */
public class WktReader implements OutputFileReader{

  /**
   * Reads {@link Geometry} from WKT format
   * @param file input {@link File}
   * @return {@link Geometry} from input file
   * @throws IOException if input {@link File} cannot be found
   * @throws ParseException if input {@link File} cannot be parsed as WKT
   */
  @Override
  public Geometry read(File file) throws IOException, ParseException {
    try (
        InputStream inputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        ) {

      WKTReader wktReader = new WKTReader();
      return wktReader.read(inputStreamReader);
    }
  }

  @Override
  public String getExt() {
    return "wkt";
  }
}
