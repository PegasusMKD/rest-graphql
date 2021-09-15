package spring.graphql.rest.nonoptimized.timer;

public class Timer {

	public static double timer(TimedAction timedAction) {
		long startTime = System.nanoTime();
		timedAction.execute();
		long endTime = System.nanoTime();
		return (endTime - startTime) / 1000000.;
	}

	public static String roundedTimer(TimedAction timedAction) {
		return String.format("%.2f", timer(timedAction));
	}

}
