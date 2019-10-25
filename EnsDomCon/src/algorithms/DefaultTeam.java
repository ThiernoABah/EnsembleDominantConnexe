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
		 result = calculSteiner(clone,result, edgeThreshold);
//		result = algoA(result, clone, edgeThreshold);
		System.out.println(isMIS(result, points, edgeThreshold));

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
		ArrayList<ColoredNode> grey = new ArrayList<>();
		ArrayList<ColoredNode> voisin = new ArrayList<>();

		int max = 0;
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
			grey.add(cn);
		}
		boolean white = true;
		while (white) {
			voisin = new ArrayList<>();
			for (ColoredNode p : coloredPts) {
				boolean blackNeig = false;
				boolean greyNeig = false;
				if (p.color == Color.WHITE) {
					for (ColoredNode vp : neighbor(p, coloredPts, edgeThreshold)) {
						if (vp.color == Color.BLACK) {
							blackNeig = true;
							break;
						}
					}
					for (ColoredNode vp : neighbor(p, coloredPts, edgeThreshold)) {
						if (vp.color == Color.GREY) {
							greyNeig = true;
							break;
						}
					}
					if (greyNeig && !blackNeig) {
						start = p;
						start.color = Color.BLACK;
						black.add(start);
						for (ColoredNode cn : whiteNeighbor(p, coloredPts, edgeThreshold)) {
							cn.color = Color.GREY;
							grey.add(cn);
						}
					}
				}
			}
			white = false;
			for (ColoredNode p : coloredPts) {
				if (p.color == Color.WHITE) {
					white = true;
				}
			}
		}

		return decolorMyPts(black);
	}

	public ArrayList<Point> algoA(ArrayList<Point> MIS, ArrayList<Point> points, int edgeThreshold) {
		ArrayList<ColoredNode> coloredPts = colorMyPtsAlgoA(MIS, points);
		ArrayList<ColoredNode> grey = new ArrayList<>();
		ArrayList<ColoredNode> blackN = new ArrayList<>();
		ArrayList<ColoredNode> voisin = new ArrayList<>();
		ArrayList<Point> res = new ArrayList<>();
		for (ColoredNode cp : coloredPts) {
			if (cp.color == Color.BLACK) {
				for (ColoredNode v : neighbor(cp, coloredPts, edgeThreshold)) {
					if (v.color == Color.GREY) {
						continue;
					}
					v.color = Color.GREY;
					grey.add(v);
				}
			}
		}
		
		boolean add = true;
		ColoredNode pt = coloredPts.get(0);
		for (int i = 5; i > 1; i--) {
			for (int g = 0 ;g<grey.size();g++) {
				voisin = neighbor(grey.get(g), coloredPts, edgeThreshold);
				if (voisin.size() >= i) {
					blackN = new ArrayList<>();
					
					for (ColoredNode v : voisin) {
						if (v.color == Color.BLACK) {
							add = true;
							for (ColoredNode r : blackN) {
								if (r.idCompenent == v.idCompenent && r.idCompenent != -1) {
									
									add = false;
									break;
								}
							}
							if (add) {
								blackN.add(v);
							}
						}
					}
					if (blackN.size() >= i) {
						int id = -1;
						for (int j = 0; j < i; j++) {
							if (blackN.get(j).idCompenent > id) {
								id = blackN.get(j).idCompenent;
							}
						}
						if (id == -1) {
							id = ColoredNode.id;
							ColoredNode.id++;
						}
						grey.get(g).idCompenent = id;
						grey.get(g).color = Color.BLUE;
						for (int j = 0; j < i; j++) {
							blackN.get(j).idCompenent = id;
						}
						res.add(grey.get(g).p);
						grey.remove(g);
						g--;
					}

				}
			}
		}
		res.addAll(MIS);
		return res;
	}

	public ColoredNode greyNodeWithIBlack(ArrayList<ColoredNode> points, int edgeThreshold, int i) {
		ArrayList<ColoredNode> res = new ArrayList<>();
		int a = 0;
		for (ColoredNode cn : points) {
			if (cn.color == Color.GREY) {
				a = 0;
				res = new ArrayList<>();

				for (ColoredNode cnn : neighbor(cn, points, edgeThreshold)) {
					if (cnn.color == Color.BLACK) {
						boolean add = true;
						for (ColoredNode r : res) {
							if (r.idCompenent == cnn.idCompenent && r.idCompenent != -1) {
								add = false;
								break;
							}
						}
						if (add) {
							int nid = -1;
							res.add(cnn);
							a++;

							if (a == i) {
								for (ColoredNode r : res) {
									if (r.idCompenent != -1) {
										nid = r.idCompenent;
										break;
									}
								}
								if (nid == -1) {
									nid = ColoredNode.id;
									ColoredNode.id++;
								}
								cn.idCompenent = nid;
								cn.color = Color.BLUE;
								for (ColoredNode r : res) {
									r.idCompenent = nid;
								}
								return cn;
							}
						}
					}
				}
			}
		}
		return null;

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
