package fr.insarennes.learningbot.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import robocode.RobocodeFileWriter;
import fr.insarennes.learningbot.model.BonzaiProperty;
import fr.insarennes.learningbot.model.LearnedData;

public class LearnedDataWriter {
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
		
		/*
		 * .names file
		 */
		//Get properties
		String namesLabels = "";
		boolean firstLabel = true;
		String namesProps = "";
		List<BonzaiProperty> classLabels = new ArrayList<BonzaiProperty>();
		for(BonzaiProperty bp : properties) {
			if(bp.getType() == BonzaiProperty.CLASS_LABEL) {
				if(!firstLabel) { namesLabels += ", "; }
				else { firstLabel = false; }
				namesLabels += bp.toString();
				classLabels.add(bp);
			} else {
				namesProps += bp.toString() + "\n"; 
			}
		}
		
		String namesContent = namesLabels + ".\n" + namesProps;
		writeTextFile(names, namesContent);
		
		/*
		 * .data file
		 */
		String datasContent = "";
		properties.removeAll(classLabels);
		for(LearnedData ld : data) {
			//Write properties
			boolean first = true;
			for(BonzaiProperty bp : properties) {
				if(!first) { datasContent += ", "; }
				else { first = false; }
				
				String current = ld.getValue(bp);
				if(current != null) {
					datasContent += current;
				}
			}
			
			//Write labels data
			datasContent += ",";
			for(BonzaiProperty bp : classLabels) {
				datasContent += " "+ld.getValue(bp);
			}
			datasContent += ".\n";
		}
		//Write .data file
		writeTextFile(datas, datasContent);
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
