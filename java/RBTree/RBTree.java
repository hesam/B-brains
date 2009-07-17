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
    
    List<Integer> nodes;

    final CyclicBarrier barrier;    

    // solver related
    private boolean waitingForSolver;
    private static int solRootNodeIdx;
    private static int [] solNodeKeys;
    private static Color [] solNodeColors;
    private static int [] solNodeLefts;
    private static int [] solNodeRights;

    // Constructor
    public RBTree() {
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

    public void insert(Integer key, V value) {
        Node<V> insertedNode = new Node<V>(key, value, Color.RED, null, null);
	nodes.add(key);
	try {
	    if (key == 13) { int i = 1/0; /* FIXME - THROW EXCEPTION HERE */ }
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
	    insertCase1(insertedNode);
	    verifyProperties();
	} catch (Throwable rte) {
	    callSolver(rte);
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

    
    public void callSolver(Throwable rte) {

	System.out.println("!! " + rte + "... start solver fallback!");

	waitingForSolver = true;
	int numNodes = nodes.size();
	int[] keys = new int[numNodes];
	int maxVal = 0;
	for (int i = 0; i < numNodes; i++) {
	    int val = nodes.get(i).intValue();
	    keys[i] = val;
	    if (val > maxVal) {
		maxVal = val;
	    }
	}
	int maxInt = Math.max(maxVal,19);
	solNodeKeys = new int[numNodes];
	solNodeColors = new Color[numNodes];
	solNodeLefts = new int[numNodes];
	solNodeRights = new int[numNodes];

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

	    Relation RB = Relation.unary("RB");
	    Relation Red = Relation.unary("Red");
	    Relation Black = Relation.unary("Black");
	    Relation Leaf = Relation.unary("Leaf");
	    Relation Node = Relation.unary("Node");
	    Relation Root = Relation.nary("RBTree.root", 2);
	    Relation RelColor = Relation.nary("GNode.color", 2);
	    Relation Parent = Relation.nary("GNode.parent", 2);
	    Relation Value = Relation.nary("Node.value", 2);
	    Relation Left = Relation.nary("Node.left", 2);
	    Relation Right = Relation.nary("Node.right", 2);

	    int numNodes = keys.length;
	    int intBitWidth = 1+(int)Math.ceil((double)Math.log(maxInt+1)/(double)Math.log(2));
	    String[] atoms = new String[maxInt+4];
	    String[] nodes = new String[numNodes];
	    String[] leaves = new String[numNodes+1];

	    //            idxs = {  0,  1,  2,  3,  4,  5,  6,  7  }
	    //            keys = {  0,  8,  2,  6,  4,  9, 12, 13  }
	    int [] parentFixes = {  2,  5,  3, -1,  2,  3,  5,  6 };
	    int [] leftFixes   = { -1, -1,  0,  2, -1,  1, -1, -1 };
	    int [] rightFixes  = { -1, -1,  4,  5, -1,  6,  7, -1 };

	    Object ti;
	    int i, j=0, s=maxInt+numNodes+1;
	    for(i=0;i<=maxInt;i++) {
		atoms[i] = Integer.toString(i); 
	    }
	    for(i=0;i<numNodes;i++) {
		nodes[i] = atoms[i];
	    }
	    for(i=numNodes;i<=numNodes*2;i++) {
		leaves[i-numNodes] = atoms[i];
	    }
	    atoms[maxInt+1] = "RB"; 
	    atoms[maxInt+2] = "Red"; 
	    atoms[maxInt+3] = "Black"; 
	    println(atoms);
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
	    bounds.boundExactly(RB, RB_upper);
	    
	    TupleSet Red_upper = factory.noneOf(1);
	    Red_upper.add(factory.tuple("Red"));
	    bounds.boundExactly(Red, Red_upper);
	    
	    TupleSet Black_upper = factory.noneOf(1);
	    Black_upper.add(factory.tuple("Black"));
	    bounds.boundExactly(Black, Black_upper);
	    
	    TupleSet Leaf_upper = factory.noneOf(1);
	    TupleSet Node_upper = factory.noneOf(1);
	    TupleSet Root_upper = factory.noneOf(2);
	    TupleSet Color_upper = factory.noneOf(2);
	    TupleSet Parent_upper = factory.noneOf(2);
	    TupleSet Value_upper = factory.noneOf(2);
	    TupleSet Left_upper = factory.noneOf(2);
	    TupleSet Right_upper = factory.noneOf(2);
	    for(i=0;i<numNodes;i++) {
		Object l = leaves[i], n = nodes[i];
		Leaf_upper.add(factory.tuple(l));
		Node_upper.add(factory.tuple(n));
		Root_upper.add(factory.tuple("RB").product(factory.tuple(n)));
		Color_upper.add(factory.tuple(l).product(factory.tuple("Red")));
		Color_upper.add(factory.tuple(l).product(factory.tuple("Black")));
		Color_upper.add(factory.tuple(n).product(factory.tuple("Red")));
		Color_upper.add(factory.tuple(n).product(factory.tuple("Black")));
		if (parentFixes[i] != -1) {
		    Parent_upper.add(factory.tuple(n).product(factory.tuple(nodes[parentFixes[i]])));
		}
		if (leftFixes[i] != -1) {
		    Left_upper.add(factory.tuple(n).product(factory.tuple(nodes[leftFixes[i]])));
		}
		if (rightFixes[i] != -1) {
		    Right_upper.add(factory.tuple(n).product(factory.tuple(nodes[rightFixes[i]])));
		}
		for(j=0;j<numNodes;j++) {
		    Object l2 = leaves[j], n2 = nodes[j];		    
		    Parent_upper.add(factory.tuple(l).product(factory.tuple(n2)));
		    if (parentFixes[i] == -1) {
			Parent_upper.add(factory.tuple(n).product(factory.tuple(n2)));
		    }
		    if (leftFixes[i] == -1) {
			Left_upper.add(factory.tuple(n).product(factory.tuple(l2)));
			Left_upper.add(factory.tuple(n).product(factory.tuple(n2)));
		    }
		    if (rightFixes[i] == -1) {
			Right_upper.add(factory.tuple(n).product(factory.tuple(l2)));
			Right_upper.add(factory.tuple(n).product(factory.tuple(n2)));
		    }

		}
		Object l2 = leaves[j];
		if (leftFixes[i] == -1) {
		    Left_upper.add(factory.tuple(n).product(factory.tuple(l2)));
		}
		if (rightFixes[i] == -1) {
		    Right_upper.add(factory.tuple(n).product(factory.tuple(l2)));
		}
		/*
		for(j=0;j<numNodes;j++) {
		    Value_upper.add(factory.tuple(n).product(factory.tuple(atoms[keys[j]])));
		}
		*/
		Value_upper.add(factory.tuple(n).product(factory.tuple(atoms[keys[i]])));
	    }
	    String l = leaves[i];
	    Leaf_upper.add(factory.tuple(l));
	    Color_upper.add(factory.tuple(l).product(factory.tuple("Red")));
	    Color_upper.add(factory.tuple(l).product(factory.tuple("Black")));
	    for(j=0;j<numNodes;j++) {
		Parent_upper.add(factory.tuple(l).product(factory.tuple(nodes[j])));
	    }
	    bounds.boundExactly(Leaf, Leaf_upper);
	    bounds.boundExactly(Node, Node_upper);
	    bounds.bound(Root, Root_upper);
	    bounds.bound(RelColor, Color_upper);
	    bounds.bound(Parent, Parent_upper);
	    //bounds.bound(Value, Value_upper);
	    bounds.boundExactly(Value, Value_upper);
	    bounds.bound(Left, Left_upper);
	    bounds.bound(Right, Right_upper);

	    Expression x19=Red.intersection(Black);
	    Formula x18=x19.no();
	    Expression x21=Leaf.intersection(Node);
	    Formula x20=x21.no();
	    Variable x24=Variable.unary("this");
	    Decls x23=x24.oneOf(RB);
	    Expression x27=x24.join(Root);
	    Formula x26=x27.one();
	    Formula x28=x27.in(Node);
	    Formula x25=x26.and(x28);
	    Formula x22=x25.forAll(x23);
	    Expression x30=Root.join(Expression.UNIV);
	    Formula x29=x30.in(RB);
	    Variable x34=Variable.unary("this");
	    Decls x33=x34.oneOf(Node);
	    Expression x37=x34.join(Value);
	    Formula x36=x37.one();
	    Formula x38=x37.in(Expression.INTS);
	    Formula x35=x36.and(x38);
	    Formula x32=x35.forAll(x33);
	    Expression x41=Value.join(Expression.UNIV);
	    Formula x40=x41.in(Node);
	    Variable x44=Variable.unary("this");
	    Decls x43=x44.oneOf(Node);
	    Expression x47=x44.join(Left);
	    Formula x46=x47.one();
	    Expression xGNode=Leaf.union(Node);
	    Formula x48=x47.in(xGNode);
	    Formula x45=x46.and(x48);
	    Formula x42=x45.forAll(x43);
	    Expression x51=Left.join(Expression.UNIV);
	    Formula x50=x51.in(Node);
	    Variable x54=Variable.unary("this");
	    Decls x53=x54.oneOf(Node);
	    Expression x57=x54.join(Right);
	    Formula x56=x57.one();
	    Formula x58=x57.in(xGNode);
	    Formula x55=x56.and(x58);
	    Formula x52=x55.forAll(x53);
	    Expression x60=Right.join(Expression.UNIV);
	    Formula x59=x60.in(Node);
	    Expression x62=Left.intersection(Right);
	    Formula x61=x62.no();
	    Variable x65=Variable.unary("this");
	    Decls x64=x65.oneOf(xGNode);
	    Expression x68=x65.join(RelColor);
	    Formula x67=x68.one();
	    Expression xColor=Red.union(Black);
	    Formula x69=x68.in(xColor);
	    Formula x66=x67.and(x69);
	    Formula x63=x66.forAll(x64);
	    Expression x72=RelColor.join(Expression.UNIV);
	    Formula x71=x72.in(xGNode);
	    Variable x75=Variable.unary("this");
	    Decls x74=x75.oneOf(xGNode);
	    Expression x78=x75.join(Parent);
	    Formula x77=x78.lone();
	    Formula x79=x78.in(Node);
	    Formula x76=x77.and(x79);
	    Formula x73=x76.forAll(x74);
	    Expression x81=Parent.join(Expression.UNIV);
	    Formula x80=x81.in(xGNode);
	    Expression x84=RB.join(Root);
	    Expression x83=x84.join(Parent);
	    Formula x82=x83.no();
	    Variable x87=Variable.unary("n");
	    Decls x86=x87.oneOf(Node);
	    Expression x92=Left.union(Right);
	    Expression x91=x92.closure();
	    Expression x90=x87.join(x91);
	    Formula x89=x87.in(x90);
	    Formula x88=x89.not();
	    Formula x85=x88.forAll(x86);
	    Variable x95=Variable.unary("n");
	    Decls x94=x95.oneOf(Node);
	    Variable x99=Variable.unary("p");
	    Decls x98=x99.oneOf(Node);
	    Expression x102=Left.union(Right);
	    Expression x101=x99.join(x102);
	    Formula x100=x101.eq(x95);
	    Expression x97=x100.comprehension(x98);
	    Formula x96=x97.lone();
	    Formula x93=x96.forAll(x94);
	    Variable x106=Variable.unary("n1");
	    Decls x105=x106.oneOf(xGNode);
	    Variable x108=Variable.unary("n2");
	    Decls x107=x108.oneOf(Node);
	    Decls x104=x105.and(x107);
	    Expression x112=x108.join(Left);
	    Formula x111=x112.eq(x106);
	    Expression x114=x108.join(Right);
	    Formula x113=x114.eq(x106);
	    Formula x110=x111.or(x113);
	    Expression x116=x106.join(Parent);
	    Formula x115=x116.eq(x108);
	    Formula x109=x110.iff(x115);
	    Formula x103=x109.forAll(x104);
	    Expression x119=Node.join(Value);
	    IntExpression x118=x119.count();
	    IntExpression x120=Node.count();
	    Formula x117=x118.eq(x120);
	    Variable x124=Variable.unary("n");
	    Decls x123=x124.oneOf(Node);
	    Variable x126=Variable.unary("c");
	    Expression x128=x124.join(Left);
	    Expression x131=Left.union(Right);
	    Expression x130=x131.closure();
	    Expression x138=Expression.INTS;
	    Expression x137=x138.union(RB);
	    Expression x136=x137.union(xColor);
	    Expression x135=x136.union(xGNode);
	    Expression x134=x135.product(Expression.UNIV);
	    Expression x132=Expression.IDEN.intersection(x134);
	    Expression x129=x130.union(x132);
	    Expression x127=x128.join(x129);
	    Decls x125=x126.oneOf(x127);
	    Decls x122=x123.and(x125);
	    Formula x141=x126.in(Node);
	    Formula x140=x141.not();
	    Expression x144=x126.join(Value);
	    IntExpression x143=x144.sum();
	    Expression x146=x124.join(Value);
	    IntExpression x145=x146.sum();
	    Formula x142=x143.lt(x145);
	    Formula x139=x140.or(x142);
	    Formula x121=x139.forAll(x122);
	    Variable x150=Variable.unary("n");
	    Decls x149=x150.oneOf(Node);
	    Variable x152=Variable.unary("c");
	    Expression x154=x150.join(Right);
	    Expression x157=Left.union(Right);
	    Expression x156=x157.closure();
	    Expression x159=x135.product(Expression.UNIV);
	    Expression x158=Expression.IDEN.intersection(x159);
	    Expression x155=x156.union(x158);
	    Expression x153=x154.join(x155);
	    Decls x151=x152.oneOf(x153);
	    Decls x148=x149.and(x151);
	    Formula x162=x152.in(Node);
	    Formula x161=x162.not();
	    Expression x165=x152.join(Value);
	    IntExpression x164=x165.sum();
	    Expression x167=x150.join(Value);
	    IntExpression x166=x167.sum();
	    Formula x163=x164.gt(x166);
	    Formula x160=x161.or(x163);
	    Formula x147=x160.forAll(x148);
	    Expression x170=RB.join(Root);
	    Expression x169=x170.join(RelColor);
	    Formula x168=x169.eq(Black);
	    Expression x172=Leaf.join(RelColor);
	    Formula x171=x172.eq(Black);
	    Variable x175=Variable.unary("n");
	    Decls x174=x175.oneOf(Node);
	    Expression x179=x175.join(RelColor);
	    Formula x178=x179.eq(Red);
	    Formula x177=x178.not();
	    Expression x183=Left.union(Right);
	    Expression x182=x175.join(x183);
	    Expression x181=x182.join(RelColor);
	    Formula x180=x181.eq(Black);
	    Formula x176=x177.or(x180);
	    Formula x173=x176.forAll(x174);
	    Variable x187=Variable.unary("l1");
	    Decls x186=x187.oneOf(Leaf);
	    Variable x189=Variable.unary("l2");
	    Decls x188=x189.oneOf(Leaf);
	    Decls x185=x186.and(x188);
	    Expression x193=x187.intersection(x189);
	    Formula x192=x193.no();
	    Formula x191=x192.not();
	    Variable x198=Variable.unary("n");
	    Expression x200=Parent.closure();
	    Expression x199=x187.join(x200);
	    Decls x197=x198.oneOf(x199);
	    Expression x202=x198.join(RelColor);
	    Formula x201=x202.eq(Black);
	    Expression x196=x201.comprehension(x197);
	    IntExpression x195=x196.count();
	    Variable x206=Variable.unary("n");
	    Expression x208=Parent.closure();
	    Expression x207=x189.join(x208);
	    Decls x205=x206.oneOf(x207);
	    Expression x210=x206.join(RelColor);
	    Formula x209=x210.eq(Black);
	    Expression x204=x209.comprehension(x205);
	    IntExpression x203=x204.count();
	    Formula x194=x195.eq(x203);
	    Formula x190=x191.or(x194);
	    Formula x184=x190.forAll(x185);
	    Formula x17=Formula.compose(FormulaOperator.AND, x18, x20, x22, x29, x32, x40, x42, x50, x52, x59, x61, x63, x71, x73, x80, x82, x85, x93, x103, x117, x121, x147, x168, x171, x173, x184);
	    
	    Solver solver = new Solver();
	    solver.options().setSolver(SATFactory.MiniSat);
	    solver.options().setBitwidth(intBitWidth);
	    solver.options().setFlatten(false);
	    solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
	    solver.options().setSymmetryBreaking(20);
	    solver.options().setSkolemDepth(0);
	    System.out.println("Solving...");
	    System.out.flush();
	    Solution sol = solver.solve(x17,bounds);
	    System.out.println(sol.toString());
	    
	    // extract solution
	    Iterator<Tuple> rootRel = sol.instance().tuples(Root).iterator();
	    Iterator<Tuple> colorRel = sol.instance().tuples(RelColor).iterator();
	    Iterator<Tuple> valueRel = sol.instance().tuples(Value).iterator();
	    Iterator<Tuple> leftRel = sol.instance().tuples(Left).iterator();
	    Iterator<Tuple> rightRel = sol.instance().tuples(Right).iterator();
	    solRootNodeIdx = Integer.parseInt((String)rootRel.next().atom(1));
	    for (i=0;i<numNodes;i++) {
		int tmp;
		solNodeColors[i] = ((String)colorRel.next().atom(1)) == "Black" ? Color.BLACK : Color.RED;
		solNodeKeys[i] = Integer.parseInt((String)valueRel.next().atom(1));
		tmp = Integer.parseInt((String)leftRel.next().atom(1));
		solNodeLefts[i] = tmp < numNodes ? tmp : -1;
		tmp = Integer.parseInt((String)rightRel.next().atom(1));
		solNodeRights[i] = tmp < numNodes ? tmp : -1;
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
        RBTree<Integer> t = new RBTree<Integer>();
        t.print();

	int [] a = {0,8,2,6,4,9,12,13}; //,1,10,14,7};
	for (int i=0; i < a.length; i++) {
	    t.insert(a[i],null);
	    t.print();
	}
    }

}


/*

}}


 */