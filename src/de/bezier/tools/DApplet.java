/**
 *	DoodleApplet
 *
 *	@author		florian jenett - mail@bezier.de
 *	@modified	##date##
 *	@version	##version##
 */
 
package de.bezier.tools;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.util.Hashtable;

import processing.app.*;
import processing.core.*;
import processing.app.tools.*;

import controlP5.*; 
import geomerative.*; 

public class DApplet
extends PApplet
implements DParserListener
{
    Editor editor;
    Sketch sketch;
    Tool tool;
    
    int widthO, heightO;
    boolean rendererWarning;

    DApplet ( Editor _e, Doodle _t )
    {
        editor = _e;
        sketch = _e.getSketch();
        sketchChanged();
    }
    
    private void setupDoodle ()
    {
    	size( 400, 400 );
    	widthO = width;
    	heightO = height;
    }
    
    private void checkChanges ()
    {
    	if ( sketch == null || sketch != editor.getSketch() )
    	{
    	    sketchChanged();
    	}
    	
    	if ( widthO != width && heightO != height )
    	{
    	    sketchSizeChanged();
    	}
    }
    
    private void sketchChanged ()
    {
	println( sketchPath );
	
	sketch = editor.getSketch();
		
        //println( "Sketch changed to: " + sketch.getName() );
    }
    
    private void sketchSizeChanged ()
    {
        widthO = width; heightO = height;
        //println( "Sketch size changed to: " + width + " , " + height );
    }

    public void size ( int i, int j )
    {
        if ( i != width || j != height )
        {
	    if ( frame != null )
		frame.setSize( new Dimension( i, j ) );
		
            super.size( i, j );
        }
    }
    
    public void size ( int i, int j, String klass )
    {
    	size( i, j );
    }
    
    public String exportToCode ()
    {
        String cde = "";
        boolean toCommand = true;
        
        if ( keyPressed && key == CODED && keyCode == SHIFT )
        {
	    toCommand = false;
	    exportArrayNames = new String[0];
        }
        
        if ( selectedShape == null )
        {
	    for ( int i = 0; i < shapes.length; i++ )
    	    {
    	    	if ( toCommand )
	            cde = cde + " " + toCommands(shapes[i]) + "\n";
	        else
	            cde = cde + " " + toArray(shapes[i]) + "\n";
            }
        }
        else
        {
	    if ( toCommand )
		cde = cde + " " + toCommands(selectedShape) + "\n";
	    else
		cde = cde + " " + toArray(selectedShape) + "\n";
        }
        
        if ( !toCommand )
        {	
	    cde = cde + "/*\nvoid drawArray ( float[] arr ) { " +
	    " if ( arr.length == 2 ) line( arr[0],arr[1],arr[2],arr[3] ); else if ( arr.length == 6 ) " +
	    " bezier( arr[0],arr[1],arr[2],arr[3],arr[4],arr[5],arr[6],arr[7] ); };";
        
	    cde = cde + "\nvoid drawArray ( float[][] arr ) { beginShape(); vertex(arr[0][0],arr[0][1]); " + 
	    "for ( int i = 1; i < arr.length; i++ ) { " +
	    " if ( arr[i].length == 2 ) vertex( arr[i][0],arr[i][1] ); else if ( arr[i].length == 6 ) " +
	    " bezierVertex( arr[i][0],arr[i][1],arr[i][2],arr[i][3],arr[i][4],arr[i][5] ); }; endShape(); }\n*/";
    		
	    if ( exportArrayNames != null && exportArrayNames.length > 0 )
		cde = cde + "\nvoid " + uniqueName("drawDrawing_","") + " () {\ndrawArray( " + 
	        join( exportArrayNames, " );\ndrawArray( " ) + " );\n}\n";
        }
        
        if ( !cde.equals("") )
        {
	    int selStart = editor.getSelectionStart();
	    
	    if ( editor.isSelectionActive() )
	    {
		editor.setSelectedText( cde );
	    }
	    else
	    {
		editor.insertText( cde );
	    }
	    
	    editor.setSelection( selStart, selStart + cde.length() );
	}
		
        return cde;
    }
    
    private String toCommands ( RSubshape shape )
    {
    	String cde;
    	
    	if ( shape == null ) return "";
    	
    	RPoint[] pnts = shape.getPoints();
    	
    	if ( shape.commands == null && pnts != null && pnts.length > 0 ) 
    	    return "point( " + pnts[0].x + " , " + pnts[0].x + " );";
    		
    	if ( shape.commands.length == 1 )
    	{
	    RCommand cmd = shape.commands[0];
	    pnts = cmd.getPoints();
	    
	    switch ( cmd.getCommandType() )
	    {
		case RCommand.LINETO:
		    return "line( " + pnts[0].x + " , " + pnts[0].y + " , " +
				      pnts[1].x + " , " + pnts[1].y + " );";
		case RCommand.QUADBEZIERTO:	// needs fix since bezier() is cubic
		    return "bezier( " + pnts[0].x + " , " + pnts[0].y + " , " +
					pnts[1].x + " , " + pnts[1].y + " , " +
					pnts[1].x + " , " + pnts[1].y + " , " +
					pnts[2].x + " , " + pnts[2].y + " );";
		case RCommand.CUBICBEZIERTO:
		    return "bezier( " + pnts[0].x + " , " + pnts[0].y + " , " +
					pnts[1].x + " , " + pnts[1].y + " , " +
					pnts[2].x + " , " + pnts[2].y + " , " +
					pnts[3].x + " , " + pnts[3].y + " );";
	    }
    	}
    	
    	cde = "beginShape();\n";
    	cde = cde + "vertex( " + pnts[0].x + " , " + pnts[0].y + " );\n";
    		
    	for ( int i = 0; i < shape.commands.length; i++ )
    	{
    	    RCommand cmd = shape.commands[i];
    	    pnts = cmd.getPoints();
    		
	    switch ( cmd.getCommandType() )
	    {
		case RCommand.LINETO:
		    cde = cde + "vertex( " + pnts[1].x + " , " + pnts[1].y + " );\n";
		    break;
		case RCommand.QUADBEZIERTO:	// needs fix since bezier() is cubic
		    cde = cde + "bezierVertex( " + pnts[1].x + " , " + pnts[1].y + " , " +
						   pnts[1].x + " , " + pnts[1].y + " , " +
						   pnts[2].x + " , " + pnts[2].y + " );\n";
		    break;
		case RCommand.CUBICBEZIERTO:
		    cde = cde + "bezierVertex( " + pnts[1].x + " , " + pnts[1].y + " , " +
						   pnts[2].x + " , " + pnts[2].y + " , " +
						   pnts[3].x + " , " + pnts[3].y + " );\n";
		    break;
	    }
    	}
    	
    	cde = cde + "endShape();";
    	
    	return cde;
    }
    
    String[] exportArrayNames;
    
    private String toArray ( RSubshape shape )
    {
    	String cde = "";
    	
    	if ( shape == null ) return "";
    	
    	RPoint[] pnts = shape.getPoints();
    	
    	if ( shape.commands == null || pnts == null || pnts.length == 0 ) 
    	    return "";
    		
    	if ( shape.commands.length == 1 )
    	{
	    RCommand cmd = shape.commands[0];
	    pnts = cmd.getPoints();
	    String name = "";
	    
	    switch ( cmd.getCommandType() )
	    {
		case RCommand.LINETO:
			name = uniqueName("line_","");
			cde = "float[] " + name + " = new float[]{ " + 
							  pnts[0].x + " , " + pnts[0].y + " , " +
							  pnts[1].x + " , " + pnts[1].y + " };";
			break;
		case RCommand.QUADBEZIERTO:	// needs fix since bezier() is cubic
			name = uniqueName("bezier_","");
			cde = "float[] " + uniqueName("bezier_","") + " = new float[]{ " + 
								pnts[0].x + " , " + pnts[0].y + " , " +
								pnts[1].x + " , " + pnts[1].y + " , " +
								pnts[1].x + " , " + pnts[1].y + " , " +
								pnts[2].x + " , " + pnts[2].y + " };";
			break;
		case RCommand.CUBICBEZIERTO:
			name = uniqueName("bezier_","");
			cde = "float[] " + uniqueName("bezier_","") + " = new float[]{ " + 
								pnts[0].x + " , " + pnts[0].y + " , " +
								pnts[1].x + " , " + pnts[1].y + " , " +
								pnts[2].x + " , " + pnts[2].y + " , " +
								pnts[3].x + " , " + pnts[3].y + " };";
			break;
	    }
	    
	    if ( !name.equals("") )
	    {
		exportArrayNames = append( exportArrayNames, name );
	    }
	    
	    return cde;
    	}
    	
    	String name = uniqueName("shape_","");
	exportArrayNames = append( exportArrayNames, name );
    	
    	cde = "float[][] " + name + " = new float[][]{\n";
    	cde = cde + "new float[]{ " + pnts[0].x + " , " + pnts[0].y + " },\n";
    		
    	for ( int i = 0; i < shape.commands.length; i++ )
    	{
	    RCommand cmd = shape.commands[i];
	    pnts = cmd.getPoints();
	    
	    switch ( cmd.getCommandType() )
	    {
		case RCommand.LINETO:
			cde = cde + "new float[]{ " + pnts[1].x + " , " + pnts[1].y + " }";
			break;
		case RCommand.QUADBEZIERTO:	// needs fix since bezier() is cubic
			cde = cde + "new float[]{ " + pnts[1].x + " , " + pnts[1].y + " , " +
						    pnts[1].x + " , " + pnts[1].y + " , " +
						    pnts[2].x + " , " + pnts[2].y + " }";
			break;
		case RCommand.CUBICBEZIERTO:
			cde = cde + "new float[]{ " + pnts[1].x + " , " + pnts[1].y + " , " +
						    pnts[2].x + " , " + pnts[2].y + " , " +
						    pnts[3].x + " , " + pnts[3].y + " }";
			break;
	    }
	    if ( i < shape.commands.length-1 )
	    {
		cde = cde + ",\n";
	    }
    	}
    	
    	cde = cde + "};";
    	
    	return cde;
    }
    
    private String uniqueName ( String pre, String post )
    {
	String un = pre + nf((int)random( 10000 ), 5) + post;
	return un;
    }
    
    public void importFromCode ()
    {
    	if ( editor.isSelectionActive() )
    	{
	    String code = editor.getSelectedText();
	    
	    if ( !DParser.parseCode( code, this ) )
	    {
		System.err.println( "Unable to parse code:" );
		System.err.println( code );
	    }
    	}
    }
    
    private RSubshape shapeToAdd;
    private RPoint firstPoint;
    private boolean isTempShape;
    
    public void addShape ( String type, float[] values )
    {
	if ( shapes == null ) shapes = new RSubshape[0];
	
	if ( type.equals( DParser.TYPE_LINE ) )
	{
	    shapes = (RSubshape[])append( shapes, new RSubshape(values[0], values[1]) );
	    shapes[shapes.length-1].addLineTo( values[2], values[3] );
	
	    selectedShape = shapes[shapes.length-1];
	    selectedShapeIndex = shapes.length-1;
	}
	else if ( type.equals( DParser.TYPE_BEZIER ) )
	{
	    shapes = (RSubshape[])append( shapes, new RSubshape(values[0], values[1]) );
	    shapes[shapes.length-1].addBezierTo( values[2], values[3], values[4], values[5], values[6], values[7] );
    
	    selectedShape = shapes[shapes.length-1];
	    selectedShapeIndex = shapes.length-1;
	}
	else if ( type.equals( DParser.TYPE_RECT ) ) // assumes CORNER
	{
	    RPoint org = new RPoint( values[0], values[1] );
	    shapes = (RSubshape[])append( shapes, new RSubshape(org) );	    // top-left
	    shapes[shapes.length-1].addLineTo( values[0] + values[2], values[1] );		        // top-right
	    shapes[shapes.length-1].addLineTo( values[0] + values[2], values[1] + values[3] );  // lower-right
	    shapes[shapes.length-1].addLineTo( values[0], values[1] + values[3] );			    // lower-left
	    shapes[shapes.length-1].addLineTo( org );			    // close
    
	    selectedShape = shapes[shapes.length-1];
	    selectedShapeIndex = shapes.length-1;
	}
	else if ( type.equals( DParser.TYPE_ELLIPSE ) ) // assumes CENTER
	{
	    float w2 = values[2] / 2.0f;
	    float h2 = values[3] / 2.0f;
	    float mbn = 1.81066579454f; // "magic bezier number", picked from illustrator
		
	    RPoint org = new RPoint( values[0], values[1]-h2 );
	    shapes = (RSubshape[])append( shapes, new RSubshape(org) );
	    shapes[shapes.length-1].addBezierTo( values[0] + w2 / mbn, values[1]-h2,
						values[0] + w2, values[1] - h2 / mbn,
						values[0] + w2, values[1] );
	    shapes[shapes.length-1].addBezierTo( values[0] + w2, values[1] + h2 / mbn,
						values[0] + w2 / mbn, values[1] + h2,
    						 values[0], values[1] + h2 );
	    shapes[shapes.length-1].addBezierTo( values[0] - w2 / mbn, values[1] + h2,
						values[0] - w2, values[1] + h2 / mbn,
						values[0] - w2, values[1] );
	    shapes[shapes.length-1].addBezierTo( new RPoint( values[0] - w2, values[1] - h2 / mbn ),
						new RPoint( org.x - w2 / mbn, org.y ),
						org );
    
	    selectedShape = shapes[shapes.length-1];
	    selectedShapeIndex = shapes.length-1;
	}
	else if ( type.equals( DParser.TYPE_BEGINSHAPE ) )
	{
	    if ( shapeToAdd != null )
	    {
		System.err.println( "doodle code parser: \n beginShape() happened twice or endShape() not found" );
		
		shapeToAdd = null;
		firstPoint = null;
		isTempShape = false;
	    }
	    else
	    {
		shapeToAdd = new RSubshape(); // this is a temp shape
		isTempShape = true;
	    }
	}
	else if ( type.equals( DParser.TYPE_VERTEX ) )
	{
	    if ( shapeToAdd != null )
	    {
		if ( isTempShape )
		{
		    firstPoint = new RPoint( values[0], values[1] );
		    shapeToAdd = new RSubshape( firstPoint );
		    isTempShape = false;
		}
		else
		{
		    shapeToAdd.addLineTo( values[0], values[1] );
		}
	    }
	    else
	    {
		System.err.println( "doodle code parser: \n vertex() happened without beginShape()." );
		
		shapeToAdd = null;
		firstPoint = null;
		isTempShape = false;
	    }
	}
	else if ( type.equals( DParser.TYPE_BEZIERVERTEX ) )
	{
	    if ( shapeToAdd != null )
	    {
		if ( isTempShape )
		{
		    System.err.println( "doodle code parser: \n vertex() must be called before bezierVertex()." );
		
		    shapeToAdd = null;
		    firstPoint = null;
		    isTempShape = false;
		}
		else
		{
		    shapeToAdd.addBezierTo( values[0], values[1], values[2], values[3], values[4], values[5] );
		}
	    }
	    else
	    {
		System.err.println( "doodle code parser: \n vertex() happened without beginShape()." );
		
		shapeToAdd = null;
		firstPoint = null;
		isTempShape = false;
	    }
	    
	}
	else if ( type.equals( DParser.TYPE_ENDSHAPE ) )
	{
	    if ( shapeToAdd != null )
	    {
		if ( isTempShape )
		{
		    System.err.println( "doodle code parser: \n no points added but endShape() called." );
	    
		    shapeToAdd = null;
		    firstPoint = null;
		    isTempShape = false;
		}
		else
		{
		    //TODO: have to handle CLOSE differently to account for bezierVertex
		    if ( (int)values[0] == PConstants.CLOSE )
		    {
			    shapeToAdd.addLineTo( firstPoint );
		    }
		    
		    shapes = (RSubshape[])append( shapes, shapeToAdd );
		    
		    shapeToAdd = null;
		    firstPoint = null;
		    isTempShape = false;
		    
		    selectedShape = shapes[shapes.length-1];
		    selectedShapeIndex = shapes.length-1;
		}
	    }
	    else
	    {
		System.err.println( "doodle code parser: \n endShape() happened without beginShape()." );
		
		shapeToAdd = null;
		firstPoint = null;
		isTempShape = false;
	    }
	}
	else
	{
	    println( "doodle code parser: the following was not handled" );
	    println( type );
	    println( values );
	}
    }


    /*****/

    ControlP5 cp5;
    DoodleButton[] toolsBar;
    
    final static int TOOL_SELECT_POINTS = 0;
    final static int TOOL_ADD_POINTS = 1;
    final static int TOOL_ZOOM_MOVE = 2;
    final static int TOOL_DOODLE_LINE = 3;
    
    final String[] toolNames = new String[] {
	"select points", "add points", "zoom and move", "doodle line",
	"import from code", "export to code"
    };
    
    final String[] toolImages = new String[] {
	"select_points.png", "add_points.png", "zoom_move.png",
	"doodle_line.png", "import_from_code.png", "export_to_code.png"
    };
    
    DTool[] tools;
    
    int currentTool = TOOL_ZOOM_MOVE;
    
    RSubshape[] shapes;
    RSubshape selectedShape;
    int selectedShapeIndex;
    
    float globS = 1.0f, globSRev = 1.0f;
    float globX = 0, globY = 0;
    
    public void setup ()
    {
	setupDoodle ();
    
	size( 500, 500 );
	globX = width/2;
	globY = height/2;
	
	cp5 = new ControlP5( this );
	
	toolsBar = new DoodleButton[toolNames.length];
	
	tools = new DTool[] {
	    new SelectEditMovePointTool(), 
	    new AddRemovePointTool(), 
	    new ZoomMovePaperTool(), 
	    new LineDoodleTool()
	};
	
	for ( int i = 0; i < toolsBar.length; i++ )
	{
	    if ( i < 4 )
		    toolsBar[i] = new DoodleButton( toolNames[i], 1, 1+i*21 );
	    else if ( i == 4 )
	    {    
			    toolsBar[i] = new DoodleActionButton ( toolNames[i], 1, 1+i*21  ) {
				    public void run () {
					    importFromCode();
				    }
			    };
	    
	    }
	    else if ( i == 5 )
	    {
			    toolsBar[i] = new DoodleActionButton ( toolNames[i], 1, 1+i*21  ) {
				    public void run () {
					    exportToCode();
				    }
			    };
	    
	    }
	    toolsBar[i].setId( i );
	    toolsBar[i].img = loadImage( toolImages[i] );
	}
	
	RGeomerative.init( this );
	shapes = new RSubshape[0];
    }
    
    public void draw ()
    {
	    checkChanges();
	    
	background( 255 );
	
	pushMatrix();
	    scale( globS );
	    translate( globX, globY );
	    drawCenter();
	    
	    noFill();
	    stroke( 1*globSRev );
	    for ( int i = 0; i < shapes.length; i++ )
	    {
		shapes[i].draw();
	    }
	    
	    tools[currentTool].draw();
	    
	popMatrix();
	
	noStroke();
	fill( 35 );
	rectMode( CORNER );
	rect( 0,0, 22, height );
    }
    
    public void mousePressed ()
    {
	if ( mouseX <= 22 ) return;
	wasDragged = false;
	
	tools[currentTool].mousePressed();
    }
    
    boolean wasDragged = false;
    public void mouseDragged ()
    {
	if ( mouseX <= 22 ) return;
	wasDragged = true;
	
	tools[currentTool].mouseDragged();
    }
    
    public void mouseReleased ()
    {
	if ( mouseX <= 22 ) return;
	
	tools[currentTool].mouseReleased();
    }
    
    public void keyPressed ()
    {
	    
	    switch ( key )
	    {
		    case 'o':
			    exportToCode();
			    break;
		    case 'i':
			    importFromCode();
			    break;
	    }
    
	if ( key == DELETE || key == BACKSPACE )
	{
	    if ( !mousePressed ) // make sure we're not using a tool at that moment
	    {
		    if ( selectedShape != null &&
		     shapes[selectedShapeIndex] == selectedShape )
		{
			shapes = (RSubshape[])remove(shapes, selectedShapeIndex);
		    selectedShapeIndex = -1;
			selectedShape = null;
		    }
		    else
		    {
			    //shapes = new RSubshape[0];
		    shapes = (RSubshape[])remove(shapes, shapes.length-1);
		    selectedShapeIndex = -1;
			selectedShape = null;
		    }
	    }
	}
	
	if ( key == ESC )
	{
	    key = 0; // prevent ESC from quitting Processing altogether since PApplet calles System.exit(0)
	}
	
	tools[currentTool].keyPressed();
    }

    // additional Array functions
    
    public Object remove ( Object arr, int index )
    {
    	if ( arr == null ) return arr;
    
        int arr_length = java.lang.reflect.Array.getLength( arr );
        if ( arr_length == 0 ) return arr;
        
        Class type = arr.getClass().getComponentType();
        Object arr2 = null;
        
        if ( arr_length == 1 )
            arr2 = java.lang.reflect.Array.newInstance(type, 0);
        else 
        {
            arr2 = java.lang.reflect.Array.newInstance(type, arr_length-1);
            if ( index == 0 )
                System.arraycopy( arr, 1, arr2, 0, arr_length-1 );
            else if ( index == arr_length-1 )
                System.arraycopy( arr, 0, arr2, 0, arr_length-1 );
            else
            {
                System.arraycopy( arr, 0, arr2, 0, index );
                System.arraycopy( arr, index+1, arr2, index, arr_length-(index+1) );
            }
        }
        
        return arr2;
    }
    
    class LineDoodleTool
    extends DTool
    {
	long lastMillis = 0;
	long minMillis = 50;
	
	RPoint[] points;
	
	public void mousePressed ()
	{
	    float mx = mouseX * globSRev - globX;
	    float my = mouseY * globSRev - globY;
	    
	    points = new RPoint[] {
		new RPoint( mx, my )   
	    };
	}
	
	public void mouseDragged ()
	{
	    float mx = mouseX * globSRev - globX;
	    float my = mouseY * globSRev - globY;
		
	    if ( points.length > 0 )
	    {
		
		if ( System.currentTimeMillis() - lastMillis > minMillis
		    && dist(points[points.length-1].x, points[points.length-1].y, mx, my) > 2
		    )
		{
		    points = (RPoint[])append(points, new RPoint( mx, my ) );
		    lastMillis = System.currentTimeMillis();
		}
	    }
	    else
		points = (RPoint[])append(points, new RPoint( mx, my ) );
	}
	
	public void mouseReleased ()
	{
	    if ( points != null && points.length > 1 )
	    {
		    RSubshape shape;
		    
		    if ( selectedShape == null )
			shape = new RSubshape( points[0] );
		    else
			    shape = selectedShape;
		    
		if ( keyPressed && key == CODED && keyCode == ALT )
		{
		    for ( int i = 1; i < points.length; i++ )
		    {
			shape.addLineTo( points[i] );
		    }
		}
		else
		{
		    int i, n, nexti;
		    n = points.length;
		    if ( n > 2 )
		    {
			double x[] = new double[n];
			double y[] = new double[n];
		
			for( i = 0; i < n; i++ ) {
			    x[i] = points[i].x;
			    y[i] = points[i].y;
			}
		
			double cx[] = new double[0];
			double cy[] = new double[0];
		
			for ( int nn = 0; nn < n; nn+=10 )
			{
			    int l = n - nn;
			    if ( l > 10 ) l = 10;
		
			    if ( l > 2 )
			    {
				cx = (double[])concat( cx, CurveFitting.findCoefs( (double[])subset(x,nn,l), l-1 ) );
				cy = (double[])concat( cy, CurveFitting.findCoefs( (double[])subset(y,nn,l), l-1 ) );
			    }
			    else
			    {
				double[] xf = CurveFitting.findCoefs( (double[])subset(x,n-10,10), 9 );
				cx = (double[])concat( cx, (double[])subset( xf, xf.length-l, l) );
				double[] yf = CurveFitting.findCoefs( (double[])subset(y,n-10,10), 9 );
				cy = (double[])concat( cy, (double[])subset( yf, yf.length-l, l) );
			    }
			    
			    cx[0] = (x[1]-x[0]) / 3.0f;
			    cy[0] = (y[1]-y[0]) / 3.0f;
			    cx[1] = 2.0f * (x[1]-x[0]) / 3.0f;
			    cy[1] = 2.0f * (y[1]-y[0]) / 3.0f;
			}
    
			for( i = 1; i < (n-1); i++ )
			{
			    nexti = ( i + 1 ) % n;
		
			    shape.addBezierTo(
				(float)(x[i] + cx[i]), (float)(y[i] + cy[i]),
				(float)(x[nexti] - cx[nexti]), (float)(y[nexti] - cy[nexti]),
				points[nexti].x, points[nexti].y );
			}
		    }
		}
		
		if ( selectedShape == null )
		{
			shapes = (RSubshape[])append(shapes, shape);
			selectedShape = shapes[shapes.length-1];
		    selectedShapeIndex = shapes.length-1;
		    }
	    }
		    else
		    {
			    selectedShape = null;
			    selectedShapeIndex = -1;
		    }
	    
	    points = new RPoint[0];
	}
	
	public void draw ()
	{
	    if ( points != null && points.length > 1 )
	    {
		for ( int i = 1; i < points.length; i++ )
		{
		    stroke( 0 );
		    noFill();
		    strokeWeight( 1*globSRev );
		    line(points[i-1].x,points[i-1].y,points[i].x,points[i].y);
		}
	    }
	    
	    if ( !mousePressed )
		    drawHandles();
	}
    }

    class AddRemovePointTool
    extends DTool
    {
	RPoint clicked;
	
	RSubshape shapeToAdd;
	RPoint fromPoint;
	
	RSubshape lastShape;
	RPoint lastClicked;
	
	public void mousePressed ()
	{
	    float mx = mouseX * globSRev - globX;
	    float my = mouseY * globSRev - globY;
	    clicked = new RPoint( mx, my );
	    
	    if ( selectedShape != null )
	    {            
		RPoint[] pnts = selectedShape.getPoints();
		if ( pnts != null )
			fromPoint = pnts[pnts.length-1];
			
		    else if ( lastShape == selectedShape && lastClicked != null )
		    {
			    fromPoint = lastClicked;
		    }
	    }
	    else
	    {
		shapeToAdd = null;
		fromPoint = null;
		lastClicked = null;
		lastShape = null;
	    }
	}
    
	public void mouseDragged ()
	{
	    float mx = mouseX * globSRev - globX;
	    float my = mouseY * globSRev - globY;
	    
	    if ( selectedShape != null && fromPoint != null )
	    {
		shapeToAdd = new RSubshape(fromPoint);
		shapeToAdd.addBezierTo( fromPoint.x, fromPoint.y, 
					clicked.x - (mx-clicked.x), clicked.y - (my-clicked.y), 
					clicked.x, clicked.y );
	    }
	}
	
	public void mouseReleased ()
	{
	    float mx = mouseX * globSRev - globX;
	    float my = mouseY * globSRev - globY;
	    
	    if ( selectedShape != null )
	    {
		if ( !wasDragged )
		{
		    selectedShape.addLineTo( mx, my );
		}
		else
		{
		    selectedShape.addBezierTo( fromPoint.x, fromPoint.y, 
					       clicked.x - (mx-clicked.x), clicked.y - (my-clicked.y), 
					       clicked.x, clicked.y );
		}
		
		shapeToAdd = null;
		fromPoint = null;
		clicked = null;
		lastClicked = null;
		lastShape = null;
	    }
	    else
	    {
		    if ( shapes == null )
		    {
			    shapes = new RSubshape[] {
				    new RSubshape( clicked.x, clicked.y )
			    };
		    }
		    else
		    {
			    shapes = (RSubshape[])append( shapes, new RSubshape( clicked.x, clicked.y ) );
		    }
		    
		    lastClicked = clicked;
		    lastShape = shapes[shapes.length-1];
			    
		selectedShape = shapes[shapes.length-1];
		selectedShapeIndex = shapes.length-1;
	    }
	}
	
	public void draw ()
	{
	    stroke( 0 );
	    noFill();
	    strokeWeight( 1*globSRev );
	    rectMode( CENTER );
				    
	    if ( shapeToAdd != null )
	    {
		if ( wasDragged )
		{
		    shapeToAdd.draw();
		    
		    RPoint[] pnts = shapeToAdd.getPoints();
		    if ( pnts != null )
		    {
			fill( 255 );
			for ( int i = 0; i < pnts.length; i++ )
			{
			    rect( pnts[i].x, pnts[i].y, 5*globSRev, 5*globSRev);
			}
		    }
		}
		else  if ( clicked != null )
		{
		    if ( fromPoint != null )
			line( fromPoint.x, fromPoint.y, clicked.x, clicked.y );
		    
		    fill( 255 );
			    
		    rect( clicked.x, clicked.y, 5*globSRev, 5*globSRev);
		}
	    }
	    else if ( clicked != null )
	    {
		    if ( fromPoint != null )
			    line( fromPoint.x, fromPoint.y, clicked.x, clicked.y );
		    
			    fill( 255 );
			    
			    rect( clicked.x, clicked.y, 5*globSRev, 5*globSRev);
	    }
	    
	    drawHandles();
	}
    }

    class SelectEditMovePointTool
    extends DTool
    {
	float[] clicked;
	RCommand[] selectedSegments;
	RPoint selectedPoint;
	int selectedPointType;
	
	public void mousePressed ()
	{
	    float mx = mouseX * globSRev - globX;
	    float my = mouseY * globSRev - globY;
	    
	    selectedSegments = new RCommand[2];
	    selectedPoint = null;
	    
	    for ( int i = 0; i < shapes.length; i++ )
	    {
		for ( int ii = 0; ii < shapes[i].commands.length; ii++ )
		{
		    RPoint[] pnts = shapes[i].commands[ii].getPoints();
		    for ( int iii = 0; iii < pnts.length; iii++ )
		    {
			float cd = (5*globSRev);
			if ( pnts[iii].x > mx - cd && pnts[iii].x < mx + cd && 
			     pnts[iii].y > my - cd && pnts[iii].y < my + cd )
			{
			    selectedPointType = iii;
			    selectedPoint = pnts[iii];
			    selectedShape = shapes[i];
			    selectedShapeIndex = i;
			    clicked = new float[]{ mx, my };
			    selectedSegments[0] = shapes[i].commands[ii];
			    if ( ii == 0 && iii < 2 )
			    {
				selectedSegments[1] = shapes[i].commands[shapes[i].commands.length-1];
			    }
			    else if ( ii+1 < shapes[i].commands.length )
			    {
				selectedSegments[1] = shapes[i].commands[ii+1]; 
			    }
			    return;   
			}
		    }
		}
	    }
	    selectedSegments = null;
	    selectedShape = null;
	}
	
	public void mouseDragged ()
	{
	    moveSelected( (mouseX-pmouseX)*globSRev, (mouseY-pmouseY)*globSRev );
	}
	
	public void keyPressed ()
	{
	    float md = 1.0f*globSRev;
	    if ( key == CODED )
	    {
		switch ( keyCode )
		{
		    case UP:
			moveSelected(   0, -md );
			break;
		    case DOWN:
			moveSelected(   0,  md );
			break;
		    case LEFT:
			moveSelected( -md,  0  );
			break;
		    case RIGHT:
			moveSelected(  md,  0  );
			break;
		}   
	    }
	}
	
	private void moveSelected ( float xd, float yd )
	{
	    if ( selectedSegments != null && selectedSegments.length > 0 )
	    {
		selectedPoint.x += xd;
		selectedPoint.y += yd;
		RPoint[] pnts = selectedSegments[0].getPoints();
		switch ( selectedSegments[0].getCommandType() )
		{
		    case RCommand.CUBICBEZIERTO:
			if ( selectedPointType == 3 )
			{
			    pnts[2].x += xd;
			    pnts[2].y += yd;
			}
		}
		
		if ( selectedSegments[1] != null )
		{
		    pnts = selectedSegments[1].getPoints();
		    switch ( selectedSegments[1].getCommandType() )
		    {
			case RCommand.CUBICBEZIERTO:
			    if ( selectedPointType == 3 )
			    {
				pnts[1].x += xd;
				pnts[1].y += yd;
			    }
		    }
		}
	    }
	}
	
	public void mouseReleased ()
	{
	    float mx = mouseX * globSRev - globX;
	    float my = mouseY * globSRev - globY;
	    
	    if ( !wasDragged )
	    {
	    }
	}
	
	public void draw ()
	{        
	    drawHandles();
	   
	   stroke( 0 );
	   noFill();
	   strokeWeight( 1*globSRev );
	   rectMode( CENTER );
	   
	    if ( selectedShape != null && selectedSegments != null && selectedSegments.length > 0 )
	    {
		for ( int i = 0; i < selectedSegments.length; i++ )
		{
		    if ( selectedSegments[i] == null ) continue;
		    RPoint[] pnts = selectedSegments[i].getPoints();
		    switch ( selectedSegments[i].getCommandType() )
		    {
			case RCommand.LINETO:
			    break;
			case RCommand.QUADBEZIERTO:
			    line( pnts[0].x, pnts[0].y, pnts[1].x, pnts[1].y );
			    line( pnts[1].x, pnts[1].y, pnts[2].x, pnts[2].y );
			    break;
			case RCommand.CUBICBEZIERTO:
			    line( pnts[0].x, pnts[0].y, pnts[1].x, pnts[1].y );
			    line( pnts[2].x, pnts[2].y, pnts[3].x, pnts[3].y );
			    break;
		    }
	   
			    fill( 255 );
		    
		    for ( int ii = 0; ii < pnts.length; ii++ )
			rect(pnts[ii].x, pnts[ii].y, 5*globSRev, 5*globSRev);
		}
	    }
	}
	
	public void deactivated ()
	{
	    selectedSegments = null;
	}
    }

    class ZoomMovePaperTool
    extends DTool
    {
	public void mouseDragged ()
	{
	    globX += (mouseX-pmouseX)*(1/globS);
	    globY += (mouseY-pmouseY)*(1/globS);
	}
	
	public void mouseReleased ()
	{
	    if ( !wasDragged )
	    {
		if ( !keyPressed )
		    globS *= 1.1f;
		else if ( keyPressed && key == CODED && keyCode == ALT )
		    globS *= 0.9f;
		globSRev = 1/globS;
	    }
	}
	
	public void doubleClicked ()
	{
	    globS = 1.0f; globSRev = globS;
	    globX = width/2; globY = height/2;
	}
    }

    /**
     *    DTool is a superclass / interface for all the tools / pens on the toolbar.
     */

    class DTool
    {
	// mouse handling
	
	public void mousePressed () {}
	
	public void doubleClicked () {}
	
	public void mouseDragged () {}
	
	public void mouseReleased () {}
	
	
	// keyboad handling
	
	public void keyPressed () {}
	
	public void keyReleased () {}
	
	
	// additional drawing
	
	public void draw () {}
	
	
	// toolbar activities
	
	public void activated () {}
	
	public void deactivated () {}
    }


    public void drawCenter ( )
    {
       stroke( 0 );
       strokeWeight( 1*globSRev );
       line( -5*globSRev,0,5*globSRev,0 );
       line( 0,-5*globSRev,0,5*globSRev );
    }

    public void drawHandles ()
    {
       stroke( 0 );
       fill( 255 );
       strokeWeight( 1*globSRev );
       rectMode( CENTER );
       
       if ( selectedShape != null )
       {
	   RSubshape shp = selectedShape;
	   RPoint[] pnts = null;
	   if ( shp.commands != null )
	   {
	       for ( int iii = 0; iii < shp.commands.length; iii++ )
	       {
		    pnts = shp.commands[iii].getPoints();
		    rect(pnts[0].x, pnts[0].y, 5*globSRev, 5*globSRev);
	       }       
	       rect(pnts[pnts.length-1].x, pnts[pnts.length-1].y, 5*globSRev, 5*globSRev);
	   }
       }
    }

    long lastClick = 0;
    Controller lastController;

    public void controlEvent ( ControlEvent ce )
    {
	String type = ce.controller().getClass().getName();
	
	//println( ce.controller().label() );
	//println( type );
	
	if ( ce.isController() )
	{
	    if ( type.endsWith("$DoodleButton") )
	    {
		int nextTool = ce.controller().id();
		
		if ( nextTool != currentTool ) 
		    tools[currentTool].deactivated();
	    
		currentTool = nextTool;
		//println( "Setting tool to: " + toolNames[currentTool] );
	    
		if ( lastController == ce.controller() && System.currentTimeMillis() - lastClick < 500 )
		{
		    tools[currentTool].doubleClicked();
		}
		else
		{
		    tools[currentTool].activated();   
		}
		lastController = ce.controller();
	    }
	    else /*if ( type.endsWith("$DoodleActionButton") )*/
	    {
		((DoodleActionButton)ce.controller()).run();
	    }
	}
	lastClick = System.currentTimeMillis();
    }

    class DoodleActionButton
    extends DoodleButton
    {
	public DoodleActionButton ( String _nam, int _x, int _y )
	{
	    super(cp5, (Tab)cp5.getTab("default"), _nam, _x, _y);
	}
	
	public void run () { }
    }


    class DoodleButton
    extends controlP5.Button
    {
	String name;
	processing.core.PImage img;
    
	public DoodleButton ( String _nam, int _x, int _y )
	{
	    this(cp5, (Tab)cp5.getTab("default"), _nam, _x, _y);
	}
    
	public DoodleButton ( ControllerGroup _grp,
	String _nam,
	int _x, int _y )
	{
	    this(cp5, _grp, _nam, _x, _y);   
	}
    
	public DoodleButton ( ControlP5 _cp5,
			      ControllerGroup _grp,
			      String _nam,
			      int _x, int _y )
	{
	    super(_cp5, _grp, _nam, 10, _x, _y, 1, 1);
	    _cp5.register( this );
	    name = _nam;
	    width = 20;
	    height = 20;
	}
    
	public void draw( PApplet applet )
	{
	    if( isVisible() )
	    {
		applet.pushMatrix();
		applet.noStroke();
		applet.translate(position.x, position.y);
    
		if ( isInside || toolsBar[currentTool] == this )
		{
		    if ( isPressed || toolsBar[currentTool] == this )
		    {
			applet.fill( 200 );
		    }
		    else
		    {
			applet.fill( 180 );
		    }
		}
		else
		{
		    applet.fill( 100 );
		}
    
		applet.rect( 0,0, width, height );
    
		applet.fill( 0 );
		//applet.textFont( bFonts.lucGraBol15 );
		//applet.text( name, bStyle.buttonPadding, bStyle.buttonHeight-bStyle.buttonPadding );
		
		applet.image( img, 0,0 );
    
		applet.popMatrix();
	    }
	}
    }
}

/*******/

class CurveFitting
{
    // Curve Fitting by Atanu Mohanty with minor changes "sampled" from:
    // http://www.serc.iisc.ernet.in/~amohanty/SE288/lagrange/curves.html
    //

    public static double[] findCoefs ( double a[], int n )
    {
        int i, j;
        double b[] = new double[n+1];
        double r[] = new double[n+1];
        double c[] = new double[n+1];

        b[0] = 2 * ( a[1] - a[0] ) / 3.0;
        for( i = 1; i < n; i++ )
            b[i] = a[i+1] - a[i-1];
        b[n] = 2 * ( a[n] - a[n-1] ) / 3.0;

        r[n] = 1;
        r[n-1] = 1;
        for( i = n-2; i >= 0; i-- )
            r[i] = 4 * r[i+1] - r[i+2];
        for( i = 1; i<=n; i+=2 )
            r[i] = -r[i];

        c[0] = 0.0;
        for( i = n; i>=0; i-- )
            c[0] += r[i] * b[i];
        c[0] /= (r[0] + r[1]);
        c[1] = b[0] - c[0];
        for( i = 1; i < n; i++ )
            c[i+1] = b[i] - 4 * c[i] - c[i-1];
        return c;
    }   
}
