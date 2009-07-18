
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
import java.util.ArrayList;
import java.util.Iterator;


class Node<V>
{
    public Integer key;
    public V value;
    public Node<V> left;
    public Node<V> right;
    public Node<V> parent;

    public Node(Integer key, V value, Node<V> left, Node<V> right) {
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
        if (left  != null)  left.parent = this;
        if (right != null) right.parent = this;
        this.parent = null;
    }
}

public class BSTree<V>
{
    public static final boolean VERIFY_BSTREE = true;
    private static final int INDENT_STEP = 4;

    public Node<V> root;
    
    List<Integer> nodes;

    final CyclicBarrier barrier;    

    // solver related
    private boolean waitingForSolver;
    private static int solRootNodeIdx;
    private static int [] solNodeKeys;
    private static int [] solNodeLefts;
    private static int [] solNodeRights;

    // Constructor
    public BSTree() {
        root = null;
	nodes = new ArrayList<Integer>();
        verifyProperties();

	// init solver thread
	Runnable getSol = new Runnable() { 
		public void run() { 
		    System.out.println("Results are in!"); 		    
		    root = buildTreeRecursively(solRootNodeIdx);
		    waitingForSolver = false;
		} 

	    }; 
	barrier = new CyclicBarrier(1, getSol);
    }

    public void verifyProperties() {
        if (VERIFY_BSTREE) {
            verifyProperty1(root);
        }
    }

    private static void verifyProperty1(Node<?> n) {
        assert true;
    }


    private Node<V> lookupNode(Integer key) {
        Node<V> n = root;
        while (n != null) {
            int compResult = key.compareTo(n.key);
            if (compResult == 0) {
                return n;
            } else if (compResult < 0) {
                n = n.left;
            } else {
                assert compResult > 0;
                n = n.right;
            }
        }
        return n;
    }

    public V lookup(Integer key) {
        Node<V> n = lookupNode(key);
        return n == null ? null : n.value;
    }

    private void replaceNode(Node<V> oldn, Node<V> newn) {
        if (oldn.parent == null) {
            root = newn;
        } else {
            if (oldn == oldn.parent.left)
                oldn.parent.left = newn;
            else
                oldn.parent.right = newn;
        }
        if (newn != null) {
            newn.parent = oldn.parent;
        }
    }

    public void insert(Integer key, V value) {
        Node<V> insertedNode = new Node<V>(key, value, null, null);
	nodes.add(key);
	try {
	    if (key == 13) { int i = 1/0;  } /* FIXME - THROW EXCEPTION HERE */
	    if (root == null) {
		root = insertedNode;
	    } else {
		Node<V> n = root;
		while (true) {
		    int compResult = key.compareTo(n.key);
		    if (compResult == 0) {
			n.value = value;
			return;
		    } else if (compResult < 0) {
			if (n.left == null) {
			    n.left = insertedNode;
			    break;
			} else {
			    n = n.left;
			}
		    } else {
			assert compResult > 0;
			if (n.right == null) {
			    n.right = insertedNode;
			    break;
			} else {
			    n = n.right;
			}
		    }
		}
		
		insertedNode.parent = n;
	    }
	    verifyProperties();
	} catch (Throwable rte) {
	    callSolver(rte);
	} 
    }

    public void delete(Integer key) {
        Node<V> n = lookupNode(key);
        if (n == null)
            return;  // Key not found, do nothing
        if (n.left != null && n.right != null) {
            // Copy key/value from predecessor and then delete it instead
            Node<V> pred = maximumNode(n.left);
            n.key   = pred.key;
            n.value = pred.value;
            n = pred;
        }

        assert n.left == null || n.right == null;
        Node<V> child = (n.right == null) ? n.left : n.right;
        replaceNode(n, child);
	nodes.remove(key);
        verifyProperties();
    }

    private static <V> Node<V> maximumNode(Node<V> n) {
        assert n != null;
        while (n.right != null) {
            n = n.right;
        }
        return n;
    }

    public void print() {
        printHelper(root, 0);
	System.out.println("---------------------------------------" + nodes.size() + " nodes");
    }

    private static void printHelper(Node<?> n, int indent) {
        if (n == null) {
            System.out.println("<empty tree>");
            return;
        }
        if (n.right != null) {
            printHelper(n.right, indent + INDENT_STEP);
        }
        for (int i = 0; i < indent; i++)
            System.out.print(" "); 
            System.out.println(n.key);
        if (n.left != null) {
            printHelper(n.left, indent + INDENT_STEP);
        }
    }
    
