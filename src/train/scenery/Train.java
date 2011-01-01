/*
 * $Id$
 * 
 * Copyright © 2010 Jeff Eltgroth.
 * 
 * This file is part of Layout Designer.
 *
 * Layout Designer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Layout Designer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Layout Designer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package train.scenery;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Train implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<RailCar> cars;
	private int speed;
	private int throttle;
	public enum PowerDirection {FORWARD, BACKWARD, NEUTRAL};

	private PowerDirection dir;
	
	public Train() {
		cars = new ArrayList<RailCar>();
		speed = 0;
		dir = PowerDirection.FORWARD;
	}
	
	public List<RailCar> getCars() {
		return cars;
	}
	
	public void addCar(RailCar car) {
		cars.add(car);
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public void setDirection(PowerDirection dir) {
		if (this.dir != dir) {
			for (RailCar car : cars) {
				car.changeDirection();
			}
			this.dir = dir;
		}
	}
	
	public void render(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		for (RailCar car : cars) {
			car.render(g2);
		}
	}
	
	public void update() {
		for (RailCar car : cars) {
			car.move(speed);
		}
	}
	
	public RailCar findCarAtPoint(Point2D point) {
		for (RailCar car : cars) {
			if (car.contains(point.getX(), point.getY())) {
				return car;
			}
		}
		return null;
	}
}
