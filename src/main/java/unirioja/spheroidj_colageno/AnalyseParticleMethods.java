package unirioja.spheroidj_colageno;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

public class AnalyseParticleMethods {

	public static RoiManager analyzeParticlesAnnulus(ImagePlus imp2) {
		IJ.run(imp2, "Analyze Particles...", "size=0.001-Infinity  show=Outlines exclude add");
		imp2.changes = false;
		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		if (rm != null) {
			rm.setVisible(false);
		}
		return rm;
	}
	
	public static RoiManager analyzeParticlesCells(ImagePlus imp2) {
		IJ.run(imp2, "Analyze Particles...", "size=0.001-0.005 exclude add");
		imp2.changes = false;
		
		//imp2.close();
		
		RoiManager rm = RoiManager.getInstance();
		if (rm != null) {
			rm.setVisible(true);
		}
		return rm;
	}


}
