package net.lagerwey.nzb;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

/**
 * A dialog that displays the message string from a <code>Throwable</code>. It
 * features an <i>extended</i> state that shows the stack trace and messages of
 * the <code>Throwable</code> and, if available, its cause.   
 * 
 * @author Jos Lagerweij
 * 
 */
class ExceptionDialog extends JDialog {
	private static final int MINIMUM_WIDTH = 450;
	
	/**
	 * Boolean indicating the extended state of the dialog
	 */
	private boolean extended;
	
	/**
	 * The button that switches between non-extended and extended state
	 */
	private JButton detailsButton;
	
	/**
	 * The tabbed pane that displays the detailed message and stack trace of the
	 * <code>Throwable</code> displayed by this dialog.
	 */
	private JTabbedPane detailTabPane;
	
	/**
	 * Creates a new <code>ExceptionDialog</code> with the specified 
	 * <code>JFrame</code> as its parent, a message to display and the
	 * <code>Throwable</code> to display details for. The initial state of the
	 * dialog is not extended (i.e. it shows no detailed information on the
	 * given throwable).
	 * 
	 * @param parent   the parent <code>JFrame</code> of this dialog
	 * @param t        the <code>Throwable</code> to display details for
	 * @param message  the message to display
	 */
	public ExceptionDialog( JFrame parent, Throwable t, String message ) {
		super( parent );
		initUI( t, message );
	}
	
	/**
	 * Creates a new <code>ExceptionDialog</code> with the specified 
	 * <code>JDialog</code> as its parent, a message to display and the
	 * <code>Throwable</code> to display details for. The initial state of the
	 * dialog is not extended (i.e. it shows no detailed information on the
	 * given throwable).
	 * 
	 * @param parent   the parent <code>JDialog</code> of this dialog
	 * @param t        the <code>Throwable</code> to display details for
	 * @param message  the message to display
	 */
	public ExceptionDialog( JDialog parent, Throwable t, String message ) {
		super( parent );
		initUI( t, message );
	}
	
	/**
	 * Creates a new <code>ExceptionDialog</cdoe> with no parent that displays
	 * the specified message and details of the given <code>Throwable</code> The
	 * initial state of the dialog is not extended (i.e. it shows no detailed 
	 * information on the given throwable).
	 * 
	 * @param t        the <code>Throwable</code> to display details for
	 * @param message  the message to display
	 */
	public ExceptionDialog( Throwable t, String message ) {
		super();
		initUI( t, message );
	}
	
	/**
	 * Sets the extended state of this dialog.
	 * 
	 * @param extended  boolean indicating the extended state of this dialog
	 */
	public void setExtended( boolean extended ) {
		this.extended = extended;
		
		if ( extended ) {
			detailsButton.setText( getShowNoDetailsText() );
		}
		else {
			detailsButton.setText( getShowDetailsText() );
		}
		
		detailTabPane.setVisible( extended );
		pack();
	}
	
	/**
	 * Returns wether or not this dialog is in its extended state
	 * 
	 * @return  <code>true</code> if this dialog is in its extended state, 
	 *          <code>false</code> otherwise
	 */
	public boolean isExtended() {
		return extended;
	}
	
	/**
	 * Initializes the UI of the dialog with the specified <code>Throwable</code>
	 *  and display message. If <code>message</code> is null, the message of the 
	 *  <code>Throwable</code> will be used.
	 * 
	 * @param t        the <code>Throwable</code> to initialize the UI with
	 * @param message  the message to display
	 */
	private void initUI( Throwable t, String message ) {
		setTitle( getTitleText( t ) );
		setModal( true );
		setResizable( true );
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );		
		
		if ( message == null ) {
			message = getMessageFromThrowable( t );
		}
		
		Rectangle screenBounds = getScreenBounds( getOwner() );
		
		int minMessageWidth = MINIMUM_WIDTH;
		int maxMessageWidth = (int)(screenBounds.width * 0.9); 
		
