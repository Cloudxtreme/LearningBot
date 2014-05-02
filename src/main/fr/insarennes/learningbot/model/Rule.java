package fr.insarennes.learningbot.model;

import fr.insarennes.learningbot.controller.LearningBot;

/**
 * A non-leaf node in the DecisionTree. Its sons are either other Rules or {@link Decision}.
 * A Rule is a like a question about a characteristic of the robot environment (e.g HP, distance to opponent)
 * that will lead to one of its sons, according to the answer.
 * E.g : "Have more than 15 HP ?" "No" "Go to left son, then".
 */
public class Rule extends DecisionTreeNode {
//ATTRIBUTES
	/** The rule applies on the specified label **/
	private String onLabel;
	/** The distinctive value (if double) **/
	private double doubleValue;
	/** The distinctive value (if string) **/
	private String strValue;
	
//CONSTRUCTOR
	/**
	 * Class constructor
	 * @param label The label this rule applies on
	 * @param value The distinctive value
	 */
	public Rule(String label, String value) {
		super();
		onLabel = label;
		strValue = value;
	}
	
	/**
	 * Class constructor
	 * @param label The label this rule applies on
	 * @param value The distinctive value
	 */
	public Rule(String label, double value) {
		super();
		onLabel = label;
		doubleValue = value;
	}

//ACCESSORS
	/**
	 * @return The label this rule applies on
	 */
	public String getLabel() {
		return onLabel;
	}
	
	public double getNumericValue() {
		return doubleValue;
	}
	
	public String getStringValue() {
		return strValue;
	}

//OTHER METHODS
	@Override
	public Decision decisionToTake(LearningBot robot) {
		Decision result = null;
		
		//Call this method recursively on the correct son (regarding to robot context)
		LearnedData context = new LearnedData(robot, robot.getLastOpponentScan());
		
		String value = context.getValue(onLabel);

		//Try to parse a double value
		try {
			double dVal = Double.parseDouble(value);
			if (dVal < doubleValue) {
				result = leftSon.decisionToTake(robot);
			} else {
				result = rightSon.decisionToTake(robot);
			}
		}
		//If an error occurs, it's a string value
		catch (NumberFormatException e) {
			if (value.equals(strValue)) {
				result = leftSon.decisionToTake(robot);
			} else {
				result = rightSon.decisionToTake(robot);
			}
		}

		
		return result;
	}
}
