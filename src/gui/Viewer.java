package gui;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

public class Viewer extends JTabbedPane
{
	private static final long serialVersionUID = 5863472474939770346L;

	//protected ArrayList<FileFrame> files = new ArrayList<FileFrame>();
	protected ArrayList<JComponent> tabs = new ArrayList<JComponent>();

	protected boolean showBases = true;
	protected boolean showFileTabs = false;
		
	public Viewer()
	{
	}
	
	public void select(JComponent c)
	{
		setSelectedComponent(c);
	}
}
