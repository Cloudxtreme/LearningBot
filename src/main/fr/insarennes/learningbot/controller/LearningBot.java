package fr.insarennes.learningbot.controller;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import fr.insarennes.learningbot.model.Coordinates;
import fr.insarennes.learningbot.model.DecisionTree;
import fr.insarennes.learningbot.model.LearnedData;
import fr.insarennes.learningbot.model.SuperClass;

/**
 * A robot based on an existing one, however this one will improve itself over time,
 * by building and following a decision tree.
 */

public class LearningBot extends SuperClass {

//CONSTANTS
	/** The file which contains the decision tree **/
	private static final String TREE_FILE = "learningbot.tree.xml";
	/** The default bullet power **/
	private static final double BULLET_POWER = 1.0; //shouldn't it be little power, since accuracy is awful?
		
//ATTRIBUTES
	/** The decision tree generated by BonzaiBoost (c) **/
	private static DecisionTree tree;
	/** All the data acquired during battle (static so that each LearningBot instance has access to it) **/ 
	private static List<LearnedData> knowledge = new ArrayList<LearnedData>();
	/**
	 * Position in LearningBot.knowledge of the data that will need to be set to 
	 * "shoot" (hit) or "not_shoot" (missed) when a bullet has respectively hit 
	 * the opponent or a wall. Then it will be incremented. 
	 * Thus it (approximatively) links the shootings (impacts)
	 * to their circumstances when shot (the corresponding LearnedData)
	 */
	private static int nextDataToSet = 0;
	
	/**
	 * Used in order to keep a constantly refreshed view of the enemy.
	 */
	private ScannedRobotEvent lastOpponentScan;
	
	/**
	 * The previous tick's gun heat is constantly saved in order to be able to know if 
	 * the gun just fired (i.e if the current gun heat is suddenly above the previous one)
	 * (see the custom events)
	 */
	private double previousTicksGunHeat;
	
//CONSTRUCTOR
	/**
	 * Class constructor
	 */
	public LearningBot() {
		super();
		
		this.lastOpponentScan = null;
	}

//ACCESSORS
	/**
	 * @return The last saved data, or null if no data
	 */
	public LearnedData getLastData() {
		return (knowledge.size() > 0) ? knowledge.get(knowledge.size()-1) : null;
	}
	
//MODIFIERS
	
//OTHER METHODS
	/**
	 * This method is called when a battle starts
	 */
	@Override
	public void run() {
		loadTree();

		setAllColors(Color.GREEN);
		this.previousTicksGunHeat = getGunHeat();
		
		addCustomEvent(new Condition("onshoot", 84) {
			public boolean test() {
				return getGunHeat() > previousTicksGunHeat;
			}
		});
		
		addCustomEvent(new Condition("oneachtick", 83) {
			public boolean test() {
				return true;
			}
		});
		
		super.run();
		//TODO virer tous les codes commentés une fois qu'on sait que ça marche sans
	}
	
	/**
	 * Moves the robot to a certain point on the map, using Robocode's axes.
	 * Returns when the robot has arrived. (possibly before if there has been an error while moving)
	 * @param to the coordinate where it should move
	 */
	private void moveTo(Coordinates to) {
		//orientation
		//tournerdroite de getVect - orientation actuelle
		turnRight(Coordinates.getVectorDirection(new Coordinates(getX(), getY()), to) - getHeading());
		
		//movement
		ahead(Math.sqrt(Math.pow(to.getX() - getX(), 2) + Math.pow(to.getY() - getY(), 2)));
	}
	
	/* REDEFINING FIRING (whether the bot should actually shoot or not) */
	@Override
	public void fire(double power) {
		if (shouldActuallyFire()){
			super.fire(power);
		}
	}
	
	@Override
	public Bullet fireBullet(double power) {
		if (shouldActuallyFire()) {
			return super.fireBullet(power);
		}
		else
			return null;
	}
	
	@Override
	public void setFire(double power) {
		if (shouldActuallyFire()) {
			super.setFire(power);
		}
	}
	
	@Override
	public Bullet setFireBullet(double power) {
		if (shouldActuallyFire()) {
			return super.fireBullet(power);
		} 
		else 
			return null;
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		this.lastOpponentScan = e;
		
		if (tree != null && getGunHeat() == 0 && tree.doWeShoot(this)) {
			super.fire(BULLET_POWER); // super in order not to check the whole tree again
//			System.err.println("Proudly fired with BonzaiBoost (c)");
		}
			
		super.onScannedRobot(e);
	}

