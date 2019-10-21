package algorithms;

import java.awt.Point;

public class ColoredNode {
	public Point p;
	public Color color;
	public int idCompenent = -1;
	
	public ColoredNode(Point p) {
		this.p = p;
		color = Color.GREY;
	}
	public ColoredNode(Point p,Color c) {
		this.p = p;
		color = c;
	}
}
