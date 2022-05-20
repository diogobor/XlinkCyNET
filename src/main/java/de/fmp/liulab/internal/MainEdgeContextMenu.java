package de.fmp.liulab.internal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import de.fmp.liulab.internal.view.ExtensionFileFilter;
import de.fmp.liulab.task.MainSingleEdgeTaskFactory;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for creating main edge context menu
 * 
 * @author diogobor
 *
 */
public class MainEdgeContextMenu implements CyEdgeViewContextMenuFactory, ActionListener {
	private MainSingleEdgeTaskFactory myFactory;
	private DialogTaskManager dialogTaskManager;

	/**
	 * Constructor
	 * 
	 * @param myFactory         graphic instance
	 * @param dialogTaskManager task manager
	 */
	public MainEdgeContextMenu(MainSingleEdgeTaskFactory myFactory, DialogTaskManager dialogTaskManager) {
		this.myFactory = myFactory;
		this.dialogTaskManager = dialogTaskManager;
	}

	/**
	 * Method responsible for creating main MenuItem
	 */
	@Override
	public CyMenuItem createMenuItem(CyNetworkView netView, View<CyEdge> edgeView) {
		JMenuItem menuItem = new JMenuItem("Visualize interactions in PyMOL");
		menuItem.addActionListener(this);

		CyMenuItem cyMenuItem = new CyMenuItem(menuItem, 0);
		return cyMenuItem;
	}

	/**
	 * The task class will be instantiated here.
	 */
	public void actionPerformed(ActionEvent e) {

		TaskIterator ti = null;

		if (Util.useCustomizedPDB) {
			JFileChooser choosePDBFile = null;
			choosePDBFile = new JFileChooser();
			choosePDBFile.setFileFilter(new ExtensionFileFilter("PDB or CIF file (*.pdb|*.cif)", "pdb", "cif"));
			choosePDBFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
			choosePDBFile.setDialogTitle("Import file");

			if (choosePDBFile.showOpenDialog(choosePDBFile) == JFileChooser.APPROVE_OPTION) {

				String pdbFile = choosePDBFile.getSelectedFile().toString();

				// Get the task iterator
				ti = myFactory.createTaskIterator(pdbFile);
			}

		}

		else {
			// Get the task iterator
			ti = myFactory.createTaskIterator();

		}

		if (ti != null)
			// Execute the task through the TaskManager
			dialogTaskManager.execute(ti);
	}

}
