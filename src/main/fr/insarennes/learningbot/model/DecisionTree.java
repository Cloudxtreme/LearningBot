package fr.insarennes.learningbot.model;

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

//OTHER METHODS
	/**
	 * Do we might shot other competitor, regarding to combat context ?
	 * @param robot The current robot
	 * @return True if yes, False if no
	 */
	public boolean doWeShot(LearningBot robot) {
		//TODO Call decisionToTake and return the answer as a boolean
		Decision d = root.decisionToTake(robot);
		return false;
	}
}
