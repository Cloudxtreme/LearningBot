package fr.insarennes.learningbot.model;

import java.util.HashMap;
import java.util.Map;

import fr.insarennes.learningbot.controller.LearningBot;

/**
 * This class represents a decision, ie an action the robot will have to execute.
 * Decisions are tree leafs, they have null sons.
 */
public class Decision extends DecisionTreeNode {
//ATTRIBUTES
	/** The population for each label **/
	private Map<String,Integer> populations;
	
//CONSTRUCTOR
	/**
	 * Class constructor.
	 * Don't forget to add populations informations with addPopulation()
	 */
	public Decision() {
		super();
		populations = new HashMap<String,Integer>();
	}

//ACCESSORS
	/**
	 * @param label The label
	 * @return The population associated to the given label
	 */
	public int getPopulation(String label) {
		return (populations.containsKey(label)) ? populations.get(label) : 0;
	}
	
//MODIFIERS
	/**
	 * Adds a population
	 * @param label The label
	 * @param pop The associated population
	 */
	public void addPopulation(String label, int pop) {
		populations.put(label, pop);
	}
	
//OTHER METHODS
	@Override
	public Decision decisionToTake(LearningBot robot) {
		return this;
	}
}