    // builds the BSTree from solution obtainted by solver
    public Node<V> buildTreeRecursively(int nodeIdx) { 
	
	Node <V> newNode, left, right;
	left = right = null;
	if (solNodeLefts[nodeIdx] != -1) {
	    left = buildTreeRecursively(solNodeLefts[nodeIdx]);
	}
	if (solNodeRights[nodeIdx] != -1) {
	    right = buildTreeRecursively(solNodeRights[nodeIdx]);
	}
        newNode = new Node<V>(solNodeKeys[nodeIdx], null , left, right);
	if (solNodeLefts[nodeIdx] != -1) {
	    left.parent = newNode;
	}
	if (solNodeRights[nodeIdx] != -1) {
	    right.parent = newNode;
	}
	return newNode;
	
    }

    
    public void callSolver(Throwable rte) {

	System.out.println("!! " + rte + "... start solver fallback!");

	waitingForSolver = true;
	int numNodes = nodes.size();
	int[] keys = new int[numNodes];
	solNodeKeys = new int[numNodes];
	solNodeLefts = new int[numNodes];
	solNodeRights = new int[numNodes];

	int maxVal = 0;
	for (int i = 0; i < numNodes; i++) {
	    int val = nodes.get(i).intValue();
	    keys[i] = val;
	    if (val > maxVal) {
		maxVal = val;
	    }
	    solNodeLefts[i] = -1;
	    solNodeRights[i] = -1;
	}
	int maxInt = Math.max(maxVal,29);

	

	System.out.println("nodes: " + nodes);
	
	new SolverThread(barrier,keys,maxInt).start();

	while(waitingForSolver); // wait until solution is here
    }

    private static class SolverThread extends Thread { 
	CyclicBarrier barrier; 
	int maxInt;
	int[] keys;
	
