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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
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
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.fmp.liulab.internal.UpdateViewListener;
import de.fmp.liulab.internal.view.JTableRowRenderer;
import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.model.ProteinDomain;
import de.fmp.liulab.utils.Tuple2;
import de.fmp.liulab.utils.Util;

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

	private List<java.awt.Color> available_colors;

	private static View<CyNode> nodeView;
	private CyRow myCurrentRow;
	private List<CyNode> nodes;
	private boolean isCurrentNode_modified = false;

	private ArrayList<ProteinDomain> pFamProteinDomains;
	private ArrayList<ProteinDomain> myProteinDomains;

	// Window
	private JFrame mainFrame;
	private JPanel mainPanel;
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
	public static float proteinLength;
	public static CyNode node;

	private Thread pfamThread;
	private JButton pFamButton;

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
			mainFrame = new JFrame("XlinkCyNET - Single Node");

		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension appSize = null;
		if (Util.isWindows() || Util.isUnix()) {
			appSize = new Dimension(540, 365);
		} else {
			appSize = new Dimension(540, 345);
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
	}
	
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setTitle("XlinkCyNET - Layout task");
		// Write your own function here.
		if (cyApplicationManager.getCurrentNetwork() == null) {
			throw new Exception("ERROR: No network has been loaded.");
		}

		checkSingleOrMultipleSelectedNodes(taskMonitor);

	}

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

		if (netView == null) {
			netView = cyApplicationManager.getCurrentNetworkView();
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting the selected node...");
		nodeView = netView.getNodeView(node);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node styles...");
		setNodeStyles();
		taskMonitor.setProgress(0.2);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein domains...");
		getProteinDomains(node);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting protein domains to node...");
		setNodeDomainColors();
		taskMonitor.setProgress(0.75);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting styles on the edges...");
		isPlotDone = Util.addOrUpdateEdgesToNetwork(myNetwork, node, style, netView, nodeView, handleFactory,
				bendFactory, lexicon, proteinLength, intraLinks, interLinks, taskMonitor);
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
		proteinLength = (float) currentNodeWidth;

		/**
		 * Get intra and interlinks
		 */
		Tuple2 inter_and_intralinks = Util.getAllLinksFromAdjacentEdgesNode(node, myNetwork);
		interLinks = (ArrayList<CrossLink>) inter_and_intralinks.getFirst();
		intraLinks = (ArrayList<CrossLink>) inter_and_intralinks.getSecond();

		isCurrentNode_modified = Util.IsNodeModified(myNetwork, netView, style, node);

		/**
		 * Get possible domains
		 */

//			List<Color> nodeDomainColors = null;
//
//			VisualProperty<CyCustomGraphics2<?>> vp_node_linear_gradient = (VisualProperty<CyCustomGraphics2<?>>) lexicon
//					.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
//			if (vp_node_linear_gradient != null) {
//				try {
//					Map<String, Object> chartProps = nodeView.getVisualProperty(vp_node_linear_gradient)
//							.getProperties();
//					if (chartProps != null) {
//						for (Map.Entry<String, Object> entry : chartProps.entrySet()) {
//							if (entry.getKey().equals("cy_gradientColors")) {
//								nodeDomainColors = (List<Color>) entry.getValue();
//							}
//						}
//					}
//
//					myProteinDomains = new ArrayList<ProteinDomain>();
//					String[] node_toolTip = nodeView.getVisualProperty(BasicVisualLexicon.NODE_TOOLTIP)
//							.split("<p>|</p>");
//					int countColors = 2;
//					for (int i = 5; i < node_toolTip.length; i++) {
//						if (node_toolTip[i].startsWith("<") || node_toolTip[i].equals(""))
//							continue;
//						String[] cols = node_toolTip[i].split("\\[|\\]| - ");
//						// String domain, int startId, int endId, double eValue
//						myProteinDomains.add(new ProteinDomain(cols[0].trim(), Integer.parseInt(cols[1]),
//								Integer.parseInt(cols[2]), nodeDomainColors.get(countColors)));
//						countColors += 4;
//					}
//				} catch (Exception e) {
//				}
//			}

	}

	private static void updateRowHeader(int number_lines) {

		final String[] headers = new String[number_lines];
		for (int count = 0; count < number_lines; count++) {
			headers[count] = String.valueOf(count + 1);
		}

		ListModel lm = new AbstractListModel() {

			@Override
			public int getSize() {
				return headers.length;
			}

			@Override
			public Object getElementAt(int index) {
				return headers[index];
			}

		};

		rowHeader = new JList(lm);
		rowHeader.setFixedCellWidth(50);
		rowHeader.setFixedCellHeight(mainProteinDomainTable.getRowHeight());
		rowHeader.setCellRenderer(new JTableRowRenderer(mainProteinDomainTable));
		if (proteinDomainTableScrollPanel != null)
			proteinDomainTableScrollPanel.setRowHeaderView(rowHeader);
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

			updateRowHeader(number_lines);
		}
	}

	/**
	 * Set all labels in XLinkCyNET window / frame
	 */
	private void setFrameLabels() {

		JPanel protein_panel = new JPanel();
		protein_panel.setBorder(BorderFactory.createTitledBorder("Protein"));
		protein_panel.setBounds(10, 10, 250, 120);
		protein_panel.setLayout(null);
		mainPanel.add(protein_panel);

		JLabel textLabel_Protein_lbl = new JLabel("Name:");
		textLabel_Protein_lbl.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_lbl.setBounds(10, -20, 50, 100);
		protein_panel.add(textLabel_Protein_lbl);

		JLabel textLabel_Protein_result = new JLabel();
		textLabel_Protein_result.setText((String) myCurrentRow.getRaw(CyNetwork.NAME));
		textLabel_Protein_result.setFont(new java.awt.Font("Tahoma", Font.BOLD, 12));
		textLabel_Protein_result.setBounds(80, -20, 100, 100);
		protein_panel.add(textLabel_Protein_result);

		JLabel textLabel_Protein_size_lbl = new JLabel("Size:");
		textLabel_Protein_size_lbl.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_size_lbl.setBounds(10, 10, 70, 100);
		protein_panel.add(textLabel_Protein_size_lbl);

		JLabel textLabel_Protein_size_result = new JLabel();
		textLabel_Protein_size_result.setText((int) proteinLength + " residues");
		textLabel_Protein_size_result.setFont(new java.awt.Font("Tahoma", Font.BOLD, 12));
		textLabel_Protein_size_result.setBounds(80, 10, 100, 100);
		protein_panel.add(textLabel_Protein_size_result);

		JLabel textLabel_Protein_expansion_lbl = new JLabel("Expansion:");
		textLabel_Protein_expansion_lbl.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Protein_expansion_lbl.setBounds(10, 40, 70, 100);
		protein_panel.add(textLabel_Protein_expansion_lbl);

		JRadioButton protein_expansion_horizontal = new JRadioButton("Horizontal");
		protein_expansion_horizontal.setSelected(Util.isProtein_expansion_horizontal);
		if (Util.isWindows() || Util.isUnix()) {
			protein_expansion_horizontal.setBounds(75, 80, 90, 20);
		} else {
			protein_expansion_horizontal.setBounds(70, 80, 105, 20);
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
		protein_expansion_vertical.setSelected(!Util.isProtein_expansion_horizontal);
		protein_expansion_vertical.setBounds(165, 80, 80, 20);
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
		ButtonGroup bg = new ButtonGroup();
		bg.add(protein_expansion_horizontal);
		bg.add(protein_expansion_vertical);

		JLabel textLabel_Pfam = new JLabel("Search for domains in Pfam database:");
		textLabel_Pfam.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_Pfam.setBounds(10, 95, 300, 100);
		mainPanel.add(textLabel_Pfam);

		textLabel_status_result = new JLabel("???");
		textLabel_status_result.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status_result.setForeground(new Color(159, 17, 17));
		textLabel_status_result.setBounds(90, 120, 350, 100);

		JPanel logo_panel = new JPanel();
		logo_panel.setBorder(BorderFactory.createTitledBorder(""));
		logo_panel.setBounds(265, 16, 245, 112);
		logo_panel.setLayout(null);
		mainPanel.add(logo_panel);

		JLabel jLabelIcon = new JLabel();
		jLabelIcon.setBounds(70, -95, 300, 300);
		jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png")));
		logo_panel.add(jLabelIcon);

		JLabel textLabel_status = new JLabel("Status:");
		textLabel_status.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_status.setBounds(10, 120, 50, 100);
		mainPanel.add(textLabel_status);
		mainPanel.add(textLabel_status_result);

		JLabel textLabel_required_fields = new JLabel("(*) Required fields");
		textLabel_required_fields.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 10));
		textLabel_required_fields.setBounds(10, 232, 150, 100);
		mainPanel.add(textLabel_required_fields);
	}

	/**
	 * Set all objects to the main Frame
	 * 
	 * @param taskMonitor
	 */
	private void setFrameObjects(final TaskMonitor taskMonitor) {

		setFrameLabels();

		Icon iconBtn = new ImageIcon(getClass().getResource("/images/browse_Icon.png"));
		pFamButton = new JButton(iconBtn);
		pFamButton.setBounds(228, 130, 30, 30);
		pFamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				taskMonitor.setTitle("XL interactions");
				pFamButton.setEnabled(false);
				try {

					textLabel_status_result.setText("Accessing Pfam database...");
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "Accessing Pfam database...");
					pfamThread = new Thread() {
						public void run() {
							taskMonitor.setTitle("XL interactions");

							textLabel_status_result.setText("Getting protein domains...");
							taskMonitor.showMessage(TaskMonitor.Level.INFO, "Getting protein domains...");
							pFamProteinDomains = Util.getProteinDomains(myCurrentRow);
							taskMonitor.setProgress(0.4);
							if (pFamProteinDomains.size() > 0)
								textLabel_status_result.setText("Done!");
							else {
								textLabel_status_result.setText("WARNING: Check Task History.");
								taskMonitor.showMessage(TaskMonitor.Level.WARN, "No protein domain has been found for '"
										+ myNetwork.getDefaultNodeTable().getRow(node.getSUID()).getRaw(CyNetwork.NAME)
										+ "'.");
							}

							Object[][] data = new Object[pFamProteinDomains.size()][5];
							tableDataModel.setDataVector(data, columnNames);

							int countPtnDomain = 0;
							for (ProteinDomain domain : pFamProteinDomains) {
								tableDataModel.setValueAt(domain.name, countPtnDomain, 0);
								tableDataModel.setValueAt(domain.startId, countPtnDomain, 1);
								tableDataModel.setValueAt(domain.endId, countPtnDomain, 2);
								tableDataModel.setValueAt(domain.eValue, countPtnDomain, 3);
								countPtnDomain++;
							}

							setTableProperties(pFamProteinDomains.size());
							pFamButton.setEnabled(true);
						}
					};

					pfamThread.start();

				} catch (Exception exception) {
				}
			}
		});
		mainPanel.add(pFamButton);

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
				updateRowHeader(tableDataModel.getRowCount());
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
						updateRowHeader(tableDataModel.getRowCount());
					}
				}

				textLabel_status_result.setText("Row has been deleted.");
			}
		};

		KeyStroke keyStrokeDeleteLine = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
		mainProteinDomainTable.getActionMap().put("deleteLineToTable", deleteLineToTableAction);
		mainProteinDomainTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStrokeDeleteLine,
				"deleteLineToTable");

		// Create the scroll pane and add the table to it.
		proteinDomainTableScrollPanel = new JScrollPane();
		proteinDomainTableScrollPanel.setBounds(10, 185, 500, 90);
		proteinDomainTableScrollPanel.setViewportView(mainProteinDomainTable);
		proteinDomainTableScrollPanel.setRowHeaderView(rowHeader);
		mainPanel.add(proteinDomainTableScrollPanel);

		Icon iconBtnOk = new ImageIcon(getClass().getResource("/images/okBtn.png"));
		JButton okButton = new JButton(iconBtnOk);
		okButton.setText("OK");

		if (Util.isWindows() || Util.isUnix()) {
			okButton.setBounds(30, 295, 220, 25);
		} else {
			okButton.setBounds(30, 290, 220, 25);
		}

		okButton.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				isPlotDone = false;
				LoadProteinDomainTask.isPlotDone = false;

				if (netView == null) {
					netView = cyApplicationManager.getCurrentNetworkView();
				}

				nodeView = netView.getNodeView(node);

				textLabel_status_result.setText("Setting node styles...");
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node styles...");
				setNodeStyles();
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

				textLabel_status_result.setText("Setting styles on the edges...");
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting styles on the edges...");
				isPlotDone = false;
				isPlotDone = Util.addOrUpdateEdgesToNetwork(myNetwork, node, style, netView, nodeView, handleFactory,
						bendFactory, lexicon, proteinLength, intraLinks, interLinks, taskMonitor);
				taskMonitor.setProgress(0.95);

				// Apply the change to the view
				style.apply(netView);
				netView.updateView();
				taskMonitor.setProgress(1.0);
				textLabel_status_result.setText("Done!");

				mainFrame.dispose();
			}
		});
		mainPanel.add(okButton);

		Icon iconBtnCancel = new ImageIcon(getClass().getResource("/images/cancelBtn.png"));
		JButton cancelButton = new JButton(iconBtnCancel);
		cancelButton.setText("Cancel");

		if (Util.isWindows() || Util.isUnix()) {
			cancelButton.setBounds(265, 295, 220, 25);
		} else {
			cancelButton.setBounds(265, 290, 220, 25);
		}

		cancelButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				mainFrame.dispose();
			}
		});
		mainPanel.add(cancelButton);

		Icon iconBtnRestoreStyle = new ImageIcon(getClass().getResource("/images/restore.png"));
		JButton restoreStyleButton = new JButton(iconBtnRestoreStyle);
		restoreStyleButton.setText("Restore style");

		if (Util.isWindows() || Util.isUnix()) {
			restoreStyleButton.setBounds(390, 155, 120, 25);
		} else {
			restoreStyleButton.setBounds(390, 160, 120, 25);
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
	 * Method responsible for restoring edges style
	 * 
	 * @param taskMonitor
	 */
	private void restoreEdgesStyle(final TaskMonitor taskMonitor) {

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Restoring edges...");

		boolean IsModified_source_node = false;
		boolean IsModified_target_node = false;
		boolean IsIntraLink = false;

		int total_edges = 0;
		int old_progress = 0;
		int summary_processed = 0;
		if (taskMonitor != null)
			total_edges = myNetwork.getAdjacentEdgeList(node, CyEdge.Type.ANY).size();

		for (CyEdge edge : myNetwork.getAdjacentEdgeIterable(node, CyEdge.Type.ANY)) {

			// Check if the edge was inserted by this app
			String edge_name = myNetwork.getDefaultEdgeTable().getRow(edge.getSUID()).get(CyNetwork.NAME, String.class);

			CyNode sourceNode = myNetwork.getEdge(edge.getSUID()).getSource();
			CyNode targetNode = myNetwork.getEdge(edge.getSUID()).getTarget();

			if (sourceNode.getSUID() == targetNode.getSUID()) {
				IsIntraLink = true;
			} else {
				IsIntraLink = false;
			}
			IsModified_source_node = Util.IsNodeModified(myNetwork, netView, style, sourceNode);
			IsModified_target_node = Util.IsNodeModified(myNetwork, netView, style, targetNode);
			if (!edge_name.startsWith("[Source:")) {// original edges

				if (IsIntraLink) {
					View<CyEdge> currentEdgeView = netView.getEdgeView(edge);
					currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);
				} else if (!IsModified_source_node && !IsModified_target_node) {
					View<CyEdge> currentEdgeView = netView.getEdgeView(edge);
					currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);
				}
			} else { // created edges

				if (IsIntraLink) {
					View<CyEdge> currentEdgeView = netView.getEdgeView(edge);
					currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);

				} else if (!IsModified_source_node && !IsModified_target_node) {
					View<CyEdge> currentEdgeView = netView.getEdgeView(edge);
					currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
				}
			}

			if (taskMonitor != null) {
				summary_processed++;
				int new_progress = (int) ((double) summary_processed / (total_edges) * 100);
				if (new_progress > old_progress) {
					old_progress = new_progress;

					taskMonitor.showMessage(TaskMonitor.Level.INFO, "Restoring edges styles: " + old_progress + "%");
				}
			}
		}

		if (interLinks.size() > 0) {
			if (taskMonitor != null) {
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting styles on the inter link edges: 95%");
			}
			Util.updateAllAssiciatedInterlinkNodes(myNetwork, cyApplicationManager, netView, handleFactory, bendFactory,
					node);
		}
		if (intraLinks.size() > 0) {
			if (taskMonitor != null) {
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting styles on the intra link edges: 99%");
			}
			removeAllIntraLinks();
		}

		// ######################### UPDATE EDGES #########################
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
		this.restoreEdgesStyle(taskMonitor);

		// Apply the change to the view
		style.apply(netView);
		netView.updateView();

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done!");

		isPlotDone = true;
	}

	/**
	 * Method responsible for removing all intralinks of a node when the layout is
	 * restored
	 */
	private void removeAllIntraLinks() {

		CyNode current_node_source = null;
		CyNode current_node_target = null;
		CyEdge current_edge_intra = null;

		Set<CyNode> removeNodes = new HashSet<CyNode>();
		Set<CyEdge> removeEdges = new HashSet<CyEdge>();
		for (int countEdge = 0; countEdge < intraLinks.size(); countEdge++) {

			final String egde_name_added_by_app = "Edge" + countEdge + " [Source: "
					+ intraLinks.get(countEdge).protein_a + " (" + intraLinks.get(countEdge).pos_site_a + ")] [Target: "
					+ intraLinks.get(countEdge).protein_b + " (" + intraLinks.get(countEdge).pos_site_b + ")]";

			current_edge_intra = Util.getEdge(myNetwork, egde_name_added_by_app);
			if (current_edge_intra != null) {

				removeEdges.add(current_edge_intra);
			}

			final String node_name_source = intraLinks.get(countEdge).protein_a + " ["
					+ intraLinks.get(countEdge).pos_site_a + " - " + intraLinks.get(countEdge).pos_site_b
					+ "] - Source";

			current_node_source = Util.getNode(myNetwork, node_name_source);
			if (current_node_source != null) {

				removeNodes.add(current_node_source);
			}

			final String node_name_target = intraLinks.get(countEdge).protein_a + " ["
					+ intraLinks.get(countEdge).pos_site_a + " - " + intraLinks.get(countEdge).pos_site_b
					+ "] - Target";

			current_node_target = Util.getNode(myNetwork, node_name_target);
			if (current_node_target != null) {

				removeNodes.add(current_node_target);
			}
		}

		myNetwork.removeNodes(removeNodes);
		myNetwork.removeEdges(removeEdges);
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

			int index_domain = 0;
			if (myProteinDomains == null)
				return;

			for (ProteinDomain domain : myProteinDomains) {

				int startId = domain.startId;
				int endId = domain.endId;

				if (startId > proteinLength)
					continue;
				if (endId > proteinLength)
					endId = (int) proteinLength;

				float initial_range = ((float) startId / proteinLength);
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
					colors.add(available_colors.get(index_domain % 12));
				} else {
					colors.add(domain.color);
				}

				float end_range = ((float) endId / proteinLength);
				float end_range_white = end_range + 0.0001f <= 1.0 ? end_range + 0.0001f : end_range;

				if (end_range_white == 1.0) {
					values.add(end_range - 0.0001f);

				} else {
					values.add(end_range);
				}

				if (domain.color == null) {
					colors.add(available_colors.get(index_domain % 12));
					index_domain++;
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
			nodeView.setLockedValue(BasicVisualLexicon.NODE_TOOLTIP, "<html><p>Protein size: " + (int) proteinLength
					+ " residues</p><br/><p>Domains:</p>" + sb_domains.toString() + "</html>");

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
					"<html><p>Protein size: " + proteinLength + " residues</p></html>");
		// ############################### END ################################
	}

	/**
	 * Set style to node
	 */
	private void setNodeStyles() {

		if (Util.isProtein_expansion_horizontal) {
			nodeView.setLockedValue(BasicVisualLexicon.NODE_WIDTH, ((Number) proteinLength).doubleValue());
			nodeView.setLockedValue(BasicVisualLexicon.NODE_HEIGHT, 15d);
		} else {
			nodeView.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 15d);
			nodeView.setLockedValue(BasicVisualLexicon.NODE_HEIGHT, ((Number) proteinLength).doubleValue());
		}

		nodeView.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, 200);
		nodeView.setLockedValue(BasicVisualLexicon.NODE_PAINT, Color.WHITE);
		nodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.GRAY);
		nodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, Util.node_label_font_size);
		nodeView.setLockedValue(BasicVisualLexicon.NODE_SELECTED_PAINT, new Color(255, 255, 255, 165));
		nodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1.5d);
		nodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, Util.NodeBorderColor);
		nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);

		// ######################### NODE_LABEL_POSITION ######################

		// Try to get the label visual property by its ID
		VisualProperty<?> vp_label_position = lexicon.lookup(CyNode.class, Util.NODE_LABEL_POSITION);
		if (vp_label_position != null) {

			// If the property is supported by this rendering engine,
			// use the serialization string value to create the actual property value

			int ptn_label_length = myNetwork.getDefaultNodeTable().getRow(node.getSUID()).getRaw(CyNetwork.NAME)
					.toString().length();
			ptn_label_length *= 9;
			Object position = null;
			if (Util.isProtein_expansion_horizontal)
				position = vp_label_position.parseSerializableString("W,W,c,-" + ptn_label_length + ".00,0.00");
			else
				position = (ObjectPosition) vp_label_position.parseSerializableString("N,S,c,0.00,0.00");

			// If the parsed value is ok, apply it to the visual style
			// as default value or a visual mapping

			if (position != null)
				nodeView.setLockedValue(vp_label_position, position);

		}
		// ######################### NODE_LABEL_POSITION ######################

	}
}