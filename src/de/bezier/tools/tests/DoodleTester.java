
package de.bezier.tools.tests;

import de.bezier.tools.*;

import processing.app.*;
import processing.app.tools.*;

import java.awt.*;

public class DoodleTester
{
    public static void main ( String[] args )
    {
        Frame frame = null;
        DApplet papplet;
        
    	if ( frame == null || !frame.isVisible() )
    	{
        	frame = new Frame( "Doodle - Tester" );
        	
        	frame.setSize(new Dimension(200, 200));
        	
        	papplet = new DApplet();
        	papplet.frame = frame;
        	frame.add(papplet, BorderLayout.CENTER);
        	
        	papplet.init();
        	
        	//frame.addWindowListener( this );
        	//frame.addKeyListener(papplet);
        	//frame.addMouseListener(papplet);
        }
        
    	frame.show();
    	frame.setVisible( true );
    }
}