 package blockworld;

/*
 3APL Blockworld program by Jelle Herold, copyright 2003.
 Written for the Intelligent Systems group at Utrecht University.
 This is LGPL software.

 $Id: Env.java,v 1.7 2004/12/27 02:22:41 cvs-3apl Exp $
 */

// 2APL imports
import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.data.*;

// Standard java imports
import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import blockworld.lib.ObsVectListener;
import blockworld.lib.Signal;
import blockworld.lib.ObsVect;

public class Env extends Environment implements ObsVectListener
{
	// To hold our reference to the window
	final protected Window 					m_window;
	
	// max distance of cells visible to each agent
	protected int							_senserange = 5;
	
	// size of environment
	protected Dimension 					m_size = new Dimension( 16, 16 );
	
	private HashMap<String,Agent> 			agentmap = new HashMap<String,Agent>();
	
	/* ---- ALL the stuff in the environment -----*/
	
	// list of agents (Agent)
	protected ObsVect 						_agents = new ObsVect( this );
	// location of bomb traps
	protected ObsVect 						_traps = new ObsVect();
	// list of coordinates (Point) containing stones
	protected ObsVect 						_stones = new ObsVect();
	// list of coordinates (Point) containing bombs
	protected ObsVect 						_bombs = new ObsVect();
	// id for identifiable objects
	protected String 						_objType = "default";	
	
	/* ------------------------------------------*/


	/* ---- SIGNALS -------------------------------*/
	// / emitted on collection of a bomb in the bomb trap
	public transient Signal signalBombTrapped = new Signal( "env bomb trapped" );
	
	public transient Signal signalSenseRangeChanged = new Signal(
			"env sense range changed" );

	// emitted if environment is resized
	public transient Signal signalSizeChanged = new Signal( "env size changed" );

	// emitted if bomb traps location changed
	public transient Signal signalTrapChanged = new Signal(
			"env trap position changed" );
	
	/* ------------------------------------------*/	

	
	// The default constructor
	public Env()
	{
		super();
		// Create the window
		m_window = new Window( this );
	}

	/* Called from 2APL */
	
	// Enter the agent into the world
	// Succesful returns true, else the ExternalActionFailedException exception is thrown
	public Term enter( String sAgent, APLNum xt, APLNum yt, APLIdent colort) throws ExternalActionFailedException
	{
		int x = xt.toInt();
		int y = yt.toInt();
		String color = colort.toString();
		
		// Get the agent
		Agent agent = getAgent(sAgent);
		
		// Give a signal that we want to move
		agent.signalMove.emit();
		
		writeToLog( "Agent entered: " +agent.getName());
		
		Point position = new Point(x,y);
		String pos = "("+x+","+y+")";
		
		// Agent already entered
		if( agent.isEntered() ) 
		{
			writeToLog( "agent already entered" );
			throw new ExternalActionFailedException("Agent \""+agent.getName()+"\" has already entered.");
		}
		
		// Is this position within the world?
		if (outOfBounds(position)) 
		{
			throw new ExternalActionFailedException("Position "+pos+" out of bounds.");
		}
		// Is this position free?
		if( !isFree( position ) )
		{
			throw new ExternalActionFailedException("Position "+pos+" is not free.");
		}
		
		// Update the agent his position
		agent._position = position;
		
		// Which color does the agent want to be
		int nColorID = getColorID(color);
		agent._colorID = nColorID;
	
		// Redraw so we can see the agent
		validatewindow();
		m_window.repaint();
		
		// We came so far, this means success!
		agent.signalMoveSucces.emit();

		return wrapBoolean(true);
	}
	
