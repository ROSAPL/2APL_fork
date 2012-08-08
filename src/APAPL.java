import gui.Config;
import gui.GUI;

import java.io.File;
import java.io.IOException;

import apapl.APAPLBuilder;
import apapl.APLMAS;
import apapl.LoadEnvironmentException;
import apapl.MultiThreadedExecutor;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.messaging.LocalMessenger;
import apapl.messaging.Messenger;
import apapl.parser.ParseMASException;
import apapl.parser.ParseModuleException;
import apapl.parser.ParsePrologException;

public class APAPL {

    final static String NOGUI_ARGUMENT = "-nogui";
    final static String NOJADE_ARGUMENT = "-nojade";
    final static String HELP_ARGUMENT = "-help";
    
    final static String BENCHMARK = "-benchmark";
    final static String BENCHMARK_TIME = "-time";
    final static String BENCHMARK_NOAGENTS = "-noagents";
        
    public static void main(String[] args)
    {    	
    	// has been the -benchmark argument set?
    	boolean benchmark = false;
        // has been the -nogui argument set?
        boolean nogui = false;
        // has been the -nojade argument set?
        boolean nojade = false;
        // has been the path to MAS file provided?
        File masfile = null; 

        // Parse arguments, the last argument should be the mas filename.       
        for (int i=0; i<args.length; i++) {
        	String arg = args[i];
        	if (arg.equals(BENCHMARK)) {
        		nogui = true;
        		nojade = true;
        		benchmark = true;
        	} else if (arg.equals(BENCHMARK_TIME)) {
        		if (i+1<args.length) 
        			APLBenchmarkParam.BENCHMARK_TIME_SEC = Integer.parseInt(args[i+1]);
        		else
        			arg = HELP_ARGUMENT;
        	} else if (arg.equals(BENCHMARK_NOAGENTS)) {
        		APLBenchmarkParam.MULTIPLE_AGENT_BENCHMARK = false;
        	} else if (arg.equals(NOGUI_ARGUMENT)) {
                nogui = true;
            } else if (arg.equals(NOJADE_ARGUMENT)) {
                nojade = true;
            } 
        	if (arg.equals(HELP_ARGUMENT)) {                
                String helpmessage = 
                  " \n" +  
                  "2APL (A Practical Agent Programming Language) Interpreter \n" +
                  " \n" +
                  "Usage: java -jar 2apl.jar [-benchmark [-time <time in sec> ] [-noagents] ] [-nogui] [-nojade] [-help] [<path to MAS file>] \n" +
                  " \n" +
                  "Options: \n" + 
                  "   -benchmark do a benchmark (no graphical interface) \n" +
                  "       -time      <t> the number of seconds to perform a benchmark \n" +
                  "       -noagents  print benchmarking results for all agents combined \n" +
                  "   -nogui   do not open graphical user interface; start the MAS immediately \n" + 
                  "   -nojade  skip JADE configuration and run in standalone mode \n" +
                  "   -help    print this message \n";
                 
                System.out.print(helpmessage);
                System.exit(0);
            }
        }
        
        if (args.length > 0) {
            if (!args[args.length - 1].startsWith("-")) {
                // Does the file exist?
                masfile = new File(args[args.length - 1]);
                if (!masfile.isFile()) {
                    // Try to find the mas file in the directory
                    if (masfile.isDirectory()) {                       
                        File[] listOfFiles = masfile.listFiles();
                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isFile()
                                    && listOfFiles[i].getName()
                                            .endsWith(".mas")) {
                                System.out.print("Found mas file "
                                        + listOfFiles[i].getName()
                                        + " in directory "
                                        + args[args.length - 1] + "\n");
                                masfile = new File(args[args.length - 1] + 
                                        File.separator
                                        + listOfFiles[i].getName());
                                break;
                            }
                        }
                        // Check again if a mas file is found and loaded
                        if (!masfile.isFile()) {
                            System.out.print("Cannot access MAS file: "
                                    + masfile + "\n");
                            System.exit(0);
                        }
                    } else {
                        System.out.print("Cannot access MAS file: " + masfile
                                + "\n");
                        System.exit(0);
                    }
                }
            }
        }
        
        
        
        if (!nogui) {
            if (nojade) {
                new GUI(new LocalMessenger(), masfile);
            } else {
                new Config(masfile);
            }
        } else 
        if (nogui)
        {   
            // Is the path to MAS file provided?
            if (masfile == null) {
                System.out.println("No MAS file provided!");
                System.exit(0);
            }        
            
            Messenger msgr = new LocalMessenger();
            APAPLBuilder builder = new APAPLBuilder();

            // load the MAS
            APLMAS mas = null;
            try {
                mas = builder.buildMas(masfile, msgr,
                        new MultiThreadedExecutor());
            } catch (ParseMASException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (ParseModuleException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (ParsePrologException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (LoadEnvironmentException e) {
                e.printStackTrace();
                System.exit(0);
            }

            
            if (benchmark)
            {
            	(new APLBenchmarker()).start(mas);
            } else
            {
	            // start the MAS
	            mas.start();
           
            	System.out.println("MAS started. Press a key to quit.");
            
	            try {
					System.in.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            mas.takeDown();
	            System.out.println("Done");
            }
        }
    }
}
