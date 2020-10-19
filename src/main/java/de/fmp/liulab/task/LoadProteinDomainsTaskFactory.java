package de.fmp.liulab.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.fmp.liulab.internal.CustomChartListener;

public class LoadProteinDomainsTaskFactory extends AbstractTaskFactory {

	private CyApplicationManager cyApplicationManager;
	private VisualMappingManager vmmServiceRef;
	private CyCustomGraphics2Factory vgFactory;

	// Constructor
	public LoadProteinDomainsTaskFactory(CyApplicationManager cyApplicationManager, final VisualMappingManager vmmServiceRef,
			CustomChartListener customChartListener) {
		this.cyApplicationManager = cyApplicationManager;
		this.vmmServiceRef = vmmServiceRef;
		this.vgFactory = customChartListener.getFactory();
	}

	public LoadProteinDomainsTaskFactory() {
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new LoadProteinDomainTask(cyApplicationManager, vmmServiceRef, vgFactory));
	}
}
