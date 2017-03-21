package com.shawnkoon.quadtree;

public class QuadTreeGenerator {
	public static long xAxis;
	public static long yAxis;
	public static long nodeNumber;
	
	public static void init() {
		xAxis = 4;
		yAxis = 4;
		nodeNumber = 0;
	}
	
	public static void checkForExit() {
		if (xAxis > 0 && yAxis > 0) {
			if ((xAxis & (xAxis - 1)) == 0) {
				if((yAxis & (yAxis - 1)) != 0) {
					System.out.println(" Y-axis needs to be power of 2.");
					System.exit(-1);
				}
			}
			else {
				System.out.println(" X-axis needs to be power of 2.");
				System.exit(-1);
			}			
		}
		else {
			System.out.println(" X-Axis and Y-Axis needs to be positive Number.");
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		init();
		createTree(0,0,xAxis,yAxis);
		System.out.println("Done");
	}
	
	/**
	 * Create QuadTree using recursive Method.
	 * 
	 * @param xMin: Minimum x of the box.
	 * @param yMin:	Minimum y of the box.
	 * @param xMax: Maximum x of the box.
	 * @param yMax: Maximum y of the box.
	 * 
	 */
	public static void createTree(long xMin, long yMin, long xMax, long yMax) {
		
		// Base case.
		if((xMax - xMin) == 1 || (yMax - yMin) == 1) {
			
		}
		// Recursive case.
		else {
			System.out.println("Node : " + nodeNumber + " (" + xMin + "," + yMin + "," + xMax + "," + yMax + ")");
			nodeNumber++;
			
			// Left up
			createTree(xMin, (yMax - yMin)/2 + yMin, (xMax - xMin)/2  + xMin, yMax);
			// Right up
			createTree((xMax - xMin)/2 + xMin, (yMax - yMin)/2 + yMin, xMax, yMax);
			// Left down
			createTree(xMin, yMin, (xMax - xMin)/2 + xMin, (yMax - yMin)/2 + yMin);
			// Right down
			createTree((xMax - xMin)/2 + xMin, yMin, xMax, (yMax - yMin)/2 + yMin);
		}
		
	}
}