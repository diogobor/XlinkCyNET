package de.fmp.liulab.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import de.fmp.liulab.internal.action.ControlURLAction;
import de.fmp.liulab.internal.action.ExportProteinDomainsAction;
import de.fmp.liulab.internal.action.LoadProteinDomainsAction;
import de.fmp.liulab.internal.action.MainPanelAction;
import de.fmp.liulab.internal.action.SetDomainColorAction;
import de.fmp.liulab.internal.action.ShortcutSingleNodeExecuteAction;
import de.fmp.liulab.task.LoadProteinDomainsTaskFactory;
import de.fmp.liulab.task.MainSingleNodeTaskFactory;
import de.fmp.liulab.task.ProteinScalingFactorTableTaskFactory;
import de.fmp.liulab.task.SetDomainColorTaskFactory;

/**
 * Class responsible for initializing cytoscape methods
 * 
 * @author diogobor
 *
 */
public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	/**
	 * Method responsible for starting context
	 */
	public void start(BundleContext bc) {

		// #### 1 - ABOUT ####
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		ControlURLAction controlURLAction = new ControlURLAction(openBrowser);
		// ###############

		// #### 2 - PANEL (SETTINGS) ####
		CySwingApplication cytoscapeDesktopService = getService(bc, CySwingApplication.class);
		MainControlPanel mainControlPanel = new MainControlPanel();
		MainPanelAction panelAction = new MainPanelAction(cytoscapeDesktopService, mainControlPanel);

		// ##############################

		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		VisualMappingManager vmmServiceRef = getService(bc, VisualMappingManager.class);
		CustomChartListener customChartListener = new CustomChartListener();
		HandleFactory handleFactory = getService(bc, HandleFactory.class);
		BendFactory bendFactory = getService(bc, BendFactory.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);

		// ### 3 - PROTEIN DOMAINS ###

		// ### 3.1 - EXPORT ###
		ExportProteinDomainsAction myExportProteinDomainsAction = new ExportProteinDomainsAction(cyApplicationManager);

		// ####################

		// ### 3.2 - LOAD ####
		TaskFactory myLoadProteinDomainsFactory = new LoadProteinDomainsTaskFactory(cyApplicationManager, vmmServiceRef,
				customChartListener);

		LoadProteinDomainsAction myLoadProteinDomainsAction = new LoadProteinDomainsAction(dialogTaskManager,
				myLoadProteinDomainsFactory);

		// ###################

		// ### 3.2 - LOAD ####
		TaskFactory mySetProteinDomainsColorFactory = new SetDomainColorTaskFactory(cyApplicationManager, vmmServiceRef,
				customChartListener);

		SetDomainColorAction mySetProteinDomainsAction = new SetDomainColorAction(dialogTaskManager,
				mySetProteinDomainsColorFactory);

		// ###################

		// ##############################

		// #### 4 - EXECUTE SINGLE NODE ####

		registerServiceListener(bc, customChartListener, "addCustomGraphicsFactory", "removeCustomGraphicsFactory",
				CyCustomGraphics2Factory.class);

		TaskFactory mySingleNodeContextMenuFactory = new MainSingleNodeTaskFactory(cyApplicationManager, vmmServiceRef,
				customChartListener, bendFactory, handleFactory, true);

		CyNodeViewContextMenuFactory myNodeViewContextMenuFactory = new MainContextMenu(mySingleNodeContextMenuFactory,
				dialogTaskManager);

		Properties myNodeViewContextMenuFactoryProps = new Properties();
		myNodeViewContextMenuFactoryProps.put(PREFERRED_MENU, "Apps");
		myNodeViewContextMenuFactoryProps.put(COMMAND_DESCRIPTION,
				"XlinkCyNET :: App responsible for plotting cross-links of proteins.");
		// Our menu item should only be enabled if at least one network
		// view exists.
		myNodeViewContextMenuFactoryProps.put(ServiceProperties.ENABLE_FOR, "networkAndView");

		TaskFactory mySingleNodeShortCutFactory = new MainSingleNodeTaskFactory(cyApplicationManager, vmmServiceRef,
				customChartListener, bendFactory, handleFactory, false);

		ShortcutSingleNodeExecuteAction myShortcutSingleNodeAction = new ShortcutSingleNodeExecuteAction(
				dialogTaskManager, mySingleNodeShortCutFactory);

		// ##############################

		// ##### PROTEIN SCALING FACTOR TABLE #####
		ProteinScalingFactorTableTaskFactory proteinScalingFactorTableTaskFactory = new ProteinScalingFactorTableTaskFactory();

		// ########################################

		// #### LISTENER ######

		ViewChangedListener updateViewListener = new UpdateViewListener(cyApplicationManager, handleFactory,
				bendFactory, vmmServiceRef, dialogTaskManager, proteinScalingFactorTableTaskFactory);

		registerService(bc, updateViewListener, ViewChangedListener.class, new Properties());
		registerService(bc, updateViewListener, RowsSetListener.class, new Properties());
		registerService(bc, updateViewListener, SetCurrentNetworkListener.class, new Properties());
		// #####################

		// #### SERVICES #####
		registerService(bc, myShortcutSingleNodeAction, CyAction.class, new Properties());
		registerService(bc, myLoadProteinDomainsAction, CyAction.class, new Properties());
		registerService(bc, myExportProteinDomainsAction, CyAction.class, new Properties());
		registerService(bc, mySetProteinDomainsAction, CyAction.class, new Properties());

		registerService(bc, mainControlPanel, CytoPanelComponent.class, new Properties());
		registerService(bc, panelAction, CyAction.class, new Properties());

		registerService(bc, controlURLAction, CyAction.class, new Properties());

		registerAllServices(bc, myNodeViewContextMenuFactory, myNodeViewContextMenuFactoryProps);

	}
}