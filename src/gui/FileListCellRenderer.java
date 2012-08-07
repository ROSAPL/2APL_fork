package gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class FileListCellRenderer implements ListCellRenderer, ActionListener
{
	public FileListCellRenderer()
	{
	}
	

	public Component getListCellRendererComponent(
	JList list,
	Object value,
	int index,
	boolean isSelected,
	boolean cellHasFocus)
	{
		return (JCheckBox)value;
	}
	
	
	public void actionPerformed (ActionEvent e)
	{
	}
	
}