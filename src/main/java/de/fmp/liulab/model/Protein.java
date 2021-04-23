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
	public String gene;
	public String fullName;
	public String sequence;
	public String checksum;
	public List<PDB> pdbIds;
	public List<PTM> ptms;

	/**
	 * Constructor
	 * 
	 * @param sequence protein sequence
	 * @param pdbIds   pdb IDs
	 */
	public Protein(String proteinID, String gene, String fullName, String sequence, String checksum, List<PDB> pdbIds, List<PTM> ptms) {
		this.proteinID = proteinID;
		this.gene = gene;
		this.fullName = fullName;
		this.sequence = sequence;
		this.checksum = checksum;
		this.pdbIds = pdbIds;
	}

	/**
	 * Empty constructor
	 */
	public Protein() {
		this.pdbIds = new ArrayList<PDB>();
		this.ptms = new ArrayList<PTM>();
	}
}
