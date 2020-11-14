package de.fmp.liulab.task;

import java.util.ArrayList;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.utils.Tuple2;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for updating edges
 * 
 * @author diogobor
 *
 */
public class UpdateViewerTask extends AbstractTask {

	private CyApplicationManager cyApplicationManager;

	private HandleFactory handleFactory;
	private BendFactory bendFactory;

	private CyNetwork myNetwork;
	private CyNetworkView netView;

	private CyNode node;

	private boolean IsIntraLink = false;

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager main app manager
	 * @param handleFactory        handle factory
	 * @param bendFactory          bend factory
	 * @param myNetwork            current network
	 * @param netView              current network view
	 * @param node                 current node
	 * @param mapLastNodesPosition map of nodes position
	 */
	public UpdateViewerTask(CyApplicationManager cyApplicationManager, HandleFactory handleFactory,
			BendFactory bendFactory, CyNetwork myNetwork, CyNetworkView netView, CyNode node) {

		this.cyApplicationManager = cyApplicationManager;

		this.handleFactory = handleFactory;
		this.bendFactory = bendFactory;

		this.myNetwork = myNetwork;
		this.netView = netView;
		this.node = node;

	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		updateNodesAndEdges(this.node, taskMonitor);
	}

	/**
	 * Method responsible for updating Nodes and Edges
	 * 
	 * @param current_node current node
	 * @param taskMonitor  task monitor
	 */
	private void updateNodesAndEdges(final CyNode current_node, TaskMonitor taskMonitor) {

		MainSingleNodeTask.isPlotDone = false;

		double current_scaling_factor = myNetwork.getRow(current_node).get(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME,
				Double.class);
		View<CyNode> nodeView = netView.getNodeView(current_node);

		if (current_scaling_factor != 1)
			Util.updateMapNodesPosition(current_node, nodeView);

		CyRow proteinA_node_row = myNetwork.getRow(current_node);
		Object length_other_protein_a = proteinA_node_row.getRaw("length_protein_a");
		Object length_other_protein_b = proteinA_node_row.getRaw("length_protein_b");

		if (length_other_protein_a == null) {
			if (length_other_protein_b == null)
				length_other_protein_a = 10;
			else
				length_other_protein_a = length_other_protein_b;
		}

		Util.setProteinLength((float) ((Number) length_other_protein_a).doubleValue());

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

		Util.stopUpdateViewer = false;
		Tuple2 inter_and_intralinks = Util.getAllLinksFromAdjacentEdgesNode(current_node, myNetwork);
		MainSingleNodeTask.interLinks = (ArrayList<CrossLink>) inter_and_intralinks.getFirst();
		MainSingleNodeTask.intraLinks = (ArrayList<CrossLink>) inter_and_intralinks.getSecond();

		if (MainSingleNodeTask.interLinks.size() > 0) { // The selectedNode has interlinks
			IsIntraLink = false;
		} else {
			IsIntraLink = true;
		}

		if (Util.IsNodeModified(myNetwork, netView, current_node)) {

			Util.node_label_factor_size = 1.0;
			Util.setNodeStyles(myNetwork, current_node, netView);

			MainSingleNodeTask.isPlotDone = Util.addOrUpdateEdgesToNetwork(myNetwork, current_node, style, netView,
					nodeView, handleFactory, bendFactory, lexicon, ((Number) length_other_protein_a).floatValue(),
					MainSingleNodeTask.intraLinks, MainSingleNodeTask.interLinks, taskMonitor, null);

			Util.node_label_factor_size = current_scaling_factor;

			if (Util.node_label_factor_size != 1) {
				MainSingleNodeTask.isPlotDone = false;
				this.resizeProtein(current_node, nodeView, lexicon, length_other_protein_a, style);
			}

		} else if (!IsIntraLink) {
			Util.updateAllAssiciatedInterlinkNodes(myNetwork, cyApplicationManager, netView, handleFactory, bendFactory,
					current_node);// Check if all associated nodes are
									// unmodified
			MainSingleNodeTask.isPlotDone = true;
		}
	}

	/**
	 * Resize protein node
	 * 
	 * @param current_node           current node
	 * @param nodeView               current node view
	 * @param lexicon                lexicon
	 * @param length_other_protein_a length of the current protein
	 */
	private void resizeProtein(CyNode current_node, View<CyNode> nodeView, VisualLexicon lexicon,
			Object length_other_protein_a, VisualStyle style) {
		Util.setNodeStyles(myNetwork, current_node, netView);

		MainSingleNodeTask.isPlotDone = Util.addOrUpdateEdgesToNetwork(myNetwork, current_node, style, netView,
				nodeView, handleFactory, bendFactory, lexicon, ((Number) length_other_protein_a).floatValue(),
				MainSingleNodeTask.intraLinks, MainSingleNodeTask.interLinks, null, null);
	}
}