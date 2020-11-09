package de.fmp.liulab.task;

import java.io.IOException;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.utils.Util;

/**
 * Class responsible for expanding the protein bar
 * @author diogobor
 *
 */
public class ProteinScalingFactorHorizontalExpansionTableTask extends AbstractTask {

	private CyNetwork myNetwork;

	/**
	 * Constructor
	 * @param myNetwork current network
	 */
	public ProteinScalingFactorHorizontalExpansionTableTask(CyNetwork myNetwork) {
		this.myNetwork = myNetwork;
	}

	/**
	 * Default method
	 */
	@Override
	public void run(TaskMonitor tm) throws IOException {

		if (myNetwork == null)
			return;

		// Create Scaling factor protein column
		CyTable nodeTable = myNetwork.getDefaultNodeTable();
		if (nodeTable.getColumn(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME) == null) {
			nodeTable.createColumn(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, Double.class, false);

			for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
				row.set(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, 1.0d);
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
		
		if (nodeTable.getColumn(Util.HORIZONTAL_EXPANSION_COLUMN_NAME) == null) {
			nodeTable.createColumn(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, Boolean.class, false);

			for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
				row.set(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, true);
			}

		} else { // The column exists, but it's necessary to check the cells
			try {
				for (CyRow row : myNetwork.getDefaultNodeTable().getAllRows()) {
					if (row.get(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, Boolean.class) == null)
						row.set(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, true);
				}
			} catch (Exception e) {
			}

		}
	}
}
