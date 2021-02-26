package unirioja.spheroidj_colageno;

import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class Utils {

	// Method to search the list of files that satisfies a pattern in a folder. The
	// list of files
	// is stored in the result list.
	public static void search(final String pattern, final File folder, List<String> result) {
		for (final File f : folder.listFiles()) {

			if (f.isDirectory()) {
				search(pattern, f, result);
			}

			if (f.isFile()) {
				if (f.getName().matches(pattern) && !f.getName().contains("pred")) {
					result.add(f.getAbsolutePath());
				}
			}

		}
	}

	// Method to draw the results stored in the roi manager into the image, and then
	// save the
	// image in a given directory. Since we know that there is only one esferoide
	// per image, we
	// only keep the ROI with the biggest area stored in the ROI Manager.
	// Method to draw the results stored in the roi manager into the image, and then
	// save the
	// image in a given directory. Since we know that there is only one esferoide
	// per image, we
	// only keep the ROI with the biggest area stored in the ROI Manager.
	public static void showResultsAndSave(String dir, String name, ImagePlus imp1, RoiManager rm,
			ArrayList<Integer> goodRows) throws IOException {

		IJ.run(imp1, "RGB Color", "");

		// String name = imp1.getTitle();

		// FileInfo f = imp1.getFileInfo();
		name = name.substring(0, name.lastIndexOf("."));

		ImageStatistics stats = null;
		double[] vFeret;// = 0;
		double perimeter = 0;
		if (rm != null) {
			rm.setVisible(false);
			// keepBiggestROI(rm);
			rm.runCommand("Show None");
			rm.runCommand("Show All");
			// ImagePlus imp2 = imp1.flatten();
			rm.runCommand(imp1, "Draw");
			rm.runCommand("Save", name + ".zip");
			rm.close();

			Roi[] roi = rm.getRoisAsArray();
			// Metrics for spheroid area
			stats = roi[0].getStatistics();

			vFeret = roi[0].getFeretValues();// .getFeretsDiameter();
			perimeter = roi[0].getLength();
			Calibration cal = imp1.getCalibration();
			double pw, ph;
			if (cal != null) {
				pw = cal.pixelWidth;
				ph = cal.pixelHeight;
			} else {
				pw = 1.0;
				ph = 1.0;
			}
			// calibrate the measures
			double area = stats.area * pw * ph;
			double w = imp1.getWidth() * pw;
			double h = imp1.getHeight() * ph;
			double aFraction = area / (w * h) * 100;
			double perim = perimeter * pw;

			ResultsTable rt = ResultsTable.getResultsTable();
//	            if (rt == null) {
			//
//	                rt = new ResultsTable();
//	            }
			int nrows = Analyzer.getResultsTable().getCounter();
			goodRows.add(nrows - 1);

			rt.setPrecision(2);
			rt.setLabel(name, nrows - 1);
			rt.addValue("Spheroid Area", area);
			rt.addValue("Spheroid Area Fraction", aFraction);
			rt.addValue("Spheroid Perimeter", perim);
			double circularity = perimeter == 0.0 ? 0.0 : 4.0 * Math.PI * (area / (perim * perim));
			if (circularity > 1.0) {
				circularity = 1.0;
			}
			rt.addValue("Spheroid Circularity", circularity);
			rt.addValue("Spheroid Diam. Feret", vFeret[0]);
			rt.addValue("Spheroid Angle. Feret", vFeret[1]);
			rt.addValue("Spheroid Min. Feret", vFeret[2]);
			rt.addValue("Spheroid X Feret", vFeret[3]);
			rt.addValue("Spheroid Y Feret", vFeret[4]);

			// Metrics for annulus and points
			if (roi.length > 1) {
				// Metrics for annulus
				stats = roi[1].getStatistics();

				vFeret = roi[1].getFeretValues();// .getFeretsDiameter();
				perimeter = roi[1].getLength();

				// calibrate the measures
				double areaAnnulus = stats.area * pw * ph;
				w = imp1.getWidth() * pw;
				h = imp1.getHeight() * ph;
				double aFractionAnnulus = area / (w * h) * 100;
				double perimAnnulus = perimeter * pw;

				nrows = Analyzer.getResultsTable().getCounter();
				goodRows.add(nrows - 1);

				rt.setPrecision(2);
				rt.setLabel(name, nrows - 1);
				rt.addValue("Annulus Area", areaAnnulus);
				rt.addValue("Annulus Area Fraction", aFractionAnnulus);
				rt.addValue("Annulus Perimeter", perimAnnulus);
				double circularityAnnulus = perimeter == 0.0 ? 0.0 : 4.0 * Math.PI * (area / (perim * perim));
				if (circularityAnnulus > 1.0) {
					circularityAnnulus = 1.0;
				}
				rt.addValue("Annulus Circularity", circularityAnnulus);
				rt.addValue("Annulus Diam. Feret", vFeret[0]);
				rt.addValue("Annulus Angle. Feret", vFeret[1]);
				rt.addValue("Annulus Min. Feret", vFeret[2]);
				rt.addValue("Annulus X Feret", vFeret[3]);
				rt.addValue("Annulus Y Feret", vFeret[4]);

				// Metrics for points
				PointRoi pr = (PointRoi) roi[2];
				PointRoi center = (PointRoi) roi[3];
				FloatPolygon fp = pr.getContainedFloatPoints();
				FloatPolygon fpCenter = center.getContainedFloatPoints();
				float xpoints[] = fp.xpoints;
				float ypoints[] = fp.ypoints;
				float xCenter = fp.xpoints[0];
				float yCenter = fp.ypoints[0];

				double distances[] = new double[fp.npoints];
				for (int i = 0; i < fp.npoints; i++) {
					double dx = (xpoints[i] - xCenter) * pw;
					double dy = (ypoints[i] - yCenter) * ph;
					distances[i] = Math.sqrt(dx * dx + dy * dy);
				}

				rt.addValue("# of cells", fp.npoints);
				rt.addValue("Mean distance of cells", findMean(distances, fp.npoints));
				rt.addValue("Median distance of cells", findMedian(distances, fp.npoints));

			}

			imp1.changes = false;
			imp1.close();
			IJ.saveAs(imp1, "Tiff", name + "_pred.tiff");

		}

	}

	public static void showResultsAndSavev2(String dir, String name, ImagePlus imp1, RoiManager rm,
			ArrayList<Integer> goodRows) throws IOException {

		IJ.run(imp1, "RGB Color", "");

		// String name = imp1.getTitle();

		// FileInfo f = imp1.getFileInfo();
		name = name.substring(0, name.lastIndexOf("."));

		ImageStatistics stats = null;
		double[] vFeret;// = 0;
		double perimeter = 0;
		if (rm != null) {
			rm.setVisible(false);
			// keepBiggestROI(rm);
			rm.runCommand("Show None");
			rm.runCommand("Show All");
			// ImagePlus imp2 = imp1.flatten();
			rm.runCommand(imp1, "Draw");
			rm.runCommand("Save", name + ".zip");
			rm.close();

			Roi[] roi = rm.getRoisAsArray();
			// Metrics for spheroid area
			stats = roi[0].getStatistics();

			vFeret = roi[0].getFeretValues();// .getFeretsDiameter();
			perimeter = roi[0].getLength();
			Calibration cal = imp1.getCalibration();
			double pw, ph;
			if (cal != null) {
				pw = cal.pixelWidth;
				ph = cal.pixelHeight;
			} else {
				pw = 1.0;
				ph = 1.0;
			}
			// calibrate the measures
			double area = stats.area * pw * ph;
			double w = imp1.getWidth() * pw;
			double h = imp1.getHeight() * ph;
			double aFraction = area / (w * h) * 100;
			double perim = perimeter * pw;

			ResultsTable rt = ResultsTable.getResultsTable();
//	            if (rt == null) {
			//
//	                rt = new ResultsTable();
//	            }
			int nrows = Analyzer.getResultsTable().getCounter();
			goodRows.add(nrows - 1);

			rt.setPrecision(2);
			rt.setLabel(name, nrows - 1);
			rt.addValue("Spheroid Area", area);
			rt.addValue("Spheroid Area Fraction", aFraction);
			rt.addValue("Spheroid Perimeter", perim);
			double circularity = perimeter == 0.0 ? 0.0 : 4.0 * Math.PI * (area / (perim * perim));
			if (circularity > 1.0) {
				circularity = 1.0;
			}
			rt.addValue("Spheroid Circularity", circularity);
			rt.addValue("Spheroid Diam. Feret", vFeret[0]);
			rt.addValue("Spheroid Angle. Feret", vFeret[1]);
			rt.addValue("Spheroid Min. Feret", vFeret[2]);
			rt.addValue("Spheroid X Feret", vFeret[3]);
			rt.addValue("Spheroid Y Feret", vFeret[4]);

			// Metrics for annulus and points
			if (roi.length > 2) {

				// Metrics for points
				if (roi[1] instanceof PointRoi && roi[2] instanceof PointRoi) {
					PointRoi pr = (PointRoi) roi[1];
					PointRoi center = (PointRoi) roi[2];
					FloatPolygon fp = pr.getContainedFloatPoints();
					FloatPolygon fpCenter = center.getContainedFloatPoints();
					float xpoints[] = fp.xpoints;
					float ypoints[] = fp.ypoints;
					float xCenter = fp.xpoints[0];
					float yCenter = fp.ypoints[0];

					double distances[] = new double[fp.npoints];
					for (int i = 0; i < fp.npoints; i++) {
						double dx = (xpoints[i] - xCenter) * pw;
						double dy = (ypoints[i] - yCenter) * ph;
						distances[i] = Math.sqrt(dx * dx + dy * dy);
					}

					rt.addValue("# of cells", fp.npoints);
					rt.addValue("Mean distance of cells", findMean(distances, fp.npoints));
					rt.addValue("Median distance of cells", findMedian(distances, fp.npoints));

				}else {
					System.out.println(name);
				}
			}

			imp1.changes = false;
			imp1.close();
			IJ.saveAs(imp1, "Tiff", name + "_pred.tiff");

		}

	}

	// Function for calculating mean
	public static double findMean(double a[], int n) {
		int sum = 0;
		for (int i = 0; i < n; i++)
			sum += a[i];

		return (double) sum / (double) n;
	}

	// Function for calculating median
	public static double findMedian(double a[], int n) {
		// First we sort the array
		Arrays.sort(a);

		// check for even case
		if (n % 2 != 0)
			return (double) a[n / 2];

		return (double) (a[(n - 1) / 2] + a[n / 2]) / 2.0;
	}

	// Method to obtain the area from a polygon. Probably, there is a most direct
	// method to do this.
	protected static final double getArea(Polygon p) {
		if (p == null)
			return Double.NaN;
		int carea = 0;
		int iminus1;
		for (int i = 0; i < p.npoints; i++) {
			iminus1 = i - 1;
			if (iminus1 < 0)
				iminus1 = p.npoints - 1;
			carea += (p.xpoints[i] + p.xpoints[iminus1]) * (p.ypoints[i] - p.ypoints[iminus1]);
		}
		return (Math.abs(carea / 2.0));
	}

	// Method to keep the ROI with the biggest area stored in the ROIManager, the
	// rest of ROIs are
	// deleted.
	protected static void keepBiggestROI(RoiManager rm) {
		if (rm != null) {
			Roi[] rois = rm.getRoisAsArray();

			if (rois.length >= 1) {
				rm.runCommand("Select All");
				rm.runCommand("Delete");

				Roi biggestROI = rois[0];

				for (int i = 1; i < rois.length; i++) {

					if (getArea(biggestROI.getPolygon()) < getArea(rois[i].getPolygon())) {

						biggestROI = rois[i];
					}

				}
//					IJ.showMessage(""+getArea(biggestROI.getPolygon()));
				rm.addRoi(biggestROI);

			}

		}
	}

	public static double mean(int[] m) {
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			sum += m[i];
		}
		return sum / m.length;
	}

	public static int countBelowThreshold(ImagePlus imp1, int threshold) {

		ImageProcessor ip = imp1.getProcessor();
		int[] histogram = ip.getHistogram();
		if (histogram.length < threshold) {
			threshold = histogram.length;
		}

		int countpixels = 0;
		for (int i = 0; i < threshold; i++) {
			countpixels = countpixels + histogram[i];
		}

		return countpixels;

	}

	public static boolean countBetweenThresholdOver(ImagePlus imp1, int threshold1, int threshold2, int num) {

		ImageProcessor ip = imp1.getProcessor();
		int[] histogram = ip.getHistogram(256);
		ImageStatistics is = ip.getStatistics();
		double min = is.min;
//		System.out.println(min);
		double max = is.max;
//		System.out.println(max);
		double range = (max - min) / 256;

		int i = 0;
		double pos = min;
		while (pos < threshold1) {
			pos = pos + range;
			i++;

		}

		while (pos < threshold2 && i < histogram.length - 1) {
			if (histogram[i] < num) {
				return true;
			}
			i++;
			pos = pos + range;
			System.out.println(pos);
		}

		return false;

	}

}
