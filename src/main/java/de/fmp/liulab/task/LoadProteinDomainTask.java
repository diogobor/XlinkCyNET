package de.fmp.liulab.task;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.internal.UpdateViewListener;
import de.fmp.liulab.internal.view.MenuBar;
import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.model.GeneDomain;
import de.fmp.liulab.model.ProteinDomain;
import de.fmp.liulab.utils.Tuple2;
import de.fmp.liulab.utils.Util;

public class LoadProteinDomainTask extends AbstractTask implements ActionListener {

	private static String OS = System.getProperty("os.name").toLowerCase();

	private CyApplicationManager cyApplicationManager;
	private CyNetwork myNetwork;
	private CyNetworkView netView;
	private CyCustomGraphics2Factory vgFactory;

	public static VisualLexicon lexicon;
	public static VisualStyle style;

	// Window
	private JFrame mainFrame;
	private JPanel mainPanel;
	private JLabel textLabel_status_result;
	private MenuBar menuBar = new MenuBar();

	// Table
	private static JTable mainProteinDomainTable;
	public static DefaultTableModel tableDataModel;
	private String[] columnNames = { "Node Name", "Domain(s)" };
	private final Class[] columnClass = new Class[] { String.class, String.class };
	private List<GeneDomain> geneListFromTable;
	private String rowstring, value;
	private Clipboard clipboard;
	private StringSelection stsel;

	private boolean isPfamLoaded = true;
	private boolean pfamDoStop = false;
	private Thread pfamThread;
	private JButton pFamButton;

	private List<java.awt.Color> available_colors;

	public static boolean isPlotDone = false;

