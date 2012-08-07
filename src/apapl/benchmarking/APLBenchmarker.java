package apapl.benchmarking;

import java.util.HashMap;

import apapl.APLMAS;
import apapl.APLModule;
import apapl.beliefinertia.BeliefInertiaParam;

/**
 *     
 * The APLBenchmarker is a generic benchmarking tool for 2APL.
 * -------------------------------------------------------------------------
 * 
 * It comes with six operations that are being benchmarked, but this can be extended very easily by
 * adding them to the file apapl.benchmarking.APLBenchmarkParam. See that file for more information.
 * 
 * The benchmarker will count the number of times that this operation has been performed, the total 
 * execution time of this operation and the average execution time for one execution, and print the
 * results in a neat table.
 * 
 * The benchmarker should be called using the -benchmark commandline option, because this will hand
 * over the execution of the MAS to the benchmarker and disable both the GUI and Jade.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class APLBenchmarker
{	
	// this maps every APLModule with a private class Benchmark that contains a mapping
	// from every operation to the results
	private static HashMap<String, Benchmark> benchmarks;
	
	// collector module that is used to show general information
	private static Benchmark collector;
	
	// NOTE!! do not change this flag!
	// rather, call the benchmarker from the command-line using the -benchmark flag
	private static boolean benchmark = false;
	
	/**
	 * Start the benchmark and stop the execution of all modules after 
	 * APLBenchmarkParam.BENCHMARK_TIME_SEC seconds and print the information of each APLModule
	 * 
	 * @param m The executor that contains all APLModule (see apapl.MultiThreadedExecutor)
	 */
	public void start(APLMAS mas) 
	{
		benchmark = true;
		benchmarks = new HashMap<String, Benchmark>();
		
		// initialize a benchmark for each APLModule
		for (APLModule m : mas.getModules())
			benchmarks.put(m.getName(), new Benchmark());
		
		// add the unknown module as well
		benchmarks.put(APLBenchmarkParam.UNKNOWN_MODULE, new Benchmark());
		
		// and if we are only collecting general information, add a collector module
		if (!APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK) collector = new Benchmark();
		
		System.out.println("\n=== starting benchmark");
		System.out.println("duration: " + APLBenchmarkParam.BENCHMARK_TIME_SEC + "s");
		System.out.println("print seperate results for agents: " + 
					(APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK ? "yes" : "no"));
		System.out.println("beliefinertia: " +
				(BeliefInertiaParam.ENABLED ? "enabled" : "disabled"));
				
		IOStream.setStreams();
		
		Runnable r = new MASExecutor(mas);
		Thread t = new Thread(r);
		t.start();
	}
	
	public static void startTiming(String op) { startTiming(APLBenchmarkParam.UNKNOWN_MODULE, op); }
	
	public static void stopTiming(String op) { stopTiming(APLBenchmarkParam.UNKNOWN_MODULE, op); }
	
	public static void startTiming(APLModule m, String op) { startTiming(m.getName(), op); }
	
	public synchronized static void startTiming(String modulename, String op)
	{
		if (benchmark && benchmarks.keySet().contains(modulename))
			benchmarks.get(modulename).start(op);
	}
	
	public static void stopTiming(APLModule m, String op) { stopTiming(m.getName(), op); }
	
	public synchronized static void stopTiming(String modulename, String op)
	{
		if (benchmark && benchmarks.keySet().contains(modulename))
			benchmarks.get(modulename).stop(op);
	}
	
	public static void printResults()
	{
		int colWidth[] = { 23, 23, 13, 23, 16 };
		String headers[] = { "module name", "operation", "calls", "total proc. time", "avg. proc. time" };
		
		String str = alignText(headers, colWidth);	
		
		str += separator(colWidth);
		
		if (APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK)
			for (String modulename : benchmarks.keySet())
				str += formatModuleBenchmark(modulename, colWidth);		
		else
		{
			// put all information in the general module
			for (String m : benchmarks.keySet())
			{
				for (String op : APLBenchmarkParam.OP_ARRAY)
					collector.addResults(op, benchmarks.get(m).getResults(op));
			}
			
			// and add it to the string
			str += formatModuleBenchmark(APLBenchmarkParam.COLLECTOR_MODULE, colWidth);
			
		}

		IOStream.out.print(str);
		
		// end the execution
		System.exit(0);
	}
	
	public static String formatModuleBenchmark(String modulename, int[] colWidth)
	{
		String str = "";
		
		boolean first = true;
		boolean used = false;
		
		// show the operations in the order in which they are entered in the array
		for (String op : APLBenchmarkParam.OP_ARRAY)
		{
			TimeResult t = (modulename.equals(APLBenchmarkParam.COLLECTOR_MODULE) ?
					collector.getResults(op)
					: benchmarks.get(modulename).getResults(op));
			
			used = t.used() | used;
			
			String moduleTxt = (first?modulename:"");
			String calls     = Integer.toString(t.calls);
			String procTime  = Long.toString(t.totalTime);
			String avgTime   = Double.toString(t.calls == 0?0:((double)t.totalTime/(double)t.calls));
			
			String text[] = { moduleTxt, op, calls, procTime, avgTime };
			
			String add = alignText(text, colWidth);
			
			// TODO: make this nicer using the regular expression "\\d+\\.\\d+" in some way
			if (APLBenchmarkParam.DECIMAL_AS_COMMA)
				add = add.replaceAll("\\.", ",");
			
			str += add;
			
			first = false;
		}
		str += separator(colWidth);
		
		return (!used && !APLBenchmarkParam.PRINT_UNUSED_AGENTS ? "" : str);
	}
	
	public static String alignText(String[] columns, int[] widths)
	{
		// do not print the first column if we only show the general information
		String ret = (APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK ? fill(columns[0], widths[0]) : "");
		
		for (int i=1; i<widths.length; i++)
			ret += (i==1?mid():"| ") + fill(columns[i], widths[i]-(i==1?space():1));
		
		return ret + "\n";
	}
	
	public static String mid() { return (APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK ? "| " : ""); }
	public static int space()  { return (APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK ? 1 : 0); }
	
	public static String fill(String str, int width)
	{
		return fill(str, width, ' ');
	}
	
	public static String fill(String str, int width, char c)
	{
		String ret = str;
		for (int i=0; i<(width-str.length());i++) ret += c;
		
		return ret;
	}
	
	public static String separator(int[] colWidth)
	{
		String str = "";
		
		for (int i=(APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK?0:1); i<colWidth.length; i++)
			str += fill("", colWidth[i], '-') + "|";
		
		str = str.substring(0, str.length()-1) + "\n";
		
		return str;
	}
	
	/**
	 * This class represents a benchmark for all operations.
	 * 
	 * @author Marc van Zee (marcvanzee@gmail.com) - Linköping University
	 *
	 */
	private class Benchmark
	{
		// this maps every operation with a private class TimeResult that contains all necessary
		// information to do benchmarking
		HashMap<String, TimeResult> results;
		
		public Benchmark()
		{
			results = new HashMap<String, TimeResult>();
			
			// initialize the results
			for (String op : APLBenchmarkParam.OP_ARRAY)
				results.put(op, new TimeResult());
		}
		
		void start(String op)
		{
			if (results.containsKey(op))
				results.get(op).start();
		}
		
		void stop(String op)
		{
			if (results.containsKey(op))
				results.get(op).stop();
		}
		
		TimeResult getResults(String op)
		{
			return results.get(op);
		}
		
		void addResults(String op, TimeResult t)
		{
			results.get(op).addResults(t);
		}
	}
	
	/**
	 * This class stores all the benchmark results for an operation, and it is also used to keep track
	 * of the starting time of the benchmark for this operation
	 */
	private class TimeResult
	{
		int calls = 0;
		long startTime = 0;
		long totalTime = 0;
		
		public TimeResult() {}
		
		public void start()
		{
			startTime = System.currentTimeMillis();
		}
		
		public void stop()
		{
			totalTime += (startTime > 0) ? (System.currentTimeMillis() - startTime) : 0;
			calls++;
			startTime = 0;
		}
		
		public boolean used()
		{
			return calls > 0;
		}
		
		public void addResults(TimeResult t)
		{
			calls += t.calls;
			totalTime += t.totalTime;
		}
	}
}
