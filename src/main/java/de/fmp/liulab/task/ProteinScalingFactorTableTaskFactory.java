package de.fmp.liulab.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ProteinScalingFactorTableTaskFactory extends AbstractTaskFactory {

	public ProteinScalingFactorTableTaskFactory() {
	}

	public TaskIterator createTaskIterator(CyNetwork myNetwork) {
		return new TaskIterator(new ProteinScalingFactorTableTask(myNetwork));
	}

	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
