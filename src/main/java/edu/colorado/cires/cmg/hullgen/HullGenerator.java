package edu.colorado.cires.cmg.hullgen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.locationtech.jts.geom.Geometry;

/**
 * Computes hull from input file and writes to output file
 */
public class HullGenerator {

  private final InputFileProcessor inputFileProcessor;
  private final OutputFileWriter outputFileWriter;

  /**
   * Constructor for {@link HullGenerator}
   * @param inputFileProcessor {@link InputFileProcessor} for generating hull from input file
   * @param outputFileWriter {@link OutputFileWriter} for writing hull to formatted file
   */
  public HullGenerator(InputFileProcessor inputFileProcessor, OutputFileWriter outputFileWriter) {
    this.inputFileProcessor = inputFileProcessor;
    this.outputFileWriter = outputFileWriter;
  }

  /**
   * Computes hull from input file and writes to output file
   * @param inputFile input {@link File}
   * @param outputFile output {@link File}
   * @throws IOException if output {@link File} cannot be created or already exists
   */
  public void generate(File inputFile, File outputFile) throws IOException {
    Geometry hull = inputFileProcessor.process(inputFile);
    Files.createFile(outputFile.toPath());
    outputFileWriter.write(hull, outputFile);
  }

}
