package de.fmp.liulab.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;

import java.util.Locale;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import de.fmp.liulab.core.ConfigurationManager;
import de.fmp.liulab.internal.action.ControlURLAction;
import de.fmp.liulab.internal.action.ExportProteinDomainsAction;
import de.fmp.liulab.internal.action.LoadProteinDomainsAction;
import de.fmp.liulab.internal.action.MainPanelAction;
import de.fmp.liulab.internal.action.ReadMeAction;
import de.fmp.liulab.internal.action.SetDomainColorAction;
import de.fmp.liulab.internal.action.ShortcutSingleNodeExecuteAction;
import de.fmp.liulab.internal.action.ShortcutWindowSingleNodeLayout;
import de.fmp.liulab.task.LoadProteinDomainsTaskFactory;
import de.fmp.liulab.task.MainSingleNodeTaskFactory;
import de.fmp.liulab.task.ProteinScalingFactorHorizontalExpansionTableTaskFactory;
import de.fmp.liulab.task.SetDomainColorTaskFactory;
import de.fmp.liulab.task.UpdateViewerTaskFactory;
import de.fmp.liulab.task.command_lines.ApplyRestoreStyleCommandTask;
import de.fmp.liulab.task.command_lines.ApplyRestoreStyleCommandTaskFactory;
import de.fmp.liulab.task.command_lines.LoadProteinDomainsCommandTask;
import de.fmp.liulab.task.command_lines.LoadProteinDomainsCommandTaskFactory;
import de.fmp.liulab.task.command_lines.ReadMeCommandTask;
import de.fmp.liulab.task.command_lines.ReadMeCommandTaskFactory;
import de.fmp.liulab.task.command_lines.SetParametersCommandTask;
import de.fmp.liulab.task.command_lines.SetParametersCommandTaskFactory;

/**
 * Class responsible for initializing cytoscape methods
 * 
 * @author diogobor
 *
 */
public class CyActivator extends AbstractCyActivator {

	private Properties XlinkCyNETProps;
	private ConfigurationManager cm;
	public static final String XLINKCYNET_COMMAND_NAMESPACE = "xlinkcynet";

	public CyActivator() {
		super();
	}

