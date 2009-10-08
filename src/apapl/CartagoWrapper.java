package apapl;

import alice.cartago.*;
import alice.cartagox.gui.*;
import java.util.ArrayList;
import java.util.HashMap;

import apapl.data.*;
import apapl.Environment;
import java.io.File;

public class CartagoWrapper
{
	private ICartagoEnvironment env;
	private static HashMap<String,ICartagoContext> contexts = new HashMap<String,ICartagoContext>();
	private static HashMap<String,ArtifactId> artifacts = new HashMap<String,ArtifactId>();
	private static HashMap<String,SensorId> sensors = new HashMap<String,SensorId>();

	public CartagoWrapper()
	{
		env = Cartago.getInstance("2apl-env");
		loadClassPath();
	}
	
	public void addModule(String module)
	{
		ICartagoContext context = env.join(module);
		SensorId defaultSensor = context.linkDefaultSensor();
		contexts.put(module,context);
	}
	
	public void removeModule(String module)
	{
		contexts.put(module,null);
	}
	
	public void use(String module, String artifact, APLFunction opAtom, String sensor, int timeout) throws ActionFailedException
	{
		SensorId sid = null;
		if (sensor!=null) sid = getSensor(sensor,module);
		Op op = new Op(opAtom.getName(),opAtom.getParams().toArray());
		ICartagoContext context = getContext(module);
		try {
			ArtifactId a = getArtifact(artifact);
			if (sensor!=null&&timeout>=0) context.use(a,op,sid,timeout);
			else if(sensor!=null) context.use(a,op,sid);
			else if(timeout>=0) context.use(a,op,timeout);
			else context.use(a,op);
		}
		catch (Exception ex){
			throw new ActionFailedException("Failed using "+op+" with artifact "+artifact+".");
		}
	}
	
	public void linkSensor(String module, String s) throws ActionFailedException
	{
		AbstractSensor sensor = new DefaultSensor(s);
		SensorId id = getContext(module).linkSensor(sensor);
		sensors.put(s,id);
	}
	
	public Term sense(String module, String sensor, String filter, int timeout) throws ActionFailedException
	{
		SensorId sid = getSensor(sensor,module);
		ICartagoContext context = getContext(module);		

		Perception p;
		try {
			if (filter!=null&&timeout>=0) p = context.sense(sid,filter,timeout);
			else if (filter!=null) p = context.sense(sid,filter);
			else if (timeout>=0) p = context.sense(sid,timeout);
			else p = context.sense(sid);
			return perceptionToTerm(p);
		}
		catch (NoPerceptionException ex) {
			throw new ActionFailedException("No perception for sensor "+sensor+" with filter "+filter);
		}
		catch (Exception ex){
			throw new ActionFailedException("Unexpected exception: "+ex);
		}
	}
	
	private APLFunction perceptionToTerm(Perception p)
	{
		Term a, b, c;
		String type = p.getType();
		if (type.startsWith("property_updated")) {
			int l = type.indexOf("(");
			int r = type.lastIndexOf(")");
			a = new APLFunction("property_updated",new APLIdent(type.substring(l+2,r-1)));
		}
		else a = new APLIdent(type);
		Object content = p.getContent();
		try { b = new APLNum(Double.parseDouble(content.toString()));}
		catch (NumberFormatException e) {b = new APLIdent(content.toString());}
		catch (NullPointerException e) {b = new APLIdent("no_content");}
		c = new APLIdent(p.getSourceId().getName());
		return new APLFunction("perception",a,b,c);
	}
	
	public void unlinkSensor(String module, String s) throws ActionFailedException
	{
		try {
			getContext(module).unlinkSensor(getSensor(s,module));
		}
		catch (CartagoException e) {
			throw new ActionFailedException(""+e);
		}
	}
	
	public void focus(String module, String artifact, String sensor) throws ActionFailedException
	{
		ICartagoContext context = getContext(module);
		
		try {
			context.focus(getArtifact(artifact),getSensor(sensor,module));
		}
		catch (SensorNotLinkedException e) {
			throw new ActionFailedException("Sensor not linked: "+sensor);
		}
	}
	
	public void stopFocus(String module, String artifact) throws ActionFailedException
	{
		ICartagoContext context = getContext(module);
		context.stopFocus(getArtifact(artifact));
	}
		
	private ICartagoContext getContext(String module) throws ActionFailedException
	{
		ICartagoContext context = contexts.get(module);
		if (context==null) throw new ActionFailedException("No such module: "+ module);
		else return context;
	}
	
	private SensorId getSensor(String sensor, String module) throws ActionFailedException
	{
		SensorId sid = sensors.get(sensor);
		if (sid==null) throw new ActionFailedException("No such sensor: "+sensor);
		else return sid;
	}
	
	private ArtifactId getArtifact(String artifact) throws ActionFailedException
	{
		ArtifactId a = artifacts.get(artifact);
		if (a==null) throw new ActionFailedException("No such artifact: "+a);
		else return a;
	}
	
	public void createArtifact(String name, String def) throws ActionFailedException
	{
		CartagoGUISystem.init(env);
		
		try {
			ArtifactId aid = env.createArtifact(name, Class.forName(def));
			artifacts.put(name.toString(),aid);
		}
		catch (ArtifactAlreadyPresentException ex) {
			throw new ActionFailedException("Artifact already present: "+name);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void disposeArtifact(String name) throws ActionFailedException
	{
		try {
			ArtifactId aid = artifacts.get(name);
			if (aid==null) throw new ActionFailedException("Artifact does not exist: "+name);
			env.disposeArtifact(aid);
			artifacts.put(name.toString(),null);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void loadClassPath()
	{
		File dir = new File( "cartago" );
		File[] files = dir.listFiles();
		if (files==null) return;
		for( int i = 0; i < files.length; i++ ) {
			File file = files[i];
			String fileName = file.getName();
			if(fileName.endsWith(".jar")||fileName.endsWith(".class")) {
				try	{
					file = file.getAbsoluteFile();
					ClassPathHacker.addFile(file);
				}
				catch( Exception e ) {
					System.err.println( "Caught exception:" );
					e.printStackTrace( System.err );
				}
			}
		}
	}		
}

