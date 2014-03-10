package fr.insarennes.learningbot.model;

import fr.insarennes.learningbot.controller.LearningBot;

/**
 * FIXME javadoc, javadoc everywhere !
 */
public class Rule extends DecisionTreeNode {
//CONSTRUCTOR
	public Rule() {
		super();
		// TODO Auto-generated constructor stub
	}

//OTHER METHODS
	@Override
	public Decision decisionToTake(LearningBot robot) {
		// TODO Auto-generated method stub
		//Call this method recursively on the correct son (regarding to robot context)
		return null;
	}
}
