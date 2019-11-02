package main;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import algorithms.DefaultTeam;

public class Main {

	public static void main(String[] args) throws IOException {
		ArrayList<Point> points = new ArrayList<Point>();
		
		BufferedReader br = new BufferedReader(new FileReader(args[2]));
		try {
		    String line = "";
		    String[] split;
		    while (line != null) {
		        line = br.readLine();
		        if(line == null) {
		        	break;
		        }
		        split = line.split(" ");
		        points.add(new Point(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    br.close();
		}
		DefaultTeam main = new DefaultTeam();
		
		if(args[0].equals("Li")) {
			int edgeThreshold = Integer.parseInt(args[1]);
			System.out.println("Graph with " + points.size() + " nodes");
			
			Instant s = Instant.now();
			ArrayList<Point> result = main.MIS(points, edgeThreshold);
			Instant f = Instant.now();

			System.out.println(Duration.between(s, f).toMillis() + " ms to construct the MIS");
			System.out.println("MIS is stable ? -> " + main.isMIS(result, points, edgeThreshold));
			System.out.println("MIS size : " + result.size());

			s = Instant.now();
			result = main.algoA(result, points, edgeThreshold);
			f = Instant.now();
			System.out.println(Duration.between(s, f).toMillis() + " ms to compute algoA");

			System.out.println("is a dominating set ? -> " + main.isValid(points, result, edgeThreshold));
			System.out.println("is connected ? -> " + main.isConnected(result, edgeThreshold));
			System.out.println("CDS size : " + result.size());
		}
		else {
			if(args[0].equals("Naif")) {
				int edgeThreshold = Integer.parseInt(args[1]);
				System.out.println("Graph with " + points.size() + " nodes");
				

				Instant s = Instant.now();
				ArrayList<Point> result = main.gloutonNaif(points, edgeThreshold);
				Instant f = Instant.now();

				System.out.println(Duration.between(s, f).toMillis() + " ms to construct the Dominating set");
				System.out.println("MIS is stable ? -> " + main.isMIS(result, points, edgeThreshold));
				System.out.println("MIS size : " + result.size());

				s = Instant.now();
				result = main.algoA(result, points, edgeThreshold);
				f = Instant.now();
				System.out.println(Duration.between(s, f).toMillis() + " ms to compute algoA");

				System.out.println("is a dominating set ? -> " + main.isValid(points, result, edgeThreshold));
				System.out.println("is connected ? -> " + main.isConnected(result, edgeThreshold));
				System.out.println("CDS size : " + result.size());
			}
			else {
				System.out.println("hein");
			}
		}
		

	}

}
