package de.fmp.liulab.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyEdge;
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
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.task.LoadProteinDomainTask;
import de.fmp.liulab.task.MainSingleNodeTask;
import de.fmp.liulab.utils.Tuple2;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for updating layout
 * 
 * @author borges.diogo
 *
 */
public class UpdateViewListener implements ViewChangedListener, RowsSetListener, SetCurrentNetworkListener {

	private static CyApplicationManager cyApplicationManager;
	private static CyNetwork myNetwork;
	private static CyNetworkView netView;
	private static HandleFactory handleFactory;
	private static BendFactory bendFactory;
	private VisualStyle style;

	private CyNode selectedNode;
	private boolean IsIntraLink = false;

	public static boolean isNodeModified = false;

	/**
	 * Constructor
	 * 
	 * @param myNetwork
	 */
	public UpdateViewListener(CyApplicationManager cyApplicationManager, HandleFactory handleFactory,
			BendFactory bendFactory, VisualMappingManager vmmServiceRef) {
		this.cyApplicationManager = cyApplicationManager;
		this.handleFactory = handleFactory;
		this.bendFactory = bendFactory;
		this.style = vmmServiceRef.getCurrentVisualStyle();
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
				Object length_other_protein_a;
				Object length_other_protein_b;

				this.selectedNode = null;
				for (RowSetRecord record : e.getColumnRecords(CyNetwork.SELECTED)) {
					Long suid = record.getRow().get(CyIdentifiable.SUID, Long.class);
					Boolean value = (Boolean) record.getValue();
					if (value.equals(Boolean.TRUE)) {
						this.selectedNode = myNetwork.getNode(suid);
						MainSingleNodeTask.node = this.selectedNode;
						CyRow node_row = myNetwork.getRow(this.selectedNode);

						length_other_protein_a = node_row.getRaw("length_protein_a");
						length_other_protein_b = node_row.getRaw("length_protein_b");

						if (length_other_protein_a == null) {
							if (length_other_protein_b == null)
								length_other_protein_a = 10;
							else
								length_other_protein_a = length_other_protein_b;
						}
						MainSingleNodeTask.proteinLength = ((Number) length_other_protein_a).floatValue();
						Tuple2 inter_and_intralinks = Util.getAllLinksFromAdjacentEdgesNode(this.selectedNode, myNetwork);
						MainSingleNodeTask.interLinks = (ArrayList<CrossLink>) inter_and_intralinks.getFirst();
						MainSingleNodeTask.intraLinks = (ArrayList<CrossLink>) inter_and_intralinks.getSecond();

						if (MainSingleNodeTask.interLinks.size() > 0) { // The selectedNode has interlinks
							IsIntraLink = false;
						} else {
							IsIntraLink = true;
						}

						if (Util.IsNodeModified(myNetwork, netView, style, this.selectedNode)) {
							VisualStyle style = MainSingleNodeTask.style;
							if (style == null)
								style = LoadProteinDomainTask.style;
							VisualLexicon lexicon = MainSingleNodeTask.lexicon;
							if (lexicon == null)
								lexicon = LoadProteinDomainTask.lexicon;

							View<CyNode> nodeView = netView.getNodeView(this.selectedNode);
							Util.addOrUpdateEdgesToNetwork(myNetwork, this.selectedNode, style, netView, nodeView,
									handleFactory, bendFactory, lexicon, ((Number) length_other_protein_a).floatValue(),
									MainSingleNodeTask.intraLinks, MainSingleNodeTask.interLinks, null);
						} else if (!IsIntraLink) {
							Util.updateAllAssiciatedInterlinkNodes(myNetwork, cyApplicationManager, netView,
									handleFactory, bendFactory, this.selectedNode);// Check if all associated nodes are
																					// unmodified

						}
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

			Set<CyNode> nodeSuidList = new HashSet<CyNode>();

			for (ViewChangeRecord<?> record : e.getPayloadCollection()) {
				View<?> record_view = record.getView();
				if (record_view.getModel() instanceof CyNode) {
					nodeSuidList.add(((CyNode) record_view.getModel()));
				}
			}

			if (nodeSuidList.size() == 1) {// Only one node has been selected

				CyNode current_node = nodeSuidList.iterator().next();

				myNetwork = cyApplicationManager.getCurrentNetwork();
				netView = cyApplicationManager.getCurrentNetworkView();

				List<CyNode> nodes = CyTableUtil.getNodesInState(myNetwork, CyNetwork.SELECTED, true);
				if (nodes.size() == 1) {

					if (current_node.getSUID() == nodes.get(0).getSUID()) {

						if (this.selectedNode != null) {// If selectedNode is null means that the node is just
														// plotted.
							Tuple2 inter_and_intralinks = Util.getAllLinksFromAdjacentEdgesNode(this.selectedNode, myNetwork);
							MainSingleNodeTask.interLinks = (ArrayList<CrossLink>) inter_and_intralinks.getFirst();
							MainSingleNodeTask.intraLinks = (ArrayList<CrossLink>) inter_and_intralinks.getSecond();
						}
						View<CyNode> nodeView = netView.getNodeView(current_node);

						CyRow proteinA_node_row = myNetwork.getRow(current_node);
						Object length_other_protein_a = proteinA_node_row.getRaw("length_protein_a");
						Object length_other_protein_b = proteinA_node_row.getRaw("length_protein_b");

						if (length_other_protein_a == null) {
							if (length_other_protein_b == null)
								length_other_protein_a = 10;
							else
								length_other_protein_a = length_other_protein_b;
						}

						VisualStyle style = MainSingleNodeTask.style;
						if (style == null)
							style = LoadProteinDomainTask.style;
						if (style == null)
							return;

						VisualLexicon lexicon = MainSingleNodeTask.lexicon;
						if (lexicon == null)
							lexicon = LoadProteinDomainTask.lexicon;
						if (lexicon == null)
							return;

						if (MainSingleNodeTask.interLinks.size() > 0) { // The selectedNode has interlinks
							IsIntraLink = false;
						} else {
							IsIntraLink = true;
						}

						if (Util.IsNodeModified(myNetwork, netView, style, current_node)) {
							Util.addOrUpdateEdgesToNetwork(myNetwork, current_node, style, netView, nodeView,
									handleFactory, bendFactory, lexicon, ((Number) length_other_protein_a).floatValue(),
									MainSingleNodeTask.intraLinks, MainSingleNodeTask.interLinks, null);
						} else if (!IsIntraLink) {
							Util.updateAllAssiciatedInterlinkNodes(myNetwork, cyApplicationManager, netView,
									handleFactory, bendFactory, current_node);// Check if all associated nodes are
																				// unmodified
						}
					}
				}
			}
		} catch (Exception exception) {
		}
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {

		MainSingleNodeTask.interLinks = null;
		MainSingleNodeTask.intraLinks = null;

		// Update variables
		if (cyApplicationManager == null)
			return;
		myNetwork = cyApplicationManager.getCurrentNetwork();
		netView = cyApplicationManager.getCurrentNetworkView();
		if (netView == null)
			return;
		if (cyApplicationManager.getCurrentRenderingEngine() == null)
			return;
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

			MainSingleNodeTask.proteinLength = ((Number) length_other_protein_a).floatValue();

			try {
				if (this.style == null)
					isNodeModified = false;
				else
					isNodeModified = Util.IsNodeModified(myNetwork, netView, style, nodes.get(0));

			} catch (Exception e2) {
				isNodeModified = false;
			}
		}

		MainSingleNodeTask.isPlotDone = true;
		LoadProteinDomainTask.isPlotDone = true;
	}
}
