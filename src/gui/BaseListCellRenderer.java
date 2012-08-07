package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class BaseListCellRenderer extends JEditorPane implements ListCellRenderer
{
	Color odd, even;
	private boolean head = false;
	private boolean rtf = true;
	
	public BaseListCellRenderer(Color odd, Color even, boolean head, boolean rtf)
	{
		this.rtf = rtf;
		this.head = head;
		this.odd = odd;
		this.even = even;
	}
	
	public Component getListCellRendererComponent(
	JList list,
	Object value,
	int index,
	boolean isSelected,
	boolean cellHasFocus)
	{
		if (rtf) setContentType("text/rtf");
		
		if (rtf) setText(RTFFrame.basicRTF((String)value));
		else setText((String)value);
		
		if (head&&index==0) setFont(getFont().deriveFont(Font.BOLD));
		else setFont(getFont().deriveFont(Font.PLAIN));
		
		setBackground(index%2==1?odd:even);
		return this;
	}
}