public class FHsdTreeNode<E>
{
   // use protected access so the tree, in the same package, 
   // or derived classes can access members 
   protected FHsdTreeNode<E> firstChild, sib, prev;
   protected E data;
   protected FHsdTreeNode<E> myRoot;  // needed to test for certain error
   protected boolean deleted;
   
   //All parameter-taking constructors should be modified to take a new parameter for the deleted member
   public FHsdTreeNode( E d, FHsdTreeNode<E> sb, FHsdTreeNode<E> chld, FHsdTreeNode<E> prv, boolean deleted )
   {
      firstChild = chld; 
      sib = sb;
      prev = prv;
      data = d;
      myRoot = null;
      deleted = false;
   }
   
   //the default constructor should make sure the value for the deleted member is initialized to false
   public FHsdTreeNode()
   {
      this(null, null, null, null, false);
      
   }
   
   public E getData() { return data; }

   // for use only by FHtree (default access)
   protected FHsdTreeNode( E d, FHsdTreeNode<E> sb, FHsdTreeNode<E> chld, 
      FHsdTreeNode<E> prv, FHsdTreeNode<E> root, boolean deleted )
   {
      this(d, sb, chld, prv, deleted);
      myRoot = root;
   }
}
