package de.fmp.liulab.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.fmp.liulab.internal.CustomChartListener;

public class MainSingleNodeTaskFactory extends AbstractTaskFactory {

	private CyApplicationManager cyApplicationManager;
	private VisualMappingManager vmmServiceRef;
	private CyCustomGraphics2Factory vgFactory;
	private HandleFactory handleFactory;
	private BendFactory bendFactory;
	private boolean forcedWindowOpen;

	// Constructor
	public MainSingleNodeTaskFactory(CyApplicationManager cyApplicationManager,
			final VisualMappingManager vmmServiceRef, CustomChartListener customChartListener, BendFactory bendFactory,
			HandleFactory handleFactory, boolean forcedWindowOpen) {
		this.cyApplicationManager = cyApplicationManager;
		this.vmmServiceRef = vmmServiceRef;
		this.vgFactory = customChartListener.getFactory();
		this.bendFactory = bendFactory;
		this.handleFactory = handleFactory;
		this.forcedWindowOpen = forcedWindowOpen;
	}

	public MainSingleNodeTaskFactory() {
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new MainSingleNodeTask(cyApplicationManager, vmmServiceRef, vgFactory, bendFactory, handleFactory, forcedWindowOpen));
	}

}