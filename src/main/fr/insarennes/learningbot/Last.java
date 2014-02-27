package fr.insarennes.learningbot;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.*;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;




// GuessFactorTargeting 

public class Last extends AdvancedRobot {	
	private File fichier;
	private ArrayList<String> alStr;
	private ArrayList<Bullet> alBullet;
	private static final double BULLET_POWER = 2;

	private static double lateralDirection;
	private static double lastEnemyVelocity;
	private static GFTMovement movement;

	public Last() {
		movement = new GFTMovement(this);
	}

	public void run() {
		
			alStr = new ArrayList<String>();
		alBullet = new ArrayList<Bullet>();
		fichier = getDataFile("donnees.data");
		setColors(Color.BLUE, Color.BLACK, Color.YELLOW);
		lateralDirection = 1;
		lastEnemyVelocity = 0;
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		do {
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		} while (true);
		
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyDistance = e.getDistance();
		double enemyVelocity = e.getVelocity();
		if (enemyVelocity != 0) {
			lateralDirection = GFTUtils.sign(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
		}
		GFTWave wave = new GFTWave(this);
		wave.gunLocation = new Point2D.Double(getX(), getY());
		GFTWave.targetLocation = GFTUtils.project(wave.gunLocation, enemyAbsoluteBearing, enemyDistance);
		wave.lateralDirection = lateralDirection;
		wave.bulletPower = BULLET_POWER;
		wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
		lastEnemyVelocity = enemyVelocity;
		wave.bearing = enemyAbsoluteBearing;
		setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
		if (getGunHeat() == 0) {
			alBullet.add(fireBullet(wave.bulletPower));out.println("" +wave.bulletPower);
			recupererDonnneesScan(e);
		}	if (getEnergy() >= BULLET_POWER) {
			addCustomEvent(wave);
		}
		movement.onScannedRobot(e);
		setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()) * 2);
		
	}
	private void recupererDonnneesScan(ScannedRobotEvent e) {
		String ligne;
			final double eAbsBearing = getHeadingRadians() + e.getBearingRadians();
			final double rX = getX(), rY = getY(), bV = Rules.getBulletSpeed(2);
		    final double eX = rX + e.getDistance()*Math.sin(eAbsBearing),
		        eY = rY + e.getDistance()*Math.cos(eAbsBearing),
		        eV = e.getVelocity(),
		        eHd = e.getHeadingRadians();
		 // These constants make calculating the quadratic coefficients below easier
		    final double A = (eX - rX)/bV;
		    final double B = eV/bV*Math.sin(eHd);
		    final double C = (eY - rY)/bV;
		    final double D = eV/bV*Math.cos(eHd);
		 // Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
		    final double a = A*A + C*C;
		    final double b = 2*(A*B + C*D);
		    final double c = (B*B + D*D - 1);
		    final double discrim = b*b - 4*a*c;
		 // Reciprocal of quadratic formula
		    final double t1 = 2*a/(-b - Math.sqrt(discrim));
		    final double t2 = 2*a/(-b + Math.sqrt(discrim));
		    Point2D robotLocation = new Point2D.Double(rX, rY);
			Point2D enemyLocation = GFTUtils.project(robotLocation, eAbsBearing, e.getDistance());
			
			
		 // Linear Targeting 	
			double deltaTime = 0;
			double battleFieldHeight = 600, battleFieldWidth = 800;
			double predictedX = eX, predictedY = eY;
			while((++deltaTime) * (20.0 - 3.0 * 2) < Point2D.Double.distance(rX, rY, predictedX, predictedY)){		
				predictedX += Math.sin(e.getHeadingRadians()) * e.getVelocity();	
				predictedY += Math.cos(e.getHeadingRadians()) * e.getVelocity();
				if(	predictedX < 18.0 
					|| predictedY < 18.0
					|| predictedX > battleFieldWidth - 18.0
					|| predictedY > battleFieldHeight - 18.0){
					predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0);	
					predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
					break;
				}
			}
			
			double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
			double fireLinear  = Utils.normalRelativeAngle(theta - getGunHeadingRadians());
			
	
		 // See .names to find the description of attributes   
		ligne = e.getName() + ", ";
		ligne += e.getBearing() + ", ";
		ligne += e.getDistance() + ", ";
		ligne += e.getHeading() + ", ";
		ligne += e.getVelocity() + ", ";
		ligne += e.getEnergy() + ", ";
		ligne += getGunHeading() + ", ";
		ligne += getGunHeat() + ", ";
		ligne += getGunTurnRemaining() + ", ";
		ligne += getRadarTurnRemaining() + ", ";
		ligne += getRadarHeading()  + ", ";
		ligne += getDistanceRemaining() + ", ";
		ligne += getY() + ", ";
		ligne += getX() + ", ";
		ligne += getTurnRemaining() + ", ";
		ligne += eAbsBearing + ", ";
		ligne += Utils.normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()))  + ", "; // Lock on to our target
		ligne += Math.sin(e.getHeadingRadians()) * e.getVelocity() + ", ";
		ligne += Math.cos(e.getHeadingRadians()) * e.getVelocity() + ", ";
		ligne += e.getVelocity() * Math.sin(e.getHeadingRadians() - (e.getBearingRadians() + getHeadingRadians())) + ", "; // lateralDirection: The Direction perpendicular to our direction
		ligne += e.getVelocity() * -1 * Math.cos(e.getHeadingRadians() - (e.getBearingRadians() + getHeadingRadians())) + ", "; // parallelDirection: The Direction parallel to our direction.
		ligne += robocode.util.Utils.normalRelativeAngle(eAbsBearing - getGunHeadingRadians()) + ", "; // turn the gun to the predicted x,y location
		ligne += Utils.normalRelativeAngle(eAbsBearing - getRadarHeadingRadians())+ ", ";
		ligne += Utils.normalRelativeAngle(eAbsBearing - getGunHeadingRadians() + (e.getVelocity() * Math.sin(e.getHeadingRadians() - eAbsBearing) / 14.0))+ ", "; 
		/* (LateralVelocity / bullet speed), bullet speed = 20 - (3 * power) = 14, power = 2 */
		ligne += eX + ", ";
		ligne += eY + ", ";
		ligne += A + ", ";
		ligne += B + ", ";
		ligne += C + ", ";
		ligne += D + ", ";
		ligne += a + ", ";
		ligne += b + ", ";
		ligne += c + ", ";
		ligne += discrim + ", ";
		ligne += t1 + ", ";
		ligne += t2 + ", ";
		ligne += robotLocation + ", ";
		ligne += enemyLocation + ", ";
		ligne += fireLinear + ", ";
		alStr.add(ligne);
	}
	private void recupererDonneesBalle() {
		String ligne;
		int cpt = 0;
		for (Bullet bullet : alBullet) {
			ligne = bullet.getPower() + ", ";
			if (bullet.getVictim() != null)
				ligne += "hit"  + ".\n";
			else
				ligne += "missed"  + ".\n";
			
			alStr.set(cpt, alStr.get(cpt) + ligne);
			
			cpt++;
			}
		}
		public void onRoundEnded(RoundEndedEvent event) {
		try {out.println("The round has ended");
			recupererDonneesBalle();
			ecrireDonnees();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void ecrireDonnees() throws IOException {
		RobocodeFileWriter fw = new RobocodeFileWriter(fichier.getAbsolutePath(), true);

		for (String ligne : alStr)
			fw.write(ligne);

		fw.close();
	}

}

class GFTWave extends Condition {
	static Point2D targetLocation;

	double bulletPower;
	Point2D gunLocation;
	double bearing;
	double lateralDirection;

	private static final double MAX_DISTANCE = 1000;
	private static final int DISTANCE_INDEXES = 5;
	private static final int VELOCITY_INDEXES = 5;
	private static final int BINS = 25;
	private static final int MIDDLE_BIN = (BINS - 1) / 2;
	private static final double MAX_ESCAPE_ANGLE = 0.7;
	private static final double BIN_WIDTH = MAX_ESCAPE_ANGLE / (double)MIDDLE_BIN;

	private static int[][][][] statBuffers = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS];

	private int[] buffer;
	private AdvancedRobot robot;
	private double distanceTraveled;

	GFTWave(AdvancedRobot _robot) {
		this.robot = _robot;
	}

	public boolean test() {
		advance();
		if (hasArrived()) {
			buffer[currentBin()]++;
			robot.removeCustomEvent(this);
		}
		return false;
	}

	double mostVisitedBearingOffset() {
		return (lateralDirection * BIN_WIDTH) * (mostVisitedBin() - MIDDLE_BIN);
	}

	void setSegmentations(double distance, double velocity, double lastVelocity) {
		int distanceIndex = (int)(distance / (MAX_DISTANCE / DISTANCE_INDEXES));
		int velocityIndex = (int)Math.abs(velocity / 2);
		int lastVelocityIndex = (int)Math.abs(lastVelocity / 2);
		buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
	}

	private void advance() {
		distanceTraveled += GFTUtils.bulletVelocity(bulletPower);
	}

	private boolean hasArrived() {
		return distanceTraveled > gunLocation.distance(targetLocation) - 18;
	}

	private int currentBin() {
		int bin = (int)Math.round(((Utils.normalRelativeAngle(GFTUtils.absoluteBearing(gunLocation, targetLocation) - bearing)) /
				(lateralDirection * BIN_WIDTH)) + MIDDLE_BIN);
		return GFTUtils.minMax(bin, 0, BINS - 1);
	}

	private int mostVisitedBin() {
		int mostVisited = MIDDLE_BIN;
		for (int i = 0; i < BINS; i++) {
			if (buffer[i] > buffer[mostVisited]) {
				mostVisited = i;
			}
		}
		return mostVisited;
	}
}

