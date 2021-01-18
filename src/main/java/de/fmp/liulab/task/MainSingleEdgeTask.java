package de.fmp.liulab.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import de.fmp.liulab.core.ProteinStructureManager;
import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.model.Protein;
import de.fmp.liulab.utils.Tuple2;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for calling PyMOL
 * 
 * @author diogobor
 *
 */
public class MainSingleEdgeTask extends AbstractTask implements ActionListener {

	private CyApplicationManager cyApplicationManager;
	private CyNetwork myNetwork;
	private CyNetworkView netView;
	private CyCustomGraphics2Factory vgFactory;
	private HandleFactory handleFactory;
	private BendFactory bendFactory;
	private boolean IsCommandLine;

	private static MainSingleNodeTask SingleNodeTask;

	private CyRow myCurrentRow;
	private List<CyEdge> edges;
	private CyEdge edge;

	private static List<CrossLink> crosslinks;
	private static String proteinSequenceFromPDBFile_proteinSource;
	private static String proteinSequenceFromPDBFile_proteinTarget;
	private static String pdbFile;
	private static String source_node_name;
	private static String target_node_name;
	private static boolean HasMoreThanOneChain_proteinSource;
	private static boolean HasMoreThanOneChain_proteinTarget;
	private static String proteinChain_source;

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager
	 * @param vmmServiceRef
	 * @param vgFactory
	 * @param bendFactory
	 * @param handleFactory
	 * @param forcedWindowOpen
	 * @param isCommandLine
	 */
	public MainSingleEdgeTask(CyApplicationManager cyApplicationManager, final VisualMappingManager vmmServiceRef,
			CyCustomGraphics2Factory<?> vgFactory, BendFactory bendFactory, HandleFactory handleFactory,
			boolean forcedWindowOpen, boolean isCommandLine) {

		this.cyApplicationManager = cyApplicationManager;
		this.netView = cyApplicationManager.getCurrentNetworkView();
		this.myNetwork = cyApplicationManager.getCurrentNetwork();
		this.vgFactory = vgFactory;
		// Get the current Visual Lexicon
		this.bendFactory = bendFactory;
		this.handleFactory = handleFactory;
		this.IsCommandLine = isCommandLine;

		SingleNodeTask = new MainSingleNodeTask(cyApplicationManager, vmmServiceRef, vgFactory, bendFactory,
				handleFactory, forcedWindowOpen, isCommandLine);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setTitle("XlinkCyNET - Visualize interaction in PyMOL");
		// Write your own function here.
		if (cyApplicationManager.getCurrentNetwork() == null) {
			throw new Exception("ERROR: No network has been loaded.");
		}

		checkSingleOrMultipleSelectedEdges(taskMonitor);

//		if (IsCommandLine) {
//			this.unSelectNodes();
//		}

	}

	/**
	 * Method responsible for checking how many edges have been selected
	 * 
	 * @param taskMonitor task monitor
	 * @throws Exception
	 */
	private void checkSingleOrMultipleSelectedEdges(final TaskMonitor taskMonitor) throws Exception {

		// It is necessary to deselect nodes.
		deselectNodes();

		edges = CyTableUtil.getEdgesInState(myNetwork, CyNetwork.SELECTED, true);

		if (edges.size() == 0) {

			throw new Exception("No edge has been selected. Please select one!");

		} else if (edges.size() > 1) {

			throw new Exception("More than one edge has been selected. Please select only one.");

		} else {
			edge = edges.get(0);
			executeSingleEdge(taskMonitor);
		}

	}

	/**
	 * Method responsible for deselecting all nodes.
	 */
	private void deselectNodes() {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(myNetwork, CyNetwork.SELECTED, true);
		for (CyNode cyNode : selectedNodes) {
			myNetwork.getRow(cyNode).set(CyNetwork.SELECTED, false);
		}
	}

