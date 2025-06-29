package de.fmp.liulab.task;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.internal.UpdateViewListener;
import de.fmp.liulab.internal.view.JFrameWithoutMaxAndMinButton;
import de.fmp.liulab.internal.view.MenuBar;
import de.fmp.liulab.model.GeneDomain;
import de.fmp.liulab.model.ProteinDomain;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for loading domains of a set of proteins
 * 
 * @author borges.diogo
 *
 */
public class LoadProteinDomainTask extends AbstractTask implements ActionListener {

	private CyApplicationManager cyApplicationManager;
	private CyNetwork myNetwork;
	private CyCustomGraphics2Factory vgFactory;

	public static VisualLexicon lexicon;
	public static VisualStyle style;

	// Window
	private JFrameWithoutMaxAndMinButton mainFrame;
	private JPanel mainPanel;
	private static JLabel textLabel_status_result;
	private MenuBar menuBar = new MenuBar();
	private JPanel information_panel;

	// Table
	private static JTable mainProteinDomainTable;
	public static DefaultTableModel tableDataModel;
	private String[] columnNames = { "Node Name", "Domain(s)" };
	private final Class[] columnClass = new Class[] { String.class, String.class };
	private static List<GeneDomain> geneListFromTable;
	private String rowstring, value;
	private Clipboard clipboard;
	private StringSelection stsel;
	private static JList rowHeader;
	private static JScrollPane proteinDomainTableScrollPanel;

	private static boolean isPfamLoaded = true;
	private static boolean pfamDoStop = false;
	private static Thread pfamThread;
	private JButton proteinDomainServerButton;

	private static JButton okButton;
	private static Thread storeDomainThread;
	private static boolean isStoredDomains = false;

	public static boolean isPlotDone = false;

	public static Thread disposeMainJFrameThread;

