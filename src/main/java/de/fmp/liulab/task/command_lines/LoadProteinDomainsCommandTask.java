package de.fmp.liulab.task.command_lines;

import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import de.fmp.liulab.parser.Parser;
import de.fmp.liulab.task.LoadProteinDomainTask;

public class LoadProteinDomainsCommandTask extends CyRESTAbstractTask {

	private CyNetwork myNetwork;
	private Parser parserFile;

	private String[] columnNames = { "Node Name", "Domain(s)" };
	private final Class[] columnClass = new Class[] { String.class, String.class };

	@ProvidesTitle
	public String getTitle() {
		return "Load protein domains";
	}

	@Tunable(description = "Protein domains fila name", longDescription = "Name of the protein domain file. (Supported formats: *.tab and *.csv)", exampleStringValue = "proteinDomains.csv")
	public String fileName = "none";

	/**
	 * Constructor
	 */
	public LoadProteinDomainsCommandTask(CyApplicationManager cyApplicationManager) {
		this.myNetwork = cyApplicationManager.getCurrentNetwork();
	}

	/**
	 * Run
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {

		this.init_table_data_model_protein_domains();

		// Parse file and update data table model
		if (!fileName.equals("none"))
			this.parserFile(taskMonitor);

	}

	/**
	 * Method responsible for initializing table model of protein domains
	 */
	private void init_table_data_model_protein_domains() {
		Object[][] data = new Object[1][2];
		// create table model with data
		LoadProteinDomainTask.tableDataModel = new DefaultTableModel(data, columnNames) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return true;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnClass[columnIndex];
			}
		};
	}

	private void parserFile(TaskMonitor taskMonitor) throws Exception {

		parserFile = new Parser(fileName);
		// Update data table model
		parserFile.updateDataModel();

		// Store protein domains
		LoadProteinDomainTask.storeProteinDomains(taskMonitor, myNetwork, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return (R) "Done!";
	}

}
