/* Copyright (c) 2009 the authors listed at the following URL, and/or
the authors of referenced articles or incorporated external code:
http://en.literateprograms.org/Red-black_tree_(Java)?action=history&offset=20080130152141

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Retrieved from: http://en.literateprograms.org/Red-black_tree_(Java)?oldid=12375
*/

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
import java.util.Random;


enum Color { RED, BLACK }

class Node<V>
{
    public Integer key;
    public V value;
    public Node<V> left;
    public Node<V> right;
    public Node<V> parent;
    public Color color;

    public Node(Integer key, V value, Color nodeColor, Node<V> left, Node<V> right) {
        this.key = key;
        this.value = value;
        this.color = nodeColor;
        this.left = left;
        this.right = right;
        if (left  != null)  left.parent = this;
        if (right != null) right.parent = this;
        this.parent = null;
    }

    public Node<V> grandparent() {
        assert parent != null; // Not the root node
        assert parent.parent != null; // Not child of root
        return parent.parent;
    }

    public Node<V> sibling() {
        assert parent != null; // Root node has no sibling
        if (this == parent.left)
            return parent.right;
        else
            return parent.left;
    }

    public Node<V> uncle() {
        assert parent != null; // Root node has no uncle
        assert parent.parent != null; // Children of root have no uncle
        return parent.sibling();
    }

}

public class RBTree<V>
{
    public static final boolean VERIFY_RBTREE = true;
    private static final int INDENT_STEP = 4;

    public Node<V> root;
    
    static List<Node<?>> nodes;

    final CyclicBarrier barrier;    

    // solver related
    private static int TEST_METHOD = 0;
    private static int MAX_SIZE;
    private static int maxInt;
    private static int intBitWidth;
    private boolean waitingForSolver;
    private static int solRootNodeIdx;
    private static int [] solNodeKeys;
    private static Color [] solNodeColors;
    private static int [] solNodeLefts;
    private static int [] solNodeRights;

    // Constructor
    public RBTree() {
        root = null;
	nodes = new ArrayList<Node<?>>();
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
        if (VERIFY_RBTREE) {
            verifyProperty1(root);
            verifyProperty2(root);
            // Property 3 is implicit
            verifyProperty4(root);
            verifyProperty5(root);
        }
    }

    private static void verifyProperty1(Node<?> n) {
        assert nodeColor(n) == Color.RED || nodeColor(n) == Color.BLACK;
        if (n == null) return;
        verifyProperty1(n.left);
        verifyProperty1(n.right);
    }

    private static void verifyProperty2(Node<?> root) {
        assert nodeColor(root) == Color.BLACK;
    }

    private static Color nodeColor(Node<?> n) {
        return n == null ? Color.BLACK : n.color;
    }

    private static void verifyProperty4(Node<?> n) {
        if (nodeColor(n) == Color.RED) {
            assert nodeColor(n.left)   == Color.BLACK;
            assert nodeColor(n.right)  == Color.BLACK;
            assert nodeColor(n.parent) == Color.BLACK;
        }
        if (n == null) return;
        verifyProperty4(n.left);
        verifyProperty4(n.right);
    }

    private static void verifyProperty5(Node<?> root) {
        verifyProperty5Helper(root, 0, -1);
    }

