import java.io.File;

import apapl.APAPLBuilder;
import apapl.APLMAS;
import apapl.LoadEnvironmentException;
import apapl.MultiThreadedExecutor;
import apapl.messaging.LocalMessenger;
import apapl.messaging.Messenger;
import apapl.parser.ParseMASException;
import apapl.parser.ParseModuleException;
import apapl.parser.ParsePrologException;
import gui.Config;
import gui.Toolbar;

public class APAPL {
	
	public static void main(String[] args) {

		// if there is a first parameter, load the MAS without GUI 
		if( args.length == 1) {

			// does file exist?
			File masfile  	= new File(args[0]);
			
			if( masfile.exists() == false) {
				
				System.out.println("File does not exist!");
				System.exit(0);
				
			}
			
			
			Messenger msgr 	= new LocalMessenger();
			APAPLBuilder builder = new APAPLBuilder();

			// loading the MAS
			APLMAS mas = null;
			try {
				mas = builder.buildMas(masfile, msgr, new MultiThreadedExecutor());
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
			
			// start the MAS
			mas.start();
		
		}
		else {
		
			new Config();
			
		}
		
	}

}
