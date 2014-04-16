package fr.insarennes.learningbot.model;

public class Coordinates {
	private double x;


	private double y;
	
	public Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	/**
	 * Calculates the direction of a vector (from 0° (north) to 360° clockwise)
	 * @param x1 The start point X
	 * @param x2 The end point X
	 * @param y1 The start point Y
	 * @param y2 The end point Y
	 * @return
	 */
	public static double getVectorDirection(Coordinates from, Coordinates to) {
		double angleRad = Math.atan2(to.getY()-from.getY(), to.getX()-from.getX());
		double angle = Math.toDegrees(angleRad); /* Radian angle starts at east */
		angle = Math.abs(angle-180);
		angle = (angle < 90) ? angle + 270 : angle - 90; /* Angle from 0 (north) to 360° clockwise */
		return angle;
	}
}
