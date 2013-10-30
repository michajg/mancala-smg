package org.mancala.shared;

import java.util.Date;

/**
 * Contains methods to calculate the rating of a player using the Glicko Rating method
 * 
 * @author Harsh
 * 
 */
public class GlickoRating {
	private final static double c = 18.132;
	private final static double q = 0.00575646273;

	private static double getRD(double oldRD, int t) {
		return Math.min(Math.sqrt(oldRD * oldRD + c * c * t), 350.0);
	}

	private static double gRDi(double RDi) {
		return 1 / Math.sqrt(1 + (3 * q * q * RDi * RDi / (Math.PI * Math.PI)));
	}

	private static double E(double RDi, double r, double ri) {
		return 1 / (1 + (Math.pow(10, (gRDi(RDi) * (r - ri) / (-400.0)))));
	}

	private static double d2(double r, double RD, double rating) {
		double gRD = gRDi(RD);
		double g2 = gRD * gRD;
		double E = E(RD, r, rating);
		double temp = (g2 * E * (1 - E));
		return 1 / (q * q * temp);
	}

	public static double newRD(double rating, double RD, double oppRating, double oppRD, int t) {
		double d2 = d2(rating, oppRD, oppRating);
		double RD2 = Math.pow(getRD(RD, t), 2);
		double tmp = 1 / (1 / RD2 + 1 / d2);
		return Math.sqrt(tmp);
	}

	public static double newRating(double plRating, double plRD, double opRating, double opRD, double s, int t) {
		double r0 = plRating;
		double E = E(opRD, r0, opRating);
		double summ = gRDi(opRD) * (s - E);
		double RD2 = q * Math.pow(newRD(plRating, plRD, opRD, opRating, t), 2);
		return r0 + summ * RD2;
	}

	public static int getNumDays(Date a, Date b) {
		return (int) Math.abs(a.getTime() - b.getTime()) / (1000 * 3600 * 24);
	}
}