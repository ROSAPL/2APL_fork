package apapl.benchmarking;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Replace the normal out stream with one that does not print anything.
 * Used when the user desires not to have any output during benchmarking.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Linköping University
 *
 */
public class IOStream {
	public static PrintStream out = System.out;
	public static PrintStream err = System.err;
	public static PrintStream nullStream = new PrintStream(new OutputStream() {
			
			@Override
			public void write(int arg0) throws IOException { /* dont write anything */ }
		});
											
	public static void setStreams()
	{
		if (APLBenchmarkParam.NO_OUTPUT)
		{
			System.setOut(nullStream);
			System.setErr(nullStream);
		}
	}
}