	/**
	 * /** Constructor
	 * 
	 * @param cyApplicationManager main app manager
	 * @param vmmServiceRef        visual mapping manager
	 * @param vgFactory            graphic factory
	 */
	public LoadProteinDomainTask(CyApplicationManager cyApplicationManager, final VisualMappingManager vmmServiceRef,
			CyCustomGraphics2Factory vgFactory) {

		this.menuBar.domain_ptm_or_monolink = 0;
		this.cyApplicationManager = cyApplicationManager;
		this.myNetwork = cyApplicationManager.getCurrentNetwork();
		this.vgFactory = vgFactory;

		this.style = vmmServiceRef.getCurrentVisualStyle();
		// Get the current Visual Lexicon
		this.lexicon = cyApplicationManager.getCurrentRenderingEngine().getVisualLexicon();

		if (mainFrame == null)
			mainFrame = new JFrameWithoutMaxAndMinButton(new JFrame(), "XlinkCyNET - Load protein domains", 1);

		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Dimension appSize = null;
		if (Util.isWindows()) {
			appSize = new Dimension(540, 345);
		} else if (Util.isMac()) {
			appSize = new Dimension(525, 325);
		} else {
			appSize = new Dimension(525, 335);
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

		initThreads();
	}

	private void initThreads() {
		disposeMainJFrameThread = new Thread() {
			public synchronized void run() {
				disposeMainFrame();
			}
		};
	}

	private void disposeMainFrame() {
		mainFrame.dispose();

		JOptionPane.showMessageDialog(null, "Protein domains have been loaded successfully!",
				"XlinkCyNET - Protein domains", JOptionPane.INFORMATION_MESSAGE,
				new ImageIcon(getClass().getResource("/images/logo.png")));
	}

	/**
	 * Method responsible for running task
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setTitle("XlinkCyNET - Load protein domains task");

		if (cyApplicationManager.getCurrentNetwork() == null) {
			throw new Exception("ERROR: No networks has been loaded.");
		}

		setFrameObjects(taskMonitor);

		// Display the window
		mainFrame.add(mainPanel, BorderLayout.CENTER);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}

	/**
	 * Set all labels in XLinkCyNET window / frame
	 */
	private void initFrameLabels() {

		int offset_y = 0;

		information_panel = new JPanel();
		information_panel.setBorder(BorderFactory.createTitledBorder(""));
		information_panel.setBounds(10, 8, 355, 116);
		information_panel.setLayout(null);
		mainPanel.add(information_panel);

		JLabel textLabel_Protein_lbl_1 = null;
		if (Util.isUnix())
			textLabel_Protein_lbl_1 = new JLabel("Fill in the table below to indicate what proteins will");
		else
			textLabel_Protein_lbl_1 = new JLabel("Fill in the table below to indicate what proteins will have their");
		textLabel_Protein_lbl_1.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_lbl_1.setBounds(10, offset_y, 450, 40);
		information_panel.add(textLabel_Protein_lbl_1);
		offset_y += 20;

		JLabel textLabel_Protein_lbl_2 = null;
		if (Util.isUnix()) {
			textLabel_Protein_lbl_2 = new JLabel("have their domains loaded.");
			textLabel_Protein_lbl_2.setBounds(10, offset_y, 250, 40);
		} else {
			textLabel_Protein_lbl_2 = new JLabel("domains loaded.");
			textLabel_Protein_lbl_2.setBounds(10, offset_y, 100, 40);
		}
		textLabel_Protein_lbl_2.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		information_panel.add(textLabel_Protein_lbl_2);
		offset_y += 30;

		JLabel textLabel_Pfam = new JLabel("Search for domains:");
		textLabel_Pfam.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Pfam.setBounds(10, offset_y, 150, 40);
		information_panel.add(textLabel_Pfam);

		offset_y += 10;
		JRadioButton protein_domain_pfam = new JRadioButton("InterPro");
		protein_domain_pfam.setSelected(Util.isProteinDomainPfam);
		if (Util.isWindows()) {
			protein_domain_pfam.setBounds(179, offset_y, 75, 20);
		} else if (Util.isMac()) {
			protein_domain_pfam.setBounds(203, offset_y, 90, 20);
		} else {
			protein_domain_pfam.setBounds(228, offset_y, 90, 20);
		}
		protein_domain_pfam.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				int state = event.getStateChange();
				if (state == ItemEvent.SELECTED) {

					Util.isProteinDomainPfam = true;
				} else if (state == ItemEvent.DESELECTED) {

					Util.isProteinDomainPfam = false;
				}
			}
		});
		information_panel.add(protein_domain_pfam);

		JRadioButton protein_domain_supfam = new JRadioButton("Supfam");
		protein_domain_supfam.setSelected(!Util.isProteinDomainPfam);
		if (Util.isWindows()) {
			protein_domain_supfam.setBounds(119, offset_y, 64, 20);
		} else if (Util.isMac()) {
			protein_domain_supfam.setBounds(119, offset_y, 79, 20);
		} else {
			protein_domain_supfam.setBounds(149, offset_y, 79, 20);
		}
		protein_domain_supfam.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				int state = event.getStateChange();
				if (state == ItemEvent.SELECTED) {

					Util.isProteinDomainPfam = false;
				} else if (state == ItemEvent.DESELECTED) {

					Util.isProteinDomainPfam = true;
				}
			}
		});
		information_panel.add(protein_domain_supfam);

		ButtonGroup bg_database = new ButtonGroup();
		bg_database.add(protein_domain_pfam);
		bg_database.add(protein_domain_supfam);

		offset_y = 80;

		textLabel_status_result = new JLabel("");
		textLabel_status_result.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status_result.setForeground(new Color(159, 17, 17));
		if (Util.isUnix())
			textLabel_status_result.setBounds(65, offset_y, 350, 40);
		else
			textLabel_status_result.setBounds(55, offset_y, 350, 40);

		JPanel logo_panel = new JPanel();
		logo_panel.setBorder(BorderFactory.createTitledBorder(""));
		logo_panel.setBounds(370, 8, 140, 116);
		logo_panel.setLayout(null);
		mainPanel.add(logo_panel);

		JLabel jLabelIcon = new JLabel();
		jLabelIcon.setBounds(13, -95, 300, 300);
		jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png")));
		logo_panel.add(jLabelIcon);

		JLabel textLabel_status = new JLabel("Status:");
		textLabel_status.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status.setBounds(10, offset_y, 50, 40);
		information_panel.add(textLabel_status);
		information_panel.add(textLabel_status_result);
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
		Action insertLineToTableAction = new AbstractAction("insertLineToTable") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				tableDataModel.addRow(new Object[] { "" });

				Util.updateRowHeader(tableDataModel.getRowCount(), mainProteinDomainTable, rowHeader,
						proteinDomainTableScrollPanel);
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

					int input = JOptionPane.showConfirmDialog(null, "Do you confirm the removal of the line "
							+ (mainProteinDomainTable.getSelectedRow() + 1) + "?");
					// 0=yes, 1=no, 2=cancel
					if (input == 0) {
						// remove selected row from the model
						tableDataModel.removeRow(mainProteinDomainTable.getSelectedRow());
						Util.updateRowHeader(tableDataModel.getRowCount(), mainProteinDomainTable, rowHeader,
								proteinDomainTableScrollPanel);
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
		proteinDomainTableScrollPanel = new JScrollPane();
		proteinDomainTableScrollPanel.setBounds(10, 130, 500, 105);
		proteinDomainTableScrollPanel.setViewportView(mainProteinDomainTable);
		proteinDomainTableScrollPanel.setRowHeaderView(rowHeader);
		setTableProperties(1);
		mainPanel.add(proteinDomainTableScrollPanel);
	}

	/**
	 * Method responsible for getting protein domains from Pfam or Supfam database
	 * for each node
	 * 
	 * @param taskMonitor
	 */
	private static String getPfamOrSupfamProteinDomainsForeachNode(CyNetwork myNetwork, final TaskMonitor taskMonitor) {

		int old_progress = 0;
		int summary_processed = 0;
		int total_genes = geneListFromTable.size();

		StringBuilder sb_error = new StringBuilder();

		CyRow myCurrentRow = null;
		for (final GeneDomain geneDomain : geneListFromTable) {

			if (pfamDoStop)
				break;

			CyNode current_node = Util.getNode(myNetwork, geneDomain.geneName);

			if (current_node != null) {
				myCurrentRow = myNetwork.getRow(current_node);

				List<ProteinDomain> new_proteinDomains = Util.getProteinDomainsFromServer(myCurrentRow, taskMonitor);
				if (new_proteinDomains.size() > 0) {
					geneDomain.proteinDomains = new_proteinDomains;
				}
			} else {
				sb_error.append("ERROR: Node " + geneDomain.geneName + " has not been found.\n");
			}
			summary_processed++;
			int new_progress = (int) ((double) summary_processed / (total_genes) * 100);
			if (new_progress > old_progress) {
				old_progress = new_progress;

				if (textLabel_status_result != null) {
					textLabel_status_result.setText("Getting protein domains: " + old_progress + "%");
				}
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein domains: " + old_progress + "%");
			}
		}
		if (sb_error.toString().isBlank() || sb_error.toString().isEmpty()) {

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done!");
			if (textLabel_status_result != null) {
				textLabel_status_result.setText("Done!");
			}
		}

		return sb_error.toString();
	}

	/**
	 * Method responsible for initializing all button in the Frame
	 * 
	 * @param taskMonitor
	 */
	private void initButtons(final TaskMonitor taskMonitor) {

		Icon iconBtn = new ImageIcon(getClass().getResource("/images/browse_Icon.png"));
		proteinDomainServerButton = new JButton(iconBtn);
		if (Util.isWindows())
			proteinDomainServerButton.setBounds(250, 55, 30, 30);
		else if (Util.isMac())
			proteinDomainServerButton.setBounds(280, 55, 30, 30);
		else
			proteinDomainServerButton.setBounds(295, 55, 30, 30);

		proteinDomainServerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		proteinDomainServerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				taskMonitor.setTitle("Load protein domains...");

				if (isPfamLoaded) {
					try {

						isPlotDone = false;
						textLabel_status_result.setText("Getting protein domains...");
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein domains...");
						String msgError = getNodesFromTable(myNetwork, true);
						if (!msgError.isBlank() && !msgError.isEmpty()) {
							textLabel_status_result.setText("ERROR: Check Task History.");
							taskMonitor.showMessage(TaskMonitor.Level.ERROR, msgError);
						} else {

							if (Util.isProteinDomainPfam) {
								textLabel_status_result.setText("Accessing InterPro database...");
								taskMonitor.showMessage(TaskMonitor.Level.INFO, "Accessing InterPro database...");
							} else {
								textLabel_status_result.setText("Accessing Supfam database...");
								taskMonitor.showMessage(TaskMonitor.Level.INFO, "Accessing Supfam database...");
							}
							getProteinDomainsFromServer(taskMonitor, myNetwork, true);

						}
					} catch (Exception exception) {
					}
				} else {
					JOptionPane.showMessageDialog(null, "Wait! There is another process in progress!",
							"XlinkCyNET - Protein domains", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		information_panel.add(proteinDomainServerButton);

		Icon iconBtnOk = new ImageIcon(getClass().getResource("/images/okBtn.png"));
		okButton = new JButton(iconBtnOk);
		okButton.setText("OK");
		if (Util.isWindows())
			okButton.setBounds(30, 250, 220, 25);
		else
			okButton.setBounds(30, 240, 220, 25);

		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				boolean concluedProcess = true;
				try {

					if (!isPfamLoaded) {
						int input = JOptionPane.showConfirmDialog(null,
								"InterPro process has not been finished yet. Do you want to close this window?",
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
						storeProteinDomains(taskMonitor, myNetwork, true);

					}

				} catch (Exception e1) {
					textLabel_status_result.setText("ERROR: Check Task History.");
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, "ERROR: " + e1.getMessage());

					if (storeDomainThread != null)
						storeDomainThread.interrupt();
				}
			}
		});
		mainPanel.add(okButton);

		Icon iconBtnCancel = new ImageIcon(getClass().getResource("/images/cancelBtn.png"));
		JButton cancelButton = new JButton(iconBtnCancel);
		cancelButton.setText("Cancel");
		if (Util.isWindows())
			cancelButton.setBounds(265, 250, 220, 25);
		else
			cancelButton.setBounds(265, 240, 220, 25);

		cancelButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (cancelProcess())
					mainFrame.dispose();
			}
		});
		mainPanel.add(cancelButton);
	}

	/**
	 * Get protein domains from server
	 * 
	 * @param taskMonitor task monitor
	 */
	public static void getProteinDomainsFromServer(TaskMonitor taskMonitor, CyNetwork myNetwork, boolean fromScreen) {

		String[] columnNames = { "Node Name", "Domain(s)" };
		pfamThread = new Thread() {
			public synchronized void run() {
				pfamDoStop = false;
				isPfamLoaded = false;

				String msgError = getPfamOrSupfamProteinDomainsForeachNode(myNetwork, taskMonitor);
				if (!msgError.isBlank() && !msgError.isEmpty()) {
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, msgError);
					if (textLabel_status_result != null)
						textLabel_status_result.setText("ERROR: Check Task History.");
					isPfamLoaded = true;
				} else {
					Object[][] data = null;
					if (geneListFromTable.size() > 0)
						data = new Object[geneListFromTable.size()][2];
					else
						data = new Object[1][2];

					tableDataModel.setDataVector(data, columnNames);
					int countPtnDomain = 0;
					for (final GeneDomain geneDomain : geneListFromTable) {

						tableDataModel.setValueAt(geneDomain.geneName, countPtnDomain, 0);
						tableDataModel.setValueAt(ToStringProteinDomains(geneDomain.proteinDomains), countPtnDomain, 1);
						countPtnDomain++;
					}
					if (geneListFromTable.size() > 0)
						setTableProperties(geneListFromTable.size());
					else
						setTableProperties(1);
					isPfamLoaded = true;

					// It's called via command line
					if (!fromScreen) {
						try {
							storeProteinDomains(taskMonitor, myNetwork, false);
						} catch (Exception e) {
							taskMonitor.showMessage(TaskMonitor.Level.ERROR, "ERROR: " + e.getMessage());
						}
					}
				}
			}
		};

		pfamThread.start();
	}

	/**
	 * Method responsible for storing protein domains
	 * 
	 * @param taskMonitor
	 * @throws Exception
	 */
	public static void storeProteinDomains(TaskMonitor taskMonitor, final CyNetwork myNetwork, boolean isFromScreen)
			throws Exception {

		if (myNetwork == null) {
			throw new Exception("ERROR: No network has been found.");
		}

		storeDomainThread = new Thread() {

			public synchronized void run() {

				isPlotDone = false;
				if (isFromScreen)
					textLabel_status_result.setText("Checking nodes ...");
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Checking nodes...");
				String msgError = getNodesFromTable(myNetwork, !isFromScreen);
				if (!msgError.isBlank() && !msgError.isEmpty()) {
					textLabel_status_result.setText("ERROR: Check Task History.");
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, msgError);

					isPlotDone = true;
					UpdateViewListener.isNodeModified = true;
					isStoredDomains = true;

				} else {
					isStoredDomains = false;
					if (isFromScreen)
						textLabel_status_result.setText("Setting nodes information...");
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting nodes information...");
					try {

						msgError = setNodesInformation(taskMonitor, myNetwork);
						if (!msgError.isBlank() && !msgError.isEmpty()) {
							textLabel_status_result.setText("WARNING: Check Task History.");
							taskMonitor.showMessage(TaskMonitor.Level.WARN, msgError);
						}

						if (geneListFromTable.size() > 0) {
							Util.update_ProteinDomainColumn(taskMonitor, myNetwork, geneListFromTable);

						}

						taskMonitor.setProgress(1.0);
						taskMonitor.showMessage(TaskMonitor.Level.INFO,
								"Protein domains have been loaded successfully!");

						if (isFromScreen) {
							textLabel_status_result.setText("Done!");
							disposeMainJFrameThread.start();
						}

					} catch (Exception e) {
						if (isFromScreen)
							textLabel_status_result.setText("ERROR: Check Task History.");
						taskMonitor.showMessage(TaskMonitor.Level.ERROR, "ERROR: " + e.getMessage());
					}

					isPlotDone = true;
					UpdateViewListener.isNodeModified = true;
					isStoredDomains = true;
				}
			}
		};

		storeDomainThread.start();
	}

	/**
	 * Method responsible for canceling the loading process
	 * 
	 * @return true if process is canceled, otherwise, returns false.
	 */
	public static boolean cancelProcess() {

		boolean concluedProcess = true;

		if (!isPfamLoaded) {
			int input = JOptionPane.showConfirmDialog(null,
					"Supfam/InterPro process has not been finished yet. Do you want to close this window?",
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

		if (!okButton.isEnabled() && !isStoredDomains) {
			int input = JOptionPane.showConfirmDialog(null,
					"Protein domains has not been stored yet. Do you want to close this window?",
					"XlinkCyNET - Protein domains", JOptionPane.INFORMATION_MESSAGE);
			// 0=yes, 1=no, 2=cancel
			if (input == 0) {
				concluedProcess = true;
				if (storeDomainThread != null) {
					storeDomainThread.interrupt();
				}
			} else {
				concluedProcess = false;
			}
		}

		if (concluedProcess) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Method responsible for converting ProteinDomain collection into string
	 * 
	 * @param proteinDomains
	 * @return
	 */
	private static String ToStringProteinDomains(List<ProteinDomain> proteinDomains) {

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
	 * Method responsible for setting information to all nodes
	 * 
	 * @throws Exception
	 */
	private static String setNodesInformation(final TaskMonitor taskMonitor, final CyNetwork myNetwork)
			throws Exception {

		if (myNetwork == null)
			return "ERROR: Network has not been found.";

		// Initialize protein domain colors map if MainSingleNodeTask has not been
		// initialized
		Util.init_availableProteinDomainColorsMap();

		int old_progress = 0;
		int summary_processed = 0;
		int total_genes = geneListFromTable.size();

		StringBuilder sb_error = new StringBuilder();

		for (final GeneDomain geneDomain : geneListFromTable) {

			CyNode currentNode = null;

			// Check if the node exists in the network
			Optional<CyRow> isNodePresent = myNetwork.getDefaultNodeTable().getAllRows().stream()
					.filter(new Predicate<CyRow>() {
						public boolean test(CyRow o) {
							return o.get(CyNetwork.NAME, String.class).equals(geneDomain.geneName);
						}
					}).findFirst();

			if (isNodePresent.isPresent()) {// Get node if exists
				CyRow _node_row = isNodePresent.get();
				currentNode = myNetwork.getNode(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));

				if (geneDomain.proteinDomains.size() > 0) {
					updateProteinDomainsMap(myNetwork, currentNode, geneDomain.proteinDomains);

				}
			} else {
				sb_error.append("WARNING: Node " + geneDomain.geneName + " has not been found.\n");
			}

			summary_processed++;
			int new_progress = (int) ((double) summary_processed / (total_genes) * 100);
			if (new_progress > old_progress) {
				old_progress = new_progress;

				if (textLabel_status_result != null)
					textLabel_status_result.setText("Storing protein domains: " + old_progress + "%");
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Storing protein domains: " + old_progress + "%");
			}
		}

		taskMonitor.setProgress(0.98);
		return sb_error.toString();
	}

	/**
	 * Method responsible for update Protein domains map
	 * 
	 * @param node
	 * @param myProteinDomains
	 */
	public static void updateProteinDomainsMap(CyNetwork myNetwork, CyNode node, List<ProteinDomain> myProteinDomains) {
		String network_name = myNetwork.toString();
		if (Util.proteinDomainsMap.containsKey(network_name)) {

			Map<Long, List<ProteinDomain>> all_proteinDomains = Util.proteinDomainsMap.get(network_name);
			all_proteinDomains.put(node.getSUID(), myProteinDomains);

		} else {// Network does not exists

			Map<Long, List<ProteinDomain>> proteinDomains = new HashMap<Long, List<ProteinDomain>>();
			proteinDomains.put(node.getSUID(), myProteinDomains);
			Util.proteinDomainsMap.put(network_name, proteinDomains);
		}
		Util.updateProteinDomainsColorMap(myProteinDomains);

	}

	/**
	 * Get all nodes filled out in JTable
	 */
	public static String getNodesFromTable(CyNetwork myNetwork, boolean retrieveAllNodes) {

		geneListFromTable = new ArrayList<GeneDomain>();
		StringBuilder sbError = new StringBuilder();

		int old_progress = 0;
		int summary_processed = 0;
		int total_rows = tableDataModel.getRowCount();

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
						String domainName = domainsArray[0].trim();
						String[] colRange = domainsArray[1].split("-");
						int startId = Integer.parseInt(colRange[0]);
						int endId = Integer.parseInt(colRange[1]);
						proteinDomains.add(new ProteinDomain(domainName, startId, endId, ""));
					}
				} catch (Exception e) {
					sbError.append("ERROR: Row: " + (row + 1)
							+ " - Protein domains don't match with the pattern 'name[start_index-end_index]'\n");
				}
			}
			if (gene.isEmpty() || gene.isBlank()) {
				sbError.append("ERROR: Row: " + (row + 1) + " - Gene is empty.");
			} else {
				geneListFromTable.add(new GeneDomain(gene, proteinDomains));
			}

			summary_processed++;
			int new_progress = (int) ((double) summary_processed / (total_rows) * 100);
			if (new_progress > old_progress) {
				old_progress = new_progress;

				if (textLabel_status_result != null)
					textLabel_status_result.setText("Checking nodes: " + old_progress + "%");
			}

		}

		if (retrieveAllNodes && geneListFromTable.size() == 0
				&& sbError.toString().equals("ERROR: Row: 1 - Gene is empty.")) {
			// No protein is filled in the table. Then, get all proteins

			fillAllNodesInTheTable(myNetwork);
			getNodesFromTable(myNetwork, retrieveAllNodes);
			return "";
		}

		if (geneListFromTable.size() == 0)
			return "";
		else
			return sbError.toString();
	}

	/**
	 * Get all nodes name and add in the table
	 * 
	 * @param myNetwork current network
	 */
	private static void fillAllNodesInTheTable(CyNetwork myNetwork) {

		StringBuilder sb_data_to_be_stored = new StringBuilder();

		List<CyNode> allNodes = myNetwork.getNodeList();
		for (CyNode cyNode : allNodes) {

			String nodeName = myNetwork.getRow(cyNode).get(CyNetwork.NAME, String.class);

			if (nodeName.contains("Target") || nodeName.contains("Source") || nodeName.contains("PTM"))
				continue;

			sb_data_to_be_stored.append(nodeName).append("\n");

		}

		updateDataModel(sb_data_to_be_stored);
	}

	/**
	 * Update table data model
	 * 
	 * @param sb_data_to_be_stored data
	 */
	public static void updateDataModel(StringBuilder sb_data_to_be_stored) {

		int countPtnDomain = 0;
		String[] data_to_be_stored = sb_data_to_be_stored.toString().split("\n");

		Object[][] data = new Object[data_to_be_stored.length][2];
		String[] columnNames = { "Node Name", "Domain(s)" };
		tableDataModel.setDataVector(data, columnNames);

		for (String line : data_to_be_stored) {
			String[] cols_line = line.split("\t");
			tableDataModel.setValueAt(cols_line[0], countPtnDomain, 0);
			countPtnDomain++;
		}

		setTableProperties(countPtnDomain);
	}

	/**
	 * Set properties to the Node domain table
	 * 
	 * @param number_lines total number of lines
	 */
	public static void setTableProperties(int number_lines) {
		if (mainProteinDomainTable != null) {
			mainProteinDomainTable.setPreferredScrollableViewportSize(new Dimension(490, 90));
			mainProteinDomainTable.getColumnModel().getColumn(0).setPreferredWidth(50);
			mainProteinDomainTable.getColumnModel().getColumn(1).setPreferredWidth(250);
			mainProteinDomainTable.setFillsViewportHeight(true);
			mainProteinDomainTable.setAutoCreateRowSorter(true);

			Util.updateRowHeader(number_lines, mainProteinDomainTable, rowHeader, proteinDomainTableScrollPanel);
		}
	}

	/**
	 * Method responsible for activating action.
	 */
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

				int i = 0;
				for (i = 0; st1.hasMoreTokens(); i++) {
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

				setTableProperties(i);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
