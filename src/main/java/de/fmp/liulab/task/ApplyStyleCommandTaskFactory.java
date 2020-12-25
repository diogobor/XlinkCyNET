package de.fmp.liulab.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class ApplyStyleCommandTaskFactory extends AbstractTaskFactory {

	public static final String DESCRIPTION = "Apply style to a node";
	public static final String LONG_DESCRIPTION = "Command responsible for expanding a node to display all identified link as well as protein domains.";

	private CyApplicationManager cyApplicationManager;
	private MainSingleNodeTaskFactory myFactory;
	private DialogTaskManager dialogTaskManager;

	public ApplyStyleCommandTaskFactory(CyApplicationManager cyApplicationManager, MainSingleNodeTaskFactory myFactory,
			DialogTaskManager dialogTaskManager) {
		this.cyApplicationManager = cyApplicationManager;
		this.myFactory = myFactory;
		this.dialogTaskManager = dialogTaskManager;
	}

	public boolean isReady() {
		return true;
	}

	public TaskIterator createTaskIterator() {

		return new TaskIterator(new ApplyStyleCommandTask(cyApplicationManager, myFactory, dialogTaskManager));
	}

}
