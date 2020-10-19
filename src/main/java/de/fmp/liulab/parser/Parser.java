package de.fmp.liulab.parser;

import java.util.ArrayList;
import java.util.List;

import de.fmp.liulab.task.LoadProteinDomainTask;

public class Parser {

	private ReaderWriterTextFile parserFile;
	private List<String> qtdParser = new ArrayList<String>();
	private String[] columnNames = { "Gene", "Domain(s)" };

	public Parser(String fileName) {
		try {
			parserFile = new ReaderWriterTextFile(fileName);
			while (parserFile.hasLine()) {
				if (!(parserFile.getLine().equals(""))) {
					qtdParser.add(parserFile.getLine());
				}
			}
		} catch (Exception e) {
		}
	}

	public void updateDataModel() {
		
		Object[][] data = new Object[qtdParser.size()][2];
		LoadProteinDomainTask.tableDataModel.setDataVector(data, columnNames);

		int countPtnDomain = 0;
		for (String line : qtdParser) {
			int firstComma = line.indexOf(',');
			String gene = line.substring(0, firstComma);
			String domains = line.substring(firstComma + 1).replace('\"', ' ').trim();
			LoadProteinDomainTask.tableDataModel.setValueAt(gene, countPtnDomain, 0);
			LoadProteinDomainTask.tableDataModel.setValueAt(domains, countPtnDomain, 1);
			countPtnDomain++;
		}
		
		LoadProteinDomainTask.setTableProperties();
	}

}
