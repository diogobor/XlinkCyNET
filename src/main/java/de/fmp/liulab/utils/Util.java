package de.fmp.liulab.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.fmp.liulab.internal.UpdateViewListener;
import de.fmp.liulab.model.CrossLink;
import de.fmp.liulab.model.ProteinDomain;
import de.fmp.liulab.task.LoadProteinDomainTask;
import de.fmp.liulab.task.MainSingleNodeTask;

/**
 * Class responsible for getting several kind of attributes
 * 
 * @author borges.diogo
 *
 */
public class Util {

	public static String PROTEIN_LENGTH_A = "length_protein_a";
	public static String PROTEIN_LENGTH_B = "length_protein_b";
	public static String NODE_LABEL_POSITION = "NODE_LABEL_POSITION";
	private static String XL_PROTEIN_A_B = "pro_site_ab";
	private static String XL_PROTEIN_B_A = "pro_site_ba";
	private static String PROTEIN_A = "protein_a";
	private static String PROTEIN_B = "protein_b";

	private static String OS = System.getProperty("os.name").toLowerCase();
	private final static float OFFSET = 2;
	private static String edge_label_blank_spaces = "\n\n";

	public static Color IntraLinksColor = Color.GRAY;
	public static Color InterLinksColor = Color.GRAY;
	public static Color NodeBorderColor = new Color(315041);// Dark green
	public static boolean showLinksLegend = false;
	public static Integer edge_label_font_size = 12;
	public static Integer node_label_font_size = 12;
	public static Integer edge_label_opacity = 120;
	public static Integer edge_link_opacity = 120;
	public static boolean isProtein_expansion_horizontal = true;
	// Map<Network name, Map<Protein - Node SUID, List<ProteinDomain>>
	public static Map<String, Map<Long, List<ProteinDomain>>> proteinDomainsMap = new HashMap<String, Map<Long, List<ProteinDomain>>>();

	/**
	 * Check if a specific node has been modified
	 * 
	 * @param myNetwork
	 * @param netView
	 * @param node
	 * @return
	 */
	public static boolean IsNodeModified(CyNetwork myNetwork, CyNetworkView netView, VisualStyle style, CyNode node) {

		if (myNetwork == null || netView == null || style == null || node == null)
			return false;

		Object length_other_protein_a;
		Object length_other_protein_b;

		CyRow proteinA_node_row = myNetwork.getRow(node);

		length_other_protein_a = proteinA_node_row.getRaw(PROTEIN_LENGTH_A);
		length_other_protein_b = proteinA_node_row.getRaw(PROTEIN_LENGTH_B);

		if (length_other_protein_a == null) {
			if (length_other_protein_b == null)
				length_other_protein_a = 10;
			else
				length_other_protein_a = length_other_protein_b;
		}

		View<CyNode> proteinA_nodeView = netView.getNodeView(node);
		float proteinA_node_width = ((Number) proteinA_nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH))
				.floatValue();

		if (((Number) length_other_protein_a).floatValue() == proteinA_node_width) {// Expansion - horizontal
			return checkModifiedNode(myNetwork, netView, style, node);

		} else {// Expansion - vertical

			float proteinA_node_height = ((Number) proteinA_nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT))
					.floatValue();

