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
import java.util.Random;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;



public class AllWinsDemo {

    static int FRAMES = 3;
    static int STEPS = 10;
    static int RESOLX = 1200;
    static int RESOLY = 800;
    static int GRID = 50;
    final Random rand = new Random();

    public JFrame frames[];
    public int framesX1[], framesY1[], framesX2[], framesY2[],framesS1[],framesS2[];
    public boolean waitingForSolver;
    public boolean [] satisfiable;
    private JButton btnQuit,btnDo;
    private JFrame f; //Main frame

    //Constructor
    public AllWinsDemo() { 

	waitingForSolver = false;
	satisfiable = new boolean[1];

	// init solver thread
	Runnable getSol = new Runnable() { 
		public void run() { 
		    System.out.println("Results are in!"); 

		    for(int i=0;i<FRAMES;i++) {
			System.out.print("frame #" + i + ": ");
			System.out.println("[" + framesX1[i]/GRID + " " + framesX2[i]/GRID + "] [" + framesY1[i]/GRID + " " + framesY2[i]/GRID + "] " + framesS1[i] + " " + framesS2[i]);
		    }
		    
		    if(satisfiable[0]) {
			moveAndResize();
		    }
		    waitingForSolver = false;
		} 
	    }; 
	final CyclicBarrier barrier = new CyclicBarrier(1, getSol);
    
	// Create Quit Button
        btnQuit = new JButton("Quit");
        btnQuit.addActionListener(
				  new ActionListener(){
				      public void actionPerformed(ActionEvent e){
					  System.exit(0);         
				      }
				  }
				  );
	// Create Do Button
        btnDo = new JButton("Do");
        btnDo.addActionListener(
				  new ActionListener(){
				      public void actionPerformed(ActionEvent e){
					  waitingForSolver = true;
					  new SolverThread(barrier,satisfiable,framesX1,framesX2,framesY1,framesY2,framesS1,framesS2).start();
				      }
				  }
				  );
        // Create Frame 
        f = new JFrame("Mac All Windows Display Demo");
	f.getContentPane().setLayout(new FlowLayout());
    
	// draw windows
	frames = new JFrame [FRAMES];
	framesX1 = new int[FRAMES];
	framesY1 = new int[FRAMES];
	framesX2 = new int[FRAMES];
	framesY2 = new int[FRAMES];
	framesS1 = new int[FRAMES];
	framesS2 = new int[FRAMES];

	JFrame frame;
	for(int i=0;i<FRAMES;i++) {
	    framesX1[i] = rand.nextInt(RESOLX-GRID+1)+GRID;
	    framesX2[i] = framesX1[i] + rand.nextInt(RESOLX-framesX1[i]-GRID+1)+GRID;
	    framesY1[i] = rand.nextInt(RESOLY-GRID+1)+GRID;
	    framesY2[i] = framesY1[i] + rand.nextInt(RESOLY-framesY1[i]-GRID+1)+GRID;

	    frames[i] = new JFrame();
	    frame = frames[i];
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	   
	    JPanel panel = new JPanel();	    
	    panel.add(new JLabel("a"+i));
	    
	    frame.add(panel);
	    framesS1[i] = (framesX2[i]-framesX1[i])/(framesY2[i]-framesY1[i]);
	    framesS2[i] = (framesY2[i]-framesY1[i])/(framesX2[i]-framesX1[i]);
	    System.out.print("frame #" + i + ": ");
	    System.out.println("[" + framesX1[i]/GRID + " " + framesX2[i]/GRID + "] [" + framesY1[i]/GRID + " " + framesY2[i]/GRID + "] " + framesS1[i] + " " + framesS2[i]);
	    frame.setSize(framesX2[i]-framesX1[i],framesY2[i]-framesY1[i]);
	    frame.setLocation(framesX1[i],framesY1[i]);
	    frame.setVisible(true);
	}
    }

    public void launchFrame(){ // Create Layout
        // Add text area and button to frame
        f.getContentPane().add(btnDo);
        f.getContentPane().add(btnQuit);
	// Close when the close button is clicked
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	//Display Frame
        f.pack(); // Adjusts frame to size of components
        f.setVisible(true);
    }

