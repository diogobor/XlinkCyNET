package de.fmp.liulab.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

import de.fmp.liulab.utils.Util;

public class MainControlPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = 8292806967891823933L;
	private JPanel color_panel;
	private JPanel link_legend_panel;
	private JPanel other_panel;

	public MainControlPanel() {

		setFrameObjects();
		this.setVisible(true);
	}

	private void setFrameObjects() {

		// use the border layout for this CytoPanel
		setLayout(null);

		setSize(250, 120);
		init_group_panels();
		init_labels();
		init_color_buttons();
		init_spinners();

	}

	private void init_group_panels() {
		color_panel = new JPanel();
		color_panel.setBorder(BorderFactory.createTitledBorder("Color"));
		color_panel.setBounds(10, 10, 250, 120);
		color_panel.setLayout(null);
		this.add(color_panel);

		link_legend_panel = new JPanel();
		link_legend_panel.setBorder(BorderFactory.createTitledBorder("Link legend"));
		link_legend_panel.setBounds(10, 140, 250, 120);
		link_legend_panel.setLayout(null);
		this.add(link_legend_panel);

		other_panel = new JPanel();
		other_panel.setBorder(BorderFactory.createTitledBorder("Other"));
		other_panel.setBounds(10, 270, 250, 90);
		other_panel.setLayout(null);
		this.add(other_panel);

	}

	/**
	 * Method responsible for initializing all labels in the frame
	 */
	private void init_labels() {

		int offset_y = -20;
		JLabel textLabel_intra_link_color = new JLabel("Intralink:");
		textLabel_intra_link_color.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_intra_link_color.setBounds(10, offset_y, 450, 100);
		color_panel.add(textLabel_intra_link_color);
		offset_y += 30;

		JLabel textLabel_inter_link_color = new JLabel("Interlink:");
		textLabel_inter_link_color.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_inter_link_color.setBounds(10, offset_y, 450, 100);
		color_panel.add(textLabel_inter_link_color);
		offset_y += 30;

		JLabel textLabel_border_node_color = new JLabel("Node border:");
		textLabel_border_node_color.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		textLabel_border_node_color.setBounds(10, offset_y, 450, 100);
		color_panel.add(textLabel_border_node_color);

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
		JLabel font_size_node = new JLabel("Font size (node):");
		font_size_node.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		font_size_node.setBounds(10, offset_y, 450, 100);
		other_panel.add(font_size_node);
		offset_y += 30;

		JLabel opacity_edge_link = new JLabel("Opacity (link):");
		opacity_edge_link.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		opacity_edge_link.setBounds(10, offset_y, 450, 100);
		other_panel.add(opacity_edge_link);
	}

	/**
	 * Method responsible for initializing all buttons in the frame
	 */
	private void init_color_buttons() {

		int offset_x = 110;
		int offset_y = 25;

		final JButton intraLinkColorButton = new JButton();
		intraLinkColorButton.setBounds(offset_x, offset_y, 30, 15);
		intraLinkColorButton.setBackground(Util.IntraLinksColor);
		intraLinkColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				Color initialcolor = intraLinkColorButton.getBackground();
				Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
				intraLinkColorButton.setBackground(color);
				intraLinkColorButton.setForeground(color);
				intraLinkColorButton.setOpaque(true);
				intraLinkColorButton.setBorderPainted(false);

				Util.IntraLinksColor = color;
			}
		});
		color_panel.add(intraLinkColorButton);
		offset_y += 30;

		final JButton interLinkColorButton = new JButton();
		interLinkColorButton.setBounds(offset_x, offset_y, 30, 15);
		interLinkColorButton.setBackground(Util.InterLinksColor);
		interLinkColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				Color initialcolor = interLinkColorButton.getBackground();
				Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
				interLinkColorButton.setBackground(color);
				interLinkColorButton.setForeground(color);
				interLinkColorButton.setOpaque(true);
				interLinkColorButton.setBorderPainted(false);

				Util.InterLinksColor = color;
			}
		});
		color_panel.add(interLinkColorButton);
		offset_y += 30;

		final JButton BorderNodeColorButton = new JButton();
		BorderNodeColorButton.setBounds(offset_x, offset_y, 30, 15);
		BorderNodeColorButton.setBackground(Util.NodeBorderColor);
		BorderNodeColorButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				Color initialcolor = BorderNodeColorButton.getBackground();
				Color color = JColorChooser.showDialog(null, "Select a color", initialcolor);
				BorderNodeColorButton.setBackground(color);
				BorderNodeColorButton.setForeground(color);
				BorderNodeColorButton.setOpaque(true);
				BorderNodeColorButton.setBorderPainted(false);

				Util.NodeBorderColor = color;
			}
		});
		color_panel.add(BorderNodeColorButton);
	}

	/**
	 * Method responsible for initializing all spinners in the frame
	 */
	private void init_spinners() {

		int offset_x = 110;
		int offset_y = 50;

		SpinnerModel model_link = new SpinnerNumberModel(Util.edge_label_font_size.intValue(), // initial value
				0, // min
				30, // max
				1); // step
		final JSpinner spinner_link = new JSpinner(model_link);
		spinner_link.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_link = spinner_link.getEditor();
		JFormattedTextField field_link = (JFormattedTextField) comp_link.getComponent(0);
		DefaultFormatter formatter_link = (DefaultFormatter) field_link.getFormatter();
		formatter_link.setCommitsOnValidEdit(true);
		spinner_link.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_label_font_size = (Integer) spinner_link.getValue();
			}
		});
		link_legend_panel.add(spinner_link);
		offset_y += 30;

		SpinnerModel model_opacity_edge_label = new SpinnerNumberModel(Util.edge_label_opacity.intValue(), // initial
																											// value
				0, // min
				255, // max
				1); // step
		final JSpinner spinner_opacity_edge_label = new JSpinner(model_opacity_edge_label);
		spinner_opacity_edge_label.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_opacitiy_edge_label = spinner_opacity_edge_label.getEditor();
		JFormattedTextField field_opacity_edge_label = (JFormattedTextField) comp_opacitiy_edge_label.getComponent(0);
		DefaultFormatter formatter_opacity_edge_label = (DefaultFormatter) field_opacity_edge_label.getFormatter();
		formatter_opacity_edge_label.setCommitsOnValidEdit(true);
		spinner_opacity_edge_label.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_label_opacity = (Integer) spinner_opacity_edge_label.getValue();
			}
		});
		link_legend_panel.add(spinner_opacity_edge_label);
		offset_y += 30;

		JCheckBox show_links_legend = new JCheckBox("Display");
		show_links_legend.setSelected(true);
		show_links_legend.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
		show_links_legend.setBounds(5, offset_y - 85, 200, 20);
		show_links_legend.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {// checkbox has been selected
					spinner_link.setEnabled(true);
					spinner_opacity_edge_label.setEnabled(true);
					Util.showLinksLegend = true;
				} else {
					spinner_link.setEnabled(false);
					spinner_opacity_edge_label.setEnabled(false);
					Util.showLinksLegend = false;
				}
				;
			}
		});
		link_legend_panel.add(show_links_legend);

		offset_y = 20;
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
			}
		});
		other_panel.add(spinner_node);
		offset_y += 30;

		SpinnerModel model_opacity_edge_link = new SpinnerNumberModel(Util.edge_link_opacity.intValue(), // initial
																											// value
				0, // min
				255, // max
				1); // step
		final JSpinner spinner_opacity_edge_link = new JSpinner(model_opacity_edge_link);
		spinner_opacity_edge_link.setBounds(offset_x, offset_y, 60, 20);
		JComponent comp_opacitiy_edge_link = spinner_opacity_edge_link.getEditor();
		JFormattedTextField field_opacity_edge_link = (JFormattedTextField) comp_opacitiy_edge_link.getComponent(0);
		DefaultFormatter formatter_opacity_edge_link = (DefaultFormatter) field_opacity_edge_link.getFormatter();
		formatter_opacity_edge_link.setCommitsOnValidEdit(true);
		spinner_opacity_edge_link.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Util.edge_link_opacity = (Integer) spinner_opacity_edge_link.getValue();
			}
		});
		other_panel.add(spinner_opacity_edge_link);
	}

	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	public String getTitle() {
		return "XlinkCyNET Settings";
	}

	public Icon getIcon() {
		ImageIcon imgIcon = new ImageIcon(getClass().getResource("/images/logo.png"));
		return imgIcon;
	}

}