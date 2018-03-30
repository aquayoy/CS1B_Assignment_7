public class FHsdTree<E> implements Cloneable {
	protected int mSize = 0;
	protected FHsdTreeNode<E> mRoot;
	//
	protected boolean deleted = false; // true if the node has been removed from the tree

	public FHsdTree() {
		clear();
	}

	public void clear() {
		mSize = 0;
		mRoot = null;
	}

	public boolean empty() {
		// return (mSize == 0);
		return size() == 0;
	}

	/*
	 * size() will not use mSize, since that member only reflects physical size.
	 * Instead, size() has to compute the size, manually size() is used to return
	 * the virtual size of the tree (a count of non-deleted nodes) and all
	 * children/sub-trees of deleted nodes are considered deleted, even if their
	 * deleted flag is still set to false)
	 */
	// Initial recursive step for size function
	public int size() {
		return size(mRoot, 0);
	}

	public int size(FHsdTreeNode<E> treeNode) {
		return size(treeNode, 0);
	}

	public int size(FHsdTreeNode<E> treeNode, int level) {
		int virtualSize = 0;

		if (treeNode == null)
			return 0;

		if (treeNode == mRoot && treeNode.deleted)
			return 0;

		if (!treeNode.deleted)
			virtualSize += size(treeNode.firstChild, level + 1);

		if (level > 0)
			virtualSize += size(treeNode.sib, level);

		return virtualSize + 1;
	}

	// non-recursive takes only the data, x
	public FHsdTreeNode<E> find(E x) {
		return find(mRoot, x, 0);
	}

	/*
	 * The recursive method should check the deleted flag of any data match (a
	 * "found" x) and, if true, should return as if the data were not in the tree.
	 * If the deleted flag is false, then return the node as before
	 */
	public FHsdTreeNode<E> find(FHsdTreeNode<E> root, E x, int level) {
		FHsdTreeNode<E> retval;

		if (mSize == 0 || root == null)
			return null;

		if (root.data.equals(x)) {
			if (root.deleted) {
				return null;
			} else
				return root;
		}

		// otherwise, recurse. don't process sibs if this was the original call
		if (level > 0 && (retval = find(root.sib, x, level)) != null)
			return retval;
		return find(root.firstChild, x, ++level);
	}

	public <F extends Traverser<? super E>> void traverse(F func) {
		traverse(func, mRoot, 0);
	}

	public FHsdTreeNode<E> addChild(FHsdTreeNode<E> treeNode, E x) {

		// empty tree? - create a root node if user passes in null
		if (mSize == 0) {
			if (treeNode != null)
				return null; // error something's fishy. treeNode can't right
			mRoot = new FHsdTreeNode<E>(x, null, null, null, null, false);
			mRoot.myRoot = mRoot;
			mSize = 1;
			return mRoot;
		}
		if (treeNode == null)
			return null;// error inserting into non_null tree with a null parent
		// check the first parameter (treeNode/root) to see if it is deleted, and reject
		// the add if it is
		if (treeNode.deleted)
			return null;

		if (treeNode.myRoot != mRoot)
			return null; // silent error, node does not belong to this tree

		// push this node into the head of the sibling list; adjust prev pointers
		FHsdTreeNode<E> newNode = new FHsdTreeNode<E>(x, treeNode.firstChild, null, treeNode, mRoot, false); // data,
																												// sb,child,
																												// prev,
																												// root,
																												// delete

		treeNode.firstChild = newNode;
		if (newNode.sib != null)
			newNode.sib.prev = newNode;
		++mSize;
		return newNode;
	}

	public boolean remove(E x) {
		return remove(mRoot, x);
	}

	public boolean remove(FHsdTreeNode<E> root, E x) {
		FHsdTreeNode<E> tn = null;

		if (mSize == 0 || root == null)
			return false;

		if ((tn = find(root, x, 0)) != null) {
			if (tn.deleted) {
				return false;
			}
			removeNode(tn);
			return true;
		}
		return false;
	}

	private void removeNode(FHsdTreeNode<E> nodeToDelete) {

		if (nodeToDelete == null || mRoot == null) {
			return;
		}
		if (nodeToDelete.myRoot != mRoot)
			return; // silent error, node does not belong to this tree

		/*
		 * the deleted flag of the appropriate node is set to true If a node is marked
		 * as deleted, then the entire child sub-tree is considered gone.
		 */
		if (deleted)
			return;

		else
			nodeToDelete.deleted = true;
		// if (nodeToDelete == mRoot) {
		// mSize = 0;
		// }

		// remove all the children of this node
		while (nodeToDelete.firstChild != null) {
			if (nodeToDelete.firstChild.deleted == true) {
				return;
			}
			removeNode(nodeToDelete.firstChild);
		}

		if (nodeToDelete.prev == null)
			mRoot = null; // last node in tree

		// else if (nodeToDelete.prev.sib == nodeToDelete)
		// if (nodeToDelete.sib)
		// nodeToDelete.prev.sib = nodeToDelete.sib; // adjust left sibling
		// else
		// nodeToDelete.prev.firstChild = nodeToDelete.sib; // adjust parent

		// adjust the successor sib's prev pointer
		if (nodeToDelete.sib != null)
			nodeToDelete.sib.prev = nodeToDelete.prev;

	}

	public Object clone() throws CloneNotSupportedException {
		FHsdTree<E> newObject = (FHsdTree<E>) super.clone();
		newObject.clear(); // can't point to other's data

		newObject.mRoot = cloneSubtree(mRoot);
		newObject.mSize = mSize;
		newObject.setMyRoots(newObject.mRoot);

		return newObject;
	}

	private FHsdTreeNode<E> cloneSubtree(FHsdTreeNode<E> root) {
		FHsdTreeNode<E> newNode;
		if (root == null)
			return null;

		// does not set myRoot which must be done by caller
		newNode = new FHsdTreeNode<E>(root.data, cloneSubtree(root.sib), cloneSubtree(root.firstChild), null, root,
				deleted);

		// the prev pointer is set by parent recursive call ... this is the code:
		if (newNode.sib != null)
			newNode.sib.prev = newNode;
		if (newNode.firstChild != null)
			newNode.firstChild.prev = newNode;
		return newNode;
	}

	// recursively sets all myRoots to mRoot
	private void setMyRoots(FHsdTreeNode<E> treeNode) {
		if (treeNode == null)
			return;

		treeNode.myRoot = mRoot;
		setMyRoots(treeNode.sib);
		setMyRoots(treeNode.firstChild);
	}

	// only report on the virtual tree
	public void display() {
		display(mRoot, 0);
	}

	// define this as a static member so recursive display() does not need a local
	// version
	final static String blankString = "                                    ";

	// let be public so client can call on subtree
	public void display(FHsdTreeNode<E> treeNode, int level) {
		String indent;
if(level >35) {
	System.out.println("");
}
		// stop runaway indentation/recursion
		if (level > (int) blankString.length() - 1) {
			System.out.println(blankString + " ... ");
			return;
		}

		if (treeNode == null)
			return;

		if (treeNode.deleted)
			return;
		else
			indent = blankString.substring(0, level);

		// pre-order processing done here ("visit")
		System.out.println(indent + treeNode.data);

		if (deleted)
			display(treeNode.sib, level);

		// recursive step done here
		display(treeNode.firstChild, level + 1);
		if (level > 0)
			display(treeNode.sib, level);
	}

	// often helper of typical public version, but also callable by on subtree
	public <F extends Traverser<? super E>> void traverse(F func, FHsdTreeNode<E> treeNode, int level) {
		if (treeNode == null)
			return;

		if (!treeNode.deleted)
			func.visit(treeNode.data);

		// recursive step done here
		if (!treeNode.deleted)
			traverse(func, treeNode.firstChild, level + 1);
		if (level > 0)
			traverse(func, treeNode.sib, level);
	}

	// New Methods:
	// sizePhysical() - returns the actual, physical size.
	public int sizePhysical() {
		return sizePhysical(mRoot, 0);
	}

	/* computes number of nodes in tree */
	int sizePhysical(FHsdTreeNode<E> treeNode, int level) {
		int physicalSize = 0;
		if (treeNode == null)
			return 0;

		physicalSize += size(treeNode.firstChild, level + 1);

		if (level > 0)
			physicalSize += size(treeNode.sib, level);

		return physicalSize + 1;
	}

	// Initial step for recursive function displayPhysical
	void displayPhysical() {
		displayPhysical((FHsdTreeNode<E>) (mRoot), 0);
	}

	public void displayPhysical(FHsdTreeNode<E> treeNode, int level) {

		String indent;

		// stop runaway indentation/recursion
		if (level > (int) blankString.length() - 1) {
			System.out.println(blankString + " ... ");
			return;
		}

		if (treeNode == null)
			return;

		else
			indent = blankString.substring(0, level);

		// pre-order processing done here ("visit")
		System.out.println(indent + treeNode.data);

		// recursive step done here
		displayPhysical(treeNode.firstChild, level + 1);
		if (level > 0)
			displayPhysical(treeNode.sib, level);
	}

	/*
	 * collectGarbage() - physically removes all nodes that are marked as deleted.
	 * After this method is called, the physical and virtual size of the tree would
	 * be the same.
	 */
	public boolean collectGarbage() {

		int virtualSize = size();
		int physicalSize = sizePhysical();

		if (virtualSize == 0)
			return false;

		if (physicalSize > virtualSize)
			collectGarbage((FHsdTreeNode<E>) (mRoot), 0); // cleans tree

		return physicalSize == virtualSize;
	}

	public boolean collectGarbage(FHsdTreeNode<E> treeNode) {
		return collectGarbage(treeNode, 0);
	}

	public boolean collectGarbage(FHsdTreeNode<E> treeNode, int level) {

		if (treeNode == null)
			return false;
		if (treeNode.deleted) {
			treeNode = null;
			mSize = mSize - 1;
			return true;
		}

		// if (treeNode.deleted) {
		// FHsdTreeNode<E> previous = treeNode.prev;
		// removeNode(treeNode);
		// collectGarbage(previous.firstChild, level + 1);
		// if (level > 0)
		// collectGarbage(previous.sib, level);
		//
		// return;
		// }
		return collectGarbage(treeNode.firstChild, level + 1) || collectGarbage(treeNode.sib, level);

	}
}
