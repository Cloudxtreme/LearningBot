/*
FoilistMicro v1.0.0 by Sheldor  12/17/2013  codesize:  748 bytes
a MicroBot with Stop and Go/Random movement and a basic DC gun

Foil is one of the three forms of modern sport fencing,
along with Epee and Sabre.  http://en.wikipedia.org/wiki/Foil_%28fencing%29
 
Credits: 
Thanks go to the authors of the following bots:
	targeting: jk.mini.CunobelinDC, Falcon, pez.micro.Aristocles, kc.micro.Thorn, jk.micro.Connavar
	movement : jk.micro.Cotillion, kc.micro.Thorn, nz.jdc.HedgehogGF, wiki.nano.RaikoNano
I would also like to thank all open source bot authors and contributors to the RoboWiki.

FoilistMicro is open source and released under the terms of the RoboWiki Public Code License (RWPCL) - Version 1.1.
see license here:  http://robowiki.net/wiki/RWPCL
*/

package fr.insarennes.learningbot.controller;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;     
import java.lang.*;         
import java.util.ArrayList; 
import java.awt.*;
import java.util.*;


/**
 * Intended to be extended by LearningBot when switching its behavior
 * to other robots'.
 * You may copy-paste code of open-source robots here until finding
 * the perfect one. Then make LearningBot extends that one.
 * 
 * Available robots at http://www.robocoderepository.com
 * @author charles
 *
 */
public class SuperClass extends AdvancedRobot
{
	//Constants
	static final int    GUESS_FACTORS = 25;
	static final int    MIDDLE_FACTOR = (GUESS_FACTORS - 1) / 2;
	static final double MAXIMUM_ESCAPE_ANGLE = 0.72727272727272727272727272727273; //8 / 11
	static final double FACTOR_ANGLE = MAXIMUM_ESCAPE_ANGLE / MIDDLE_FACTOR;
	
	//Global Variables
	static double direction = 1;
	static double enemyBulletSpeed;	
	static double enemyDirection;
	static double enemyEnergy;
	static double enemyX;
	static double enemyY;
	static int    movementMode;
	
	static ArrayList hitScans = new ArrayList();
	
	public void onStatus(StatusEvent e)
	{		
		//turn the radar every tick
		//Putting the code here instead of in a while(true) loop in the run() method saves one byte.
		//I believe Wompi discovered this.
    	setTurnRadarRightRadians(1);
		
		//set the radar and gun to turn independently
		//Putting these here with the radar code lets me get rid of the run() method, saving one byte.  credit to Cotillion
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		
		//keep the DC log at a reasonable size
		if (hitScans.size() > 2000)
		{
			hitScans.remove(0);
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e)
	{
		//Local Variables
		double absoluteBearing;
		double enemyDistance;
		double localEnemyDirection;
		double relativeHeading;
		double offset = 2;
		double theta;
		
		//fire a wave
		Wave wave;
		addCustomEvent(wave = new Wave());
		
		//update the enemy location variables
		enemyX = (wave.sourceX = getX()) + Math.sin(wave.absoluteBearing = absoluteBearing = 
			(e.getBearingRadians() + getHeadingRadians())) * (enemyDistance = e.getDistance());
		enemyY = (wave.sourceY = getY()) + Math.cos(absoluteBearing) * enemyDistance;
		
		/*********************************************
		 *---------------MOVEMENT CODE---------------*
		 *********************************************/				
		
		//fire medium power bullets most of the time, but use full power at very close range
		setFire(2 + (100 / (int)enemyDistance));
		
		//wall smoothing	
		while(!(new Rectangle2D.Double(18, 18, 764, 564)).
			contains(getX() + 160 * Math.sin(theta = absoluteBearing + direction * (offset -= .02)), getY() + 160 * Math.cos(theta)));
		setTurnRightRadians(Math.tan(theta -= getHeadingRadians()));
			
		//Stop and Go movement originally based on Thorn's
		//move when the enemy fires, or when the robot is moving randomly
		double energyDelta;
		if ((energyDelta = (enemyEnergy - (enemyEnergy = e.getEnergy()))) > movementMode)
		{			
			//look-up the enemy energy drop and retrieve the appropriate Stop and Go movement length
			//credit to HedgehogGF for the copySign trick
			setAhead(Math.copySign(MOVEMENT_LENGTHS.charAt((int)energyDelta + 100), Math.cos(theta)));
		}
		
		//Random movement from Toorkild
		//don't reverse randomly if the bot is in Stop and Go mode
		//reverse direction if the bot gets too close to a wall
		if (Math.random() < (-0.6 * Math.sqrt(enemyBulletSpeed / enemyDistance) + 0.04) * movementMode || offset < Math.PI/3.5)
		{
			direction = -direction;
		}
		
		/********************************************
		 *--------------TARGETING CODE--------------*
		 ********************************************/
		
		//determine the enemy's lateral movement direction
		//use a simple rolling average to store the previous lateral direction if enemy lateral velocity == 0
		//credit to HedgehogGF
		wave.enemyDirection = localEnemyDirection = (enemyDirection = Math.signum(0.00000000000001 + ((e.getVelocity() * (relativeHeading = Math.sin(e.getHeadingRadians() - absoluteBearing)))) + (enemyDirection / 100))) * FACTOR_ANGLE;
							
		//determine the current situation
		double[] localScan = wave.scan = new double[]
		{
			0,
			enemyDistance / 1000,          //distance dimension
			Math.abs(e.getVelocity()) / 8, //velocity dimension
			Math.abs(relativeHeading)      //relative heading dimension
		};

		//find the most visited guess factor for the current situation
		//The DC is based on CunobelinDC's movement.
		int mostVisited = MIDDLE_FACTOR;
		double bestScore = 0;
		int i = GUESS_FACTORS - 1;
		do
		{
			double score = 0;
			Iterator iter = hitScans.iterator();
		
			while(iter.hasNext())
			{
				double[] scan = (double[])iter.next();
				double dist = 0;
				int j;
				for(j = 1; j < scan.length; j++)
				{
					dist += Math.abs(localScan[j] - scan[j]);
				}
				score += 1/(dist*((1.0/50) + Math.abs(i - scan[0])));
			}
				
			if (score > bestScore)
			{
				mostVisited = i;
				bestScore = score;
			}
		}
		while (--i >= 0);
		
		//turn the gun to the most visited guess factor
		//The slight offset helps to defeat simple bullet shielding.
		setTurnGunRightRadians(0.0005 + Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()
			+ (localEnemyDirection * (mostVisited - MIDDLE_FACTOR))));
		
		//radar lock
		setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
	}
	
