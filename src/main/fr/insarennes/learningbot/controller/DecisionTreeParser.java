package fr.insarennes.learningbot.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import fr.insarennes.learningbot.model.Decision;
import fr.insarennes.learningbot.model.DecisionTree;
import fr.insarennes.learningbot.model.DecisionTreeNode;
import fr.insarennes.learningbot.model.Rule;

public class DecisionTreeParser {
//OTHER METHODS
	/**
	 * Parses a BonzaiBoost (c) decision tree.
	 * @param f The XML file which contains the tree
	 * @return The parsed tree
	 * @throws FileNotFoundException 
	 * @throws XMLStreamException 
	 */
	public DecisionTree parse(File f) throws FileNotFoundException, XMLStreamException {
		DecisionTree result = null;
		DecisionTreeNode root = null;
		String[] labels = null;
		
		//Open XML file
		InputStream is = new FileInputStream(f);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(is);
		
		//Read XML
		while(reader.hasNext()) {
			if(reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				switch(reader.getLocalName()) {
					case "tree":
						root = parseNode(reader, labels);
						break;
					case "target_labels":
						//Parse labels
						labels = new String[reader.getAttributeCount()];
						for(int i=0; i < reader.getAttributeCount(); i++) {
							int labelIndex = Integer.parseInt(reader.getAttributeLocalName(i).substring(1));
							labels[labelIndex] = reader.getAttributeValue(i);
						}
						break;
				}
			}
			reader.next();
		}
		
		result = new DecisionTree(root);
		
		return result;
	}
	
	/**
	 * Parses a single node in XML
	 * @param reader The current XML reader
	 * @param labels The read labels
	 * @return The parsed node (with sons)
	 * @throws XMLStreamException
	 */
	private DecisionTreeNode parseNode(XMLStreamReader reader, String[] labels) throws XMLStreamException {
		DecisionTreeNode result = null;
		
		//Put reader on the next node
		goToNext(reader, "node");
		
		//Leaf or not ?
		if(reader.getAttributeValue(null, "type").equals("leaf")) {
			//Create decision
			Decision d = new Decision();
			
			//Read population values
			goToNext(reader, "population");
			if(reader.getEventType() == XMLStreamConstants.START_ELEMENT
					&& reader.getLocalName().equals("population")) {
				//For each LX attribute, associate value to label in decision
				for(int i=0; i < reader.getAttributeCount(); i++) {
					int labelIndex = Integer.parseInt(reader.getAttributeLocalName(i).substring(1));
					d.addPopulation(labels[labelIndex], Integer.parseInt(reader.getAttributeValue(i)));
				}
			} else {
				throw new XMLStreamException("The given tree isn't well structured (no <population>)");
			}
			
			//Put it in result
			result = d;
			
		}
		else {
			//Create rule
			Rule r;
			
			//Read question
			goToNext(reader, "question");
			if(reader.getEventType() == XMLStreamConstants.START_ELEMENT
					&& reader.getLocalName().equals("question")) {
				//If value is numeric
				if(reader.getAttributeValue(null, "type").equals("numeric")) {
					r = new Rule(
							reader.getAttributeValue(null, "name"),
							Double.parseDouble(reader.getAttributeValue(null, "patron"))
							);
				}
				//Else (value is a string)
				else {
					r = new Rule(
							reader.getAttributeValue(null, "name"),
							reader.getAttributeValue(null, "patron")
							);
				}
			} else {
				throw new XMLStreamException("The given tree isn't well structured (no <question>)");
			}
			
			//Read population
			goToNext(reader, "population");
			
			//Read left son
			goToNext(reader, "node");
			r.setLeftSon(parseNode(reader, labels));
			
			//Read right son
			goToNext(reader, "node");
			r.setRightSon(parseNode(reader, labels));
			
			//Put it in result
			result = r;
		}
		
		return result;
	}
	
	/**
	 * Put the reader on the next given markup
	 * @param reader The reader
	 * @param markup The markup
	 * @throws XMLStreamException
	 */
	private void goToNext(XMLStreamReader reader, String markup) throws XMLStreamException {
		while(reader.hasNext()
				&& (reader.getEventType() != XMLStreamConstants.START_ELEMENT
				|| !reader.getLocalName().equals(markup))) {
			reader.next();
		}
	}
}
