package de.fmp.liulab.internal.action;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.OpenBrowser;

/**
 * Class responsible for the Browser action
 * 
 * @author diogobor
 *
 */
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
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		setMenuGravity(4.0f);
		this.openBrowser = openBrowser;
	}

	/**
	 * Method responsible for activating action.
	 */
	public void actionPerformed(ActionEvent e) {
		String msg = "<html><p><b>Developed by The Liu Lab:</b></p><p><b>Diogo Borges</b> (diogobor@gmail.com)</p><p><b>Fan Liu</b> (fliu@fmp-berlin.de)</p><br/><p><b>Version:</b> 1.0.4</p></html>";

		JOptionPane.showMessageDialog(null, msg, "XlinkCyNET - About", JOptionPane.INFORMATION_MESSAGE,
				new ImageIcon(getClass().getResource("/images/logo.png")));

		openBrowser.openURL("https://www.theliulab.com/software/XlinkCyNET");
	}
}
