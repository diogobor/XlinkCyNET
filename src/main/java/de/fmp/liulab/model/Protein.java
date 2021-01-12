package de.fmp.liulab.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for protein
 * 
 * @author diogobor
 *
 */
public class Protein {

	public String proteinID;
	public String fullName;
	public String sequence;
	public List<String> pdbIds;

	/**
	 * Constructor
	 * 
	 * @param sequence protein sequence
	 * @param pdbIds   pdb IDs
	 */
	public Protein(String proteinID, String fullName, String sequence, List<String> pdbIds) {
		this.proteinID = proteinID;
		this.fullName = fullName;
		this.sequence = sequence;
		this.pdbIds = pdbIds;
	}

	/**
	 * Empty constructor
	 */
	public Protein() {
		this.pdbIds = new ArrayList<String>();
	}
}
