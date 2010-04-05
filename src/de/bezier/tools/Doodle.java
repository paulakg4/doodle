/**
 *	<h3>Doodle</h3>
 *
 *	<p>
 *	Doodle tool for Processing IDE, a simplistic vector editor for
 *	creating and editing beginShape()-endShape() blocks.
 *	</p>
 *		
 *	@author		florian jenett - mail@bezier.de
 *	@modified	##date##
 *	@version	##version##
 */

package de.bezier.tools;

import processing.app.*;
import processing.app.tools.*;

import java.awt.*;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;
import java.util.Vector;
import javax.swing.JFrame;


public class Doodle
implements Tool, WindowListener
{	
    static DApplet papplet;
    
    static String version = "##version##";
    static String builton = "##date##";
    
    Frame frame;
    boolean startupmsg;
    boolean running;
    
    Editor editor;

    public Doodle ( )
    {
    }
    
    public String getMenuTitle ()
    {
    	return "Doodle";
    }

    public void init ( Editor _e )
    {
	editor = _e;
    }

    public void run ()
    {
        startupmsg = true;
        running = false;
        
    	if ( frame == null || !frame.isVisible() )
    	{
        	frame = new Frame("Doodle - " + version);
        	
        	frame.setSize(new Dimension(200, 200));
        	
        	papplet = new DApplet( editor, this );
        	papplet.frame = frame;
        	frame.add(papplet, BorderLayout.CENTER);
        	
        	papplet.init();
        	
        	frame.addWindowListener( this );
        	//frame.addKeyListener(papplet);
        	//frame.addMouseListener(papplet);
        }
        
    	frame.show();
    	frame.setVisible( true );
        
        sayHello();
    }
    
    void halt ()
    {
    	papplet.stop();
		
        frame.hide();
    }

    public void windowOpened(WindowEvent windowevent)
    {
    }

    public void windowDeactivated(WindowEvent windowevent)
    {
    }

    public void windowActivated(WindowEvent windowevent)
    {
    }

    public void windowDeiconified(WindowEvent windowevent)
    {
    }

    public void windowIconified(WindowEvent windowevent)
    {
    }

    public void windowClosed(WindowEvent windowevent)
    {
    	halt();
    }

    public void windowClosing(WindowEvent windowevent)
    {
    	halt();
    }
    
    private void sayHello()
    {
        if ( startupmsg )
        {
	    System.out.println("");
	    System.out.println("Doodle - version "+version+" - built "+builton);
	    System.out.println("fjenett - 2008-2010 - http://bezier.de/processing/tools/");
	    System.out.println("");
	    System.out.println("Built upon Geomerative and ControlP5 libraries.");
	    System.out.println("http://ricardmarxer.com/geomerative/");
	    System.out.println("http://sojamo.de/libraries/controlP5/");
	    System.out.println("");
	    startupmsg = false;
	}
    }
}
