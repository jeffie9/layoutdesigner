/*
 * $Id$
 * 
 * Copyright � 2010 Jeff Eltgroth.
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
package net.sf.layoutdesigner.file;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.layoutdesigner.scenery.Branch;
import net.sf.layoutdesigner.scenery.RailCar;
import net.sf.layoutdesigner.scenery.SceneryManager;
import net.sf.layoutdesigner.scenery.Train;
import net.sf.layoutdesigner.util.Geometry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XMLReader {
	public static void main(String[] args) throws Exception {
		//File file = new File(System.getProperty("user.home") + "\\Documents\\test_layout.xml");
		File file = new File("scenery/library.xml");
		XMLReader reader = new XMLReader();
		//reader.readFile(file);
		List<Shape> shapes = reader.readLibraryFile(file);
		for (Shape shape : shapes) {
			System.out.println(shape);
		}
	}
	
	public void readFile(File file) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setNamespaceAware(true);
        dbf.setValidating(false);

        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document doc = db.parse(file);
        
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        Element root = (Element) xpath.evaluate("/layout", doc, XPathConstants.NODE);
        SceneryManager.getInstance().setHeight((int) makeDouble("height", root));
        SceneryManager.getInstance().setWidth((int) makeDouble("width", root));
        NodeList nodes = (NodeList) xpath.evaluate("/layout/branches/branch", doc, XPathConstants.NODESET);
        readBranches(nodes);
        nodes = (NodeList) xpath.evaluate("/layout/junctions/junction", doc, XPathConstants.NODESET);
        readJunctions(nodes);
        nodes = (NodeList) xpath.evaluate("/layout/trains/train", doc, XPathConstants.NODESET);
        readTrains(nodes);

	}
	
	public void readBranches(NodeList branchNodes) {
		for (int i = 0; i < branchNodes.getLength(); i++) {
			Branch branch = new Branch();
			SceneryManager.getInstance().getBranches().add(branch);
			NodeList shapeNodes = branchNodes.item(i).getChildNodes();
			if (shapeNodes != null) {
				for (int j = 0; j < shapeNodes.getLength(); j++) {
					if (shapeNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element shapeElement = (Element) shapeNodes.item(j);
						Shape shape = makeShape(shapeElement);
						if (shape != null) {
							branch.addShape(shape);
						}
					}
				}
			}
		}
	}
	
	public void readJunctions(NodeList junctionNodes) {
		for (int i = 0; i < junctionNodes.getLength(); i++) {
			if (junctionNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element junctionElement = (Element) junctionNodes.item(i);
				Point2D location = new Point2D.Double(makeDouble("locX", junctionElement),
						makeDouble("locY", junctionElement));
				SceneryManager.getInstance().addJunction(location);
			}
		}
	}
	
	public void readTrains(NodeList trainNodes) {
		for (int i = 0; i < trainNodes.getLength(); i++) {
			Train train = new Train();
			SceneryManager.getInstance().getTrains().add(train);
			NodeList carNodes = trainNodes.item(i).getChildNodes();
			if (carNodes != null) {
				for (int j = 0; j < carNodes.getLength(); j++) {
					if (carNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element carElement = (Element) carNodes.item(j);
						Point2D location = new Point2D.Double(makeDouble("locX", carElement), 
								makeDouble("locY", carElement));
						Point2D towards = new Point2D.Double(makeDouble("toX", carElement), 
								makeDouble("toY", carElement));
						RailCar car = new RailCar();
						car.setLoc(location);
						car.setPointTowards(towards);
						
						for (Branch branch : SceneryManager.getInstance().getBranches()) {
							Shape[] shapes = Geometry.findShapesAtPoint(location, branch, Geometry.POINT_ERROR);
							if (shapes != null) {
								car.setBranch(branch);
								car.setShape(shapes[0]);
							}
						}
						car.move(0.0);
						train.addCar(car);
					}
				}
			}
		}
	}
	
	public List<Shape> readLibraryFile(File file) throws Exception {
		List<Shape> shapes = new ArrayList<Shape>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setNamespaceAware(true);
        dbf.setValidating(false);

        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document doc = db.parse(file);
        
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("/library/curve | /library/straight", doc, XPathConstants.NODESET);
		if (nodes != null) {
			for (int j = 0; j < nodes.getLength(); j++) {
				if (nodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
					Element shapeElement = (Element) nodes.item(j);
					Shape shape = makeShape(shapeElement);
					if (shape != null) {
						shapes.add(shape);
					}
				}
			}
		}
		return shapes;
	}
	
	private double makeDouble(String name, Element element) {
		String value = element.getAttribute(name);
		if (value != null) {
			return Double.parseDouble(value);
		}
		return Double.NaN;
	}
	
	private Shape makeShape(Element shapeElement) {
		if ("straight".equals(shapeElement.getLocalName())) {
			Line2D line = new Line2D.Double(
					makeDouble("x1", shapeElement),
					makeDouble("y1", shapeElement),
					makeDouble("x2", shapeElement),
					makeDouble("y2", shapeElement));
			return line;
		} else if ("curve".equals(shapeElement.getLocalName())) {
			Point2D start = new Point2D.Double(makeDouble("x1", shapeElement),
					makeDouble("y1", shapeElement));
			Point2D end = new Point2D.Double(makeDouble("x2", shapeElement),
					makeDouble("y2", shapeElement));
			Point2D center = new Point2D.Double(makeDouble("xc", shapeElement),
					makeDouble("yc", shapeElement));
			double radius = start.distance(center);
			Arc2D arc = new Arc2D.Double(center.getX() - radius, center.getY() - radius,
					radius * 2.0, radius * 2.0, 0.0, 0.0, Arc2D.OPEN);
			arc.setAngles(start, end);
			return arc;
		}
		return null;
	}
}
