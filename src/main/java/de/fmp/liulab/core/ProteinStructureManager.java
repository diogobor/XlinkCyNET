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
import de.fmp.liulab.model.Protein;
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
	private static int proteinOffsetInPDB = -1;

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
	 * Create PDB file (temporary file)
	 * 
	 * @param pdbID       pdb ID
	 * @param taskMonitor task monitor
	 * @return file name
	 */
	public static String createPDBFile(String pdbID, TaskMonitor taskMonitor) {
		// write this script to tmp file and return path
		File f = getTmpFile(pdbID, "pdb", taskMonitor);
		if (f == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Could not create temp file with label: " + pdbID);
			return "";
		}

		FileWriter bw;
		String finalStr = "";
		try {
			bw = new FileWriter(f);
			finalStr = Util.getPDBfileFromServer(pdbID);
			if (finalStr.isBlank() || finalStr.isEmpty())
				taskMonitor.showMessage(TaskMonitor.Level.ERROR,
						"Error retrieving the PDB file for protein " + pdbID + ".");
			bw.write(finalStr);
			bw.flush();
			bw.close();
		} catch (IOException ex) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Problems while writing script to " + f.getAbsolutePath());
			finalStr = "";
		}

		if (finalStr.isBlank() || finalStr.isEmpty())
			return "ERROR";
		return f.getAbsolutePath();
	}

	/**
	 * Method responsible for creating PyMOL script (*.pml file) -> temp file
	 * 
	 * @param ptn                        protein
	 * @param crossLinks                 crosslinks
	 * @param taskMonitor                task monitor
	 * @param pdbFile                    pdb file name
	 * @param proteinSequenceFromPDBFile protein sequence in PDB file
	 * @param HasMoreThanOneChain        it has more than one chain in PDB file
	 * @param proteinChain               protein chain
	 * @return pymol script file name
	 */
	public static String createPyMOLScriptFile(Protein ptn, List<CrossLink> crossLinks, TaskMonitor taskMonitor,
			String pdbFile, String proteinSequenceFromPDBFile, boolean HasMoreThanOneChain, String proteinChain) {

		// write this script to tmp file and return path
		File f = getTmpFile(ptn.proteinID, "pml", taskMonitor);
		if (f == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Could not create temp file with label: " + ptn.proteinID);
			return "ERROR";
		}

		FileWriter bw;
		String finalStr = "";
		try {

			if (proteinSequenceFromPDBFile.isBlank() || proteinSequenceFromPDBFile.isEmpty()) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No sequence has been found in PDB file.");
				return "ERROR";
			}

			finalStr = createPyMOLScript(taskMonitor, ptn, crossLinks, pdbFile, HasMoreThanOneChain, proteinChain,
					proteinSequenceFromPDBFile);

			bw = new FileWriter(f);
			bw.write(finalStr);
			bw.flush();
			bw.close();
		} catch (IOException ex) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Problems while writing script to " + f.getAbsolutePath());
			finalStr = "";
		}

		if (finalStr.isBlank() || finalStr.isEmpty())
			return "ERROR";

		return f.getAbsolutePath();
	}

	/**
	 * Method responsible for creating PyMOL script (*.pml file) -> temp file when
	 * the chain is unknown
	 * 
	 * @param ptn         protein
	 * @param crossLinks  crosslinks
	 * @param taskMonitor task monitor
	 * @param pdbFile     pdb file name
	 * @return return pymol file name or pdb chains
	 */
	public static String[] createPyMOLScriptFileUnknowChain(Protein ptn, List<CrossLink> crossLinks,
			TaskMonitor taskMonitor, String pdbFile) {

		// write this script to tmp file and return path
		File f = getTmpFile(ptn.proteinID, "pml", taskMonitor);
		if (f == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Could not create temp file with label: " + ptn.proteinID);
			return new String[] { "ERROR" };
		}

		FileWriter bw;
		String finalStr = "";
		try {

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein sequence from PDB file...");
			
			// [pdb protein sequence, protein chain, "true" -> there is more than one chain]
			String[] returnPDB = getProteinSequenceAndChainFromPDBFile(pdbFile, ptn, taskMonitor);

			String proteinSequenceFromPDBFile = returnPDB[0];
			boolean HasMoreThanOneChain = returnPDB[2].equals("true") ? true : false;

			String proteinChain = returnPDB[1];
			if (proteinChain.startsWith("CHAINS:")) {
				taskMonitor.showMessage(TaskMonitor.Level.WARN, "No chain does not match with protein description. Select one chain...");
				f.delete();
				// return String[0-> 'CHAINS'; 1-> HasMoreThanOneChain; 2-> chains: separated by
				// '#']
				return new String[] { "CHAINS", returnPDB[2], proteinChain };
			}

			if (proteinSequenceFromPDBFile.isBlank() || proteinSequenceFromPDBFile.isEmpty()) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No sequence has been found in PDB file.");
				return new String[] { "ERROR" };
			}

			if (ptn.sequence.isBlank() || ptn.sequence.isEmpty()) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No sequence has been found in Uniprot.");
				return new String[] { "ERROR" };
			}

			finalStr = createPyMOLScript(taskMonitor, ptn, crossLinks, pdbFile, HasMoreThanOneChain, proteinChain,
					proteinSequenceFromPDBFile);

			bw = new FileWriter(f);
			bw.write(finalStr);
			bw.flush();
			bw.close();
		} catch (IOException ex) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Problems while writing script to " + f.getAbsolutePath());
			finalStr = "";
		}

		if (finalStr.isBlank() || finalStr.isEmpty())
			return new String[] { "ERROR" };

		return new String[] { f.getAbsolutePath() };
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
	private static String createPyMOLScript(TaskMonitor taskMonitor, Protein ptn, List<CrossLink> crossLinks,
			String pdbFile, boolean HasMoreThanOneChain, String proteinChain, String proteinSequenceFromPDBFile) {

		// [0]-> Path
		// [1]-> File name
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting PDB file name...");
		
		String[] pdbFilePathName = getPDBFilePathName(pdbFile);
		StringBuilder sbScript = new StringBuilder();
		sbScript.append("cd " + pdbFilePathName[0] + "\n");
		sbScript.append("load " + pdbFilePathName[1] + "\n");
		sbScript.append("set ignore_case, 0\n");
		sbScript.append("select chain_" + proteinChain + ", chain " + proteinChain + "\n");
		sbScript.append("color green, chain " + proteinChain + "\n");
		sbScript.append("hide all\n");

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Computing protein offset...");
		
		int offsetProtein = ptn.sequence.indexOf(proteinSequenceFromPDBFile);
		offsetProtein = offsetProtein > -1 ? offsetProtein : 0;
		String selectedResidueItem = "CA";

		if ((proteinOffsetInPDB - 1) == offsetProtein) {
			offsetProtein = 0;
		} else if (offsetProtein > 0) {
			offsetProtein = (proteinOffsetInPDB - 1) - offsetProtein;
		}

		StringBuilder sbDistances = new StringBuilder();
		StringBuilder sbPositions = new StringBuilder();

		for (CrossLink crossLink : crossLinks) {
			int pos1 = crossLink.pos_site_a + offsetProtein;
			int pos2 = crossLink.pos_site_b + offsetProtein;

			sbDistances.append("" + proteinChain + "/" + pos1 + "/" + selectedResidueItem + ", " + proteinChain + "/"
					+ pos2 + "/" + selectedResidueItem + "\n");
			sbPositions.append(proteinChain + "/" + pos1 + "\n").append(proteinChain + "/" + pos2 + "\n");
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
		sbScript.append("deselect\n");
		sbScript.append("show sticks, a\n");
		sbScript.append("show cartoon, chain_" + proteinChain + "\n");
		sbScript.append("zoom chain_" + proteinChain + "\n");
		sbScript.append("deselect\n");

//		if (HasMoreThanOneChain) {// There is more than one chain
//
//			sbScript.append("select chain_" + proteinChain + ", chain " + proteinChain + "\n");
//			sbScript.append("hide all\n");
//			sbScript.append("color gray30\n");
//			sbScript.append("color red, chain " + proteinChain + "\n");
//			sbScript.append("zoom chain_" + proteinChain + "\n");
//			sbScript.append("deselect\n");
//
//		}
//
//		sbScript.append("cmd.show_as(\"cartoon\", \"" + pdbFilePathName[1].substring(0, pdbFilePathName[1].length() - 4)
//				+ "\")\n");
//		sbScript.append("cmd.show_as(\"sticks\", \"a\")\n");
//		sbScript.append("cmd.disable('a')\n");

		return sbScript.toString();
	}

	/**
	 * Get protein sequence from PDB file
	 * 
	 * @param fileName      file name
	 * @param ptn           protein
	 * @param taskMonitor   task monitor
	 * @param protein_chain protein chain
	 * @return protein sequence
	 */
	public static String getProteinSequenceFromPDBFileWithSpecificChain(String fileName, Protein ptn,
			TaskMonitor taskMonitor, String protein_chain) {
		Map<ByteBuffer, Integer> ResiduesDict = Util.createResiduesDict();

		StringBuilder sbSequence = new StringBuilder();

		try {
			parserFile = new ReaderWriterTextFile(fileName);
			String line = "";
			int lastInsertedResidue = 0;
			int threshold = 10;// qtd aminoacids
			int countAA = 0;
			proteinOffsetInPDB = -1;

			while (parserFile.hasLine()) {
				line = parserFile.getLine();
				if (!(line.equals(""))) {

					if (!line.startsWith("ATOM"))
						continue;

					// It starts with 'ATOM'

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

					if (proteinOffsetInPDB == -1) {
						proteinOffsetInPDB = Integer.parseInt(cols[5]);
					}
				}

			}
		} catch (Exception e) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Problems while reading PDB file: " + fileName);
		}

		return sbSequence.toString();
	}

	/**
	 * Get protein sequence and chain from PDB file
	 * 
	 * @param fileName    file name
	 * @param taskMonitor task monitor
	 * @return [sequence, protein chain, hasMoreThanOneChain]
	 */
	private static String[] getProteinSequenceAndChainFromPDBFile(String fileName, Protein ptn,
			TaskMonitor taskMonitor) {

		Map<ByteBuffer, Integer> ResiduesDict = Util.createResiduesDict();

		StringBuilder sbSequence = new StringBuilder();
		String protein_chain = "";
		StringBuilder sbProteinChains = new StringBuilder();
		sbProteinChains.append("CHAINS:");

		boolean hasMoreThanOneChain = false;
		int countChains = 0;

		try {
			parserFile = new ReaderWriterTextFile(fileName);
			String line = "";
			int lastInsertedResidue = 0;
			int threshold = 10;// qtd aminoacids
			int countAA = 0;
			proteinOffsetInPDB = -1;

			boolean isCompleteFullName = false;
			StringBuilder sbProteinFullName = new StringBuilder();

			while (parserFile.hasLine()) {
				line = parserFile.getLine();
				if (!(line.equals(""))) {

					if (!line.startsWith("COMPND") && !line.startsWith("ATOM"))
						continue;

					if (line.startsWith("COMPND")) {
						String[] cols = line.split("\\s+");

						boolean isNumeric = false;
						try {
							Double.parseDouble(cols[1]);
							isNumeric = true;
						} catch (NumberFormatException nfe) {
						}

						if (isNumeric) {

							// Get protein full name
							if (cols[2].equals("MOLECULE:")) {

								for (int i = 3; i < cols.length; i++) {
									sbProteinFullName.append(cols[i] + " ");
								}

								if (cols[cols.length - 1].endsWith(";")) {
									isCompleteFullName = true;
									continue;
								}

							} else if (!isCompleteFullName) {
								for (int i = 2; i < cols.length; i++) {
									sbProteinFullName.append(cols[i] + " ");
								}

								if (cols[cols.length - 1].endsWith(";")) {
									isCompleteFullName = true;
									continue;
								}
							}

							if (isCompleteFullName) {
								if (cols[2].equals("CHAIN:")) {
									countChains++;

									String pdbProteinFullName_moleculeField = sbProteinFullName.toString()
											.replace(';', ' ').trim();

									String[] fullNames = pdbProteinFullName_moleculeField.split(",");
									if (fullNames.length == 1) {
										if (ptn.fullName.toLowerCase()
												.equals(pdbProteinFullName_moleculeField.toLowerCase())) {
											protein_chain = cols[3].toString().replace(';', ' ').trim();
											continue;
										}

									} else { // There is more than one chain for this ptn full name

										hasMoreThanOneChain = true;
										for (String fullName : fullNames) {
											if (ptn.fullName.toLowerCase().equals(fullName.toLowerCase().trim())) {
												protein_chain = cols[3].toString().replace(';', ' ').trim()
														.split(",")[0].trim();
												break;
											}
										}
									}
									sbProteinChains.append(cols[3].toString().replace(';', ' ').trim() + "#");

								}
							}

						} else {
							continue;
						}

					} else {// It starts with 'ATOM'

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

						if (proteinOffsetInPDB == -1) {
							proteinOffsetInPDB = Integer.parseInt(cols[5]);
						}
					}

				}
			}
		} catch (Exception e) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Problems while reading PDB file: " + fileName);
		}

		if (protein_chain.isBlank() || protein_chain.isEmpty())
			protein_chain = sbProteinChains.toString();

		if (countChains > 1 || hasMoreThanOneChain) {
			return new String[] { sbSequence.toString(), protein_chain, "true" };
		} else
			return new String[] { sbSequence.toString(), protein_chain, "false" };

	}
}
