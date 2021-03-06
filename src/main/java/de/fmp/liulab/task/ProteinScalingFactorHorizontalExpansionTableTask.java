package de.fmp.liulab.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.model.PTM;
import de.fmp.liulab.model.Protein;
import de.fmp.liulab.model.ProteinDomain;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for expanding the protein bar
 * 
 * @author diogobor
 *
 */
public class ProteinScalingFactorHorizontalExpansionTableTask extends AbstractTask {

	private CyNetwork myNetwork;
	private boolean forcedHorizontalExpansion;

	public static boolean isProcessing;

	/**
	 * Constructor
	 * 
	 * @param myNetwork                 current network
	 * @param forcedHorizontalExpansion Force horizontal expansion param as true
	 */
	public ProteinScalingFactorHorizontalExpansionTableTask(CyNetwork myNetwork, boolean forcedHorizontalExpansion) {
		this.myNetwork = myNetwork;
		this.forcedHorizontalExpansion = forcedHorizontalExpansion;
	}

	/**
	 * Check if 'crosslinks_ab' or 'crosslinks_ba' columns has been set as 'Edge Attribute' at import time
	 * @throws IOException
	 */
	private void checkCrosslinksColumns() throws IOException {
		
		CyEdge one_edge = Util.getEdge(myNetwork, "interacts with", true);
		if (one_edge != null) {

			CyRow edge_row = myNetwork.getRow(one_edge);
			Object crosslinks_ab = edge_row.getRaw(Util.XL_PROTEIN_A_B);
			Object crosslinks_ba = edge_row.getRaw(Util.XL_PROTEIN_B_A);

			if (crosslinks_ab == null && crosslinks_ba == null) {

				throw new IOException(
						"There is no information in column 'crosslinks_ab' or 'crosslinks_ba' or they have been set as 'Source/Target Node Attribute'.\nPlease set these columns as 'Edge Attribute' at import time.");

			}
		}
	}