	SolverThread(CyclicBarrier barrier, int[] keys, int maxInt) { 
	    this.barrier = barrier;
	    this.keys = keys;
	    this.maxInt = maxInt;
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

	public void run() { 
	    System.out.println("in thread..."); 

	    Relation BS = Relation.unary("BS");
	    Relation Red = Relation.unary("Red");
	    Relation Black = Relation.unary("Black");
	    Relation Node = Relation.unary("Node");
	    Relation Keys = Relation.unary("Keys");
	    Relation Root = Relation.nary("BSTree.root", 2);
	    Relation Value = Relation.nary("Node.value", 2);
	    Relation Left = Relation.nary("Node.left", 2);
	    Relation Right = Relation.nary("Node.right", 2);

	    int numNodes = keys.length;
	    int intBitWidth = 1+(int)Math.ceil((double)Math.log(maxInt+1)/(double)Math.log(2));
	    String[] atoms = new String[maxInt+4];
	    String[] nodes = new String[numNodes];

	    //            idxs = {  0,  1,  2,  3,  4,  5,  6,  7  }
	    //            keys = {  0,  8,  2,  6,  4,  9, 12, 13  }
	    /*
	    int [] parentFixes = {  2,  5,  3, -1,  2,  3,  5,  6 };
	    int [] leftFixes   = { -1, -1,  0,  2, -1,  1, -1, -1 };
	    int [] rightFixes  = { -1, -1,  4,  5, -1,  6,  7, -1 };
	    */


	    Object ti;
	    int i;
	    for(i=0;i<=maxInt;i++) {
		atoms[i] = Integer.toString(i); 
	    }
	    for(i=0;i<numNodes;i++) {
		nodes[i] = atoms[i];
	    }
	    atoms[maxInt+1] = "BS"; 
	    atoms[maxInt+2] = "Red"; 
	    atoms[maxInt+3] = "Black"; 
	    println(atoms);
	    System.out.println(numNodes);
	    System.out.println(maxInt);
	    List<String> atomlist = Arrays.asList(atoms);

	    Universe universe = new Universe(atomlist);
	    TupleFactory factory = universe.factory();
	    Bounds bounds = new Bounds(universe);

	    for(i=0;i<=maxInt;i++) {
		ti = Integer.toString(i); 
		bounds.boundExactly(i,factory.range(factory.tuple(ti),factory.tuple(ti)));
	    }

	    TupleSet BS_upper = factory.noneOf(1);
	    BS_upper.add(factory.tuple("BS"));
	    bounds.boundExactly(BS, BS_upper);
	    
	    TupleSet Red_upper = factory.noneOf(1);
	    Red_upper.add(factory.tuple("Red"));
	    bounds.boundExactly(Red, Red_upper);
	    
	    TupleSet Black_upper = factory.noneOf(1);
	    Black_upper.add(factory.tuple("Black"));
	    bounds.boundExactly(Black, Black_upper);
	    
	    TupleSet Node_upper = factory.noneOf(1);
	    TupleSet Keys_upper = factory.noneOf(1);
	    TupleSet Root_upper = factory.noneOf(2);
	    TupleSet Value_upper = factory.noneOf(2);
	    TupleSet Left_upper = factory.noneOf(2);
	    TupleSet Right_upper = factory.noneOf(2);
	    for(i=0;i<numNodes;i++) {
		Object n = nodes[i];
		Node_upper.add(factory.tuple(n));
		Keys_upper.add(factory.tuple(atoms[keys[i]]));
		Root_upper.add(factory.tuple("BS").product(factory.tuple(n)));
		/*
		if (leftFixes[i] != -1) {
		    Left_upper.add(factory.tuple(n).product(factory.tuple(nodes[leftFixes[i]])));
		}
		if (rightFixes[i] != -1) {
		    Right_upper.add(factory.tuple(n).product(factory.tuple(nodes[rightFixes[i]])));
		}
		*/
		for(int j=0;j<numNodes;j++) {
		    Object n2 = nodes[j];		    
			//if (leftFixes[i] == -1) {
			Left_upper.add(factory.tuple(n).product(factory.tuple(n2)));
			//}
			//if (rightFixes[i] == -1) {
			Right_upper.add(factory.tuple(n).product(factory.tuple(n2)));
			//}

		}
		/*
		for(j=0;j<numNodes;j++) {
		    Value_upper.add(factory.tuple(n).product(factory.tuple(atoms[keys[j]])));
		}
		*/
		Value_upper.add(factory.tuple(n).product(factory.tuple(atoms[keys[i]])));
	    }
	    bounds.boundExactly(Node, Node_upper);
	    bounds.boundExactly(Keys, Keys_upper);
	    bounds.bound(Root, Root_upper);
	    //bounds.bound(Value, Value_upper);
	    bounds.boundExactly(Value, Value_upper);
	    bounds.bound(Left, Left_upper);
	    bounds.bound(Right, Right_upper);


	    Variable x15=Variable.unary("this");
	    Decls x14=x15.oneOf(BS);
	    Expression x18=x15.join(Root);
	    Formula x17=x18.one();
	    Formula x19=x18.in(Node);
	    Formula x16=x17.and(x19);
	    Formula x13=x16.forAll(x14);
	    Expression x21=Root.join(Expression.UNIV);
	    Formula x20=x21.in(BS);
	    Variable x25=Variable.unary("this");
	    Decls x24=x25.oneOf(Node);
	    Expression x28=x25.join(Value);
	    Formula x27=x28.one();
	    Formula x29=x28.in(Expression.INTS);
	    Formula x26=x27.and(x29);
	    Formula x23=x26.forAll(x24);
	    Expression x32=Value.join(Expression.UNIV);
	    Formula x31=x32.in(Node);
	    Variable x35=Variable.unary("this");
	    Decls x34=x35.oneOf(Node);
	    Expression x38=x35.join(Left);
	    Formula x37=x38.lone();
	    Formula x39=x38.in(Node);
	    Formula x36=x37.and(x39);
	    Formula x33=x36.forAll(x34);
	    Expression x41=Left.join(Expression.UNIV);
	    Formula x40=x41.in(Node);
	    Variable x44=Variable.unary("this");
	    Decls x43=x44.oneOf(Node);
	    Expression x47=x44.join(Right);
	    Formula x46=x47.lone();
	    Formula x48=x47.in(Node);
	    Formula x45=x46.and(x48);
	    Formula x42=x45.forAll(x43);
	    Expression x50=Right.join(Expression.UNIV);
	    Formula x49=x50.in(Node);
	    Variable x53=Variable.unary("n");
	    Decls x52=x53.oneOf(Node);
	    Expression x58=Left.union(Right);
	    Expression x57=x58.closure();
	    Expression x56=x53.join(x57);
	    Formula x55=x53.in(x56);
	    Formula x54=x55.not();
	    Formula x51=x54.forAll(x52);
	    Variable x61=Variable.unary("n");
	    Decls x60=x61.oneOf(Node);
	    Variable x65=Variable.unary("p");
	    Decls x64=x65.oneOf(Node);
	    Expression x68=Left.union(Right);
	    Expression x67=x65.join(x68);
	    Formula x66=x67.eq(x61);
	    Expression x63=x66.comprehension(x64);
	    Formula x62=x63.lone();
	    Formula x59=x62.forAll(x60);
	    Expression x71=Node.join(Value);
	    IntExpression x70=x71.count();
	    IntExpression x72=Node.count();
	    Formula x69=x70.eq(x72);
	    Variable x76=Variable.unary("n");
	    Decls x75=x76.oneOf(Node);
	    Variable x78=Variable.unary("c");
	    Expression x80=x76.join(Left);
	    Expression x83=Left.union(Right);
	    Expression x82=x83.closure();
	    Expression x89=Expression.INTS;
	    Expression x88=x89.union(BS);
	    Expression x87=x88.union(Node);
	    Expression x86=x87.product(Expression.UNIV);
	    Expression x84=Expression.IDEN.intersection(x86);
	    Expression x81=x82.union(x84);
	    Expression x79=x80.join(x81);
	    Decls x77=x78.oneOf(x79);
	    Decls x74=x75.and(x77);
	    Expression x92=x78.join(Value);
	    IntExpression x91=x92.sum();
	    Expression x94=x76.join(Value);
	    IntExpression x93=x94.sum();
	    Formula x90=x91.lt(x93);
	    Formula x73=x90.forAll(x74);
	    Variable x98=Variable.unary("n");
	    Decls x97=x98.oneOf(Node);
	    Variable x100=Variable.unary("c");
	    Expression x102=x98.join(Right);
	    Expression x105=Left.union(Right);
	    Expression x104=x105.closure();
	    Expression x107=x87.product(Expression.UNIV);
	    Expression x106=Expression.IDEN.intersection(x107);
	    Expression x103=x104.union(x106);
	    Expression x101=x102.join(x103);
	    Decls x99=x100.oneOf(x101);
	    Decls x96=x97.and(x99);
	    Expression x110=x100.join(Value);
	    IntExpression x109=x110.sum();
	    Expression x112=x98.join(Value);
	    IntExpression x111=x112.sum();
	    Formula x108=x109.gt(x111);
	    Formula x95=x108.forAll(x96);
	    Expression x118=BS.join(Root);
	    Expression x121=Left.union(Right);
	    Expression x120=x121.closure();
	    Expression x123=x87.product(Expression.UNIV);
	    Expression x122=Expression.IDEN.intersection(x123);
	    Expression x119=x120.union(x122);
	    Expression x117=x118.join(x119);
	    Expression x116=x117.join(Value);
	    Formula fVals = x116.eq(Keys);
	    
	    Formula x207=BS.eq(BS);
	    Formula x208=Node.eq(Node);
	    Formula x209=Root.eq(Root);
	    Formula x210=Value.eq(Value);
	    Formula x211=Left.eq(Left);
	    Formula x212=Right.eq(Right);
	    Formula x12=Formula.compose(FormulaOperator.AND, x13, x20, x23, x31, x33, x40, x42, x49, x51, x59, x69, x73, x95, x207, x208, x209, x210, x211, x212, fVals);
	    
	    Solver solver = new Solver();
	    solver.options().setSolver(SATFactory.MiniSat);
	    solver.options().setBitwidth(intBitWidth);
	    solver.options().setFlatten(false);
	    solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
	    solver.options().setSymmetryBreaking(20);
	    solver.options().setSkolemDepth(0);
	    System.out.println("Solving...");
	    System.out.flush();
	    Solution sol = solver.solve(x12,bounds);
	    System.out.println(sol.toString());
	    
	    // extract solution
	    Iterator<Tuple> rootRel = sol.instance().tuples(Root).iterator();
	    Iterator<Tuple> valueRel = sol.instance().tuples(Value).iterator();
	    Iterator<Tuple> leftRel = sol.instance().tuples(Left).iterator();
	    Iterator<Tuple> rightRel = sol.instance().tuples(Right).iterator();
	    solRootNodeIdx = Integer.parseInt((String)rootRel.next().atom(1));
	    for (i=0;i<numNodes;i++) {
		solNodeKeys[i] = Integer.parseInt((String)valueRel.next().atom(1));
	    }
	    while (leftRel.hasNext()) {
		Tuple item = leftRel.next();
		solNodeLefts[Integer.parseInt((String)item.atom(0))] = Integer.parseInt((String)item.atom(1));
	    }
	    while (rightRel.hasNext()) {
		Tuple item = rightRel.next();
		solNodeRights[Integer.parseInt((String)item.atom(0))] = Integer.parseInt((String)item.atom(1));
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

    public static void main(String[] args) {
        BSTree<Integer> t = new BSTree<Integer>();
        t.print();

	int [] a = {23,8,0,1,2,4,9,16,17,5,22,13}; //20,21,19,11,24,10,18,6,14,7,15,3,24};
	for (int i=0; i < a.length; i++) {
	    t.insert(a[i],null);
	    t.print();
	}
    }

}


/*

}}


 */