package de.fmp.liulab.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Class responsible for calling the class to expand the protein bar
 * @author diogobor
 *
 */
public class ProteinScalingFactorHorizontalExpansionTableTaskFactory extends AbstractTaskFactory {

	/**
	 * Empty constructor
	 */
	public ProteinScalingFactorHorizontalExpansionTableTaskFactory() {
	}

	/**
	 * Method responsible for calling thee task 
	 * @param myNetwork
	 * @return
	 */
	public TaskIterator createTaskIterator(CyNetwork myNetwork) {
		return new TaskIterator(new ProteinScalingFactorHorizontalExpansionTableTask(myNetwork));
	}

	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
