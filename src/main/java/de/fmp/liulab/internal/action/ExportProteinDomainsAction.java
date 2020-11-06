package de.fmp.liulab.internal.action;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;

import de.fmp.liulab.internal.view.ExtensionFileFilter;
import de.fmp.liulab.model.ProteinDomain;
import de.fmp.liulab.utils.Util;

/**
 * Class responsible for exporting protein domains
 * 
 * @author diogobor
 *
 */
public class ExportProteinDomainsAction extends AbstractCyAction {

	private static final String MENU_NAME = "Export";
	private static final String MENU_CATEGORY = "Apps.XlinkCyNET.Protein Domains";
	private static final long serialVersionUID = 1L;
	private CyApplicationManager cyApplicationManager;
	private CyNetwork myNetwork;

	/**
	 * Constructor
	 * 
	 * @param cyApplicationManager
	 */
	public ExportProteinDomainsAction(CyApplicationManager cyApplicationManager) {
		super(MENU_NAME);
		setPreferredMenu(MENU_CATEGORY);
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_D, CTRL_DOWN_MASK));
		this.cyApplicationManager = cyApplicationManager;
	}

	/**
	 * Method responsible for activating action.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		this.myNetwork = cyApplicationManager.getCurrentNetwork();

		if (myNetwork == null) {
			return;
		}

		JOptionPane.showMessageDialog(null, "Selected network: " + myNetwork.toString(),
				"XlinkCyNET - Export protein domains", JOptionPane.INFORMATION_MESSAGE);

		JFrame parentFrame = new JFrame();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new ExtensionFileFilter("CSV file", "csv"));
		fileChooser.setDialogTitle("Save protein domains");

		int userSelection = fileChooser.showSaveDialog(parentFrame);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = getSelectedFileWithExtension(fileChooser);
			String full_fileName = fileToSave.getAbsolutePath();
			if (!full_fileName.toLowerCase().endsWith(".csv")) {
				full_fileName += ".csv";
			}
			this.createProteinDomainsFile(full_fileName);
		}

	}

	/**
	 * Returns the selected file from a JFileChooser, including the extension from
	 * the file filter.
	 */
	public static File getSelectedFileWithExtension(JFileChooser c) {
		File file = c.getSelectedFile();
		if (c.getFileFilter() instanceof FileNameExtensionFilter) {
			String[] exts = ((FileNameExtensionFilter) c.getFileFilter()).getExtensions();
			String nameLower = file.getName().toLowerCase();
			for (String ext : exts) { // check if it already has a valid extension
				if (nameLower.endsWith('.' + ext.toLowerCase())) {
					return file; // if yes, return as-is
				}
			}
			// if not, append the first extension from the selected filter
			file = new File(file.toString() + '.' + exts[0]);
		}
		return file;
	}

	/**
	 * Method responsible for creating the output file with all domains for the
	 * selected network
	 */
	private void createProteinDomainsFile(String fileName) {
		try {

			if (Util.proteinDomainsMap.containsKey(myNetwork.toString())) {

				FileWriter myWriter = new FileWriter(fileName);

				Map<Long, List<ProteinDomain>> all_proteinDomains = Util.proteinDomainsMap.get(myNetwork.toString());
				for (Map.Entry<Long, List<ProteinDomain>> entry : all_proteinDomains.entrySet()) {

					String node_name = myNetwork.getDefaultNodeTable().getRow(entry.getKey()).getRaw(CyNetwork.NAME)
							.toString();

					List<ProteinDomain> current_proteinDomains = entry.getValue();
					StringBuilder sb_domains = new StringBuilder();
					for (ProteinDomain domain : current_proteinDomains) {
						sb_domains.append(domain.name + "[" + domain.startId + "-" + domain.endId + "],");
					}
					myWriter.write(node_name + "," + "\""
							+ sb_domains.toString().substring(0, sb_domains.toString().length() - 1) + "\"\n");
				}

				myWriter.close();
				JOptionPane.showMessageDialog(null, "File has been saved successfully!",
						"XlinkCyNET - Export protein domains", JOptionPane.INFORMATION_MESSAGE);

			} else {// Network does not exists
				JOptionPane.showMessageDialog(null, "Network has not been found!",
						"XlinkCyNET - Export protein domains", JOptionPane.WARNING_MESSAGE);
				return;
			}

		} catch (IOException e) {

			String errorMsg = "ERROR: It is not possible to save the file." + e.getMessage();
			JOptionPane.showMessageDialog(null, errorMsg, "XlinkCyNET - Export protein domains",
					JOptionPane.ERROR_MESSAGE);

		}
	}
}
