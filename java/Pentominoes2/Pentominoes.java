
import static kodkod.ast.Expression.UNIV;

import kodkod.ast.*;
import kodkod.ast.operator.*;
import kodkod.instance.*;
import kodkod.engine.*;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.config.Options;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Pentominoes{
    static int MAX_INT = 499;
    public int PIECES = 12;
    public int WIDTH = 12, HEIGHT = 5;
    public int BOARD_SIZE = WIDTH*HEIGHT;

    //Constructor
    public Pentominoes() {

    }

    void solve() {

	Relation Pieces = Relation.unary("Pieces");
	Relation AllOrients = Relation.unary("AllOrients");
	Relation Piece1 = Relation.unary("Piece1");
	Relation OrientIdxs = Relation.unary("OrientIdxs");
	Relation BoardIdxs = Relation.unary("BoardIdxs");
	Relation Idxs2To5 = Relation.unary("Idxs2To5");
	Relation BoardNums = Relation.unary("BoardNums");
	Relation Board = Relation.nary("Board", 2);
	Relation PieceChoices = Relation.nary("PieceChoices", 2);
	Relation PieceOrients = Relation.nary("PieceOrients", 2);
	Relation PieceBoardIdxs = Relation.nary("PieceBoardIdxs", 3);
	Relation Orients = Relation.nary("Orients", 3);

	String[] atoms = new String[MAX_INT+1+63];
	int orientIdx = MAX_INT+1;
	
	for(int i=0;i<=MAX_INT;i++)
	    atoms[i] = Integer.toString(i);
	for(int i=1;i<=63;i++)
	    atoms[i+MAX_INT] = "R"+i;

	List<String> atomlist = Arrays.asList(atoms);
	Universe universe = new Universe(atomlist);
	TupleFactory factory = universe.factory();
	Bounds bounds = new Bounds(universe);

	//TupleSet Idxs_upper = factory.noneOf(1);

	for(int i=0;i<=MAX_INT;i++) {
	    Object ti = Integer.toString(i); 
	    bounds.boundExactly(i,factory.range(factory.tuple(ti),factory.tuple(ti)));
	}

	TupleSet Board_upper = factory.noneOf(2);
	TupleSet Pieces_upper = factory.noneOf(1);
	TupleSet AllOrients_upper = factory.noneOf(1);
	TupleSet Piece1_upper = factory.noneOf(1);
	TupleSet BoardIdxs_upper = factory.noneOf(1);
	TupleSet Idxs2To5_upper = factory.noneOf(1);
	TupleSet OrientIdxs_upper = factory.noneOf(1);
	TupleSet BoardNums_upper = factory.noneOf(1);
	TupleSet Orients_upper = factory.noneOf(3);
	TupleSet PieceBoardIdxs_upper = factory.noneOf(3);
	TupleSet PieceOrients_upper = factory.noneOf(2);

	for(int i=2;i<=5;i++) {
	    Object ti = universe.atom(i);
	    Idxs2To5_upper.add(factory.tuple(ti));
	}

	for(int i=1;i<=PIECES;i++) {
	    Object ti = universe.atom(i);
	    Pieces_upper.add(factory.tuple(ti));
	    for(int j=1;j<=5;j++) {
		Object ti2 = universe.atom((i-1)*5+j);
		PieceBoardIdxs_upper.add(factory.tuple(ti).product(factory.tuple(universe.atom(j))).product(factory.tuple(ti2)));
	    }
	}

	Piece1_upper.add(factory.tuple(universe.atom(1)));

	for (int i=1;i<=8;i++) {
	    Object ti = universe.atom(i);
	    OrientIdxs_upper.add(factory.tuple(ti));
	}

	for (int i=1;i<=BOARD_SIZE;i++) {
	    Object ti = universe.atom(i);
	    BoardIdxs_upper.add(factory.tuple(ti));
	    for (int a=0;a<=400;a+=100) {
		for (int j=1;j<=12;j++) {
		    Object ti2 = universe.atom(a+j);
		    BoardNums_upper.add(factory.tuple(ti2));
		    Board_upper.add(factory.tuple(ti).product(factory.tuple(ti2)));
		}
	    }
	}
	bounds.boundExactly(Pieces, Pieces_upper);
	bounds.boundExactly(Piece1, Piece1_upper);
	bounds.boundExactly(OrientIdxs, OrientIdxs_upper);
	bounds.boundExactly(Idxs2To5, Idxs2To5_upper);
	bounds.boundExactly(BoardIdxs, BoardIdxs_upper);
	bounds.boundExactly(BoardNums, BoardNums_upper);
	bounds.bound(Board, Board_upper);
	bounds.bound(PieceOrients, PieceOrients_upper);
	bounds.boundExactly(PieceBoardIdxs, PieceBoardIdxs_upper);

	int[][] orientsT = {{0, 100, 101, 102, 200}, {0, 98, 99, 100, 200}, {0, 1, 2, 101, 201}, {0, 100, 199, 200, 201}};
	int[][] orientsL = {{0, 100, 101, 102, 103}, {0, 100, 200, 300, 301}, {0, 1, 2, 3, 100}, {0, 100, 200, 299, 300}, {0, 97, 98, 99, 100}, {0, 1, 100, 200, 300}, {0, 1, 2, 3, 103}, {0, 1, 101, 201, 301}};
	int[][] orientsR = {{0, 99, 100, 101, 201}, {0, 99, 100, 200, 201}, {0, 100, 101, 199, 200}, {0, 99, 100, 101, 199}, {0, 98, 99, 100, 199}, {0, 100, 101, 102, 201}, {0, 1, 99, 100, 200}, {0, 1, 101, 102, 201}};
	int[][] orientsP = {{0, 1, 100, 101, 201}, {0, 1, 2, 100, 101}, {0, 99, 100, 199, 200}, {0, 1, 2, 101, 102}, {0, 1, 99, 100, 101}, {0, 1, 100, 101, 200}, {0, 1, 100, 101, 102}, {0, 100, 101, 200, 201}};
	int[][] orientsW = {{0, 1, 99, 100, 199}, {0, 1, 101, 102, 202}, {0, 99, 100, 198, 199}, {0, 100, 101, 201, 202}};
	int[][] orientsZ = {{0, 1, 101, 201, 202}, {0, 100, 101, 102, 202}, {0, 1, 100, 199, 200}, {0, 98, 99, 100, 198}};
	int[][] orientsU = {{0, 1, 100, 200, 201}, {0, 2, 100, 101, 102}, {0, 1, 2, 100, 102}, {0, 1, 101, 200, 201}};
	int[][] orientsI = {{0, 100, 200, 300, 400}, {0, 1, 2, 3, 4}};
	int[][] orientsX = {{0, 99, 100, 101, 200}};
	int[][] orientsV = {{0, 1, 2, 100, 200}, {0, 100, 200, 201, 202}, {0, 1, 2, 102, 202}, {0, 100, 198, 199, 200}};
	int[][] orientsY = {{0, 1, 2, 3, 102}, {0, 100, 200, 201, 300}, {0, 99, 100, 101, 102}, {0, 100, 199, 200, 300}, {0, 98, 99, 100, 101}, {0, 100, 101, 200, 300}, {0, 99, 100, 200, 300}, {0, 1, 2, 3, 101}};
	int[][] orientsN = {{0, 1, 98, 99, 100}, {0, 100, 101, 201, 301}, {0, 99, 100, 199, 299}, {0, 100, 199, 200, 299}, {0, 1, 2, 102, 103}, {0, 1, 2, 99, 100}, {0, 1, 101, 102, 103}, {0, 100, 200, 201, 301}};


	// pieceChoices:  4 2 7 2 1 3 2 2 1 4 8 8
	int[] pc = {4, 2, 7, 2, 1, 3, 2, 2, 1, 4, 8, 8};
	int pieceCtr = 0;
	boolean fixed = true; //false;
	for(int i=0;i<orientsT.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsT[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsL.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsL[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsR.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsR[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsP.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsP[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsW.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsW[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}

	for(int i=0;i<orientsZ.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsZ[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsU.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsU[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsI.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsI[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsX.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsX[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsV.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsV[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsY.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsY[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	for(int i=0;i<orientsN.length;i++) {
	    if (fixed && pc[pieceCtr] != i+1) continue;
	    Object ti = universe.atom(i+1);
	    Object ti2 = universe.atom(orientIdx++);
	    int[] a = orientsN[i];
	    for(int j=0;j<5;j++)
		Orients_upper.add(factory.tuple(ti2).product(factory.tuple(universe.atom(j+1)).product(factory.tuple(universe.atom(a[j])))));
	    PieceOrients_upper.add(factory.tuple(universe.atom(++pieceCtr)).product(factory.tuple(ti2)));	    
	    AllOrients_upper.add(factory.tuple(ti2));
	}
	bounds.boundExactly(Orients, Orients_upper);
	bounds.boundExactly(PieceOrients, PieceOrients_upper);
	bounds.boundExactly(AllOrients, AllOrients_upper);
	bounds.bound(PieceChoices, PieceOrients_upper);	
	
	IntExpression x1 = IntConstant.constant(1);

	// board permutation of (1 to:12), (101 to: 112), (201 to: 212), (301 to: 312), (401 to: 412).
	Formula x100 = BoardIdxs.join(Board).eq(BoardNums);

	// one valid pick of orient for each piece
	Variable aPiece = Variable.unary("aPiece");
	Decls x201 = aPiece.oneOf(Pieces);
	Expression x202 = aPiece.join(PieceChoices);
	Formula x203 = x202.one();
	Formula x200 = x203.forAll(x201);

	/* all p : piece | all i in 2 to: 5 |  
	   (board at: (p boardIndices at: i) = 
	            ( (p orients at: p orientIdx) squares at: i) + 
                        board at: p boardIndices first). */	
	Variable anIdx2To5 = Variable.unary("anIdx2To5");
	Decls x301 = anIdx2To5.oneOf(Idxs2To5);
	Expression x302 = aPiece.join(PieceBoardIdxs);
	Expression x303 = anIdx2To5.join(x302).join(Board);
	Expression x304 = x1.toExpression().join(x302).join(Board);
	Expression x305 = anIdx2To5.join(aPiece.join(PieceChoices).join(Orients));
	Formula x309 = x303.sum().eq(x304.sum().plus(x305.sum()));
	Formula x300 = x309.forAll(x301).forAll(x201);

	Formula formula=Formula.compose(FormulaOperator.AND, Board.function(BoardIdxs,BoardNums), PieceChoices.function(Pieces,AllOrients), /*x100,*/ x200, x300);



	Solver solver = new Solver();
	solver.options().setSolver(SATFactory.MiniSat);
	solver.options().setBitwidth(((int) Math.ceil(Math.log(MAX_INT) / Math.log(2))) + 2);
	solver.options().setFlatten(false);
	solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
	solver.options().setSymmetryBreaking(20);
	solver.options().setSkolemDepth(0);
	System.out.println("Solving...");
	System.out.flush();
	Solution sol = solver.solve(formula,bounds);
	System.out.println(sol.toString());
	
	if (sol.instance()==null) {
	    System.out.println(sol);
	} else {
	    System.out.println(sol.stats());

	    final Iterator<Tuple> iter = sol.instance().tuples(Board).iterator();	    
	}


    }

    
    public static void main(String args[]){


	// create an array of the given size

        Pentominoes l = new Pentominoes();
	l.solve();
    }
}

