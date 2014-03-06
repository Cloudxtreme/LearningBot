package fr.insarennes.learningbot.old;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.RobocodeFileWriter;
import robocode.RoundEndedEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdom2.DataConversionException;

/**
 * Last : a modification of Thorn created by Kevin Clark.
 * see http://robowiki.net/wiki/Thorn
 * Last uses supervised learning thanks to a XML tree made by BonzaiBoost
 * and saves datas about every shot he performs.
 * see http://bonzaiboost.gforge.inria.fr/
 */
public class Last extends AdvancedRobot {
	/**
	 * A buffer where important datas are saved at the end of each round
	 * The datas are later copied on the hard drive in the .data file 
	 */
	private List<String> alStr;
	/**
	 * The list of all the bullets shot by Last and that didn't hit yet
	 */
	private List<Bullet> alBullet;
	/**
	 * Contains the value of all the attributes  linked to a shot
	 * for 1 given shot (see .names file for the attributes list)
	 */
	private Map<String, Double> ensAttr;
	/**
	 * Representation of the XML tree
	 */
	private Tree tree;
	/**
	 * A pseudorandom number generator used to randomly select
	 * the color of the robot every time one of its bullets disappears
	 */
	Random colorNumber = new Random();
	/**
	 * Informations about the last robot that has been scanned
	 */
	public ScannedRobotEvent lastScan;

	private File fichier;

	final int AIM_FACTORS = 62;
	final int MIDDLE_FACTOR = 31;
	final double ESCAPE_ANGLE = 0.03924165;// MAX_ESCAPE_ANGLE * 2 /
	// MIDDLE_FACTOR
	static Point2D.Double enemyLocation;
	static double lastEnergy;
	static double lastBulletSpeed;
	static double lastVelocity;
	static double lastV;
	static double lastOrbitDirection;
	static double lastEnemyVChangeTime;
	static double lastEnemyAccelTime;
	static double lastEnemyDeccelTime;
	static double lastEnemyHeading;
	static double bulletX;
	static double bulletY;
	static int direction = 1;
	static boolean isFlat = true;
	int i = 0;
	double[][][][][][] data = new double[5][3][5][3][2][AIM_FACTORS];

	/**
	 * A table of tables which contains availables colors for the robot
	 */
	Color[] lightColors, mediumColors, darkColors;
	/**
	 * The index of the current color of the robot in the Color table
	 * assuming that lightColors, mediumCollors and darkColors are adjacent
	 * e.g. If curCol == 10 then the color is the first of the second (10-8) table : Orange
	 */
	int curCol;

	public Last() {
		// Tango! Desktop Project colors

		lightColors = new Color[9];
		lightColors[0] = new Color(252, 233, 79);
		lightColors[1] = new Color(252, 175, 62);
		lightColors[2] = new Color(233, 185, 110);
		lightColors[3] = new Color(138, 226, 52);
		lightColors[4] = new Color(114, 159, 207);
		lightColors[5] = new Color(173, 127, 168);
		lightColors[6] = new Color(239, 41, 41);
		lightColors[7] = new Color(238, 238, 236);
		lightColors[8] = new Color(136, 138, 133);

		mediumColors = new Color[9];
		mediumColors[0] = new Color(237, 212, 0); // Butter
		mediumColors[1] = new Color(245, 121, 0); // Orange
		mediumColors[2] = new Color(193, 125, 17); // Chocolate
		mediumColors[3] = new Color(115, 210, 22); // Chameleon
		mediumColors[4] = new Color(52, 101, 164); // Sky blue
		mediumColors[5] = new Color(117, 80, 123); // Plum
		mediumColors[6] = new Color(204, 0, 0); // Scarlet red
		mediumColors[7] = new Color(211, 215, 207);// Aluminium
		mediumColors[8] = new Color(85, 87, 83); // Dark

		darkColors = new Color[9];
		darkColors[0] = new Color(196, 160, 0);
		darkColors[1] = new Color(206, 92, 0);
		darkColors[2] = new Color(143, 89, 2);
		darkColors[3] = new Color(78, 154, 6);
		darkColors[4] = new Color(32, 74, 135);
		darkColors[5] = new Color(92, 53, 102);
		darkColors[6] = new Color(164, 0, 0);
		darkColors[7] = new Color(186, 189, 182);
		darkColors[8] = new Color(46, 52, 54);

	}

