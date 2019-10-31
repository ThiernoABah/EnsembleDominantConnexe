package algorithms;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import arbreCouvrant.Steiner;

public class DefaultTeam {
	public ArrayList<Point> calculConnectedDominatingSet(ArrayList<Point> points, int edgeThreshold) {
		@SuppressWarnings("unchecked")
		ArrayList<Point> clone = (ArrayList<Point>) points.clone();

		
		
		System.out.println("Graph with " + clone.size() + " nodes");
		
//		Instant s = Instant.now();
//		ArrayList<Point> result = MIS(clone, edgeThreshold);
//		Instant f = Instant.now();

		Instant s = Instant.now();
		ArrayList<Point> result = gloutonNaif(clone, edgeThreshold);
		Instant f = Instant.now();

		System.out.println(Duration.between(s, f).toMillis() + " ms to construct the MIS");
		System.out.println("MIS is stable ? -> " + isMIS(result, points, edgeThreshold));
		System.out.println("MIS size : " + result.size());

		s = Instant.now();
		result = algoA(result, clone, edgeThreshold);
		f = Instant.now();
		System.out.println(Duration.between(s, f).toMillis() + " ms to compute algoA");

		// s = Instant.now();
		// result = calculSteiner(clone, result, edgeThreshold);
		// f = Instant.now();
		// System.out.println(Duration.between(s,f)+" ms to compute Kruskal");

		System.out.println("is a MIS ? -> " + isValid(points, result, edgeThreshold));
		System.out.println("is connected ? -> " + isConnected(result, edgeThreshold));
		System.out.println("Connected MIS size : " + result.size());

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

		ArrayList<ColoredNode> voisin = new ArrayList<>();
		ArrayList<ColoredNode> tmpV = new ArrayList<>();
		int max = 0;
		ColoredNode start = coloredPts.get(0);
		for (ColoredNode p : coloredPts) {
			tmpV = whiteNeighbor(p, coloredPts, edgeThreshold);
			if (tmpV.size() > max) {
				voisin = tmpV;
				max = voisin.size();
				start = p;
			}
		}
		
		start.color = Color.BLACK;
		black.add(start);
		for (ColoredNode cn : voisin) {
			cn.color = Color.GREY;
		}
		boolean white = true;
		while (white) {
			voisin = new ArrayList<>();
			for (ColoredNode p : coloredPts) {
				boolean greyNeig = false;
				if (p.color == Color.WHITE) {
					for (ColoredNode vp : neighbor(p, coloredPts, edgeThreshold)) {
						if (vp.color == Color.GREY) {
							greyNeig = true;
							break;
						}
					}
					if (greyNeig) {
						start = p;
						start.color = Color.BLACK;
						black.add(start);
						for (ColoredNode cn : whiteNeighbor(p, coloredPts, edgeThreshold)) {
							cn.color = Color.GREY;
						}
					}
				}
			}
			white = false;
			for (ColoredNode p : coloredPts) {
				if (p.color == Color.WHITE) {
					white = true;
					break;
				}
			}
		}

		return decolorMyPts(black);
	}