class GFTUtils {
	static double bulletVelocity(double power) {
		return 20 - 3 * power;
	}

	static Point2D project(Point2D sourceLocation, double angle, double length) {
		return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
				sourceLocation.getY() + Math.cos(angle) * length);
	}

	static double absoluteBearing(Point2D source, Point2D target) {
		return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
	}

	static int sign(double v) {
		return v < 0 ? -1 : 1;
	}

	static int minMax(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}

class GFTMovement {
	private static final double BATTLE_FIELD_WIDTH = 800;
	private static final double BATTLE_FIELD_HEIGHT = 600;
	private static final double WALL_MARGIN = 18;
	private static final double MAX_TRIES = 125;
	private static final double REVERSE_TUNER = 0.421075;
	private static final double DEFAULT_EVASION = 1.2;
	private static final double WALL_BOUNCE_TUNER = 0.699484;

	private AdvancedRobot robot;
	private Rectangle2D fieldRectangle = new Rectangle2D.Double(WALL_MARGIN, WALL_MARGIN,
		BATTLE_FIELD_WIDTH - WALL_MARGIN * 2, BATTLE_FIELD_HEIGHT - WALL_MARGIN * 2);
	private double enemyFirePower = 3;
	private double direction = 0.4;

	GFTMovement(AdvancedRobot _robot) {
		this.robot = _robot;
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double enemyAbsoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
		double enemyDistance = e.getDistance();
		Point2D robotLocation = new Point2D.Double(robot.getX(), robot.getY());
		Point2D enemyLocation = GFTUtils.project(robotLocation, enemyAbsoluteBearing, enemyDistance);
		Point2D robotDestination;
		double tries = 0;
		while (!fieldRectangle.contains(robotDestination = GFTUtils.project(enemyLocation, enemyAbsoluteBearing + Math.PI + direction,
				enemyDistance * (DEFAULT_EVASION - tries / 100.0))) && tries < MAX_TRIES) {
			tries++;
		}
		if ((Math.random() < (GFTUtils.bulletVelocity(enemyFirePower) / REVERSE_TUNER) / enemyDistance ||
				tries > (enemyDistance / GFTUtils.bulletVelocity(enemyFirePower) / WALL_BOUNCE_TUNER))) {
			direction = -direction;
		}
		// Jamougha's cool way
		double angle = GFTUtils.absoluteBearing(robotLocation, robotDestination) - robot.getHeadingRadians();
		robot.setAhead(Math.cos(angle) * 100);
		robot.setTurnRightRadians(Math.tan(angle));
	}
}
																																																																													
