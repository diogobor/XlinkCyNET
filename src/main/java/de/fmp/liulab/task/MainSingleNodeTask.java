package de.fmp.liulab.task;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultFormatter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.internal.UpdateViewListener;
import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.model.ProteinDomain;
import de.fmp.liulab.utils.Tuple2;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for applying layout to a node
 * 
 * @author diogobor
 *
 */
public class MainSingleNodeTask extends AbstractTask implements ActionListener {

	public static boolean isPlotDone = false;

	private CyApplicationManager cyApplicationManager;
	private CyNetwork myNetwork;
	private CyNetworkView netView;
	private CyCustomGraphics2Factory vgFactory;
	private HandleFactory handleFactory;
	private BendFactory bendFactory;
	private boolean forcedWindowOpen;
	public static VisualStyle style;
	public static VisualLexicon lexicon;

	private static View<CyNode> nodeView;
	private CyRow myCurrentRow;
	private List<CyNode> nodes;
	private boolean isCurrentNode_modified = false;

	private ArrayList<ProteinDomain> proteinDomainsServer;
	private ArrayList<ProteinDomain> myProteinDomains;

	// Window
	private JFrame mainFrame;
	private JPanel mainPanel;
	private JPanel protein_panel;
	private JLabel textLabel_status_result;
	private String[] columnNames = { "Domain(*)", "Start Residue(*)", "End Residue(*)", "e-value", "Color" };
	private final Class[] columnClass = new Class[] { String.class, Integer.class, Integer.class, String.class,
			String.class };
	private DefaultTableModel tableDataModel;
	private static JTable mainProteinDomainTable;
	private static JList rowHeader;
	private static JScrollPane proteinDomainTableScrollPanel;

	public static ArrayList<CrossLink> interLinks;
	public static ArrayList<CrossLink> intraLinks;

	public static CyNode node;