	public void onBulletHit(BulletHitEvent e)
	{
		//adjust the enemy energy variable when the bot hits the enemy
		//This makes a big difference against linear targeting.
		enemyEnergy -= 10;
	}
	
	public void onHitByBullet(HitByBulletEvent e)
	{
		//adjust the enemy energy variable when the bot gets hit
		//store the velocity of the enemy's bullet for the Random movement
		enemyEnergy += 20 - (enemyBulletSpeed = e.getVelocity());
    }
	
	public void onDeath(DeathEvent e)
	{
		//If the bot dies in the first five rounds, switch to RM.
		if (getRoundNum() < 5)
		{
			movementMode = -1;
		}
	}
	
	static class Wave extends Condition
	{
		//Global Variables
		double absoluteBearing;
		double enemyDirection;
		double distanceTraveled;
		double sourceX;
		double sourceY;
		double[] scan;
		
		public boolean test()
		{
			//check if the wave has passed the enemy's current location
			if (Math.abs((distanceTraveled += 14) - Point2D.distance(sourceX, sourceY, enemyX, enemyY)) <= 7)
			{
				//calculate the guess factor that the enemy has visited
				scan[0] = Math.round(((Utils.normalRelativeAngle(Math.atan2(enemyX - sourceX,
					enemyY - sourceY) - absoluteBearing)) / enemyDirection) + MIDDLE_FACTOR);
					
				//add this wave's scan array to the ArrayList		
				hitScans.add(this.scan);
			}
			return false;
		}
	}
	
	//the maximum number of pixels the Stop and Go can move before the enemy can fire again
	//Actually, these are less than the theoretical maxima, because it seems that in practice, the theoretical maxima were a bit too long.
	static final String MOVEMENT_LENGTHS = ""

		//just to be safe, keep one hundred cells filled with the minimum movement length at the beginning of the table
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27
																																									
		+ (char)27 + (char)33 + (char)48
		+ (char)64		
		
		//If the the energy drop is between 3 and 6, the enemy has likely just hit a wall the same tick in which they fired.
		//assume that the enemy was moving at full speed and infer their bullet power
		+ (char)33 + (char)48 + (char)64
		
		//just to be safe, keep one hundred cells filled with the minimum movement length at the end of the table
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27 + (char)27 + (char)27
		+ (char)27;
}																																												