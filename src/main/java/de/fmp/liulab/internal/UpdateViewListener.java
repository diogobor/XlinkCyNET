package de.fmp.liulab.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import de.fmp.liulab.task.LoadProteinDomainTask;
import de.fmp.liulab.task.MainSingleNodeTask;
import de.fmp.liulab.task.ProteinScalingFactorHorizontalExpansionTableTaskFactory;
import de.fmp.liulab.task.UpdateViewerTaskFactory;
import de.fmp.liulab.utils.Tuple2;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for updating layout
 * 
 * @author borges.diogo
 *
 */
public class UpdateViewListener implements ViewChangedListener, RowsSetListener, SetCurrentNetworkListener {

	private CyApplicationManager cyApplicationManager;
	private CyNetwork myNetwork;
	private static CyNetworkView netView;
	private HandleFactory handleFactory;
	private BendFactory bendFactory;
	private VisualStyle style;

	private DialogTaskManager dialogTaskManager;
	private ProteinScalingFactorHorizontalExpansionTableTaskFactory proteinScalingFactorHorizontalExpansionTableTaskFactory;
	private UpdateViewerTaskFactory updateViewerTaskFactory;

	private CyNode selectedNode;

	private Map<CyNode, Tuple2<Double, Double>> mapLastNodesPosition = new HashMap<CyNode, Tuple2<Double, Double>>();

	public static boolean isNodeModified = false;

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
	 */
	public UpdateViewListener(CyApplicationManager cyApplicationManager, HandleFactory handleFactory,
			BendFactory bendFactory, VisualMappingManager vmmServiceRef, DialogTaskManager dialogTaskManager,
			ProteinScalingFactorHorizontalExpansionTableTaskFactory proteinScalingFactorHorizontalExpansionTableTaskFactory,
			UpdateViewerTaskFactory updateViewerTaskFactory) {
		this.cyApplicationManager = cyApplicationManager;
		this.handleFactory = handleFactory;
		this.bendFactory = bendFactory;
		this.style = vmmServiceRef.getCurrentVisualStyle();
		this.dialogTaskManager = dialogTaskManager;
		this.proteinScalingFactorHorizontalExpansionTableTaskFactory = proteinScalingFactorHorizontalExpansionTableTaskFactory;
		this.updateViewerTaskFactory = updateViewerTaskFactory;
	}

	/**
	 * Method responsible for capturing the selected node.
	 */
	@Override
	public void handleEvent(RowsSetEvent e) {

		myNetwork = cyApplicationManager.getCurrentNetwork();
		netView = cyApplicationManager.getCurrentNetworkView();

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

						updateNodesAndEdges(this.selectedNode);
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
			netView = cyApplicationManager.getCurrentNetworkView();

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

			// Check if all selected nodes have been modified
			for (final CyNode _node : nodes) {
				// Check if the node exists in the network
				Optional<CyNode> isNodePresent = nodeSuidList.stream().filter(new Predicate<CyNode>() {
					public boolean test(CyNode o) {
						return o.getSUID() == _node.getSUID();
					}
				}).findFirst();
				if (!isNodePresent.isPresent()) {
					return;
				}
			}

			// Iterating over hash set items
			Iterator<CyNode> _iterator_CyNode = nodes.iterator();
			while (_iterator_CyNode.hasNext()) {

				updateNodesAndEdges(_iterator_CyNode.next());

			}
		} catch (Exception exception) {
		}
	}

	/**
	 * Update nodes and edges
	 * 
	 * @param current_node current node
	 */
	private void updateNodesAndEdges(CyNode current_node) {

		View<CyNode> nodeView = netView.getNodeView(current_node);
		double current_posX = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		double current_posY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		if (mapLastNodesPosition.containsKey(current_node)) {
			double last_posX = mapLastNodesPosition.get(current_node).getFirst();
			double last_posY = mapLastNodesPosition.get(current_node).getSecond();
			if (current_posX == last_posX && current_posY == last_posY)
				return;
		}
		
		// Create protein scaling factor table
		if (this.dialogTaskManager != null && this.updateViewerTaskFactory != null) {
			TaskIterator ti = this.updateViewerTaskFactory.createTaskIterator(cyApplicationManager, handleFactory,
					bendFactory, myNetwork, netView, current_node, mapLastNodesPosition);
			this.dialogTaskManager.execute(ti);
		}
	}

	/**
	 * Method responsible for update all nodes according to the current selected
	 * network
	 */
	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {

		MainSingleNodeTask.interLinks = null;
		MainSingleNodeTask.intraLinks = null;
		mapLastNodesPosition.clear();

		try {

			// Update variables
			if (cyApplicationManager == null)
				return;
			myNetwork = cyApplicationManager.getCurrentNetwork();
			netView = cyApplicationManager.getCurrentNetworkView();
			if (netView == null)
				return;
			if (cyApplicationManager.getCurrentRenderingEngine() == null)
				return;

			// Create protein scaling factor table
			if (this.dialogTaskManager != null
					&& this.proteinScalingFactorHorizontalExpansionTableTaskFactory != null) {
				TaskIterator ti = this.proteinScalingFactorHorizontalExpansionTableTaskFactory
						.createTaskIterator(myNetwork);
				this.dialogTaskManager.execute(ti);
			}

			MainSingleNodeTask.lexicon = cyApplicationManager.getCurrentRenderingEngine().getVisualLexicon();
			MainSingleNodeTask.style = this.style;

			List<CyNode> nodes = CyTableUtil.getNodesInState(myNetwork, CyNetwork.SELECTED, true);
			if (nodes.size() == 1) {

				CyRow proteinA_node_row = myNetwork.getRow(nodes.get(0));
				Object length_other_protein_a = proteinA_node_row.getRaw("length_protein_a");
				Object length_other_protein_b = proteinA_node_row.getRaw("length_protein_b");

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
		} catch (Exception exception) {
		} finally {

			MainSingleNodeTask.isPlotDone = true;
			LoadProteinDomainTask.isPlotDone = true;
		}
	}
}
