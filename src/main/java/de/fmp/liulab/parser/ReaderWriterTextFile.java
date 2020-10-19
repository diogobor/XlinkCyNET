package de.fmp.liulab.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Class responsible for reading / writing a file
 * @author borges.diogo
 *
 */
public class ReaderWriterTextFile {

	BufferedReader bf;

	BufferedWriter bw;

	String line;

	/**
	 * Constructor 1: the argument is a file name
	 * @param fileName
	 * @throws IOException
	 */
	public ReaderWriterTextFile(String fileName) throws IOException {
		bf = new BufferedReader(new FileReader(fileName));
	}

	/**
	 * Constructor 2: a new file is created.
	 * @throws IOException
	 */
	public ReaderWriterTextFile() throws IOException {
		bw = new BufferedWriter(new FileWriter("./file/output.txt"));
	}

	/**
	 * 
	 * @return the current line
	 */

	public String getLine() {
		return line;
	}

	/**
	 * 
	 * @return return true if there is more line to be read.
	 * @throws IOException
	 */
	public boolean hasLine() throws IOException {
		while (bf.ready()) { 
			line = bf.readLine();
			return true;
		}
		bf.close();
		return false;
	}
	
	/**
	 * Release memory data and close the file
	 */
	public void closeFile() {
		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.out.println("Error to close the file!");
		}
	}

	/**
	 * Append a new line to the file
	 * 
	 * @param line 
	 *            
	 */

	public void appendLine(String line) {
		try {
			bw.write(line, 0, line.length());
			bw.newLine();
		} catch (IOException e) {
			System.out.println("Error to append a new line!");
		}
	}

}