	// Move the agent north
	public Term north(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct agent
		Agent agent = getAgent(sAgent);
		
		// Get the agent his position
		Point p = (Point) agent.getPosition().clone();
		p.y = p.y - 1;
		
		// Set the position for the agent
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Move the agent east
	public Term east(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct agent
		Agent agent = getAgent(sAgent);
		
		// Get the agent his position
		Point p = (Point) agent.getPosition().clone();
		p.x = p.x + 1;
		
		// Set the position for the agent
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Move the agent south
	public Term south(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct agent
		Agent agent = getAgent(sAgent);
		
		// Get the agent his position
		Point p = (Point) agent.getPosition().clone();
		p.y = p.y + 1;
		
		// Set the position for the agent
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Move the agent west
	public Term west(String sAgent) throws ExternalActionFailedException
	{
		// Get the correct agent
		Agent agent = getAgent(sAgent);
		
		// Get the agent his position
		Point p = (Point) agent.getPosition().clone();
		p.x = p.x - 1;
		
		// Set the position for the agent
		boolean  r = setAgentPosition( agent, p);
		
		// can't move north
		if (!r) throw new ExternalActionFailedException("Moving north failed.");

		// Redraw the window
		validatewindow();
		m_window.repaint();
		return wrapBoolean(r);
	}
	
	// Pickup a bomb
	public Term pickup( String sAgent ) throws ExternalActionFailedException
	{
		// Get the agent
		Agent agent = getAgent(sAgent);
		
		// Let everyone know we are going to pick up a bomb	
		agent.signalPickupBomb.emit();

		// see if we are not already carrying a bomb
		if( agent.atCapacity() ) 
		{
			writeToLog( "Pickup bomb failed" );
			throw new ExternalActionFailedException("Pickup bomb failed");
		}

		// we are not already carying a bomb so get this one
		TypeObject bomb = removeBomb( agent.getPosition() );
		if( bomb == null ) 
		{
			writeToLog( "Pickup bomb failed" );
			throw new ExternalActionFailedException("Pickup bomb failed");
		}

		// Yes
		agent.signalPickupBombSucces.emit();

		// there was a bomb at that position, so set token
		agent.pickBomb(bomb);
		
		// show what happened
		validatewindow();
		m_window.repaint();
		
		return wrapBoolean(true);
	}
	
	// Drop a bomb
	public Term drop( String sAgent ) throws ExternalActionFailedException
	{
		// Get the agent
		Agent agent = getAgent(sAgent);
		// we are going to drop a bomb
		agent.signalDropBomb.emit();

		TypeObject bomb = agent.senseBomb();
		// see if we are actually carrying a bomb
		if( bomb == null)
		{
			writeToLog( "Drop bomb failed" );
			throw new ExternalActionFailedException("Drop bomb failed");
		}
			
		Point pos = agent.getPosition();
		// see if we can drop that bomb here
		
		if( !addBomb(pos)	&& (isTrap( pos ) == null
			|| agent.senseBomb( isTrap( pos ).getType() ) == null) ) 
		{
			writeToLog( "Drop bomb failed" );
			throw new ExternalActionFailedException("Drop bomb failed");
		}
		
		if(isTrap( pos ) != null
			&& agent.senseBomb( isTrap( pos ).getType() ) != null) 
		{
			signalBombTrapped.emit();
		}

		// unset token
		agent.dropBomb();

		agent.signalDropBombSucces.emit();

		// Show it
		validatewindow();
		m_window.repaint();

		// return success
		return wrapBoolean(true);
	}
	
	// What is the agent his Sense Range
	public Term getSenseRange(String agent)
	{
		// the below function is also used by EnvView
		int r = getSenseRange();
		return new APLList(new APLNum(r));
	}

	// Sense all agents. This does not include self.
	public Term senseAllAgent(String sAgent) throws ExternalActionFailedException
	{
		//Collection c = senseAllAgents(getAgent(agent).getPosition());
		Point position = getAgent(sAgent).getPosition();
		// iterate over all agents
		Vector all = new Vector();

		Iterator j = _agents.iterator();
		while( j.hasNext() ) {
			Agent agent = (Agent) j.next();
			Point p = agent.getPosition();
			
			// Changed SA: when there are no other agents, this return null, which
			// causes Java to throw an exception and never return the empty list.
			if (p == null)
				continue;

			// skip self
			if( p.equals( position ) )
				continue;

			all.add( agent );
		}
		
		LinkedList<Term> listpar = new LinkedList<Term>();
		for(Object i : all) {
			final Agent a = (Agent) i;
			APLListVar tmp = new APLList(new APLIdent(a.getName()),new APLNum(a.getPosition().x),new APLNum(a.getPosition().y));
			listpar.add(tmp);
		}
		return new APLList(listpar);
	}
	
	// Sense the given agent his position
	public Term sensePosition(String sAgent) throws ExternalActionFailedException
	{
		Point p = getAgent(sAgent).getPosition();
		return new APLList(new APLNum(p.x),new APLNum(p.y));
	}
	
	// is there a trap in the senserange of the agent?
	public Term senseTraps(String agent) throws ExternalActionFailedException
	{
		// Get the agent his position
		Point position = getAgent(agent).getPosition();
		
		// iterate over all traps and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();

		Iterator i = _traps.iterator();
		while( i.hasNext() ) {
			TypeObject t = (TypeObject) i.next();
			if( position.distance( t.getPosition() ) <= _senserange )
				visible.add( t );
		}
		
		return convertCollection(visible);
	}
	
	// Get all the traps in the env
	public Term senseAllTraps(String agent)
	{
		// iterate over all traps
		Vector all = new Vector();

		Iterator i = _traps.iterator();
		while( i.hasNext() ) 
		{
			all.add( (TypeObject) i.next() );
		}
		
		return convertCollection(all);
	}
	
	// Sends a bom in the senserange of the agent
	public Term senseBombs(String agent) throws ExternalActionFailedException
	{
		// Get the agent his position
		Point position = getAgent(agent).getPosition();
		
		// iterate over all bombs and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();

		Iterator i = _bombs.iterator();
		while( i.hasNext() ) 
		{
			TypeObject b = (TypeObject) i.next();
			if( position.distance( b.getPosition() ) <= _senserange )
				visible.add( b );
		}
		
		return convertCollection(visible);
	}
	
	// Find all the bombs in the environment
	public Term senseAllBombs (String agent)
	{
		// iterate over all bombs
		Vector all = new Vector();

		Iterator i = _bombs.iterator();
		while( i.hasNext() ) 
		{
			all.add( (TypeObject) i.next() );
		}
		
		return convertCollection( all);
	}
	
	// Sense the stones in the agent senserange
	public Term senseStones(String agent) throws ExternalActionFailedException
	{
		// Get the agent his position
		Point position = getAgent(agent).getPosition();
		
		// iterate over all stones and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();

		Iterator i = _stones.iterator();
		while( i.hasNext() ) 
		{
			TypeObject t = (TypeObject) i.next();
			if( position.distance( t.getPosition() ) <= _senserange )
				visible.add( t );
		}
		
		return convertCollection(visible);
	}
	
	// Sense all stones
	public Term senseAllStones (String agent)
	{
		// iterate over all traps
		Vector all = new Vector();

		Iterator i = _stones.iterator();
		while( i.hasNext() ) 
		{
			all.add( (TypeObject) i.next() );
		}
		
		return convertCollection(all);
	}
	
	// Sense visible area for agents. This does not include self.
	public Term senseAgent(String sAgent) throws ExternalActionFailedException
	{
		//Collection c = senseAgents(getAgent(agent).getPosition());
		Point position = getAgent(sAgent).getPosition();
		
		// iterate over all agents and decide according to distance if it is in
		// vision range
		Vector visible = new Vector();
		
		Iterator j = _agents.iterator();
		
		while( j.hasNext() )
		{
			Agent agent = (Agent) j.next();

			Point p = agent.getPosition();
			
			// Changed SA: when there are no other agents, this return null, which
			// causes Java to throw an exception and never return the empty list.
			if (p == null)
				continue;
			
			// skip self
			if( p.equals( position ) )
				continue;

			// agent within visible range
			if( position.distance( p ) <= _senserange )
				visible.add( agent );
		}
		
		LinkedList<Term> listpar = new LinkedList<Term>();
		for(Object i : visible) {
			final Agent a = (Agent) i;
			APLListVar tmp = new APLList(new APLIdent(a.getName()),new APLNum(a.getPosition().x),new APLNum(a.getPosition().y));
			listpar.add(tmp);
		}
		return new APLList(listpar);
	}


	
	/* Standard functions --------------------------------------*/
	
	public void notifyAgents(APLFunction event, String... receivers) {
		 throwEvent(event);
	}
	
	private void notifyEvent(String parm1, Point ptPosition)
	{
		APLNum	nX	= new APLNum((double)(ptPosition.getX()));
		APLNum	nY	= new APLNum((double)(ptPosition.getY()));

		// Send an external event to all agents within the senserange.
		ArrayList<String> targetAgents = new ArrayList<String>();
		for (Agent a : agentmap.values())
		{
			// Changed SA: I got no idea why there is always 1 agent which does not exists, 
			// but this fixes the exceptions
			if ((a.getPosition() != null) && (ptPosition.distance(a.getPosition()) <= getSenseRange()))
				targetAgents.add(a.getName());
		}

		writeToLog("EVENT: "+parm1+"("+nX+","+nY+")"+" to "+targetAgents);

		if (!targetAgents.isEmpty())
		{
			notifyAgents(new APLFunction(parm1,nX,nY),targetAgents.toArray(new String[0]));
		}
	}

	// what is it
	public String getDescription() { return "Blockworld for 2APL"; }
	
	public String toString() { return "blockworld"; }
	
	public void refresh() { }
	
		// Add an agent to the environment
    public void addAgent(String sAgent) {
        String sAgentMain = getMainModule(sAgent);
        // Agent not yet in the environment
        if (agentmap.keySet().contains(sAgentMain)) {
            agentmap.put(sAgent,agentmap.get(sAgentMain));  
            writeToLog("linking " + sAgent + "");
        } else{
            final Agent agent = new Agent(sAgentMain);
            _agents.add(agent);
            agentmap.put(sAgent, agent);
            writeToLog("agent " + agent + " added");
        }

                
    }
	
	// Remove the agent from the environment
	public void removeAgent(String sAgent)
	{
		try 
		{
			//String sAgentMain = getMainModule(sAgent);
			
			Agent a = getAgent(sAgent);			
			agentmap.remove( sAgent );
			
			// there can be several agent
			if (!agentmap.containsValue(a)) {
			    _agents.remove(a);		
			    a.reset();
			} 
			
			writeToLog("Agent removed: " + sAgent);
	
			synchronized( this ) 
			{
				notifyAll();
			}
		}
		catch (ExternalActionFailedException e) {}
	}
	
	/* END Standard functions --------------------------------------*/
	
	
	
	
	/* Helper functions --------------------------------------*/
	
	// Get the size of the blockworld
	public Term getWorldSize(String agent)
	{
		int w = getWidth();
		int h = getHeight();
		return new APLList(new APLNum(w),new APLNum(h));
	}
	
	// Get the agent from its name
	private Agent getAgent(String name) throws ExternalActionFailedException
	{    
		Agent a = null;
		//a = agentmap.get(getMainModule(name));
		a = agentmap.get(name);
		if (a==null) throw new ExternalActionFailedException("No such agent: "+name);
		else return a;
		
	}
	
	private String getMainModule(String sAgent)
	{
		int dotPos;
		if ((dotPos = sAgent.indexOf('.')) == -1)
			return sAgent;
		else
			return sAgent.substring(0, dotPos);
	}
	
	// Get the environment width
	public int getWidth() {	return m_size.width; }

	// Get the environment height
	public int getHeight() { return m_size.height; }
	
	// Return the agents
	public Collection getBlockWorldAgents() 
	{
		return _agents;
	}
	
	// convert a collection to a term
	private Term convertCollection(Collection c)
	{
		LinkedList<Term> listpar = new LinkedList<Term>();
		for(Object i : c) {
			final TypeObject o = (TypeObject) i;
			APLListVar tmp = new APLList(new APLIdent(o.getType()),new APLNum(o.getPosition().x),new APLNum(o.getPosition().y));
			listpar.add(tmp);
		}
		return  new APLList(listpar);
	}
	
	// Get senserange
	public int getSenseRange() 
	{
		return _senserange;
	}
	
	// Redrawing the window is a nightmare, this does some redraw stuff
	private void validatewindow()
	{
		Runnable addTab = new Runnable()
		{
			public void run()
			{
				//try {Thread.sleep(500);} catch(Exception e) {}
				m_window.doLayout();
				// Changed SA: NO you may not close
				if (!m_window.isVisible())
				{
					m_window.setVisible( true );
				}
			}
		};
		SwingUtilities.invokeLater(addTab);
	}
	
	// Move the agent
	private boolean setAgentPosition( Agent agent, Point position) 
	{
		agent.signalMove.emit();

		if( outOfBounds( position ) )
			return false;

		// suspend thread if some other agent is blocking our entrance
		
		// Is the position free?
		if( !isFree( position ) )
			return false;

		agent.signalMoveSucces.emit();

		// there may be other threads blocked because this agent was in the way,
		// notify
		// them of the changed state of environment
		synchronized( this ) 
		{
			notifyAll();
		}

		// set the agent position
		agent._position = position;
		return true;
	}
	
	// check if point is within environment boundaries
	// return false is p is within bounds
	protected boolean outOfBounds( Point p ) 
	{
		if( (p.x >= m_size.getWidth()) || (p.x < 0) || (p.y >= m_size.getHeight()) || (p.y < 0) )
		{
			return true;
		}
		
		return false;
	}
	
	// Is the position free?
	public boolean isFree( final Point position ) 
	{
		return (isStone( position )) == null && (isAgent( position ) == null);
	}
	
	 // Check for agent at coordinate. \return Null if there is no agent at the
	 // specified coordinate. Otherwise return a reference to the agent there.
	public Agent isAgent( final Point p ) 
	{
		Iterator i = _agents.iterator();
		while( i.hasNext() ) {
			final Agent agent = (Agent) i.next();
			if( p.equals( agent.getPosition() ) )
				return agent;
		}
		return null;
	}
	
	// Is there a stone at this point
	public TypeObject isStone( Point p ) 
	{
		Iterator i = _stones.iterator();
		while( i.hasNext() ) {
			TypeObject stones = (TypeObject) i.next();
			if( p.equals( stones.getPosition() ) )
				return stones;
		}
		return null;
	}
	
	// see if there is a trap at the specified coordinate
	public TypeObject isTrap( Point p ) {
		Iterator i = _traps.iterator();
		while( i.hasNext() ) {
			TypeObject trap = (TypeObject) i.next();
			if( p.equals( trap.getPosition() ) )
				return trap;
		}
		return null;
	}
	
	public TypeObject isBomb( Point p ) 
	{
		for (Object b : _bombs) {
			TypeObject bomb = (TypeObject)b;
			if(p.equals(bomb.getPosition())) return bomb;
		}
		return null;
	}
	
	// Remove bomb at position TODO Jaap; why is this different then remove stone???
	public TypeObject removeBomb( Point position )
	{
		// find bomb in bombs list
		Iterator i = _bombs.iterator();

		while (i.hasNext())
		{
			TypeObject bomb = (TypeObject) i.next();
			if (position.equals(bomb.getPosition()))
			{
				i.remove();
				notifyEvent("bombRemovedAt", position);
				return bomb;
			}
		}
		return null;
	}
	
	// remove stone at position
	public boolean removeStone( Point position )
	{
		// find stone in stones list
		Iterator i = _stones.iterator();

		while (i.hasNext())
			//if( position.equals( i.next() ) ) {
			// Changed SA:
			if (position.equals(((TypeObject)i.next()).getPosition()))
			{
				i.remove();
				
				notifyEvent("wallRemovedAt", position);

				// there may be other threads blocked because this agent was in
				// the way, notify
				// them of the changed state of environment
				synchronized( this ) 
				{
					notifyAll();
				}

				return true;
			}

		return false;
	}
	
	// remove trap at position
	public boolean removeTrap( Point position )
	{
		// find trap in traps list
		Iterator i = _traps.iterator();

		while (i.hasNext())
			if (position.equals(((TypeObject)i.next()).getPosition()))
			{
				i.remove();

				notifyEvent("trapRemovedAt", position);

				// Sohan: I believe this notification is unnecessary, commented it out:
				//synchronized( this ) {
				//	notifyAll();
				//}

				return true;
			}

		return false;
	}
	
	// Add a stone at the given position
	public boolean addStone( Point position ) throws IndexOutOfBoundsException 
	{
		// valid coordinate
		if( outOfBounds( position ) )
			throw new IndexOutOfBoundsException( "setStone out of range: "
					+ position + ", " + m_size );

		// is position clear of other stuff
		// Changed SA:
		if( isBomb( position ) != null || isStone( position ) != null ||  isTrap( position ) != null )
			return false;

		_stones.add( new TypeObject(_objType,position) );
		
		notifyEvent("wallAt", position);

		return true;
	}
	
	// Add a bomb to the environment
	boolean addBomb( Point position ) throws IndexOutOfBoundsException
	{
		if( outOfBounds( position ) )
			throw new IndexOutOfBoundsException( "addBomb outOfBounds: "
					+ position + ", " + m_size );

		// is position clear of other stuff
		if( isBomb( position ) != null ||  isStone( position ) != null ||  isTrap( position ) != null )
			return false;

		// all clear, accept bomb
		_bombs.add( new TypeObject(_objType,position) );

		notifyEvent("bombAt", position);

		return true;
	}
	
	// Add a trap at the given position
	public boolean addTrap( Point position ) throws IndexOutOfBoundsException {
		// valid coordinate
		if( outOfBounds( position ) )
			throw new IndexOutOfBoundsException( "setTrap out of range: "
					+ position + ", " + m_size );

		// is position clear of other stuff
		// Changed SA:
		if( isBomb( position ) != null ||  isStone( position ) != null ||  isTrap( position ) != null )
			return false;

		_traps.add( new TypeObject(_objType,position) );
		
		notifyEvent("trapAt", position);

		return true;
	}
	
	// Print a message to the console
    static public void writeToLog(String message) {
      //System.out.println("blockworld: " + message);
    }
	
	// helper function to wrap a boolean value inside a ListPar.
	static public APLListVar wrapBoolean( boolean b )
	{
		return new APLList(new APLIdent(b ? "true" : "false"));
	}
	
	// Which color does the agent want to be!
	private int getColorID(String sColor)
	{
		if (sColor.equals("army") )
		{
			return 0;
		}
		else if (sColor.equals("blue") )
		{
			return 1;
		}
		else if (sColor.equals("gray") )
		{
			return 2;
		}
		else if (sColor.equals("green") )
		{
			return 3;
		}
		else if (sColor.equals("orange") )
		{
			return 4;
		}
		else if (sColor.equals("pink") )
		{
			return 5;
		}
		else if (sColor.equals("purple") )
		{
			return 6;
		}
		else if (sColor.equals("red") )
		{
			return 7;
		}
		else if (sColor.equals("teal") )
		{
			return 8;
		}
		else if (sColor.equals("yellow") )
		{
			return 9;
		}
		
		// Red is the default
		return 7;
	}
	
	// Set the senserange
	public void setSenseRange( int senserange ) 
	{
		_senserange = senserange;
		signalSenseRangeChanged.emit();
	}

	// helper function, calls setSize(Dimension)
	public void setSize( int width, int height ) 
	{
		setSize( new Dimension( width, height ) );
	}
	
	// resize world
	public void setSize( Dimension size ) 
	{
		m_size = size;
		signalSizeChanged.emit();

		Iterator i = _bombs.iterator();
		while( i.hasNext() ) {
			if( outOfBounds( ((TypeObject) i.next()).getPosition() ) )
				i.remove();
		}
		i = _stones.iterator();
		while( i.hasNext() ) {
			if( outOfBounds( (Point) i.next() ) )
				i.remove();
		}
		i = _traps.iterator();
		while( i.hasNext() ) {
			if( outOfBounds( ((TypeObject) i.next()).getPosition() ) )
				i.remove();
		}
	}
	
	// what kind of object is it, bomb, stone, wall ?
	public String getObjType() 
	{
		return _objType;
	}

	// what kind of object is it, bomb, stone, wall ?
	public void setObjType(String objType) 
	{
		_objType = objType;
	}

	// Remove everything
	public void clear() 
	{
		_stones.removeAllElements();
		_bombs.removeAllElements();
		_traps.removeAllElements();
	}
	
	// Save the environment
	public void save( OutputStream destination ) throws IOException 
	{
		ObjectOutputStream stream = new ObjectOutputStream( destination );
		stream.writeObject( m_size );

		stream.writeInt( _senserange );

		stream.writeObject( (Vector) _stones );
		stream.writeObject( (Vector) _bombs );
		stream.writeObject( (Vector) _traps );
		stream.flush();
	}

	// Load the environment
	public void load( InputStream source ) throws IOException, ClassNotFoundException 
	{
		ObjectInputStream stream = new ObjectInputStream( source );
		Dimension size = (Dimension) stream.readObject();

		int senserange = stream.readInt();

		Vector stones = (Vector) stream.readObject();
		Vector bombs = (Vector) stream.readObject();
		Vector traps = (Vector) stream.readObject();

		// delay assignments until complete load is succesfull
		m_size = size;
		_senserange = senserange;

		signalSizeChanged.emit();
		signalTrapChanged.emit();
		signalSenseRangeChanged.emit();

		clear();

		_stones.addAll( stones );
		_bombs.addAll( bombs );
		_traps.addAll( traps );
	}
	
	/* END Helper functions --------------------------------------*/
	
	/* Listeners ------------------------------------------------*/
	
	// / This listener is notified upon changes regarding the Agent list.
	// / Please note that this only involves registering new agents or
	// / removing existing agents. To track agent position changes, add
	// / a listener to that specific agent.
	// / \sa Agent
	public void addAgentListener( ObsVectListener o ) 
	{
		_agents.addListener( o );
	}

	// / This listener is notified upon changes regarding the Stones list.
	public void addStonesListener( ObsVectListener o ) 
	{
		_stones.addListener( o );
	}

	// / This listener is notified upon changes regarding the Bombs list.
	public void addBombsListener( ObsVectListener o ) 
	{
		_bombs.addListener( o );
	}

	// / This listener is notified upon changes regarding the Traps list.
	public void addTrapsListener( ObsVectListener o ) 
	{
		_traps.addListener( o );
	}
	
	/* END Liseteners ------------------------------------------*/
	

	/* Overrides for ObsVector ---------------------------------*/
	
	public void onAdd( int index, Object o ) {}

	public void onRemove( int index, Object o ) 
	{
		((Agent) o).deleteObservers();
	}
	
	/* END Overrides for ObsVector ---------------------------------*/
}