	/**
	 * Default method
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {

		if (myNetwork == null)
			return;

		// Check if crosslink_ab and cross_link_ba are set on Edge Table
		checkCrosslinksColumns();

		if (isProcessing)
			return;

		isProcessing = true;// It indicates that there is a process here

		taskMonitor.setTitle("XlinkCyNET - Adding extra columns to the tables");

		// Create Scaling factor protein column
		CyTable nodeTable = myNetwork.getDefaultNodeTable();
		if (nodeTable.getColumn(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME) == null) {
			try {
				nodeTable.createColumn(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, Double.class, false);

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					row.set(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, 1.0d);
				}

			} catch (IllegalArgumentException e) {
				try {
					for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
						if (row.get(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, Double.class) == null)
							row.set(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, 1.0d);
					}
				} catch (Exception e2) {
					return;
				}
			} catch (Exception e) {
			}

		} else { // The column exists, but it's necessary to check the cells
			try {
				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					if (row.get(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, Double.class) == null)
						row.set(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, 1.0d);
				}
			} catch (Exception e) {
			}

		}

		// ###### HORIZONTAL OR VERTICAL EXPANSION #######
		if (nodeTable.getColumn(Util.HORIZONTAL_EXPANSION_COLUMN_NAME) == null) {
			try {
				nodeTable.createColumn(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, Boolean.class, false);

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					row.set(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, true);
				}

			} catch (IllegalArgumentException e) {
				try {
					for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
						if (forcedHorizontalExpansion
								|| row.get(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, Boolean.class) == null)
							row.set(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, true);
					}
				} catch (Exception e2) {
					return;
				}
			} catch (Exception e) {
			}

		} else { // The column exists, but it's necessary to check the cells
			try {
				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					if (forcedHorizontalExpansion
							|| row.get(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, Boolean.class) == null)
						row.set(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, true);
				}
			} catch (Exception e) {
			}

		}

		// ######## PROTEIN DOMAINS ########
		if (nodeTable.getColumn(Util.PROTEIN_DOMAIN_COLUMN) == null) {
			try {
				nodeTable.createColumn(Util.PROTEIN_DOMAIN_COLUMN, String.class, false);

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					row.set(Util.PROTEIN_DOMAIN_COLUMN, "");
				}

			} catch (IllegalArgumentException e) {
				try {
					for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
						if (row.get(Util.PROTEIN_DOMAIN_COLUMN, String.class) == null)
							row.set(Util.PROTEIN_DOMAIN_COLUMN, "");
					}
				} catch (Exception e2) {
				}
			} catch (Exception e) {
			}

		} else { // The column exists, but it's necessary to check the cells
			try {

				// Check if proteinDomainsMap has been initialized
				boolean proteinDomainsMapOK = true;
				if (Util.proteinDomainsMap == null)
					proteinDomainsMapOK = false;

				// Initialize protein domain colors map if LoadProteinDomainTask has not been
				// initialized
				Util.init_availableProteinDomainColorsMap();

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					if (row.get(Util.PROTEIN_DOMAIN_COLUMN, String.class) == null)
						row.set(Util.PROTEIN_DOMAIN_COLUMN, "");
					else {
						String nodeName = row.get(CyNetwork.NAME, String.class);
						String domains = row.get(Util.PROTEIN_DOMAIN_COLUMN, String.class);

						if (!(domains.isBlank() || domains.isEmpty()) && proteinDomainsMapOK) {

							updateProteinDomainsMap(nodeName, domains, taskMonitor);
						}
					}
				}
			} catch (Exception e) {
			}
		}

		// ####### POST-TRANSLATIONAL MODIFICATIONS #########
		if (nodeTable.getColumn(Util.PTM_COLUMN) == null) {
			try {
				nodeTable.createColumn(Util.PTM_COLUMN, String.class, false);

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					row.set(Util.PTM_COLUMN, "");
				}

			} catch (IllegalArgumentException e) {
				try {
					for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
						if (row.get(Util.PTM_COLUMN, String.class) == null)
							row.set(Util.PTM_COLUMN, "");
					}
				} catch (Exception e2) {
				}
			} catch (Exception e) {
			}

		} else { // The column exists, but it's necessary to check the cells
			try {

				// Check if ptmsMap has been initialized
				boolean ptmsMapOK = true;
				if (Util.ptmsMap == null)
					ptmsMapOK = false;

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					if (row.get(Util.PTM_COLUMN, String.class) == null)
						row.set(Util.PTM_COLUMN, "");
					else {
						String nodeName = row.get(CyNetwork.NAME, String.class);
						String ptms = row.get(Util.PTM_COLUMN, String.class);

						if (!(ptms.isBlank() || ptms.isEmpty()) && ptmsMapOK) {

							updatePTMsMap(nodeName, ptms, taskMonitor);
						}
					}
				}
			} catch (Exception e) {
			}
		}

		// ####### MONOLINKS #########
		if (nodeTable.getColumn(Util.MONOLINK_COLUMN) == null) {
			try {
				nodeTable.createColumn(Util.MONOLINK_COLUMN, String.class, false);

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					row.set(Util.MONOLINK_COLUMN, "");
				}

			} catch (IllegalArgumentException e) {
				try {
					for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
						if (row.get(Util.MONOLINK_COLUMN, String.class) == null)
							row.set(Util.MONOLINK_COLUMN, "");
					}
				} catch (Exception e2) {
				}
			} catch (Exception e) {
			}

		} else { // The column exists, but it's necessary to check the cells
			try {

				// Check if monolinksMap has been initialized
				boolean monolinksMapOK = true;
				if (Util.monolinksMap == null)
					monolinksMapOK = false;

				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					if (row.get(Util.MONOLINK_COLUMN, String.class) == null)
						row.set(Util.MONOLINK_COLUMN, "");
					else {
						String nodeName = row.get(CyNetwork.NAME, String.class);
						String monolinks = row.get(Util.MONOLINK_COLUMN, String.class);

						if (!(monolinks.isBlank() || monolinks.isEmpty()) && monolinksMapOK) {

							updateMonolinksMap(nodeName, monolinks, taskMonitor);
						}
					}
				}
			} catch (Exception e) {
			}
		}

		isProcessing = false;
	}

	/**
	 * Method responsible for updating protein domains map
	 * 
	 * @param nodeName    node name
	 * @param domainsStr  domains stored in Cytoscape Table
	 * @param taskMonitor task monitor
	 */
	private void updateProteinDomainsMap(String nodeName, String domainsStr, TaskMonitor taskMonitor) {

		List<ProteinDomain> proteinDomains = new ArrayList<ProteinDomain>();
		try {
			String[] cols = domainsStr.split(",");
			for (String col : cols) {
				String[] domainsArray = col.split("\\[|\\]");
				String domainName = domainsArray[0].trim();
				String[] colRange = domainsArray[1].split("-");
				int startId = Integer.parseInt(colRange[0]);
				int endId = Integer.parseInt(colRange[1]);
				proteinDomains.add(new ProteinDomain(domainName, startId, endId, ""));
			}
		} catch (Exception e) {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "ERROR: Node: " + nodeName
					+ " - Protein domains don't match with the pattern 'name[start_index-end_index]'\n");
			return;
		}

		CyNode currentNode = null;

		// Check if the node exists in the network
		Optional<CyRow> isNodePresent = myNetwork.getDefaultNodeTable().getAllRows().stream()
				.filter(new Predicate<CyRow>() {
					public boolean test(CyRow o) {
						return o.get(CyNetwork.NAME, String.class).equals(nodeName);
					}
				}).findFirst();