	@Override
	public void onBulletHit(BulletHitEvent e) {
		knowledge.get(nextDataToSet).setShootSuccesful();
		nextDataToSet++;
		
		super.onBulletHit(e);
	}
	
	@Override
	public void onBulletMissed(BulletMissedEvent e) {
		knowledge.get(nextDataToSet).setShootFailed();
		nextDataToSet++;
		
		super.onBulletMissed(e);
	}
	
	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e) {
		// TODO TO_DISCUSS Should it be considered a failure or a success ?
		
		knowledge.get(nextDataToSet).setShootSuccesful();
		nextDataToSet++;
		
		super.onBulletHitBullet(e);
	}

	@Override
	public void onBattleEnded(BattleEndedEvent e) {
		//Save data in filesystem
System.err.println("Data that will be written : "); for (LearnedData i : knowledge) System.err.println(i.getValue("shoot"));//FIXME
		if(knowledge.size() > 0) {
			LearnedDataWriter ldw = new LearnedDataWriter();
			try {
				ldw.write(knowledge, getDataFile("learningbot.data"), getDataFile("learningbot.names"));
			} catch (IOException e1) {
				e1.printStackTrace();
				System.err.println("Failed to save collected data.");
			}
		} else {
			System.err.println("No data to save.");
		}
		
		LearningBot.nextDataToSet = 0; // In case another battle would start
		
		super.onBattleEnded(e);
	}
	
	@Override
	public void onSkippedTurn(SkippedTurnEvent e) {
		System.err.println("Taking too long to compute stuff, another turn was skipped :(");
	} 
	
	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		nextDataToSet = knowledge.size(); // If there are bullets still in the air, the next data to set must be one of the next round
		super.onRoundEnded(event);
	}
	
	@Override
	public void onCustomEvent(CustomEvent e) {
		/*
		 * Every turn :
		 * Updating previousTicksGunHeat.
		 */
		if (e.getCondition().getName().equals("oneachtick")) {
			this.previousTicksGunHeat = this.getGunHeat();
		}
		/*
		 * Every time it shoots a bullet : 
		 * Collecting data, that will eventually later
		 * be considered as a good situation to shoot or not.
		 */
		else if (e.getCondition().getName().equals("onshoot")) {
			knowledge.add(new LearnedData(this, this.lastOpponentScan));
		}
	}
	
	/**
	 * Tries to load tree from XML file
	 */
	private void loadTree() {
		//Load tree if needed
		if(tree == null) {
			DecisionTreeParser dtp = new DecisionTreeParser();
			try {
				File treeXml = getDataFile(TREE_FILE);
				if(treeXml.length() > 0) {
					tree = dtp.parse(treeXml);
				} else {
					//Delete the file created by getDataFile
					treeXml.delete();
				}
			} catch (FileNotFoundException | XMLStreamException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * DEPRECATED (might be useful to future features, but is currently not used)
	 * Sets the last used direction as wrong in data
	 */
	private void setLastDirectionWrong() {
		String oppositeDirection;
		switch(knowledge.get(knowledge.size()-1).getValue("direction")) {
			case "forward":
				oppositeDirection = "backward";
				break;
			case "backward":
				oppositeDirection = "forward";
				break;
			case "stay":
				oppositeDirection = "forward";
				break;
			case "left":
				oppositeDirection = "right";
				break;
			case "right":
				oppositeDirection = "left";
				break;
			default:
				oppositeDirection = "backward";
		}
		knowledge.get(knowledge.size()-1).setDirection(oppositeDirection);
	}
	
	/**
	 * DEPRECATED
	 * Sets the last used direction for gun as wrong in data
	 */
	private void setLastGunDirectionWrong() {
		String oppositeDirection;
		switch(knowledge.get(knowledge.size()-1).getValue("gundirection")) {
			case "front":
				oppositeDirection = "back";
				break;
			case "back":
				oppositeDirection = "front";
				break;
			case "left":
				oppositeDirection = "right";
				break;
			case "right":
				oppositeDirection = "left";
				break;
			default:
				oppositeDirection = "back";
		}
		knowledge.get(knowledge.size()-1).setGunDirection(oppositeDirection);
	}
	
	/**
	 * Return true when either :
	 * - LearningBot is behaving exactly like its super class
	 * - LearningBot is following the decision tree, which said firing would be a good idea right now.
	 */
	private boolean shouldActuallyFire() {
		return (tree == null || tree.doWeShoot(this));
	}
	/**
	 * Returns the last scan picturing the opponent that the robot made
	 */
	public ScannedRobotEvent getLastOpponentScan() {
		return this.lastOpponentScan;
	}
	
	
}
