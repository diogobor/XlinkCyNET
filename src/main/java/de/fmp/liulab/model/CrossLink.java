package de.fmp.liulab.model;

/**
 * Model class for Cross links
 * @author borges.diogo
 *
 */
public class CrossLink implements Comparable<CrossLink> {
	public String protein_a;
	public String protein_b;
	public int pos_site_a;
	public int pos_site_b;

	/**
	 * Empty Constructor
	 */
	public CrossLink() {
		
	}
	
	/**
	 * Constructor
	 * @param ptn_a
	 * @param ptn_b
	 * @param pos_a
	 * @param pos_b
	 */
	public CrossLink(String ptn_a, String ptn_b, int pos_a, int pos_b) {
		this.protein_a = ptn_a;
		this.protein_b = ptn_b;
		this.pos_site_a = pos_a;
		this.pos_site_b = pos_b;
	}

	/**
	 * Method responsible for comparing two objects
	 */
	public int compareTo(CrossLink d) {

		int result = this.pos_site_a - d.pos_site_a;
		if (result != 0) { // pos_site_b is equal, then sort by pos_site_b
			return result;
		}

		return this.pos_site_b - d.pos_site_b;
	}

	/**
	 * Convert to string
	 */
	@Override
	public String toString() {
		return "CrossLink {" + this.protein_a + " - " + this.protein_b + " [" + this.pos_site_a + " - "
				+ this.pos_site_b + "]}";
	}
}