	private Thread pfamThread;
	private JButton proteinDomainServerButton;
	private Thread applyLayoutThread;
	private JButton okButton;

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager
	 * @param vmmServiceRef
	 * @param vgFactory
	 * @param bendFactory
	 * @param handleFactory
	 * @param forcedWindowOpen
	 */
	public MainSingleNodeTask(CyApplicationManager cyApplicationManager, final VisualMappingManager vmmServiceRef,
			CyCustomGraphics2Factory<?> vgFactory, BendFactory bendFactory, HandleFactory handleFactory,
			boolean forcedWindowOpen) {
		this.cyApplicationManager = cyApplicationManager;
		this.netView = cyApplicationManager.getCurrentNetworkView();
		this.myNetwork = cyApplicationManager.getCurrentNetwork();
		this.vgFactory = vgFactory;
		style = vmmServiceRef.getCurrentVisualStyle();
		// Get the current Visual Lexicon
		lexicon = cyApplicationManager.getCurrentRenderingEngine().getVisualLexicon();
		this.bendFactory = bendFactory;
		this.handleFactory = handleFactory;
		this.forcedWindowOpen = forcedWindowOpen;

		// Initialize protein domain colors map if LoadProteinDomainTask has not been
		// initialized
		Util.init_availableProteinDomainColorsMap();

		if (mainFrame == null)
			mainFrame = new JFrame("XlinkCyNET - Single Node");

		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension appSize = null;
		if (Util.isWindows()) {
			appSize = new Dimension(540, 395);
		} else {
			appSize = new Dimension(540, 375);
		}
		mainFrame.setSize(appSize);
		mainFrame.setResizable(false);

		if (mainPanel == null)
			mainPanel = new JPanel();
		mainPanel.setBounds(10, 10, 490, 365);
		mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""));
		mainPanel.setLayout(null);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setLocation((screenSize.width - appSize.width) / 2, (screenSize.height - appSize.height) / 2);
	}

	/**
	 * Method responsible for initializing the task
	 */
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setTitle("XlinkCyNET - Layout task");
		// Write your own function here.
		if (cyApplicationManager.getCurrentNetwork() == null) {
			throw new Exception("ERROR: No network has been loaded.");
		}

		checkSingleOrMultipleSelectedNodes(taskMonitor);

	}

	/**
	 * Method responsible for checking how many nodes have been selected
	 * 
	 * @param taskMonitor
	 * @throws Exception
	 */
	private void checkSingleOrMultipleSelectedNodes(final TaskMonitor taskMonitor) throws Exception {

		nodes = CyTableUtil.getNodesInState(myNetwork, CyNetwork.SELECTED, true);

		if (nodes.size() == 0) {

			throw new Exception("No node has been selected. Please select one!");

		} else if (nodes.size() > 1) {

			executeMultipleNodes(taskMonitor);

		} else {
			node = nodes.get(0);
			executeSingleNode(taskMonitor);
		}

	}

	/**
	 * Method responsible for executing layout to a single node
	 * 
	 * @param taskMonitor
	 */
	private void executeSingleNode(final TaskMonitor taskMonitor) {

		getNodeInformation();

		if (intraLinks.size() == 0 && interLinks.size() == 0)// It's a intralink_single_node
			return;

		if (forcedWindowOpen) {// Action comes from Context Menu item

			this.init_xl_layout(taskMonitor);

		} else {// Action comes from Shortcut

			if (isCurrentNode_modified) {
				if (isPlotDone)
					this.restoreDefaultStyle(taskMonitor);

			} else {

				this.setCurrentLayout(taskMonitor);

			}
		}
	}

	/**
	 * Method responsible for executing layout to multiple nodes
	 * 
	 * @param taskMonitor
	 */
	private void executeMultipleNodes(final TaskMonitor taskMonitor) {

		forcedWindowOpen = false;
		for (CyNode current_node : nodes) {
			node = current_node;
			executeSingleNode(taskMonitor);
		}
	}

	@Override
	public void cancel() {

		this.cancel();
		super.cancel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	/**
	 * Method responsible for applying to node the current layout/style
	 * 
	 * @param taskMonitor
	 */
	private void setCurrentLayout(final TaskMonitor taskMonitor) {

		isPlotDone = false;
		LoadProteinDomainTask.isPlotDone = false;
		Util.stopUpdateViewer=false;

		if (netView == null) {
			netView = cyApplicationManager.getCurrentNetworkView();
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting the selected node...");
		nodeView = netView.getNodeView(node);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node styles...");
		Util.setNodeStyles(myNetwork, node, netView);
		taskMonitor.setProgress(0.2);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein domains...");
		getProteinDomains(node);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting protein domains to node...");
		setNodeDomainColors();
		taskMonitor.setProgress(0.75);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Defining styles for cross-links...");
		isPlotDone = Util.addOrUpdateEdgesToNetwork(myNetwork, node, style, netView, nodeView, handleFactory,
				bendFactory, lexicon, Util.getProteinLengthScalingFactor(), intraLinks, interLinks, taskMonitor, null);
		taskMonitor.setProgress(0.95);

		// Apply the change to the view
		style.apply(netView);
		netView.updateView();
		taskMonitor.setProgress(1.0);
	}

	/**
	 * Method responsible for getting the protein domains of the selected node from
	 * the main map (Util.proteinDomainsMap)
	 * 
	 * @param node
	 */
	private void getProteinDomains(CyNode node) {

		boolean hasProteinDomains = false;

		String network_name = myNetwork.toString();
		if (Util.proteinDomainsMap.containsKey(network_name)) {

			Map<Long, List<ProteinDomain>> all_proteinDomains = Util.proteinDomainsMap.get(network_name);

			if (all_proteinDomains.containsKey(node.getSUID())) {
				hasProteinDomains = true;
				myProteinDomains = (ArrayList<ProteinDomain>) all_proteinDomains.get(node.getSUID());
			}
		}

		if (!hasProteinDomains) {
			myProteinDomains = new ArrayList<ProteinDomain>();
		}
	}

	/**
	 * Method responsible for opening the Single Node Layout window
	 * 
	 * @param taskMonitor
	 */
	private void init_xl_layout(final TaskMonitor taskMonitor) {

		isPlotDone = false;
		setFrameObjects(taskMonitor);
		// Display the window
		mainFrame.add(mainPanel, BorderLayout.CENTER);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}

	/**
	 * Get all information of a node
	 * 
	 * @throws Exception
	 */
	public void getNodeInformation() {

		// ##### GET THE SELECTED NODE - ONLY ONE IS POSSIBLE TO APPLY CHANGES ######

		myCurrentRow = myNetwork.getRow(node);
		nodeView = netView.getNodeView(node);
		Object length_protein_a = myCurrentRow.getRaw(Util.PROTEIN_LENGTH_A);
		Object length_protein_b = myCurrentRow.getRaw(Util.PROTEIN_LENGTH_B);

		double currentNodeWidth = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		if (currentNodeWidth == 0)
			currentNodeWidth = 1;

		if (length_protein_a == null) {
			if (length_protein_b == null)
				length_protein_a = 10;
			else
				length_protein_a = length_protein_b;
		}

		/**
		 * Modify node style
		 */
		currentNodeWidth = ((Number) length_protein_a).doubleValue();
		Util.setProteinLength((float) currentNodeWidth);

		/**
		 * Get intra and interlinks
		 */
		Tuple2 inter_and_intralinks = Util.getAllLinksFromAdjacentEdgesNode(node, myNetwork);
		interLinks = (ArrayList<CrossLink>) inter_and_intralinks.getFirst();
		intraLinks = (ArrayList<CrossLink>) inter_and_intralinks.getSecond();

		isCurrentNode_modified = Util.IsNodeModified(myNetwork, netView, node);
		Util.node_label_factor_size = myNetwork.getRow(node).get(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME,
				Double.class) == null ? 1.0
						: myNetwork.getRow(node).get(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME, Double.class);
		Util.isProtein_expansion_horizontal = myNetwork.getRow(node).get(Util.HORIZONTAL_EXPANSION_COLUMN_NAME,
				Boolean.class) == null ? true
						: myNetwork.getRow(node).get(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, Boolean.class);

	}

	/**
	 * Set properties to the Node domain table
	 */
	private void setTableProperties(int number_lines) {
		if (mainProteinDomainTable != null) {
			mainProteinDomainTable.setPreferredScrollableViewportSize(new Dimension(490, 90));
			mainProteinDomainTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			mainProteinDomainTable.getColumnModel().getColumn(1).setPreferredWidth(150);
			mainProteinDomainTable.getColumnModel().getColumn(2).setPreferredWidth(150);
			mainProteinDomainTable.getColumnModel().getColumn(3).setPreferredWidth(80);
			mainProteinDomainTable.getColumnModel().getColumn(4).setPreferredWidth(100);
			mainProteinDomainTable.setFillsViewportHeight(true);
			mainProteinDomainTable.setAutoCreateRowSorter(true);

			Util.updateRowHeader(number_lines, mainProteinDomainTable, rowHeader, proteinDomainTableScrollPanel);
		}
	}

	/**
	 * Set all labels in XLinkCyNET window / frame
	 */
	private void setFrameLabels() {

		int offset_y = -20;

		protein_panel = new JPanel();
		protein_panel.setBorder(BorderFactory.createTitledBorder("Protein"));
		if (Util.isWindows())
			protein_panel.setBounds(10, 10, 250, 150);
		else
			protein_panel.setBounds(10, 10, 275, 150);
		protein_panel.setLayout(null);
		mainPanel.add(protein_panel);

		JLabel textLabel_Protein_lbl = new JLabel("Name:");
		textLabel_Protein_lbl.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_lbl.setBounds(10, offset_y, 50, 100);
		protein_panel.add(textLabel_Protein_lbl);

		JLabel textLabel_Protein_result = new JLabel();
		textLabel_Protein_result.setText((String) myCurrentRow.getRaw(CyNetwork.NAME));
		textLabel_Protein_result.setFont(new java.awt.Font("Tahoma", Font.BOLD, 12));
		textLabel_Protein_result.setBounds(95, offset_y, 100, 100);
		protein_panel.add(textLabel_Protein_result);
		offset_y += 30;

		JLabel textLabel_Protein_size_lbl = new JLabel("Size:");
		textLabel_Protein_size_lbl.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_size_lbl.setBounds(10, offset_y, 70, 100);
		protein_panel.add(textLabel_Protein_size_lbl);

		JLabel textLabel_Protein_size_result = new JLabel();
		textLabel_Protein_size_result.setText((int) Util.getProteinLength() + " residues");
		textLabel_Protein_size_result.setFont(new java.awt.Font("Tahoma", Font.BOLD, 12));
		textLabel_Protein_size_result.setBounds(95, offset_y, 100, 100);
		protein_panel.add(textLabel_Protein_size_result);
		offset_y += 30;

		JLabel textLabel_Protein_expansion_lbl = new JLabel("Expansion:");
		textLabel_Protein_expansion_lbl.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_expansion_lbl.setBounds(10, offset_y, 70, 100);
		protein_panel.add(textLabel_Protein_expansion_lbl);
		offset_y += 40;

		boolean isHorizontalExpansion = myNetwork.getRow(node).get(Util.HORIZONTAL_EXPANSION_COLUMN_NAME,
				Boolean.class) == null ? true
						: myNetwork.getRow(node).get(Util.HORIZONTAL_EXPANSION_COLUMN_NAME, Boolean.class);
		JRadioButton protein_expansion_horizontal = new JRadioButton("Horizontal");
		protein_expansion_horizontal.setSelected(isHorizontalExpansion);
		if (Util.isWindows()) {
			protein_expansion_horizontal.setBounds(89, offset_y, 90, 20);
		} else {
			protein_expansion_horizontal.setBounds(89, offset_y, 105, 20);
		}
		protein_expansion_horizontal.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				int state = event.getStateChange();
				if (state == ItemEvent.SELECTED) {

					Util.isProtein_expansion_horizontal = true;
				} else if (state == ItemEvent.DESELECTED) {

					Util.isProtein_expansion_horizontal = false;
				}
			}
		});
		protein_panel.add(protein_expansion_horizontal);

		JRadioButton protein_expansion_vertical = new JRadioButton("Vertical");
		protein_expansion_vertical.setSelected(!isHorizontalExpansion);
		if (Util.isWindows()) {
			protein_expansion_vertical.setBounds(179, offset_y, 63, 20);
		} else {
			protein_expansion_vertical.setBounds(185, offset_y, 90, 20);
		}
		protein_expansion_vertical.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				int state = event.getStateChange();
				if (state == ItemEvent.SELECTED) {

					Util.isProtein_expansion_horizontal = false;
				} else if (state == ItemEvent.DESELECTED) {

					Util.isProtein_expansion_horizontal = true;
				}
			}
		});
		protein_panel.add(protein_expansion_vertical);
		ButtonGroup bg_expansion = new ButtonGroup();
		bg_expansion.add(protein_expansion_horizontal);
		bg_expansion.add(protein_expansion_vertical);
		offset_y -= 10;

		JLabel factor_size_node = new JLabel("Scaling factor:");
		factor_size_node.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		factor_size_node.setBounds(10, offset_y, 90, 100);
		protein_panel.add(factor_size_node);

		offset_y = 165;

		JRadioButton protein_domain_pfam = new JRadioButton("Pfam");
		protein_domain_pfam.setSelected(Util.isProteinDomainPfam);
		if (Util.isWindows()) {
			protein_domain_pfam.setBounds(179, offset_y, 50, 20);
		} else {
			protein_domain_pfam.setBounds(193, offset_y, 65, 20);
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
		mainPanel.add(protein_domain_pfam);

		JRadioButton protein_domain_supfam = new JRadioButton("Supfam");
		protein_domain_supfam.setSelected(!Util.isProteinDomainPfam);
		if (Util.isWindows()) {
			protein_domain_supfam.setBounds(119, offset_y, 64, 20);
		} else {
			protein_domain_supfam.setBounds(119, offset_y, 79, 20);
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
		mainPanel.add(protein_domain_supfam);

		ButtonGroup bg_database = new ButtonGroup();
		bg_database.add(protein_domain_pfam);
		bg_database.add(protein_domain_supfam);

		offset_y -= 40;

		JLabel textLabel_Pfam = new JLabel("Search for domains:");
		textLabel_Pfam.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Pfam.setBounds(10, offset_y, 300, 100);
		mainPanel.add(textLabel_Pfam);
		offset_y += 25;

		textLabel_status_result = new JLabel("???");
		textLabel_status_result.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status_result.setForeground(new Color(159, 17, 17));
		textLabel_status_result.setBounds(65, offset_y, 350, 100);

		JPanel logo_panel = new JPanel();
		logo_panel.setBorder(BorderFactory.createTitledBorder(""));
		if (Util.isWindows())
			logo_panel.setBounds(265, 16, 245, 142);
		else
			logo_panel.setBounds(290, 16, 220, 142);
		logo_panel.setLayout(null);
		mainPanel.add(logo_panel);

		JLabel jLabelIcon = new JLabel();
		if (Util.isWindows())
			jLabelIcon.setBounds(70, -75, 300, 300);
		else
			jLabelIcon.setBounds(55, -75, 300, 300);
		jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png")));
		logo_panel.add(jLabelIcon);

		JLabel textLabel_status = new JLabel("Status:");
		textLabel_status.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status.setBounds(10, offset_y, 50, 100);
		mainPanel.add(textLabel_status);
		mainPanel.add(textLabel_status_result);

		offset_y = 262;
		JLabel textLabel_required_fields = new JLabel("(*) Required fields");
		textLabel_required_fields.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 10));
		textLabel_required_fields.setBounds(10, offset_y, 150, 100);
		mainPanel.add(textLabel_required_fields);
	}

	/**
	 * Set all objects to the main Frame
	 * 
	 * @param taskMonitor
	 */
	private void setFrameObjects(final TaskMonitor taskMonitor) {

		setFrameLabels();

		double current_scaling_factor = myNetwork.getRow(node).get(Util.PROTEIN_SCALING_FACTOR_COLUMN_NAME,
				Double.class);

		SpinnerModel factor_size_node = new SpinnerNumberModel(current_scaling_factor, // initial value
				0.1, // min
				1, // max
				0.1); // step
		final JSpinner spinner_factor_size_node = new JSpinner(factor_size_node);
		spinner_factor_size_node.setBounds(95, 110, 60, 20);
		JComponent comp_factor_size_node = spinner_factor_size_node.getEditor();
		JFormattedTextField field_factor_size_node = (JFormattedTextField) comp_factor_size_node.getComponent(0);
		DefaultFormatter formatter_factor_size_node = (DefaultFormatter) field_factor_size_node.getFormatter();
		formatter_factor_size_node.setCommitsOnValidEdit(true);
		spinner_factor_size_node.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.node_label_factor_size = (double) spinner_factor_size_node.getValue();
			}
		});
		spinner_factor_size_node.setToolTipText(
				"Scaling factor to the protein length. It ranges between 0 (small) and 1 (original length).");
		protein_panel.add(spinner_factor_size_node);

		Icon iconBtn = new ImageIcon(getClass().getResource("/images/browse_Icon.png"));
		proteinDomainServerButton = new JButton(iconBtn);
		if (Util.isWindows())
			proteinDomainServerButton.setBounds(228, 160, 30, 30);
		else
			proteinDomainServerButton.setBounds(253, 160, 30, 30);

		proteinDomainServerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				taskMonitor.setTitle("XL interactions");
				proteinDomainServerButton.setEnabled(false);
				try {

					textLabel_status_result.setText("Accessing Pfam database...");
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "Accessing Pfam database...");
					pfamThread = new Thread() {
						public void run() {
							taskMonitor.setTitle("XL interactions");

							textLabel_status_result.setText("Getting protein domains...");
							taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein domains...");
							proteinDomainsServer = Util.getProteinDomains(myCurrentRow);
							taskMonitor.setProgress(0.4);
							if (proteinDomainsServer.size() > 0)
								textLabel_status_result.setText("Done!");
							else {
								textLabel_status_result.setText("WARNING: Check Task History.");
								taskMonitor.showMessage(TaskMonitor.Level.WARN, "No protein domain has been found for '"
										+ myNetwork.getDefaultNodeTable().getRow(node.getSUID()).getRaw(CyNetwork.NAME)
										+ "'.");
							}

							Object[][] data = null;
							if (proteinDomainsServer.size() > 0)
								data = new Object[proteinDomainsServer.size()][5];
							else
								data = new Object[1][5];
							tableDataModel.setDataVector(data, columnNames);

							int countPtnDomain = 0;
							for (ProteinDomain domain : proteinDomainsServer) {
								tableDataModel.setValueAt(domain.name, countPtnDomain, 0);
								tableDataModel.setValueAt(domain.startId, countPtnDomain, 1);
								tableDataModel.setValueAt(domain.endId, countPtnDomain, 2);
								tableDataModel.setValueAt(domain.eValue, countPtnDomain, 3);
								countPtnDomain++;
							}

							if (proteinDomainsServer.size() > 0)
								setTableProperties(proteinDomainsServer.size());
							else
								setTableProperties(1);
							proteinDomainServerButton.setEnabled(true);
						}
					};

					pfamThread.start();

				} catch (Exception exception) {
				}
			}
		});
		mainPanel.add(proteinDomainServerButton);

		Object[][] data = new Object[1][5];
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

		getProteinDomains(node); // Fill in myProteinDomains collection based on the main Map
									// (Util.proteinDomainsMap)

		mainProteinDomainTable = new JTable(tableDataModel);
		// Create the scroll pane and add the table to it.
		proteinDomainTableScrollPanel = new JScrollPane();
		proteinDomainTableScrollPanel.setBounds(10, 215, 500, 90);
		proteinDomainTableScrollPanel.setViewportView(mainProteinDomainTable);
		proteinDomainTableScrollPanel.setRowHeaderView(rowHeader);
		mainPanel.add(proteinDomainTableScrollPanel);

		if (myProteinDomains != null && myProteinDomains.size() > 0) {
			data = new Object[myProteinDomains.size()][5];
			tableDataModel.setDataVector(data, columnNames);

			int countPtnDomain = 0;
			for (ProteinDomain domain : myProteinDomains) {
				tableDataModel.setValueAt(domain.name, countPtnDomain, 0);
				tableDataModel.setValueAt(domain.startId, countPtnDomain, 1);
				tableDataModel.setValueAt(domain.endId, countPtnDomain, 2);
				tableDataModel.setValueAt(domain.eValue, countPtnDomain, 3);
				Color color = domain.color;
				if (color != null) {

					String colorStr = color.getRed() + "#" + color.getGreen() + "#" + color.getBlue() + "#"
							+ color.getAlpha();
					tableDataModel.setValueAt(colorStr, countPtnDomain, 4);
				}
				countPtnDomain++;
			}
			setTableProperties(myProteinDomains.size());
		} else {
			setTableProperties(1);
		}

		mainProteinDomainTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int viewRow = mainProteinDomainTable.rowAtPoint(evt.getPoint());
				int viewColumn = mainProteinDomainTable.columnAtPoint(evt.getPoint());
				if (viewColumn == 4) {
					String currentColor = (String) tableDataModel.getValueAt(viewRow, viewColumn);
					Color initialcolor = Color.RED;
					if (currentColor != null && !currentColor.equals("")) {
						String[] cols = currentColor.split("#");
						initialcolor = new Color(Integer.parseInt(cols[0]), Integer.parseInt(cols[1]),
								Integer.parseInt(cols[2]), Integer.parseInt(cols[3]));
					}

					Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
					String colorStr = color.getRed() + "#" + color.getGreen() + "#" + color.getBlue() + "#"
							+ color.getAlpha();
					tableDataModel.setValueAt(colorStr, viewRow, viewColumn);
				}

			}
		});

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

		Icon iconBtnOk = new ImageIcon(getClass().getResource("/images/okBtn.png"));
		okButton = new JButton(iconBtnOk);
		okButton.setText("OK");

		if (Util.isWindows()) {
			okButton.setBounds(30, 325, 220, 25);
		} else {
			okButton.setBounds(30, 320, 220, 25);
		}

		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				okButton.setEnabled(false);
				proteinDomainServerButton.setEnabled(false);

				isPlotDone = false;
				LoadProteinDomainTask.isPlotDone = false;

				if (netView == null) {
					netView = cyApplicationManager.getCurrentNetworkView();
				}

				nodeView = netView.getNodeView(node);

				applyLayoutThread = new Thread() {

					public void run() {

						textLabel_status_result.setText("Setting node styles...");
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node styles...");
						Util.node_label_factor_size = (double) spinner_factor_size_node.getValue();
						Util.setNodeStyles(myNetwork, node, netView);
						taskMonitor.setProgress(0.2);

						textLabel_status_result.setText("Getting protein domains from table...");
						try {
							getNodeDomainsFromTable();
							taskMonitor.setProgress(0.4);
						} catch (Exception e2) {
							textLabel_status_result.setText(e2.getMessage());
							taskMonitor.showMessage(TaskMonitor.Level.WARN, e2.getMessage());
						}

						textLabel_status_result.setText("Setting protein domains to node...");
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting protein domains to node...");
						setNodeDomainColors();
						taskMonitor.setProgress(0.75);

						textLabel_status_result.setText("Setting styles to the edges...");
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting styles to the edges...");
						isPlotDone = false;
						isPlotDone = Util.addOrUpdateEdgesToNetwork(myNetwork, node, style, netView, nodeView,
								handleFactory, bendFactory, lexicon, Util.getProteinLengthScalingFactor(), intraLinks,
								interLinks, taskMonitor, textLabel_status_result);
						taskMonitor.setProgress(0.95);

						// Apply the change to the view
						style.apply(netView);
						netView.updateView();
						taskMonitor.setProgress(1.0);
						textLabel_status_result.setText("Done!");

						mainFrame.dispose();
					}
				};

				applyLayoutThread.start();

			}
		});
		mainPanel.add(okButton);

		Icon iconBtnCancel = new ImageIcon(getClass().getResource("/images/cancelBtn.png"));
		JButton cancelButton = new JButton(iconBtnCancel);
		cancelButton.setText("Cancel");

		if (Util.isWindows()) {
			cancelButton.setBounds(265, 325, 220, 25);
		} else {
			cancelButton.setBounds(265, 320, 220, 25);
		}

		cancelButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				boolean concluedProcess = true;

				if (!okButton.isEnabled()) {
					int input = JOptionPane.showConfirmDialog(null,
							"Style has not been finished yet. Do you want to close this window?",
							"XlinkCyNET - Single Node", JOptionPane.INFORMATION_MESSAGE);
					// 0=yes, 1=no, 2=cancel
					if (input == 0) {
						isPlotDone = true;
						concluedProcess = true;
						Util.stopUpdateViewer=true;
						if (applyLayoutThread != null) {
							applyLayoutThread.interrupt();
						}
					} else {
						isPlotDone = false;
						concluedProcess = false;
						Util.stopUpdateViewer = false;
					}
				} else
					concluedProcess = true;

				if (concluedProcess) {
					mainFrame.dispose();
				}
			}
		});
		mainPanel.add(cancelButton);

		Icon iconBtnRestoreStyle = new ImageIcon(getClass().getResource("/images/restore.png"));
		JButton restoreStyleButton = new JButton(iconBtnRestoreStyle);
		restoreStyleButton.setText("Restore style");

		if (Util.isWindows()) {
			restoreStyleButton.setBounds(390, 185, 120, 25);
		} else {
			restoreStyleButton.setBounds(390, 190, 120, 25);
		}

		restoreStyleButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				taskMonitor.setProgress(0.6);
				restoreDefaultStyle(taskMonitor);
				taskMonitor.setProgress(1.0);
				mainFrame.dispose();
			}
		});
		mainPanel.add(restoreStyleButton);
	}

	/**
	 * Method responsible for restoring basic node styles
	 * 
	 * @param taskMonitor
	 */
	private void clearNodeStyle(final TaskMonitor taskMonitor) {

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Restoring node layout...");
		nodeView.clearValueLock(BasicVisualLexicon.NODE_WIDTH);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_TRANSPARENCY);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_PAINT);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_LABEL_COLOR);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_SELECTED_PAINT);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_BORDER_WIDTH);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_BORDER_PAINT);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_HEIGHT);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_SHAPE);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_TOOLTIP);
		nodeView.clearValueLock(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);

		// ######################### NODE_LABEL_POSITION ######################

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Restoring node label position...");

		// Try to get the label visual property by its ID
		VisualProperty<?> vp_label_position = lexicon.lookup(CyNode.class, Util.NODE_LABEL_POSITION);
		if (vp_label_position != null) {
			nodeView.clearValueLock(vp_label_position);
		}
		// ######################### NODE_LABEL_POSITION ######################

		// ######################### NODE_COLOR_LINEAR_GRADIENT ######################
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Clearing all colors of node domains...");

		VisualProperty<CyCustomGraphics2> vp_node_linear_gradient = (VisualProperty<CyCustomGraphics2>) lexicon
				.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		if (vp_node_linear_gradient != null) {
			nodeView.clearValueLock(vp_node_linear_gradient);
		}

		// ############################################################################
	}

	/**
	 * Method responsible for restoring the layout of the selected node.
	 * 
	 * @param taskMonitor
	 */
	private void restoreDefaultStyle(final TaskMonitor taskMonitor) {

		isPlotDone = false;

		if (style == null) {
			return;
		}
		if (netView == null) {
			netView = cyApplicationManager.getCurrentNetworkView();
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein...");
		nodeView = netView.getNodeView(node);

		this.clearNodeStyle(taskMonitor);
		UpdateViewListener.isNodeModified = false;
		Util.restoreEdgesStyle(taskMonitor, myNetwork, cyApplicationManager, netView, handleFactory, bendFactory, node);

		// Apply the change to the view
		style.apply(netView);
		netView.updateView();

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done!");

		isPlotDone = true;
	}

	/**
	 * Get all domains assigned on the Table
	 * 
	 * @throws Exception
	 */
	private void getNodeDomainsFromTable() throws Exception {

		myProteinDomains = new ArrayList<ProteinDomain>();
		for (int row = 0; row < tableDataModel.getRowCount(); row++) {

			String domain = tableDataModel.getValueAt(row, 0) != null ? tableDataModel.getValueAt(row, 0).toString()
					: "";
			int startId = tableDataModel.getValueAt(row, 1) != null ? (int) tableDataModel.getValueAt(row, 1) : 0;
			int endId = tableDataModel.getValueAt(row, 2) != null ? (int) tableDataModel.getValueAt(row, 2) : 0;
			if (tableDataModel.getValueAt(row, 4) != null && !tableDataModel.getValueAt(row, 4).toString().equals("")) {
				String[] colorStr = tableDataModel.getValueAt(row, 4).toString().split("#");
				Color color = new Color(Integer.parseInt(colorStr[0]), Integer.parseInt(colorStr[1]),
						Integer.parseInt(colorStr[2]), 100);
				myProteinDomains.add(new ProteinDomain(domain, startId, endId, color));
			} else if (domain.equals("") && startId == 0 && endId == 0) {
				continue;
			} else {
				myProteinDomains.add(new ProteinDomain(domain, startId, endId, ""));
			}
		}

		if (myProteinDomains.size() == 0) {
			throw new Exception("WARNING: No domain has been found.");
		}
		Util.updateProteinDomainsColorMap(myProteinDomains);
	}

	/**
	 * Set all domains to a node
	 */
	private void setNodeDomainColors() {
		// ######################### NODE_COLOR_LINEAR_GRADIENT ######################
		boolean hasDomain = false;
		StringBuilder sb_domains = new StringBuilder();
		VisualProperty<CyCustomGraphics2<?>> vp_node_linear_gradient = (VisualProperty<CyCustomGraphics2<?>>) lexicon
				.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		if (vp_node_linear_gradient != null) {

			Map<String, Object> chartProps = new HashMap<String, Object>();
			List<java.awt.Color> colors = new ArrayList<java.awt.Color>();
			List<Float> values = new ArrayList<Float>();
			values.add(0.0f);
			colors.add(new Color(255, 255, 255, 100));

			if (myProteinDomains == null)
				return;

			for (ProteinDomain domain : myProteinDomains) {

				int startId = domain.startId;
				int endId = domain.endId;

				if (startId > Util.getProteinLength())
					continue;
				if (endId > Util.getProteinLength())
					endId = (int) Util.getProteinLength();

				float initial_range = ((float) startId / Util.getProteinLength());
				float initial_range_white = initial_range - 0.0001f >= 0.0 ? initial_range - 0.0001f : initial_range;

				if (initial_range_white == 0) {
					values.add(initial_range_white);
					values.add(initial_range + 0.0001f);

				} else {
					values.add(initial_range_white);
					values.add(initial_range);
				}
				colors.add(new Color(255, 255, 255, 100));
				if (domain.color == null) {
					colors.add(Util.proteinDomainsColorMap.get(domain.name));
				} else {
					colors.add(domain.color);
				}

				float end_range = ((float) endId / Util.getProteinLength());
				float end_range_white = end_range + 0.0001f <= 1.0 ? end_range + 0.0001f : end_range;

				if (end_range_white == 1.0) {
					values.add(end_range - 0.0001f);

				} else {
					values.add(end_range);
				}

				if (domain.color == null) {
					colors.add(Util.proteinDomainsColorMap.get(domain.name));
				} else {
					colors.add(domain.color);
				}
				values.add(end_range_white);
				colors.add(new Color(255, 255, 255, 100));

				sb_domains.append("<p>" + domain.name + " [" + startId + " - " + endId + "]</p>");
				hasDomain = true;
			}
			values.add(1.0f);
			colors.add(new Color(255, 255, 255, 100));
			chartProps.put("cy_gradientFractions", values);
			chartProps.put("cy_gradientColors", colors);

			if (Util.isProtein_expansion_horizontal)
				chartProps.put("cy_angle", 0.0);
			else
				chartProps.put("cy_angle", 270.0);

			CyCustomGraphics2<?> customGraphics = vgFactory.getInstance(chartProps);
			if (vp_node_linear_gradient != null)
				nodeView.setLockedValue(vp_node_linear_gradient, customGraphics);
		}

		if (hasDomain) {
			nodeView.setLockedValue(BasicVisualLexicon.NODE_TOOLTIP,
					"<html><p>Protein size: " + (int) Util.getProteinLength() + " residues</p><br/><p>Domains:</p>"
							+ sb_domains.toString() + "</html>");

			String network_name = myNetwork.toString();
			if (Util.proteinDomainsMap.containsKey(network_name)) {

				Map<Long, List<ProteinDomain>> all_proteinDomains = Util.proteinDomainsMap.get(network_name);
				all_proteinDomains.put(node.getSUID(), myProteinDomains);

			} else {// Network does not exists

				Map<Long, List<ProteinDomain>> proteinDomains = new HashMap<Long, List<ProteinDomain>>();
				proteinDomains.put(node.getSUID(), myProteinDomains);
				Util.proteinDomainsMap.put(network_name, proteinDomains);
			}

		} else
			nodeView.setLockedValue(BasicVisualLexicon.NODE_TOOLTIP,
					"<html><p>Protein size: " + Util.getProteinLength() + " residues</p></html>");
		// ############################### END ################################
	}

}