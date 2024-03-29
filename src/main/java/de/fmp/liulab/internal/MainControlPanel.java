package de.fmp.liulab.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualStyle;

import de.fmp.liulab.core.ConfigurationManager;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for controlling all layout parameters
 * 
 * @author diogobor
 *
 */
public class MainControlPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = 8292806967891823933L;
	private JPanel link_panel;
	private JPanel link_score_panel;
	private JPanel link_legend_panel;
	private JPanel node_panel;
	private JPanel node_border_panel;
	private JPanel pymol_panel;

	private static JButton intraLinkColorButton;
	private static JButton interLinkColorButton;
	private static JButton ptmColorButton;
	private static JButton monolinkColorButton;
	private static JButton borderNodeColorButton;
	private static JCheckBox show_inter_link;
	private static JCheckBox show_intra_link;
	private static JCheckBox show_ptms;
	private static JCheckBox show_monolinks;

	private static JSpinner spinner_font_size_link_legend;
	private static JSpinner spinner_opacity_edge_label;
	private static JSpinner spinner_opacity_edge_link;
	private static JSpinner spinner_width_edge_link;
	private static JSpinner spinner_score_intralink;
	private static JSpinner spinner_score_interlink;
	private static JSpinner spinner_score_combinedlink;
	private static JSpinner spinner_font_size_node;
	private static JSpinner spinner_opacity_node_border;
	private static JSpinner spinner_width_node_border;

	private static JLabel pymolPathStr;
	private static JCheckBox show_links_legend;

	private static JCheckBox useAlphaFold;
	private static JCheckBox useCustomizedPDB;

	private Properties XlinkCyNETProps;

	// Update nodes and edges
	public static CyNetwork myNetwork;
	public static CyNetworkView netView;
	public static VisualStyle style;
	public static HandleFactory handleFactory;
	public static BendFactory bendFactory;
	public static VisualLexicon lexicon;

	/**
	 * Constructor
	 * 
	 * @param XlinkCyNETProps setting properties
	 * @param cm              configuration manager
	 */
	public MainControlPanel(Properties XlinkCyNETProps, ConfigurationManager cm) {

		this.XlinkCyNETProps = XlinkCyNETProps;
		this.load_default_parameters(cm);
		this.setFrameObjects();
		this.setVisible(true);
	}

	/**
	 * Load default parameters to main panel
	 * 
	 * @param bc main context
	 * @param cm configuration manager
	 */
	private void load_default_parameters(ConfigurationManager cm) {

		String propertyValue = "";
		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.showLinksLegend");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.showLinksLegend");
		Util.showLinksLegend = Boolean.parseBoolean(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.showIntraLinks");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.showIntraLinks");
		Util.showIntraLinks = Boolean.parseBoolean(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.showInterLinks");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.showInterLinks");
		Util.showInterLinks = Boolean.parseBoolean(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.edge_label_font_size");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.edge_label_font_size");
		Util.edge_label_font_size = Integer.parseInt(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.node_label_font_size");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.node_label_font_size");
		Util.node_label_font_size = Integer.parseInt(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.edge_label_opacity");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.edge_label_opacity");
		Util.edge_label_opacity = Integer.parseInt(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.edge_link_opacity");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.edge_link_opacity");
		Util.edge_link_opacity = Integer.parseInt(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.node_border_opacity");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.node_border_opacity");
		Util.node_border_opacity = Integer.parseInt(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.edge_link_width");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.edge_link_width");
		Util.edge_link_width = Double.parseDouble(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.node_border_width");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.node_border_width");
		Util.node_border_width = Double.parseDouble(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.IntraLinksColor");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.IntraLinksColor");
		Util.IntraLinksColor = stringToColor(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.InterLinksColor");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.InterLinksColor");
		Util.InterLinksColor = stringToColor(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.NodeBorderColor");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.NodeBorderColor");
		Util.NodeBorderColor = stringToColor(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.intralink_threshold_score");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.intralink_threshold_score");
		Util.intralink_threshold_score = Double.parseDouble(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.interlink_threshold_score");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.interlink_threshold_score");
		Util.interlink_threshold_score = Double.parseDouble(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.combinedlink_threshold_score");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.combinedlink_threshold_score");
		Util.combinedlink_threshold_score = Double.parseDouble(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.pymol_path");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.pymol_path");
		Util.PYMOL_PATH = propertyValue;

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.showPTMs");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.showPTMs");
		Util.showPTMs = Boolean.parseBoolean(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.PTMColor");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.PTMColor");
		Util.PTMColor = stringToColor(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.showMonolinkedPeptides");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.showMonolinkedPeptides");
		Util.showMonolinkedPeptides = Boolean.parseBoolean(propertyValue);

		if (cm == null)
			propertyValue = ((Properties) XlinkCyNETProps).getProperty("xlinkcynet.MonoLinksPeptideColor");
		else
			propertyValue = cm.getProperties().getProperty("xlinkcynet.MonoLinksPeptideColor");
		Util.MonoLinksPeptideColor = stringToColor(propertyValue);

	}

	/**
	 * Method responsible for checking if the PyMOL is correct for a specific OS.
	 */
	private void checkPyMOLname() {

		if (Util.isWindows()) {
			if (Util.PYMOL_PATH.endsWith(".exe")) {
				pymolPathStr.setText(Util.PYMOL_PATH);
			} else {
				pymolPathStr.setText("pymol");
				Util.PYMOL_PATH = "pymol";
			}
		} else if (Util.isMac()) {
			if (Util.PYMOL_PATH.endsWith(".app")) {
				pymolPathStr.setText(Util.PYMOL_PATH);
			} else {
				pymolPathStr.setText("/Applications/PyMOL.app");
				Util.PYMOL_PATH = "/Applications/PyMOL.app";
			}
		} else if (Util.isUnix()) {
			if (Util.PYMOL_PATH.endsWith("pymol")) {
				pymolPathStr.setText(Util.PYMOL_PATH);
			} else {
				pymolPathStr.setText("pymol");
				Util.PYMOL_PATH = "pymol";
			}
		}
	}

	/**
	 * Converter string to color
	 * 
	 * @param color_string string color
	 * @return color object
	 */
	private Color stringToColor(String color_string) {

		Color color = null;
		String[] cols = color_string.split("#");
		color = new Color(Integer.parseInt(cols[0]), Integer.parseInt(cols[1]), Integer.parseInt(cols[2]), 255);

		return color;

	}

	/**
	 * Method responsible for initializing buttons of link panel
	 * 
	 * @param offset_x     offset x
	 * @param button_width button width
	 */
	private void init_link_color_buttons(int offset_x, int button_width) {

		int offset_y = 25;

		link_panel = new JPanel();
		link_panel.setBackground(Color.WHITE);
		link_panel.setBorder(BorderFactory.createTitledBorder("Link"));
		link_panel.setLayout(null);

		intraLinkColorButton = new JButton();
		intraLinkColorButton.setBounds(offset_x, offset_y, button_width, 15);
		intraLinkColorButton.setBackground(Util.IntraLinksColor);
		intraLinkColorButton.setForeground(Util.IntraLinksColor);
		intraLinkColorButton.setOpaque(true);
		intraLinkColorButton.setBorderPainted(false);
		intraLinkColorButton.setToolTipText("Value: R:" + Util.IntraLinksColor.getRed() + " G:"
				+ Util.IntraLinksColor.getGreen() + " B:" + Util.IntraLinksColor.getBlue() + " - "
				+ String.format("#%02X%02X%02X", Util.IntraLinksColor.getRed(), Util.IntraLinksColor.getGreen(),
						Util.IntraLinksColor.getBlue()));

		intraLinkColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		intraLinkColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (intraLinkColorButton.isEnabled()) {
					Color initialcolor = intraLinkColorButton.getBackground();
					Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
					if (color == null)
						color = initialcolor;
					intraLinkColorButton.setBackground(color);
					intraLinkColorButton.setForeground(color);
					intraLinkColorButton.setOpaque(true);
					intraLinkColorButton.setBorderPainted(false);
					Util.IntraLinksColor = color;
					XlinkCyNETProps.setProperty("xlinkcynet.IntraLinksColor",
							color.getRed() + "#" + color.getGreen() + "#" + color.getBlue());

					intraLinkColorButton.setToolTipText("Value: R:" + Util.IntraLinksColor.getRed() + " G:"
							+ Util.IntraLinksColor.getGreen() + " B:" + Util.IntraLinksColor.getBlue() + " - "
							+ String.format("#%02X%02X%02X", Util.IntraLinksColor.getRed(),
									Util.IntraLinksColor.getGreen(), Util.IntraLinksColor.getBlue()));

					if (myNetwork != null && netView != null) {
						Util.updateEdgesStyle(myNetwork, netView);
					}
				}
			}
		});
		link_panel.add(intraLinkColorButton);
		offset_y += 30;

		interLinkColorButton = new JButton();
		interLinkColorButton.setBounds(offset_x, offset_y, button_width, 15);
		interLinkColorButton.setBackground(Util.InterLinksColor);
		interLinkColorButton.setForeground(Util.InterLinksColor);
		interLinkColorButton.setOpaque(true);
		interLinkColorButton.setBorderPainted(false);
		interLinkColorButton.setToolTipText("Value: R:" + Util.InterLinksColor.getRed() + " G:"
				+ Util.InterLinksColor.getGreen() + " B:" + Util.InterLinksColor.getBlue() + " - "
				+ String.format("#%02X%02X%02X", Util.InterLinksColor.getRed(), Util.InterLinksColor.getGreen(),
						Util.InterLinksColor.getBlue()));
		interLinkColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		interLinkColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (interLinkColorButton.isEnabled()) {
					Color initialcolor = interLinkColorButton.getBackground();
					Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
					if (color == null)
						color = initialcolor;
					interLinkColorButton.setBackground(color);
					interLinkColorButton.setForeground(color);
					interLinkColorButton.setOpaque(true);
					interLinkColorButton.setBorderPainted(false);
					Util.InterLinksColor = color;
					XlinkCyNETProps.setProperty("xlinkcynet.InterLinksColor",
							color.getRed() + "#" + color.getGreen() + "#" + color.getBlue());

					interLinkColorButton.setToolTipText("Value: R:" + Util.InterLinksColor.getRed() + " G:"
							+ Util.InterLinksColor.getGreen() + " B:" + Util.InterLinksColor.getBlue() + " - "
							+ String.format("#%02X%02X%02X", Util.InterLinksColor.getRed(),
									Util.InterLinksColor.getGreen(), Util.InterLinksColor.getBlue()));

					if (myNetwork != null && netView != null) {
						Util.updateEdgesStyle(myNetwork, netView);
					}
				}
			}
		});
		link_panel.add(interLinkColorButton);
		offset_y += 30;

		ptmColorButton = new JButton();
		ptmColorButton.setBounds(offset_x, offset_y, button_width, 15);
		ptmColorButton.setBackground(Util.PTMColor);
		ptmColorButton.setForeground(Util.PTMColor);
		ptmColorButton.setOpaque(true);
		ptmColorButton.setBorderPainted(false);
		ptmColorButton.setToolTipText("Value: R:" + Util.PTMColor.getRed() + " G:" + Util.PTMColor.getGreen() + " B:"
				+ Util.PTMColor.getBlue() + " - " + String.format("#%02X%02X%02X", Util.PTMColor.getRed(),
						Util.PTMColor.getGreen(), Util.PTMColor.getBlue()));

		ptmColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		ptmColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (ptmColorButton.isEnabled()) {
					Color initialcolor = ptmColorButton.getBackground();
					Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
					if (color == null)
						color = initialcolor;
					ptmColorButton.setBackground(color);
					ptmColorButton.setForeground(color);
					ptmColorButton.setOpaque(true);
					ptmColorButton.setBorderPainted(false);
					Util.PTMColor = color;
					XlinkCyNETProps.setProperty("xlinkcynet.PtmLinksColor",
							color.getRed() + "#" + color.getGreen() + "#" + color.getBlue());

					ptmColorButton
							.setToolTipText("Value: R:" + Util.PTMColor.getRed() + " G:" + Util.PTMColor.getGreen()
									+ " B:" + Util.PTMColor.getBlue() + " - " + String.format("#%02X%02X%02X",
											Util.PTMColor.getRed(), Util.PTMColor.getGreen(), Util.PTMColor.getBlue()));

					if (myNetwork != null && netView != null) {
						Util.setPTMStyleForAllNodes(myNetwork, netView);
					}
				}
			}
		});
		link_panel.add(ptmColorButton);
		offset_y += 30;

		monolinkColorButton = new JButton();
		monolinkColorButton.setBounds(offset_x, offset_y, button_width, 15);
		monolinkColorButton.setBackground(Util.MonoLinksPeptideColor);
		monolinkColorButton.setForeground(Util.MonoLinksPeptideColor);
		monolinkColorButton.setOpaque(true);
		monolinkColorButton.setBorderPainted(false);
		monolinkColorButton.setToolTipText("Value: R:" + Util.MonoLinksPeptideColor.getRed() + " G:"
				+ Util.MonoLinksPeptideColor.getGreen() + " B:" + Util.MonoLinksPeptideColor.getBlue() + " - "
				+ String.format("#%02X%02X%02X", Util.MonoLinksPeptideColor.getRed(),
						Util.MonoLinksPeptideColor.getGreen(), Util.MonoLinksPeptideColor.getBlue()));

		monolinkColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		monolinkColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (monolinkColorButton.isEnabled()) {
					Color initialcolor = monolinkColorButton.getBackground();
					Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
					if (color == null)
						color = initialcolor;
					monolinkColorButton.setBackground(color);
					monolinkColorButton.setForeground(color);
					monolinkColorButton.setOpaque(true);
					monolinkColorButton.setBorderPainted(false);
					Util.MonoLinksPeptideColor = color;
					XlinkCyNETProps.setProperty("xlinkcynet.MonoLinksPeptideColor",
							color.getRed() + "#" + color.getGreen() + "#" + color.getBlue());

					monolinkColorButton.setToolTipText("Value: R:" + Util.MonoLinksPeptideColor.getRed() + " G:"
							+ Util.MonoLinksPeptideColor.getGreen() + " B:" + Util.MonoLinksPeptideColor.getBlue()
							+ " - " + String.format("#%02X%02X%02X", Util.MonoLinksPeptideColor.getRed(),
									Util.MonoLinksPeptideColor.getGreen(), Util.MonoLinksPeptideColor.getBlue()));

					if (myNetwork != null && netView != null) {
						Util.setMonolinkPeptidesStyle(myNetwork, netView);
					}
				}
			}
		});
		link_panel.add(monolinkColorButton);

	}

	/**
	 * Method responsible for initializing link style features
	 * 
	 * @param offset_x     offset x
	 * @param button_width button width
	 */
	private void init_link_style_features(int offset_x, int button_width) {

		int offset_y = 132;

		JLabel opacity_edge_link = new JLabel("Opacity:");
		opacity_edge_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		opacity_edge_link.setBounds(10, offset_y, 100, 40);
		link_panel.add(opacity_edge_link);
		offset_y += 30;

		JLabel width_edge_link_label = new JLabel("Width:");
		width_edge_link_label.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		width_edge_link_label.setBounds(10, offset_y, 100, 40);
		link_panel.add(width_edge_link_label);

		offset_y = 145;
		offset_x = 140;
		if (Util.isWindows())
			offset_x = 135;
		SpinnerModel model_opacity_edge_link = new SpinnerNumberModel(Util.edge_link_opacity.intValue(), // initial
				// value
				0, // min
				255, // max
				1); // step
		spinner_opacity_edge_link = new JSpinner(model_opacity_edge_link);
		spinner_opacity_edge_link.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_opacitiy_edge_link = spinner_opacity_edge_link.getEditor();
		JFormattedTextField field_opacity_edge_link = (JFormattedTextField) comp_opacitiy_edge_link.getComponent(0);
		DefaultFormatter formatter_opacity_edge_link = (DefaultFormatter) field_opacity_edge_link.getFormatter();
		formatter_opacity_edge_link.setCommitsOnValidEdit(true);
		spinner_opacity_edge_link.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_link_opacity = (Integer) spinner_opacity_edge_link.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.edge_link_opacity", Util.edge_link_opacity.toString());

				if (myNetwork != null && netView != null) {
					Util.updateEdgesStyle(myNetwork, netView);
				}
			}
		});
		spinner_opacity_edge_link.setToolTipText("Set a value between 0 (transparent) and 255 (opaque).");
		link_panel.add(spinner_opacity_edge_link);
		offset_y += 30;

		SpinnerModel width_edge_link = new SpinnerNumberModel(Util.edge_link_width, // initial
				// value
				1, // min
				10.1, // max
				0.1); // step
		spinner_width_edge_link = new JSpinner(width_edge_link);
		spinner_width_edge_link.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_width_edge_link = spinner_width_edge_link.getEditor();
		JFormattedTextField field_width_edge_link = (JFormattedTextField) comp_width_edge_link.getComponent(0);
		DefaultFormatter formatter_width_edge_link = (DefaultFormatter) field_width_edge_link.getFormatter();
		formatter_width_edge_link.setCommitsOnValidEdit(true);
		spinner_width_edge_link.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_link_width = (double) spinner_width_edge_link.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.edge_link_width", String.valueOf(Util.edge_link_width));

				if (myNetwork != null && netView != null) {
					Util.updateEdgesStyle(myNetwork, netView);
				}
			}
		});
		spinner_width_edge_link.setToolTipText("Set a value between 1 and 10.");
		link_panel.add(spinner_width_edge_link);

	}

	/**
	 * Method responsible for initializing log score panel
	 * 
	 * @param offset_x     offset x
	 * @param button_width button width
	 */
	private void init_link_log_score_features(int offset_x, int button_width) {

		int offset_y = 10;
		link_score_panel = new JPanel();
		link_score_panel.setBackground(Color.WHITE);
		link_score_panel.setBorder(BorderFactory.createTitledBorder("-Log(Score)"));
		link_score_panel.setBounds(10, 200, 230, 115);
		link_score_panel.setLayout(null);
		link_panel.add(link_score_panel);

		JLabel score_intralink = new JLabel("Intralink:");
		score_intralink.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		score_intralink.setBounds(10, offset_y, 100, 40);
		link_score_panel.add(score_intralink);
		offset_y += 30;

		JLabel score_interlink = new JLabel("Interlink:");
		score_interlink.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		score_interlink.setBounds(10, offset_y, 100, 40);
		link_score_panel.add(score_interlink);
		offset_y += 30;

		JLabel score_combinedlink = new JLabel("PPI link:");
		score_combinedlink.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		score_combinedlink.setBounds(10, offset_y, 100, 40);
		link_score_panel.add(score_combinedlink);

		offset_y = 20;
		offset_x -= 10;
		if (!Util.isWindows())// MacOS or Unix
			offset_x -= 5;

		SpinnerModel model_intralink_spinner = new SpinnerNumberModel(Util.intralink_threshold_score, // initial
				// value
				0, // min
				500, // max
				0.1); // step
		spinner_score_intralink = new JSpinner(model_intralink_spinner);
		spinner_score_intralink.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_score_intra_link = spinner_score_intralink.getEditor();
		JFormattedTextField field_score_intra_link = (JFormattedTextField) comp_score_intra_link.getComponent(0);
		DefaultFormatter formatter_score_intra_link = (DefaultFormatter) field_score_intra_link.getFormatter();
		formatter_score_intra_link.setCommitsOnValidEdit(true);
		spinner_score_intralink.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.intralink_threshold_score = (double) spinner_score_intralink.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.intralink_threshold_score",
						String.valueOf(Util.intralink_threshold_score));

				if (myNetwork != null && netView != null) {
					Util.updateEdgesStyle(myNetwork, netView);
				}
			}
		});
		link_score_panel.add(spinner_score_intralink);
		offset_y += 30;

		SpinnerModel model_interlink_spinner = new SpinnerNumberModel(Util.interlink_threshold_score, // initial
				// value
				0, // min
				500, // max
				0.1); // step
		spinner_score_interlink = new JSpinner(model_interlink_spinner);
		spinner_score_interlink.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_score_inter_link = spinner_score_interlink.getEditor();
		JFormattedTextField field_score_inter_link = (JFormattedTextField) comp_score_inter_link.getComponent(0);
		DefaultFormatter formatter_score_inter_link = (DefaultFormatter) field_score_inter_link.getFormatter();
		formatter_score_inter_link.setCommitsOnValidEdit(true);
		spinner_score_interlink.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.interlink_threshold_score = (double) spinner_score_interlink.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.interlink_threshold_score",
						String.valueOf(Util.interlink_threshold_score));

				if (myNetwork != null && netView != null) {
					Util.updateEdgesStyle(myNetwork, netView);
				}
			}
		});
		link_score_panel.add(spinner_score_interlink);
		offset_y += 30;

		SpinnerModel model_combinedlink_spinner = new SpinnerNumberModel(Util.combinedlink_threshold_score, // initial
				// value
				0, // min
				500, // max
				0.1); // step
		spinner_score_combinedlink = new JSpinner(model_combinedlink_spinner);
		spinner_score_combinedlink.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_score_combined_link = spinner_score_combinedlink.getEditor();
		JFormattedTextField field_score_combined_link = (JFormattedTextField) comp_score_combined_link.getComponent(0);
		DefaultFormatter formatter_score_combined_link = (DefaultFormatter) field_score_combined_link.getFormatter();
		formatter_score_combined_link.setCommitsOnValidEdit(true);
		spinner_score_combinedlink.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.combinedlink_threshold_score = (double) spinner_score_combinedlink.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.combinedlink_threshold_score",
						String.valueOf(Util.combinedlink_threshold_score));
				if (myNetwork != null && netView != null) {
					Util.filterUnmodifiedEdges(myNetwork, netView);
				}

			}
		});
		link_score_panel.add(spinner_score_combinedlink);
	}

	/**
	 * Method responsible for initializing legends of the links
	 * 
	 * @param offset_x     offset x
	 * @param button_width button width
	 */
	private void init_link_edge_labels_features(int offset_x, int button_width) {

		int offset_y = 40;
		link_legend_panel = new JPanel();
		link_legend_panel.setBackground(Color.WHITE);
		link_legend_panel.setBorder(BorderFactory.createTitledBorder("Edge labels"));
		link_legend_panel.setBounds(10, 315, 230, 115);
		link_legend_panel.setLayout(null);
		link_panel.add(link_legend_panel);

		JLabel font_size_links = new JLabel("Font size:");
		font_size_links.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		font_size_links.setBounds(10, offset_y, 100, 40);
		link_legend_panel.add(font_size_links);
		offset_y += 30;

		JLabel opacity_edge_label = new JLabel("Opacity:");
		opacity_edge_label.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		opacity_edge_label.setBounds(10, offset_y, 100, 40);
		link_legend_panel.add(opacity_edge_label);

		offset_y = 50;
		offset_x -= 10;
		if (!Util.isWindows())// MacOS or Unix
			offset_x -= 5;

		SpinnerModel model_link = new SpinnerNumberModel(Util.edge_label_font_size.intValue(), // initial value
				0, // min
				100, // max
				1); // step
		spinner_font_size_link_legend = new JSpinner(model_link);
		spinner_font_size_link_legend.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_link = spinner_font_size_link_legend.getEditor();
		JFormattedTextField field_link = (JFormattedTextField) comp_link.getComponent(0);
		DefaultFormatter formatter_link = (DefaultFormatter) field_link.getFormatter();
		formatter_link.setCommitsOnValidEdit(true);
		spinner_font_size_link_legend.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_label_font_size = (Integer) spinner_font_size_link_legend.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.edge_label_font_size", Util.edge_label_font_size.toString());

				if (myNetwork != null && netView != null) {
					Util.updateEdgesStyle(myNetwork, netView);
				}
			}
		});
		link_legend_panel.add(spinner_font_size_link_legend);
		offset_y += 30;

		SpinnerModel model_opacity_edge_label = new SpinnerNumberModel(Util.edge_label_opacity.intValue(), // initial
																											// value
				0, // min
				255, // max
				1); // step
		spinner_opacity_edge_label = new JSpinner(model_opacity_edge_label);
		spinner_opacity_edge_label.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_opacitiy_edge_label = spinner_opacity_edge_label.getEditor();
		JFormattedTextField field_opacity_edge_label = (JFormattedTextField) comp_opacitiy_edge_label.getComponent(0);
		DefaultFormatter formatter_opacity_edge_label = (DefaultFormatter) field_opacity_edge_label.getFormatter();
		formatter_opacity_edge_label.setCommitsOnValidEdit(true);
		spinner_opacity_edge_label.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_label_opacity = (Integer) spinner_opacity_edge_label.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.edge_label_opacity", Util.edge_label_opacity.toString());

				if (myNetwork != null && netView != null) {
					Util.updateEdgesStyle(myNetwork, netView);
				}
			}
		});
		spinner_opacity_edge_label.setToolTipText("Set a value between 0 (transparent) and 255 (opaque).");
		link_legend_panel.add(spinner_opacity_edge_label);

		offset_y = 25;
		show_links_legend = new JCheckBox("Display");
		show_links_legend.setBackground(Color.WHITE);
		show_links_legend.setSelected(Util.showLinksLegend);
		show_links_legend.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		show_links_legend.setBounds(5, offset_y, 200, 20);

		if (!Util.showIntraLinks && !Util.showInterLinks) {
			show_links_legend.setEnabled(false);
		} else {
			show_links_legend.setEnabled(true);
		}

		if (Util.showLinksLegend) {
			spinner_font_size_link_legend.setEnabled(true);
			spinner_opacity_edge_label.setEnabled(true);
		} else {
			spinner_font_size_link_legend.setEnabled(false);
			spinner_opacity_edge_label.setEnabled(false);
		}
		show_links_legend.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					spinner_font_size_link_legend.setEnabled(true);
					spinner_opacity_edge_label.setEnabled(true);
					Util.showLinksLegend = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showLinksLegend", "true");
				} else {
					spinner_font_size_link_legend.setEnabled(false);
					spinner_opacity_edge_label.setEnabled(false);
					Util.showLinksLegend = false;
					XlinkCyNETProps.setProperty("xlinkcynet.showLinksLegend", "false");
				}

				if (myNetwork != null && netView != null) {
					Util.updateEdgesStyle(myNetwork, netView);
				}
			}
		});
		link_legend_panel.add(show_links_legend);
	}

	/**
	 * Method responsible for initializing check boxes of link colors
	 * 
	 * @param offset_x     offset x
	 * @param button_width button width
	 */
	private void init_link_check_boxes_colors(int offset_x, int button_width) {

		int offset_y = 20;
		show_inter_link = new JCheckBox("Display Interlink:");
		show_inter_link.setBackground(Color.WHITE);
		show_inter_link.setSelected(Util.showInterLinks);
		show_inter_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));

		show_intra_link = new JCheckBox("Display Intralink:");
		show_intra_link.setBackground(Color.WHITE);
		show_intra_link.setSelected(Util.showIntraLinks);
		show_intra_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		if (Util.isWindows())
			show_intra_link.setBounds(5, offset_y, 115, 20);
		else
			show_intra_link.setBounds(5, offset_y, 130, 20);

		if (Util.showIntraLinks) {
			intraLinkColorButton.setEnabled(true);
			spinner_score_intralink.setEnabled(true);
		} else {
			intraLinkColorButton.setEnabled(false);
			spinner_score_intralink.setEnabled(false);
		}
		show_intra_link.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					intraLinkColorButton.setEnabled(true);
					spinner_score_intralink.setEnabled(true);
					Util.showIntraLinks = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showIntraLinks", "true");

					if (!show_inter_link.isSelected()) {
						spinner_opacity_edge_link.setEnabled(true);
						spinner_width_edge_link.setEnabled(true);
						show_links_legend.setEnabled(true);
					}

				} else {
					intraLinkColorButton.setEnabled(false);
					spinner_score_intralink.setEnabled(false);
					Util.showIntraLinks = false;
					XlinkCyNETProps.setProperty("xlinkcynet.showIntraLinks", "false");

					if (!show_inter_link.isSelected()) {
						spinner_opacity_edge_link.setEnabled(false);
						spinner_width_edge_link.setEnabled(false);
						show_links_legend.setSelected(false);
						show_links_legend.setEnabled(false);

					} else {
						spinner_opacity_edge_link.setEnabled(true);
						spinner_width_edge_link.setEnabled(true);
						show_links_legend.setEnabled(true);
					}
				}

				if (myNetwork != null && netView != null && style != null && handleFactory != null
						&& bendFactory != null && lexicon != null) {
					try {
						Util.updateNodesStyles(myNetwork, netView, style, handleFactory, bendFactory, lexicon,
								Util.showInterLinks);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "XlinkCyNET - Alert",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

				}
			}
		});
		link_panel.add(show_intra_link);
		offset_y += 30;

		if (Util.isWindows())
			show_inter_link.setBounds(5, offset_y, 115, 20);
		else
			show_inter_link.setBounds(5, offset_y, 130, 20);

		if (Util.showInterLinks) {
			interLinkColorButton.setEnabled(true);
			spinner_score_interlink.setEnabled(true);
		} else {
			interLinkColorButton.setEnabled(false);
			spinner_score_interlink.setEnabled(false);
		}
		show_inter_link.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					interLinkColorButton.setEnabled(true);
					spinner_score_interlink.setEnabled(true);
					Util.showInterLinks = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showInterLinks", "true");

					if (!show_intra_link.isSelected()) {
						spinner_opacity_edge_link.setEnabled(true);
						spinner_width_edge_link.setEnabled(true);
						show_links_legend.setEnabled(true);
					}

				} else {
					interLinkColorButton.setEnabled(false);
					spinner_score_interlink.setEnabled(false);
					Util.showInterLinks = false;
					XlinkCyNETProps.setProperty("xlinkcynet.showInterLinks", "false");

					if (!show_intra_link.isSelected()) {
						spinner_opacity_edge_link.setEnabled(false);
						spinner_width_edge_link.setEnabled(false);
						show_links_legend.setSelected(false);
						show_links_legend.setEnabled(false);

					} else {
						spinner_opacity_edge_link.setEnabled(true);
						spinner_width_edge_link.setEnabled(true);
						show_links_legend.setEnabled(true);
					}
				}

				if (myNetwork != null && netView != null && style != null && handleFactory != null
						&& bendFactory != null && lexicon != null) {
					try {
						Util.updateNodesStyles(myNetwork, netView, style, handleFactory, bendFactory, lexicon,
								e.getStateChange() == ItemEvent.DESELECTED);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "XlinkCyNET - Alert",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

				}
			}
		});
		link_panel.add(show_inter_link);

		offset_y += 30;
		show_ptms = new JCheckBox("Display PTM:");
		show_ptms.setBackground(Color.WHITE);
		show_ptms.setSelected(Util.showPTMs);
		show_ptms.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		if (Util.isWindows())
			show_ptms.setBounds(5, offset_y, 115, 20);
		else
			show_ptms.setBounds(5, offset_y, 130, 20);

		show_ptms.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					ptmColorButton.setEnabled(true);
					Util.showPTMs = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showPTMs", "true");

				} else {
					ptmColorButton.setEnabled(false);
					Util.showPTMs = false;
					XlinkCyNETProps.setProperty("xlinkcynet.showPTMs", "false");
				}

				if (myNetwork != null && netView != null) {
					Util.setPTMStyleForAllNodes(myNetwork, netView);
				}
			}
		});
		link_panel.add(show_ptms);

		offset_y += 30;
		show_monolinks = new JCheckBox("Display Peptides:");
		show_monolinks.setBackground(Color.WHITE);
		show_monolinks.setSelected(Util.showPTMs);
		show_monolinks.setToolTipText("Display intralinked peptides throughout the protein.");
		show_monolinks.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		show_monolinks.setBounds(5, offset_y, 130, 20);
		show_monolinks.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					monolinkColorButton.setEnabled(true);
					Util.showMonolinkedPeptides = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showMonolinkedPeptides", "true");

				} else {
					monolinkColorButton.setEnabled(false);
					Util.showMonolinkedPeptides = false;
					XlinkCyNETProps.setProperty("xlinkcynet.showMonolinkedPeptides", "false");
				}

				if (myNetwork != null && netView != null) {
					Util.setMonolinkPeptidesStyle(myNetwork, netView);
				}
			}
		});
		link_panel.add(show_monolinks);
	}

	/**
	 * Method responsible for initializing node style features
	 * 
	 * @param offset_x     offset x
	 * @param button_width button width
	 */
	private void init_node_style_features(int offset_x, int button_width) {

		node_panel = new JPanel();
		node_panel.setBackground(Color.WHITE);
		node_panel.setBorder(BorderFactory.createTitledBorder("Node"));
		node_panel.setLayout(null);

		int offset_y = 10;
		JLabel font_size_node = new JLabel("Font size:");
		font_size_node.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		font_size_node.setBounds(10, offset_y, 100, 40);
		node_panel.add(font_size_node);

		offset_y = 20;
		if (!Util.isWindows())// MacOS or Unix
			offset_x -= 5;
		SpinnerModel model_node = new SpinnerNumberModel(Util.node_label_font_size.intValue(), // initial value
				0, // min
				100, // max
				1); // step
		spinner_font_size_node = new JSpinner(model_node);
		spinner_font_size_node.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_node = spinner_font_size_node.getEditor();
		JFormattedTextField field_node = (JFormattedTextField) comp_node.getComponent(0);
		DefaultFormatter formatter_node = (DefaultFormatter) field_node.getFormatter();
		formatter_node.setCommitsOnValidEdit(true);
		spinner_font_size_node.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.node_label_font_size = (Integer) spinner_font_size_node.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.node_label_font_size", Util.node_label_font_size.toString());

				if (myNetwork != null && netView != null) {
					Util.updateNodesStyles(myNetwork, netView);
				}
			}
		});
		node_panel.add(spinner_font_size_node);

	}

	/**
	 * Method responsible for initializing node border panel
	 * 
	 * @param offset_x     offset x
	 * @param button_width button width
	 */
	private void init_node_border_features(int offset_x, int button_width) {

		node_border_panel = new JPanel();
		node_border_panel.setBackground(Color.WHITE);
		node_border_panel.setBorder(BorderFactory.createTitledBorder("Border"));
		node_border_panel.setBounds(10, 50, 230, 120);
		node_border_panel.setLayout(null);
		node_panel.add(node_border_panel);

		int offset_y = 10;
		JLabel textLabel_border_node_color = new JLabel("Color:");
		textLabel_border_node_color.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_border_node_color.setBounds(10, offset_y, 50, 40);
		node_border_panel.add(textLabel_border_node_color);
		offset_y += 30;

		JLabel opacity_node_border = new JLabel("Opacity:");
		opacity_node_border.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		opacity_node_border.setBounds(10, offset_y, 100, 40);
		node_border_panel.add(opacity_node_border);
		offset_y += 30;

		JLabel width_node_border = new JLabel("Width:");
		width_node_border.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		width_node_border.setBounds(10, offset_y, 100, 40);
		node_border_panel.add(width_node_border);

		offset_y = 20;
		offset_x -= 10;
		borderNodeColorButton = new JButton();
		borderNodeColorButton.setBounds(offset_x, offset_y, button_width, 15);
		borderNodeColorButton.setBackground(Util.NodeBorderColor);
		borderNodeColorButton.setForeground(Util.NodeBorderColor);
		borderNodeColorButton.setOpaque(true);
		borderNodeColorButton.setBorderPainted(false);
		borderNodeColorButton.setToolTipText("Value: R:" + Util.NodeBorderColor.getRed() + " G:"
				+ Util.NodeBorderColor.getGreen() + " B:" + Util.NodeBorderColor.getBlue() + " - "
				+ String.format("#%02X%02X%02X", Util.NodeBorderColor.getRed(), Util.NodeBorderColor.getGreen(),
						Util.NodeBorderColor.getBlue()));

		borderNodeColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		borderNodeColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				Color initialcolor = borderNodeColorButton.getBackground();
				Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
				if (color == null)
					color = initialcolor;
				borderNodeColorButton.setBackground(color);
				borderNodeColorButton.setForeground(color);
				borderNodeColorButton.setOpaque(true);
				borderNodeColorButton.setBorderPainted(false);
				Util.NodeBorderColor = color;
				XlinkCyNETProps.setProperty("xlinkcynet.NodeBorderColor",
						color.getRed() + "#" + color.getGreen() + "#" + color.getBlue());

				borderNodeColorButton.setToolTipText("Value: R:" + Util.NodeBorderColor.getRed() + " G:"
						+ Util.NodeBorderColor.getGreen() + " B:" + Util.NodeBorderColor.getBlue() + " - "
						+ String.format("#%02X%02X%02X", Util.NodeBorderColor.getRed(), Util.NodeBorderColor.getGreen(),
								Util.NodeBorderColor.getBlue()));

				if (myNetwork != null && netView != null) {
					Util.updateNodesStyles(myNetwork, netView);
				}
			}
		});

		node_border_panel.add(borderNodeColorButton);

		offset_y = 50;
		if (!Util.isWindows())// MacOS or Unix
			offset_x -= 5;
		SpinnerModel model_opacity_node_border = new SpinnerNumberModel(Util.node_border_opacity.intValue(), // initial
				// value
				0, // min
				255, // max
				1); // step
		spinner_opacity_node_border = new JSpinner(model_opacity_node_border);
		spinner_opacity_node_border.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_opacitiy_node_border = spinner_opacity_node_border.getEditor();
		JFormattedTextField field_opacity_node_border = (JFormattedTextField) comp_opacitiy_node_border.getComponent(0);
		DefaultFormatter formatter_opacity_node_border = (DefaultFormatter) field_opacity_node_border.getFormatter();
		formatter_opacity_node_border.setCommitsOnValidEdit(true);
		spinner_opacity_node_border.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.node_border_opacity = (Integer) spinner_opacity_node_border.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.node_border_opacity", Util.node_border_opacity.toString());

				if (myNetwork != null && netView != null) {
					Util.updateNodesStyles(myNetwork, netView);
				}
			}
		});
		spinner_opacity_node_border.setToolTipText("Set a value between 0 (transparent) and 255 (opaque).");
		node_border_panel.add(spinner_opacity_node_border);
		offset_y += 30;

		SpinnerModel width_node_border_spinner = new SpinnerNumberModel(Util.node_border_width, // initial
				// value
				1, // min
				10, // max
				0.1); // step
		spinner_width_node_border = new JSpinner(width_node_border_spinner);
		spinner_width_node_border.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_width_node_border = spinner_width_node_border.getEditor();
		JFormattedTextField field_width_node_border = (JFormattedTextField) comp_width_node_border.getComponent(0);
		DefaultFormatter formatter_width_node_border = (DefaultFormatter) field_width_node_border.getFormatter();
		formatter_width_node_border.setCommitsOnValidEdit(true);
		spinner_width_node_border.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.node_border_width = (double) spinner_width_node_border.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.node_border_width", String.valueOf(Util.node_border_width));

				if (myNetwork != null && netView != null) {
					Util.updateNodesStyles(myNetwork, netView);
				}
			}
		});
		spinner_width_node_border.setToolTipText("Set a value between 1 and 10.");
		node_border_panel.add(spinner_width_node_border);
	}

	/**
	 * Method responsible for initializing pymol setting panel
	 * 
	 * @param offset_x offset x
	 */
	private void init_pymol_panel(int offset_x) {

		pymol_panel = new JPanel();
		pymol_panel.setBackground(Color.WHITE);
		pymol_panel.setBorder(BorderFactory.createTitledBorder("PyMOL"));
		pymol_panel.setLayout(null);

		int offset_y = 10;
		JLabel pymolPath_label = new JLabel("Application path:");
		pymolPath_label.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		if (Util.isUnix())
			pymolPath_label.setBounds(10, offset_y, 130, 40);
		else
			pymolPath_label.setBounds(10, offset_y, 100, 40);
		pymol_panel.add(pymolPath_label);

		offset_y += 20;
		pymolPathStr = new JLabel("???");
		pymolPathStr.setFont(new java.awt.Font("Tahoma", Font.ITALIC, 8));
		pymolPathStr.setBounds(10, offset_y - 3, 350, 40);
		pymol_panel.add(pymolPathStr);

		offset_y = 20;
		if (!Util.isWindows())// MacOS or Unix
			offset_x -= 5;

		Icon iconPyMOLBtn = new ImageIcon(getClass().getResource("/images/pyMOL_logo.png"));
		JButton pyMOL_pathButton = new JButton(iconPyMOLBtn);
		if (Util.isWindows())
			offset_x -= 5;

		offset_y -= 5;
		pyMOL_pathButton.setBounds(offset_x, offset_y, 30, 30);

		pyMOL_pathButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		pyMOL_pathButton.setToolTipText("Select the PyMOL software");

		pyMOL_pathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				pymolPathStr.setText(getPyMOLPath());
				Util.PYMOL_PATH = pymolPathStr.getText();
				XlinkCyNETProps.setProperty("xlinkcynet.pymol_path", pymolPathStr.getText());
			}
		});

		pymol_panel.add(pyMOL_pathButton);

		offset_y += 40;

		useAlphaFold = new JCheckBox("Use AlphaFold");
		useAlphaFold.setBackground(Color.WHITE);
		useAlphaFold.setSelected(Util.useAlphaFold);
		useAlphaFold.setToolTipText("Use AlphaFold for predicting protein structure.");
		useAlphaFold.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		useAlphaFold.setBounds(5, offset_y, offset_x - 5, 20);
		useAlphaFold.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					Util.useAlphaFold = true;
					XlinkCyNETProps.setProperty("xlinkcynet.useAlphaFold", "true");

				} else {
					Util.useAlphaFold = false;
					XlinkCyNETProps.setProperty("xlinkcynet.useAlphaFold", "false");
				}

			}
		});
		pymol_panel.add(useAlphaFold);

		useCustomizedPDB = new JCheckBox("Use customized PDB file");
		useCustomizedPDB.setBackground(Color.WHITE);
		useCustomizedPDB.setSelected(Util.useCustomizedPDB);
		useCustomizedPDB.setToolTipText("Use customized PDB file to display protein structure.");
		useCustomizedPDB.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		useCustomizedPDB.setBounds(offset_x, offset_y, 200, 20);
		useCustomizedPDB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					Util.useCustomizedPDB = true;
					XlinkCyNETProps.setProperty("xlinkcynet.useCustomizedPDB", "true");

				} else {
					Util.useCustomizedPDB = false;
					XlinkCyNETProps.setProperty("xlinkcynet.useCustomizedPDB", "false");
				}

			}
		});
		pymol_panel.add(useCustomizedPDB);
	}

	/**
	 * Select PyMOL file
	 * 
	 * @return path
	 */
	private String getPyMOLPath() {
		JFileChooser choosePyMOL = new JFileChooser();
		choosePyMOL.setFileSelectionMode(JFileChooser.FILES_ONLY);
		choosePyMOL.setDialogTitle("Select PyMOL file");

		if (choosePyMOL.showOpenDialog(choosePyMOL) != JFileChooser.APPROVE_OPTION)
			return Util.PYMOL_PATH;

		return choosePyMOL.getSelectedFile().toString();
	}

	/**
	 * Method responsible for putting objects to Panel
	 */
	private void setFrameObjects() {

		// use the border layout for this CytoPanel
		setLayout(new GridBagLayout());
		this.setBackground(Color.WHITE);

		int button_width = 38;
		int offset_x = 145;// MacOS and Unix
		int ipdax = 300;// MacOS and Unix
		if (Util.isWindows()) {
			offset_x = 135;
			ipdax = 250;
		}

		this.init_link_color_buttons(offset_x, button_width);
		this.init_link_style_features(offset_x, button_width);
		this.init_link_log_score_features(offset_x, button_width);
		this.init_link_edge_labels_features(offset_x, button_width);
		this.init_link_check_boxes_colors(offset_x, button_width);

		this.add(link_panel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), ipdax, 440));

		this.init_node_style_features(offset_x, button_width);
		this.init_node_border_features(offset_x, button_width);

		this.add(node_panel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), ipdax, 170));

		this.init_pymol_panel(offset_x);
		this.checkPyMOLname();

		this.add(pymol_panel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), ipdax, 75));

	}

	public static void updateParamsValue() {

		show_intra_link.setSelected(Util.showIntraLinks);
		show_inter_link.setSelected(Util.showInterLinks);
		show_ptms.setSelected(Util.showPTMs);
		show_monolinks.setSelected(Util.showMonolinkedPeptides);

		if (Util.showIntraLinks) {
			intraLinkColorButton.setEnabled(true);
			spinner_score_intralink.setEnabled(true);
		} else {

			intraLinkColorButton.setEnabled(false);
			spinner_score_intralink.setEnabled(false);
		}

		if (Util.showInterLinks) {
			interLinkColorButton.setEnabled(true);
			spinner_score_interlink.setEnabled(true);
		} else {
			interLinkColorButton.setEnabled(false);
			spinner_score_interlink.setEnabled(false);
		}

		if (Util.showPTMs) {
			ptmColorButton.setEnabled(true);
		} else {
			ptmColorButton.setEnabled(false);
		}

		if (Util.showMonolinkedPeptides) {
			monolinkColorButton.setEnabled(true);
		} else {
			monolinkColorButton.setEnabled(false);
		}

		intraLinkColorButton.setBackground(Util.IntraLinksColor);
		intraLinkColorButton.setForeground(Util.IntraLinksColor);

		interLinkColorButton.setBackground(Util.InterLinksColor);
		interLinkColorButton.setForeground(Util.InterLinksColor);

		spinner_opacity_edge_link.setValue(Util.edge_link_opacity);
		spinner_width_edge_link.setValue(Util.edge_link_width);

		spinner_score_intralink.setValue(Util.intralink_threshold_score);
		spinner_score_interlink.setValue(Util.interlink_threshold_score);
		spinner_score_combinedlink.setValue(Util.combinedlink_threshold_score);

		if (!Util.showIntraLinks && !Util.showInterLinks) {
			show_links_legend.setEnabled(false);
		} else {
			show_links_legend.setEnabled(true);
		}

		if (Util.showLinksLegend) {
			spinner_font_size_link_legend.setEnabled(true);
			spinner_opacity_edge_label.setEnabled(true);
		} else {
			spinner_font_size_link_legend.setEnabled(false);
			spinner_opacity_edge_label.setEnabled(false);
		}

		spinner_font_size_link_legend.setValue(Util.edge_label_font_size);
		spinner_opacity_edge_label.setValue(Util.edge_label_opacity);

		spinner_font_size_node.setValue(Util.node_label_font_size);
		borderNodeColorButton.setBackground(Util.NodeBorderColor);
		borderNodeColorButton.setForeground(Util.NodeBorderColor);
		spinner_opacity_node_border.setValue(Util.node_border_opacity);
		spinner_width_node_border.setValue(Util.node_border_width);

	}

	/**
	 * Get current component
	 */
	public Component getComponent() {
		return this;
	}

	/**
	 * Returns Cytoscape panel location
	 */
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	/**
	 * Returns panel title
	 */
	public String getTitle() {
		return "XlinkCyNET Settings";
	}

	/**
	 * Return the logo
	 */
	public Icon getIcon() {
		ImageIcon imgIcon = new ImageIcon(getClass().getResource("/images/logo.png"));
		return imgIcon;
	}

}
