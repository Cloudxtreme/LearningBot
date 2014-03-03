package kc.micro;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Condition;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * Thorn - a robot by Kevin Clark (Kev).
 * See http://robowiki.net/wiki/Thorn for more information. 
 *
 * Code is released under the RoboWiki Public Code Licence (see http://robowiki.net/wiki/RWPCL)
 */
public class Thorn extends AdvancedRobot {
	static final int AIM_FACTORS = 62;
	static final int MIDDLE_FACTOR = 31;
	static final double ESCAPE_ANGLE = 0.03924165;//MAX_ESCAPE_ANGLE * 2 / MIDDLE_FACTOR

	static Point2D.Double enemyLocation;
	static double lastEnergy;
	static double lastBulletSpeed; 
	static double lastVelocity;
	static double lastOrbitDirection;
	
	static int direction = 1;
	static boolean isFlat;
	
	static double[][][][][][] data = new double[5][3][5][3][2][AIM_FACTORS];
	
	public void run() {
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		
		do {
			turnRadarLeftRadians(1);
		} while(true);
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {			
		Wave w = new Wave();
		
		double temp = e.getDistance();//used for distance, target heading, and energy difference
		int temp2 = (int)(Math.min(temp / 100, 1));//used for anti-ram mode and current guessfactor
		
		double absoluteBearing;
		enemyLocation = projectMotion(absoluteBearing = w.absoluteBearing = e.getBearingRadians() + getHeadingRadians(), temp);
		w.source = new Point2D.Double(getX(), getY());
		
		w.guessFactors = data[(int)(temp / 200)]
		                     [1 + (int)Math.signum(lastVelocity - (lastVelocity = Math.abs(e.getVelocity())))]
		                     [(int)(lastVelocity) >> 1]
		                     [fieldContains(absoluteBearing + ((0.55 / ESCAPE_ANGLE) * (w.orbitDirection = lastOrbitDirection = (lastVelocity == 0 ? lastOrbitDirection : (Math.sin(e.getHeadingRadians() - absoluteBearing) * e.getVelocity() < 0 ? -ESCAPE_ANGLE : ESCAPE_ANGLE)))), temp) ? 1 : 0]
							 [fieldContains(absoluteBearing - ((0.55 / ESCAPE_ANGLE)  * lastOrbitDirection), temp) ? 1 : 0];
		
		double targetHeading;
        double offset = Math.PI/2 + 1 - (temp / 530);
	    while(!fieldContains(targetHeading = absoluteBearing + (direction * (offset -= 0.01)), 170));
		if((isFlat && Math.random() < temp2 * 0.65 * Math.pow(temp / lastBulletSpeed, -0.65)) || offset < Math.PI/3.5) {
			direction = -direction;
		}
		setTurnRightRadians(Math.tan(targetHeading -= getHeadingRadians()));
    	
		if(((temp = lastEnergy - e.getEnergy() + temp2 - 1) > 0 && temp <= 3.0) || isFlat) {
		    setAhead((24 + ((int)(temp / 0.5000001) << 3)) * Math.signum(Math.cos(targetHeading)));
			lastBulletSpeed = Rules.getBulletSpeed(temp);
		}
		
		if((int)(getEnergy()) > 3) {
			setFire(3 - temp2);
			addCustomEvent(w);
		}
		
		int bestGF = MIDDLE_FACTOR;
		try {
			while(true) {
				if(w.guessFactors[temp2] > w.guessFactors[bestGF]) {
					bestGF = temp2;
				}
				temp2++;
			}
		} catch(Exception ex) {}
		
		setTurnGunRightRadians(0.0005 + Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians() + ((bestGF - MIDDLE_FACTOR) * lastOrbitDirection)));
		setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
	
		lastEnergy = e.getEnergy();
	}
	
	public void onBulletHit(BulletHitEvent e) {
		lastEnergy -= 10;
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		lastEnergy += Rules.getBulletHitBonus(e.getPower());
	}

	public void onDeath(DeathEvent e) {
		if(getRoundNum() < 4) {
			isFlat = true;
		}
	}

	boolean fieldContains(double heading, double distance) {
		return new Rectangle2D.Double(18, 18, 764, 564).contains(projectMotion(heading, distance));
	}
	
	Point2D.Double projectMotion(double heading, double distance) {	
		return new Point2D.Double(getX() + (distance * Math.sin(heading)), getY() + (distance * Math.cos(heading)));			
	}
	
	public class Wave extends Condition {
		Point2D.Double source;
		double[] guessFactors;
		double orbitDirection;
		double absoluteBearing; 
		double radius;
		
		public boolean test() {
			if(enemyLocation.distance(source) <= (radius += 14) + 14) {
				try {
					int i = 0;
					while(true) {
						guessFactors[i] += 1 / (1 + Math.abs(i - MIDDLE_FACTOR - (Utils.normalRelativeAngle(Math.atan2(enemyLocation.x - source.x, enemyLocation.y - source.y) - absoluteBearing) / orbitDirection)));	
						i++;
					}
				} catch(Exception ex) {}
		
				removeCustomEvent(this);
			}
			return false;
		}
	}
}