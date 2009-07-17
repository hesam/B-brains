abstract sig RBTree {
   root: one Node,
}
one sig RB extends RBTree {}

abstract sig Color {}
one sig Red extends Color {}
one sig Black extends Color {}

abstract sig Node {
   color: one Color,
   value: one Int,
   parent, left, right: lone Node
}

// Binary Tree 
fact { no RBTree.root.parent }
fact { no n: Node | n in n.^(left+right) }
fact { all n: Node | lone p: Node | p.(left+right) = n }
fact { all n1, n2: Node | n1 in n2.(left+right) <=> n1.parent = n2 }
fact { #(Node.value) = #(Node) }
 
fact { all n: Node, c: n.left.*(left+right) | c.value < n.value }
fact { all n: Node, c: n.right.*(left+right) | c.value > n.value }
// RB Tree 
fact { RBTree.root.color = Black }
fact { all n: Node | n.color = Red => 
        ((some n.left => n.left.color = Black) && (some n.right => n.right.color = Black)) }
fact { all disj l1, l2: Node | (l1.hasLeaf && l2.hasLeaf) =>
           #({ n: l1.*parent | n.color = Black }) = #({ n: l2.*parent | n.color = Black }) }

pred hasLeaf [n: Node] { no n.left || no n.right }

run { {0} in RB.root.*(left+right).value and {1} in RB.root.*(left+right).value and {2} in RB.root.*(left+right).value and {3} in RB.root.*(left+right).value and {4} in RB.root.*(left+right).value and {5} in RB.root.*(left+right).value and {6} in RB.root.*(left+right).value and {7} in RB.root.*(left+right).value } for 4 int, exactly 8 Node
