package de.fmp.liulab.internal.action;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.OpenBrowser;

public class ControlURLAction extends AbstractCyAction {

	private static final String MENU_NAME = "About";
	private static final String MENU_CATEGORY = "Apps.XlinkCyNET";
	private static final long serialVersionUID = 1L;
	private OpenBrowser openBrowser;

	/**
	 * Constructor
	 * 
	 * @param openBrowser
	 */
	public ControlURLAction(OpenBrowser openBrowser) {
		super(MENU_NAME);
		setPreferredMenu(MENU_CATEGORY);
		setMenuGravity(4.0f);
		this.openBrowser = openBrowser;
	}

	public void actionPerformed(ActionEvent e) {
		openBrowser.openURL("http://diogobor.droppages.com/xlinkcynet_protocol_exchange.png");
	}
}
