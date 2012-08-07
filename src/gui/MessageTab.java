package gui;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import apapl.messaging.APLMessage;
import apapl.messaging.MessageListener;

public class MessageTab extends JTable implements MessageListener
{
	private static final long serialVersionUID = -2147217312552626057L;
	private MessagesModel model;
	
	public MessageTab()
	{
		model = new MessagesModel();
		update();
	}
		
	public void messageSent(APLMessage message)
	{
		model.addMessage(message);
		if (isShowing()) update();
	}
	
	private void update()
	{
		setModel(model);
		//TODO: hoe netjes updaten
		//When invokeLater is called contiunously without a sleep, it causes the ui to crash.
		try {Thread.sleep(1);}
		catch (Exception e) {System.out.println("oops");}
		
		//Directly updating the ui causes exceptions (docuemted by swing).
		//Swing suggests invokeLater for updating the ui.
		Runnable update = new Runnable() {public void run() {updateUI();}};
		SwingUtilities.invokeLater(update);
	}	
}