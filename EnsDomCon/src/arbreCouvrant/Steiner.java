package arbreCouvrant;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Steiner {
	public Matrices K = null;
	@SuppressWarnings("unchecked")
	public ArrayList<Point> calculSteiner(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
		/////////////// mets des labels sur mes points et hitspoint ////////////////////
		ArrayList<LabelPt> pointsL = labelMyPts(points);
		ArrayList<LabelPt> hitpointsL = new ArrayList<>();
		
		int ind = 0;
		for (Point p : hitPoints) {
			ind = points.indexOf(p);
			hitpointsL.add(new LabelPt(ind, p));
		}
		K = calculShortestPaths2(points, edgeThreshold);
		
		ArrayList<LabelPt> pointsLabelled = (ArrayList<LabelPt>) hitpointsL.clone();
		
		/////////////// 1 er Kruskal pour construire l'abre couvrant des hitpoints//////
		ArrayList<ArrayList<LabelPt>> AllPaires = new ArrayList<ArrayList<LabelPt>>();
		for (LabelPt p : pointsLabelled) {
			for (LabelPt q : pointsLabelled) {
				if (p.getPoint().equals(q.getPoint()))
					continue;

				ArrayList<LabelPt> paire = new ArrayList<LabelPt>();
				paire.add(p);
				paire.add(q);
				if (AllPaires.contains(paire))
					continue;
				AllPaires.add(paire);
			}
		}
		Collections.sort(AllPaires, new Sortbydist2(K.dists));
		ArrayList<ArrayList<LabelPt>> T0 = new ArrayList<ArrayList<LabelPt>>();
		ArrayList<LabelPt> current;
		while (AllPaires.size() != 0) {
			current = AllPaires.remove(0);
			if (current.get(0).getLabel() != current.get(1).getLabel()) {
				T0.add(current);
				reLabelMyPts(AllPaires, T0.get(T0.size() - 1).get(0).getLabel(),
						T0.get(T0.size() - 1).get(1).getLabel());
			}
		}
		///////////////////////////////////////////////////////////////////////////
		////////////////////// Repercute sur G l'arbre couvrant des hitspoints ////
		ArrayList<Integer> ch = new ArrayList<>();
		ArrayList<LabelPt> resres = new ArrayList<>();
		ArrayList<Point> pts = new ArrayList<>();
		ArrayList<ArrayList<LabelPt>> paires = new ArrayList<ArrayList<LabelPt>>();
		for (ArrayList<LabelPt> e : T0) {
			ch = chemin(e, K.paths);
			for (Integer l : ch) {
				resres.add(pointsL.get(l));
			}
			ArrayList<LabelPt> paire = new ArrayList<LabelPt>();
			for(int i = 0;i<ch.size()-1;i++) {
				paire.add(pointsL.get(ch.get(i)));
				paire.add(pointsL.get(ch.get((i+1))));
				paires.add(paire);
				paire = new ArrayList<LabelPt>();

			}
			for (int i = 0; i < resres.size(); i++) {
				if (pts.contains(resres.get(i).getPoint())) {
					continue;
				}
				pts.add(resres.get(i).getPoint());
			}
			resres = new ArrayList<>();
		}
		////////////////////////////////////////////////////////////////////////////
		//////////////////// 2 eme Kruskal mais sur la liste de points qui forme les arete de l'arbre final -> Solution "un peu" ameliorer	
		return Kruskal(pts);
		
	}
	
	public double score(ArrayList<ArrayList<LabelPt>> paires){
		if(paires.size() == 0) {
			return 0.0;
		}
		double s = 0;
		double dist = 0;
		for(ArrayList<LabelPt> paire : paires){
			dist = paire.get(0).getPoint().distance(paire.get(1).getPoint());
			s = s + dist;
		}
		return s;
	}
	
	public boolean isBoutChaine( ArrayList<LabelPt> lpt, ArrayList<ArrayList<LabelPt>> paires){
		
		Point P = lpt.get(0).getPoint();
		Point Q = lpt.get(1).getPoint();
		
		int nbP = 0;
		int nbQ = 0;
		
		for(ArrayList<LabelPt> paire : paires){
			if(paire.get(0).getPoint().equals(P) || paire.get(1).getPoint().equals(P)) {
				nbP ++;
			}
			if(paire.get(0).getPoint().equals(Q) || paire.get(1).getPoint().equals(Q)) {
				nbQ++;
			}
		}
		//si il apparais que une fois il est forcement en bout
		if(nbP == 1 || nbQ == 1) {
			return true;
		}
		return false;
	}



	public ArrayList<Integer> chemin(ArrayList<LabelPt> uv, int[][] paths) {
		LabelPt u = uv.get(0);
		LabelPt v = uv.get(1);
		ArrayList<Integer> res = new ArrayList<Integer>();
		res.add(u.originLabel);
		int next = paths[u.originLabel][v.originLabel];
		while (next != v.originLabel) {
//			if(next == -1) {
//				return res;
//			}
			res.add(next);
			next = paths[next][v.originLabel];
		}
		res.add(next);
		return res;
	}

	public Matrices calculShortestPaths2(ArrayList<Point> points, int edgeThreshold) {
		int[][] paths = new int[points.size()][points.size()];
		double[][] dist = new double[points.size()][points.size()];

		for (int i = 0; i < points.size(); i++) {
			for (int j = 0; j < points.size(); j++) {
				if ((points.get(i).distance(points.get(j))) < edgeThreshold) {
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
		return new Matrices(dist, paths);
	}

	@SuppressWarnings("null")
	public ArrayList<Point> Kruskal(ArrayList<Point> points) {

		ArrayList<LabelPt> pointsLabelled = labelMyPts(points);
		ArrayList<ArrayList<LabelPt>> AllPaires = new ArrayList<ArrayList<LabelPt>>();
		for (LabelPt p : pointsLabelled) {
			for (LabelPt q : pointsLabelled) {
				if (p.getPoint().equals(q.getPoint())) {
					continue;
				}else {
					ArrayList<LabelPt> paire = new ArrayList<LabelPt>();
					paire.add(p);
					paire.add(q);
					if (AllPaires.contains(paire)) {
						continue;
					}
					else {
						AllPaires.add(paire);
					}
				}
			}
		}
		
		Collections.sort(AllPaires, new Sortbydist2(K.dists));
		ArrayList<ArrayList<LabelPt>> res = new ArrayList<ArrayList<LabelPt>>();
		ArrayList<LabelPt> current;
		
		while (AllPaires.size() != 0) {
			current = AllPaires.remove(0);
			if (current.get(0).getLabel() != current.get(1).getLabel()) {
				res.add(current);
				reLabelMyPts(AllPaires, res.get(res.size() - 1).get(0).getLabel(),
						res.get(res.size() - 1).get(1).getLabel());
			}
		}
		Set<Point> trueRes = new HashSet<>();
		
		for(ArrayList<LabelPt> p : res) {
			trueRes.add(p.get(0).getPoint());
			trueRes.add(p.get(1).getPoint());
		}
		
		ArrayList<Point> trueTrueRes = new ArrayList<>();
		for(Point p : trueRes) {
			trueTrueRes.add(p);
		}
//		return edgeListToTree(res.get(0).get(0), res);
		return trueTrueRes;
	}

	public ArrayList<LabelPt> labelMyPts(ArrayList<Point> pts) {
		
		//// mets des labels sur une collection de points
		ArrayList<LabelPt> lpt = new ArrayList<LabelPt>();
		int i = 0;
		for (Point p : pts) {
			lpt.add(new LabelPt(i, p));
			i++;
		}
		return lpt;
	}

	public void reLabelMyPts(ArrayList<ArrayList<LabelPt>> pts, int x, int y) {
		for (ArrayList<LabelPt> paire : pts) {
			if (paire.get(0).getLabel() == x) {
				paire.get(0).setLabel(y);
			}
			if (paire.get(1).getLabel() == x) {
				paire.get(1).setLabel(y);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Tree2D edgeListToTree(LabelPt r, ArrayList<ArrayList<LabelPt>> edges) {

		ArrayList<ArrayList<LabelPt>> remainder = new ArrayList<>();
		ArrayList<LabelPt> subTreeRoots = new ArrayList<>();
		ArrayList<LabelPt> current;

		while (edges.size() != 0) {
			current = edges.remove(0);
			if (current.get(0).getPoint().equals(r.getPoint())) {
				subTreeRoots.add(current.get(1));
			} else {
				if (current.get(1).getPoint().equals(r.getPoint())) {
					subTreeRoots.add(current.get(0));
				} else {
					remainder.add(current);
				}
			}
		}
		ArrayList<Tree2D> subTrees = new ArrayList<Tree2D>();
		for (LabelPt subTreeRoot : subTreeRoots)
			subTrees.add(edgeListToTree(subTreeRoot, (ArrayList<ArrayList<LabelPt>>) remainder.clone()));

		return new Tree2D(r.getPoint(), subTrees);
	}

	public class Matrices {

		public double[][] dists;
		public int[][] paths;

		public Matrices(double[][] d, int[][] pa) {

			this.dists = d;
			this.paths = pa;
		}

	}

	public class LabelPt {
		private int label;
		public int originLabel;
		private Point pt;

		public LabelPt(int i, Point p) {
			label = i;
			originLabel = label;
			pt = p;
		}

		public int getLabel() {
			return label;
		}

		public void setLabel(int i) {
			label = i;
		}

		public Point getPoint() {
			return pt;
		}

		public void setPoint(Point p) {
			pt = p;
		}
	}

	class Sortbydist implements Comparator<ArrayList<LabelPt>> {
		public int compare(ArrayList<LabelPt> o1, ArrayList<LabelPt> o2) {
			return (int) ( (o1.get(0).getPoint().distance(o1.get(1).getPoint()))
					- (o2.get(0).getPoint().distance(o2.get(1).getPoint())));
		}
	}

	class Sortbydist2 implements Comparator<ArrayList<LabelPt>> {
		double[][] dist;

		public Sortbydist2(double[][] d) {
			this.dist = d;
		}

		public int compare(ArrayList<LabelPt> o1, ArrayList<LabelPt> o2) {
			int i = o1.get(0).originLabel;
			int j = o1.get(1).originLabel;
			int u = o2.get(0).originLabel;
			int v = o2.get(1).originLabel;
			double diff = (dist[i][j]) - (dist[u][v]);
			if (diff < 0) {
				return -1;
			}
			if (diff > 0) {
				return 1;
			}
			return 0;
		}
	}
}
