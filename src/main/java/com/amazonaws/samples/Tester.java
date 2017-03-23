package com.amazonaws.samples;

public class Tester {
	public static void main(String[] args){
		QuadTreeGenerator generator = new QuadTreeGenerator();
		generator.init();
		generator.checkForExit();
		generator.createTable();
		generator.createTree(0, 0, generator.xAxis, generator.yAxis);
		System.out.println("Done");
	}
}