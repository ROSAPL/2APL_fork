package apapl.benchmarking;

/**
 * To add an operation to the benchmarker, do the following:
 * 
 * 1. Suppose you want to benchmark some operation "OperationX". Add the field OPERATIONX
 *    to this file with some description that you prefer (this will be displayed in the
 *    benchmarking result):
 *    
 *    public static String OPERATIONX = "Operation X";
 * 
 * 2. Then add this String to the array of operations called "OP_ARRAY".
 * 
 *    private static String opArray[] = { ...., OPERATIONX };
 *    
 * 3. Now you have to tell the benchmarker when to benchmark. Just before the operation you are
 *    interested in, add APLBenchmarker.startTiming(APLBenchmarker.OPERATIONX) and just after,
 *    add APLBenchmarker.stopTiming(APLBenchmarker.OPERATIONX).
 *   
 *    APLBenchmarker.startTiming(APLBenchmarker.OPERATIONX)
 *    ...
 *    operationX();
 *    ...
 *    APLBenchmarker.stopTiming(APLBenchmarker.OPERATIONX)
 *    
 * 4. The benchmarker will count the number of times that this operation has been performed, 
 *    the total execution time of this operation and the average execution time for one
 *    execution.
 *    
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class APLBenchmarkParam 
{
	/**
	 * The benchmark time in seconds
	 */
	public static int BENCHMARK_TIME_SEC = 10;
	
	/**
	 * Enable this if you want an agent specific benchmark. Else, all the agent-specific information
	 * is taken together and the sum of it all is printed.
	 * 
	 */
	public static boolean MULTIPLE_AGENT_BENCHMARK = false; 
	
	/**
	 * Enable this to print the agents that have not done anything.
	 */
	public static boolean PRINT_UNUSED_AGENTS = false;
	
	/**
	 * Enable to discard all text output that is sent from the multi-agent system, and
	 * only print the benchmark information
	 */
	public static boolean NO_OUTPUT = true;
	
	/**
	 * Write decimal numbers using a comma instead of a point
	 * If this is false, then 1/4 is written as 0.25.
	 * If this is true, it is written as 0,25.
	 */
	public static boolean DECIMAL_AS_COMMA = true;
	
	/**
	 * Operation constants (see description above)
	 */
	public static final String DELIB_STEP = "deliberation step";
	public static final String BEL_QUERY = "belief query";
	public static final String BEL_UPD = "belief update";
	public static final String GOAL_QUERY = "goal query";
	public static final String GOAL_UPD = "goal update";
	public static final String PCRULE = "pc rule call";
	public static final String PGRULE = "pg rule call";
	public static final String PRRULE = "pr rule call";
	
	/**
	 * Use this module name when you don't know which one it is
	 */
	public static final String UNKNOWN_MODULE = "#unknown";
	
	/**
	 * Internal module that is created to show general results, do not use
	 */
	public static final String COLLECTOR_MODULE = ",#collector#,";
	
	/**
	 * Operations array (see description above)
	 */
	protected static final String OP_ARRAY[] = { DELIB_STEP, BEL_QUERY, BEL_UPD, GOAL_QUERY, GOAL_UPD, PGRULE/*, PCRULE,  PRRULE*/ };
	

	/**
	 * Deprecated, do not enable
	 */
	@Deprecated
	public static final boolean DIRTY_LOADER = false;
}
