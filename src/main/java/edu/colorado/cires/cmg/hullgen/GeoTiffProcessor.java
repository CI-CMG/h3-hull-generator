package edu.colorado.cires.cmg.hullgen;

import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageMetadata;
import com.uber.h3core.util.GeoCoord;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.locationtech.jts.geom.Geometry;

/**
 * Implementation of {@link InputFileProcessor} which computes hulls from a GeoTiff image
 */
public class GeoTiffProcessor implements InputFileProcessor{

  private final int pixelArea;
  private final Hull hull;

  /**
   * Constructor for {@link CSVProcessor}
   * @param pixelArea width of squares used to read image in parts
   * @param hull {@link Hull} for generating hull from H3 ids
   */
  public GeoTiffProcessor(int pixelArea, Hull hull) {
    this.pixelArea = pixelArea;
    this.hull = hull;
  }

  /**
   * Computes hull from a GeoTIFF image
   * @param file {@link File} containing GeoTIFF image
   * @return {@link Geometry} containing hull
   * @throws IOException if input {@link File} does not exist
   */
  @Override
  public Geometry process(File file) throws IOException {
    try (
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)
        ) {
      Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
      if (!imageReaders.hasNext()) {
        throw new IllegalArgumentException("Image readers not found for: " + file.getName());
      }
      ImageReader imageReader = imageReaders.next();
      imageReader.setInput(imageInputStream);
      GeoTiffMetadata geoTiffMetadata = GeoTiffMetadata.fromTIFFImageMetadata(
          (TIFFImageMetadata) imageReader.getImageMetadata(0)
      );
      Rectangle readerWindow = initializeReaderWindow(geoTiffMetadata.getImageDimensions());
      Point readPoint = new Point(readerWindow.x, readerWindow.y);
      Raster raster;
      try {
        while (readerWindow.y < geoTiffMetadata.getImageDimensions().height) {
          ImageReadParam readParam = new ImageReadParam();
          readParam.setSourceRegion(readerWindow);
          raster = imageReader.readRaster(0, readParam);
          while (readPoint.x < readerWindow.x + readerWindow.width - 1) {
            int startingYPosition = readPoint.y;
            while (readPoint.y < readerWindow.y + readerWindow.height - 1) {
              Optional<GeoCoord> geoCoord = pixelToGeoCoord(readPoint, geoTiffMetadata, raster, readerWindow);
              geoCoord.ifPresent(hull::addPoint);
              readPoint = incrementPointY(readPoint);
            }
            readPoint = incrementPointToNextRow(readPoint, startingYPosition);
          }
          readerWindow = updateReaderWindow(readerWindow, readPoint, geoTiffMetadata.getImageDimensions());
          readPoint = updatePointToNewReaderWindow(readPoint, readerWindow);
        }
        hull.generateHull();
        return hull.getHullGeometry();
      } finally {
        imageReader.dispose();
      }
    }
  }

  private Rectangle initializeReaderWindow(Rectangle imageDimensions) {
    return new Rectangle(
        0,
        0,
        Math.min(pixelArea, imageDimensions.width),
        Math.min(pixelArea, imageDimensions.height)
    );
  }

  private Optional<GeoCoord> pixelToGeoCoord(Point point, GeoTiffMetadata metadata, Raster raster, Rectangle window) {
    int pixel = raster.getPixel(
        point.x - window.x,
        point.y - window.y,
        (int []) null
    )[0];

    if (pixel == 0 || pixel == 255) {
      return Optional.empty();
    }

    double relativeLat = metadata.getPixelScale().y * point.y;
    double relativeLon = metadata.getPixelScale().x * point.x;

    Point2D.Double tiePoint = metadata.getTiePoint();
    relativeLat = relativeLat > tiePoint.getY() ? relativeLat * 1 : relativeLat * -1;
    relativeLon = relativeLon > tiePoint.getX() ? relativeLon * 1 : relativeLon * -1;

    return Optional.of(new GeoCoord(
        metadata.getTiePoint().getY() + relativeLat,
        metadata.getTiePoint().getX() + relativeLon
    ));
  }

  private Point incrementPointY(Point point) {
    point.setLocation(point.x, point.y + 1);
    return point.getLocation();
  }

  private Point incrementPointToNextRow(Point point, int startingYPosition) {
    point.setLocation(point.x + 1, startingYPosition);
    return point.getLocation();
  }

  private Rectangle updateReaderWindow(Rectangle window, Point point, Rectangle imageDimensions) {
    int width = pixelArea;
    int height = window.height;
    int x = point.x + 1;
    int y = point.y;

    if (x + pixelArea > imageDimensions.width) {
      width = (imageDimensions.width - x);
    }
    if (x == imageDimensions.width) {
      x = 0;
      width = pixelArea;
      y += height;
      if (y + height > imageDimensions.height) {
        height = (imageDimensions.height - y);
      }
    }
    window.setBounds(x, y, width, height);
    return window.getBounds();
  }

  private Point updatePointToNewReaderWindow(Point point, Rectangle window) {
    point.setLocation(window.x, window.y);
    return point.getLocation();
  }
}
