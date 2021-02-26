package unirioja.spheroidj_colageno;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.mchange.v1.lang.GentleThread;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;

public class DetectSpheroidImageMethods {

	public static ImagePlus processSpheroidNoFluo(ImagePlus imp2) {
		ImagePlus[] channels = ChannelSplitter.split(imp2);
		if (channels.length >1) {
			channels[1].close();
			channels[2].close();
			imp2 = channels[0];
		}
		// IJ.run(imp2, "Median...", "radius=3");
		IJ.run(imp2, "Find Edges", "");
		IJ.run(imp2, "Variance...", "radius=2");
		IJ.setAutoThreshold(imp2, "Default dark");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Fill Holes", "");
		IJ.run(imp2, "Watershed", "");
		return imp2;
	}

	public static ImagePlus processSpheroidNoFluov2(ImagePlus imp2) {
		ImagePlus[] channels = ChannelSplitter.split(imp2);
		if (channels.length >1) {
			channels[1].close();
			channels[2].close();
			imp2 = channels[0];
		}
		IJ.run(imp2, "Median...", "radius=5");
		IJ.run(imp2, "Find Edges", "");
		IJ.run(imp2, "Variance...", "radius=2");
		IJ.setAutoThreshold(imp2, "Otsu dark");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Fill Holes", "");
		return imp2;
	}

	public static ImagePlus processSpheroidFluo(ImagePlus imp2) {
		ImagePlus[] channels = ChannelSplitter.split(imp2);
		if (channels.length >1) {
			channels[1].close();
			channels[2].close();
			imp2 = channels[0];
		}
		IJ.run(imp2, "Maximum...", "radius=5");
		IJ.setAutoThreshold(imp2, "RenyiEntropy dark");// Li, MaxEntropy

		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Erode", "");
		IJ.run(imp2, "Fill Holes", "");
		IJ.run(imp2, "Watershed", "");
		return imp2;
	}

	public static ImagePlus processSpheroidFluov2(ImagePlus imp2) {
		ImagePlus[] channels = ChannelSplitter.split(imp2);
		if (channels.length >1) {
			channels[1].close();
			channels[2].close();
			imp2 = channels[0];
		}
		IJ.run(imp2, "Auto Local Threshold", "method=Phansalkar radius=15 parameter_1=0 parameter_2=0 white");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Erode", "");
		IJ.run(imp2, "Fill Holes", "");
		return imp2;
	}

	public static ImagePlus processSpheroidNoFluoWithRoi(ImagePlus imp, Roi r) {
		ImagePlus imp2 = imp.duplicate();
		ImagePlus[] channels = ChannelSplitter.split(imp2);
		if (channels.length >1) {
			channels[1].close();
			channels[2].close();
			imp2 = channels[0];
		}	

		IJ.run(imp2, "Median...", "radius=3");
		imp2.setRoi(r);
		IJ.setAutoThreshold(imp2, "Default");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Fill Holes", "");
		IJ.run(imp2, "Watershed", "");
		IJ.run(imp2, "Fill Holes", "");
		IJ.setBackgroundColor(255, 255, 255);
		imp2.setRoi(r);
		IJ.run(imp2, "Clear Outside", "");
		IJ.setBackgroundColor(0, 0, 0);
		return imp2;
	}

}
