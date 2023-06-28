package edu.colorado.cires.cmg.hullgen;

import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageMetadata;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * Information related to a GeoTIFF image
 */
public class GeoTiffMetadata {

  private final Point2D.Double pixelScale;
  private final Point2D.Double tiePoint;

  private final Point2D.Double tiePointRaster;
  private final Rectangle imageDimensions;

  /**
   * Constructor for {@link GeoTiffMetadata}
   * @param pixelScale {@link Point2D.Double} containing per-pixel scale for latitude and longitude
   * @param tiePoint {@link Point2D.Double} containing the image's reference coordinate in latitude and longitude
   * @param imageDimensions {@link Rectangle} specifying height and width of image
   */
  public GeoTiffMetadata(Double pixelScale, Double tiePoint, Rectangle imageDimensions, Double tiePointRaster) {
    this.pixelScale = pixelScale;
    this.tiePoint = tiePoint;
    this.imageDimensions = imageDimensions;
    this.tiePointRaster = tiePointRaster;
  }

  public Double getPixelScale() {
    return pixelScale;
  }

  public Double getTiePoint() {
    return tiePoint;
  }

  public Double getTiePointRaster() {
    return tiePointRaster;
  }

  public Rectangle getImageDimensions() {
    return imageDimensions;
  }

  /**
   * Generates {@link GeoTiffMetadata} from {@link TIFFImageMetadata}
   * @param tiffImageMetadata {@link TIFFImageMetadata} containing TIFF image tags
   * @return {@link GeoTiffMetadata} related to a GeoTIFF image
   */
  public static GeoTiffMetadata fromTIFFImageMetadata(TIFFImageMetadata tiffImageMetadata) {
    return new GeoTiffMetadata(
        getPixelScale(tiffImageMetadata),
        getTiePoint(tiffImageMetadata),
        getImageDimensions(tiffImageMetadata),
        getTiePointRaster(tiffImageMetadata)
    );
  }

  /**
   * Gets pixel scale values from {@link TIFFImageMetadata}
   * @param tiffImageMetadata {@link TIFFImageMetadata} containing TIFF image tags
   * @return {@link Point2D.Double} containing per-pixel scale for latitude and longitude
   */
  public static Point2D.Double getPixelScale(TIFFImageMetadata tiffImageMetadata) {
    double[] pixelScale = (double[]) tiffImageMetadata.getTIFFField(GeoTiffTags.PIXEL_SCALE).getValue();
    return new Point2D.Double(pixelScale[0], pixelScale[1]);
  }

  /**
   * Gets reference tie point from {@link TIFFImageMetadata}
   * @param tiffImageMetadata {@link TIFFImageMetadata} containing TIFF image tags
   * @return {@link Point2D.Double} containing image's reference coordinate in latitude and longitude
   */
  public static Point2D.Double getTiePoint(TIFFImageMetadata tiffImageMetadata) {
    double[] tiePoint = (double[]) tiffImageMetadata.getTIFFField(GeoTiffTags.TIE_POINT).getValue();
    return new Point2D.Double(tiePoint[3], tiePoint[4]);
  }

  /**
   * Gets image dimensions from {@link TIFFImageMetadata}
   * @param tiffImageMetadata {@link TIFFImageMetadata} containing TIFF image tags
   * @return {@link Rectangle} specifying height and width of image
   */
  public static Rectangle getImageDimensions(TIFFImageMetadata tiffImageMetadata) {
    int imageWidth = (int) tiffImageMetadata.getTIFFField(GeoTiffTags.IMAGE_WIDTH).getValue();
    int imageHeight = (int) tiffImageMetadata.getTIFFField(GeoTiffTags.IMAGE_HEIGHT).getValue();
    return new Rectangle(0, 0, imageWidth, imageHeight);
  }

  public static Point2D.Double getTiePointRaster(TIFFImageMetadata tiffImageMetadata) {
    double[] tiePoint = (double[]) tiffImageMetadata.getTIFFField(GeoTiffTags.TIE_POINT).getValue();
    return new Point2D.Double(tiePoint[0], tiePoint[1]);
  }
}
