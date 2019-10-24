package algorithms;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import arbreCouvrant.Steiner;

public class DefaultTeam {
	public ArrayList<Point> calculConnectedDominatingSet(ArrayList<Point> points, int edgeThreshold) {
		ArrayList<Point> clone = (ArrayList<Point>) points.clone();
		// pretraitement pour trouver la plus grande composante connexe ou alors l'input
		// est un graphe connexe
		ArrayList<Point> result = MIS(clone, edgeThreshold);
		// ArrayList<Point> result = gloutonNaif(clone, edgeThreshold);
		System.out.println(isMIS(result, points, edgeThreshold));
		// result = calculSteiner(clone, result, edgeThreshold);
		// System.out.println(isMIS(result, points, edgeThreshold));

		return result;
	}

	public ArrayList<Point> gloutonNaif(ArrayList<Point> points, int edgeThreshold) {
		ArrayList<Point> res = new ArrayList<>();
		@SuppressWarnings("unchecked")
		ArrayList<Point> clonePoints = (ArrayList<Point>) points.clone();

		while (!isValid(points, res, edgeThreshold)) {
			if (clonePoints.size() <= 0) {
				break;
			}
			Point p = findMax(clonePoints, edgeThreshold);
			for (Point n : neighbor(p, points, edgeThreshold)) {
				if (!res.contains(n))
					clonePoints.remove(n);
			}
			clonePoints.remove(p);
			res.add(p);
		}
		return res;
	}
	
	public ArrayList<Point> calculSteiner(ArrayList<Point> points, ArrayList<Point> sol, int edgeTreshold) {
		Steiner steiner = new Steiner();
		ArrayList<Point> res = steiner.calculSteiner(points, edgeTreshold, sol);
		if (res.size() == 0) {
			return sol;
		}
		return res;
	}

	public ArrayList<Point> MIS(ArrayList<Point> points, int edgeThreshold) {
		ArrayList<ColoredNode> coloredPts = colorMyPts(points);
		ArrayList<ColoredNode> black = new ArrayList<>();
		ArrayList<ColoredNode> orange = new ArrayList<>();

		int max = 0;

		ArrayList<ColoredNode> voisin = new ArrayList<>();
		ColoredNode start = coloredPts.get(0);
		for (ColoredNode p : coloredPts) {
			if (whiteNeighbor(p, coloredPts, edgeThreshold).size() > max) {
				voisin = whiteNeighbor(p, coloredPts, edgeThreshold);
				max = voisin.size();
				start = p;
			}
		}
		start.color = Color.BLACK;
		black.add(start);

		for (ColoredNode cn : voisin) {
			cn.color = Color.GREY;
		}
		for (ColoredNode cn : voisin) {
			for (ColoredNode o : whiteNeighbor(cn, coloredPts, edgeThreshold)) {
				o.color = Color.ORANGE;
				orange.add(o);
			}
		}

		while (!orange.isEmpty()) {
			voisin = new ArrayList<>();
			ArrayList<ColoredNode> voisin2 = new ArrayList<>();
			max = 0;
			for (ColoredNode p : orange) {
				if (orangeWhiteNeighbor(p, coloredPts, edgeThreshold).size() > max) {
					voisin = orangeWhiteNeighbor(p, coloredPts, edgeThreshold);
					voisin2 = neighbor(p, coloredPts, edgeThreshold);
					max = voisin.size();
					start = p;
				}
			}
			start.color = Color.BLACK;
			black.add(start);
			for (ColoredNode cn : voisin) {
				if (cn.color == Color.ORANGE) {
					orange.remove(cn);
				}
				if (cn.color == Color.BLACK) {
					System.out.println("nope dude");
				}
				cn.color = Color.GREY;
			}
			for (ColoredNode cn : voisin2) {
				for (ColoredNode o : whiteNeighbor(cn, coloredPts, edgeThreshold)) {
					o.color = Color.ORANGE;
					orange.add(o);
				}
			}
			boolean keep = false;
			for (ColoredNode p : orange) {
				// que des voisins gris
				if (orangeWhiteNeighbor(p, coloredPts, edgeThreshold).size() > 0) {
					keep = true;
					System.out.println("keep going strong");
					break;
				}
			}
			if (!keep) {
				black.addAll(orange);
				orange.clear();
			}
		}
		return decolorMyPts(black);
	}
	
	public void color(ArrayList<ColoredNode> points, ArrayList<ColoredNode> dominated, Color c) {
		for (ColoredNode cn : points) {
			if (dominated.contains(cn)) {
				cn.color = c;
			}
		}
	}
	public ArrayList<ColoredNode> colorMyPts(ArrayList<Point> points) {
		ArrayList<ColoredNode> res = new ArrayList<>(points.size());
		for (Point p : points) {
			res.add(new ColoredNode(p));
		}
		return res;
	}

	public ArrayList<Point> decolorMyPts(ArrayList<ColoredNode> points) {
		ArrayList<Point> res = new ArrayList<>(points.size());
		for (ColoredNode p : points) {
			res.add(p.p);
		}
		return res;
	}

	public ArrayList<ColoredNode> colorMyPtsAlgoA(ArrayList<Point> MIS, ArrayList<Point> points) {
		ArrayList<ColoredNode> res = new ArrayList<>(points.size());

		for (Point p : points) {
			if (MIS.contains(p)) {
				res.add(new ColoredNode(p, Color.BLACK));
			} else {
				res.add(new ColoredNode(p));
			}
		}
		return res;
	}

