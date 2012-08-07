package gui;

import javax.swing.JScrollPane;

public class MasTab extends Viewer 
{
	private MessageTab messageTable;

	public MasTab( MessageTab messageTable )
	{
		this.messageTable = messageTable;
		addTab("Messages",new JScrollPane(messageTable));
	}
}
