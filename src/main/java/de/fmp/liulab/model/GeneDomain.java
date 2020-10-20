package de.fmp.liulab.model;

import java.util.List;

/**
 * Model class for protein domains of each gene
 * @author borges.diogo
 *
 */
public class GeneDomain implements Comparable<GeneDomain> {

	public String getGeneName;
	public List<ProteinDomain> proteinDomains;

	/**
	 * Constructor
	 * @param geneName
	 * @param proteinDomains
	 */
	public GeneDomain(String geneName, List<ProteinDomain> proteinDomains) {
		this.getGeneName = geneName;
		this.proteinDomains = proteinDomains;
	}

	@Override
	public int compareTo(GeneDomain o) {
		return getGeneName.compareTo(o.getGeneName);
	}

	@Override
	public String toString() {
		return "Gene {" + this.getGeneName + "}";
	}
}