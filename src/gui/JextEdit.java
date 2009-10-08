package gui;

import java.util.HashSet;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.UIManager;
import javax.swing.JTextPane;
import org.jext.*;
import org.jext.event.*;

public class JextEdit implements JextListener, Runnable, Edit
{
	
	private static HashSet<String> filesInJext = new HashSet<String>();
	private Thread t = new Thread(this);
	
	private boolean backgroundJext = false;
		
	public JextEdit()
	{
	}
	
	public void run()
	{
		launchNewJextWindow();
	}
	
	public void init()
	{
		if (backgroundJext)	t.start();
	}
	
	public void editFile(File file, Viewer viewer)
	{
		if (file!=null) {
			ArrayList<JextFrame> instances = Jext.getInstances();
			if (instances.size()<1) launchNewJextWindow();
			instances = Jext.getInstances();
			if (instances.size()>0) {
				if (!filesInJext.contains(file.toString())) {
					instances.get(0).open(file.getAbsolutePath());
					filesInJext.add(file.toString());
				}
				instances.get(0).setVisible(true);
				instances.get(0).requestFocus();
			}
		}
	}
	
	private void launchNewJextWindow()
	{
		Jext.main(new String[0]);
		ArrayList<JextFrame> instances = Jext.getInstances();
		if (instances.size()>0) {
			JextFrame j = instances.get(0);
			Jext.setProperty("leftPanel.show", "off");
			instances.get(0).triggerTabbedPanes();
			j.setVisible(false);
			j.closeAll();
			j.addJextListener(this);
			
			WindowListener[] wls = j.getWindowListeners();
			for (int i=0; i< wls.length; i++) j.removeWindowListener(wls[i]);
			j.addWindowListener(new WindowHandler());
		}
	}
	
	public void jextEventFired(JextEvent evt)
	{
		if (evt.getWhat()==JextEvent.TEXT_AREA_CLOSED)
		{
			JextTextArea j = evt.getTextArea();
			if (j!=null) {
				File f = j.getFile();
				if (f!=null) filesInJext.remove(f.toString());
			}
		}
	}
	
	class WindowHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent evt)
		{
			if (evt.getWindow() instanceof JextFrame) {
				JextFrame j = (JextFrame)(evt.getWindow());
						
				j.closeWindow();
				if (Jext.getInstances().size()<1) {
					filesInJext = new HashSet<String>();
					Jext.stopServer();
				}
				
			}
		}
	}
}
	