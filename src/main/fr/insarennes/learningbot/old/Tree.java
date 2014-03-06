package fr.insarennes.learningbot.old;

import java.io.File;
import java.util.Map;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 *Java representation of an XML tree made thanks to JDOM2
 */
public class Tree {
	/**
	 * The XML file 
	 */
	private Document doc;
	private Element root;
	
	public Tree(String name) {
		SAXBuilder sxb = new SAXBuilder();
		
		//File file = new File("");
		//String chemin = file.getAbsolutePath();
		
		try {
			// Creation of a new JDOM document with the XML file as argument 
			// Parsing is over ;)
			doc = sxb.build(new File("/home/adrien/Documents/INSA%203/Projets/2013-2014%20Études%20pratiques/Code/LearningBot/bin/bonzai/"+name + ".tree.xml"));
			// Initialisation of a new root element with the root element of the document
			root = doc.getRootElement().getChild("tree").getChild("node");
		} catch (Exception e) {
			System.out.println("Opening of the learning tree error");
		}
	}
	
	/**
	 * Uses the tree to decide weither to shot or not
	 * @param Element the current node 
	 * @param Map<String, Double> ensAttr the attributes used for take the decision 
	 * @return true if the robot should shot, false otherwise
	 */
	public boolean shotTest(Element node, Map<String, Double> ensAttr) throws DataConversionException {
		boolean res = false;
		
		if(ensAttr.isEmpty())
			return true;
		
		if (node.getAttributeValue("type").equals("leaf")) { 
			Element leaf = node.getChild("population");
			
			System.out.println(leaf.getAttribute("L0"));

			if (leaf.getAttribute("L0") != null)
				return true;
			else 
				return false;
			
		} else if (node.getAttributeValue("type").equals("node")) {
			//collect the result of the question of the node 
			Element question = node.getChild("question");
			
			double patron = question.getAttribute("patron").getDoubleValue();
			double value = ensAttr.get(question.getAttributeValue("name"));
			
			res = testQuestion(value, patron);
			
			//true -> left child tree
			if (res)
				return shotTest(node.getChildren("node").get(0), ensAttr);
			//false -> right child tree
			else
				return shotTest(node.getChildren("node").get(1), ensAttr);
		} else
			//normally this case never happens
			return false;
	}
	
	private boolean testQuestion(double val, double patron) {
		System.out.println(val + " < " + patron + " ?");
		return val < patron;
	}
	
	public Element getRoot() {
		return root;
	}
}