	/**
	 * Method responsible for executing pyMOL to a single edge
	 * 
	 * @param taskMonitor
	 * @throws Exception
	 */
	private void executeSingleEdge(final TaskMonitor taskMonitor) throws Exception {

		String edge_name = myNetwork.getDefaultEdgeTable().getRow(edge.getSUID()).get(CyNetwork.NAME, String.class);
		CyNode sourceNode = myNetwork.getEdge(edge.getSUID()).getSource();
		CyNode targetNode = myNetwork.getEdge(edge.getSUID()).getTarget();
		boolean isIntralink = sourceNode.getSUID() == targetNode.getSUID() || edge_name.startsWith("Edge") ? true
				: false;

		crosslinks = new ArrayList<CrossLink>();

		if (!Util.isEdgeModified(myNetwork, netView, edge)) { // Display all cross-links between two proteins

			if (!edge_name.contains("[Source:")) {// Check if edge is not modified by name

				if (!isIntralink) { // Interlinks

					Tuple2 inter_and_intralinks_source = Util.getAllLinksFromAdjacentEdgesNode(sourceNode, myNetwork);
					crosslinks = (ArrayList<CrossLink>) inter_and_intralinks_source.getFirst();

					Tuple2 inter_and_intralinks_target = Util.getAllLinksFromAdjacentEdgesNode(targetNode, myNetwork);
					crosslinks.addAll((ArrayList<CrossLink>) inter_and_intralinks_target.getFirst());

					crosslinks = crosslinks.stream().distinct().collect(Collectors.toList());

					final String source_node_name = myNetwork.getDefaultNodeTable().getRow(sourceNode.getSUID())
							.getRaw(CyNetwork.NAME).toString();
					final String target_node_name = myNetwork.getDefaultNodeTable().getRow(targetNode.getSUID())
							.getRaw(CyNetwork.NAME).toString();

					crosslinks.removeIf(new Predicate<CrossLink>() {

						public boolean test(CrossLink o) {
							return !(o.protein_a.equals(source_node_name) && o.protein_b.equals(target_node_name)
									|| o.protein_a.equals(target_node_name) && o.protein_b.equals(source_node_name));
						}
					});

				} else {// Intralink
					Tuple2 inter_and_intralinks_source = Util.getAllLinksFromAdjacentEdgesNode(sourceNode, myNetwork);
					crosslinks = (ArrayList<CrossLink>) inter_and_intralinks_source.getSecond();
					MainSingleNodeTask.intraLinks = (ArrayList<CrossLink>) crosslinks;
					myCurrentRow = myNetwork.getRow(sourceNode);

					processIntraLinks(taskMonitor, myCurrentRow);

					return;

				}
			}

		} else { // Display only the selected edge

			if (edge_name.contains("[Source:")) {// Check if edge is modified by name

				String[] edgeNameArr = edge_name.split("\\[|\\]");
				String[] pos_a = edgeNameArr[1].split("\\(|\\)");
				String[] pos_b = edgeNameArr[3].split("\\(|\\)");

				String ptn_a = pos_a[0].replaceAll("Source: ", "").trim();
				String ptn_b = pos_b[0].replaceAll("Target: ", "").trim();

				CrossLink cl = new CrossLink(ptn_a, ptn_b, Integer.parseInt(pos_a[1]), Integer.parseInt(pos_b[1]));
				crosslinks.add(cl);
			}

			if (isIntralink) {

				sourceNode = Util.getNode(myNetwork, crosslinks.get(0).protein_a);
				MainSingleNodeTask.intraLinks = (ArrayList<CrossLink>) crosslinks;
				myCurrentRow = myNetwork.getRow(sourceNode);

				processIntraLinks(taskMonitor, myCurrentRow);

				return;
			}
		}

		taskMonitor.showMessage(Level.INFO, "Selecting source node: " + sourceNode.getSUID());
		myCurrentRow = myNetwork.getRow(sourceNode);

		taskMonitor.showMessage(Level.INFO, "Getting PDB information...");
		Protein ptnSource = Util.getPDBidFromUniprot(myCurrentRow, taskMonitor);
		source_node_name = (String) myCurrentRow.getRaw(CyNetwork.NAME);

		String msgINFO = "";
		List<String> pdbIdsSource = new ArrayList<String>();
//		List<String> pdbIdsSource = ptnSource.pdbIds;

		if (pdbIdsSource.size() > 0) {

			taskMonitor.showMessage(Level.INFO, "Selecting target node: " + targetNode.getSUID());
			myCurrentRow = myNetwork.getRow(targetNode);

			taskMonitor.showMessage(Level.INFO, "Getting PDB information...");
			Protein ptnTarget = Util.getPDBidFromUniprot(myCurrentRow, taskMonitor);
			target_node_name = (String) myCurrentRow.getRaw(CyNetwork.NAME);

//			List<String> pdbIdsTarget = ptnTarget.pdbIds;
			List<String> pdbIdsTarget = new ArrayList<String>();
			if (pdbIdsTarget.size() > 0) {

				Set<String> result = pdbIdsSource.stream().distinct().filter(pdbIdsTarget::contains)
						.collect(Collectors.toSet());

				if (result.size() == 0) {
					taskMonitor.showMessage(Level.ERROR,
							"There is no common PDB for nodes: " + source_node_name + " and " + target_node_name + ".");

					throw new Exception(
							"There is no common PDB for nodes: " + source_node_name + " and " + target_node_name + ".");
				}

				if (result.size() > 1) {

					List<String> pdbIds = new ArrayList<String>(result);
					// Open a window to select only one PDB
//					SingleNodeTask.getPDBInformation(pdbIds, msgINFO, taskMonitor, ptnSource, ptnTarget, true, "",
//							false, true, source_node_name + "#" + target_node_name, false);

				} else {

					MainSingleEdgeTask.processPDBFile(taskMonitor, result.iterator().next(), ptnSource, ptnTarget,
							source_node_name + "#" + target_node_name, false);

				}

			} else {
				taskMonitor.showMessage(Level.ERROR, "There is no PDB for the target node: " + target_node_name);

				throw new Exception("There is no PDB for the target node: " + target_node_name + ".");
			}

		} else {
			taskMonitor.showMessage(Level.ERROR, "There is no PDB for the source node: " + source_node_name);

			throw new Exception("There is no PDB for the source node: " + source_node_name + ".");
		}

	}