    public void moveAndResize() {

	int xInc = 0, yInc = 0, wInc = 0, hInc = 0;
	int [] cx1s = new int [FRAMES];
	int [] cy1s = new int [FRAMES];
	int [] cws = new int [FRAMES];
	int [] chs = new int [FRAMES];
	int [] xIncs = new int [FRAMES];
	int [] yIncs = new int [FRAMES];
	int [] wIncs = new int [FRAMES];
	int [] hIncs = new int [FRAMES];
	for(int i=0;i<FRAMES;i++) {
	    int x1 = framesX1[i];
	    int x2 = framesX2[i];
	    int y1 = framesY1[i];
	    int y2 = framesY2[i]; 
	    JFrame f = frames[i];
	    Point loc = f.getLocation();
	    Dimension dim = f.getSize();
	    int cx1 = loc.x, cy1 = loc.y, cx2 = cx1 + dim.width, cy2 = cy1 + dim.height;
	    int cw = cx2 - cx1, ch = cy2 - cy1;
	    cx1s[i] = cx1;
	    cy1s[i] = cy1;
	    cws[i] = cw;
	    chs[i] = ch;
	    xIncs[i] = (x1 - cx1) / STEPS;
	    yIncs[i] = (y1 - cy1) / STEPS;
	    wIncs[i] = (x2 - x1 - cw) / STEPS;
	    hIncs[i] = (y2 - y1 - ch) / STEPS;
	    xInc = (x1 - cx1 > xInc) ? x1 - cx1 : xInc;
	    yInc = (y1 - cy1 > yInc) ? y1 - cy1 : yInc;
	    wInc = (x2 - x1 - cw > wInc) ? x2 - x1 - cw : wInc;
	    hInc = (y2 - y1 - ch > hInc) ? y2 - y1 - ch : hInc;
	}
	xInc = xInc / STEPS;
	yInc = yInc / STEPS;
	wInc = wInc / STEPS;
	hInc = hInc / STEPS;
	int s = 0;
	while (s < STEPS) {
	    //try {
		//Thread.currentThread().sleep(5);
		for(int i=0;i<FRAMES;i++) {
		    JFrame f = frames[i];
		    cx1s[i] += xIncs[i];
		    cy1s[i] += yIncs[i];
		    cws[i] += wIncs[i];
		    chs[i] += hIncs[i];
		    f.setLocation(cx1s[i],cy1s[i]);
		    f.setSize(cws[i],chs[i]);
		}
		//}
	    //catch(InterruptedException ie){
		//If this thread was intrrupted by nother thread 
	    //}
	    s++;
	}
    }

