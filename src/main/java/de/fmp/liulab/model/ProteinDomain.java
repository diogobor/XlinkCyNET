package de.fmp.liulab.model;

/**
 * Model class for Protein domains
 * @author borges.diogo
 *
 */
public class ProteinDomain implements Comparable<ProteinDomain> {
	public String name;
	public int startId;
	public int endId;
	public String eValue;
	public java.awt.Color color;

	/**
	 *  Constructor
	 * @param name
	 * @param startId
	 * @param endId
	 * @param eValue
	 */
	public ProteinDomain(String name, int startId, int endId, String eValue) {
		this.name = name;
		this.startId = startId;
		this.endId = endId;
		this.eValue = eValue;
	}

	/**
	 *  Constructor 2
	 * @param name
	 * @param startId
	 * @param endId
	 * @param color
	 */
	public ProteinDomain(String name, int startId, int endId, java.awt.Color color) {
		this.name = name;
		this.startId = startId;
		this.endId = endId;
		this.color = color;
	}

	public int compareTo(ProteinDomain d) {
		return this.startId - d.startId;
	}

	@Override
	public String toString() {
		return "Protein domain {" + this.name + "}";
	}

}