			if (((Number) length_other_protein_a).floatValue() == proteinA_node_height) {
				return checkModifiedNode(myNetwork, netView, style, node);
			}
			return false;
		}
	}

	private static boolean checkModifiedNode(CyNetwork myNetwork, CyNetworkView netView, VisualStyle style,
			CyNode node) {
		View<CyNode> nodeView = netView.getNodeView(node);

		VisualLexicon lexicon = MainSingleNodeTask.lexicon;
		if (lexicon == null)
			lexicon = LoadProteinDomainTask.lexicon;

		if (lexicon == null)
			return false;

		// Try to get the label visual property by its ID
		VisualProperty<?> vp_label_position = lexicon.lookup(CyNode.class, Util.NODE_LABEL_POSITION);
		if (vp_label_position != null) {

			// If the property is supported by this rendering engine,
			// use the serialization string value to create the actual property value

			int ptn_label_length = myNetwork.getDefaultNodeTable().getRow(node.getSUID()).getRaw(CyNetwork.NAME)
					.toString().length();
			ptn_label_length *= 9;
			ObjectPosition position = (ObjectPosition) vp_label_position
					.parseSerializableString("W,W,c,-" + ptn_label_length + ".00,0.00");

			// If the parsed value is ok, apply it to the visual style
			// as default value or a visual mapping

			if (position != null) {
				ObjectPosition current_position = (ObjectPosition) nodeView.getVisualProperty(vp_label_position);
				if (current_position.getJustify() == position.getJustify()
						&& current_position.getOffsetX() == position.getOffsetX()
						&& current_position.getOffsetY() == position.getOffsetY()
						&& (!nodeView.getVisualProperty(BasicVisualLexicon.NODE_TOOLTIP)
								.equals(style.getDefaultValue(BasicVisualLexicon.NODE_TOOLTIP)))) {// Expansion
																									// horizontal
					isProtein_expansion_horizontal = true;
					return true;
				} else {
					isProtein_expansion_horizontal = false;
					position = (ObjectPosition) vp_label_position.parseSerializableString("N,S,c,0.00,0.00");
					if (current_position.getJustify() == position.getJustify()
							&& current_position.getOffsetX() == position.getOffsetX()
							&& current_position.getOffsetY() == position.getOffsetY()
							&& (!nodeView.getVisualProperty(BasicVisualLexicon.NODE_TOOLTIP)
									.equals(style.getDefaultValue(BasicVisualLexicon.NODE_TOOLTIP))))// Expansion
																										// vertical
						return true;
					else
						return false;
				}
			} else
				return false;
		} else
			return false;
	}

	/**
	 * Add all edges to the network
	 */
	public static boolean addOrUpdateEdgesToNetwork(CyNetwork myNetwork, CyNode node, VisualStyle style,
			CyNetworkView netView, View<CyNode> nodeView, HandleFactory handleFactory, BendFactory bendFactory,
			VisualLexicon lexicon, float proteinLength, ArrayList<CrossLink> intraLinks,
			ArrayList<CrossLink> interLinks, final TaskMonitor taskMonitor) {
		boolean HasAdjacentEdges = false;
		boolean IsIntraLink = false;
		boolean ContainsInterLink = false;
		boolean ContainsIntraLink = false;
		boolean IsMixedNode = false;

		if (myNetwork == null || node == null || style == null || netView == null) {
			return false;
		} else {
			nodeView = netView.getNodeView(node);
		}

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

			if (!edge_name.startsWith("[Source:")) {// New edges
				HasAdjacentEdges = true;

				View<CyEdge> currentEdgeView = netView.getEdgeView(edge);
				while (currentEdgeView == null) {
					// Apply the change to the view
					style.apply(netView);
					netView.updateView();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					currentEdgeView = netView.getEdgeView(edge);
				}

				currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);

				if (IsIntraLink) {
					ContainsIntraLink = true;
					plotIntraLinks(myNetwork, nodeView, netView, handleFactory, bendFactory, style, proteinLength,
							intraLinks);// Add or update intralinks

				} else {
					ContainsInterLink = true;
					plotInterLinks(myNetwork, nodeView, netView, handleFactory, bendFactory, style, node, sourceNode,
							targetNode, lexicon, proteinLength, interLinks);
				}

			} else { // Update all sites of the current selected node
				HasAdjacentEdges = true;

				updateInterLinkEdgesPosition(myNetwork, node, netView, handleFactory, bendFactory, style, lexicon, edge,
						sourceNode, targetNode, edge_name, proteinLength);
			}

			if (taskMonitor != null) {
				summary_processed++;
				int new_progress = (int) ((double) summary_processed / (total_edges) * 100);
				if (new_progress > old_progress) {
					old_progress = new_progress;

					taskMonitor.showMessage(TaskMonitor.Level.INFO,
							"Setting styles on the edges: " + old_progress + "%");
				}
			}
		}

		if (ContainsInterLink && !ContainsIntraLink && intraLinks.size() > 0)
			IsMixedNode = true;

		if (!HasAdjacentEdges || IsMixedNode) { // Node is alone and it does not have adjacent
			if (taskMonitor != null) {
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting styles on the intra link edges: 98%");
			}

			plotIntraLinks(myNetwork, nodeView, netView, handleFactory, bendFactory, style, proteinLength, intraLinks);
		}

		UpdateViewListener.isNodeModified = true;
		LoadProteinDomainTask.isPlotDone = true;
		return true;
	}

	/**
	 * Plot all intralinks
	 * 
	 * @param source
	 * @param target
	 */
	private static void plotIntraLinks(CyNetwork myNetwork, View<CyNode> nodeView, CyNetworkView netView,
			HandleFactory handleFactory, BendFactory bendFactory, VisualStyle style, float proteinLength,
			ArrayList<CrossLink> intraLinks) {

		double initial_positionX_node = getXPositionOf(nodeView);
		double initial_positionY_node = getYPositionOf(nodeView);
		double center_position_node = proteinLength / 2.0;

		double x_or_y_Pos_source = 0;
		double xl_pos_source = 0;

		double x_or_y_Pos_target = 0;
		double xl_pos_target = 0;

		for (int countEdge = 0; countEdge < intraLinks.size(); countEdge++) {

			final String egde_name_added_by_app = "Edge" + countEdge + " [Source: "
					+ intraLinks.get(countEdge).protein_a + " (" + intraLinks.get(countEdge).pos_site_a + ")] [Target: "
					+ intraLinks.get(countEdge).protein_b + " (" + intraLinks.get(countEdge).pos_site_b + ")]";

			CyEdge current_edge = getEdge(myNetwork, egde_name_added_by_app);
			if (current_edge == null) {// Add a new edge if does not exist

				String node_name_source = intraLinks.get(countEdge).protein_a + " ["
						+ intraLinks.get(countEdge).pos_site_a + " - " + intraLinks.get(countEdge).pos_site_b
						+ "] - Source";

				CyNode new_node_source = myNetwork.addNode();
				myNetwork.getRow(new_node_source).set(CyNetwork.NAME, node_name_source);

				String node_name_target = intraLinks.get(countEdge).protein_a + " ["
						+ intraLinks.get(countEdge).pos_site_a + " - " + intraLinks.get(countEdge).pos_site_b
						+ "] - Target";

				CyNode new_node_target = myNetwork.addNode();
				myNetwork.getRow(new_node_target).set(CyNetwork.NAME, node_name_target);

				CyEdge newEdge = myNetwork.addEdge(new_node_source, new_node_target, true);
				myNetwork.getRow(newEdge).set(CyNetwork.NAME, egde_name_added_by_app);

				View<CyEdge> newEdgeView = netView.getEdgeView(newEdge);
				while (newEdgeView == null) {
					// Apply the change to the view
					style.apply(netView);
					netView.updateView();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					newEdgeView = netView.getEdgeView(newEdge);
				}
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT, IntraLinksColor);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, edge_link_opacity);

				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL, "[" + intraLinks.get(countEdge).pos_site_a
						+ "] - [" + intraLinks.get(countEdge).pos_site_b + "]");
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_TOOLTIP, "[" + intraLinks.get(countEdge).pos_site_a
						+ "] - [" + intraLinks.get(countEdge).pos_site_b + "]");
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, edge_label_font_size);

				if (showLinksLegend) {
					newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, edge_label_opacity);
					newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, edge_label_font_size);
				} else {
					newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 0);
				}
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH, 2.0);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);

				xl_pos_source = intraLinks.get(countEdge).pos_site_a;
				if (xl_pos_source <= center_position_node) { // [-protein_length/2, 0]
					x_or_y_Pos_source = (-center_position_node) + xl_pos_source;
				} else { // [0, protein_length/2]
					x_or_y_Pos_source = xl_pos_source - center_position_node;
				}
				if (isProtein_expansion_horizontal) {
					x_or_y_Pos_source += initial_positionX_node;
				} else {
					x_or_y_Pos_source += initial_positionY_node;
				}

				xl_pos_target = intraLinks.get(countEdge).pos_site_b;
				if (xl_pos_target <= center_position_node) { // [-protein_length/2, 0]
					x_or_y_Pos_target = (-center_position_node) + xl_pos_target;
				} else { // [0, protein_length/2]
					x_or_y_Pos_target = xl_pos_target - center_position_node;
				}
				if (isProtein_expansion_horizontal) {
					x_or_y_Pos_target += initial_positionX_node;
				} else {
					x_or_y_Pos_target += initial_positionY_node;
				}

				View<CyNode> new_node_source_view = netView.getNodeView(new_node_source);

				if (isProtein_expansion_horizontal) {
					new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, x_or_y_Pos_source);
					new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, initial_positionY_node);
				} else {
					new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, initial_positionX_node);
					new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, x_or_y_Pos_source);
				}

				new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 0.01);
				new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 0);
				new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_LABEL, "");

				View<CyNode> new_node_target_view = netView.getNodeView(new_node_target);
				if (isProtein_expansion_horizontal) {
					new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, x_or_y_Pos_target);
					new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, initial_positionY_node);
				} else {
					new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, initial_positionX_node);
					new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, x_or_y_Pos_target);
				}

				new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 0.01);
				new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 0);
				new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_LABEL, "");

				Bend bend = bendFactory.createBend();

				double x_or_y_Pos = (x_or_y_Pos_source + x_or_y_Pos_target) / 2;

				if (Math.abs(x_or_y_Pos_source) > Math.abs(x_or_y_Pos_target)) {
					x_or_y_Pos = Math.abs(x_or_y_Pos_source) - Math.abs(x_or_y_Pos);
				} else {
					x_or_y_Pos = Math.abs(x_or_y_Pos_target) - Math.abs(x_or_y_Pos);
				}
				x_or_y_Pos += 50;
				if (isProtein_expansion_horizontal) {
					x_or_y_Pos += initial_positionY_node;
				} else {
					x_or_y_Pos += initial_positionX_node;
				}

				Handle h = null;
				if (isProtein_expansion_horizontal) {
					h = handleFactory.createHandle(netView, newEdgeView, (x_or_y_Pos_source + x_or_y_Pos_target) / 2,
							x_or_y_Pos);
				} else {
					h = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos,
							(x_or_y_Pos_source + x_or_y_Pos_target) / 2);
				}

				bend.insertHandleAt(0, h);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bend);

			} else { // Update edge position

				View<CyEdge> edgeView = netView.getEdgeView(current_edge);
				updateIntraLinkEdgesPosition(myNetwork, netView, edgeView, intraLinks, countEdge, x_or_y_Pos_source,
						xl_pos_source, x_or_y_Pos_target, xl_pos_target, center_position_node, initial_positionX_node,
						initial_positionY_node);
			}
		}
	}

	/**
	 * Method responsible for getting edge from a name
	 * 
	 * @param edge_name
	 * @return
	 */
	public static CyEdge getEdge(CyNetwork myNetwork, final String edge_name) {

		CyEdge _edge = null;

		if (myNetwork == null)
			return _edge;

		// Check if the node exists in the network
		Optional<CyRow> isEdgePresent = myNetwork.getDefaultEdgeTable().getAllRows().stream()
				.filter(new Predicate<CyRow>() {
					public boolean test(CyRow o) {
						return o.get(CyNetwork.NAME, String.class).equals(edge_name);
					}
				}).findFirst();

		if (isEdgePresent.isPresent()) {// Get node if exists
			CyRow _node_row = isEdgePresent.get();
			_edge = myNetwork.getEdge(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));
		}

		return _edge;
	}

	/**
	 * Method responsible for getting node from a name
	 * 
	 * @param node_name
	 * @return
	 */
	public static CyNode getNode(CyNetwork myNetwork, final String node_name) {

		CyNode _node = null;

		if (myNetwork == null)
			return _node;

		// Check if the node exists in the network
		Optional<CyRow> isNodePresent = myNetwork.getDefaultNodeTable().getAllRows().stream()
				.filter(new Predicate<CyRow>() {
					public boolean test(CyRow o) {
						return o.get(CyNetwork.NAME, String.class).equals(node_name);
					}
				}).findFirst();

		if (isNodePresent.isPresent()) {// Get node if exists
			CyRow _node_row = isNodePresent.get();
			_node = myNetwork.getNode(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));
		}

		return _node;
	}

	/**
	 * Method responsible for getting node from a SUID
	 * 
	 * @param node_suid
	 * @return
	 */
	public static CyNode getNode(CyNetwork myNetwork, final Long node_suid) {

		CyNode _node = null;

		if (myNetwork == null)
			return _node;

		// Check if the node exists in the network
		Optional<CyRow> isNodePresent = myNetwork.getDefaultNodeTable().getAllRows().stream()
				.filter(new Predicate<CyRow>() {
					public boolean test(CyRow o) {
						return o.get(CyNetwork.SUID, Long.class).equals(node_suid);
					}
				}).findFirst();

		if (isNodePresent.isPresent()) {// Get node if exists
			CyRow _node_row = isNodePresent.get();
			_node = myNetwork.getNode(Long.parseLong(_node_row.getRaw(CyIdentifiable.SUID).toString()));
		}

		return _node;
	}

	/**
	 * Plot all inter-links
	 * 
	 * @param sourceNode
	 * @param targetNode
	 */
	private static void plotInterLinks(CyNetwork myNetwork, View<CyNode> nodeView, CyNetworkView netView,
			HandleFactory handleFactory, BendFactory bendFactory, VisualStyle style, CyNode node, CyNode sourceNode,
			CyNode targetNode, VisualLexicon lexicon, float proteinLength, ArrayList<CrossLink> interLinks) {

		View<CyNode> sourceNodeView = netView.getNodeView(sourceNode);
		View<CyNode> targetNodeView = netView.getNodeView(targetNode);

		final String source_node_name = myNetwork.getDefaultNodeTable().getRow(sourceNode.getSUID())
				.getRaw(CyNetwork.NAME).toString();
		final String target_node_name = myNetwork.getDefaultNodeTable().getRow(targetNode.getSUID())
				.getRaw(CyNetwork.NAME).toString();

		double x_or_y_Pos_source = 0;
		double xl_pos_source = 0;

		double x_or_y_Pos_target = 0;
		double xl_pos_target = 0;

		double center_position_source_node = 0;
		double center_position_target_node = 0;

		double initial_position_source_node = 0;
		double initial_position_target_node = 0;

		Object length_other_protein_a;
		Object length_other_protein_b;
		CyRow other_node_row = null;

		if (sourceNode.getSUID() == node.getSUID()) {
			other_node_row = myNetwork.getRow(targetNode);
		} else {
			other_node_row = myNetwork.getRow(sourceNode);
		}

		length_other_protein_a = other_node_row.getRaw(PROTEIN_LENGTH_A);
		length_other_protein_b = other_node_row.getRaw(PROTEIN_LENGTH_B);

		if (length_other_protein_a == null) {
			if (length_other_protein_b == null)
				length_other_protein_a = 10;
			else
				length_other_protein_a = length_other_protein_b;
		}

		List<CrossLink> current_inter_links = new ArrayList<CrossLink>(interLinks);

		current_inter_links.removeIf(new Predicate<CrossLink>() {

			public boolean test(CrossLink o) {
				return !(o.protein_a.equals(source_node_name) && o.protein_b.equals(target_node_name)
						|| o.protein_a.equals(target_node_name) && o.protein_b.equals(source_node_name));
			}
		});

		float other_node_width_or_height = 0;
		if (current_inter_links.size() > 0) {
			if (isProtein_expansion_horizontal) {

				initial_position_source_node = getXPositionOf(sourceNodeView);
				initial_position_target_node = getXPositionOf(targetNodeView);

				other_node_width_or_height = ((Number) targetNodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH))
						.floatValue();

				if (other_node_width_or_height == proteinLength)
					other_node_width_or_height = ((Number) sourceNodeView
							.getVisualProperty(BasicVisualLexicon.NODE_WIDTH)).floatValue();
			} else {

				initial_position_source_node = getYPositionOf(sourceNodeView);
				initial_position_target_node = getYPositionOf(targetNodeView);

				other_node_width_or_height = ((Number) targetNodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT))
						.floatValue();

				if (other_node_width_or_height == proteinLength)
					other_node_width_or_height = ((Number) sourceNodeView
							.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT)).floatValue();
			}

			if (sourceNode.getSUID() == node.getSUID()) {
				center_position_source_node = proteinLength / 2.0;
				center_position_target_node = other_node_width_or_height / 2.0;

			} else {
				center_position_source_node = other_node_width_or_height / 2.0;
				center_position_target_node = proteinLength / 2.0;
			}
		} else {
			return;
		}

		for (int countEdge = 0; countEdge < current_inter_links.size(); countEdge++) {

			if (!myNetwork.getDefaultNodeTable().getRow(targetNode.getSUID()).getRaw(CyNetwork.NAME).toString()
					.equals(current_inter_links.get(countEdge).protein_b))
				continue;

			final String egde_name_added_by_app = "[Source: " + current_inter_links.get(countEdge).protein_a + " ("
					+ current_inter_links.get(countEdge).pos_site_a + ")] [Target: "
					+ current_inter_links.get(countEdge).protein_b + " ("
					+ current_inter_links.get(countEdge).pos_site_b + ")] - Edge" + countEdge;

			CyEdge newEdge = getEdge(myNetwork, egde_name_added_by_app);
			if (newEdge == null) {// Add a new edge if does not exist

				newEdge = myNetwork.addEdge(sourceNode, targetNode, true);// INTERLINK
				myNetwork.getRow(newEdge).set(CyNetwork.NAME, egde_name_added_by_app);

				View<CyEdge> newEdgeView = netView.getEdgeView(newEdge);
				while (newEdgeView == null) {
					// Apply the change to the view
					style.apply(netView);
					netView.updateView();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					newEdgeView = netView.getEdgeView(newEdge);
				}
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT, InterLinksColor);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, edge_link_opacity);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH, 2.0);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);

				// ##### EDGE_LABEL ########
				String blank_spaces = edge_label_blank_spaces;
				for (int count_bs = 0; count_bs < countEdge; count_bs++) {
					blank_spaces += edge_label_blank_spaces;
				}
				blank_spaces += ".";

				String mainLabel = current_inter_links.get(countEdge).protein_a + " ["
						+ current_inter_links.get(countEdge).pos_site_a + "] - "
						+ current_inter_links.get(countEdge).protein_b + " ["
						+ current_inter_links.get(countEdge).pos_site_b + "]" + blank_spaces;

				VisualProperty<?> vp_edge_label = lexicon.lookup(CyEdge.class, "EDGE_LABEL");
				if (vp_edge_label != null) {

					Object edge_label = vp_edge_label.parseSerializableString(mainLabel);

					if (edge_label != null)
						newEdgeView.setLockedValue(vp_edge_label, edge_label);

				}

				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_TOOLTIP,
						current_inter_links.get(countEdge).protein_a + " ["
								+ current_inter_links.get(countEdge).pos_site_a + "] - "
								+ current_inter_links.get(countEdge).protein_b + " ["
								+ current_inter_links.get(countEdge).pos_site_b + "]");

				if (showLinksLegend) {
					newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, edge_label_opacity);
					newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, edge_label_font_size);
				} else {
					newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 0);
				}

				// #########################

				xl_pos_source = current_inter_links.get(countEdge).pos_site_a;
				xl_pos_target = current_inter_links.get(countEdge).pos_site_b;

				if (xl_pos_source <= center_position_source_node) { // [-protein_length/2, 0]
					x_or_y_Pos_source = (-center_position_source_node) + xl_pos_source;
				} else { // [0, protein_length/2]
					x_or_y_Pos_source = xl_pos_source - center_position_source_node;
				}
				x_or_y_Pos_source += initial_position_source_node;
				if (xl_pos_target <= center_position_target_node) { // [-protein_length/2, 0]
					x_or_y_Pos_target = (-center_position_target_node) + xl_pos_target;
				} else { // [0, protein_length/2]
					x_or_y_Pos_target = xl_pos_target - center_position_target_node;
				}
				x_or_y_Pos_target += initial_position_target_node;

				// ########## GET EDGE_BEND STYLE TO MODIFY #########

				Bend bend = bendFactory.createBend();

				Handle h = null;
				Handle h2 = null;

				if (other_node_width_or_height == ((Number) length_other_protein_a).floatValue()) {// Target node has
																									// already been
																									// modified

					if (isProtein_expansion_horizontal) {

						h = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_source - OFFSET,
								getYPositionOf(sourceNodeView));
						h2 = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_target - OFFSET,
								getYPositionOf(targetNodeView));
					} else {
						// modified
						h = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(sourceNodeView),
								x_or_y_Pos_source - OFFSET);
						h2 = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(targetNodeView),
								x_or_y_Pos_target - OFFSET);
					}

					bend.insertHandleAt(0, h);
					bend.insertHandleAt(1, h2);

				} else {// Target node is intact

					if (isProtein_expansion_horizontal) {
						if (sourceNode.getSUID() == node.getSUID()) {
							h = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_source - OFFSET,
									getYPositionOf(sourceNodeView));
						} else {
							h = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_target - OFFSET,
									getYPositionOf(targetNodeView));
						}
					} else {
						if (sourceNode.getSUID() == node.getSUID()) {
							h = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(sourceNodeView),
									x_or_y_Pos_source - OFFSET);
						} else {
							h = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(targetNodeView),
									x_or_y_Pos_target - OFFSET);
						}
					}

					bend.insertHandleAt(0, h);
				}

				newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bend);

				VisualProperty<?> vp_edge_curved = lexicon.lookup(CyEdge.class, "EDGE_CURVED");
				if (vp_edge_curved != null) {
					Object edge_curved_obj = vp_edge_curved.parseSerializableString("false");
					if (edge_curved_obj != null) {
						newEdgeView.setLockedValue(vp_edge_curved, edge_curved_obj);
					}
				}
			}
		}
	}

	/**
	 * Update all edges that represent interlinks according to their current
	 * position
	 * 
	 * @param edge
	 */
	private static void updateInterLinkEdgesPosition(CyNetwork myNetwork, CyNode node, CyNetworkView netView,
			HandleFactory handleFactory, BendFactory bendFactory, VisualStyle style, VisualLexicon lexicon, CyEdge edge,
			CyNode sourceNode, CyNode targetNode, String edge_name, float proteinLength) {

		View<CyEdge> newEdgeView = netView.getEdgeView(edge);
		while (newEdgeView == null) {
			// Apply the change to the view
			style.apply(netView);
			netView.updateView();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			newEdgeView = netView.getEdgeView(edge);
		}

		View<CyNode> sourceNodeView = netView.getNodeView(sourceNode);
		View<CyNode> targetNodeView = netView.getNodeView(targetNode);

		Object length_other_protein_a;
		Object length_other_protein_b;
		CyRow other_node_row = null;

		if (sourceNode.getSUID() == node.getSUID()) {
			other_node_row = myNetwork.getRow(targetNode);
		} else {
			other_node_row = myNetwork.getRow(sourceNode);
		}

		length_other_protein_a = other_node_row.getRaw(PROTEIN_LENGTH_A);
		length_other_protein_b = other_node_row.getRaw(PROTEIN_LENGTH_B);

		if (length_other_protein_a == null) {
			if (length_other_protein_b == null)
				length_other_protein_a = 10;
			else
				length_other_protein_a = length_other_protein_b;
		}

		/**
		 * Modify node style
		 */

		float other_node_width_or_height = 0;

		double initial_position_source_node = 0;
		double initial_position_target_node = 0;

		if (isProtein_expansion_horizontal) {
			other_node_width_or_height = ((Number) targetNodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH))
					.floatValue();
			if (other_node_width_or_height == proteinLength)
				other_node_width_or_height = ((Number) sourceNodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH))
						.floatValue();

			initial_position_source_node = getXPositionOf(sourceNodeView);
			initial_position_target_node = getXPositionOf(targetNodeView);
		} else {
			other_node_width_or_height = ((Number) targetNodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT))
					.floatValue();
			if (other_node_width_or_height == proteinLength)
				other_node_width_or_height = ((Number) sourceNodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT))
						.floatValue();

			initial_position_source_node = getYPositionOf(sourceNodeView);
			initial_position_target_node = getYPositionOf(targetNodeView);
		}

		double center_position_source_node = 0;
		double center_position_target_node = 0;

		if (sourceNode.getSUID() == node.getSUID()) {
			center_position_source_node = proteinLength / 2.0;
			center_position_target_node = other_node_width_or_height / 2.0;

		} else {
			center_position_source_node = other_node_width_or_height / 2.0;
			center_position_target_node = proteinLength / 2.0;
		}

		double x_or_y_Pos_source = 0;
		double xl_pos_source = 0;

		double x_or_y_Pos_target = 0;
		double xl_pos_target = 0;

		String[] edgeNameArr = edge_name.split("\\[|\\]");

		String[] position1 = edgeNameArr[1].split("\\(|\\)");
		String[] position2 = edgeNameArr[3].split("\\(|\\)");
		xl_pos_source = Double.parseDouble(position1[1]);
		xl_pos_target = Double.parseDouble(position2[1]);

		if (xl_pos_source <= center_position_source_node) { // [-protein_length/2, 0]
			x_or_y_Pos_source = (-center_position_source_node) + xl_pos_source;
		} else { // [0, protein_length/2]
			x_or_y_Pos_source = xl_pos_source - center_position_source_node;
		}
		x_or_y_Pos_source += initial_position_source_node;
		if (xl_pos_target <= center_position_target_node) { // [-protein_length/2, 0]
			x_or_y_Pos_target = (-center_position_target_node) + xl_pos_target;
		} else { // [0, protein_length/2]
			x_or_y_Pos_target = xl_pos_target - center_position_target_node;
		}
		x_or_y_Pos_target += initial_position_target_node;

		// BLEND

		// ########## GET EDGE_BEND STYLE TO MODIFY #########

		Bend bend = bendFactory.createBend();

		Handle h = null;
		Handle h2 = null;

		if (other_node_width_or_height == ((Number) length_other_protein_a).floatValue()) {// Target node has already
																							// been modified

			if (isProtein_expansion_horizontal) {
				h = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_source - OFFSET,
						getYPositionOf(sourceNodeView));
				h2 = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_target - OFFSET,
						getYPositionOf(targetNodeView));
			} else {
				h = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(sourceNodeView),
						x_or_y_Pos_source - OFFSET);
				h2 = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(targetNodeView),
						x_or_y_Pos_target - OFFSET);
			}

			bend.insertHandleAt(0, h);
			bend.insertHandleAt(1, h2);

		} else {// Target node is intact

			if (isProtein_expansion_horizontal) {
				if (sourceNode.getSUID() == node.getSUID()) {
					h = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_source - OFFSET,
							getYPositionOf(sourceNodeView));
				} else {
					h = handleFactory.createHandle(netView, newEdgeView, x_or_y_Pos_target - OFFSET,
							getYPositionOf(targetNodeView));
				}
			} else {
				if (sourceNode.getSUID() == node.getSUID()) {
					h = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(sourceNodeView),
							x_or_y_Pos_source - OFFSET);
				} else {
					h = handleFactory.createHandle(netView, newEdgeView, getXPositionOf(targetNodeView),
							x_or_y_Pos_target - OFFSET);
				}
			}

			bend.insertHandleAt(0, h);
		}

		newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bend);

		VisualProperty<?> vp_edge_curved = lexicon.lookup(CyEdge.class, "EDGE_CURVED");
		if (vp_edge_curved != null) {
			Object edge_curved_obj = vp_edge_curved.parseSerializableString("false");
			if (edge_curved_obj != null) {
				newEdgeView.setLockedValue(vp_edge_curved, edge_curved_obj);
			}
		}

		newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);

		// #### UPDATE EDGE COLOR ####
		newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT, InterLinksColor);
		newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, edge_link_opacity);
		// ###########################

		// ### DISPLAY LINK LEGEND ###
		if (showLinksLegend) {
			newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, edge_label_opacity);
			newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, edge_label_font_size);
		} else {
			newEdgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 0);
		}
		// ###########################
	}

	/**
	 * Update all edges that represent intralinks according to their current
	 * position
	 * 
	 * @param countEdge
	 * @param x_or_y_Pos_source
	 * @param xl_pos_source
	 * @param x_or_y_Pos_target
	 * @param xl_pos_target
	 * @param center_position_node
	 * @param initial_positionX_node
	 * @param initial_positionY_node
	 */
	private static void updateIntraLinkEdgesPosition(CyNetwork myNetwork, CyNetworkView netView, View<CyEdge> edgeView,
			ArrayList<CrossLink> intraLinks, int countEdge, double x_or_y_Pos_source, double xl_pos_source,
			double x_or_y_Pos_target, double xl_pos_target, double center_position_node, double initial_positionX_node,
			double initial_positionY_node) {

		final String node_name_source = intraLinks.get(countEdge).protein_a + " ["
				+ intraLinks.get(countEdge).pos_site_a + " - " + intraLinks.get(countEdge).pos_site_b + "] - Source";

		CyNode new_node_source = getNode(myNetwork, node_name_source);
		if (new_node_source != null) {

			xl_pos_source = intraLinks.get(countEdge).pos_site_a;
			if (xl_pos_source <= center_position_node) { // [-protein_length/2, 0]
				x_or_y_Pos_source = (-center_position_node) + xl_pos_source;
			} else { // [0, protein_length/2]
				x_or_y_Pos_source = xl_pos_source - center_position_node;
			}
			if (isProtein_expansion_horizontal) {
				x_or_y_Pos_source += initial_positionX_node;
			} else {
				x_or_y_Pos_source += initial_positionY_node;
			}

			View<CyNode> new_node_source_view = netView.getNodeView(new_node_source);
			if (isProtein_expansion_horizontal) {
				new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, x_or_y_Pos_source);
				new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, initial_positionY_node);
			} else {
				new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, initial_positionX_node);
				new_node_source_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, x_or_y_Pos_source);
			}
		}

		final String node_name_target = intraLinks.get(countEdge).protein_a + " ["
				+ intraLinks.get(countEdge).pos_site_a + " - " + intraLinks.get(countEdge).pos_site_b + "] - Target";

		CyNode new_node_target = getNode(myNetwork, node_name_target);
		if (new_node_target != null) {

			xl_pos_target = intraLinks.get(countEdge).pos_site_b;
			if (xl_pos_target <= center_position_node) { // [-protein_length/2, 0]
				x_or_y_Pos_target = (-center_position_node) + xl_pos_target;
			} else { // [0, protein_length/2]
				x_or_y_Pos_target = xl_pos_target - center_position_node;
			}
			if (isProtein_expansion_horizontal) {
				x_or_y_Pos_target += initial_positionX_node;
			} else {
				x_or_y_Pos_target += initial_positionY_node;
			}

			View<CyNode> new_node_target_view = netView.getNodeView(new_node_target);
			if (isProtein_expansion_horizontal) {
				new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, x_or_y_Pos_target);
				new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, initial_positionY_node);
			} else {
				new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_X_LOCATION, initial_positionX_node);
				new_node_target_view.setLockedValue(BasicVisualLexicon.NODE_Y_LOCATION, x_or_y_Pos_target);
			}
		}

		// #### UPDATE EDGE COLOR ####
		edgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT, IntraLinksColor);
		edgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, edge_link_opacity);
		// ###########################

		// ### DISPLAY LINK LEGEND ###
		if (showLinksLegend) {
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, edge_label_opacity);
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, edge_label_font_size);
		} else {
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 0);
		}
		// ###########################
	}

	/**
	 * Check and update all edges of associated nodes
	 */
	public static void updateAllAssiciatedInterlinkNodes(CyNetwork myNetwork, CyApplicationManager cyApplicationManager,
			CyNetworkView netView, HandleFactory handleFactory, BendFactory bendFactory, CyNode current_node) {

		if (myNetwork == null) {
			myNetwork = cyApplicationManager.getCurrentNetwork();
			netView = cyApplicationManager.getCurrentNetworkView();
		}

		Object length_other_protein_a;
		Object length_other_protein_b;

		Set<Long> nodeSuidList = new HashSet<Long>();
		nodeSuidList.add(current_node.getSUID());

		for (CyEdge edge : myNetwork.getAdjacentEdgeIterable(current_node, CyEdge.Type.ANY)) {
			CyNode sourceNode = myNetwork.getEdge(edge.getSUID()).getSource();
			CyNode targetNode = myNetwork.getEdge(edge.getSUID()).getTarget();

			if (sourceNode.getSUID() == current_node.getSUID())
				nodeSuidList.add(targetNode.getSUID());
			else
				nodeSuidList.add(sourceNode.getSUID());
		}

		VisualStyle style = MainSingleNodeTask.style;
		if (style == null)
			style = LoadProteinDomainTask.style;

		if (style == null)
			return;

		VisualLexicon lexicon = MainSingleNodeTask.lexicon;
		if (lexicon == null)
			lexicon = LoadProteinDomainTask.lexicon;

		if (lexicon == null)
			return;

		for (final Long protein_suid : nodeSuidList) {

			try {

				CyNode proteinA_node = getNode(myNetwork, protein_suid);
				if (proteinA_node != null) {

					CyRow proteinA_node_row = myNetwork.getRow(proteinA_node);

					length_other_protein_a = proteinA_node_row.getRaw("length_protein_a");
					length_other_protein_b = proteinA_node_row.getRaw("length_protein_b");

					if (length_other_protein_a == null) {
						if (length_other_protein_b == null)
							length_other_protein_a = 10;
						else
							length_other_protein_a = length_other_protein_b;
					}

					if (Util.IsNodeModified(myNetwork, netView, style, proteinA_node)) {
						MainSingleNodeTask.node = proteinA_node;
						MainSingleNodeTask.proteinLength = ((Number) length_other_protein_a).floatValue();

						Tuple2 inter_and_intralinks = Util.getAllLinksFromAdjacentEdgesNode(proteinA_node, myNetwork);
						MainSingleNodeTask.interLinks = (ArrayList<CrossLink>) inter_and_intralinks.getFirst();
						MainSingleNodeTask.intraLinks = (ArrayList<CrossLink>) inter_and_intralinks.getSecond();

						View<CyNode> proteinA_nodeView = netView.getNodeView(proteinA_node);
						addOrUpdateEdgesToNetwork(myNetwork, proteinA_node, style, netView, proteinA_nodeView,
								handleFactory, bendFactory, lexicon, ((Number) length_other_protein_a).floatValue(),
								MainSingleNodeTask.intraLinks, MainSingleNodeTask.interLinks, null);

						if (current_node != null) {
							inter_and_intralinks = Util.getAllLinksFromAdjacentEdgesNode(current_node, myNetwork);// update
																													// intraLinks
																													// &
																													// interLinks
																													// with
																													// the
																													// current
																													// selected
																													// node
							MainSingleNodeTask.interLinks = (ArrayList<CrossLink>) inter_and_intralinks.getFirst();
							MainSingleNodeTask.intraLinks = (ArrayList<CrossLink>) inter_and_intralinks.getSecond();
						}
					}
				}
			} catch (Exception e) {
				continue;
			}

		}
	}

	/**
	 * Get all links from adjacent edges of a node
	 */
	public static Tuple2<ArrayList<CrossLink>, ArrayList<CrossLink>> getAllLinksFromAdjacentEdgesNode(CyNode node,
			CyNetwork myNetwork) {

		if (node == null || myNetwork == null) {
			return new Tuple2(new ArrayList<CrossLink>(), new ArrayList<CrossLink>());
		}
		Set<Long> nodeSuidList = new HashSet<Long>();
		nodeSuidList.add(node.getSUID());

		for (CyEdge edge : myNetwork.getAdjacentEdgeIterable(node, CyEdge.Type.ANY)) {
			CyNode sourceNode = myNetwork.getEdge(edge.getSUID()).getSource();
			CyNode targetNode = myNetwork.getEdge(edge.getSUID()).getTarget();

			if (sourceNode.getSUID() == node.getSUID())
				nodeSuidList.add(targetNode.getSUID());
			else
				nodeSuidList.add(sourceNode.getSUID());
		}

		Set<String> cross_links_set = new HashSet<String>();
		for (Long nodeSUID : nodeSuidList) {
			CyNode currentNode = myNetwork.getNode(nodeSUID);

			CyRow myCurrentRow = myNetwork.getRow(currentNode);

			if (myCurrentRow.getRaw(XL_PROTEIN_A_B) != null) {
				cross_links_set.addAll(Arrays.asList(myCurrentRow.getRaw(XL_PROTEIN_A_B).toString().split("#")));
			}
			if (myCurrentRow.getRaw(XL_PROTEIN_B_A) != null) {
				cross_links_set.addAll(Arrays.asList(myCurrentRow.getRaw(XL_PROTEIN_B_A).toString().split("#")));
			}
		}

		// ############ GET ALL EDGES THAT BELONG TO THE SELECTED NODE #############

		final String selected_node_name = myNetwork.getDefaultNodeTable().getRow(node.getSUID()).getRaw(CyNetwork.NAME)
				.toString();

		// Get only links that belong to the selected node
		cross_links_set.removeIf(new Predicate<String>() {

			public boolean test(String xl) {
				if (xl.equals("0") || xl.equals("NA"))
					return true;
				String[] current_xl = xl.split("-");
				return !(current_xl[0].equals(selected_node_name) || current_xl[2].equals(selected_node_name));
			}
		});

		List<CrossLink> interLinks = new ArrayList<CrossLink>();
		List<CrossLink> intraLinks = new ArrayList<CrossLink>();

		for (String xl : cross_links_set) {

			try {
				String[] current_xl = xl.split("-");

				if (current_xl[0].equals(current_xl[2])) {// it's intralink

					int pos_a = Integer.parseInt(current_xl[1]);
					int pos_b = Integer.parseInt(current_xl[3]);
					if (pos_a > pos_b) {
						int tmp_ = pos_a;
						pos_a = pos_b;
						pos_b = tmp_;
					}
					intraLinks.add(new CrossLink(current_xl[0], current_xl[2], pos_a, pos_b));

				} else {// it's interlink

					interLinks.add(new CrossLink(current_xl[0], current_xl[2], Integer.parseInt(current_xl[1]),
							Integer.parseInt(current_xl[3])));

				}
			} catch (Exception e) {
			}
		}

		Collections.sort(interLinks);
		Collections.sort(intraLinks);

		return new Tuple2(interLinks, intraLinks);
	}

	/**
	 * Get protein domains in PFam database
	 */
	public static ArrayList<ProteinDomain> getProteinDomains(CyRow myCurrentRow) {
		Object protein_a_name = myCurrentRow.getRaw(PROTEIN_A);
		Object protein_b_name = myCurrentRow.getRaw(PROTEIN_B);

		if (protein_a_name == null) {
			if (protein_b_name == null)
				protein_a_name = 10;
			else
				protein_a_name = protein_b_name;
		}

		String ptnID = protein_a_name.toString();
		String[] cols = ptnID.split("\\|");

		// ############ GET PROTEIN DOMAINS #################
		ArrayList<ProteinDomain> pFamProteinDomains = new ArrayList<ProteinDomain>(0);
		if (cols.length == 3) { // Correct format: sp|XXX|YYYY or tr|XXX|YYY
			pFamProteinDomains = getProteinDomains(cols[1]);
			Collections.sort(pFamProteinDomains);
		}
		// ############################### END ################################

		return pFamProteinDomains;
	}

	/**
	 * Connect to PFam and get domains
	 * 
	 * @param proteinID
	 * @return
	 */
	private static ArrayList<ProteinDomain> getProteinDomains(String proteinID) {

		try {
			String _url = "https://pfam.xfam.org/protein?entry=" + proteinID + "&output=xml";
			final URL url = new URL(_url);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setReadTimeout(1000);
			connection.setConnectTimeout(1000);
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

				// Get Response
				InputStream inputStream = connection.getErrorStream(); // first check for error.
				if (inputStream == null) {
					inputStream = connection.getInputStream();
				}
				BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				StringBuilder response = new StringBuilder();
				while ((line = rd.readLine()) != null) {
					response.append(line);
					response.append('\r');
				}
				rd.close();
				String responseString = response.toString();

				if (responseString.startsWith("<!DOCTYPE html PUBLIC"))
					return new ArrayList<ProteinDomain>();

				// Use method to convert XML string content to XML Document object
				Document doc = convertStringToXMLDocument(responseString);

				if (doc == null)
					return new ArrayList<ProteinDomain>();

				// check if exists error
				NodeList xmlnodes = doc.getElementsByTagName("error");
				if (xmlnodes.getLength() > 0) {
					throw new Exception("XlinkCyNET ERROR: " + xmlnodes.item(0).getNodeValue());
				}

				xmlnodes = doc.getElementsByTagName("matches");

				ArrayList<ProteinDomain> proteinDomainList = new ArrayList<ProteinDomain>();
				for (int i = 0; i < xmlnodes.getLength(); i++) {
					for (int j = 0; j < xmlnodes.item(i).getChildNodes().getLength(); j++) {
						Node nNode = xmlnodes.item(i).getChildNodes().item(j);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							// get attributes names and values
							String domain = nNode.getAttributes().item(1).getNodeValue();
							int startId = Integer
									.parseInt(nNode.getChildNodes().item(1).getAttributes().item(7).getNodeValue());
							int endId = Integer
									.parseInt(nNode.getChildNodes().item(1).getAttributes().item(3).getNodeValue());
							String eValue = nNode.getChildNodes().item(1).getAttributes().item(4).getNodeValue();
							proteinDomainList.add(new ProteinDomain(domain, startId, endId, eValue));
						}
					}
				}
				return proteinDomainList;
			} else {
				return new ArrayList<ProteinDomain>();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ArrayList<ProteinDomain>();
		}
	}

	/**
	 * Convert Pfam object to XML
	 * 
	 * @param xmlString
	 * @return
	 */
	private static Document convertStringToXMLDocument(String xmlString) {
		// Parser that produces DOM object trees from XML content
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// API to obtain DOM Document instance
		DocumentBuilder builder = null;
		try {
			// Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();

			// Parse the content to Document object
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static double getXPositionOf(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
	}

	private static double getYPositionOf(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}
}