	/**
	 * Method responsible for starting context
	 */
	public void start(BundleContext bc) {

		// #### 1 - ABOUT ####
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		String version = bc.getBundle().getVersion().toString();
		ControlURLAction controlURLAction = new ControlURLAction(openBrowser, version);
		ReadMeAction readMe = new ReadMeAction(openBrowser);

		// ###############

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

		Properties myNodeViewContextMenuFactoryProps = new Properties();
		myNodeViewContextMenuFactoryProps.put(PREFERRED_MENU, "Apps");
		// Our menu item should only be enabled if at least one network
		// view exists.
		myNodeViewContextMenuFactoryProps.put(ServiceProperties.ENABLE_FOR, "networkAndView");

		TaskFactory mySingleNodeShortCutFactory = new MainSingleNodeTaskFactory(cyApplicationManager, vmmServiceRef,
				customChartListener, bendFactory, handleFactory, false);

		ShortcutSingleNodeExecuteAction myShortcutSingleNodeAction = new ShortcutSingleNodeExecuteAction(
				dialogTaskManager, mySingleNodeShortCutFactory);

		TaskFactory mySingleNodeContextMenuFactory = new MainSingleNodeTaskFactory(cyApplicationManager, vmmServiceRef,
				customChartListener, bendFactory, handleFactory, true);

		CyNodeViewContextMenuFactory myNodeViewContextMenuFactory = new MainContextMenu(mySingleNodeContextMenuFactory,
				dialogTaskManager);

		ShortcutWindowSingleNodeLayout myShortcutWindowSingleNodeAction = new ShortcutWindowSingleNodeLayout(
				dialogTaskManager, mySingleNodeContextMenuFactory);

		// ##############################

		// ##### PROTEIN SCALING FACTOR TABLE #####
		ProteinScalingFactorHorizontalExpansionTableTaskFactory proteinScalingFactorTableTaskFactory = new ProteinScalingFactorHorizontalExpansionTableTaskFactory();

		// ########################################

		// #### LISTENER ######

		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		UpdateViewerTaskFactory updateViewerTaskFactory = new UpdateViewerTaskFactory();
		ViewChangedListener updateViewListener = new UpdateViewListener(cyApplicationManager, handleFactory,
				bendFactory, vmmServiceRef, dialogTaskManager, proteinScalingFactorTableTaskFactory,
				updateViewerTaskFactory, cyNetworkViewManagerServiceRef);

		registerService(bc, updateViewListener, ViewChangedListener.class, new Properties());
		registerService(bc, updateViewListener, RowsSetListener.class, new Properties());
		registerService(bc, updateViewListener, SetCurrentNetworkListener.class, new Properties());
		registerService(bc, updateViewListener, NetworkAddedListener.class, new Properties());
		// #####################

		// #### 2 - PANEL (SETTINGS) ####
		init_default_params(bc);

		CySwingApplication cytoscapeDesktopService = getService(bc, CySwingApplication.class);
		MainControlPanel mainControlPanel = new MainControlPanel(XlinkCyNETProps, cm);
		MainPanelAction panelAction = new MainPanelAction(cytoscapeDesktopService, mainControlPanel);

		// ##############################

		// #### SERVICES #####
		registerService(bc, myShortcutWindowSingleNodeAction, CyAction.class, new Properties());
		registerService(bc, myLoadProteinDomainsAction, CyAction.class, new Properties());
		registerService(bc, myShortcutSingleNodeAction, CyAction.class, new Properties());
		registerService(bc, myExportProteinDomainsAction, CyAction.class, new Properties());
		registerService(bc, mySetProteinDomainsAction, CyAction.class, new Properties());

		registerService(bc, mainControlPanel, CytoPanelComponent.class, new Properties());
		registerService(bc, panelAction, CyAction.class, new Properties());

		registerService(bc, readMe, CyAction.class, new Properties());
		registerService(bc, controlURLAction, CyAction.class, new Properties());

		registerAllServices(bc, myNodeViewContextMenuFactory, myNodeViewContextMenuFactoryProps);
		// ###################

		// ####### COMMANDS ########

		init_commands(bc, cyApplicationManager, mySingleNodeShortCutFactory, dialogTaskManager, openBrowser);

		// #########################
	}

