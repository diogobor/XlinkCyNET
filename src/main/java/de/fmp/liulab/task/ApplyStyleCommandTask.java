package de.fmp.liulab.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;

import de.fmp.liulab.utils.Util;

public class ApplyStyleCommandTask extends CyRESTAbstractTask {

	private CyNetworkView netView;
	private CyNetwork myNetwork;

	@ProvidesTitle
	public String getTitle() {
		return "Apply style to a node";
	}

	@Tunable(description = "Node(s) to set style", longDescription = "Give the node(s) name that will be expanded. (type all to set style to all nodes)", exampleStringValue = "PDE12")
	public String nodesName;

	// public ListSingleSelection<string> targetColumnList;
//
//	@Tunable(description = "Mapping Column for Existing Network:", groups = "Select a Network Collection", listenForChange = {
//			"RootNetworkList" })
//	public ListSingleSelection<string> getTargetColumnList() {
//		return targetColumnList;
//	}
//
//	public void setTargetColumnList(ListSingleSelection<string> colList) {
//		this.targetColumnList = colList;
//	}

	private MainSingleNodeTaskFactory myFactory;
	private DialogTaskManager dialogTaskManager;

//	@Tunable (description="The name of the person you want to greet.")
	public String name = null;

//	private Double value = 2.0;

	public ApplyStyleCommandTask(CyApplicationManager cyApplicationManager, MainSingleNodeTaskFactory myFactory,
			DialogTaskManager dialogTaskManager) {
		this.myFactory = myFactory;
		this.dialogTaskManager = dialogTaskManager;
		this.netView = cyApplicationManager.getCurrentNetworkView();
		this.myNetwork = cyApplicationManager.getCurrentNetwork();
	}

	public void run(TaskMonitor taskMonitor) throws Exception {

		this.selectNodes();

		// Get the task iterator
		TaskIterator ti = myFactory.createTaskIterator(true);

		// Execute the task through the TaskManager
		dialogTaskManager.execute(ti);

//		taskMonitor.showMessage(Level.INFO, "Done!");

	}

	private void selectNodes() {

		List<String> names = Arrays.asList(nodesName.split(","));

		if (names.contains("all")) {
			List<CyNode> allNodes = myNetwork.getNodeList();
			for (CyNode cyNode : allNodes) {
				myNetwork.getRow(cyNode).set(CyNetwork.SELECTED, true);
				View<CyNode> nodeView = netView.getNodeView(cyNode);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_SELECTED, true);
				netView.updateView();
			}
		} else {

			for (String name : names) {

				CyNode node = Util.getNode(myNetwork, name.trim());
				if (node == null)
					return;
				myNetwork.getRow(node).set(CyNetwork.SELECTED, true);
				View<CyNode> nodeView = netView.getNodeView(node);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_SELECTED, true);
				netView.updateView();
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
//		if (type.equals(String.class)) {
//			return (R) (value.toString());
//		} else if (type.equals(Double.class)) {
//			return (R) value;
//		} else {
//			return null;
//		}
		return (R) "Done!";
	}

}
