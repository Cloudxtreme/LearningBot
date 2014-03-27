package fr.insarennes.learningbot.model;

import java.util.TreeMap;

import fr.insarennes.learningbot.controller.DecisionTreeParser;
import fr.insarennes.learningbot.controller.LearningBot;

/**
 * This class represents the decision tree, which is the core of the learning process.
 * There are several methods which give simple answer to know the action the robot should perform.
 * See {@link DecisionTreeParser} to read a decision tree from XML.
 */
public class DecisionTree {
//ATTRIBUTES
	/** The root of the tree **/
	private DecisionTreeNode root;
	
//CONSTRUCTOR
	/**
	 * Class constructor
	 * @param r The root of the tree
	 */
	public DecisionTree(DecisionTreeNode r) {
		root = r;
	}

//ACCESSORS
	/**
	 * @return The tree root
	 */
	public DecisionTreeNode getRoot() {
		return root;
	}
	
//OTHER METHODS
	/**
	 * Should we shoot at the other competitor, regarding combat context ?
	 * @param robot The current robot, the one wondering if it should fire.
	 * @return True if yes, False if no
	 */
	public boolean doWeShoot(LearningBot robot) {
		Decision d = root.decisionToTake(robot);
		
		return d.getPopulation("shoot") > d.getPopulation("not_shoot");
	}
	
	/**
	 * Where the robot should go ?
	 * @param robot The current robot.
	 * @return The direction to take as a string : "forward", "backward", "right", "left", "stay"
	 */
	public String whereDoWeGo(LearningBot robot) {
		Decision d = root.decisionToTake(robot);
		
		//We put the given populations in a tree to get easily which direction we should take
		TreeMap<Integer,String> directions = new TreeMap<Integer,String>();
		String[] availableDirs = {"forward","backward","left","right","stay"};
		for(String dir : availableDirs) {
			directions.put(d.getPopulation(dir), dir);
		}
		
		return directions.lastEntry().getValue();
	}
	
	/**
	 * Where the robot gun should point ?
	 * @param robot The current robot.
	 * @return The direction the gun should take as a string : "front", "back", "right", "left"
	 */
	public String whatGunOrientation(LearningBot robot) {
		Decision d = root.decisionToTake(robot);
		
		//We put the given populations in a tree to get easily which direction we should take
		TreeMap<Integer,String> directions = new TreeMap<Integer,String>();
		String[] availableDirs = {"front", "back", "right", "left"};
		for(String dir : availableDirs) {
			directions.put(d.getPopulation(dir), dir);
		}
		
		return directions.lastEntry().getValue();
	}
}
