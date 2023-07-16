package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

/**
 * Merges {@link Geometry} from multiple files
 */
public class MultiFileHullMerger implements InputFileProcessor{

  private final OutputFileReader outputFileReader;
  private final GeometryProcessor geometryProcessor;

  /**
   * Constructor for {@link MultiFileHullMerger}
   * @param outputFileReader {@link OutputFileReader} for reading {@link Geometry} from input {@link File}
   * @param geometryProcessor {@link GeometryProcessor} for processing geometries
   */
  public MultiFileHullMerger(OutputFileReader outputFileReader, GeometryProcessor geometryProcessor) {
    this.outputFileReader = outputFileReader;
    this.geometryProcessor = geometryProcessor;
  }

  /**
   * Merges {@link Geometry} from directory of files
   * @param file input {@link File}
   * @return {@link Geometry} containing merged geometry
   * @throws IOException if input {@link File} cannot be found
   */
  @Override
  public Geometry process(File file) throws IOException {
    List<File> inputFiles = Files.walk(file.toPath())
        .filter(path -> path.getFileName().toString().endsWith(outputFileReader.getExt()))
        .map(Path::toFile)
        .collect(Collectors.toList());

    Geometry hull = null;

    try {
      for (File inputFile : inputFiles) {
        Geometry geometry = outputFileReader.read(inputFile);
        if (hull == null) {
          hull = geometry;
          continue;
        }
        hull = geometryProcessor.mergeGeometryOutlines(geometry, hull);
      }
      return hull;
    } catch (ParseException e) {
      throw new IllegalStateException("Geometry could no be parsed from file: " + file.getName());
    }
  }
}
