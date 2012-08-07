package apapl.benchmarking;

import apapl.APLMAS;

/**
 * This class will start the multi-agent system. wait for the the specified number of seconds, then terminate the
 * it and request the benchmarker to print the results.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class MASExecutor implements Runnable 
{
	APLMAS mas = null;
	
	public MASExecutor(APLMAS mas) 
	{
		this.mas = mas;
	}
	
	public void run() 
	{
		mas.start();
		long benchmarkTime = APLBenchmarkParam.BENCHMARK_TIME_SEC * 1000;
		long time = System.currentTimeMillis();
		
		int i = 0;
		IOStream.out.println("    __________");
		IOStream.out.print("0% |");
		
		while (i<10)
		{
			i++;
			try {
				// recalculate the sleep time to make the benchmarking more accurate
				// put it in one line to make calculations go faster, but this is what happens
				// long elapsedTime = System.currentTimeMillis() - time;
				// long timeToGo = benchmarkTime - elapsedTime;
				// long sleep = timeToGo / (11-i);
				// Thread.sleep(sleep);
				
				Thread.sleep((benchmarkTime - System.currentTimeMillis() + time) / (11-i));
				
				IOStream.out.print((APLBenchmarkParam.DIRTY_LOADER ? (i==8? "|" : "_") : ">"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		mas.stop();
		
		IOStream.out.println( (APLBenchmarkParam.DIRTY_LOADER ? ")- --\n   |_)_)" : "| 100%"));
		
		IOStream.out.println("\n=== benchmarking done, printing results.\n");
		APLBenchmarker.printResults();
		
		mas.takeDown();
	}
}