		Icon icon = getWarningIcon();
		JLabel iconLabel = new JLabel();
		if ( icon != null ) {
			iconLabel.setIcon(icon);
			
			Dimension iconSize = iconLabel.getPreferredSize();
			minMessageWidth -= iconSize.width;
			maxMessageWidth -= iconSize.width;			
		}
		JComponent messageComponent = getMessageComponent( message, minMessageWidth, maxMessageWidth );
		detailsButton = new JButton( getShowDetailsText() );
		detailsButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				setExtended( !extended );
			}
		});
		JButton okButton = new JButton( "OK" );
		okButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				dispose();
			}
		});		
		detailTabPane = new JTabbedPane();
		detailTabPane.setVisible( false );
		detailTabPane.setPreferredSize( new Dimension( MINIMUM_WIDTH, 150 ) );
		
		JTextArea detailMessageArea = new JTextArea( getDetailedText( t ) );		
		detailMessageArea.setLineWrap( true );
		detailMessageArea.setWrapStyleWord( true );
		detailMessageArea.setEditable( false );
		detailTabPane.add( new JScrollPane( detailMessageArea ), "Details" );
		
		JTree stackTraceTree = new JTree( getStackTraceTree( t ) );
		stackTraceTree.setCellRenderer( new StackTraceTreeCellRenderer() );
		detailTabPane.add( new JScrollPane( stackTraceTree ), "Stack trace" );
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout( layout );
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(iconLabel)
								.addGap(10)
								.addComponent(messageComponent)
								)
						.addGroup(layout.createSequentialGroup()
								.addComponent(detailsButton)
								.addComponent(okButton)
								)
						)
				.addComponent(detailTabPane)
				)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(iconLabel)
				.addComponent(messageComponent, Alignment.CENTER)
				)
			.addGroup(layout.createParallelGroup()
				.addComponent(detailsButton)
				.addComponent(okButton)
				)
			.addComponent(detailTabPane)
			);
		
		pack();
		
		setLocation( screenBounds.x + (screenBounds.width - getWidth() ) / 2, screenBounds.y + (screenBounds.height - getHeight()) / 2 );
		
		okButton.requestFocus();
	}

	/* Helper methods */
	
	/**
	 * Returns the bounds of the device displaying the specified 
	 * <code>Window</code>. Returns the bounds of the device displaying this
	 * dialog if <code>window</code> is null.
	 * 
	 * @param window  the window to get the device bounds for
	 * 
	 * @return  the bounds of the device displaying the specified window
	 */
	private Rectangle getScreenBounds( Window window ) {
		Rectangle screenBounds;
		
		if ( window == null ) {
			screenBounds = getGraphicsConfiguration().getBounds();
		}
		else {
			screenBounds = window.getGraphicsConfiguration().getBounds();
		}
		
		return screenBounds;
	}
	
	/**
	 * Returns a suitable component for displaying the specified message. The
	 * component should be at least <code>minWidth</code> pixels and at most
	 * <code>maxWidth</code> pixels wide.
	 * 
	 * @param message   the message to display
	 * @param minWidth  the minimum width of the component
	 * @param maxWidth  the maximimum width o fthe component
	 * 
	 * @return  a suitable component for displaying the specified message
	 */
	private JComponent getMessageComponent( String message, int minWidth, int maxWidth ) {
		JLabel label = new JLabel( message );
		if ( label.getPreferredSize().width < maxWidth ) {
			return label;
		}
		else {
			//Label is bigger than maximum width. Create a scrolling text pane
			//that can wrap the message.
			JTextArea textPane = new JTextArea( message );
			textPane.setEditable( false );
			textPane.setLineWrap( true );
			textPane.setWrapStyleWord( true );
			textPane.setBackground( getContentPane().getBackground() );
			
			JScrollPane scrollPane = new JScrollPane( textPane );
			scrollPane.setPreferredSize( new Dimension( minWidth, 75 ) );
			return scrollPane;
		}
	}
	
	/**
	 * Returns the className of the class of the provided object without its
	 * package name. 
	 * 
	 * @param o  the object to return the class name for
	 * 
	 * @return  the class name of the specified object without its package name
	 */
	private String getSimpleClassName( Object o )	{
		if (o == null) {
			return null;
		}
	    String className = o.getClass().getName();
	    int lastPeriod = className.lastIndexOf( "." );
	    
	    String simpleClassName;
	    if ( lastPeriod == -1 ) {
	    	simpleClassName = className;
	    }
	    else {
	    	simpleClassName = className.substring( lastPeriod + 1 );
	    }
	    
	    return simpleClassName;
	}

	/* Methods that can be overridden to customize the dialog */
	
    /**
     * Constructs a string containing the information from a StackTraceElement
     * object.
     * 
     * @param element  an element of the stacktrace
     * 
     * @return  a string representation of the given element
     */
    protected String getStackTraceElementString( StackTraceElement element )
    {
        String str = element.getClassName() + "." + element.getMethodName();
        
        if ( !element.isNativeMethod() && element.getFileName() != null )             
            str += " (" + element.getFileName() + ":" + element.getLineNumber() + ")";

        return str;
    }
	
	/**
	 * Constructs a display message from the specified <code>Throwable</code>.
	 * If the given throwable object has a <code>null</code> message, this
	 * method returns the class name of the throwable.
	 * 
	 * @param t  the throwable to get the display message for
	 *  
	 * @return  a display message string based on the specified throwable
	 */
	protected String getMessageFromThrowable( Throwable t ) {
		if (t == null) {
			return null;
		}
        String message = t.getMessage();
        if ( message == null )
            message = t.getClass().getName();

        return message;
	}
	
    /**
     * Retreives the warning icon from the look and feel defaults
     * 
     * @return  the warning icon from the current look and feel
     */
    protected Icon getWarningIcon() {
        return UIManager.getLookAndFeelDefaults().getIcon( "OptionPane.warningIcon" );        
    }

	/**
	 * Returns the text to display on the 'show details' button when no details
	 * are showing.
	 * 
	 * @return  a string to display when no details are showing
	 * 
	 * @see #getShowNoDetailsText()
	 */
    protected String getShowDetailsText() {
		return "Details >>";
	}
	
	/**
	 * Returns the text to show on the 'show details' button when details are
	 * showing.
	 * 
	 * @return a string to display when details are showing
	 * 
	 * @see #getShowDetailsText()
	 */
    protected String getShowNoDetailsText() {
		return "<< Details";
	}
	
	/**
	 * Returns the title for this <code>ExceptionDialog</code> based on the
	 * specified <code>Throwable</code>.
	 * 
	 * @param t  the <code>Throwable</code> this dialog is showing information on
	 * 
	 * @return  the title of this dialog based on the specified throwable
	 */
    protected String getTitleText( Throwable t ) {
		return getSimpleClassName( t );
	}
    
    /**
     * Returns a string containing the message strings of the specified
     * <code>Throwable</code> and, if available, its cause.
     * 
     * @param t  the <code>Throwable</code> to construct a detailed message
     * 
     * @return  the detailed message
     */
    protected String getDetailedText( Throwable t ) {
        StringBuffer details = new StringBuffer();
        	
        details.append( getSimpleClassName( t ) );
        if ( t != null &&  isNotEmpty( t.getMessage() ) ) {
        	details.append( ":\n" );
        	details.append( t.getMessage() );
        	details.append( "\n" );
        }        
        
        while ( t != null && t.getCause() != null )
        {
        	details.append( "\n" );
        	t = t.getCause();
            details.append( "caused by: " );
            details.append( getSimpleClassName( t ) );
            if ( isNotEmpty( t.getMessage() ) ) {
            	details.append( ":\n" );
            	details.append( t.getMessage() );
            	details.append( "\n" );
            }            
        }
        
        return details.toString();
    }
    
    public static boolean isEmpty(String str)
    {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }
    
    /**
     * Creates a tree structure based on the stack trace of the specified 
     * <code>Throwable</code>. The nested exceptions are represented as branches
     * of the root.
     * 
     * @param t  
     *   the stack trace of this <code>Throwable</code> is used to build the 
     *   tree
     * 
     * @return  the root node of the stack trace tree
     */
    protected TreeNode getStackTraceTree( Throwable t ) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Stack trace" );
        while( t != null )
        {
            StackTraceElement[] stack = t.getStackTrace();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode( t.getClass() );
            
            if ( isNotEmpty( t.getMessage() ) ) {
            	child.add( new DefaultMutableTreeNode( t.getMessage() ) );
            }
            
            for ( int i = 0; i < stack.length; i++ )
                child.add( new DefaultMutableTreeNode( stack[i] ) );

            root.add( child );
            t = t.getCause();
        }
        
        return root;
    }
    
    static Window getWindowForComponent(Component parentComponent) throws HeadlessException {
    	if (parentComponent == null) {
    		return null;
    	}
    	if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
    		return (Window)parentComponent;
    	return ExceptionDialog.getWindowForComponent(parentComponent.getParent());
    }

	
	/* Static show methods */
	
	public static void showExceptionDialog( Throwable t ) {
		showExceptionDialog( t, (String)null );
	}
	
	public static void showExceptionDialog( Component parentComponent, Throwable t ) {
		String message = null;
		if (t != null) {
			t.getMessage();
		}
		showExceptionDialog( parentComponent, t, message );
	}
	
	public static void showExceptionDialog( Throwable t, String message ) {
		new ExceptionDialog( t, message ).setVisible( true );
	}
	
	public static void showExceptionDialog( Component parentComponent, Throwable t, String message ) {
		Window window = getWindowForComponent( parentComponent );
		if ( window instanceof JDialog ) {
			new ExceptionDialog( (JDialog)window, t, message ).setVisible( true );
		}
		else if ( window instanceof JFrame ) {
			new ExceptionDialog( (JFrame)window, t, message ).setVisible( true );
		}
		else {
			new ExceptionDialog( t, message ).setVisible( true );
		}
	}
	
	//*** Inner classes ***//
	
	private class StackTraceTreeCellRenderer extends DefaultTreeCellRenderer {
		Icon descriptionIcon;
		
		public StackTraceTreeCellRenderer() {
			descriptionIcon = new DescriptionIcon();
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			
			if ( node.getUserObject() instanceof Class ) {
				setText( ((Class<?>)node.getUserObject()).getName() );
			}
			
			if ( node.getUserObject() instanceof String && !node.isRoot() ) {
				setIcon( descriptionIcon );
			}
			
			return this;
		}
		
	}
	
	private static class DescriptionIcon implements Icon {

		public int getIconHeight() {
			return 16;
		}

		public int getIconWidth() {
			return 16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor( Color.white );
			g.fillRect( x + 2, y, 12, 15 );
			g.setColor( Color.black );
			g.drawRect( x + 2, y, 12, 15 );
			g.drawLine( x + 4, y + 3, x + 6, y + 3 );
			g.drawLine( x + 4, y + 5, x + 11, y + 5 );
			g.drawLine( x + 4, y + 7, x + 11, y + 7 );
			g.drawLine( x + 4, y + 9, x + 11, y + 9 );
			g.drawLine( x + 4, y + 11, x + 10, y + 11 );
		}
	}

	public static void main( String[] args ) {
        try
        {
            UIManager.setLookAndFeel( "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
		
        JFrame frame = new JFrame();
		frame.setBounds( 1500, 400, 500, 400 );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().add( new JLabel( "This frame is only here as a placeholder" ) );
		frame.setVisible( true );

		// This will fail and thus show an exception window...
		try {
			String x = "TestString";
			if (x.substring(200).equals("x")){
				
			}
		}
		catch( Exception e ) {
			ExceptionDialog.showExceptionDialog( frame, e, null );
		}
		frame.dispose();
		System.out.println( "done" );
	}

}
