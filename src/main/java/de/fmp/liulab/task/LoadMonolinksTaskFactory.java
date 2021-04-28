package de.fmp.liulab.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.fmp.liulab.internal.CustomChartListener;

/**
 * Class responsible for loading monolinks factory
 * @author borges.diogo
 *
 */
public class LoadMonolinksTaskFactory extends AbstractTaskFactory {

	private CyApplicationManager cyApplicationManager;
	private VisualMappingManager vmmServiceRef;
	private CyCustomGraphics2Factory vgFactory;

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager main app manager
	 * @param vmmServiceRef        visual mapping manager
	 * @param customChartListener  chart style listener
	 */
	public LoadMonolinksTaskFactory(CyApplicationManager cyApplicationManager, final VisualMappingManager vmmServiceRef,
			CustomChartListener customChartListener) {
		this.cyApplicationManager = cyApplicationManager;
		this.vmmServiceRef = vmmServiceRef;
		this.vgFactory = customChartListener.getFactory();
	}

	/**
	 * Empty constructor
	 */
	public LoadMonolinksTaskFactory() {
	}

	/**
	 * Method responsible for initializing task
	 */
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new LoadMonolinksTask(cyApplicationManager, vmmServiceRef, vgFactory));
	}

}