	public void run() {
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		changeColor();
		alStr = new ArrayList<String>();
		alBullet = new ArrayList<Bullet>();
		ensAttr = new HashMap<String, Double>();
		fichier = getDataFile("donnees.data");

//		do {
			turnRadarLeftRadians(1);
//		} while (this.);
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

	/**
	 * Analyzes datas about the target once it has been scanned, 
	 * and reacts, eventually using the XML decision tree.  
	 * @param ScannedRobotEvent e the datas about the scanned target
	 */
	public void onScannedRobot(ScannedRobotEvent e) {

		if (tree == null) tree = new Tree(e.getName());

		Wave w = new Wave();

		double temp = e.getDistance();// used for distance, target heading, and
		// energy difference
		int temp2 = (int) (Math.min(temp / 100, 1));// used for anti-ram mode
		// and current guessfactor

		double absoluteBearing;
		enemyLocation = projectMotion(
				absoluteBearing = w.absoluteBearing = e.getBearingRadians()
				+ getHeadingRadians(), temp);
		w.source = new Point2D.Double(getX(), getY());

		w.guessFactors = data[(int) (temp / 200)][1 + (int) Math
		                                          .signum(lastVelocity
		                                        		  - (lastVelocity = Math.abs(e.getVelocity())))][(int) (lastVelocity) >> 1][fieldContains(
		                                        				  absoluteBearing
		                                        				  + ((0.55 / ESCAPE_ANGLE) * (w.orbitDirection = lastOrbitDirection = (lastVelocity == 0 ? lastOrbitDirection
		                                        						  : (Math.sin(e.getHeadingRadians()
		                                        								  - absoluteBearing)
		                                        								  * e.getVelocity() < 0 ? -ESCAPE_ANGLE
		                                        										  : ESCAPE_ANGLE)))), temp) ? 1 : 0][fieldContains(
		                                        												  absoluteBearing - ((0.55 / ESCAPE_ANGLE) * lastOrbitDirection),
		                                        												  temp) ? 1 : 0];

		double targetHeading;
		double offset = Math.PI / 2 + 1 - (temp / 530);
		while (!fieldContains(targetHeading = absoluteBearing
				+ (direction * (offset -= 0.01)), 170))
			;
		if ((isFlat && Math.random() < temp2 * 0.65
				* Math.pow(temp / lastBulletSpeed, -0.65))
				|| offset < Math.PI / 3.5) {
			direction = -direction;
		}
		setTurnRightRadians(Math.tan(targetHeading -= getHeadingRadians()));

		if (((temp = lastEnergy - e.getEnergy() + temp2 - 1) > 0 && temp <= 3.0)
				|| isFlat) {
			setAhead((24 + ((int) (temp / 0.5000001) << 3))
					* Math.signum(Math.cos(targetHeading)));
			lastBulletSpeed = Rules.getBulletSpeed(temp);
		}

		try {
			if (getGunHeat() == 0 && getEnergy() >= 3) {
				recupererDonnneesScan(e);
				if (tree.shotTest(tree.getRoot(), ensAttr)) {
					if (i == 1) {
						alBullet.add(fireBullet(3 - temp2));
						addLine(e.getName());
					} else {
						i = 1;
						fireBullet(3 - temp2);
					}
				}

			} } catch (DataConversionException e1) {
				System.out.println("Erreur lors du parcours de l'arbre"); }

		addCustomEvent(w);

		if(e.getEnergy() <= 1){ fireBullet(0.1); }


		if (getGunHeat() == 0) {

			fireBullet(3 - temp2); addCustomEvent(w); } addCustomEvent(w);


			int bestGF = MIDDLE_FACTOR;
			try {
				while (true) {
					if (w.guessFactors[temp2] > w.guessFactors[bestGF]) {
						bestGF = temp2;
					}
					temp2++;
				}
			} catch (Exception ex) {
			}

			setTurnGunRightRadians(0.0005 + Utils
					.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()
							+ ((bestGF - MIDDLE_FACTOR) * lastOrbitDirection)));
			setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(absoluteBearing
					- getRadarHeadingRadians()));