	private static void processIntraLinks(TaskMonitor taskMonitor, CyRow myCurrentRow) throws Exception {

		String msgINFO = "";
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting PDB information from Uniprot...");

		Protein ptn = Util.getPDBidFromUniprot(myCurrentRow, taskMonitor);
//		List<String> pdbIds = ptn.pdbIds;
		List<String> pdbIds = new ArrayList<String>();
		if (pdbIds.size() > 0) {

			SingleNodeTask.myCurrentRow = myCurrentRow;
			String pdbID = pdbIds.get(0);

			if (pdbIds.size() > 1) {

				// Open a window to select only one PDB
//				SingleNodeTask.getPDBInformation(pdbIds, msgINFO, taskMonitor, ptn, null, true, "", false, false,
//						(String) myCurrentRow.getRaw(CyNetwork.NAME), false);

				return;
			}

			SingleNodeTask.processPDBFile(msgINFO, taskMonitor, pdbID, ptn);

		} else {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "There is no PDB for the protein: " + ptn.proteinID);
			throw new Exception("There is no PDB for the protein: " + ptn.proteinID);
		}
	}

	/**
	 * Method responsible for creating and processing PDB file
	 * 
	 * @param msgINFO     output info
	 * @param taskMonitor taskmonitor
	 * @param pdbID       pdb ID
	 * @param ptnSource   protein source
	 * @param ptnTarget   protein target
	 * @param nodeName    node name
	 */
	public static void processPDBFile(TaskMonitor taskMonitor, String pdbID, Protein ptnSource, Protein ptnTarget,
			String nodeName, boolean processTarget) {

		if (processTarget) { // process

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Chain of protein source: " + pdbID);
			// Set the chain of protein source
			proteinChain_source = pdbID;

			// [pdb protein sequence, protein chain, "true" -> there is more than one chain]
			String[] returnPDB_proteinTarget = null;
			if (pdbFile.endsWith("pdb")) {
				taskMonitor.showMessage(TaskMonitor.Level.INFO,
						"Getting protein sequence and chain of protein source from PDB file...");
				returnPDB_proteinTarget = ProteinStructureManager.getProteinSequenceAndChainFromPDBFile(pdbFile,
						ptnTarget, taskMonitor);
			} else {

				taskMonitor.showMessage(TaskMonitor.Level.INFO,
						"Getting all chains of protein source from CIF file...");
				returnPDB_proteinTarget = ProteinStructureManager.getChainFromCIFFile(pdbFile, ptnTarget, taskMonitor);
			}

			proteinSequenceFromPDBFile_proteinTarget = returnPDB_proteinTarget[0];
			HasMoreThanOneChain_proteinTarget = returnPDB_proteinTarget[2].equals("true") ? true : false;

			String proteinChain_proteinTarget = returnPDB_proteinTarget[1];
			if (proteinChain_proteinTarget.startsWith("CHAINS:")) {// There is more than one chain
				taskMonitor.showMessage(TaskMonitor.Level.WARN,
						"No chain does not match with protein target description.");

				String[] protein_chains = returnPDB_proteinTarget[1].replace("CHAINS:", "").split("#");
				List<String> protein_chainsList = Arrays.asList(protein_chains);
				if (protein_chainsList.size() > 1) {
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "There is more than one chain. Select one...");

					// Open a window to select only one chain
//					SingleNodeTask.getPDBInformation(protein_chainsList, "", taskMonitor, ptnSource, ptnTarget, false,
//							pdbFile, HasMoreThanOneChain_proteinTarget, true, target_node_name, true);

				}

			} else {

				// At this point we have the selected chain of protein source and one chain of
				// protein target. So, we can create pymol script

				processPDBFileWithSpecificChain(taskMonitor, ptnSource, ptnTarget, returnPDB_proteinTarget[1]);

			}

		} else { // Process protein source

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Creating tmp PDB file...");

			pdbFile = ProteinStructureManager.createPDBFile(pdbID, taskMonitor);
			if (pdbFile.equals("ERROR")) {

				taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error creating PDB file.");

				JOptionPane.showMessageDialog(null, "Error creating PDB file. Check Task History for more details.",
						"XlinkCyNET - Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// [pdb protein sequence, protein chain, "true" -> there is more than one chain]
			String[] returnPDB_proteinSource = null;
			if (pdbFile.endsWith("pdb")) {

				taskMonitor.showMessage(TaskMonitor.Level.INFO,
						"Getting protein sequence and chain of protein source from PDB file...");
				returnPDB_proteinSource = ProteinStructureManager.getProteinSequenceAndChainFromPDBFile(pdbFile,
						ptnSource, taskMonitor);
			} else {

				taskMonitor.showMessage(TaskMonitor.Level.INFO,
						"Getting all chains of protein source from CIF file...");
				returnPDB_proteinSource = ProteinStructureManager.getChainFromCIFFile(pdbFile, ptnSource, taskMonitor);
			}

			proteinSequenceFromPDBFile_proteinSource = returnPDB_proteinSource[0];
			HasMoreThanOneChain_proteinSource = returnPDB_proteinSource[2].equals("true") ? true : false;
			String proteinChain_proteinSource = returnPDB_proteinSource[1];

			if (proteinChain_proteinSource.startsWith("CHAINS:")) { // There is more than one chain
				taskMonitor.showMessage(TaskMonitor.Level.WARN,
						"No chain does not match with protein source description.");

				String[] protein_chains = returnPDB_proteinSource[1].replace("CHAINS:", "").split("#");
				List<String> protein_chainsList = Arrays.asList(protein_chains);
				if (protein_chainsList.size() > 1) {
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "There is more than one chain. Select one...");

					// Open a window to select only one chain
//					SingleNodeTask.getPDBInformation(protein_chainsList, "", taskMonitor, ptnSource, ptnTarget, true,
//							pdbFile, HasMoreThanOneChain_proteinSource, true, source_node_name, true);

				}

			} else { // There is only one chain (source)

				// Call this method to obtain the target chain
				processPDBFile(taskMonitor, proteinChain_proteinSource, ptnSource, ptnTarget, nodeName, true);
			}
		}
	}

	/**
	 * Method responsible for creating pymol script
	 * 
	 * @param taskMonitor         task monitor
	 * @param ptnSource           protein source
	 * @param ptnTarget           protein target
	 * @param proteinChain_target chain of protein target
	 */
	public static void processPDBFileWithSpecificChain(TaskMonitor taskMonitor, Protein ptnSource, Protein ptnTarget,
			String proteinChain_target) {

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Chain of protein target: " + proteinChain_target);
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting sequence of protein source: " + source_node_name);

		String proteinSequence_source_FromPDBFile = "";

		if (pdbFile.endsWith("pdb"))
			proteinSequence_source_FromPDBFile = ProteinStructureManager.getProteinSequenceFromPDBFileWithSpecificChain(
					pdbFile, ptnSource, taskMonitor, proteinChain_source, false);
		else
			proteinSequence_source_FromPDBFile = ProteinStructureManager.getProteinSequenceFromCIFFileWithSpecificChain(
					pdbFile, ptnSource, taskMonitor, proteinChain_source, false);

		if (proteinSequence_source_FromPDBFile.isBlank() || proteinSequence_source_FromPDBFile.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,
					"No sequence has been found in pdb/cif file for: " + source_node_name);

			JOptionPane.showMessageDialog(null, "No sequence has been found in pdb/cif file for: " + source_node_name,
					"XlinkCyNET - Alert", JOptionPane.WARNING_MESSAGE);
			return;
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting sequence of protein target: " + target_node_name);

		String proteinSequence_target_FromPDBFile = "";

		if (pdbFile.endsWith("pdb"))
			proteinSequence_target_FromPDBFile = ProteinStructureManager.getProteinSequenceFromPDBFileWithSpecificChain(
					pdbFile, ptnTarget, taskMonitor, proteinChain_target, true);
		else
			proteinSequence_target_FromPDBFile = ProteinStructureManager.getProteinSequenceFromCIFFileWithSpecificChain(
					pdbFile, ptnTarget, taskMonitor, proteinChain_target, true);

		if (proteinSequence_target_FromPDBFile.isBlank() || proteinSequence_target_FromPDBFile.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,
					"No sequence has been found in pdb/cif file for: " + target_node_name);

			JOptionPane.showMessageDialog(null, "No sequence has been found in pdb/cif file for: " + target_node_name,
					"XlinkCyNET - Alert", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Filter cross-links to obtain only links that belong to source and target
		// nodes
		crosslinks.removeIf(new Predicate<CrossLink>() {

			public boolean test(CrossLink o) {
				return !(o.protein_a.equals(source_node_name) && o.protein_b.equals(target_node_name)
						|| o.protein_a.equals(target_node_name) && o.protein_b.equals(source_node_name));
			}
		});

		String tmpPyMOLScriptFile = ProteinStructureManager.createPyMOLScriptFile(ptnSource, ptnTarget, crosslinks,
				taskMonitor, pdbFile, proteinSequence_source_FromPDBFile, proteinSequence_target_FromPDBFile,
				HasMoreThanOneChain_proteinSource, HasMoreThanOneChain_proteinTarget, proteinChain_source,
				proteinChain_target, source_node_name, target_node_name);

		if (tmpPyMOLScriptFile.equals("ERROR")) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error creating PyMOL script file.");

			JOptionPane.showMessageDialog(null, "Error creating PyMOL script file.", "XlinkCyNET - Alert",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		ProteinStructureManager.executePyMOL(taskMonitor, tmpPyMOLScriptFile, null);

	}

}
