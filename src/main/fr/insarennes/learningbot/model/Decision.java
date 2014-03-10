package fr.insarennes.learningbot.model;

import fr.insarennes.learningbot.controller.LearningBot;

/**
 * This class represents a decision, ie an action the robot will have to execute.
 * Decisions are tree leafs, they have null sons.
 */
public class Decision extends DecisionTreeNode {
//CONSTRUCTOR
	//TODO Default constructor, add decision informations
	public Decision() {
		super();
	}

//OTHER METHODS
	@Override
	public Decision decisionToTake(LearningBot robot) {
		return this;
	}
}
