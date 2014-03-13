package fr.insarennes.learningbot.controller;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import fr.insarennes.learningbot.model.Decision;
import fr.insarennes.learningbot.model.DecisionTree;
import fr.insarennes.learningbot.model.DecisionTreeNode;
import fr.insarennes.learningbot.model.Rule;

/**
 * Test class for {@link DecisionTreeParser}
 */
public class DecisionTreeParserTest {
//ATTRIBUTES
	private String pathTest = "test"+File.separator+"tree"+File.separator;
	private DecisionTreeParser dtp;
	private DecisionTree result;
	private File xml;
	private Map<String,Integer> pops;

//SETUP
	@Before
	public void setUp() throws Exception {
		dtp = new DecisionTreeParser();
	}

//UTILITY METHODS
	/**
	 * Test a rule node
	 * @param n The node
	 * @param l The label
	 * @param v The value
	 */
	private void testRule(DecisionTreeNode n, String l, double v) {
		assertEquals(Rule.class, n.getClass());
		Rule r = (Rule) n;
		assertEquals(l, r.getLabel());
		assertEquals(v, r.getNumericValue(), 0.0001);
	}
	
	/**
	 * Test a decision node
	 * @param n The node
	 * @param populations The populations
	 */
	private void testDecision(DecisionTreeNode n, Map<String,Integer> populations) {
		assertEquals(Decision.class, n.getClass());
		Decision d = (Decision) n;
		
		for(String l : populations.keySet()) {
			assertEquals(populations.get(l).intValue(), d.getPopulation(l));
		}
	}
	
//TESTS
	@Test
	public void testParseWorking() throws FileNotFoundException, XMLStreamException {
		xml = new File(pathTest+"working.tree.xml");
		result = dtp.parse(xml);
		DecisionTreeNode root = result.getRoot();
		
		//Test root node
		testRule(root, "op_distance", 552.326);
		
		//Test left son
		pops = new HashMap<String, Integer>();
		pops.put("not_shoot", 12);
		testDecision(root.getLeftSon(), pops);
		
		//Test right son
		pops = new HashMap<String, Integer>();
		pops.put("shoot", 42);
		testDecision(root.getRightSon(), pops);
	}
}
