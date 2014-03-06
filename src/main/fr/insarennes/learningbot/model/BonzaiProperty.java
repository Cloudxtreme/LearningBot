package fr.insarennes.learningbot.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class represents a BonzaiBoost property
 */
public class BonzaiProperty {
//CONSTANTS
	/*
	 * Kind of property
	 */
	public static final int CLASS_LABEL = 1;
	public static final int INPUT_CONTINUOUS = 2;
	public static final int INPUT_DISCRETE = 3;
	public static final int INPUT_TEXT = 4;
	public static final int INPUT_SCOREDTEXT = 5;

//ATTRIBUTES
	/** The kind of property (see class constants) **/
	private int type;
	/** The name of this property **/
	private String name;
	/** The values of the property if not label or continuous **/
	private Set<String> values;

//CONSTRUCTOR
	/**
	 * Class constructor
	 * @param type The kind of property
	 * @param name The name
	 */
	public BonzaiProperty(int type, String name) {
		this.type = type;
		this.name = name;
		if(type != INPUT_CONTINUOUS) {
			values = new LinkedHashSet<String>();
		}
	}

//ACCESSORS
	@Override
	public String toString() {
		String result;
		
		switch(type) {
			case CLASS_LABEL:
				boolean firstLabel = true;
				result = "";
				for(String val : values) {
					if(!firstLabel) { result += ", "; }
					else { firstLabel = false; }
					result += val;
				}
				break;
			case INPUT_CONTINUOUS:
				result = name+": continuous.";
				break;
			case INPUT_DISCRETE:
				result = name+": ";
				boolean first = true;
				for(String val : values) {
					if(!first) { result += ", "; }
					else { first = false; }
					result += val;
				}
				result += ".";
				break;
			case INPUT_TEXT:
				result = name+": text.";
				break;
			case INPUT_SCOREDTEXT:
				result = name+": scoredtext.";
				break;
			default:
				result= name;
		}
		
		return result;
	}
	
	/**
	 * @return The kind of property
	 */
	public int getType() {
		return type;
	}
	
//MODIFIERS
	/**
	 * Adds a new value to the property
	 * @param v The value
	 */
	public void addValue(String v) {
		if(type == INPUT_CONTINUOUS) {
			throw new RuntimeException("Values can't be added to continuous properties.");
		}
		values.add(v);
	}

//OTHER METHODS
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + type;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BonzaiProperty other = (BonzaiProperty) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}
}
