package fr.insarennes.learningbot.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import robocode.RobocodeFileWriter;
import fr.insarennes.learningbot.model.BonzaiProperty;
import fr.insarennes.learningbot.model.LearnedData;

public class LearnedDataWriter {
//CONSTANTS
	/** The properties which should not be written in text file **/
	private static String[] IGNORE_PROPS = { /*"my_x", "my_y", "my_gunheading"*/ };
	/** The properties which should not be written in text file, as a list **/
	private static List<String> IGNORE_PROPS_LIST = Arrays.asList(IGNORE_PROPS);

//OTHER METHODS
	/**
	 * Saves a lot of data in a file
	 * @param data The data learned during battles
	 * @param datas The file to write the data in
	 * @param names The file to write the data header in
	 * @throws IOException 
	 */
	public void write(List<LearnedData> data, File datas, File names) throws IOException {
		if(data.size() == 0) {
			throw new IOException("The given data is empty");
		}
		
		//File management
		if(names.exists()) {
			names.delete();
		}
		
		if(datas.exists()) {
			datas.delete();
		}
		
		Set<BonzaiProperty> properties = data.get(0).getProperties();
		
		//Remove ignored properties
		List<BonzaiProperty> toRemove = new ArrayList<BonzaiProperty>();
		for(BonzaiProperty bp : properties) {
			if(IGNORE_PROPS_LIST.contains(bp.getName())) {
				toRemove.add(bp);
			}
		}
		properties.removeAll(toRemove);
		
		/*
		 * .names file
		 */
		//Get properties
		StringBuilder namesLabels = new StringBuilder();
		boolean firstLabel = true;
		StringBuilder namesProps = new StringBuilder();
		List<BonzaiProperty> classLabels = new ArrayList<BonzaiProperty>();
		for(BonzaiProperty bp : properties) {
			if(bp.getType() == BonzaiProperty.CLASS_LABEL) {
				if(!firstLabel) { namesLabels.append(", "); }
				else { firstLabel = false; }
				namesLabels.append(bp.toString());
				classLabels.add(bp);
			} else {
				namesProps.append(bp.toString() + "\n"); 
			}
		}
		
		String namesContent = namesLabels + ".\n" + namesProps;
		writeTextFile(names, namesContent);
		
		/*
		 * .data file
		 */
		StringBuilder datasContent = new StringBuilder(50 * data.size());
		properties.removeAll(classLabels);
		for(LearnedData ld : data) {
			//Write properties
			boolean first = true;
			for(BonzaiProperty bp : properties) {
				if(!first) { datasContent.append(", "); }
				else { first = false; }
				
				String current = ld.getValue(bp);
				if(current != null) {
					datasContent.append(current);
				}
			}
			
			//Write labels data
			datasContent.append(",");
			for(BonzaiProperty bp : classLabels) {
				datasContent.append(" "+ld.getValue(bp));
			}
			datasContent.append(".\n");
		}
		
		//Write .data file
		writeTextFile(datas, datasContent.toString());
	}
	
	/**
	 * Writes a text file
	 * @param f The file to write in
	 * @param s The text to write
	 * @throws IOException If an error occurs during file writing
	 */
	private void writeTextFile(File f, String s) throws IOException {
		RobocodeFileWriter w = new RobocodeFileWriter(f);
		w.write(s);
		w.close();
	}
}
