
import static kodkod.ast.Expression.UNIV;

import kodkod.ast.*;
import kodkod.ast.operator.*;
import kodkod.instance.*;
import kodkod.engine.*;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.config.Options;

import java.util.concurrent.*;  

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TextArea1 {
    private JFrame f; //Main frame
    private JTextAreaKeyListener ta; // Text area
    private JScrollPane sbrText; // Scroll pane for text area
    private JButton btnQuit,btnOpt,btnOpt50,btnCols;
    public String content;
    public boolean evenColumns;
    public int optimizationLevel;
    public int boxHeigth, boxWidth, layoutWidth;
    public String [] words;
    public Integer [] wordSps;
    public int startLayoutPos, endLayoutPos;
    public boolean waitingForSolver;
    public boolean [] satisfiable;
    //Constructor
    public TextArea1(int bh, int bw, int lw, int optimizeLevel, boolean evenCols){ 

	boxHeigth = bh;
	boxWidth = bw;
	layoutWidth = lw;
	optimizationLevel = optimizeLevel;
	evenColumns = evenCols;
	waitingForSolver = false;
	satisfiable = new boolean[1];
	// init solver thread

	Runnable getSol = new Runnable() { 
		public void run() { 
		    System.out.println("Results are in!"); 
		    println(wordSps);
		    if(satisfiable[0]) {
			String newContent = "";
			String newPar = "";
			ta.setEditable(false);
			content = ta.getText();			
			ta.setText("");
			for(int i=0;i<wordSps.length-1;i++) {
			    String sps = "";
			    if (wordSps[i] == 0) {
				sps = "\n";
			    } else {
				int j = 0;
				while (j < wordSps[i]) {
				    sps = sps + " ";
				    j++;
				}
			    }
			    newPar = newPar + (words[i]+sps);
			}
			newPar = newPar + words[wordSps.length-1];
			System.out.println(newPar); 
			newContent = content.substring(0,startLayoutPos) + newPar +
			             content.substring(endLayoutPos);
			ta.setText(newContent);
			ta.setEditable(true);
			ta.setCaretPosition(ta.getText().length());
			ta.requestFocus();
		    }
		    waitingForSolver = false;
		} 
	    }; 
	final CyclicBarrier barrier = new CyclicBarrier(1, getSol);

        // Create Frame 
        f = new JFrame("Text Layout Demo");
	f.getContentPane().setLayout(new FlowLayout());
      
        // Create Scrolling Text Area in Swing
        ta = new JTextAreaKeyListener("this is one too simplistic text box for sure but it can handle line breaking, as well as even columns for you. You can even set the optimization level: no optimizations, 50%, or 100%!", boxHeigth, boxWidth,optimizationLevel,evenColumns,barrier);
	Font font = new Font("Monaco", Font.PLAIN, 14);
        ta.setFont(font);
	ta.setLineWrap(false);
	sbrText = new JScrollPane(ta);
	sbrText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	// Create Even Columns Button
        btnCols = new JButton("Even Columns");
        btnCols.addActionListener(
				  new ActionListener(){
				      public void actionPerformed(ActionEvent e){
					  evenColumns = !evenColumns;
					  Color c = evenColumns ? Color.blue : Color.gray;
					  btnCols.setForeground(c);
				      }
				  }
				  );
	// Create Optimization Button
        btnOpt = new JButton("Optimization Level: 0");
        btnOpt.addActionListener(
				  new ActionListener(){
				      public void actionPerformed(ActionEvent e){
					  optimizationLevel = optimizationLevel == 2 ? 0 : optimizationLevel + 1;
					  btnOpt.setText("Optimization Level: " + optimizationLevel);
				      }
				  }
				  );
	// Create Quit Button
        btnQuit = new JButton("Quit");
        btnQuit.addActionListener(
				  new ActionListener(){
				      public void actionPerformed(ActionEvent e){
					  System.exit(0);         
				      }
				  }
				  );
        
    }
        
    public void launchFrame(){ // Create Layout
        // Add text area and button to frame
	f.getContentPane().add(sbrText);
        f.getContentPane().add(btnCols);
        f.getContentPane().add(btnOpt);
        f.getContentPane().add(btnQuit);
	btnCols.setForeground(evenColumns ? Color.blue : Color.gray);
	// Close when the close button is clicked
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	//Display Frame
        f.pack(); // Adjusts frame to size of components
        f.setVisible(true);
    }

    public void println(Object [] l){
	System.out.print("[");
	for(int i=0;i<l.length-1;i++) {
	    System.out.print(l[i]);
	    System.out.print(", ");
	}
	if(l.length > 0) {
	    System.out.print(l[l.length-1]);
	}
	System.out.println("]");
    }

    private class JTextAreaKeyListener extends JTextArea implements KeyListener 
    {
	CyclicBarrier barrier;
	public JTextAreaKeyListener(String text, int rows, int columns,  int optimizationLevel, boolean evenColumns, CyclicBarrier barrier) {
	    super(text,rows,columns);	    
	    this.barrier = barrier;	    
	    // allow frame to process Key events
	    addKeyListener( this );

	}

	public void keyPressed( KeyEvent e )
	{
	    int key = e.getKeyCode();
	    if (!waitingForSolver && key == KeyEvent.VK_ENTER) {
		System.out.println("ENTER pressed");
		waitingForSolver = true;
		content = ta.getText();
		endLayoutPos = ta.getCaretPosition();
		startLayoutPos = endLayoutPos-1;
		System.out.print("end: ");
		System.out.println(endLayoutPos);
		while (startLayoutPos >= 0 && content.charAt(startLayoutPos) != '\n') {
		    startLayoutPos--;
		} 
		startLayoutPos++;
		System.out.print("start: ");
		System.out.println(startLayoutPos);
		System.out.println(content.substring(startLayoutPos, endLayoutPos));
		words = content.substring(startLayoutPos, endLayoutPos).split("\\s+");
		wordSps = new Integer[words.length];
		System.out.println(words.length);

		////// apply optimizations.. ///////////
		if (optimizationLevel > 0) {
		    int budget, prev, c, i = 0;
		    while(i<words.length) {
			budget = 0;
			prev = i;
			while(i<words.length && budget + words[i].length() <= layoutWidth) {
			    wordSps[i] = 1;
			    budget += words[i].length() + 1;
			    i++;
			}
			budget--;
			
			c = 0;
			if (optimizationLevel > 1 && evenColumns && i < words.length) {
			    while(budget < layoutWidth) {
				if (prev+c==i-1) { c = 0; }
				wordSps[prev+c]++;
				budget++;
				c++; 
			    }
			}
			wordSps[i-1] = 0;
		    }
		    println(wordSps);
		}
		////////////////////////////////////////

		new SolverThread(barrier,layoutWidth,words,wordSps,optimizationLevel,evenColumns,satisfiable).start();
	    }	
	}
	
	public void keyReleased( KeyEvent e )
	{
	 }
	
	public void keyTyped( KeyEvent e )
	{
	}
	
    }


    private static class SolverThread extends Thread { 
	CyclicBarrier barrier; 
	boolean evenColumns;
	int optimizationLevel;
	int width;
	String [] words;
	Integer [] wordSps;
	boolean [] satisfiable;
	SolverThread(CyclicBarrier barrier, int width, String [] words, Integer [] wordSps, int optimizationLevel, boolean evenColumns, boolean [] satisfiable) { 
	    this.barrier = barrier;
	    this.width = width;
	    this.words = words;
	    this.wordSps = wordSps;
	    this.satisfiable = satisfiable;
	    this.optimizationLevel = optimizationLevel;
	    this.evenColumns = evenColumns;
	} 
	public void run() { 
	    System.out.println("in thread..."); 
	    
	    Bounds b;
	    Formula f;
	    
	    Relation x0 = Relation.unary("uptoMaxSp");
	    Relation x5 = Relation.unary("uptoWordsLength");
	    Relation r0 = Relation.nary("r0", 2); // word lengths
	    Relation r1 = Relation.nary("r1", 2); // lineBreak idxs
	    Relation r3 = Relation.nary("r3", 2); // word spaces
	    
	    int MAXINT = Math.max(words.length,width);
	    int NEGONE = MAXINT + 1;
	    int MAXSPACE = evenColumns ? 5 : 1; // width / 2
	    String[] atoms = new String[MAXINT+2];
	    for(int i=0;i<=MAXINT;i++) {
		atoms[i] = Integer.toString(i);
	    }
	    atoms[NEGONE] = Integer.toString(-1);
	    List<String> atomlist = Arrays.asList(atoms);
	    
	    Universe universe = new Universe(atomlist);
	    TupleFactory factory = universe.factory();
	    Bounds bounds = new Bounds(universe);
	    
	    Object ti = universe.atom(NEGONE);
	    bounds.boundExactly(-1,factory.range(factory.tuple(ti),factory.tuple(ti)));
	    for (int i=0;i<=MAXINT;i++) {
		ti = universe.atom(i);
		bounds.boundExactly(i,factory.range(factory.tuple(ti),factory.tuple(ti)));
	    }
	    	    
	    TupleSet x5_upper=factory.range(factory.tuple(universe.atom(0)),
					    factory.tuple(universe.atom(words.length-1)));
	    bounds.boundExactly(x5, x5_upper);

	    TupleSet x0_upper=factory.range(factory.tuple(universe.atom(0)),
					    factory.tuple(universe.atom(MAXSPACE)));
	    bounds.boundExactly(x0, x0_upper);
	    
	    TupleSet r0_upper = factory.noneOf(2);
	    for (int i=0;i<words.length;i++) {
		ti = universe.atom(i);
		r0_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(words[i].length()))));
	    }
	    bounds.boundExactly(r0, r0_upper);
	    
	    TupleSet r1_lower = factory.noneOf(2);
	    TupleSet r1_upper = factory.noneOf(2);
	    r1_lower.add(factory.tuple(universe.atom(0)).product(factory.tuple(universe.atom(NEGONE))));	
	    r1_upper.add(factory.tuple(universe.atom(0)).product(factory.tuple(universe.atom(NEGONE))));
	    
	    if (optimizationLevel > 0) {
		int j = 0;
		for(int i=0;i<words.length;i++) {
		    if (wordSps[i] == 0) {
			j++;
			r1_upper.add(factory.tuple(universe.atom(j)).product(factory.tuple(universe.atom(i)))); 
		    }
		}
		bounds.boundExactly(r1, r1_upper);
	    } else {
		for (int i=1;i<words.length;i++) {
		    ti = universe.atom(i);
		    for (int j=0;j<words.length;j++) {
			r1_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		    }
		}
		bounds.bound(r1, r1_lower, r1_upper);
	    }

	    TupleSet r3_upper = factory.noneOf(2);
	    if (optimizationLevel > 1) {	    
		for(int i=0;i<words.length;i++) {
		    r3_upper.add(factory.tuple(universe.atom(i)).product(factory.tuple(universe.atom(wordSps[i])))); 
		}
		bounds.boundExactly(r3, r3_upper);
	    } else if (optimizationLevel > 0) {
		for (int i=0;i<words.length-1;i++) {
		    if (wordSps[i] == 0) {
			r3_upper.add(factory.tuple(universe.atom(i)).product(factory.tuple(universe.atom(0)))); 
		    } else {
			ti = universe.atom(i);
			for (int j=0;j<=MAXSPACE;j++) {
			    r3_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
			}
		    }
		}
		r3_upper.add(factory.tuple(universe.atom(words.length-1)).product(factory.tuple(universe.atom(0)))); 
		bounds.bound(r3, r3_upper);		
	    } else {
		for (int i=0;i<words.length-1;i++) {
		    ti = universe.atom(i);
		    for (int j=0;j<=MAXSPACE;j++) {
			r3_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		    }
		}
		r3_upper.add(factory.tuple(universe.atom(words.length-1)).product(factory.tuple(universe.atom(0)))); 
		bounds.bound(r3, r3_upper);		
	    }

	    b = bounds;
	    
	    // x1: spaces indices = 0 (newlines)
	    IntExpression x_n1 = IntConstant.constant(-1);
	    IntExpression x_0 = IntConstant.constant(0);
	    IntExpression x_1 = IntConstant.constant(1);
	    IntExpression x_2 = IntConstant.constant(2);
	    IntExpression x_3 = IntConstant.constant(3);
	    Expression x1 = x_n1.toExpression().union(r3.join(x_0.toExpression()));
	    Variable vf=Variable.unary("vf");
	    Expression x12 = vf.sum().lt(x1.count()).comprehension(vf.oneOf(x5));
	    	    
	    // its lineBreaks elementsUnique
	    Variable va=Variable.unary("");
	    Decls x32=va.oneOf(x5);
	    Expression x36=r1.join(va);
	    IntExpression x46=x36.count();
	    Formula x50=x46.lte(x_1);
	    Formula xUniqueElms=x50.forAll(x32);
	    
	    
	    // its lineBreaks sorted (not needed?)
	    Variable vb=Variable.unary("vb");
	    Expression x13=x12.difference(x1.count().minus(x_1).toExpression());
	    Expression x15 = x1.count().minus(x_2).toExpression();
	    Expression x14=x13.difference(x15);
	    Decls x33=vb.oneOf(x13);
	    Expression x69=vb.join(r1);
	    IntExpression x68=x69.sum();
	    IntExpression x75=vb.sum();
	    IntExpression x74=x75.plus(x_1);
	    Expression x73=x74.toExpression();
	    Expression x72=x73.join(r1);
	    IntExpression x71=x72.sum();
	    Formula x60=x68.lte(x71);
	    Formula xSorted=x60.forAll(x33);
	    
	    // its lineAt: L sum = its width
	    Variable vc=Variable.unary("vc");
	    Variable vd=Variable.unary("vd");
	    Variable ve=Variable.unary("ve");
	    IntExpression x80=IntConstant.constant(width);
	    IntExpression x82=vd.sum().plus(x_1);
	    Expression x821 = vd.join(r1);
	    Expression x822 = x82.toExpression().join(r1);
	    IntExpression x820 = x821.sum();
	    IntExpression x823 = x820.plus(x_1);
	    IntExpression x824 = x822.sum();
	    IntExpression x825 = x824.minus(x820);
	    Expression x83 = vc.sum().gte(x823).and(vc.sum().lte(x824)).comprehension(vc.oneOf(x5));
	    Variable idx = Variable.unary("idx");    
	    IntExpression body = idx.join(r0).sum();
	    IntExpression x851 = body.sum(idx.oneOf(x83));
	    
	    Variable idx2 = Variable.unary("idx2");    
	    IntExpression body2 = idx2.join(r3).sum();
	    IntExpression x852 = body2.sum(idx2.oneOf(x83));
	    Expression x830 = x83.difference(x824.toExpression()).difference(x824.minus(x_1).toExpression());
	    Expression x841 = x83.product(x0);
	    Expression x843 = x841.intersection(r3); // line spaces
	    
	    IntExpression x85 = x851.plus(x852);
	    // last word 0 space, others > 0 space, deviation < 2, extra spaces < next word length
	    Variable vh = Variable.unary("vh");
	    Formula x853 = x843.join(x_0.toExpression()).count().eq(x_1);
	    Formula x854 = x824.toExpression().join(r3).sum().eq(x_0);
	    Expression x848 = x_0.toExpression().union(x_1.toExpression()).union(x_n1.toExpression());
	    Formula x849 = vh.join(r3).sum().minus(vh.sum().plus(x_1).toExpression().join(r3).sum()).toExpression().in(x848).forAll(vh.oneOf(x830));
	    Formula x855 = x853.and(x854).and(x849);
	    IntExpression x857 = x824.plus(x_1).toExpression().join(r0).sum(); // wd next line
	    Formula x88 = x825.plus(x851).plus(x857).gt(x80);
	    Decls x89=vd.oneOf(x14);
	    Formula xOptSps=x88.forAll(x89);

	    // length line = width:
	    Formula x86 = evenColumns ? x85.eq(x80) : x85.lte(x80); 
	    Formula x850 = x83.join(r3).difference(x_0.toExpression()).difference(x_1.toExpression()).no();
	    Formula x856 = x853.and(x850);
	    Decls x87=vd.oneOf(x14);
	    Decls x871=vd.oneOf(x15);
	    // all lines but last:
	    //   x86: length = width
	    //   x855: last word space = 0, others > 0.
	    // last line:
	    //   x511.lte(x80): length < width
	    //   x856: last word space = 0, other = 1
	    Formula xLineLengths=x86.forAll(x87).and(x855.forAll(x87)).and(x85.lte(x80).forAll(x871)).and(x856.forAll(x871));
	    
	    Formula x100=Formula.compose(FormulaOperator.AND, r1.function(x12,x1), r3.function(x5,x0), xSorted, xUniqueElms, xLineLengths, xOptSps);
	    
	    f = x100;
	    
	    Solver solver = new Solver();
	    solver.options().setSolver(SATFactory.MiniSat);
	    solver.options().setBitwidth(9);
	    solver.options().setFlatten(false);
	    solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
	    solver.options().setSymmetryBreaking(20);
	    solver.options().setSkolemDepth(0);
	    System.out.println("Solving...");
	    System.out.flush();	
	    Solution sol = solver.solve(f,b);
	    System.out.println(sol.toString());
	    if (sol.instance()==null) {
		satisfiable[0] = false;
		System.out.println(sol);
		//return (false);
	    } else {
		satisfiable[0] = true;
		//System.out.println(sol.stats());
		
		Iterator<Tuple> iter = sol.instance().tuples(r3).iterator();
		for(int i = 0; i < wordSps.length; i++) {
		    wordSps[i] = Integer.parseInt((String)iter.next().atom(1));
		    //System.out.print("\t");
		}
		//return (true);
	    }		
	
	    // wait for others 
	    try { 
		barrier.await(); 
	    } catch (InterruptedException ex) { 
		ex.printStackTrace(); 
	    } catch (BrokenBarrierException ex) { 
		ex.printStackTrace(); 
	    } 
	    
	}
    }
    
    public static void main(String args[]){
	boolean evenCols = true;
	int  optimizeLevel = 0;
	int textBoxHeight = 20, textBoxWidth = 60, textBoxLayoutWidth = 50;

        TextArea1 gui = new TextArea1(textBoxHeight, textBoxWidth, textBoxLayoutWidth, optimizeLevel, evenCols);
        gui.launchFrame();
    }
}

