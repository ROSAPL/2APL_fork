package gui;

import org.jext.*;
import org.jext.toolbar.JextToolBar;
import org.jext.gui.JextButton;
import org.jext.misc.Workspaces;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Label;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.DefaultEditorKit;


public class FileFrame extends JComponent
{
	private JEditorPane ta;
	private File file;
	private String filename;
	private boolean changed = false;
	private Viewer viewer;
	private boolean readOnly = true;

	
	public FileFrame(File file, Viewer viewer)
	{
		this.file = file;
		this.viewer = viewer;
		
		ta = new JEditorPane();

		loadFile(file);
		
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(ta),BorderLayout.CENTER);
		
		ta.setEditable(false);
	}
	
	private void loadFile(File file)
	{
		BufferedReader r;
		String t = "";
		try
		{
			r = new BufferedReader(new FileReader(file));
			String s = r.readLine();
			while (s!=null) {t=t+s+"\n";s=r.readLine();}
			r.close();
			ta.setText(t);
		}
		catch (IOException e) {System.out.println("Error reading file: "+file);}
	}
	
	public File getFile()
	{
		return file;
	}
}
