package de.fmp.liulab.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fmp.liulab.task.LoadPTMsTask;
import de.fmp.liulab.task.LoadProteinDomainTask;

/**
 * Class responsible for parsing input files
 * 
 * @author diogobor
 *
 */
public class Parser {

	private ReaderWriterTextFile parserFile;
	private List<String> qtdParser = new ArrayList<String>();
	private String[] columnNames = { "Node Name", "Domain(s)" };
	private String[] columnNamesPTMTable = { "Node Name", "PTM(s)" };

	/**
	 * UNIPROT lines
	 */
	private String[] uniprot_header_lines;
	private boolean isUniprot;

	/**
	 * Constructor
	 * 
	 * @param fileName file name
	 */
	public Parser(String fileName) {

		try {
			parserFile = new ReaderWriterTextFile(fileName);
			isUniprot = false;
			if (fileName.endsWith(".tab")) {
				parserFile.hasLine();// Load current line to 'line' variable
				uniprot_header_lines = parserFile.getLine().split("\t");// Get the current content in 'line' variable
				isUniprot = true;
			}

			while (parserFile.hasLine()) {
				if (!(parserFile.getLine().equals(""))) {
					qtdParser.add(parserFile.getLine());
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Method responsible for updating data model table
	 * 
	 * @throws Exception
	 */
	public void updateDataModel(boolean isFromPTM) throws Exception {

		StringBuilder sb_data_to_be_stored = new StringBuilder();

		for (String line : qtdParser) {
			try {

				// ##### Load Protein Domains ########
				if (!isFromPTM) {

					String gene_name = "";
					String domains = "";

					if (isUniprot) {
						String[] each_line_cols = line.split("\t");

						int index = Arrays.asList(uniprot_header_lines).indexOf("Gene names");
						if (index == -1)
							index = Arrays.asList(uniprot_header_lines).indexOf("Gene names  (primary )");
						if (index == -1)
							index = Arrays.asList(uniprot_header_lines).indexOf("Gene names  (synonym )");
						if (index == -1)
							index = Arrays.asList(uniprot_header_lines).indexOf("Gene names  (ORF )");
						if (index == -1)
							index = Arrays.asList(uniprot_header_lines).indexOf("Gene names  (ordered locus )");
						if (index == -1)
							return;

						if (index < each_line_cols.length)
							gene_name = each_line_cols[index];

						index = Arrays.asList(uniprot_header_lines).indexOf("Topological domain");
						if (index == -1)
							return;

						String col_topological_domains = "";
						if (index < each_line_cols.length)
							col_topological_domains = each_line_cols[index];

						String[] cols_topol_domains = col_topological_domains.split(";");// e.g. TOPO_DOM 1..83;
																							// /note="Cytoplasmic";
																							// /evidence="ECO:0000255";

						StringBuilder sb_domains = new StringBuilder();
						if (cols_topol_domains.length > 1) {
							for (int i = 0; i < cols_topol_domains.length; i++) {
								String protein_domain_name = cols_topol_domains[i + 1].replace("/note=\"", "")
										.replace("\"", "").replace(",", "-").trim();
								String[] cols_range = cols_topol_domains[i].replace("TOPO_DOM ", "").split("\\.");
								int start_index = Integer.parseInt(cols_range[0].trim());
								int end_index = 0;
								if (cols_range.length == 1) {
									end_index = start_index;
								} else {
									end_index = Integer.parseInt(cols_range[2].trim());
								}
								sb_domains.append(protein_domain_name);
								sb_domains.append("[");
								sb_domains.append(start_index);
								sb_domains.append("-");
								sb_domains.append(end_index);
								sb_domains.append("],");

								if (i + 2 < cols_topol_domains.length && cols_topol_domains[i + 2].contains("evidence"))
									i += 2;
								else
									i += 1;

							}
							domains = sb_domains.toString().substring(0, sb_domains.toString().length() - 1);
						}

					} else {
						int firstComma = line.indexOf(',');
						gene_name = line.substring(0, firstComma);
						domains = line.substring(firstComma + 1).replace('\"', ' ').trim();
					}

					String[] cols_gene = gene_name.split(" ");
					for (String each_gene : cols_gene) {
						if (each_gene.isBlank() || each_gene.isEmpty() || each_gene.trim().equals("\t"))
							continue;
						sb_data_to_be_stored.append(each_gene);
						sb_data_to_be_stored.append("\t");
						sb_data_to_be_stored.append(domains).append("\n");
					}
				}
				// #### Load PTMs #####
				else {
					int firstComma = line.indexOf(',');
					String gene_name = line.substring(0, firstComma);
					String ptms = line.substring(firstComma + 1).replace('\"', ' ').trim();

					String[] cols_gene = gene_name.split(" ");
					for (String each_gene : cols_gene) {
						if (each_gene.isBlank() || each_gene.isEmpty() || each_gene.trim().equals("\t"))
							continue;
						sb_data_to_be_stored.append(each_gene);
						sb_data_to_be_stored.append("\t");
						sb_data_to_be_stored.append(ptms).append("\n");
					}

				}
			} catch (Exception e) {
			}
		}

		if (qtdParser.size() == 0)
			throw new Exception("ERROR: There is an error reading the file.");

		int countPtnDomain = 0;
		String[] data_to_be_stored = sb_data_to_be_stored.toString().split("\n");

		Object[][] data = new Object[data_to_be_stored.length][2];
		if (isFromPTM)
			LoadPTMsTask.ptmTableDataModel.setDataVector(data, columnNamesPTMTable);
		else
			LoadProteinDomainTask.tableDataModel.setDataVector(data, columnNames);

		for (String line : data_to_be_stored) {
			try {
				String[] cols_line = line.split("\t");

				if (isFromPTM)
					LoadPTMsTask.ptmTableDataModel.setValueAt(cols_line[0], countPtnDomain, 0);
				else
					LoadProteinDomainTask.tableDataModel.setValueAt(cols_line[0], countPtnDomain, 0);

				if (cols_line.length > 1) {
					if (isFromPTM)
						LoadPTMsTask.ptmTableDataModel.setValueAt(cols_line[1], countPtnDomain, 1);
					else
						LoadProteinDomainTask.tableDataModel.setValueAt(cols_line[1], countPtnDomain, 1);

				}

			} catch (Exception e) {
				System.out.println();
			} finally {
				countPtnDomain++;
			}

		}

		if (isFromPTM)
			LoadPTMsTask.setTableProperties(countPtnDomain);
		else
			LoadProteinDomainTask.setTableProperties(countPtnDomain);
	}

}
