package de.fmp.liulab.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

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
	private JPanel link_legend_panel;
	private JPanel node_panel;
	private JPanel node_border_panel;

	private JButton intraLinkColorButton;
	private JButton interLinkColorButton;
	private JButton borderNodeColorButton;

	private JSpinner spinner_link;
	private JSpinner spinner_opacity_edge_label;
	private JSpinner spinner_opacity_edge_link;
	private JSpinner spinner_width_edge_link;

	private JCheckBox show_links_legend;

	private Properties XlinkCyNETProps;

	/**
	 * Constructor
	 */
	public MainControlPanel(Properties XlinkCyNETProps, ConfigurationManager cm) {

		this.XlinkCyNETProps = XlinkCyNETProps;
		this.load_default_parameters(cm);
		setFrameObjects();
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
	 * Method responsible for putting objects to Panel
	 */
	private void setFrameObjects() {

		// use the border layout for this CytoPanel
		setLayout(new GridLayout(2, 1));

		setSize(250, 120);
		init_group_panels();
		init_color_buttons();
		init_labels();
		init_spinners();
		init_checkBoxes();

	}

	/**
	 * Method responsible for initializing JPanels
	 */
	private void init_group_panels() {

		link_panel = new JPanel();
		link_panel.setBackground(Color.WHITE);
		link_panel.setBorder(BorderFactory.createTitledBorder("Link"));
		link_panel.setBounds(10, 10, 250, 120);
		link_panel.setLayout(null);
		this.add(link_panel);

		link_legend_panel = new JPanel();
		link_legend_panel.setBackground(Color.WHITE);
		link_legend_panel.setBorder(BorderFactory.createTitledBorder("Legend"));
		link_legend_panel.setBounds(10, 140, 250, 120);
		link_legend_panel.setLayout(null);
		link_panel.add(link_legend_panel);

		node_panel = new JPanel();
		node_panel.setBackground(Color.WHITE);
		node_panel.setBorder(BorderFactory.createTitledBorder("Node"));
		node_panel.setBounds(10, 270, 250, 150);
		node_panel.setLayout(null);
		this.add(node_panel);

		node_border_panel = new JPanel();
		node_border_panel.setBackground(Color.WHITE);
		node_border_panel.setBorder(BorderFactory.createTitledBorder("Border"));
		node_border_panel.setBounds(10, 50, 250, 120);
		node_border_panel.setLayout(null);
		node_panel.add(node_border_panel);

	}

	/**
	 * Method responsible for initializing all check boxes in the frame
	 */
	private void init_checkBoxes() {
		int offset_y = 20;

		final JCheckBox show_inter_link = new JCheckBox("Display Interlink:");
		show_inter_link.setSelected(Util.showInterLinks);

		final JCheckBox show_intra_link = new JCheckBox("Display Intralink:");
		show_intra_link.setBackground(Color.WHITE);
		show_intra_link.setSelected(Util.showIntraLinks);
		show_intra_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		if (Util.isWindows())
			show_intra_link.setBounds(5, offset_y, 115, 20);
		else
			show_intra_link.setBounds(5, offset_y, 130, 20);

		if (Util.showIntraLinks) {
			intraLinkColorButton.setEnabled(true);
		} else {
			intraLinkColorButton.setEnabled(false);
		}
		show_intra_link.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					intraLinkColorButton.setEnabled(true);
					Util.showIntraLinks = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showIntraLinks", "true");

					if (!show_inter_link.isSelected()) {
						spinner_opacity_edge_link.setEnabled(true);
						spinner_width_edge_link.setEnabled(true);
						show_links_legend.setEnabled(true);
					}

				} else {
					intraLinkColorButton.setEnabled(false);
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
				;
			}
		});
		link_panel.add(show_intra_link);
		offset_y += 30;

		show_inter_link.setBackground(Color.WHITE);
		show_inter_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		if (Util.isWindows())
			show_inter_link.setBounds(5, offset_y, 115, 20);
		else
			show_inter_link.setBounds(5, offset_y, 130, 20);

		if (Util.showInterLinks) {
			interLinkColorButton.setEnabled(true);
		} else {
			interLinkColorButton.setEnabled(false);
		}
		show_inter_link.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					interLinkColorButton.setEnabled(true);
					Util.showInterLinks = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showInterLinks", "true");

					if (!show_intra_link.isSelected()) {
						spinner_opacity_edge_link.setEnabled(true);
						spinner_width_edge_link.setEnabled(true);
						show_links_legend.setEnabled(true);
					}

				} else {
					interLinkColorButton.setEnabled(false);
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
				;
			}
		});
		link_panel.add(show_inter_link);

		offset_y = 25;
		show_links_legend = new JCheckBox("Display");
		show_links_legend.setBackground(Color.WHITE);
		show_links_legend.setSelected(Util.showLinksLegend);
		show_links_legend.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		show_links_legend.setBounds(5, offset_y, 200, 20);
		if (Util.showLinksLegend) {
			spinner_link.setEnabled(true);
			spinner_opacity_edge_label.setEnabled(true);
		} else {
			spinner_link.setEnabled(false);
			spinner_opacity_edge_label.setEnabled(false);
		}
		show_links_legend.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					spinner_link.setEnabled(true);
					spinner_opacity_edge_label.setEnabled(true);
					Util.showLinksLegend = true;
					XlinkCyNETProps.setProperty("xlinkcynet.showLinksLegend", "true");
				} else {
					spinner_link.setEnabled(false);
					spinner_opacity_edge_label.setEnabled(false);
					Util.showLinksLegend = false;
					XlinkCyNETProps.setProperty("xlinkcynet.showLinksLegend", "false");
				}
				;
			}
		});
		link_legend_panel.add(show_links_legend);
	}

	/**
	 * Method responsible for initializing all labels in the frame
	 */
	private void init_labels() {

		int offset_y = 42;

		JLabel opacity_edge_link = new JLabel("Opacity:");
		opacity_edge_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		opacity_edge_link.setBounds(10, offset_y, 450, 100);
		link_panel.add(opacity_edge_link);
		offset_y += 30;

		JLabel width_edge_link = new JLabel("Width:");
		width_edge_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		width_edge_link.setBounds(10, offset_y, 450, 100);
		link_panel.add(width_edge_link);

		offset_y = 10;
		JLabel font_size_links = new JLabel("Font size:");
		font_size_links.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		font_size_links.setBounds(10, offset_y, 450, 100);
		link_legend_panel.add(font_size_links);
		offset_y += 30;

		JLabel opacity_edge_label = new JLabel("Opacity:");
		opacity_edge_label.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		opacity_edge_label.setBounds(10, offset_y, 450, 100);
		link_legend_panel.add(opacity_edge_label);

		offset_y = -20;
		JLabel font_size_node = new JLabel("Font size:");
		font_size_node.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		font_size_node.setBounds(10, offset_y, 450, 100);
		node_panel.add(font_size_node);
		offset_y += 30;

		offset_y = -20;
		JLabel textLabel_border_node_color = new JLabel("Color:");
		textLabel_border_node_color.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_border_node_color.setBounds(10, offset_y, 450, 100);
		node_border_panel.add(textLabel_border_node_color);
		offset_y += 30;

		JLabel opacity_node_border = new JLabel("Opacity:");
		opacity_node_border.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		opacity_node_border.setBounds(10, offset_y, 450, 100);
		node_border_panel.add(opacity_node_border);
		offset_y += 30;

		JLabel width_node_border = new JLabel("Width:");
		width_node_border.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		width_node_border.setBounds(10, offset_y, 450, 100);
		node_border_panel.add(width_node_border);

	}

	/**
	 * Method responsible for initializing all buttons in the frame
	 */
	private void init_color_buttons() {

		int button_width = 38;
		int offset_x = 140;
		if (Util.isWindows())
			offset_x = 135;
		int offset_y = 25;

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
				}
			}
		});
		link_panel.add(interLinkColorButton);

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
			}
		});
		node_border_panel.add(borderNodeColorButton);
	}

	/**
	 * Method responsible for initializing all spinners in the frame
	 */
	private void init_spinners() {

		int offset_x = 125;
		int offset_y = 50;

		SpinnerModel model_link = new SpinnerNumberModel(Util.edge_label_font_size.intValue(), // initial value
				0, // min
				30, // max
				1); // step
		spinner_link = new JSpinner(model_link);
		spinner_link.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_link = spinner_link.getEditor();
		JFormattedTextField field_link = (JFormattedTextField) comp_link.getComponent(0);
		DefaultFormatter formatter_link = (DefaultFormatter) field_link.getFormatter();
		formatter_link.setCommitsOnValidEdit(true);
		spinner_link.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_label_font_size = (Integer) spinner_link.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.edge_label_font_size", Util.edge_label_font_size.toString());
			}
		});
		link_legend_panel.add(spinner_link);
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
			}
		});
		spinner_opacity_edge_label.setToolTipText("Set a value between 0 (transparent) and 255 (opaque).");
		link_legend_panel.add(spinner_opacity_edge_label);

		offset_y = 85;
		offset_x += 10;
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
			}
		});
		spinner_opacity_edge_link.setToolTipText("Set a value between 0 (transparent) and 255 (opaque).");
		link_panel.add(spinner_opacity_edge_link);
		offset_y += 30;

		SpinnerModel width_edge_link = new SpinnerNumberModel(Util.edge_link_width, // initial
				// value
				1, // min
				10, // max
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
			}
		});
		spinner_width_edge_link.setToolTipText("Set a value between 1 and 10.");
		link_panel.add(spinner_width_edge_link);

		offset_y = 50;
		offset_x -= 10;
		SpinnerModel model_opacity_node_border = new SpinnerNumberModel(Util.node_border_opacity.intValue(), // initial
				// value
				0, // min
				255, // max
				1); // step
		final JSpinner spinner_opacity_node_border = new JSpinner(model_opacity_node_border);
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
			}
		});
		spinner_opacity_node_border.setToolTipText("Set a value between 0 (transparent) and 255 (opaque).");
		node_border_panel.add(spinner_opacity_node_border);
		offset_y += 30;

		SpinnerModel width_node_border = new SpinnerNumberModel(Util.node_border_width, // initial
				// value
				1, // min
				10, // max
				0.1); // step
		final JSpinner spinner_width_node_border = new JSpinner(width_node_border);
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
			}
		});
		spinner_width_node_border.setToolTipText("Set a value between 1 and 10.");
		node_border_panel.add(spinner_width_node_border);

		offset_y = 20;
		offset_x += 10;
		SpinnerModel model_node = new SpinnerNumberModel(Util.node_label_font_size.intValue(), // initial value
				0, // min
				30, // max
				1); // step
		final JSpinner spinner_node = new JSpinner(model_node);
		spinner_node.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_node = spinner_node.getEditor();
		JFormattedTextField field_node = (JFormattedTextField) comp_node.getComponent(0);
		DefaultFormatter formatter_node = (DefaultFormatter) field_node.getFormatter();
		formatter_node.setCommitsOnValidEdit(true);
		spinner_node.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.node_label_font_size = (Integer) spinner_node.getValue();
				XlinkCyNETProps.setProperty("xlinkcynet.node_label_font_size", Util.node_label_font_size.toString());
			}
		});
		node_panel.add(spinner_node);
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
