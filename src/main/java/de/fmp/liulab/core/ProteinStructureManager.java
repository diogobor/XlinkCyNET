package de.fmp.liulab.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.parser.ReaderWriterTextFile;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for creating PyMOL script (*.pml file) and calling software
 * to be executed This class was adapted from CyToStruct, a Cytoscape app that
 * makes a bridge between molecular viewers and Cytoscape (doi:
 * 10.1016/j.str.2015.02.013)
 * 
 * @author diogobor
 *
 */
public class ProteinStructureManager {

	private static ReaderWriterTextFile parserFile;

	public static void execUnix(String[] cmdarray, TaskMonitor taskMonitor) throws IOException {
		// instead of calling command directly, we'll call the shell
		Runtime rt = Runtime.getRuntime();
		File rDir = new File(System.getProperty("user.dir")).getAbsoluteFile();
		String cmd = cmdarray[0] + " " + cmdarray[1]; // should be exactly 2 elements
		String path = writeToTempAndGetPath("cd " + rDir.getAbsolutePath() + " \n " + cmd, "run_", "sh", taskMonitor);
		if (path.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Could not produce script");
		}
		String[] cmdA = { "sh", path };
		taskMonitor.showMessage(TaskMonitor.Level.INFO,
				"Executing: '" + cmdA[0] + "' '" + cmdA[1] + "' @" + rDir.getAbsolutePath());
		rt.exec(cmdA);
	}

	private static void execWindows(String[] cmdarray, TaskMonitor taskMonitor) throws IOException {
		String cmd = cmdarray[0] + " " + cmdarray[1]; // should be exactly 2 elements
		File rDir = new File(System.getProperty("user.dir")).getAbsoluteFile();
		ProcessBuilder pb = new ProcessBuilder("cmd", "/C", cmdarray[0], cmdarray[1]);
		pb.directory(rDir);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Executing: '" + cmd + "' @ " + rDir.getAbsolutePath());
		pb.start();
	}

	// IMPORT FROM CYTOSTRCUT PROJECT

