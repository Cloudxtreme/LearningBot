package fr.insarennes.learningbot.model;

import fr.insarennes.learningbot.controller.LearningBot;

/**
 * This class represents a node in the decision tree.
 * It's abstract because a node will be a {@link Rule} or a {@link Decision}.
 */
public abstract class DecisionTreeNode {
//ATTRIBUTES
	/** The left son **/
	protected DecisionTreeNode leftSon;
	/** The right son **/
	protected DecisionTreeNode rightSon;

//CONSTRUCTOR
	/**
	 * Class constructor
	 * @param left The left son of this node
	 * @param right The right son of this node
	 */
	public DecisionTreeNode(DecisionTreeNode left, DecisionTreeNode right) {
		leftSon = left;
		rightSon = right;
	}
	
	/**
	 * Class constructor.
	 * Don't forget to define sons if you want to.
	 */
	public DecisionTreeNode() {
		leftSon = null;
		rightSon = null;
	}

//ACCESSORS
	/**
	 * @return the left son
	 */
	public DecisionTreeNode getLeftSon() {
		return leftSon;
	}

	/**
	 * @return the right son
	 */
	public DecisionTreeNode getRightSon() {
		return rightSon;
	}

//MODIFIERS
	/**
	 * Defines the left son of this node
	 * @param n The son
	 */
	public void setLeftSon(DecisionTreeNode n) {
		leftSon = n;
	}
	
	/**
	 * Defines the right son of this node
	 * @param n The son
	 */
	public void setRightSon(DecisionTreeNode n) {
		rightSon = n;
	}
	
//OTHER METHODS
	/**
	 * FIXME Shouldn't there be the opponent in the parameters ? Some decisions may rely on it.
	 *  
	 * Looks at the tree and returns the decision to take in function of the current context.
	 * @param robot The current robot
	 * @return The decision to take
	 */
	public abstract Decision decisionToTake(LearningBot robot);
}
