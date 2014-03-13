package fr.insarennes.learningbot.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import robocode.ScannedRobotEvent;
import fr.insarennes.learningbot.controller.LearningBot;

/**
 * This class saves data from {@link ScannedRobotEvent}, in order to keep it and save it later.
 * This data will be used to create decision trees. The robot gathers a new data every time the enemy is detected
 * A "LearnedData" is a set of different characteristics we consider interesting
 * and to which is given the recorded value. E.g : distance_between_robots=10  energy=50
 */
public class LearnedData {
//CLASS ATTRIBUTES
	/** The bonzaiboost properties to save **/
	private static Map<String,BonzaiProperty> properties;
	
//ATTRIBUTES
	/** The data to save (a map is used to be more flexible) **/
	private Map<BonzaiProperty,String> data;
	
//CONSTRUCTOR
	/**
	 * Class constructor
	 * @param e The event to save
	 */
	public LearnedData(LearningBot b, ScannedRobotEvent e) {
		if(properties == null) { 
			initProperties();
		}
		data = new HashMap<BonzaiProperty,String>();
		data.put(properties.get("op_bearing"), String.valueOf(e.getBearing())); // not sure if useful
		data.put(properties.get("op_distance"), String.valueOf(e.getDistance()));
		data.put(properties.get("op_energy"), String.valueOf(e.getEnergy()));
//		data.put(properties.get("op_heading"), String.valueOf(e.getHeading()));
//		data.put(properties.get("op_name"), String.valueOf(e.getName()));
		data.put(properties.get("op_velocity"), String.valueOf(e.getVelocity()));
//		data.put(properties.get("my_distremain"), String.valueOf(b.getDistanceRemaining()));
		data.put(properties.get("my_energy"), String.valueOf(b.getEnergy()));
//		data.put(properties.get("my_gunheading"), String.valueOf(b.getGunHeading()));
//		data.put(properties.get("my_gunheat"), String.valueOf(b.getGunHeat()));
		data.put(properties.get("shoot"), "not_shoot");
	}
	
//ACCESSORS
	/**
	 * @return The properties
	 */
	public Set<BonzaiProperty> getProperties() {
		return data.keySet();
	}
	
	/**
	 * Get the value associated to the given property
	 * @param prop The property
	 * @return The value, or null if the property isn't contained in data
	 */
	public String getValue(BonzaiProperty prop) {
		return data.get(prop);
	}
	
	/**
	 * Get the value associated to the given property
	 * @param prop The property
	 * @return The value, or null if the property isn't contained in data
	 */
	public String getValue(String prop) {
		return data.get(properties.get(prop));
	}

//MODIFIERS
	public void setShootSuccesfull() {
		data.put(properties.get("shoot"), "shoot");
	}

//OTHER METHODS
	/**
	 * Initializes the properties which CAN be used
	 */
	private void initProperties() {
		properties = new HashMap<String,BonzaiProperty>();
		properties.put("op_bearing", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "op_bearing"));
		properties.put("op_distance", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "op_distance"));
		properties.put("op_energy", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "op_energy"));
		properties.put("op_name", new BonzaiProperty(BonzaiProperty.INPUT_TEXT, "op_name")); 	// probably not really relevant, but, eh
		properties.put("op_velocity", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "op_velocity"));
		properties.put("my_energy", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_energy"));
		properties.put("op_heading", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "op_heading"));
		properties.put("my_distremain", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_distremain"));
		properties.put("my_gunheading", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_gunheading"));
		properties.put("my_gunheat", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_gunheat"));
		properties.put("shoot", new BonzaiProperty(BonzaiProperty.CLASS_LABEL, "shoot"));
		properties.get("shoot").addValue("shoot");
		properties.get("shoot").addValue("not_shoot");
	}
}