	private static String writeToTempAndGetPath(String text, String prefix, String suffix, TaskMonitor taskMonitor) {
		File out = getTmpFile(prefix, suffix, taskMonitor);
		if (out == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error to create tmp file!");
			return "";
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(out));
			writer.write(text);
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error to create tmp file!");
		}
		return out.getAbsolutePath();
	}

	private static File getTmpFile(String prefix, String suffix, TaskMonitor taskMonitor) {
		File dr = new File(System.getProperty("java.io.tmpdir"), "cytoTmpScripts");
		if (dr.exists() || dr.mkdir()) {
			try {
				return File.createTempFile(prefix + "_scr_", "." + suffix, dr);
			} catch (IOException e) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR,
						"Could not work with tmp dir: " + dr.getAbsolutePath());
			}
		}
		return null;
	}

	/**
	 * Method responsible for creating PyMOL script (*.pml file) -> temp file
	 * 
	 * @param prefix          node name (protein name)
	 * @param proteinSequence protein sequence
	 * @param taskMonitor     task monitor
	 * @return file name
	 */
	public static String createPyMOLScriptFile(String prefix, String proteinSequence, List<CrossLink> crossLinks,
			TaskMonitor taskMonitor, int proteinOffsetInPDB, String protein_chain, String pdbFile) {
		// write this script to tmp file and return path
		File f = getTmpFile(prefix, "pml", taskMonitor);
		if (f == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Could not create temp file with label: " + prefix);
			return "";
		}

		FileWriter bw;
		try {
			bw = new FileWriter(f);
			String finalStr = createPyMOLScript(taskMonitor, proteinSequence, crossLinks, proteinOffsetInPDB,
					protein_chain, pdbFile);
			bw.write(finalStr);
			bw.flush();
			bw.close();
		} catch (IOException ex) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Problems while writing script to " + f.getAbsolutePath());
		}
		return f.getAbsolutePath();
	}

	/**
	 * Get path and fileName of PDB file
	 * 
	 * @param pdbFile full path
	 * @return [0] -> path; [1] -> name
	 */
	private static String[] getPDBFilePathName(String pdbFile) {
		String separator = File.separator;
		String[] pdbPathAndFileName = pdbFile.split(separator);
		StringBuilder pdbFilePath = new StringBuilder();
		for (int i = 0; i < pdbPathAndFileName.length - 1; i++) {
			pdbFilePath.append(pdbPathAndFileName[i] + separator);
		}
		String pdbFileName = pdbPathAndFileName[pdbPathAndFileName.length - 1];

		return new String[] { pdbFilePath.toString(), pdbFileName };
	}

	/**
	 * Create the pml script
	 * 
	 * @param taskMonitor task monitor
	 * @return script
	 */
	private static String createPyMOLScript(TaskMonitor taskMonitor, String proteinSequence, List<CrossLink> crossLinks,
			int proteinOffsetInPDB, String protein_chain, String pdbFile) {

		String[] pdbFilePathName = getPDBFilePathName(pdbFile);
		StringBuilder sbScript = new StringBuilder();
		sbScript.append("cd " + pdbFilePathName[0] + "\n");
		sbScript.append("load " + pdbFilePathName[1] + "\n");
		sbScript.append("set ignore_case, 0\n");

		String proteinSequenceFromPDBFile = getProteinSequenceFromPDBFile(Util.PDB_PATH, protein_chain, taskMonitor);
		if (proteinSequenceFromPDBFile.isBlank() || proteinSequenceFromPDBFile.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No sequence has been found in PDB file.");
			return "";
		}

		if (proteinSequence.isBlank() || proteinSequence.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No sequence has been found in Uniprot.");
			return "";
		}

		int offsetProtein = proteinSequence.indexOf(proteinSequenceFromPDBFile);
		offsetProtein = offsetProtein > -1 ? offsetProtein : 0;
		String selectedResidueItem = "CA";

		StringBuilder sbDistances = new StringBuilder();
		StringBuilder sbPositions = new StringBuilder();

		for (CrossLink crossLink : crossLinks) {
			int pos1 = crossLink.pos_site_a - offsetProtein + proteinOffsetInPDB;
			int pos2 = crossLink.pos_site_b - offsetProtein + proteinOffsetInPDB;

			sbDistances.append("" + pos1 + "/" + selectedResidueItem + ", " + pos2 + "/" + selectedResidueItem + "\n");
			sbPositions.append(pos1 + "\n").append(pos2 + "\n");
		}

		ArrayList<String> distancesList = (ArrayList<String>) Arrays.asList(sbDistances.toString().split("[\\n]"))
				.stream().distinct().collect(Collectors.toList());

		ArrayList<String> positionList = (ArrayList<String>) Arrays.asList(sbPositions.toString().split("[\\n]"))
				.stream().distinct().collect(Collectors.toList());

		int countXL = 1;
		for (String dist : distancesList) {
			sbScript.append("distance xl" + countXL + ", " + dist + "\n");
			countXL++;
		}

		sbScript.append("select a, res " + String.join("+", positionList) + ";\n");
		sbScript.append("set dash_width, 5\n");
		sbScript.append("set dash_length, 0.1\n");
		sbScript.append("set dash_color, [1.000, 1.000, 0.000]\n");
		sbScript.append("set label_color, [1.000, 0.000, 0.000]\n");
		sbScript.append("set label_size, 14\n");
		sbScript.append("cmd.show_as(\"cartoon\", \"" + pdbFilePathName[1].substring(0, pdbFilePathName[1].length() - 4)
				+ "\")\n");
		sbScript.append("cmd.show_as(\"sticks\", \"a\")\n");
		sbScript.append("cmd.disable('a')\n");

		return sbScript.toString();
	}

	/**
	 * Get protein sequence from PDB file
	 * 
	 * @param fileName    file name
	 * @param taskMonitor task monitor
	 * @return sequence
	 */
	private static String getProteinSequenceFromPDBFile(String fileName, String protein_chain,
			TaskMonitor taskMonitor) {

		Map<ByteBuffer, Integer> ResiduesDict = new HashMap<ByteBuffer, Integer>();// e.g <GLU, E>
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 71, 76, 89 }), 71);// Glycine (G)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 65, 76, 65 }), 65);// Alanine (A)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 83, 69, 82 }), 83);// Serine (S)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 80, 82, 79 }), 80);// Proline (P)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 86, 65, 76 }), 86);// Valine (V)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 84, 72, 82 }), 84);// Threonine (T)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 67, 89, 83 }), 67);// Cystein (C)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 73, 76, 69 }), 73);// Isoleucine (I)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 76, 69, 85 }), 76);// Leucine (L)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 65, 83, 78 }), 78);// Asparagine (N)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 65, 83, 80 }), 68);// Aspartic Acid (D)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 71, 76, 78 }), 81);// Glutamine (Q)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 76, 89, 83 }), 75);// Lysine (K)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 71, 76, 88 }), 90);// Glutamic Acid or Glutamine (Z)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 71, 76, 85 }), 69);// Glutamic Acid (E)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 77, 69, 84 }), 77);// Methionine (M)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 72, 73, 83 }), 72);// Histidine (H)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 80, 72, 69 }), 70);// Phenilanyne (F)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 83, 68, 67 }), 85);// Selenocysteine (U)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 65, 82, 71 }), 82);// Arginine (R)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 84, 89, 82 }), 89);// Tyrosine (Y)
		ResiduesDict.put(ByteBuffer.wrap(new byte[] { 84, 82, 80 }), 87);// Tyrosine (Y)

		StringBuilder sbSequence = new StringBuilder();
		try {
			parserFile = new ReaderWriterTextFile(fileName);
			String line = "";
			int lastInsertedResidue = 0;
			int threshold = 10;// qtd aminoacids
			int countAA = 0;

			while (parserFile.hasLine()) {
				line = parserFile.getLine();
				if (!(line.equals(""))) {

					if (!line.startsWith("ATOM"))
						continue;

					String[] cols = line.split("\\s+");

					if (!cols[4].equals(protein_chain))
						continue;

					byte[] pdbResidue = cols[3].getBytes();// Residue -> three characters
					int newResidue = ResiduesDict.get(ByteBuffer.wrap(pdbResidue));

					if (newResidue != lastInsertedResidue) {
						byte[] _byte = new byte[1];
						_byte[0] = (byte) newResidue;
						String string = new String(_byte);
						sbSequence.append(string);
						countAA++;
						if (countAA > threshold) {
							break;
						}
					}
					lastInsertedResidue = newResidue;
				}
			}
		} catch (Exception e) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Problems while reading PDB file: " + fileName);
		}

		return sbSequence.toString();

	}
}
