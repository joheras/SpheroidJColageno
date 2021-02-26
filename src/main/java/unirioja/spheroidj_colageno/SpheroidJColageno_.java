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
import ij.measure.ResultsTable;
import loci.plugins.in.ImporterOptions;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>SpheroidJColageno>Main")
public class SpheroidJColageno_ implements Command {

	@Parameter(label = "Fix the scale")
	private boolean setScale = false;

	private static ArrayList<Integer> goodRows;

	public void run() {
		
		IJ.run("Set Measurements...", "  redirect=None decimal=3");


		if (setScale) {

			ImagePlus imp = IJ.createImage("Untitled", "8-bit white", 1, 1, 1);
			IJ.run(imp, "Set Scale...", "");
			imp.close();
		}

		// We initialize the ResultsTable
		ResultsTable rt = new ResultsTable();
		// We construct the EsferoidProcessorObject

		// We first read the list of files
		List<String> result = SearchFilesMethods.searchFilesFluo();

		String dir = result.get(0);
		result.remove(0);

		// ProgressBar

		IJ.setForegroundColor(255, 0, 0);
		goodRows = new ArrayList<>();
		JFrame frame = new JFrame("Work in progress");
		JProgressBar progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setString("");
		progressBar.setStringPainted(true);
		Border border = BorderFactory.createTitledBorder("Processing...");
		progressBar.setBorder(border);
		Container content = frame.getContentPane();
		content.add(progressBar, BorderLayout.NORTH);
		frame.setSize(300, 100);
		frame.setVisible(true);
		progressBar.setMaximum(result.size());
		int n=0;
		
		// For each file in the folder we detect the esferoid on it.
		for (String name : result) {
			DetectSpheroidMethods.detectSpheroidFluoColagenov2(dir, name, goodRows);
			progressBar.setValue(n++);

		}

		rt = ResultsTable.getResultsTable();

		/// Remove empty rows
		int rows = rt.getCounter();
		for (int i = rows; i > 0; i--) {
			if (!(goodRows.contains(i - 1))) {
				rt.deleteRow(i - 1);
			}
		}

		ExportToExcel ete = new ExportToExcel(rt, dir);
		ete.convertToExcel();

		rt.reset();

		frame.setVisible(false);
		frame.dispose();
		IJ.showMessage("Process finished");

	}

}
