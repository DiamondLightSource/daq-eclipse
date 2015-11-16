package org.eclipse.scanning.api.points;

/**
 * This enum represents the type required to create a point generator.
 * It is called org.eclipse.scanning.api.points.ScanType, and should not be
 * confused with other enums for Scan Types, this one is specifically for
 * points generation.
 * 
 * @author Matthew Gerring
 *
 */
public enum ScanType {

	SINGLE_POINT, GRID, LISSAJOUS, ONED_EQUAL_SPACING, ONED_STEP_SCAN, RASTER, SPIRAL;
}
