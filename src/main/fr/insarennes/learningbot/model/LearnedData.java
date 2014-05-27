package fr.insarennes.learningbot.model;

import java.util.HashMap;
import java.util.List;
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
	 * @param b LearningBot itself.
	 * @param e The ennemy robot
	 */
	public LearnedData(LearningBot b, ScannedRobotEvent e) {
		if(properties == null) { 
			initProperties();
		}

		data = new HashMap<BonzaiProperty,String>();
		data.put(properties.get("op_bearing"), String.valueOf(e.getBearing())); // not sure if useful
		data.put(properties.get("op_distance"), String.valueOf(e.getDistance()));
		data.put(properties.get("op_energy"), String.valueOf(e.getEnergy()));
		data.put(properties.get("op_heading"), String.valueOf(e.getHeading()));
		data.put(properties.get("op_name"), String.valueOf(e.getName()));
		data.put(properties.get("op_velocity"), String.valueOf(e.getVelocity()));
		data.put(properties.get("my_distremain"), String.valueOf(b.getDistanceRemaining()));
		data.put(properties.get("my_energy"), String.valueOf(b.getEnergy()));
		data.put(properties.get("my_gunheading"), String.valueOf(b.getGunHeading()));
		data.put(properties.get("my_gunheat"), String.valueOf(b.getGunHeat()));
		data.put(properties.get("my_heading"), String.valueOf(b.getHeading()));
		data.put(properties.get("my_x"), String.valueOf(b.getX()));
		data.put(properties.get("my_y"), String.valueOf(b.getY()));
		data.put(properties.get("op_x"), String.valueOf(getEnnemyX(b, e)));
		data.put(properties.get("op_y"), String.valueOf(getEnnemyY(b, e)));
		
		data.put(properties.get("shoot"), "unknown"); 
		// we don't know for now if it will be missed or not. Almost all data should be set shoot or not_shoot before the end of the battle

		data.put(properties.get("my_guntowardsennemy"), String.valueOf(((b.getHeading() + e.getBearing()) % 360) - b.getGunHeading()));//TODO Might need tests and/or simplifications.
		
		//Calculate temporal attributes
		String[] values = {
				"op_bearing",
				"op_distance",
				"op_energy",
				"op_heading",
				"op_velocity",
				"my_distremain",
				"my_energy",
				"my_gunheading",
				"my_gunheat",
				"my_heading",
				"my_x",
				"my_y",
				"op_x",
				"op_y"
				};
		
		LearnedData past = b.getDataFromPast(5);
		
		if(past != null) {
			for(String val : values) {
				setProperty("diff_"+val,
						Double.toString(
								Double.parseDouble(getValue(val))
								- Double.parseDouble(past.getValue(val))
								));
			}
		} else {
			//Set attributes to null
			for(String val : values) {
				setProperty("diff_"+val, "0");
			}
		}
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
	public void setShootSuccesful() {
		data.put(properties.get("shoot"), "shoot");
	}
	
	public void setShootFailed() {
		data.put(properties.get("shoot"), "not_shoot");
	}
	
	
	public void setDirection(String d) {
		data.put(properties.get("direction"), d);
	}
	
	public void setGunDirection(String d) {
		data.put(properties.get("gundirection"), d);
	}
	
	private void setProperty(String name, String value) {
		data.put(properties.get(name), value);
	}

//OTHER METHODS
	/**
	 * Initializes the properties which CAN be used (doesn't matter if initialized but not used)
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
		properties.put("my_heading", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_heading"));
		properties.put("my_gunheat", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_gunheat"));
		properties.put("my_x", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_x"));
		properties.put("my_y", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_y"));
		properties.put("op_x", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "op_x"));
		properties.put("op_y", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "op_y"));
		
		properties.put("diff_op_bearing", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_op_bearing"));
		properties.put("diff_op_distance", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_op_distance"));
		properties.put("diff_op_energy", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_op_energy"));
		properties.put("diff_op_velocity", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_op_velocity"));
		properties.put("diff_my_energy", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_my_energy"));
		properties.put("diff_op_heading", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_op_heading"));
		properties.put("diff_my_distremain", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_my_distremain"));
		properties.put("diff_my_gunheading", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_my_gunheading"));
		properties.put("diff_my_heading", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_my_heading"));
		properties.put("diff_my_gunheat", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_my_gunheat"));
		properties.put("diff_my_x", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_my_x"));
		properties.put("diff_my_y", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_my_y"));
		properties.put("diff_op_x", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_op_x"));
		properties.put("diff_op_y", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "diff_op_y"));
		
		properties.put("shoot", new BonzaiProperty(BonzaiProperty.CLASS_LABEL, "shoot"));
		properties.get("shoot").addValue("shoot");
		properties.get("shoot").addValue("not_shoot");
		properties.get("shoot").addValue("unknown");
		properties.put("my_guntowardsennemy", new BonzaiProperty(BonzaiProperty.INPUT_CONTINUOUS, "my_guntowardsennemy"));

		
//		properties.put("direction", new BonzaiProperty(BonzaiProperty.CLASS_LABEL, "direction"));
//		properties.get("direction").addValue("forward");
//		properties.get("direction").addValue("backward");
//		properties.get("direction").addValue("left");
//		properties.get("direction").addValue("right");
//		properties.get("direction").addValue("stay");
//		properties.put("gundirection", new BonzaiProperty(BonzaiProperty.CLASS_LABEL, "gundirection"));
//		properties.get("gundirection").addValue("front");
//		properties.get("gundirection").addValue("back");
//		properties.get("gundirection").addValue("left");
//		properties.get("gundirection").addValue("right");
	}
	
	/**
	 * Calculates the direction the robot took between this event and the given event
	 * @param ld The last learned data
	 * @return The direction : "forward", "backward", "right", "left", "stay"
	 */
	private String calculateDirection(LearnedData ld) {
		String result;
		if(ld == null) {
			result = "stay";
		} else {
			//Get the needed data : position, heading
			double lastHeading = Double.valueOf(ld.getValue("my_heading"));
			double lastX = Double.valueOf(ld.getValue("my_x"));
			double lastY = Double.valueOf(ld.getValue("my_y"));
			double x = Double.valueOf(this.getValue("my_x"));
			double y = Double.valueOf(this.getValue("my_y"));
			
			//Calculate the direction of the movement vector
			double movementHeading = getVectorDirection(lastX, x, lastY, y);

			//Calculate the robot relative rotation
			double relativeRobotHeading = movementHeading-lastHeading;
			if(relativeRobotHeading >= 45 && relativeRobotHeading < 135) {
				result = "right";
			}
			else if(relativeRobotHeading >= 135 && relativeRobotHeading < 225) {
				result = "backward"; 
			}
			else if(relativeRobotHeading >= 225 && relativeRobotHeading < 315) {
				result = "left";
			}
			else {
				result = "forward";
			}
		}
		
		return result;
	}
	
	/**
	 * Calculates the direction the robot gun took between this event and the given event
	 * @param ld The last learned data
	 * @return The direction : "front", "back", "right", "left"
	 */
	private String calculateGunDirection(LearnedData ld) {
		String result;
		
		if(ld == null) {
			result = "front";
		} else {
			//Get the needed data : position, heading
			double lastHeading = Double.valueOf(ld.getValue("my_gunheading"));
			double heading = Double.valueOf(this.getValue("my_gunheading"));
			
			//Calculate the robot relative rotation
			double relativeGunHeading = heading-lastHeading;
			if(relativeGunHeading >= 45 && relativeGunHeading < 135) {
				result = "right";
			}
			else if(relativeGunHeading >= 135 && relativeGunHeading < 225) {
				result = "back"; 
			}
			else if(relativeGunHeading >= 225 && relativeGunHeading < 315) {
				result = "left";
			}
			else {
				result = "front";
			}
		}
		
		return result;
	}
	
	/**FIXME use Coordinates instead
	 * Calculates the direction of a vector (from 0° (north) to 360° clockwise)
	 * @param x1 The start point X
	 * @param x2 The end point X
	 * @param y1 The start point Y
	 * @param y2 The end point Y
	 * @return
	 */
	private double getVectorDirection(double x1, double x2, double y1, double y2) {
		double angleRad = Math.atan2(y2-y1, x2-x1);
		double angle = Math.toDegrees(angleRad); /* Radian angle starts at east */
		angle = Math.abs(angle-180);
		angle = (angle < 90) ? angle + 270 : angle - 90; /* Angle from 0 (north) to 360° clockwise */
		return angle;
	}
	
	/**
	 * Get the ennemy X position
	 * @param robot Our robot
	 * @param e The event
	 * @return The X coordinate
	 */
	private double getEnnemyX(LearningBot robot, ScannedRobotEvent e) {
		double absBearingDeg = (robot.getHeading() + e.getBearing());
		if (absBearingDeg < 0) absBearingDeg += 360;
		return robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
	}
	
	/**
	 * Get the ennemy Y position
	 * @param robot Our robot
	 * @param e The event
	 * @return The Y coordinate
	 */
	private double getEnnemyY(LearningBot robot, ScannedRobotEvent e) {
		double absBearingDeg = (robot.getHeading() + e.getBearing());
		if (absBearingDeg < 0) absBearingDeg += 360;
		return robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
	}

	/**
	 * Calculates the temporal attributes of data (relative direction, ...)
	 * This method is called when all the data is available. 
	 * @param knowledge The data
	 * @return The data, with temporal attributes
	 */
	public static List<LearnedData> calculateTemporalAttributes(List<LearnedData> knowledge) {
		int diffTemporal = 5;
		LearnedData current, next;
		
		String[] values = {
				"op_bearing",
				"op_distance",
				"op_energy",
				"op_heading",
				"op_velocity",
				"my_distremain",
				"my_energy",
				"my_gunheading",
				"my_gunheat",
				"my_heading",
				"my_x",
				"my_y",
				"op_x",
				"op_y"
				};
		
		for(int i=0; i<knowledge.size()-diffTemporal; i++) {
			current = knowledge.get(i);
			next = knowledge.get(i+diffTemporal);
			
			//Calculates attributes
			for(String val : values) {
				current.setProperty("diff_"+val,
						Double.toString(
								Double.parseDouble(next.getValue(val))
								- Double.parseDouble(current.getValue(val))
								));
			}
		}
		
		for(int i=knowledge.size()-diffTemporal; i < knowledge.size(); i++) {
			current = knowledge.get(i);
			
			//Set attributes to null
			for(String val : values) {
				current.setProperty("diff_"+val, "0");
			}
		}
		
		return knowledge;
	}
}