    private static int verifyProperty5Helper(Node<?> n, int blackCount, int pathBlackCount) {
        if (nodeColor(n) == Color.BLACK) {
            blackCount++;
        }
        if (n == null) {
            if (pathBlackCount == -1) {
                pathBlackCount = blackCount;
            } else {
                assert blackCount == pathBlackCount;
            }
            return pathBlackCount;
        }
        pathBlackCount = verifyProperty5Helper(n.left,  blackCount, pathBlackCount);
        pathBlackCount = verifyProperty5Helper(n.right, blackCount, pathBlackCount);
        return pathBlackCount;
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

    private void rotateLeft(Node<V> n) {
        Node<V> r = n.right;
        replaceNode(n, r);
        n.right = r.left;
        if (r.left != null) {
            r.left.parent = n;
        }
        r.left = n;
        n.parent = r;
    }

    private void rotateRight(Node<V> n) {
        Node<V> l = n.left;
        replaceNode(n, l);
        n.left = l.right;
        if (l.right != null) {
            l.right.parent = n;
        }
        l.right = n;
        n.parent = l;
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

    public Node<V> getNewNodeParent(Integer key) {
	return getNewNodeParent(key, null, null);
    }
    
    public Node<V> getNewNodeParent(Integer key, V value, Node<V> insertedNode) {
	
	Node<V> n = root;
	while (true) {
	    int compResult = key.compareTo(n.key);
	    if (compResult == 0) {
		if (insertedNode != null) {
		    n.value = value;
		}
		return n;
	    } else if (compResult < 0) {
		if (n.left == null) {
		    if (insertedNode != null) {
			n.left = insertedNode;
		    }
		    break;
		} else {
		    n = n.left;
		}
	    } else {
		assert compResult > 0;
		if (n.right == null) {
		    if (insertedNode != null) {
			n.right = insertedNode;
		    }
		    break;
		} else {
		    n = n.right;
		}
	    }
	}
	return n;
    }

    public void insert(Integer key, V value) {
        Node<V> insertedNode = new Node<V>(key, value, Color.RED, null, null);
	nodes.add(insertedNode);
	try {
	    if (TEST_METHOD == 0 && nodes.size() == MAX_SIZE) { int i = 1/0; /* FIXME - THROW EXCEPTION HERE */ }
	    if (root == null) {
		root = insertedNode;
	    } else {
		Node<V> n = getNewNodeParent(key, value, insertedNode);		
		insertedNode.parent = n;
	    }
	    insertCase1(insertedNode);
	    verifyProperties();
	} catch (Throwable rte) {
	    int[][] boundFixes = insert_bounds(insertedNode);
	    callSolver(rte, boundFixes);
	} 
    }

    private void insertCase1(Node<V> n) {
        if (n.parent == null)
            n.color = Color.BLACK;
        else
            insertCase2(n);
    }

    private void insertCase2(Node<V> n) {
        if (nodeColor(n.parent) == Color.BLACK)
            return; // Tree is still valid
        else
            insertCase3(n);
    }

    void insertCase3(Node<V> n) {
        if (nodeColor(n.uncle()) == Color.RED) {
            n.parent.color = Color.BLACK;
            n.uncle().color = Color.BLACK;
            n.grandparent().color = Color.RED;
            insertCase1(n.grandparent());
        } else {
            insertCase4(n);
        }
    }

    void insertCase4(Node<V> n) {
        if (n == n.parent.right && n.parent == n.grandparent().left) {
            rotateLeft(n.parent);
            n = n.left;
        } else if (n == n.parent.left && n.parent == n.grandparent().right) {
            rotateRight(n.parent);
            n = n.right;
        }
        insertCase5(n);
    }

    void insertCase5(Node<V> n) {
        n.parent.color = Color.BLACK;
        n.grandparent().color = Color.RED;
        if (n == n.parent.left && n.parent == n.grandparent().left) {
            rotateRight(n.grandparent());
        } else {
            assert n == n.parent.right && n.parent == n.grandparent().right;
            rotateLeft(n.grandparent());
        }
    }


    ////////////////////////

    private void insertObserveCase1(Node<?> n, List<Integer> affectedNodes) {
	System.out.println("case1");
        if (n.parent == null)
            affectedNodes.add(n.key);
        else
            insertObserveCase2(n, affectedNodes);
    }

    private void insertObserveCase2(Node<?> n, List<Integer> affectedNodes) {
	System.out.println("case2");
        if (nodeColor(n.parent) == Color.BLACK) {
	    affectedNodes.add(n.parent.key);
            return; // Tree is still valid
	} else
            insertObserveCase3(n, affectedNodes);
    }

    void insertObserveCase3(Node<?> n, List<Integer> affectedNodes) {
        if (nodeColor(n.uncle()) == Color.RED) {
	    System.out.println("case3.1");
	    affectedNodes.add(n.parent.key);
	    affectedNodes.add(n.uncle().key);
	    affectedNodes.add(n.grandparent().key);
            insertObserveCase1(n.grandparent(), affectedNodes);
        } else {
	    System.out.println("case3.2");
            insertObserveCase4(n, affectedNodes);
        }
    }

    void insertObserveCase4(Node<?> n, List<Integer> affectedNodes) {
	if (n.parent != null) 
	    affectedNodes.add(n.parent.key);
	if (n.grandparent() != null) 
	    affectedNodes.add(n.grandparent().key);
	if (n.parent.grandparent() != null) 
	    affectedNodes.add(n.parent.grandparent().key);
        if (n == n.parent.right && n.parent == n.grandparent().left) {
	    affectedNodes.add(n.parent.left.key);
	    if (n.left != null) 
		affectedNodes.add(n.left.key);
	    if (n.right != null) 
		affectedNodes.add(n.right.key);
        } else if (n == n.parent.left && n.parent == n.grandparent().right) {
	    affectedNodes.add(n.parent.right.key);
	    if (n.left != null) 
		affectedNodes.add(n.left.key);
	    if (n.right != null) 
		affectedNodes.add(n.right.key);            
        }
    }


    ///////////////////////

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
        if (nodeColor(n) == Color.BLACK) {
            n.color = nodeColor(child);
            deleteCase1(n);
        }
        replaceNode(n, child);
	nodes.remove(n);
        verifyProperties();
    }

    private static <V> Node<V> maximumNode(Node<V> n) {
        assert n != null;
        while (n.right != null) {
            n = n.right;
        }
        return n;
    }

    private void deleteCase1(Node<V> n) {
        if (n.parent == null)
            return;
        else
            deleteCase2(n);
    }

    private void deleteCase2(Node<V> n) {
        if (nodeColor(n.sibling()) == Color.RED) {
            n.parent.color = Color.RED;
            n.sibling().color = Color.BLACK;
            if (n == n.parent.left)
                rotateLeft(n.parent);
            else
                rotateRight(n.parent);
        }
        deleteCase3(n);
    }

    private void deleteCase3(Node<V> n) {
        if (nodeColor(n.parent) == Color.BLACK &&
            nodeColor(n.sibling()) == Color.BLACK &&
            nodeColor(n.sibling().left) == Color.BLACK &&
            nodeColor(n.sibling().right) == Color.BLACK)
        {
            n.sibling().color = Color.RED;
            deleteCase1(n.parent);
        }
        else
            deleteCase4(n);
    }

    private void deleteCase4(Node<V> n) {
        if (nodeColor(n.parent) == Color.RED &&
            nodeColor(n.sibling()) == Color.BLACK &&
            nodeColor(n.sibling().left) == Color.BLACK &&
            nodeColor(n.sibling().right) == Color.BLACK)
        {
            n.sibling().color = Color.RED;
            n.parent.color = Color.BLACK;
        }
        else
            deleteCase5(n);
    }

    private void deleteCase5(Node<V> n) {
        if (n == n.parent.left &&
            nodeColor(n.sibling()) == Color.BLACK &&
            nodeColor(n.sibling().left) == Color.RED &&
            nodeColor(n.sibling().right) == Color.BLACK)
        {
            n.sibling().color = Color.RED;
            n.sibling().left.color = Color.BLACK;
            rotateRight(n.sibling());
        }
        else if (n == n.parent.right &&
                 nodeColor(n.sibling()) == Color.BLACK &&
                 nodeColor(n.sibling().right) == Color.RED &&
                 nodeColor(n.sibling().left) == Color.BLACK)
        {
            n.sibling().color = Color.RED;
            n.sibling().right.color = Color.BLACK;
            rotateLeft(n.sibling());
        }
        deleteCase6(n);
    }

    private void deleteCase6(Node<V> n) {
        n.sibling().color = nodeColor(n.parent);
        n.parent.color = Color.BLACK;
        if (n == n.parent.left) {
            assert nodeColor(n.sibling().right) == Color.RED;
            n.sibling().right.color = Color.BLACK;
            rotateLeft(n.parent);
        }
        else
        {
            assert nodeColor(n.sibling().left) == Color.RED;
            n.sibling().left.color = Color.BLACK;
            rotateRight(n.parent);
        }
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
        if (n.color == Color.BLACK)
            System.out.println(n.key);
        else
            System.out.println("<" + n.key + ">");
        if (n.left != null) {
            printHelper(n.left, indent + INDENT_STEP);
        }
    }
    
    // builds the RBTree from solution obtainted by solver
    public Node<V> buildTreeRecursively(int nodeIdx) { 
	
	Node <V> newNode, left, right;
	left = right = null;
	if (solNodeLefts[nodeIdx] != -1) {
	    left = buildTreeRecursively(solNodeLefts[nodeIdx]);
	}
	if (solNodeRights[nodeIdx] != -1) {
	    right = buildTreeRecursively(solNodeRights[nodeIdx]);
	}
        newNode = new Node<V>(solNodeKeys[nodeIdx], null , solNodeColors[nodeIdx], left, right);
	if (solNodeLefts[nodeIdx] != -1) {
	    left.parent = newNode;
	}
	if (solNodeRights[nodeIdx] != -1) {
	    right.parent = newNode;
	}
	return newNode;
	
    }

    public void initSolverProblem() {

	int numNodes = nodes.size();
	solNodeKeys = new int[numNodes];
	solNodeColors = new Color[numNodes];
	solNodeLefts = new int[numNodes];
	solNodeRights = new int[numNodes];

	int maxVal = 0;
	for (int i = 0; i < numNodes; i++) {
	    int val = nodes.get(i).key;
	    if (val > maxVal) {
		maxVal = val;
	    }
	    solNodeLefts[i] = -1;
	    solNodeRights[i] = -1;
	}
	maxInt = Math.max(maxVal+1,MAX_SIZE);
	intBitWidth = 1+(int)Math.ceil((double)Math.log(maxInt+1)/(double)Math.log(2));

    }
    // set bounds via insert method invariants including @modifies clause...
    public int[][] insert_bounds(Node<V> node) {
	    
	initSolverProblem();
	int numNodes = nodes.size();
	
	int [] nodeIdxs    = new int[maxInt];
	int [] leftFixes   = new int[numNodes];
	int [] rightFixes  = new int[numNodes];	
	int [] parentFixes = new int[numNodes];	
	int [] colorFixes = new int[numNodes];	

	for(int i=0;i<numNodes;i++) {
	    int k = nodes.get(i).key;
	    nodeIdxs[k] = i;
	}
	int key = node.key;
	Node<V> p = getNewNodeParent(key);
  
	node.parent = p; // fixme
       
	System.out.println("new node will be a child of: " + p.key);

	List<Integer> affectedNodes = new ArrayList<Integer>();

	insertObserveCase1(node, affectedNodes);

	System.out.println("affected nodes will be: " + affectedNodes);

	for(int i=0;i<numNodes;i++) {
	    Node<?> nd = nodes.get(i);
	    int k = nd.key;
	    Boolean unfixed = k == key || affectedNodes.contains(k);
	    leftFixes[i] = unfixed ? -1 : nd.left == null ? -2 : nodeIdxs[nd.left.key];
	    rightFixes[i] = unfixed ? -1 : nd.right == null ? -2 : nodeIdxs[nd.right.key];
	    parentFixes[i] = unfixed ? -1 : nd.parent == null? -2 : nodeIdxs[nd.parent.key];
	    colorFixes[i] = unfixed ? -1 : nd.color == Color.BLACK ? 0 : 1;
	}	
	System.out.println(numNodes);
	System.out.println(maxInt);
	intprintln(nodeIdxs);
	intprintln(leftFixes);
	intprintln(rightFixes);
	intprintln(parentFixes);
	intprintln(colorFixes);
	
	int[][] boundFixes = {leftFixes, rightFixes, parentFixes, colorFixes};
	return(boundFixes);
    }
    
    public void callSolver(Throwable rte, int[][] boundFixes) {

	System.out.println("!! " + rte + "... start solver fallback!");

	waitingForSolver = true;

	//System.out.println("nodes: " + nodes);
	
	new SolverThread(barrier,boundFixes).start();

	while(waitingForSolver); // wait until solution is here
    }

    private static class SolverThread extends Thread { 
	CyclicBarrier barrier; 
	int[][] boundFixes;
	
	SolverThread(CyclicBarrier barrier,int[][] boundFixes) { 
	    this.barrier = barrier;
	    this.boundFixes = boundFixes;
	} 
	
	public void run() { 
	    System.out.println("in thread..."); 

	    Relation RB = Relation.unary("RB");
	    Relation Red = Relation.unary("Red");
	    Relation Black = Relation.unary("Black");
	    Relation Node = Relation.unary("Node");
	    Relation Keys = Relation.unary("Keys");
	    Relation Root = Relation.nary("RBTree.root", 2);
	    Relation RelColor = Relation.nary("Node.color", 2);
	    Relation Parent = Relation.nary("Node.parent", 2);
	    Relation Value = Relation.nary("Node.value", 2);
	    Relation Left = Relation.nary("Node.left", 2);
	    Relation Right = Relation.nary("Node.right", 2);

	    int numNodes = nodes.size();

	    String[] atoms = new String[maxInt+4];
	    String[] nodeAtoms = new String[numNodes];

	    int [] leftFixes   = boundFixes[0];
	    int [] rightFixes  = boundFixes[1];
	    int [] parentFixes = boundFixes[2];
	    int [] colorFixes = boundFixes[3];

	    Object ti;
	    int i;
	    for(i=0;i<=maxInt;i++) {
		atoms[i] = Integer.toString(i); 
	    }
	    for(i=0;i<numNodes;i++) {
		nodeAtoms[i] = atoms[i];
	    }
	    atoms[maxInt+1] = "RB"; 
	    atoms[maxInt+2] = "Red"; 
	    atoms[maxInt+3] = "Black"; 

	    List<String> atomlist = Arrays.asList(atoms);

	    Universe universe = new Universe(atomlist);
	    TupleFactory factory = universe.factory();
	    Bounds bounds = new Bounds(universe);

	    for(i=0;i<=maxInt;i++) {
		ti = Integer.toString(i); 
		bounds.boundExactly(i,factory.range(factory.tuple(ti),factory.tuple(ti)));
	    }

	    TupleSet RB_upper = factory.noneOf(1);
	    RB_upper.add(factory.tuple("RB"));

	    
	    TupleSet Red_upper = factory.noneOf(1);
	    Red_upper.add(factory.tuple("Red"));
	    
	    TupleSet Black_upper = factory.noneOf(1);
	    Black_upper.add(factory.tuple("Black"));
	    
	    TupleSet Node_upper = factory.noneOf(1);
	    TupleSet Keys_upper = factory.noneOf(1);
	    TupleSet Root_upper = factory.noneOf(2);
	    TupleSet Color_lower = factory.noneOf(2);
	    TupleSet Color_upper = factory.noneOf(2);
	    TupleSet Parent_lower = factory.noneOf(2);
	    TupleSet Parent_upper = factory.noneOf(2);
	    TupleSet Value_upper = factory.noneOf(2);
	    TupleSet Left_lower = factory.noneOf(2);
	    TupleSet Left_upper = factory.noneOf(2);
	    TupleSet Right_lower = factory.noneOf(2);
	    TupleSet Right_upper = factory.noneOf(2);

	    for(i=0;i<numNodes;i++) {
		Object n = nodeAtoms[i];
		int k = nodes.get(i).key;
		Node_upper.add(factory.tuple(n));
		Keys_upper.add(factory.tuple(atoms[k]));
		Root_upper.add(factory.tuple("RB").product(factory.tuple(n)));
		if (colorFixes[i] >= 0) {
		    String c = colorFixes[i] == 0 ? "Black" : "Red";
		    Color_lower.add(factory.tuple(n).product(factory.tuple(c)));
		    Color_upper.add(factory.tuple(n).product(factory.tuple(c)));
		} else {
		    Color_upper.add(factory.tuple(n).product(factory.tuple("Red")));
		    Color_upper.add(factory.tuple(n).product(factory.tuple("Black")));
		}
		if (parentFixes[i] >= 0) {
		    Parent_lower.add(factory.tuple(n).product(factory.tuple(nodeAtoms[parentFixes[i]])));
		    Parent_upper.add(factory.tuple(n).product(factory.tuple(nodeAtoms[parentFixes[i]])));
		}
		if (leftFixes[i] >= 0) {
		    Left_lower.add(factory.tuple(n).product(factory.tuple(nodeAtoms[leftFixes[i]])));
		    Left_upper.add(factory.tuple(n).product(factory.tuple(nodeAtoms[leftFixes[i]])));
		}
		if (rightFixes[i] >= 0) {
		    Right_lower.add(factory.tuple(n).product(factory.tuple(nodeAtoms[rightFixes[i]])));
		    Right_upper.add(factory.tuple(n).product(factory.tuple(nodeAtoms[rightFixes[i]])));
		}
		for(int j=0;j<numNodes;j++) {
		    Object n2 = nodeAtoms[j];		    
		    if (parentFixes[i] == -1) {
			Parent_upper.add(factory.tuple(n).product(factory.tuple(n2)));
		    }
		    if (leftFixes[i] == -1) {
			Left_upper.add(factory.tuple(n).product(factory.tuple(n2)));
		    }
		    if (rightFixes[i] == -1) {
			Right_upper.add(factory.tuple(n).product(factory.tuple(n2)));
		    }
		}
		Value_upper.add(factory.tuple(n).product(factory.tuple(atoms[k])));
	    }
	    bounds.boundExactly(RB, RB_upper);
	    bounds.boundExactly(Red, Red_upper);
	    bounds.boundExactly(Black, Black_upper);
	    bounds.boundExactly(Node, Node_upper);
	    bounds.boundExactly(Keys, Keys_upper);
	    bounds.bound(Root, Root_upper);
	    bounds.boundExactly(Value, Value_upper);
	    bounds.bound(RelColor, Color_upper);
	    bounds.bound(Parent, Parent_lower, Parent_upper);
	    bounds.bound(Left, Left_lower, Left_upper);
	    bounds.bound(Right, Right_lower, Right_upper);

	    Expression x18=Red.intersection(Black);
	    Formula x17=x18.no();
	    Variable x21=Variable.unary("this");
	    Decls x20=x21.oneOf(RB);
	    Expression x24=x21.join(Root);
	    Formula x23=x24.one();
	    Formula x25=x24.in(Node);
	    Formula x22=x23.and(x25);
	    Formula x19=x22.forAll(x20);
	    Expression x27=Root.join(Expression.UNIV);
	    Formula x26=x27.in(RB);
	    Variable x31=Variable.unary("this");
	    Decls x30=x31.oneOf(Node);
	    Expression x34=x31.join(RelColor);
	    Formula x33=x34.one();
	    Expression x36=Red.union(Black);
	    Formula x35=x34.in(x36);
	    Formula x32=x33.and(x35);
	    Formula x29=x32.forAll(x30);
	    Expression x38=RelColor.join(Expression.UNIV);
	    Formula x37=x38.in(Node);
	    Variable x41=Variable.unary("this");
	    Decls x40=x41.oneOf(Node);
	    Expression x44=x41.join(Value);
	    Formula x43=x44.one();
	    Formula x45=x44.in(Expression.INTS);
	    Formula x42=x43.and(x45);
	    Formula x39=x42.forAll(x40);
	    Expression x48=Value.join(Expression.UNIV);
	    Formula x47=x48.in(Node);
	    Variable x51=Variable.unary("this");
	    Decls x50=x51.oneOf(Node);
	    Expression x54=x51.join(Parent);
	    Formula x53=x54.lone();
	    Formula x55=x54.in(Node);
	    Formula x52=x53.and(x55);
	    Formula x49=x52.forAll(x50);
	    Expression x57=Parent.join(Expression.UNIV);
	    Formula x56=x57.in(Node);
	    Variable x60=Variable.unary("this");
	    Decls x59=x60.oneOf(Node);
	    Expression x63=x60.join(Left);
	    Formula x62=x63.lone();
	    Formula x64=x63.in(Node);
	    Formula x61=x62.and(x64);
	    Formula x58=x61.forAll(x59);
	    Expression x66=Left.join(Expression.UNIV);
	    Formula x65=x66.in(Node);
	    Variable x69=Variable.unary("this");
	    Decls x68=x69.oneOf(Node);
	    Expression x72=x69.join(Right);
	    Formula x71=x72.lone();
	    Formula x73=x72.in(Node);
	    Formula x70=x71.and(x73);
	    Formula x67=x70.forAll(x68);
	    Expression x75=Right.join(Expression.UNIV);
	    Formula x74=x75.in(Node);
	    Expression x78=RB.join(Root);
	    Expression x77=x78.join(Parent);
	    Formula x76=x77.no();
	    Variable x81=Variable.unary("n");
	    Decls x80=x81.oneOf(Node);
	    Expression x86=Left.union(Right);
	    Expression x85=x86.closure();
	    Expression x84=x81.join(x85);
	    Formula x83=x81.in(x84);
	    Formula x82=x83.not();
	    Formula x79=x82.forAll(x80);
	    Variable x89=Variable.unary("n");
	    Decls x88=x89.oneOf(Node);
	    Variable x93=Variable.unary("p");
	    Decls x92=x93.oneOf(Node);
	    Expression x96=Left.union(Right);
	    Expression x95=x93.join(x96);
	    Formula x94=x95.eq(x89);
	    Expression x91=x94.comprehension(x92);
	    Formula x90=x91.lone();
	    Formula x87=x90.forAll(x88);
	    Variable x100=Variable.unary("n1");
	    Decls x99=x100.oneOf(Node);
	    Variable x102=Variable.unary("n2");
	    Decls x101=x102.oneOf(Node);
	    Decls x98=x99.and(x101);
	    Expression x106=Left.union(Right);
	    Expression x105=x102.join(x106);
	    Formula x104=x100.in(x105);
	    Expression x108=x100.join(Parent);
	    Formula x107=x108.eq(x102);
	    Formula x103=x104.iff(x107);
	    Formula x97=x103.forAll(x98);
	    Expression x111=Node.join(Value);
	    IntExpression x110=x111.count();
	    IntExpression x112=Node.count();
	    Formula x109=x110.eq(x112);
	    Variable x116=Variable.unary("n");
	    Decls x115=x116.oneOf(Node);
	    Variable x118=Variable.unary("c");
	    Expression x120=x116.join(Left);
	    Expression x123=Left.union(Right);
	    Expression x122=x123.closure();
	    Expression x130=Expression.INTS;
	    Expression x129=x130.union(RB);
	    Expression x128=x129.union(x36);
	    Expression x127=x128.union(Node);
	    Expression x126=x127.product(Expression.UNIV);
	    Expression x124=Expression.IDEN.intersection(x126);
	    Expression x121=x122.union(x124);
	    Expression x119=x120.join(x121);
	    Decls x117=x118.oneOf(x119);
	    Decls x114=x115.and(x117);
	    Expression x133=x118.join(Value);
	    IntExpression x132=x133.sum();
	    Expression x135=x116.join(Value);
	    IntExpression x134=x135.sum();
	    Formula x131=x132.lt(x134);
	    Formula x113=x131.forAll(x114);
	    Variable x139=Variable.unary("n");
	    Decls x138=x139.oneOf(Node);
	    Variable x141=Variable.unary("c");
	    Expression x143=x139.join(Right);
	    Expression x146=Left.union(Right);
	    Expression x145=x146.closure();
	    Expression x148=x127.product(Expression.UNIV);
	    Expression x147=Expression.IDEN.intersection(x148);
	    Expression x144=x145.union(x147);
	    Expression x142=x143.join(x144);
	    Decls x140=x141.oneOf(x142);
	    Decls x137=x138.and(x140);
	    Expression x151=x141.join(Value);
	    IntExpression x150=x151.sum();
	    Expression x153=x139.join(Value);
	    IntExpression x152=x153.sum();
	    Formula x149=x150.gt(x152);
	    Formula x136=x149.forAll(x137);
	    Expression x156=RB.join(Root);
	    Expression x155=x156.join(RelColor);
	    Formula x154=x155.eq(Black);
	    Variable x159=Variable.unary("n");
	    Decls x158=x159.oneOf(Node);
	    Expression x163=x159.join(RelColor);
	    Formula x162=x163.eq(Red);
	    Formula x161=x162.not();
	    Expression x168=x159.join(Left);
	    Formula x167=x168.some();
	    Formula x166=x167.not();
	    Expression x171=x159.join(Left);
	    Expression x170=x171.join(RelColor);
	    Formula x169=x170.eq(Black);
	    Formula x165=x166.or(x169);
	    Expression x175=x159.join(Right);
	    Formula x174=x175.some();
	    Formula x173=x174.not();
	    Expression x178=x159.join(Right);
	    Expression x177=x178.join(RelColor);
	    Formula x176=x177.eq(Black);
	    Formula x172=x173.or(x176);
	    Formula x164=x165.and(x172);
	    Formula x160=x161.or(x164);
	    Formula x157=x160.forAll(x158);
	    Variable x182=Variable.unary("l1");
	    Decls x181=x182.oneOf(Node);
	    Variable x184=Variable.unary("l2");
	    Decls x183=x184.oneOf(Node);
	    Decls x180=x181.and(x183);
	    Expression x188=x182.intersection(x184);
	    Formula x187=x188.no();
	    Formula x186=x187.not();
	    Expression x194=x182.join(Left);
	    Formula x193=x194.no();
	    Expression x196=x182.join(Right);
	    Formula x195=x196.no();
	    Formula x192=x193.or(x195);
	    Expression x199=x184.join(Left);
	    Formula x198=x199.no();
	    Expression x201=x184.join(Right);
	    Formula x200=x201.no();
	    Formula x197=x198.or(x200);
	    Formula x191=x192.and(x197);
	    Formula x190=x191.not();
	    Variable x206=Variable.unary("n");
	    Expression x209=Parent.closure();
	    Expression x211=x127.product(Expression.UNIV);
	    Expression x210=Expression.IDEN.intersection(x211);
	    Expression x208=x209.union(x210);
	    Expression x207=x182.join(x208);
	    Decls x205=x206.oneOf(x207);
	    Expression x213=x206.join(RelColor);
	    Formula x212=x213.eq(Black);
	    Expression x204=x212.comprehension(x205);
	    IntExpression x203=x204.count();
	    Variable x217=Variable.unary("n");
	    Expression x220=Parent.closure();
	    Expression x222=x127.product(Expression.UNIV);
	    Expression x221=Expression.IDEN.intersection(x222);
	    Expression x219=x220.union(x221);
	    Expression x218=x184.join(x219);
	    Decls x216=x217.oneOf(x218);
	    Expression x224=x217.join(RelColor);
	    Formula x223=x224.eq(Black);
	    Expression x215=x223.comprehension(x216);
	    IntExpression x214=x215.count();
	    Formula x202=x203.eq(x214);
	    Formula x189=x190.or(x202);
	    Formula x185=x186.or(x189);
	    Formula x179=x185.forAll(x180);

	    Expression x230=RB.join(Root);
	    Expression x233=Left.union(Right);
	    Expression x232=x233.closure();
	    Expression x235=x127.product(Expression.UNIV);
	    Expression x234=Expression.IDEN.intersection(x235);
	    Expression x231=x232.union(x234);
	    Expression x229=x230.join(x231);
	    Expression x228=x229.join(Value);

	    Formula fVals = x228.eq(Keys);

	    Formula x319=RB.eq(RB);
	    Formula x320=Red.eq(Red);
	    Formula x321=Black.eq(Black);
	    Formula x322=Node.eq(Node);
	    Formula x323=Root.eq(Root);
	    Formula x324=RelColor.eq(RelColor);
	    Formula x325=Value.eq(Value);
	    Formula x326=Parent.eq(Parent);
	    Formula x327=Left.eq(Left);
	    Formula x328=Right.eq(Right);
	    Formula x16=Formula.compose(FormulaOperator.AND, x17, x19, x26, x29, x37, x39, x47, x49, x56, x58, x65, x67, x74, x76, x79, x87, x97, x109, x113, x136, x154, x157, x179, x319, x320, x321, x322, x323, x324, x325, x326, x327, x328, fVals);
	    
	    
	    Solver solver = new Solver();
	    solver.options().setSolver(SATFactory.MiniSat);
	    solver.options().setBitwidth(intBitWidth);
	    solver.options().setFlatten(false);
	    solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
	    solver.options().setSymmetryBreaking(20);
	    solver.options().setSkolemDepth(0);
	    System.out.println("Solving...");
	    System.out.flush();
	    Solution sol = solver.solve(x16,bounds);
	    System.out.println(sol.toString());
	    
	    // extract solution
	    Iterator<Tuple> rootRel = sol.instance().tuples(Root).iterator();
	    Iterator<Tuple> colorRel = sol.instance().tuples(RelColor).iterator();
	    Iterator<Tuple> valueRel = sol.instance().tuples(Value).iterator();
	    Iterator<Tuple> leftRel = sol.instance().tuples(Left).iterator();
	    Iterator<Tuple> rightRel = sol.instance().tuples(Right).iterator();
	    solRootNodeIdx = Integer.parseInt((String)rootRel.next().atom(1));
	    for (i=0;i<numNodes;i++) {
		solNodeColors[i] = ((String)colorRel.next().atom(1)) == "Black" ? Color.BLACK : Color.RED;
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
    
    public void intprintln(int [] l){
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
    
    public static void main(String[] args) {

	TEST_METHOD = 0;
	MAX_SIZE = 100;
	Random rand = new Random(1111L);

        RBTree<Integer> t = new RBTree<Integer>();
        t.print();

	// create an array of the given size
	int[] a = new int[MAX_SIZE];
	  	
	for (int i = 0 ; i < MAX_SIZE; ++i) {
	    a[i] = i;// * 5;
	}

	// randomly shuffle the elements in the array and 
	// insert them in the tree	
	for (int i = MAX_SIZE; i >0; --i) {
	    int n = rand.nextInt(i);
	    int temp = a[n];
	    a[n] = a[i-1];
	    t.insert(temp,null);
	    t.print();
	}

    }

}

