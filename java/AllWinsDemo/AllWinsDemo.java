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

    static int FRAMES = 8;
    static int STEPS = 10;
    static int RESOLX = 1200;
    static int RESOLY = 800;
    static int GRID = 25;
    static int MAXSCALE = 10;
    static int MINW = 180;
    static int MINH = 90;
    final Random rand = new Random();

    public JFrame frames[];
    public int framesX[], framesY[], framesW[], framesH[];
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
			System.out.println("[" + framesX[i]/GRID + " " + (framesX[i]+framesW[i])/GRID + "] [" + framesY[i]/GRID + " " + (framesY[i]+framesW[i])/GRID + "] " + framesW[i]/framesH[i] + " " + framesH[i]/framesW[i]);
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
					  new SolverThread(barrier,satisfiable,framesX,framesY,framesW,framesH).start();
				      }
				  }
				  );
        // Create Frame 
        f = new JFrame("Mac All Windows Display Demo");
	f.getContentPane().setLayout(new FlowLayout());
    
	// draw windows
	frames = new JFrame [FRAMES];
	framesX = new int[FRAMES];
	framesY = new int[FRAMES];
	framesW = new int[FRAMES];
	framesH = new int[FRAMES];

	JFrame frame;
	for(int i=0;i<FRAMES;i++) {
	    framesX[i] = rand.nextInt(RESOLX-MINW-GRID);
	    framesY[i] = rand.nextInt(RESOLY-MINH-GRID);
	    framesW[i] = Math.max(rand.nextInt(RESOLX-framesX[i]),MINW);
	    framesH[i] = Math.max(rand.nextInt(RESOLY-framesY[i]),MINH);

	    frames[i] = new JFrame();
	    frame = frames[i];
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	   
	    JPanel panel = new JPanel();	    
	    panel.add(new JLabel("a"+i));
	    
	    frame.add(panel);
	    System.out.print("frame #" + i + ": ");
	    System.out.println("[" + framesX[i]/GRID + " " + (framesX[i]+framesW[i])/GRID + "] [" + framesY[i]/GRID + " " + (framesY[i]+framesH[i])/GRID + "] " + framesW[i]/framesH[i] + " " + framesH[i]/framesW[i]);
	    frame.setSize(framesW[i],framesH[i]);
	    frame.setLocation(framesX[i],framesY[i]);
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
	int [] cxs = new int [FRAMES];
	int [] cys = new int [FRAMES];
	int [] cws = new int [FRAMES];
	int [] chs = new int [FRAMES];
	int [] xIncs = new int [FRAMES];
	int [] yIncs = new int [FRAMES];
	int [] wIncs = new int [FRAMES];
	int [] hIncs = new int [FRAMES];
	for(int i=0;i<FRAMES;i++) {
	    int x = framesX[i];
	    int w = framesW[i];
	    int y = framesY[i];
	    int h = framesH[i]; 
	    JFrame f = frames[i];
	    Point loc = f.getLocation();
	    Dimension dim = f.getSize();
	    int cx = loc.x, cy = loc.y;
	    int cw = dim.width, ch = dim.height;
	    cxs[i] = cx;
	    cys[i] = cy;
	    cws[i] = cw;
	    chs[i] = ch;
	    xIncs[i] = (x - cx) / STEPS;
	    yIncs[i] = (y - cy) / STEPS;
	    wIncs[i] = (w - cw) / STEPS;
	    hIncs[i] = (h - ch) / STEPS;
	    xInc = (x - cx > xInc) ? x - cx : xInc;
	    yInc = (y - cy > yInc) ? y - cy : yInc;
	    wInc = (w - cw > wInc) ? w - cw : wInc;
	    hInc = (h - ch > hInc) ? h - ch : hInc;
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
		    cxs[i] += xIncs[i];
		    cys[i] += yIncs[i];
		    cws[i] += wIncs[i];
		    chs[i] += hIncs[i];
		    f.setLocation(cxs[i],cys[i]);
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
	int framesX[], framesY[], framesW[], framesH[];
	SolverThread(CyclicBarrier barrier, boolean [] satisfiable, int [] framesX, int [] framesY, int [] framesW, int [] framesH) { 
	    this.barrier = barrier;
	    this.satisfiable = satisfiable;
	    this.framesX = framesX;
	    this.framesY = framesY;
	    this.framesW = framesW;
	    this.framesH = framesH;
	} 
	public void run() { 
	    System.out.println("in thread..."); 
	    
	    Relation idxs = Relation.unary("idxs"); // 0 .. FRAMES
	    Relation sm = Relation.unary("sm"); // 1 .. MAXSCALE
	    Relation xm = Relation.unary("xm"); // 0 .. max X
	    Relation ym = Relation.unary("ym"); // 0 .. max Y
	    Relation disjIdxs = Relation.nary("disjIdxPairs", 2); // disjoint idxs
	    Relation x = Relation.nary("x", 2); // new Xs
	    Relation y = Relation.nary("y", 2); // new Ys
	    Relation w = Relation.nary("w", 2); // Widths
	    Relation h = Relation.nary("h", 2); // Heigths
	    Relation wOrig = Relation.nary("wOrig", 2); // orig Widths
	    Relation hOrig = Relation.nary("hOrig", 2); // orig Heigths
	    Relation scale = Relation.nary("scale", 2); // Scales
	    
	    int MAXX = RESOLX/GRID;
	    int MAXY = RESOLY/GRID;
	    int GRIDMINW = MINW/GRID;
	    int GRIDMINH = MINH/GRID;

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

	    TupleSet sm_upper=factory.range(factory.tuple(universe.atom(1)),
					    factory.tuple(universe.atom(MAXSCALE)));
	    bounds.boundExactly(sm, sm_upper);

	    TupleSet wOrig_upper = factory.noneOf(2);
	    TupleSet hOrig_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		wOrig_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(framesW[i]/GRID))));
		hOrig_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(framesH[i]/GRID))));
	    }
	    bounds.boundExactly(wOrig, wOrig_upper);
	    bounds.boundExactly(hOrig, hOrig_upper);

	    TupleSet scale_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		for (int j=1;j<MAXSCALE;j++) {
		    scale_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.bound(scale, scale_upper);
	    
	    TupleSet x_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		for (int j=0;j<=MAXX;j++) {
		    x_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.bound(x, x_upper);

	    TupleSet y_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		for (int j=0;j<=MAXY;j++) {
		    y_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.bound(y, y_upper);

	    TupleSet w_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		for (int j=GRIDMINW;j<=MAXX;j++) {
		    w_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.bound(w, w_upper);

	    TupleSet h_upper = factory.noneOf(2);
	    for (int i=0;i<FRAMES;i++) {
		ti = universe.atom(i);
		for (int j=GRIDMINH;j<=MAXY;j++) {
		    h_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))));
		}
	    }
	    bounds.bound(h, h_upper);

	    // build constraint formula

	    IntExpression x_0 = IntConstant.constant(0);
	    IntExpression x_1 = IntConstant.constant(1);
	    IntExpression x_2 = IntConstant.constant(2);
	    IntExpression x_3 = IntConstant.constant(3);
	    IntExpression x_mx = IntConstant.constant(MAXX);
	    IntExpression y_my = IntConstant.constant(MAXY);

	    // w * scale = wOrig, h * scale = hOrig
	    Variable va = Variable.unary("va");
	    Formula f10 = va.join(wOrig).sum().eq(va.join(w).sum().multiply(va.join(scale).sum()));
	    Formula f20 = va.join(hOrig).sum().eq(va.join(h).sum().multiply(va.join(scale).sum()));
	    Formula f30 = f10.and(f20).forAll(va.oneOf(idxs));

	    // x2 < MAXX, y2 < MAXY
	    Formula f110 = va.join(x).sum().plus(va.join(w).sum()).lt(x_mx);
	    Formula f120 = va.join(y).sum().plus(va.join(h).sum()).lt(y_my);
	    Formula f130 = f110.and(f120).forAll(va.oneOf(idxs));

	    // no overlaps
	    Variable vb = Variable.unary("vb");
	    Variable vc = Variable.unary("vc");
	    Formula f210 = vb.join(x).sum().gt(vc.join(x).sum().plus(vc.join(w).sum()));
	    Formula f215 = vc.join(x).sum().gt(vb.join(x).sum().plus(vb.join(w).sum()));
	    Formula f220 = vb.join(y).sum().gt(vc.join(y).sum().plus(vc.join(h).sum()));
	    Formula f225 = vb.join(y).sum().gt(vb.join(y).sum().plus(vb.join(h).sum()));
	    Formula f230 = vb.product(vc).in(disjIdxs);
	    Formula f235 = f210.or(f220).or(f215).or(f225);
	    Formula f240 = f230.implies(f235);
	    Formula f250 = f240.forAll(vc.oneOf(idxs)).forAll(vb.oneOf(idxs));

	    Formula f990 = f30.and(f130).and(f250);

	    Formula formula = Formula.compose(FormulaOperator.AND, x.function(idxs,xm), 
					      y.function(idxs,xm), w.function(idxs,xm), 
					      h.function(idxs,ym), scale.function(idxs,sm), f990);
	    
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
		Iterator<Tuple> iterX = sol.instance().tuples(x).iterator();
		Iterator<Tuple> iterY = sol.instance().tuples(y).iterator();
		Iterator<Tuple> iterW = sol.instance().tuples(w).iterator();
		Iterator<Tuple> iterH = sol.instance().tuples(h).iterator();
		
		for(int i = 0; i < FRAMES; i++) {
		    framesX[i] = GRID * Integer.parseInt((String)iterX.next().atom(1));
		    framesY[i] = GRID * Integer.parseInt((String)iterY.next().atom(1));
		    framesW[i] = GRID * Integer.parseInt((String)iterW.next().atom(1));
		    framesH[i] = GRID * Integer.parseInt((String)iterH.next().atom(1));
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