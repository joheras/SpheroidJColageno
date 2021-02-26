package unirioja.spheroidj_colageno;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import loci.plugins.in.ImporterOptions;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>SpheroidJColageno>Single")
public class SpheroidJColagenoSingle_ implements Command {

	@Parameter(callback = "prominenceChanged", style = "slider", min = "0", max = "100")
	private int prominence = 15;

	private void prominenceChanged() {
		ImagePlus imp = IJ.getImage();
		imp.deleteRoi();
		ImagePlus imp4 = imp.duplicate();

		ImagePlus[] channels = ChannelSplitter.split(imp4);
		channels[0].close();
		channels[2].close();
		imp4 = channels[1];
		IJ.run(imp4, "Median...", "radius=3");
		IJ.run(imp4, "Sharpen", "");
		IJ.run(imp4, "Find Maxima...", "prominence=" +  prominence + " strict light output=[Point Selection]");
		PointRoi pr = (PointRoi) imp4.getRoi();
		imp.setRoi(pr);
		imp4.changes=false;
		imp4.close();
	}

	public void run() {

		/*ImagePlus imp4 = IJ.getImage();

		ImagePlus[] channels = ChannelSplitter.split(imp4);
		channels[0].close();
		channels[2].close();
		imp4 = channels[1];
		IJ.run(imp4, "Median...", "radius=3");
		IJ.run(imp4, "Sharpen", "");
		IJ.run(imp4, "Enlarge...", "enlarge=0.15");
		IJ.run(imp4, "To Bounding Box", "");
		IJ.run(imp4, "Find Maxima...", "prominence=15 strict light output=[Point Selection]");
		PointRoi pr = (PointRoi) imp4.getRoi();
	*/
	}

}