		if (isNodePresent.isPresent()) {// Get node if exists
			CyRow _node_row = isNodePresent.get();
			currentNode = myNetwork.getNode(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));

			if (proteinDomains.size() > 0) {
				LoadProteinDomainTask.updateProteinDomainsMap(myNetwork, currentNode, proteinDomains);

			}
		} else {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "WARNING: Node " + nodeName + " has not been found.\n");
		}

	}

	/**
	 * Method responsible for updating ptms map
	 * 
	 * @param nodeName    node name
	 * @param ptmsStr     ptms stored in Cytoscape Table
	 * @param taskMonitor task monitor
	 */
	private void updatePTMsMap(String nodeName, String ptmsStr, TaskMonitor taskMonitor) {

		List<PTM> ptmsList = new ArrayList<PTM>();
		try {
			String[] cols = ptmsStr.split(",");
			for (String col : cols) {
				String[] domainsArray = col.split("\\[|\\]");
				String ptmName = domainsArray[0].trim();
				String[] colRange = domainsArray[1].split("-");
				char residue = colRange[0].charAt(0);
				int position = Integer.parseInt(colRange[1]);
				ptmsList.add(new PTM(ptmName, residue, position));
			}
		} catch (Exception e) {
			taskMonitor.showMessage(TaskMonitor.Level.WARN,
					"ERROR: Node: " + nodeName + " - PTMs don't match with the pattern 'name[residue-position]'\n");
			return;
		}

		CyNode currentNode = null;

		// Check if the node exists in the network
		Optional<CyRow> isNodePresent = myNetwork.getDefaultNodeTable().getAllRows().stream()
				.filter(new Predicate<CyRow>() {
					public boolean test(CyRow o) {
						return o.get(CyNetwork.NAME, String.class).equals(nodeName);
					}
				}).findFirst();

		if (isNodePresent.isPresent()) {// Get node if exists
			CyRow _node_row = isNodePresent.get();
			currentNode = myNetwork.getNode(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));

			if (ptmsList.size() > 0) {
				LoadPTMsTask.updatePTMsMap(myNetwork, currentNode, ptmsList);

			}
		} else {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "WARNING: Node " + nodeName + " has not been found.\n");
		}

	}

	/**
	 * Method responsible for updating monolink map
	 * 
	 * @param nodeName     node name
	 * @param monolinksStr monolinks stored in Cytoscape Table
	 * @param taskMonitor  task monitor
	 */
	private void updateMonolinksMap(String nodeName, String monolinksStr, TaskMonitor taskMonitor) {

		List<CrossLink> monolinksList = new ArrayList<CrossLink>();
		try {
			String[] cols = monolinksStr.split(",");
			for (String col : cols) {
				// SEQUENCE[xl_pos_a-xl_pos_b][pept_pos1-pept_pos2]
				String[] monolinksArray = col.split("\\[|\\]");
				String sequence = monolinksArray[0].trim();

				String[] colXLpositions = monolinksArray[1].split("-");
				int xl_a = Integer.parseInt(colXLpositions[0]);
				int xl_b = Integer.parseInt(colXLpositions[1]);

				String[] colPeptidePosition_Protein = monolinksArray[3].split("-");
				int pos_a = Integer.parseInt(colPeptidePosition_Protein[0]);
				int pos_b = Integer.parseInt(colPeptidePosition_Protein[1]);
				monolinksList.add(new CrossLink(sequence, xl_a, xl_b, pos_a, pos_b));
			}
		} catch (Exception e) {
			taskMonitor.showMessage(TaskMonitor.Level.WARN,
					"ERROR: Node: " + nodeName + " - Monolinks don't match with the pattern 'name[xl_a-xl_b]'\n");
			return;
		}

		CyNode currentNode = null;

		// Check if the node exists in the network
		Optional<CyRow> isNodePresent = myNetwork.getDefaultNodeTable().getAllRows().stream()
				.filter(new Predicate<CyRow>() {
					public boolean test(CyRow o) {
						return o.get(CyNetwork.NAME, String.class).equals(nodeName);
					}
				}).findFirst();

		if (isNodePresent.isPresent()) {// Get node if exists
			CyRow _node_row = isNodePresent.get();
			currentNode = myNetwork.getNode(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));

			if (monolinksList.size() > 0) {
				String node_name = myNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get(CyNetwork.NAME,
						String.class);

				Protein ptn = new Protein(node_name, "", monolinksList);
				LoadMonolinksTask.updateMonolinksMap(myNetwork, currentNode, ptn);

			}
		} else {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "WARNING: Node " + nodeName + " has not been found.\n");
		}

	}
}
