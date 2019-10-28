package algorithms;

import java.awt.Point;

public class ColoredNode {
	public static int id = 0;
	public Point p;
	public Color color;
	public int composant = 0;
	
	public ColoredNode(Point p) {
		this.p = p;
		color = Color.WHITE;
	}
	public ColoredNode(Point p,Color c) {
		this.p = p;
		color = c;
	}
}
