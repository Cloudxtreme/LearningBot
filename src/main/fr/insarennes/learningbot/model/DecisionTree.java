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
	
	/**
	 * Percentage (between 0 and 100) of how many successful hits are needed (at least)
	 * to allow firing
	 * Ex : On all shots (hit+miss), I'll shoot as long as there is at least 30% of successful hits.
	 */
	public final static int MIN_HIT_PERCENTAGE = 30;
	
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
		
		if (d == null) {// Was not able to take a decision. Should not occur, but, just in case...
			System.err.println("\nPROBLEM IN TREE : WAS NOT ABLE TO TAKE A DECISION\n");
			return false;
		}
		else {
			int shoot = d.getPopulation("shoot"), not_shoot = d.getPopulation("not_shoot");
					
			return (shoot/(shoot+not_shoot)*100) > MIN_HIT_PERCENTAGE;
		}
	}
	
	/**
	 * DEPRECATED 
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
	 * DEPRECATED
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