	public LoadProteinDomainTask(CyApplicationManager cyApplicationManager, final VisualMappingManager vmmServiceRef,
			CyCustomGraphics2Factory vgFactory) {
		this.cyApplicationManager = cyApplicationManager;
		this.netView = cyApplicationManager.getCurrentNetworkView();
		this.myNetwork = cyApplicationManager.getCurrentNetwork();
		this.vgFactory = vgFactory;
		this.style = vmmServiceRef.getCurrentVisualStyle();
		// Get the current Visual Lexicon
		this.lexicon = cyApplicationManager.getCurrentRenderingEngine().getVisualLexicon();

		available_colors = new ArrayList<Color>();
		available_colors.add(new Color(0, 64, 128, 100));
		available_colors.add(new Color(0, 128, 64, 100));
		available_colors.add(new Color(255, 128, 0, 100));
		available_colors.add(new Color(128, 128, 0, 100));
		available_colors.add(new Color(128, 128, 128, 100));
		available_colors.add(new Color(128, 64, 64, 100));
		available_colors.add(new Color(0, 128, 192, 100));
		available_colors.add(new Color(174, 0, 0, 100));
		available_colors.add(new Color(255, 255, 0, 100));
		available_colors.add(new Color(0, 64, 0, 100));
		available_colors.add(new Color(204, 0, 0, 100));

		if (mainFrame == null)
			mainFrame = new JFrame("XlinkCyNET - Load protein domains");

		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension appSize = null;
		if (this.isWindows() || this.isUnix()) {
			appSize = new Dimension(540, 345);
		} else {
			appSize = new Dimension(540, 315);
		}
		mainFrame.setSize(appSize);
		mainFrame.setResizable(false);

		if (mainPanel == null)
			mainPanel = new JPanel();
		mainPanel.setBounds(10, 10, 490, 335);
		mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""));
		mainPanel.setLayout(null);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setLocation((screenSize.width - appSize.width) / 2, (screenSize.height - appSize.height) / 2);
		mainFrame.setJMenuBar(menuBar.getMenuBar());
		mainFrame.setVisible(true);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setTitle("XlinkCyNET - Set of nodes task");

		if (cyApplicationManager.getCurrentNetwork() == null) {
			throw new Exception("ERROR: No networks has been loaded.");
		}

		setFrameObjects(taskMonitor);

		// Display the window
		mainFrame.add(mainPanel, BorderLayout.CENTER);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}

	private Image getScaledImage(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

	/**
	 * Set all labels in XLinkCyNET window / frame
	 */
	private void initFrameLabels() {

		JLabel textLabel_Protein_lbl_1 = new JLabel(
				"Fill in the table below to indicate what proteins will have their");
		textLabel_Protein_lbl_1.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_lbl_1.setBounds(10, -20, 450, 100);
		mainPanel.add(textLabel_Protein_lbl_1);

		JLabel textLabel_Protein_lbl_2 = new JLabel("domains loaded.");
		textLabel_Protein_lbl_2.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_lbl_2.setBounds(10, 0, 300, 100);
		mainPanel.add(textLabel_Protein_lbl_2);

		JLabel textLabel_Pfam = new JLabel("Search for domains in Pfam database:");
		textLabel_Pfam.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Pfam.setBounds(10, 30, 300, 100);
		mainPanel.add(textLabel_Pfam);

		textLabel_status_result = new JLabel("");
		textLabel_status_result.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status_result.setForeground(new Color(159, 17, 17));
		textLabel_status_result.setBounds(55, 60, 350, 100);

		ImageIcon imgIcon = new ImageIcon(getClass().getResource("/images/logo.png"));
		imgIcon.setImage(getScaledImage(imgIcon.getImage(), 80, 68));
		JLabel jLabelIcon = new JLabel();
		jLabelIcon.setBounds(400, 0, 100, 100);
		jLabelIcon.setIcon(imgIcon);
		mainPanel.add(jLabelIcon);

		JLabel textLabel_status = new JLabel("Status:");
		textLabel_status.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status.setBounds(10, 60, 50, 100);
		mainPanel.add(textLabel_status);
		mainPanel.add(textLabel_status_result);
	}

	/**
	 * Method responsible for initializing the table in the Frame
	 */
	private void initTableScreen() {

		Object[][] data = new Object[1][2];
		// create table model with data
		tableDataModel = new DefaultTableModel(data, columnNames) {
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

		mainProteinDomainTable = new JTable(tableDataModel);
		setTableProperties();

		Action insertLineToTableAction = new AbstractAction("insertLineToTable") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				tableDataModel.addRow(new Object[] { "" });
				textLabel_status_result.setText("Row has been inserted.");
			}
		};

		KeyStroke keyStrokeInsertLine = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);
		mainProteinDomainTable.getActionMap().put("insertLineToTable", insertLineToTableAction);
		mainProteinDomainTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStrokeInsertLine,
				"insertLineToTable");

		Action deleteLineToTableAction = new AbstractAction("deleteLineToTable") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {

				if (mainProteinDomainTable.getSelectedRow() != -1) {

					int input = JOptionPane.showConfirmDialog(null, "Do you confirm the removal of the line?");
					// 0=yes, 1=no, 2=cancel
					if (input == 0) {
						// remove selected row from the model
						tableDataModel.removeRow(mainProteinDomainTable.getSelectedRow());
					}
				}

				textLabel_status_result.setText("Row has been deleted.");
			}
		};

		KeyStroke keyStrokeDeleteLine = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
		mainProteinDomainTable.getActionMap().put("deleteLineToTable", deleteLineToTableAction);
		mainProteinDomainTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStrokeDeleteLine,
				"deleteLineToTable");

		final KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, false);
		// Identifying the copy KeyStroke user can modify this
		// to copy on some other Key combination.
		final KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, false);
		// Identifying the Paste KeyStroke user can modify this
		// to copy on some other Key combination.
		mainProteinDomainTable.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
		mainProteinDomainTable.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		// Create the scroll pane and add the table to it.
		JScrollPane proteinDomainTableScrollPanel = new JScrollPane();
		proteinDomainTableScrollPanel.setBounds(10, 130, 500, 105);
		proteinDomainTableScrollPanel.setViewportView(mainProteinDomainTable);
		mainPanel.add(proteinDomainTableScrollPanel);
	}

	/**
	 * Method responsible for getting protein domains from Pfam database for each
	 * node
	 * 
	 * @param taskMonitor
	 */
	private void getPfamProteinDomainsForeachNode(final TaskMonitor taskMonitor) {

		int old_progress = 0;
		int summary_processed = 0;
		int total_genes = geneListFromTable.size();

		CyRow myCurrentRow = null;
		for (final GeneDomain geneDomain : geneListFromTable) {

			if (pfamDoStop)
				break;

			CyNode current_node = Util.getNode(myNetwork, geneDomain.getGeneName);

			if (current_node != null) {
				myCurrentRow = myNetwork.getRow(current_node);

				List<ProteinDomain> new_proteinDomains = Util.getProteinDomains(myCurrentRow);
				if (new_proteinDomains.size() > 0) {
					geneDomain.proteinDomains = new_proteinDomains;
				}
			}
			summary_processed++;
			int new_progress = (int) ((double) summary_processed / (total_genes) * 100);
			if (new_progress > old_progress) {
				old_progress = new_progress;

				textLabel_status_result.setText("Getting protein domains: " + old_progress + "%");
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Accessing Pfam database: " + old_progress + "%");
			}
		}
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done!");
		textLabel_status_result.setText("Done!");

		// Enable Pfam button and menu
		pFamButton.setEnabled(true);
		menuBar.setEnabled(true);
	}

	/**
	 * Method responsible for initializing all button in the Frame
	 * 
	 * @param taskMonitor
	 */
	private void initButtons(final TaskMonitor taskMonitor) {

		Icon iconBtn = new ImageIcon(getClass().getResource("/images/browse_Icon.png"));
		pFamButton = new JButton(iconBtn);
		pFamButton.setBounds(220, 65, 30, 30);
		pFamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				taskMonitor.setTitle("XL interactions");
				pFamButton.setEnabled(false);
				menuBar.setEnabled(false);
				try {

					isPlotDone = false;
					textLabel_status_result.setText("Getting protein domains...");
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein domains...");
					String msgError = getNodesFromTable();
					if (!msgError.isBlank() && !msgError.isEmpty()) {
						taskMonitor.showMessage(TaskMonitor.Level.ERROR, msgError);
					} else {

						textLabel_status_result.setText("Accessing Pfam database...");
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "Accessing Pfam database...");
						pfamThread = new Thread() {
							public void run() {
								pfamDoStop = false;
								isPfamLoaded = false;
								getPfamProteinDomainsForeachNode(taskMonitor);

								Object[][] data = new Object[geneListFromTable.size()][2];
								tableDataModel.setDataVector(data, columnNames);
								int countPtnDomain = 0;
								for (final GeneDomain geneDomain : geneListFromTable) {

									tableDataModel.setValueAt(geneDomain.getGeneName, countPtnDomain, 0);
									tableDataModel.setValueAt(ToStringProteinDomains(geneDomain.proteinDomains),
											countPtnDomain, 1);
									countPtnDomain++;
								}
								setTableProperties();
								isPfamLoaded = true;
							}
						};

						pfamThread.start();

					}
				} catch (Exception exception) {
				}
			}
		});
		mainPanel.add(pFamButton);

		Icon iconBtnOk = new ImageIcon(getClass().getResource("/images/okBtn.png"));
		JButton okButton = new JButton(iconBtnOk);
		okButton.setText("OK");
		okButton.setBounds(30, 250, 220, 25);

		okButton.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				boolean concluedProcess = true;
				String msgError = "";
				try {

					if (!isPfamLoaded) {
						int input = JOptionPane.showConfirmDialog(null,
								"Pfam process has not been finished yet. Do you want to close this window?",
								"XlinkCyNET - Protein domains", JOptionPane.INFORMATION_MESSAGE);
						// 0=yes, 1=no, 2=cancel
						if (input == 0) {
							concluedProcess = true;
							if (pfamThread != null) {
								pfamDoStop = true;
								pfamThread.interrupt();
							}
						} else {
							concluedProcess = false;
							pfamDoStop = false;
						}
					}

					if (concluedProcess) {
						isPlotDone = false;
						textLabel_status_result.setText("Detecting nodes filled out on the table...");
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "Detecting nodes filled out on the table...");
						msgError = getNodesFromTable();
						if (!msgError.isBlank() && !msgError.isEmpty()) {
							taskMonitor.showMessage(TaskMonitor.Level.ERROR, msgError);
						} else {
							textLabel_status_result.setText("Setting nodes information...");
							taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting nodes information...");
							setNodesInformation(taskMonitor);

							taskMonitor.setProgress(1.0);
							taskMonitor.showMessage(TaskMonitor.Level.INFO,
									"Protein domains have been loaded successfully!");

							isPlotDone = true;
							UpdateViewListener.isNodeModified = true;
						}
					}

				} catch (Exception e1) {
					textLabel_status_result.setText("ERROR: Check Task History.");
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, "ERROR: " + e1.getMessage());
					msgError += e1.getMessage();
				}

				if (concluedProcess) {
					if (msgError.isBlank() && msgError.isEmpty()) {
						mainFrame.dispose();
						JOptionPane.showMessageDialog(null, "Protein domains have been loaded successfully!",
								"XlinkCyNET - Protein domains", JOptionPane.INFORMATION_MESSAGE);
					} else {
						textLabel_status_result.setText("ERROR: Check Task History.");
					}
				}
			}
		});
		mainPanel.add(okButton);

		Icon iconBtnCancel = new ImageIcon(getClass().getResource("/images/cancelBtn.png"));
		JButton cancelButton = new JButton(iconBtnCancel);
		cancelButton.setText("Cancel");
		cancelButton.setBounds(265, 250, 220, 25);

		cancelButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				boolean concluedProcess = true;

				if (!isPfamLoaded) {
					int input = JOptionPane.showConfirmDialog(null,
							"Pfam process has not been finished yet. Do you want to close this window?",
							"XlinkCyNET - Protein domains", JOptionPane.INFORMATION_MESSAGE);
					// 0=yes, 1=no, 2=cancel
					if (input == 0) {
						concluedProcess = true;
						if (pfamThread != null) {
							pfamDoStop = true;
							pfamThread.interrupt();
						}
					} else {
						concluedProcess = false;
						pfamDoStop = false;
					}
				}
				if (concluedProcess) {
					mainFrame.dispose();
				}
			}
		});
		mainPanel.add(cancelButton);
	}

	private String ToStringProteinDomains(List<ProteinDomain> proteinDomains) {

		if (proteinDomains == null || proteinDomains.size() == 0) {
			return "";
		}

		StringBuilder sb_proteinDomains = new StringBuilder();
		for (ProteinDomain ptn_domain : proteinDomains) {
			sb_proteinDomains.append(ptn_domain.name + "[" + ptn_domain.startId + "-" + ptn_domain.endId + "],");
		}
		return sb_proteinDomains.toString().substring(0, sb_proteinDomains.toString().length() - 1);
	}

	/**
	 * Set all objects to main Frame
	 * 
	 * @param taskMonitor
	 */
	private void setFrameObjects(final TaskMonitor taskMonitor) {

		initFrameLabels();

		initTableScreen();

		initButtons(taskMonitor);
	}

	/**
	 * Method responsible for getting all information of all nodes
	 * 
	 * @throws Exception
	 */
	private void setNodesInformation(final TaskMonitor taskMonitor) throws Exception {
		for (final GeneDomain geneDomain : geneListFromTable) {

			CyNode currentNode = null;

			// Check if the node exists in the network
			Optional<CyRow> isNodePresent = myNetwork.getDefaultNodeTable().getAllRows().stream()
					.filter(new Predicate<CyRow>() {
						public boolean test(CyRow o) {
							return o.get(CyNetwork.NAME, String.class).equals(geneDomain.getGeneName);
						}
					}).findFirst();

			if (isNodePresent.isPresent()) {// Get node if exists
				CyRow _node_row = isNodePresent.get();
				currentNode = myNetwork.getNode(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));

				if (geneDomain.proteinDomains.size() > 0) {
					textLabel_status_result.setText("Updating information of " + geneDomain.getGeneName);
					taskMonitor.showMessage(TaskMonitor.Level.INFO,
							"Updating information of " + geneDomain.getGeneName);
					this.updateProteinDomainsMap(currentNode, geneDomain.proteinDomains);

				}
			}
		}

	}

	/**
	 * Method responsible for update Protein domains map
	 * 
	 * @param node
	 * @param myProteinDomains
	 */
	private void updateProteinDomainsMap(CyNode node, List<ProteinDomain> myProteinDomains) {
		String network_name = myNetwork.toString();
		if (Util.proteinDomainsMap.containsKey(network_name)) {

			Map<Long, List<ProteinDomain>> all_proteinDomains = Util.proteinDomainsMap.get(network_name);
			all_proteinDomains.put(node.getSUID(), myProteinDomains);

		} else {// Network does not exists

			Map<Long, List<ProteinDomain>> proteinDomains = new HashMap<Long, List<ProteinDomain>>();
			proteinDomains.put(node.getSUID(), myProteinDomains);
			Util.proteinDomainsMap.put(network_name, proteinDomains);
		}
	}

	/**
	 * Get all nodes filled out in JTable
	 */
	private String getNodesFromTable() {

		geneListFromTable = new ArrayList<GeneDomain>();
		StringBuilder sbError = new StringBuilder();

		for (int row = 0; row < tableDataModel.getRowCount(); row++) {
			String gene = tableDataModel.getValueAt(row, 0) != null ? tableDataModel.getValueAt(row, 0).toString() : "";

			List<ProteinDomain> proteinDomains = new ArrayList<ProteinDomain>();
			String domainsStr = tableDataModel.getValueAt(row, 1) != null ? tableDataModel.getValueAt(row, 1).toString()
					: "";
			if (!domainsStr.isBlank() && !domainsStr.isEmpty()) {

				try {
					String[] cols = domainsStr.split(",");
					for (String col : cols) {
						String[] domainsArray = col.split("\\[|\\]");
						String domainName = domainsArray[0];
						String[] colRange = domainsArray[1].split("-");
						int startId = Integer.parseInt(colRange[0]);
						int endId = Integer.parseInt(colRange[1]);
						proteinDomains.add(new ProteinDomain(domainName, startId, endId, ""));
					}
				} catch (Exception e) {
					sbError.append("ERROR: Row: " + row
							+ " - Protein domains don't match with the pattern 'name[start_index-end_index]'\n");
				}
			}
			if (gene.isEmpty() || gene.isBlank()) {
				sbError.append("ERROR: Row: " + (row + 1) + " - Gene is empty.");
			} else {
				geneListFromTable.add(new GeneDomain(gene, proteinDomains));
			}
		}
		if (geneListFromTable.size() == 0)
			return "";
		else
			return sbError.toString();
	}

	/**
	 * Set properties to the Node domain table
	 */
	public static void setTableProperties() {
		if (mainProteinDomainTable != null) {
			mainProteinDomainTable.setPreferredScrollableViewportSize(new Dimension(490, 90));
			mainProteinDomainTable.getColumnModel().getColumn(0).setPreferredWidth(50);
			mainProteinDomainTable.getColumnModel().getColumn(1).setPreferredWidth(250);
			mainProteinDomainTable.setFillsViewportHeight(true);
			mainProteinDomainTable.setAutoCreateRowSorter(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		final String actionCommand = e.getActionCommand();

		if (actionCommand.equals("Copy")) {
			StringBuilder sbf = new StringBuilder();
			// Check to ensure we have selected only a contiguous block of cells.
			final int numcols = mainProteinDomainTable.getSelectedColumnCount();
			final int numrows = mainProteinDomainTable.getSelectedRowCount();
			final int[] rowsselected = mainProteinDomainTable.getSelectedRows();
			final int[] colsselected = mainProteinDomainTable.getSelectedColumns();

			if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0]
					&& numrows == rowsselected.length)
					&& (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
							&& numcols == colsselected.length))) {
				JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			for (int i = 0; i < numrows; i++) {
				for (int j = 0; j < numcols; j++) {
					sbf.append(mainProteinDomainTable.getValueAt(rowsselected[i], colsselected[j]));
					if (j < numcols - 1) {
						sbf.append('\t');
					}
				}
				sbf.append('\n');
			}
			stsel = new StringSelection(sbf.toString());
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stsel, stsel);
		} else if (actionCommand.equals("Paste")) {

			final int startRow = (mainProteinDomainTable.getSelectedRows())[0];
			final int startCol = (mainProteinDomainTable.getSelectedColumns())[0];
			try {
				final String trString = (String) (clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor));
				final StringTokenizer st1 = new StringTokenizer(trString, "\n");

				Object[][] data = new Object[st1.countTokens()][2];
				tableDataModel.setDataVector(data, columnNames);

				for (int i = 0; st1.hasMoreTokens(); i++) {
					rowstring = st1.nextToken();
					StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
					for (int j = 0; st2.hasMoreTokens(); j++) {
						value = (String) st2.nextToken();
						if (startRow + i < mainProteinDomainTable.getRowCount()
								&& startCol + j < mainProteinDomainTable.getColumnCount()) {
							mainProteinDomainTable.setValueAt(value, startRow + i, startCol + j);
						}
					}
				}

				setTableProperties();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	private boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	private boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}
}