			lastEnergy = e.getEnergy();
	}

	public void onBulletHit(BulletHitEvent e) {
		lastEnergy -= 10;
		bulletX = e.getBullet().getX();
		bulletY = e.getBullet().getY();
		changeColor();
	}

	public void onBulletMissed(BulletMissedEvent e) {
		lastEnergy -= 10;
		bulletX = e.getBullet().getX();
		bulletY = e.getBullet().getY();
		changeColor();
	}

	public void onHitByBullet(HitByBulletEvent e) {
		lastEnergy += Rules.getBulletHitBonus(e.getPower());
		changeColor();
	}

	public void onDeath(DeathEvent e) {
		if (getRoundNum() < 4) {
			isFlat = true;
		}
	}

	boolean fieldContains(double heading, double distance) {
		return new Rectangle2D.Double(18, 18, 764, 564).contains(projectMotion(
				heading, distance));
	}

	Point2D.Double projectMotion(double heading, double distance) {
		return new Point2D.Double(getX() + (distance * Math.sin(heading)),
				getY() + (distance * Math.cos(heading)));
	}

	/**
	 * Gets the datas linked to the last scanned target and the last 
	 * shot and saves them in ensAttr
	 * @param ScannedRobotEvent e the datas about the last sc
	 */
	private void onScannedDatas(ScannedRobotEvent e) {
		ensAttr.clear();

		if (i == 1) {

			long gameTime = getTime();
			int enemyOrbitDirection;
			final double eAbsBearing = getHeadingRadians()
					+ e.getBearingRadians();
			final double rX = getX(), rY = getY(), bV = Rules.getBulletSpeed(2);
			final double eX = rX + e.getDistance() * Math.sin(eAbsBearing), eY = rY
					+ e.getDistance() * Math.cos(eAbsBearing), eV = e
					.getVelocity(), eHd = e.getHeadingRadians();
			// These constants make calculating the quadratic coefficients below
			// easier
			final double A = (eX - rX) / bV;
			final double B = eV / bV * Math.sin(eHd);
			final double C = (eY - rY) / bV;
			final double D = eV / bV * Math.cos(eHd);
			// Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
			final double a = A * A + C * C;
			final double b = 2 * (A * B + C * D);
			final double c = (B * B + D * D - 1);
			final double discrim = b * b - 4 * a * c;
			// Reciprocal of quadratic formula
			final double t1 = 2 * a / (-b - Math.sqrt(discrim));
			final double t2 = 2 * a / (-b + Math.sqrt(discrim));

			Point2D robotLocation = GFTUtils.project(new Point2D.Double(getX(),
					getY()), getHeadingRadians(), getVelocity());
			Point2D enemyLocation = GFTUtils.project(robotLocation,
					eAbsBearing, e.getDistance());
			Point2D nextEnemyLocation = GFTUtils.project(
					GFTUtils.project(robotLocation, eAbsBearing,
							e.getDistance()), e.getHeadingRadians(),
							e.getVelocity());

			// DrussGT
			double absBear = GFTUtils
					.absoluteBearing(
							GFTUtils.project(enemyLocation,
									lastScan.getHeadingRadians(),
									lastScan.getVelocity()),
									new Point2D.Double(getX(), getY()));

			Point2D.Double myLocation = new Point2D.Double(getX(), getY());

			double velAddition = (eV < 0 ? 2 : 1);
			double predictedVelocity = limit(-8, eV + velAddition, 8);

			Point2D.Double predictedPosition = (java.awt.geom.Point2D.Double) GFTUtils
					.project(myLocation, eHd, predictedVelocity);

			double distance1 = enemyLocation.distanceSq(myLocation);
			double stick = limit(121, distance1, 160);
			double offset = Math.max(Math.PI / 3 + 0.021, Math.PI / 2 + 1
					- limit(0.2, distance1 / (400 * 400), 1.2));
			Point2D endPoint = GFTUtils.project(myLocation, absBear + direction
					* (offset -= 0.02), stick);

			// Serpent
			double bulletPower = 2;
			double bulletSpeed = GFTUtils.bulletSpeed(bulletPower);
			double bulletImpactTime = e.getDistance() / bulletSpeed;
			double maxEscapeAngle = GFTUtils.maxEscapeAngle(bulletSpeed);
			enemyOrbitDirection = GFTUtils.sign(Math.sin(eHd - eAbsBearing)
					* eV);

			double accel = Math.abs(eV - lastV)
					* (Math.abs(eV) < Math.abs(lastV) ? -1 : 1);

			if (Math.abs(accel) > 0.01) {
				lastEnemyVChangeTime = gameTime;
				if (accel < 0) {
					lastEnemyAccelTime = gameTime;
				} else {
					lastEnemyDeccelTime = gameTime;
				}
			}

			double vChangeTimer = (double) (gameTime - lastEnemyVChangeTime)
					/ bulletImpactTime;
			double accelTimer = (double) (gameTime - lastEnemyAccelTime)
					/ bulletImpactTime;
			double deccelTimer = (double) (gameTime - lastEnemyDeccelTime)
					/ bulletImpactTime;

			double nextAccel = 0;
			if (accel != 0) {
				boolean deccelAccelSwitch = false;
				if (eV == 0
						|| (lastV != 0 && GFTUtils.sign(lastV) != GFTUtils
						.sign(eV))) {
					deccelAccelSwitch = true;
				}

				nextAccel = deccelAccelSwitch ? GFTUtils.sign(accel)
						* Math.min(Math.abs(accel), 1) : accel;
			}
			double nextVelocity = GFTUtils.minMax(eV + nextAccel, -8, 8);
			double nextHeading = eHd
					+ Utils.normalRelativeAngle(eHd - lastEnemyHeading);
			Point2D.Double nextEnemyLocation2 = GFTUtils.projectMotion(
					enemyLocation, nextHeading, nextVelocity);

			Point2D.Double enemyProjectedLocation = GFTUtils.projectMotion(
					myLocation, eAbsBearing
					+ (enemyOrbitDirection * maxEscapeAngle),
					e.getDistance());

			double approachAngle = Math.abs(Utils.normalRelativeAngle(eHd
					- eAbsBearing + (eV > 0 ? 0 : Math.PI)));

			double firingAngle1 = GFTUtils.botWidthAngle(30.0, e.getDistance()); // pas
			// complet
			double firingAngle2 = GFTUtils.botWidthAngle(18.0, e.getDistance());

			// Linear Targeting
			double deltaTime = 0;
			double battleFieldHeight = 600, battleFieldWidth = 800;
			double predictedX = eX, predictedY = eY;
			while ((++deltaTime) * (20.0 - 3.0 * 2) < Point2D.Double.distance(
					rX, rY, predictedX, predictedY)) {
				predictedX += Math.sin(e.getHeadingRadians()) * e.getVelocity();
				predictedY += Math.cos(e.getHeadingRadians()) * e.getVelocity();
				if (predictedX < 18.0 || predictedY < 18.0
						|| predictedX > battleFieldWidth - 18.0
						|| predictedY > battleFieldHeight - 18.0) {
					predictedX = Math.min(Math.max(18.0, predictedX),
							battleFieldWidth - 18.0);
					predictedY = Math.min(Math.max(18.0, predictedY),
							battleFieldHeight - 18.0);
					break;
				}
			}

			double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX
					- getX(), predictedY - getY()));
			double fireLinear = Utils.normalRelativeAngle(theta
					- getGunHeadingRadians());

			ensAttr.put("A", A);
			ensAttr.put(
					"AngleEnemy",
					Utils.normalRelativeAngleDegrees(e.getBearing()
							+ (getHeading() - getRadarHeading())));
			ensAttr.put("AngleEnemyX",
					Math.sin(e.getHeadingRadians()) * e.getVelocity());
			ensAttr.put("AngleEnemyY",
					Math.cos(e.getHeadingRadians()) * e.getVelocity());
			ensAttr.put(
					"AngleGun",
					robocode.util.Utils.normalRelativeAngle(eAbsBearing
							- getGunHeadingRadians()));
			ensAttr.put(
					"AngleGunMorePrecision",
					Utils.normalRelativeAngle(eAbsBearing
							- getGunHeadingRadians()
							+ (e.getVelocity()
									* Math.sin(e.getHeadingRadians()
											- eAbsBearing) / 14.0)));
			ensAttr.put(
					"AngleRadar",
					Utils.normalRelativeAngle(eAbsBearing
							- getRadarHeadingRadians()));
			ensAttr.put("B", B);
			ensAttr.put("Bearing", e.getBearing());
			ensAttr.put("C", C);
			ensAttr.put("D", D);
			ensAttr.put("Distance", e.getDistance());
			ensAttr.put("DistanceEnemyX", eX);
			ensAttr.put("DistanceEnemyY", eY);
			ensAttr.put("DistanceRemaining", getDistanceRemaining());
			ensAttr.put("EnemyLocationX", enemyLocation.getX());
			ensAttr.put("EnemyLocationY", enemyLocation.getY());
			ensAttr.put("Energy", e.getEnergy());
			ensAttr.put("FireLinear", fireLinear);
			ensAttr.put("GunHeading", getGunHeading());
			ensAttr.put("GunHeat", getGunHeat());
			ensAttr.put("GunTurnRemaining", getGunTurnRemaining());
			ensAttr.put("Heading", e.getHeading());
			ensAttr.put(
					"LateralVelocity",
					e.getVelocity()
					* Math.sin(e.getHeadingRadians()
							- (e.getBearingRadians() + getHeadingRadians())));
			ensAttr.put("OurRobotX", getX());
			ensAttr.put("OurRobotY", getY());
			ensAttr.put(
					"Parallelvelocity",
					e.getVelocity()
					* -1
					* Math.cos(e.getHeadingRadians()
							- (e.getBearingRadians() + getHeadingRadians())));
			ensAttr.put("RadarHeading", getRadarHeading());
			ensAttr.put("RadarTurnRemaining", getRadarTurnRemaining());
			ensAttr.put("Solution1", t1);
			ensAttr.put("Solution2", t2);
			ensAttr.put("TurnRemaining", getTurnRemaining());
			ensAttr.put("Velocity", e.getVelocity());
			ensAttr.put("a", a);
			ensAttr.put("absBear", absBear);
			ensAttr.put("accel", accel);
			ensAttr.put("accelTimer", accelTimer);
			ensAttr.put("approachAngle", approachAngle);
			ensAttr.put("b", b);
			ensAttr.put("bulletImpactTime", bulletImpactTime);
			ensAttr.put("bulletPower", bulletPower);
			ensAttr.put("bulletSpeed", bulletSpeed);
			ensAttr.put("bulletX", bulletX);
			ensAttr.put("bulletY", bulletY);
			ensAttr.put("c", c);
			ensAttr.put("deccelTimer", deccelTimer);
			ensAttr.put("discrimant", discrim);
			ensAttr.put("distance1", distance1);
			ensAttr.put("eAbsBearing", eAbsBearing);
			ensAttr.put("endPointX", endPoint.getX());
			ensAttr.put("endPointY", endPoint.getY());
			ensAttr.put("enemyLocationX", enemyLocation.getX());
			ensAttr.put("enemyLocationY", enemyLocation.getY());
			// ensAttr.put("enemyOrbitDirection", enemyOrbitDirection);
			ensAttr.put("enemyProjectedLocationX",
					enemyProjectedLocation.getX());
			ensAttr.put("enemyProjectedLocationY",
					enemyProjectedLocation.getY());
			ensAttr.put("firingAngle1", firingAngle1);
			ensAttr.put("firingAngle2", firingAngle2);
			ensAttr.put("lastBulletSpeed", lastBulletSpeed);
			ensAttr.put("lastEnemyAccelTime", lastEnemyAccelTime);
			ensAttr.put("lastEnemyDeccelTime", lastEnemyDeccelTime);
			ensAttr.put("lastEnemyVChangeTime", lastEnemyVChangeTime);
			ensAttr.put("lastEnergy", lastEnergy);
			ensAttr.put("maxEscapeAngle", maxEscapeAngle);
			ensAttr.put("myLocationX", myLocation.getX());
			ensAttr.put("myLocationY", myLocation.getY());
			ensAttr.put("nextAccel", nextAccel);
			ensAttr.put("nextEnemyLocationX", nextEnemyLocation.getX());
			ensAttr.put("nextEnemyLocationY", nextEnemyLocation.getY());
			ensAttr.put("nextEnemyLocation2X", nextEnemyLocation2.getX());
			ensAttr.put("nextEnemyLocation2Y", nextEnemyLocation2.getY());
			ensAttr.put("nextHeading", nextHeading);
			ensAttr.put("nextVelocity", nextVelocity);
			ensAttr.put("offset", offset);
			ensAttr.put("predictedPositionX", predictedPosition.getX());
			ensAttr.put("predictedPositionY", predictedPosition.getY());
			ensAttr.put("predictedVelocity", predictedVelocity);
			ensAttr.put("robotLocationX", robotLocation.getX());
			ensAttr.put("robotLocationY", robotLocation.getY());
			ensAttr.put("stick", stick);
			ensAttr.put("vChangeTimer", vChangeTimer);
			ensAttr.put("velAddition", velAddition);
		}
		// i = 1;
		lastScan = e;
		lastV = e.getVelocity();
	}

	/**
	 * Adds an attribute to ensAttr
	 * @param String name the name of the attribute 
	 */
	private void addLine(String name) {
		String line = "";
		line = name + ", ";

		SortedSet<String> keys = new TreeSet<String>(ensAttr.keySet());
		for (String key : keys)
			line += ensAttr.get(key) + ", ";

		alStr.add(line);
	}

	/**
	 * Saves datas about all the bullets in alBullet
	 */
	private void getBulletDatas() {
		String line;
		int cpt = 0;
		for (Bullet bullet : alBullet) {
			line = bullet.getPower() + ", ";
			if (bullet.getVictim() != null)
				line += "hit" + ".\n";
			else
				line += "missed" + ".\n";

			alStr.set(cpt, alStr.get(cpt) + line);

			cpt++;
		}
	}

	public void onRoundEnded(RoundEndedEvent event) {
		try {
			out.println("The round has ended");
			getBulletDatas();
			writeDatas();

		} catch (IOException e) {
			System.out.println("Data writing error");
		}
	}

	/**
	 * Saves the datas in alStr in the file "target_name".data
	 */
	private void writeDatas() throws IOException {
		File fichier = getDataFile(lastScan.getName() + ".data");
		RobocodeFileWriter fw = new RobocodeFileWriter(
				fichier.getAbsolutePath(), true);

		for (String ligne : alStr)
			fw.write(ligne);

		fw.close();

		alStr.clear();
		alBullet.clear();
	}

	public static double limit(double min, double value, double max) {
		if (value > max)
			return max;
		if (value < min)
			return min;

		return value;
	}

	private void changeColor() {
		int i = colorNumber.nextInt(mediumColors.length);
		while (i == curCol)
			i = colorNumber.nextInt(mediumColors.length - 1);
		changeColor(i);
	}

	private void changeColor(int i) {
		if (i >= mediumColors.length)
			i = mediumColors.length - 1;
		setBodyColor(mediumColors[i]);
		setGunColor(darkColors[i]);
		setRadarColor(lightColors[i]);
		setBulletColor(lightColors[i]);
		setScanColor(lightColors[i]);
		curCol = i;
	}

	public class Wave extends Condition {
		Point2D.Double source;
		double[] guessFactors;
		double orbitDirection;
		double absoluteBearing;
		double radius;

		public boolean test() {
			if (enemyLocation.distance(source) <= (radius += 14) + 14) {
				try {
					int i = 0;
					while (true) {
						guessFactors[i] += 1 / (1 + Math.abs(i
								- MIDDLE_FACTOR
								- (Utils.normalRelativeAngle(Math.atan2(
										enemyLocation.x - source.x,
										enemyLocation.y - source.y)
										- absoluteBearing) / orbitDirection)));
						i++;
					}
				} catch (Exception ex) {
				}

				removeCustomEvent(this);
			}
			return false;
		}
	}
}

class GFTUtils {

	static Point2D project(Point2D sourceLocation, double angle, double length) {
		return new Point2D.Double(sourceLocation.getX() + Math.sin(angle)
				* length, sourceLocation.getY() + Math.cos(angle) * length);
	}

	static double absoluteBearing(Point2D source, Point2D target) {
		return Math.atan2(target.getX() - source.getX(),
				target.getY() - source.getY());
	}

	static int sign(double v) {
		return v > 0 ? 1 : -1;
	}

	public static double bulletSpeed(double power) {
		return 20 - (3 * power);
	}

	public static double maxEscapeAngle(double speed) {
		return Math.asin(8 / speed);
	}

	public static Point2D.Double projectMotion(Point2D source, double heading,
			double distance) {
		return new Point2D.Double(source.getX()
				+ (Math.sin(heading) * distance), source.getY()
				+ (Math.cos(heading) * distance));
	}

	public static double minMax(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	public static double botWidthAngle(double width, double distance) {
		return Math.atan(width / distance);
	}

}
