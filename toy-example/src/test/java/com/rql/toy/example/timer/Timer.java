package com.rql.toy.example.timer;

public class Timer {

	public double timer(TimedAction timedAction) {
		long startTime = System.nanoTime();
		timedAction.execute();
		long endTime = System.nanoTime();
		return (endTime - startTime) / 1000000.;
	}

	public String roundedTimer(TimedAction timedAction) {
		return String.format("%.2f", timer(timedAction));
	}

}
