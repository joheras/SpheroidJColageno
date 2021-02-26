package unirioja.spheroidj_colageno;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.hamcrest.core.IsInstanceOf;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import ij.process.ImageStatistics;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class DetectSpheroidMethods {

	// Method to detect esferoides.
	public static void detectSpheroidFluoColageno(String dir, String name, ArrayList<Integer> goodRows) {

		if ((new File(name.replace("fluo", "")).exists())) {
			ImagePlus impFluo = IJ.openImage(name);

			name = name.replace("fluo", "");
			ImagePlus impNoFluo = IJ.openImage(name);

			String title = impNoFluo.getTitle();

			ImagePlus imp = impNoFluo.duplicate();
			imp.setTitle(title);

			impFluo = DetectSpheroidImageMethods.processSpheroidFluo(impFluo);
			impNoFluo = DetectSpheroidImageMethods.processSpheroidNoFluo(impNoFluo);
			ImagePlus impFluoD = impFluo.duplicate();
			ImageCalculator ic = new ImageCalculator();

			ImagePlus imp3 = ic.run("Add create", impNoFluo, impFluo);
			IJ.run(imp3, "Fill Holes", "");
			// imp3.show();
//		impFluo.show();
//		impNoFluo.show();
			ImagePlus imp4 = imp.duplicate();

			RoiManager rm = AnalyseParticleMethods.analyzeParticlesAnnulus(imp3);
			Utils.keepBiggestROI(rm);

			Roi annulusRoi = rm.getRoi(0);
			rm.runCommand("Delete");

			imp3 = DetectSpheroidImageMethods.processSpheroidNoFluoWithRoi(imp.duplicate(), annulusRoi);

			// Para los pequeños
			ImagePlus imp5 = ic.run("Add create", imp3, impFluoD);
			IJ.run(imp5, "Fill Holes", "");
			rm = AnalyseParticleMethods.analyzeParticlesAnnulus(imp5);
			Utils.keepBiggestROI(rm);
			Roi spheroidRoiSmall = rm.getRoi(0);
			rm.runCommand("Delete");
			// Para los grandes
			IJ.run(imp3, "Fill Holes", "");
			rm = AnalyseParticleMethods.analyzeParticlesAnnulus(imp3);
			Utils.keepBiggestROI(rm);
			Roi spheroidRoiBig = rm.getRoi(0);
			rm.runCommand("Delete");

			ImagePlus[] channels = ChannelSplitter.split(imp4);
			channels[0].close();
			channels[2].close();
			imp4 = channels[1];
			imp4.setCalibration(imp.getCalibration());
			IJ.run(imp4, "Median...", "radius=3");
			IJ.run(imp4, "Sharpen", "");
			imp4.setRoi(annulusRoi);
			IJ.run(imp4, "Enlarge...", "enlarge=100 pixel");
			IJ.run(imp4, "To Bounding Box", "");
			IJ.run(imp4, "Find Maxima...", "prominence=15 strict light output=[Point Selection]");
			PointRoi pr = (PointRoi) imp4.getRoi();

			int containedSmall = 0;
			int containedAnnulus = 0;
			FloatPolygon fp = pr.getContainedFloatPoints();
			float xpoints[] = fp.xpoints;
			float ypoints[] = fp.ypoints;
			FloatPolygon fpNew = new FloatPolygon();

			for (int i = 0; i < fp.npoints; i++) {
				if (spheroidRoiSmall.contains((int) xpoints[i], (int) ypoints[i])) {
					containedSmall++;
				}

				if (annulusRoi.contains((int) xpoints[i], (int) ypoints[i])) {
					containedAnnulus++;
				}

				if (annulusRoi.contains((int) xpoints[i], (int) ypoints[i])
						&& !(spheroidRoiSmall.contains((int) xpoints[i], (int) ypoints[i]))
						|| !(annulusRoi.contains((int) xpoints[i], (int) ypoints[i]))) {
					fpNew.addPoint(xpoints[i], ypoints[i]);
				}

			}

			if (true) { // containedSmall * 2 < containedAnnulus
				spheroidRoiBig.setName("spheroid");
				spheroidRoiBig.setStrokeColor(new Color(255, 0, 0));
				rm.addRoi(spheroidRoiBig);
				annulusRoi.setName("annulus");
				annulusRoi.setStrokeColor(new Color(0, 255, 0));
				rm.addRoi(annulusRoi);
				PointRoi cells = new PointRoi(fpNew);
				cells.setName("cells");
				cells.setStrokeColor(new Color(0, 0, 255));
				rm.addRoi(cells);
				double result[] = spheroidRoiBig.getContourCentroid();
				PointRoi center = new PointRoi((int) result[0], (int) result[1]);
				center.setName("center");
				rm.addRoi(center);

			} else {
				spheroidRoiSmall.setName("spheroid");
				spheroidRoiSmall.setStrokeColor(new Color(255, 0, 0));
				rm.addRoi(spheroidRoiSmall);
			}

//		imp4.changes=false;
//		imp4.close();
			imp3.close();
			impFluo.changes = false;
			impNoFluo.changes = false;
			impFluo.close();
			impNoFluo.close();
			try {

				Utils.showResultsAndSave(dir, name, imp, rm, goodRows);

				imp.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidFluoColagenov2(String dir, String name, ArrayList<Integer> goodRows) {

		if ((new File(name.replace("fluo", "")).exists())) {
			// System.out.println(name);
			ImagePlus impFluo = IJ.openImage(name);

			name = name.replace("fluo", "");
			ImagePlus impNoFluo = IJ.openImage(name);

			String title = impNoFluo.getTitle();

			ImagePlus imp = impNoFluo.duplicate();
			imp.setTitle(title);

			impFluo = DetectSpheroidImageMethods.processSpheroidFluo(impFluo);
			impNoFluo = DetectSpheroidImageMethods.processSpheroidNoFluov2(impNoFluo);
			ImagePlus impFluoD = impFluo.duplicate();
			ImageCalculator ic = new ImageCalculator();

			ImagePlus imp3 = ic.run("Add create", impNoFluo, impFluo);
			IJ.run(imp3, "Fill Holes", "");

			ImagePlus imp4 = imp.duplicate();

			RoiManager rm = AnalyseParticleMethods.analyzeParticlesAnnulus(imp3);
			Utils.keepBiggestROI(rm);

			rm.select(0);
			imp3.setRoi(rm.getRoi(0));
			IJ.run(imp3, "Fit Spline", "");
			Roi spheroidROI = imp3.getRoi();

			rm.runCommand("Delete");

			ImagePlus[] channels = ChannelSplitter.split(imp4);
			if (channels.length > 1) {
				channels[0].close();
				channels[2].close();
				imp4 = channels[1];
			}

			imp4.setCalibration(imp.getCalibration());
			IJ.run(imp4, "Median...", "radius=3");
			IJ.run(imp4, "Sharpen", "");
			imp4.setRoi(spheroidROI);
			IJ.run(imp4, "Enlarge...", "enlarge=100 pixel");
			rm.addRoi(imp4.getRoi());
			IJ.run(imp4, "Select All", "");
			IJ.run(imp4, "Enlarge...", "enlarge=-50 pixel");
			rm.addRoi(imp4.getRoi());
			rm.setSelectedIndexes(new int[] { 0, 1 });
			rm.runCommand(imp4, "AND");
			// Antes era 15
			IJ.run(imp4, "Find Maxima...", "prominence=15 strict light output=[Point Selection]");
			rm.runCommand("Delete");
			if (imp4.getRoi() instanceof PointRoi) {
				PointRoi pr = (PointRoi) imp4.getRoi();
				

				// Para comprobar que mayoría de puntos están cerca
				imp4.deleteRoi();
				imp4.setRoi(spheroidROI);
				IJ.run(imp4, "Enlarge...", "enlarge=15 pixel");
				IJ.run(imp4, "To Bounding Box", "");
				IJ.run(imp4, "Find Maxima...", "prominence=15 strict light output=[Point Selection]");
				if (imp4.getRoi() instanceof PointRoi) {
					PointRoi pr2 = (PointRoi) imp4.getRoi();
					FloatPolygon fp2 = pr2.getContainedFloatPoints();

					FloatPolygon fp = pr.getContainedFloatPoints();
					

					float xpoints[] = fp.xpoints;
					float ypoints[] = fp.ypoints;
					FloatPolygon fpNew = new FloatPolygon();
					FloatPolygon fpNew2 = new FloatPolygon();

					for (int i = 0; i < fp.npoints; i++) {

						if (!(spheroidROI.contains((int) xpoints[i], (int) ypoints[i]))) {
							fpNew.addPoint(xpoints[i], ypoints[i]);
						}

					}
					
					xpoints = fp2.xpoints;
					ypoints = fp2.ypoints;
					for (int i = 0; i < fp2.npoints; i++) {

						if (!(spheroidROI.contains((int) xpoints[i], (int) ypoints[i]))) {
							fpNew2.addPoint(xpoints[i], ypoints[i]);
						}

					}
					
					
					
					if (fpNew2.npoints < (fpNew.npoints - fpNew2.npoints)) {

						fpNew = fpNew2;
					}

					spheroidROI.setName("spheroid");
					spheroidROI.setStrokeColor(new Color(255, 0, 0));
					rm.addRoi(spheroidROI);

					if (fpNew.npoints > 5) {
						PointRoi cells = new PointRoi(fpNew);
						cells.setName("cells");
						cells.setStrokeColor(new Color(0, 0, 255));
						rm.addRoi(cells);
						double result[] = spheroidROI.getContourCentroid();
						PointRoi center = new PointRoi((int) result[0], (int) result[1]);
						center.setName("center");
						rm.addRoi(center);
					}
				}else {

					spheroidROI.setName("spheroid");
					spheroidROI.setStrokeColor(new Color(255, 0, 0));
					rm.addRoi(spheroidROI);
				}
			} else {
				
				spheroidROI.setName("spheroid");
				spheroidROI.setStrokeColor(new Color(255, 0, 0));
				rm.addRoi(spheroidROI);
			}

			imp3.changes = false;
			imp3.close();
			impFluo.changes = false;
			impNoFluo.changes = false;
			impFluo.close();
			impNoFluo.close();
			try {

				Utils.showResultsAndSavev2(dir, name, imp, rm, goodRows);

				imp.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
