package de.fmp.liulab.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import de.fmp.liulab.task.LoadProteinDomainTask;
import de.fmp.liulab.task.MainSingleNodeTask;
import de.fmp.liulab.task.ProteinScalingFactorHorizontalExpansionTableTask;
import de.fmp.liulab.task.ProteinScalingFactorHorizontalExpansionTableTaskFactory;
import de.fmp.liulab.task.UpdateViewerTaskFactory;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for updating layout
 * 
 * @author borges.diogo
 *
 */
public class UpdateViewListener
		implements ViewChangedListener, RowsSetListener, SetCurrentNetworkListener, NetworkAddedListener {

	private CyApplicationManager cyApplicationManager;
	private CyNetwork myNetwork;
	private CyNetworkView netView;
	private HandleFactory handleFactory;
	private BendFactory bendFactory;
	private VisualStyle style;
	private CyNetworkViewManager networkViewManager;

	private DialogTaskManager dialogTaskManager;
	private ProteinScalingFactorHorizontalExpansionTableTaskFactory proteinScalingFactorHorizontalExpansionTableTaskFactory;
	private UpdateViewerTaskFactory updateViewerTaskFactory;

	private CyNode selectedNode;;
	private CyNode current_node;

	public static boolean isNodeModified = false;

	private List<Long> nodes_suids = new ArrayList<Long>();

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager                                    main app
	 *                                                                manager
	 * @param handleFactory                                           handle factory
	 * @param bendFactory                                             bend factory
	 * @param vmmServiceRef                                           visual mapping
	 *                                                                manager
	 * @param dialogTaskManager                                       task manager
	 * @param proteinScalingFactorHorizontalExpansionTableTaskFactory protein length
	 *                                                                scaling factor
	 *                                                                factory
	 * @param updateViewerTaskFactory                                 update viewer
	 *                                                                task factory
	 * @param networkViewManager                                      networkview
	 *                                                                manager
	 */
	public UpdateViewListener(CyApplicationManager cyApplicationManager, HandleFactory handleFactory,
			BendFactory bendFactory, VisualMappingManager vmmServiceRef, DialogTaskManager dialogTaskManager,
			ProteinScalingFactorHorizontalExpansionTableTaskFactory proteinScalingFactorHorizontalExpansionTableTaskFactory,
			UpdateViewerTaskFactory updateViewerTaskFactory, CyNetworkViewManager networkViewManager) {
		this.cyApplicationManager = cyApplicationManager;
		this.handleFactory = handleFactory;
		this.bendFactory = bendFactory;
		this.style = vmmServiceRef.getCurrentVisualStyle();
		this.dialogTaskManager = dialogTaskManager;
		this.proteinScalingFactorHorizontalExpansionTableTaskFactory = proteinScalingFactorHorizontalExpansionTableTaskFactory;
		this.updateViewerTaskFactory = updateViewerTaskFactory;
		this.networkViewManager = networkViewManager;
	}

	/**
	 * Method responsible for capturing the selected node.
	 */
	@Override
	public void handleEvent(RowsSetEvent e) {

		myNetwork = cyApplicationManager.getCurrentNetwork();
		Collection<CyNetworkView> views = networkViewManager.getNetworkViews(myNetwork);
		if (views.size() != 0)
			netView = views.iterator().next();
		else
			return;
		nodes_suids.clear();

		try {
			// First see if this even has anything to do with selections
			if (!e.containsColumn(CyNetwork.SELECTED)) {
				// Nope, we're done
				return;
			}

			// For each selected node, get the view in the current network
			// and change the shape
			if (e.getSource() != myNetwork.getDefaultNodeTable())
				return;

			if (e.getColumnRecords(CyNetwork.SELECTED).size() == 1) {
				this.selectedNode = null;
				for (RowSetRecord record : e.getColumnRecords(CyNetwork.SELECTED)) {
					Long suid = record.getRow().get(CyIdentifiable.SUID, Long.class);
					Boolean value = (Boolean) record.getValue();
					if (value.equals(Boolean.TRUE)) {
						this.selectedNode = myNetwork.getNode(suid);

						updateNodesAndEdges(new ArrayList<CyNode>(Arrays.asList(this.selectedNode)));
					}
				}
			}
		} catch (Exception exception) {
		}
	}

	/**
	 * Method responsible for update all nodes according to the movement of the
	 * mouse.
	 */
	@Override
	public void handleEvent(ViewChangedEvent<?> e) {

		try {

			if (!MainSingleNodeTask.isPlotDone)
				return;
			if (!LoadProteinDomainTask.isPlotDone)
				return;

			myNetwork = cyApplicationManager.getCurrentNetwork();
			Collection<CyNetworkView> views = networkViewManager.getNetworkViews(myNetwork);
			if (views.size() != 0)
				netView = views.iterator().next();
			else
				return;

			List<CyNode> nodes = CyTableUtil.getNodesInState(myNetwork, CyNetwork.SELECTED, true);

			Set<CyNode> nodeSuidList = new HashSet<CyNode>();

			for (ViewChangeRecord<?> record : e.getPayloadCollection()) {
				View<?> record_view = record.getView();
				if (record_view.getModel() instanceof CyNode) {
					nodeSuidList.add(((CyNode) record_view.getModel()));
				}
			}

			if (nodeSuidList.size() == 0)// It means no CyNode has been selected
				return;

			List<CyNode> nodes_to_be_updated = new ArrayList<CyNode>();
			// Check if all selected nodes have been modified
			for (final CyNode _node : nodes) {

				String node_name = myNetwork.getRow(_node).get(CyNetwork.NAME, String.class);
				// Check if the node exists in the network

				Optional<CyNode> isNodePresent = nodeSuidList.stream().filter(new Predicate<CyNode>() {
					public boolean test(CyNode o) {
						return o.getSUID() == _node.getSUID();
					}
				}).findFirst();
				if (!isNodePresent.isPresent()) {
					if (!(node_name.contains("Source") || node_name.contains("Target") || node_name.contains("PTM")))
						return;
				}

				if (node_name.contains("Source") || node_name.contains("Target") || node_name.contains("PTM")) {
					nodes_to_be_updated.add(_node);
				} else {
					Optional<Long> isNodePresent_SUID = nodes_suids.stream().filter(new Predicate<Long>() {
						public boolean test(Long o) {
							return o == _node.getSUID();
						}
					}).findFirst();
					if (!isNodePresent_SUID.isPresent()) {
						nodes_to_be_updated.add(_node);
					}
				}
			}

			updateNodesAndEdges(nodes_to_be_updated);

		} catch (Exception exception) {
		}
	}

	/**
	 * Update nodes and edges
	 * 
	 * @param current_node current node
	 */
	private void updateNodesAndEdges(List<CyNode> nodes) {
		final Iterator<CyNode> _iterator_CyNode = nodes.iterator();

		if (!_iterator_CyNode.hasNext())
			return;

		current_node = _iterator_CyNode.next();

		String node_name = myNetwork.getRow(current_node).get(CyNetwork.NAME, String.class);
		while (node_name.contains("Source") || node_name.contains("Target") || node_name.contains("PTM")) {
			if (_iterator_CyNode.hasNext()) {
				current_node = _iterator_CyNode.next();
				node_name = myNetwork.getRow(current_node).get(CyNetwork.NAME, String.class);
			} else
				return;
		}

		View<CyNode> nodeView = netView.getNodeView(current_node);
		double current_posX = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		double current_posY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		if (Util.mapLastNodesPosition.containsKey(current_node)) {
			double last_posX = (double) Util.mapLastNodesPosition.get(current_node).getFirst();
			double last_posY = (double) Util.mapLastNodesPosition.get(current_node).getSecond();
			if (current_posX == last_posX && current_posY == last_posY)
				return;
		}

		if (!Util.stopUpdateViewer) {
			if (this.dialogTaskManager != null && this.updateViewerTaskFactory != null) {

				nodes_suids.add(current_node.getSUID());

				TaskIterator ti = this.updateViewerTaskFactory.createTaskIterator(cyApplicationManager, handleFactory,
						bendFactory, myNetwork, netView, current_node);

				TaskObserver observer = new TaskObserver() {

					@Override
					public void taskFinished(ObservableTask task) {

					}

					@Override
					public void allFinished(FinishStatus finishStatus) {
						if (!_iterator_CyNode.hasNext()) {
							nodes_suids.remove(current_node.getSUID());
							return;
						}

						final List<CyNode> remainingList = new ArrayList<CyNode>();
						_iterator_CyNode.forEachRemaining(new Consumer<CyNode>() {
							@Override
							public void accept(CyNode key) {
								remainingList.add(key);
							}
						});
						nodes_suids.remove(current_node.getSUID());
						updateNodesAndEdges(remainingList);
					}
				};

				this.dialogTaskManager.execute(ti, observer);

			}
		}

	}

	/**
	 * Method responsible for update all nodes according to the current selected
	 * network
	 */
	
	public void handleEvent(SetCurrentNetworkEvent e){

		MainSingleNodeTask.interLinks = null;
		MainSingleNodeTask.intraLinks = null;
		Util.mapLastNodesPosition.clear();
		nodes_suids.clear();

		try {

			// Update variables
			if (cyApplicationManager == null)
				return;
			myNetwork = cyApplicationManager.getCurrentNetwork();
			if (myNetwork == null)
				return;

			// Create protein scaling factor table
			createAuxiliarColumnsTable();

			Collection<CyNetworkView> views = networkViewManager.getNetworkViews(myNetwork);
			if (views.size() != 0)
				netView = views.iterator().next();
			else
				return;

			if (netView == null)
				return;
			if (cyApplicationManager.getCurrentRenderingEngine() == null)
				return;

			// Load network and netview to XlinkCyNET setting to update the edges plot
			MainControlPanel.myNetwork = myNetwork;
			MainControlPanel.netView = netView;
			MainControlPanel.style = this.style;
			MainControlPanel.handleFactory = this.handleFactory;
			MainControlPanel.bendFactory = this.bendFactory;
			MainControlPanel.lexicon = cyApplicationManager.getCurrentRenderingEngine().getVisualLexicon();

			MainSingleNodeTask.lexicon = cyApplicationManager.getCurrentRenderingEngine().getVisualLexicon();
			MainSingleNodeTask.style = this.style;

			List<CyNode> nodes = CyTableUtil.getNodesInState(myNetwork, CyNetwork.SELECTED, true);
			if (nodes.size() == 1) {

				CyRow proteinA_node_row = myNetwork.getRow(nodes.get(0));
				Object length_other_protein_a = proteinA_node_row.getRaw(Util.PROTEIN_LENGTH_A);
				Object length_other_protein_b = proteinA_node_row.getRaw(Util.PROTEIN_LENGTH_B);

				if (length_other_protein_a == null) {
					if (length_other_protein_b == null)
						length_other_protein_a = 10;
					else
						length_other_protein_a = length_other_protein_b;
				}

				Util.setProteinLength(((Number) length_other_protein_a).floatValue());

				try {
					if (this.style == null)
						isNodeModified = false;
					else
						isNodeModified = Util.IsNodeModified(myNetwork, netView, nodes.get(0));

				} catch (Exception e2) {
					isNodeModified = false;
				}
			}

			Util.filterUnmodifiedEdges(myNetwork, netView);

		} catch (Exception exception) {
		} finally {

			MainSingleNodeTask.isPlotDone = true;
			LoadProteinDomainTask.isPlotDone = true;
		}
	}

	/**
	 * Method responsible for updating proteinScalingFactor after creating a network
	 */
	@Override
	public void handleEvent(NetworkAddedEvent event) {

		CyNetwork cyNetwork = event.getNetwork();
		if (cyNetwork != null) {
			Util.myCyNetworkList.add(cyNetwork);
		}
	}

	/**
	 * Create auxiliar columns in the tables
	 */
	private void createAuxiliarColumnsTable() {

		// Check if the node exists in the network
		Optional<CyNetwork> isNetworkPresent = Util.myCyNetworkList.stream().filter(new Predicate<CyNetwork>() {
			public boolean test(CyNetwork o) {
				return o.getSUID() == myNetwork.getSUID();
			}
		}).findFirst();

		if (isNetworkPresent.isPresent()) {// Get node if exists
			CyNetwork current_network = isNetworkPresent.get();
			if (this.dialogTaskManager != null && this.proteinScalingFactorHorizontalExpansionTableTaskFactory != null
					&& !ProteinScalingFactorHorizontalExpansionTableTask.isProcessing) {
				TaskIterator ti = this.proteinScalingFactorHorizontalExpansionTableTaskFactory
						.createTaskIterator(current_network, false);
				this.dialogTaskManager.execute(ti);

				Util.myCyNetworkList.remove(current_network);
			}
		}
	}
}