    private static class SolverThread extends Thread { 
	CyclicBarrier barrier; 
	boolean [] satisfiable;
	int framesX1[], framesY1[], framesX2[], framesY2[], framesS1[], framesS2[];
	SolverThread(CyclicBarrier barrier, boolean [] satisfiable, int [] framesX1, int [] framesX2, int [] framesY1, int [] framesY2, int [] framesS1, int [] framesS2) { 
	    this.barrier = barrier;
	    this.satisfiable = satisfiable;
	    this.framesX1 = framesX1;
	    this.framesX2 = framesX2;
	    this.framesY1 = framesY1;
	    this.framesY2 = framesY2;
	    this.framesS1 = framesS1;
	    this.framesS2 = framesS2;
	} 
	public void run() { 
	    System.out.println("in thread..."); 
	    
	    Relation idxs = Relation.unary("idxs"); // 0 .. FRAMES
	    Relation xm = Relation.unary("xm"); // 0 .. max X
	    Relation ym = Relation.unary("ym"); // 0 .. max Y
	    Relation disjIdxs = Relation.nary("disjIdxPairs", 2); // disjoint idxs
	    Relation x1 = Relation.nary("x1", 2); // X1s
	    Relation x2 = Relation.nary("x2", 2); // X2s
	    Relation y1 = Relation.nary("y1", 2); // Y1s
	    Relation y2 = Relation.nary("y2", 2); // Y2s
	    Relation proportions1 = Relation.nary("proportions1", 2); // width Scales
	    Relation proportions2 = Relation.nary("proportions2", 2); // height Scales
	    
	    int MAXX = RESOLX/GRID;
	    int MAXY = RESOLY/GRID;
	    int MAXINT = MAXX;
	    String[] atoms = new String[MAXINT+1];
	    for(int i=0;i<=MAXINT;i++) {
		atoms[i] = Integer.toString(i);
	    }
	    List<String> atomlist = Arrays.asList(atoms);
	    
	    Universe universe = new Universe(atomlist);
	    TupleFactory factory = universe.factory();
	    Bounds bounds = new Bounds(universe);
	    Object ti;
	    for (int i=0;i<=MAXINT;i++) {
		ti = universe.atom(i);
		bounds.boundExactly(i,factory.range(factory.tuple(ti),factory.tuple(ti)));
	    }
	    	    
	    TupleSet idxs_upper=factory.range(factory.tuple(universe.atom(0)),
					      factory.tuple(universe.atom(FRAMES-1)));
	    bounds.boundExactly(idxs, idxs_upper);

	    TupleSet disjIdxs_upper = factory.noneOf(2);
	    for (int i=FRAMES-1;i>0;i--) {
		for (int j=0;j<i;j++) {
		    disjIdxs_upper.add(factory.tuple(universe.atom(i)).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.boundExactly(disjIdxs, disjIdxs_upper);

	    TupleSet xm_upper=factory.range(factory.tuple(universe.atom(0)),
					    factory.tuple(universe.atom(MAXX)));
	    bounds.boundExactly(xm, xm_upper);

	    TupleSet ym_upper=factory.range(factory.tuple(universe.atom(0)),
					    factory.tuple(universe.atom(MAXY)));
	    bounds.boundExactly(ym, ym_upper);

	    TupleSet proportions1_upper = factory.noneOf(2);
	    TupleSet proportions2_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		proportions1_upper.add(factory.tuple(universe.atom(i)).product(factory.tuple(universe.atom(framesS1[i]))));
		proportions2_upper.add(factory.tuple(universe.atom(i)).product(factory.tuple(universe.atom(framesS2[i]))));
	    }
	    bounds.boundExactly(proportions1, proportions1_upper);
	    bounds.boundExactly(proportions2, proportions2_upper);
	    
	    TupleSet x1_lower = factory.noneOf(2);
	    TupleSet x1_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		for (int j=0;j<=MAXX;j++) {
		    x1_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.bound(x1, x1_lower, x1_upper);
	    bounds.bound(x2, x1_lower, x1_upper);

	    TupleSet y1_lower = factory.noneOf(2);
	    TupleSet y1_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		for (int j=0;j<=MAXY;j++) {
		    y1_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.bound(y1, y1_lower, y1_upper);
	    bounds.bound(y2, y1_lower, y1_upper);

	    // build constraint formula

	    IntExpression x_0 = IntConstant.constant(0);
	    IntExpression x_1 = IntConstant.constant(1);
	    IntExpression x_2 = IntConstant.constant(2);
	    IntExpression x_3 = IntConstant.constant(3);

	    // end range > start range
	    Variable va = Variable.unary("va");
	    Formula f10 = va.join(x2).sum().gt(va.join(x1).sum().plus(x_3));
	    Formula f20 = va.join(y2).sum().gt(va.join(y1).sum().plus(x_3));
	    Formula f30 = f10.and(f20).forAll(va.oneOf(idxs));

	    // preserved proportions
	    Formula f210 = va.join(x2).sum().minus(va.join(x1).sum()).divide(va.join(y2).sum().minus(va.join(y1).sum())).eq(va.join(proportions1).sum());
	    Formula f220 = va.join(y2).sum().minus(va.join(y1).sum()).divide(va.join(x2).sum().minus(va.join(x1).sum())).eq(va.join(proportions2).sum());
	    Formula f230 = f210.and(f220).forAll(va.oneOf(idxs));

	    // no overlaps
	    Variable vb = Variable.unary("vb");
	    Variable vc = Variable.unary("vc");
	    Formula f110 = vb.join(x1).sum().gt(vc.join(x2).sum());
	    Formula f120 = vb.join(x2).sum().lt(vc.join(x1).sum());
	    Formula f115 = vb.join(y1).sum().gt(vc.join(y2).sum());
	    Formula f125 = vb.join(y2).sum().lt(vc.join(y1).sum());
	    Formula f130 = vb.product(vc).in(disjIdxs);
	    Formula f135 = f110.or(f120).or(f115).or(f125);
	    Formula f140 = f130.implies(f135);
	    Formula f150 = f140.forAll(vc.oneOf(idxs)).forAll(vb.oneOf(idxs));

	    Formula f990 = f30.and(f230).and(f150);

	    Formula formula = Formula.compose(FormulaOperator.AND, x1.function(idxs,xm), 
					 x2.function(idxs,xm), y1.function(idxs,ym), 
					 y2.function(idxs,ym), f990);
	    	    
	    Solver solver = new Solver();
	    solver.options().setSolver(SATFactory.MiniSat);
	    solver.options().setBitwidth(10);
	    solver.options().setFlatten(false);
	    solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
	    solver.options().setSymmetryBreaking(20);
	    solver.options().setSkolemDepth(0);
	    System.out.println("Solving...");
	    System.out.flush();	
	    Solution sol = solver.solve(formula,bounds);
	    System.out.println(sol.toString());

	    if (sol.instance()==null) {
		satisfiable[0] = false;
		System.out.println("");
		//return (false);
	    } else {
		satisfiable[0] = true;
		Iterator<Tuple> iterX1 = sol.instance().tuples(x1).iterator();
		Iterator<Tuple> iterX2 = sol.instance().tuples(x2).iterator();
		Iterator<Tuple> iterY1 = sol.instance().tuples(y1).iterator();
		Iterator<Tuple> iterY2 = sol.instance().tuples(y2).iterator();
		
		for(int i = 0; i < FRAMES; i++) {
		    framesX1[i] = GRID * Integer.parseInt((String)iterX1.next().atom(1));
		    framesX2[i] = GRID * Integer.parseInt((String)iterX2.next().atom(1));
		    framesY1[i] = GRID * Integer.parseInt((String)iterY1.next().atom(1));
		    framesY2[i] = GRID * Integer.parseInt((String)iterY2.next().atom(1));
		    framesS1[i] = (framesX2[i]-framesX1[i])/(framesY2[i]-framesY1[i]);
		    framesS2[i] = (framesY2[i]-framesY1[i])/(framesX2[i]-framesX1[i]);
		}

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
	
    }

    public static void main(String[] a) {
	AllWinsDemo demo = new AllWinsDemo();
        demo.launchFrame();
    }
    
}