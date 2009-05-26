abstract sig RBTree {
   root: one Node   
}
one sig RB extends RBTree {}

abstract sig Color {}
one sig Red extends Color {}
one sig Black extends Color {}

abstract sig GNode {
   color: one Color,
   parent: lone Node
}
sig Leaf extends GNode {}
sig Node extends GNode  {
   value: one Int,
   disj left, right: one GNode
}

// Binary Tree
fact { no n: Node | n in n.^(left+right) }
fact { all n: GNode | lone p: Node | p.children = n }
fact { all n1: GNode, n2: Node | (n2.left = n1 or n2.right = n1) <=> n1.parent = n2 }
fact { #(Node.value) = #(Node) }
fact { all n: Node, c: n.left.*(left+right) | c in Node => c.value < n.value }
fact { all n: Node, c: n.right.*(left+right) | c in Node => c.value > n.value }
// RB Tree
fact { RBTree.root.color = Black }
fact { Leaf.color = Black }
fact { all n: Node | n.color = Red => n.children.color = Black }
fact { all disj l1, l2: Leaf |
           #({ n: l1.^parent | n.color = Black }) = #({ n: l2.^parent | n.color = Black }) }

fun size [T: RBTree]: one Int { #(T.root.*(left+right).value) }
fun children [N: Node]: set GNode { N.(left+right) }

run { RB.size = 4 } for 4 int, 9 GNode