	private void init_commands(BundleContext bc, CyApplicationManager cyApplicationManager,
			TaskFactory mySingleNodeShortCutFactory, DialogTaskManager dialogTaskManager, OpenBrowser openBrowser) {

		// Register Read Me function
		Properties readmeProperties = new Properties();
		readmeProperties.setProperty(COMMAND_NAMESPACE, XLINKCYNET_COMMAND_NAMESPACE);
		readmeProperties.setProperty(COMMAND, "readMe");
		readmeProperties.setProperty(COMMAND_DESCRIPTION, ReadMeCommandTaskFactory.DESCRIPTION);
		readmeProperties.setProperty(COMMAND_LONG_DESCRIPTION, ReadMeCommandTaskFactory.LONG_DESCRIPTION);
		readmeProperties.setProperty(COMMAND_EXAMPLE_JSON, ReadMeCommandTask.getExample());
		readmeProperties.setProperty(COMMAND_SUPPORTS_JSON, "true");

		TaskFactory readMeTaskFactory = new ReadMeCommandTaskFactory(openBrowser);
		registerAllServices(bc, readMeTaskFactory, readmeProperties);

		// Register apply / restore style function
		Properties applyStyleRestoreProperties = new Properties();
		applyStyleRestoreProperties.setProperty(COMMAND_NAMESPACE, XLINKCYNET_COMMAND_NAMESPACE);
		applyStyleRestoreProperties.setProperty(COMMAND, "applyRestoreStyle");
		applyStyleRestoreProperties.setProperty(COMMAND_DESCRIPTION, ApplyRestoreStyleCommandTaskFactory.DESCRIPTION);
		applyStyleRestoreProperties.setProperty(COMMAND_LONG_DESCRIPTION,
				ApplyRestoreStyleCommandTaskFactory.LONG_DESCRIPTION);
		applyStyleRestoreProperties.setProperty(COMMAND_EXAMPLE_JSON, ApplyRestoreStyleCommandTask.getExample());
		applyStyleRestoreProperties.setProperty(COMMAND_SUPPORTS_JSON, "true");

		TaskFactory applyRestoreStyleTaskFactory = new ApplyRestoreStyleCommandTaskFactory(cyApplicationManager,
				(MainSingleNodeTaskFactory) mySingleNodeShortCutFactory, dialogTaskManager);
		registerAllServices(bc, applyRestoreStyleTaskFactory, applyStyleRestoreProperties);

		// Register set parameters function
		Properties setParametersProperties = new Properties();
		setParametersProperties.setProperty(COMMAND_NAMESPACE, XLINKCYNET_COMMAND_NAMESPACE);
		setParametersProperties.setProperty(COMMAND, "setParameters");
		setParametersProperties.setProperty(COMMAND_DESCRIPTION, SetParametersCommandTaskFactory.DESCRIPTION);
		setParametersProperties.setProperty(COMMAND_LONG_DESCRIPTION, SetParametersCommandTaskFactory.LONG_DESCRIPTION);
		setParametersProperties.setProperty(COMMAND_EXAMPLE_JSON, SetParametersCommandTask.getExample());
		setParametersProperties.setProperty(COMMAND_SUPPORTS_JSON, "true");

		TaskFactory setParametersTaskFactory = new SetParametersCommandTaskFactory();
		registerAllServices(bc, setParametersTaskFactory, setParametersProperties);

		// Register load protein domains function
		Properties loadProteinDomainsProperties = new Properties();
		loadProteinDomainsProperties.setProperty(COMMAND_NAMESPACE, XLINKCYNET_COMMAND_NAMESPACE);
		loadProteinDomainsProperties.setProperty(COMMAND, "loadProteinDomains");
		loadProteinDomainsProperties.setProperty(COMMAND_DESCRIPTION, LoadProteinDomainsCommandTaskFactory.DESCRIPTION);
		loadProteinDomainsProperties.setProperty(COMMAND_LONG_DESCRIPTION,
				LoadProteinDomainsCommandTaskFactory.LONG_DESCRIPTION);
		loadProteinDomainsProperties.setProperty(COMMAND_EXAMPLE_JSON, LoadProteinDomainsCommandTask.getExample());
		loadProteinDomainsProperties.setProperty(COMMAND_SUPPORTS_JSON, "true");

		TaskFactory loadProteinDomainsTaskFactory = new LoadProteinDomainsCommandTaskFactory(cyApplicationManager);
		registerAllServices(bc, loadProteinDomainsTaskFactory, loadProteinDomainsProperties);

	}

	private void init_default_params(BundleContext bc) {

		try {
			XlinkCyNETProps = (Properties) getService(bc, CyProperty.class, "(cyPropertyName=xlinkcynet.props)");

		} catch (Exception e) {
			Properties propsReaderServiceProps = null;
			if (XlinkCyNETProps == null) {
				cm = new ConfigurationManager("xlinkcynet", "xlinkcynet.props");
				propsReaderServiceProps = new Properties();
				propsReaderServiceProps.setProperty("cyPropertyName", "xlinkcynet.props");

				Locale usEnglish = new Locale("en", "US");
				cm.getProperties().setProperty("locale", usEnglish.getLanguage() + usEnglish.getCountry());
				XlinkCyNETProps = cm.getProperties();

				registerAllServices(bc, cm, propsReaderServiceProps);
			}
		}
	}
}