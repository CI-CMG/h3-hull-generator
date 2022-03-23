# h3-hull-generator

The h3-hull-generator computes approximate concave hulls using Uber's H3 library and JTS.

## Adding to your project

Add the following dependency to your pom.xml

```xml
<dependency>
  <groupId>io.github.ci-cmg</groupId>
  <artifactId>h3-hull-generator</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Runtime Requirements
* Java 8

## Building From Source
Maven 3.6.0+ is required.
```bash
mvn clean install
```

## Supported Input File Formats
* CSV (longitude/latitude column order)
* GeoTiff

## Supported Output File Formats
* WKT
* GeoJSON

## Usage

### Generate hull from CSV coordinates and write to GeoJSON
```java
int h3Resolution = 8;
String delimiters = "[, ]";
GeometryFactory geometryFactory = new GeometryFactory();
GeometryProcessor geometryProcessor = new CompleteGeometryProcessor(h3Resolution, geometryFactory);
Hull hull = new CompleteHull(geometryProcessor);
InputFileProcessor inputFileProcessor = new CSVProcessor(delimiters, hull);

OutputFileWriter outputFileWriter = new GeoJSONWriter();

HullGenerator hullGenerator = new HullGenerator(inputFileProcessor, outputFileWriter);
hullGenerator.generate(inputFile, outputFile);
```

### Generate hull from GeoTiff image and write to WKT
```java
int h3Resolution = 8;
int pixelArea = 1000;
GeometryFactory geometryFactory = new GeometryFactory();
GeometryProcessor geometryProcessor = new CompleteGeometryProcessor(h3Resolution, geometryFactory);
Hull hull = new CompleteHull(geometryProcessor);
InputFileProcessor inputFileProcessor = new GeoTiffProcessor(pixelArea, hull);

OutputFileWriter outputFileWriter = new WktWriter();

HullGenerator hullGenerator = new HullGenerator(inputFileProcessor, outputFileWriter);
hullGenerator.generate(inputFile, outputFile);
```

### Get output hull as JTS Geometry
```java
int h3Resolution = 8;
String delimiters = "[, ]";
GeometryFactory geometryFactory = new GeometryFactory();
GeometryProcessor geometryProcessor = new CompleteGeometryProcessor(h3Resolution, geometryFactory);
Hull hull = new CompleteHull(geometryProcessor);
InputFileProcessor inputFileProcessor = new CSVProcessor(delimiters, hull);
Geometry geometry = inputFileProcessor.process(inputFile);
```

## Advanced Usage

### Self-simplifying GeometryGenerator
```java
int h3Resolution = 8;
GeometryFactory geometryFactory = new GeometryFactory();
double distanceTolerance = 0.07;
double deltaDistanceTolerance = 0.001;
int maxGeometryPointsAllowed = 10000;
GeometryProcessor geometryProcessor = new SimplifyingGeometryProcessor(
    h3Resolution, geometryFactory, distanceTolerance, deltaDistanceTolerance, maxGeometryPointsAllowed  
);
```

### Buffered Hull
```java
int pointBufferSize = 10000;
Hull hull = new BufferedHull(geometryProcessor, pointBufferSize);
```

### Merge hulls from multiple files
```java
InputFileProcessor inputFileProcessor = new MultiFileHullMerger(
        outputFileReader, geometryProcessor
);
```