	public ArrayList<Point> algoA(ArrayList<Point> MIS, ArrayList<Point> points, int edgeThreshold) {
		ArrayList<ColoredNode> coloredPts = colorMyPtsAlgoA(MIS, points);
		ArrayList<ColoredNode> grey = new ArrayList<>();
		ArrayList<ColoredNode> blackN = new ArrayList<>();
		ArrayList<Point> res = new ArrayList<>();

		ArrayList<ColoredNode> voisin = new ArrayList<>();
		for (ColoredNode cp : coloredPts) {
			if (cp.color == Color.GREY) {
				grey.add(cp);
			}
		}

		boolean add = true;
		for (int i = 5; i > 1; i--) {
			for (int g = 0; g < grey.size(); g++) {
				voisin = neighbor(grey.get(g), coloredPts, edgeThreshold);
				if (voisin.size() < i) {
					continue;
				}
				blackN = new ArrayList<>();
				for (ColoredNode v : voisin) {
					if (v.color == Color.BLACK) {
						if (v.composant == 0) {
							// appartient a aucun comp
							blackN.add(v);
						} else {
							add = true;
							for (ColoredNode r : blackN) {
								if (r.composant == v.composant && !r.equals(v)) {
									add = false;
									break;
								}
							}
							if (add) {
								blackN.add(v);
							}
						}
					}
				}
				if (blackN.size() < i) {
					continue;
				}
				boolean noComp = true;
				for (int j = 0; j < i; j++) {
					if (blackN.get(j).composant != 0) {
						noComp = false;
						break;
					}
				}
				if (noComp) {
					grey.get(g).color = Color.BLUE;
					ColoredNode.id++;
					grey.get(g).composant = ColoredNode.id;
					for (int j = 0; j < i; j++) {
						blackN.get(j).composant = grey.get(g).composant;
					}

				} else {
					grey.get(g).color = Color.BLUE;
					ArrayList<Integer> composant = new ArrayList<>();
					for (int j = 0; j < i; j++) {
						if (blackN.get(j).composant != 0) {
							composant.add(blackN.get(j).composant);
						}
					}

					grey.get(g).composant = composant.get(0);
					for (ColoredNode c : coloredPts) {
						if (composant.contains(c.composant)) {
							c.composant = composant.get(0);
						}
					}
					for (int z = 0; z < i; z++) {
						blackN.get(z).composant = composant.get(0);
					}
				}
				res.add(grey.get(g).p);
				grey.remove(g);
				g--;
			}
		}
		res.addAll(MIS);
		return res;

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
				res.add(new ColoredNode(p, Color.GREY));
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

		for (int i = 0; i < MIS.size(); i++) {
			for (int j = 0; j < MIS.size(); j++) {
				if (i == j) {
					continue;
				}
				if (MIS.get(i).distance(MIS.get(j)) <= edgeThreshold) {
					return false;
				}
			}
		}
		for (Point p : MIS) {
			voisins = neighbor(p, points, edgeThreshold);
			for (Point v : voisins) {
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
			for (Point pp : points) {
				if (pp.distance(p) <= edgeThreshold) {
					result.remove(pp);
				}
			}
		}
		return result.size() == 0;
	}

	public boolean isConnected(ArrayList<Point> points, int edgeThreshold) {
		int[][] paths = new int[points.size()][points.size()];
		double[][] dist = new double[points.size()][points.size()];

		for (int i = 0; i < points.size(); i++) {
			for (int j = 0; j < points.size(); j++) {
				if ((points.get(i).distance(points.get(j))) <= edgeThreshold) {
					paths[i][j] = j;
					dist[i][j] = (points.get(i).distance(points.get(j)));
				} else {
					paths[i][j] = -1;
					dist[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		double d = 0.0;
		for (int k = 0; k < points.size(); k++) {
			for (int i = 0; i < paths.length; i++) {
				for (int j = 0; j < paths.length; j++) {
					if ((paths[i][k] != -1) && (paths[k][j] != -1)) {
						d = dist[i][k] + dist[k][j];
						if (d < dist[i][j]) {
							dist[i][j] = d;
							paths[i][j] = paths[i][k];
						}
					}
				}
			}
		}

		for (int i = 0; i < paths.length; i++) {
			for (int j = 0; j < paths.length; j++) {
				if (paths[i][j] == -1) {
					return false;
				}
			}
		}
		return true;
	}

	public ArrayList<Point> neighbor(Point p, ArrayList<Point> vertices, int edgeThreshold) {
		ArrayList<Point> result = new ArrayList<Point>();
		for (Point point : vertices)
			if (point.distance(p) <= edgeThreshold && !point.equals(p))
				result.add((Point) point.clone());
		return result;
	}

	public ArrayList<ColoredNode> neighbor(ColoredNode p, ArrayList<ColoredNode> vertices, int edgeThreshold) {
		ArrayList<ColoredNode> result = new ArrayList<ColoredNode>();
		for (ColoredNode point : vertices)
			if (point.p.distance(p.p) <= edgeThreshold && !point.equals(p))
				result.add(point);
		return result;
	}

	public ArrayList<ColoredNode> whiteNeighbor(ColoredNode p, ArrayList<ColoredNode> vertices, int edgeThreshold) {
		ArrayList<ColoredNode> result = new ArrayList<ColoredNode>();
		for (ColoredNode point : vertices)
			if (point.p.distance(p.p) <= edgeThreshold && !point.equals(p) && point.color == Color.WHITE)
				result.add(point);
		return result;
	}

	public ArrayList<ColoredNode> orangeWhiteNeighbor(ColoredNode p, ArrayList<ColoredNode> vertices,
			int edgeThreshold) {
		ArrayList<ColoredNode> result = new ArrayList<ColoredNode>();
		for (ColoredNode point : vertices)
			if (point.p.distance(p.p) <= edgeThreshold && !point.equals(p) && point.color != Color.BLACK
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
	@SuppressWarnings("unused")
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

	// FILE LOADER
	@SuppressWarnings("unused")
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
	
	private void printToFile(String filename, ArrayList<Point> points) {
		try {
			PrintStream output = new PrintStream(new FileOutputStream(filename));
			for (Point p : points)
				output.println(Integer.toString((int) p.getX()) + " " + Integer.toString((int) p.getY()));
			output.close();
		} catch (FileNotFoundException e) {
			System.err.println("I/O exception: unable to create " + filename);
		}
	}
}
