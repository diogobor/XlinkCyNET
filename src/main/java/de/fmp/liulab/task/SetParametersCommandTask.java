package de.fmp.liulab.task;

import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import de.fmp.liulab.utils.Util;

/**
 * Class responsible for setting XlinkCyNET parameters via command line
 * 
 * @author diogobor
 *
 */
public class SetParametersCommandTask extends CyRESTAbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Set XlinkCyNET parameters";
	}

	@Tunable(description = "Display intralinks", longDescription = "Display or hide all identified intralinks", exampleStringValue = "true")
	public boolean displayIntralinks = true;

	@Tunable(description = "Display interlinks", longDescription = "Display or hide all identified interlinks", exampleStringValue = "true")
	public boolean displayInterlinks = true;

	@Tunable(description = "Set opacity of cross-links", longDescription = "Set the opacity of all identified cross-links (range between 0 - transparent and 255 - opaque)", exampleStringValue = "120")
	public Integer opacityLinks = Util.edge_link_opacity;

	@Tunable(description = "Set width of cross-links", longDescription = "Set the width of all identified cross-links (range between 1 and 10)", exampleStringValue = "3")
	public double widthLinks = Util.edge_link_width;

	@Tunable(description = "Display cross-links legend", longDescription = "Display or hide the legends of all identified cross-links", exampleStringValue = "true")
	public boolean displayLinksLegend = false;

	@Tunable(description = "Set font size of cross-links legend", longDescription = "Set the font size of the legend of all identified cross-links", exampleStringValue = "12")
	public Integer fontSizeLinksLegend = Util.edge_label_font_size;

	@Tunable(description = "Set opacity of cross-links legend", longDescription = "Set the opacity of the legend of all identified cross-links (range between 0 - transparent and 255 - opaque)", exampleStringValue = "120")
	public Integer opacityLinksLegend = Util.edge_label_opacity;

	@Tunable(description = "Set the threshold -log(score) to intralinks.", longDescription = "Set the threshold score to intralinks. All intralinks that have a -log(score) above the threshold will be displayed.", exampleStringValue = "20")
	public double scoreIntralink = 0.0;

	@Tunable(description = "Set the threshold -log(score) to interlinks.", longDescription = "Set the threshold score to interlinks. All interlinks that have a -log(score) above the threshold will be displayed.", exampleStringValue = "20")
	public double scoreInterlink = 0.0;

	@Tunable(description = "Set the threshold -log(score) to PPI links.", longDescription = "Set the threshold score to PPI links. All PPI links that have a -log(score) above the threshold will be displayed.", exampleStringValue = "20")
	public double scorePPIlink = 0.0;

	@Tunable(description = "Set font size of nodes name", longDescription = "Set the font size of the name of all nodes", exampleStringValue = "PDE12")
	public Integer fontSizeNodesName = Util.node_label_font_size;

	@Tunable(description = "Set opacity of border nodes", longDescription = "Set the opacity of the border of all nodes (range between 0 - transparent and 255 - opaque)", exampleStringValue = "120")
	public Integer opacityBorderNodes = Util.node_border_opacity;

	@Tunable(description = "Set width of cross-links", longDescription = "Set the width of the border of all nodes (range between 1 and 10)", exampleStringValue = "3")
	public double widthBorderNodes = Util.node_border_width;

	/**
	 * Constructor
	 */
	public SetParametersCommandTask() {

	}

	/**
	 * Run
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {

		Util.showIntraLinks = this.displayIntralinks;
		Util.showInterLinks = this.displayInterlinks;
		Util.edge_link_opacity = this.opacityLinks;
		Util.edge_link_width = this.widthLinks;
		Util.showLinksLegend = this.displayLinksLegend;
		Util.edge_label_font_size = this.fontSizeLinksLegend;
		Util.edge_label_opacity = this.opacityLinksLegend;
		Util.intralink_threshold_score = this.scoreIntralink;
		Util.interlink_threshold_score = this.scoreInterlink;
		Util.combinedlink_threshold_score = this.scorePPIlink;
		Util.node_label_font_size = this.fontSizeNodesName;
		Util.node_border_opacity = this.opacityBorderNodes;
		Util.node_border_width = this.widthBorderNodes;

	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return (R) "Done!";
	}
}
