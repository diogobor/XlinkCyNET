package de.fmp.liulab.task;

import java.io.IOException;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.utils.Util;

public class ProteinScalingFactorTableTask extends AbstractTask {

	private CyNetwork myNetwork;

	public ProteinScalingFactorTableTask(CyNetwork myNetwork) {
		this.myNetwork = myNetwork;
	}

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
	}
}
