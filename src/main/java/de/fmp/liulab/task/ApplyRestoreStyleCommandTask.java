package de.fmp.liulab.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;

import de.fmp.liulab.utils.Util;

/**
 * Class responsible for applying and restoring style via command line
 * 
 * @author diogobor
 *
 */
public class ApplyRestoreStyleCommandTask extends CyRESTAbstractTask {

	private CyNetwork myNetwork;
	private MainSingleNodeTaskFactory myFactory;
	private DialogTaskManager dialogTaskManager;

	@ProvidesTitle
	public String getTitle() {
		return "Apply/Restore style to a node";
	}

	@Tunable(description = "Node(s) to set/restore style", longDescription = "Give the node(s) name, separated by comma, that will be expanded or restored. (type 'all' to set style to all nodes)", exampleStringValue = "PDE12")
	public String nodesName;

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager main app manager
	 * @param myFactory            main factory
	 * @param dialogTaskManager    task manager
	 */
	public ApplyRestoreStyleCommandTask(CyApplicationManager cyApplicationManager, MainSingleNodeTaskFactory myFactory,
			DialogTaskManager dialogTaskManager) {
		this.myFactory = myFactory;
		this.dialogTaskManager = dialogTaskManager;
		this.myNetwork = cyApplicationManager.getCurrentNetwork();
	}

	/**
	 * Run
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {

		// Select nodes based on nodesName variable
		this.selectNodes();

		// Get the task iterator
		TaskIterator ti = myFactory.createTaskIterator(true);

		// Execute the task through the TaskManager
		dialogTaskManager.execute(ti);

	}

	/**
	 * Method responsible for selecting nodes based on 'nodesName' variable
	 */
	private void selectNodes() {

		List<String> names = Arrays.asList(nodesName.split(","));

		if (names.contains("all")) {
			List<CyNode> allNodes = myNetwork.getNodeList();
			for (CyNode cyNode : allNodes) {
				myNetwork.getRow(cyNode).set(CyNetwork.SELECTED, true);
			}
		} else {

			for (String name : names) {

				CyNode node = Util.getNode(myNetwork, name.trim());
				if (node == null)
					continue;
				myNetwork.getRow(node).set(CyNetwork.SELECTED, true);
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return (R) "Done!";
	}

}
