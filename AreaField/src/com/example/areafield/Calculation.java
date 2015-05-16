package com.example.areafield;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;

public class Calculation {

	public LatLng coordinatesSecondLine(Location location, double radius) {

		double R = 6371d;
		double d = (radius / R) / 1000;
		
		double brng = Math.toRadians(location.getBearing() + 90);
		double latitudeRad = Math.asin(Math.sin(Math.toRadians(location
				.getLatitude()))
				* Math.cos(d)
				+ Math.cos(Math.toRadians(location.getLatitude()))
				* Math.sin(d) * Math.cos(brng));
		double longitudeRad = (Math.toRadians(location.getLongitude()) + Math
				.atan2(Math.sin(brng) * Math.sin(d)
						* Math.cos(Math.toRadians(location.getLatitude())),
						Math.cos(d)
								- Math.sin(Math.toRadians(location
										.getLatitude()))
								* Math.sin(latitudeRad)));

		return (new LatLng(Math.toDegrees(latitudeRad),
				Math.toDegrees(longitudeRad)));
	}

	public double routing(Location locationStart, Location locationFinish) {

		double lat1 = locationStart.getLatitude();
		double lon1 = locationStart.getLongitude();
		double lat2 = locationFinish.getLatitude();
		double lon2 = locationFinish.getLongitude();
		double R = 6371; // km
		double dLat = (lat2 - lat1) * Math.PI / 180;
		double dLon = (lon2 - lon1) * Math.PI / 180;
		lat1 = lat1 * Math.PI / 180;
		lat2 = lat2 * Math.PI / 180;

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
				* Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c * 1000;

		return d;

	}

}
