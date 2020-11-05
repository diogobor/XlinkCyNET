package de.fmp.liulab.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ProteinScalingFactorHorizontalExpansionTableTaskFactory extends AbstractTaskFactory {

	public ProteinScalingFactorHorizontalExpansionTableTaskFactory() {
	}

	public TaskIterator createTaskIterator(CyNetwork myNetwork) {
		return new TaskIterator(new ProteinScalingFactorHorizontalExpansionTableTask(myNetwork));
	}

	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
