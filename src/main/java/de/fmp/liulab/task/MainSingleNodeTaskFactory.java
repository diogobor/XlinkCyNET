package de.fmp.liulab.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.fmp.liulab.internal.CustomChartListener;

/**
 * Class responsible for initializing Layout node task
 * 
 * @author diogobor
 *
 */
public class MainSingleNodeTaskFactory extends AbstractTaskFactory {

	private CyApplicationManager cyApplicationManager;
	private VisualMappingManager vmmServiceRef;
	private CyCustomGraphics2Factory vgFactory;
	private HandleFactory handleFactory;
	private BendFactory bendFactory;
	private CyTableFactory tableFactory;
	private CyTableManager tableManager;
	private boolean forcedWindowOpen;

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager main app manager
	 * @param vmmServiceRef        visual mapping manager
	 * @param customChartListener  chart style listener
	 * @param bendFactory          bend factory
	 * @param handleFactory        handle factory
	 * @param forcedWindowOpen     forced window open
	 */
	public MainSingleNodeTaskFactory(CyApplicationManager cyApplicationManager,
			final VisualMappingManager vmmServiceRef, CustomChartListener customChartListener, BendFactory bendFactory,
			HandleFactory handleFactory, CyTableFactory tableFactory, CyTableManager tableManager,
			boolean forcedWindowOpen) {
		this.cyApplicationManager = cyApplicationManager;
		this.vmmServiceRef = vmmServiceRef;
		this.vgFactory = customChartListener.getFactory();
		this.bendFactory = bendFactory;
		this.handleFactory = handleFactory;
		this.tableFactory = tableFactory;
		this.tableManager = tableManager;
		this.forcedWindowOpen = forcedWindowOpen;
	}

	/**
	 * Empty constructor
	 */
	public MainSingleNodeTaskFactory() {
	}

	/**
	 * Method responsible for initializing task
	 */
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MainSingleNodeTask(cyApplicationManager, vmmServiceRef, vgFactory, bendFactory,
				handleFactory, tableFactory, tableManager, forcedWindowOpen));
	}

}