	public boolean isMIS(ArrayList<Point> MIS, ArrayList<Point> points, int edgeThreshold) {
		if (!isValid(points, MIS, edgeThreshold)) {
			return false;
		}
		ArrayList<Point> voisins = new ArrayList<Point>();
		Set<Point> deuxVoisins = new HashSet<Point>();
		for (Point p : MIS) {
			voisins = neighbor(p, points, edgeThreshold);

			for (Point v : voisins) {
				if (MIS.contains(v)) {
					return false;
				}
				deuxVoisins.addAll(neighbor(v, points, edgeThreshold));
			}

			boolean ok = false;
			deuxVoisins.remove(p);
			deuxVoisins.removeAll(voisins);

			for (Point v : deuxVoisins) {
				if (MIS.contains(v)) {
					ok = true;
					break;
				}
			}
			if (!ok) {
				System.out.println("boom");
				return false;
			}
			deuxVoisins = new HashSet<Point>();
		}
		return true;
	}

	public boolean isValid(ArrayList<Point> points, ArrayList<Point> sol, int edgeThreshold) {
		// is a dominating set
		if (sol.size() == 0) {
			return false;
		}
		@SuppressWarnings("unchecked")
		ArrayList<Point> result = (ArrayList<Point>) points.clone();

		for (Point p : sol) {
			result.remove(p);
			result.removeAll(neighbor(p, points, edgeThreshold));
		}
		return result.size() == 0;
	}

	public boolean isTree(ArrayList<Point> points, ArrayList<Point> sol, int edgeThreshold) {
		// no cycle and connected
		ArrayList<Point> seen = new ArrayList<>();
		ArrayList<Point> stack = new ArrayList<>();
		@SuppressWarnings("unchecked")
		ArrayList<Point> toSee = (ArrayList<Point>) points.clone();
		Point current;
		stack.add(toSee.get(0));
		seen.add(toSee.get(0));
		toSee.remove(0);
		while (!stack.isEmpty()) {
			current = stack.remove(0);
			for (Point p : neighbor(current, toSee, edgeThreshold)) {
				if (seen.contains(p)) {
					return false; // cycle
				}
				stack.add(p);
			}
			toSee.removeAll(stack);
		}
		return true;
	}

	public ArrayList<Point> neighbor(Point p, ArrayList<Point> vertices, int edgeThreshold) {
		ArrayList<Point> result = new ArrayList<Point>();
		for (Point point : vertices)
			if (point.distance(p) < edgeThreshold && !point.equals(p))
				result.add((Point) point.clone());
		return result;
	}
	public ArrayList<ColoredNode> neighbor(ColoredNode p, ArrayList<ColoredNode> vertices, int edgeThreshold) {
		ArrayList<ColoredNode> result = new ArrayList<ColoredNode>();
		for (ColoredNode point : vertices)
			if (point.p.distance(p.p) < edgeThreshold && !point.equals(p))
				result.add(point);
		return result;
	}

	public ArrayList<ColoredNode> whiteNeighbor(ColoredNode p, ArrayList<ColoredNode> vertices, int edgeThreshold) {
		ArrayList<ColoredNode> result = new ArrayList<ColoredNode>();
		for (ColoredNode point : vertices)
			if (point.p.distance(p.p) < edgeThreshold && !point.equals(p) && point.color == Color.WHITE)
				result.add(point);
		return result;
	}

	public ArrayList<ColoredNode> orangeWhiteNeighbor(ColoredNode p, ArrayList<ColoredNode> vertices,
			int edgeThreshold) {
		ArrayList<ColoredNode> result = new ArrayList<ColoredNode>();
		for (ColoredNode point : vertices)
			if (point.p.distance(p.p) < edgeThreshold && !point.equals(p) && point.color != Color.BLACK
					&& point.color != Color.GREY)
				result.add(point);
		return result;
	}

	public Point findMax(ArrayList<Point> graphe, int edgeThreshold) {
		int max = 0;
		Point res = graphe.get(0);
		for (Point p : graphe) {
			if (neighbor(p, graphe, edgeThreshold).size() > max) {
				max = neighbor(p, graphe, edgeThreshold).size();
				res = p;
			}
		}
		return res;
	}
	public Point findMin(ArrayList<Point> graphe, int edgeThreshold) {
		int min = Integer.MAX_VALUE;
		Point res = graphe.get(0);
		for (Point p : graphe) {
			if (neighbor(p, graphe, edgeThreshold).size() < min) {
				min = neighbor(p, graphe, edgeThreshold).size();
				res = p;
			}
		}
		return res;
	}

	// FILE PRINTER
	private void saveToFile(String filename, ArrayList<Point> result) {
		int index = 0;
		try {
			while (true) {
				BufferedReader input = new BufferedReader(
						new InputStreamReader(new FileInputStream(filename + Integer.toString(index) + ".points")));
				try {
					input.close();
				} catch (IOException e) {
					System.err.println(
							"I/O exception: unable to close " + filename + Integer.toString(index) + ".points");
				}
				index++;
			}
		} catch (FileNotFoundException e) {
			printToFile(filename + Integer.toString(index) + ".points", result);
		}
	}

	private void printToFile(String filename, ArrayList<Point> points) {
		try {
			PrintStream output = new PrintStream(new FileOutputStream(filename));
			int x, y;
			for (Point p : points)
				output.println(Integer.toString((int) p.getX()) + " " + Integer.toString((int) p.getY()));
			output.close();
		} catch (FileNotFoundException e) {
			System.err.println("I/O exception: unable to create " + filename);
		}
	}

	// FILE LOADER
	private ArrayList<Point> readFromFile(String filename) {
		String line;
		String[] coordinates;
		ArrayList<Point> points = new ArrayList<Point>();
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			try {
				while ((line = input.readLine()) != null) {
					coordinates = line.split("\\s+");
					points.add(new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
				}
			} catch (IOException e) {
				System.err.println("Exception: interrupted I/O.");
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("I/O exception: unable to close " + filename);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Input file not found.");
		}
		return points;
	}
}
