/*====================================================================*\

TreeUtils.java

Class: tree-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.tree;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import java.util.stream.Collectors;

//----------------------------------------------------------------------


// CLASS: TREE-RELATED UTILITY METHODS.


/**
 * This class contains utility methods that relate to trees whose nodes implement the {@link ITreeNode} interface.
 */

public class TreeUtils
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private TreeUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the root node of the tree to which the specified {@linkplain ITreeNode node} belongs.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  node
	 *           the node whose root ancestor is required.
	 * @return the root node of the tree to which <i>node</i> belongs.
	 */

	public static <T extends ITreeNode<T>> T getRoot(T node)
	{
		while (true)
		{
			T parent = node.getParent();
			if (parent == null)
				return node;
			node = parent;
		}
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the siblings of the specified {@linkplain ITreeNode node}.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  node
	 *           the node whose siblings are required.
	 * @return a list of the siblings of <i>node</i>.
	 */

	public static <T extends ITreeNode<T>> List<T> getSiblings(T node)
	{
		T parent = node.getParent();
		return (parent == null) ? Collections.emptyList()
								: parent.getChildren().stream()
														.filter(child -> (child != node))
														.map(child -> child)
														.collect(Collectors.toList());
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified node is an ancestor of the specified target node.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  node
	 *           the node that is a potential ancestor of <i>target</i>.
	 * @param  target
	 *           the node that is a potential descendant of <i>node</i>.
	 * @return {@code true} if <i>node</i> is an ancestor of <i>target</i>.
	 */

	public static <T extends ITreeNode<T>> boolean isAncestor(T node,
															  T target)
	{
		return isAncestor(node, target, false);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified node is an ancestor of the specified target node or, optionally, if the
	 * node is identical to the target.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  node
	 *           the node that is a potential ancestor of <i>target</i> or (if <i>testForIdentity</i> is {@code true})
	 *           is potentially identical to <i>target</i>.
	 * @param  target
	 *           the node that is a potential descendant of <i>node</i> or (if <i>testForIdentity</i> is {@code true})
	 *           is potentially identical to <i>node</i>.
	 * @param  testForIdentity
	 *           if {@code true}, <i>node</i> and <i>target</i> will be tested for identity as well as for an
	 *           ancestor&ndash;descendant relationship.
	 * @return {@code true} if
	 * 		   <ul>
	 *           <li><i>node</i> is the ancestor of <i>target</i>, or</li>
	 *           <li><i>testForIdentity</i> is {@code true} and <i>node</i> is identical to <i>target</i>.</li>
	 * 		   </ul>
	 */

	public static <T extends ITreeNode<T>> boolean isAncestor(T       node,
															  T       target,
															  boolean testForIdentity)
	{
		T target0 = testForIdentity ? target : target.getParent();
		while (target0 != null)
		{
			if (node == target0)
				return true;
			target0 = target0.getParent();
		}
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the depth of the specified {@linkplain ITreeNode node} (ie, the number of levels below the root node of
	 * the tree to which the specified node belongs).
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  node
	 *           the node whose depth is required.
	 * @return the depth of <i>node</i>.
	 * @see    #getDepth(ITreeNode, ITreeNode)
	 */

	public static <T extends ITreeNode<T>> int getDepth(T node)
	{
		int depth = 0;
		while (true)
		{
			T parent = node.getParent();
			if (parent == null)
				break;
			node = parent;
			++depth;
		}
		return depth;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the number of levels of the specified {@linkplain ITreeNode node} below the specified root node.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the root of the tree node whose depth is required.
	 * @param  node
	 *           the node whose depth below <i>root</i> is required.
	 * @return the depth of <i>node</i> below <i>root</i>, or -1 if <i>root</i> is not an ancestor of <i>node</i>.
	 * @see    #getDepth(ITreeNode)
	 */

	public static <T extends ITreeNode<T>> int getDepth(T root,
														T node)
	{
		int depth = 0;
		while (depth >= 0)
		{
			// Test for root
			if (node == root)
				break;

			// Get parent of node
			T parent = node.getParent();

			// If no parent, invalidate depth ...
			if (parent == null)
				depth = -1;

			// ... otherwise, ascend tree
			else
			{
				node = parent;
				++depth;
			}
		}
		return depth;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the path to the specified {@linkplain ITreeNode node} from the root of the tree to which it belongs.  The
	 * path is a list of nodes that includes the root and the target node.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  node
	 *           the node whose path is required.
	 * @return a list of nodes that denotes the path to <i>node</i> from the root of the tree to which it belongs.
	 * @see    #getPath(ITreeNode, ITreeNode)
	 */

	public static <T extends ITreeNode<T>> List<T> getPath(T node)
	{
		// Initialise list of nodes
		LinkedList<T> path = new LinkedList<>();

		// Add target node and its ancestors to list
		visitAscending(node, node0 ->
		{
			path.addFirst(node0);
			return true;
		});

		// Return list of nodes
		return path;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the path from the specified root {@linkplain ITreeNode node} to the specified node, which is assumed to
	 * be a descendant of the specified root.  The path is a list of nodes that includes the root and the target node.
	 * <p>
	 * If the specified root node is not an ancestor of the target node, the path will be a list of nodes from the root
	 * of the tree to which the target node belongs, which will be the first element of the list.
	 * </p>
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the node at which the path will start, unless it is not an ancestor of <i>node</i>.
	 * @param  node
	 *           the node whose path is required.
	 * @return a list of nodes that denotes the path from <i>root</i> to <i>node</i>, or, if <i>root</i> is not an
	 *         ancestor of <i>node</i>, the path to <i>node</i> from the root of the tree to which it belongs.
	 * @see    #getPath(ITreeNode)
	 */

	public static <T extends ITreeNode<T>> List<T> getPath(T root,
														   T node)
	{
		// Initialise list of nodes
		LinkedList<T> path = new LinkedList<>();

		// Add target node and its ancestors to list
		visitAscending(node, root, true, node0 ->
		{
			path.addFirst(node0);
			return true;
		});

		// Return list of nodes
		return path;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of indices to the specified {@linkplain ITreeNode node} from the root of the tree to which it
	 * belongs.  Each element of the list is the sum of the specified base index and the index of the corresponding node
	 * in its {@linkplain ITreeNode#getParent() parent}'s {@linkplain ITreeNode#getChildren() list of children}, or -1
	 * if the node is not a child of its parent.  The first element of the list is a child of the root node.  The list
	 * is empty if the target node is a root node.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  node
	 *           the node whose indices are required.
	 * @param  baseIndex
	 *           the base index that is added to each valid index in the list of indices.
	 * @return a list of nodes that denotes the path to <i>node</i> from the root of the tree to which it belongs.
	 * @see    #getPath(ITreeNode)
	 */

	public static <T extends ITreeNode<T>> List<Integer> getIndices(T   node,
																	int baseIndex)
	{
		// Initialise list of indices
		LinkedList<Integer> indices = new LinkedList<>();

		// Add indices of target node and its ancestors to list
		visitAscending(node, null, false, node0 ->
		{
			T parent = node0.getParent();
			if (parent != null)
			{
				int index = parent.getChildren().indexOf(node0);
				indices.addFirst((index < 0) ? -1 : baseIndex + index);
			}
			return true;
		});

		// Return list of indices
		return indices;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of indices of the path from the specified root {@linkplain ITreeNode node} to the specified node,
	 * which is assumed to be a descendant of the specified root.  Each element of the list is the sum of the specified
	 * base index and the index of the corresponding node in its {@linkplain ITreeNode#getParent() parent}'s {@linkplain
	 * ITreeNode#getChildren() list of children}, or -1 if the node has no parent or is not a child of its parent.  The
	 * list of indices may optionally include the index of the root node.
	 * <p>
	 * If the specified root node is not an ancestor of the target node, the list of indices will start at
	 * </p>
	 * <ul>
	 *   <li>the root of the tree to which the target node belongs, if <i>includeRoot</i> is {@code true}, or</li>
	 *   <li>the ancestor of the target node that is a child of the root of the tree to which the target node belongs,
	 *       if <i>includeRoot</i> is {@code false}.</li>
	 * </ul>
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the node at which the indices will start, unless it is not an ancestor of <i>node</i>.
	 * @param  node
	 *           the node whose indices are required.
	 * @param  includeRoot
	 *           the index of <i>root</i> will be included in the list of indices.
	 * @param  baseIndex
	 *           the base index that is added to each valid index in the list of indices.
	 * @return a list of nodes that denotes the path from <i>root</i> to <i>node</i>.
	 * @see    #getPath(ITreeNode)
	 */

	public static <T extends ITreeNode<T>> List<Integer> getIndices(T       root,
																	T       node,
																	boolean includeRoot,
																	int     baseIndex)
	{
		// Initialise list of indices
		LinkedList<Integer> indices = new LinkedList<>();

		// Add indices of target node and its ancestors to list
		visitAscending(node, root, includeRoot, node0 ->
		{
			T parent = node0.getParent();
			int index = (parent == null) ? -1 : parent.getChildren().indexOf(node0);
			indices.addFirst((index < 0) ? -1 : baseIndex + index);
			return true;
		});

		// Return list of indices
		return indices;
	}

	//------------------------------------------------------------------

	/**
	 * Searches a tree of {@link ITreeNode}s, starting from the specified root node, for a node whose path from the root
	 * matches the specified path descriptor.  The first element of the path descriptor is tested against the root node
	 * with the specified matcher: if there is no match, the search terminates immediately; otherwise, the root node
	 * becomes the current node, and the search proceeds by descending the tree, testing successive elements against the
	 * children of the current node until either no children match or the last element of the path descriptor is
	 * matched.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  <U>
	 *           the type of the elements of the path.
	 * @param  root
	 *           the node at the root of a tree of {@code ITreeNode}s at which the search will start.
	 * @param  path
	 *           a path descriptor whose elements are tested sequentially against <i>root</i> and its descendants with
	 *           <i>matcher</i>.
	 * @param  matcher
	 *           the matcher that tests a node against an element of <i>path</i>.
	 * @return the node that matches <i>path</i>, or {@code null} if no matching node is found.
	 */

	public static <T extends ITreeNode<T>, U> T findNode(T                 root,
														 Iterable<U>       path,
														 BiPredicate<T, U> matcher)
	{
		T result = null;
		Iterator<U> it = path.iterator();
		if (it.hasNext() && matcher.test(root, it.next()))
		{
			result = root;
			while (it.hasNext())
			{
				U target = it.next();
				result = result.getChildren().stream()
												.filter(child -> matcher.test(child, target))
												.findFirst()
												.orElse(null);
				if (result == null)
					break;
			}
		}
		return result;
	}

	//------------------------------------------------------------------

	/**
	 * Performs a depth-first traversal of a tree of {@link ITreeNode}s, starting from the specified root node and
	 * applying the specified action to each node that is visited.  The root node may optionally be visited.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the node at the root of a tree of {@code ITreeNode}s, each of which will have <i>action</i> applied to
	 *           it.
	 * @param  preorder
	 *           if {@code true}, each node will be visited <i>before</i> its descendants; otherwise, each node will be
	 *           visited <i>after</i> its descendants.
	 * @param  includeRoot
	 *           if {@code true}, <i>root</i> will have <i>action</i> applied to it.
	 * @param  action
	 *           the action that will be applied to each node of the tree that is visited.
	 * @return {@code true} if the traversal of the tree was completed.
	 */

	public static <T extends ITreeNode<T>> boolean visitDepthFirst(T                    root,
																   boolean              preorder,
																   boolean              includeRoot,
																   Function<T, Boolean> action)
	{
		// Visit root (preorder)
		if (includeRoot && preorder && !action.apply(root))
			return false;

		// Visit descendants
		for (T child : root.getChildren())
		{
			if (!visitDepthFirst(child, preorder, true, action))
				return false;
		}

		// Visit root (postorder)
		if (includeRoot && !preorder && !action.apply(root))
			return false;

		// Indicate traversal completed
		return true;
	}

	//------------------------------------------------------------------

	/**
	 * Performs a breadth-first traversal of a tree of {@link ITreeNode}s, starting from the specified root node and
	 * applying the specified action to each node that is visited.  The root node may optionally be visited.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the node at the root of a tree of {@code ITreeNode}s, each of which will have <i>action</i> applied to
	 *           it.
	 * @param  includeRoot
	 *           if {@code true}, <i>root</i> will have <i>action</i> applied to it.
	 * @param  action
	 *           the action that will be applied to each node of the tree that is visited.
	 * @return {@code true} if the traversal of the tree was completed.
	 */

	public static <T extends ITreeNode<T>> boolean visitBreadthFirst(T                    root,
																	 boolean              includeRoot,
																	 Function<T, Boolean> action)
	{
		@SuppressWarnings("unchecked")
		final	T	ROOT_PLACEHOLDER	= (T)new ITreeNode<T>()
		{
			@Override
			public T getParent()
			{
				return null;
			}

			@Override
			public List<T> getChildren()
			{
				return null;
			}
		};

		// Initialise queue
		Deque<T> queue = new ArrayDeque<>(32);

		// Add root or root placholder to queue
		queue.addLast(includeRoot ? root : ROOT_PLACEHOLDER);

		// While there are nodes to visit ...
		while (!queue.isEmpty())
		{
			// Get next node from queue
			T node = queue.removeFirst();

			// If node is root placholder, replace it with root ...
			if (node == ROOT_PLACEHOLDER)
				node = root;

			// ... otherwise, visit node
			else if (!action.apply(node))
				return false;

			// Add children of node to queue
			for (T child : node.getChildren())
				queue.addLast(child);
		}

		// Indicate traversal completed
		return true;
	}

	//------------------------------------------------------------------

	/**
	 * Ascends a tree of {@link ITreeNode}s from the specified node to the root node, applying the specified action to
	 * each node that is visited, including the root.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  startNode
	 *           the node from which the ascent will start.
	 * @param  action
	 *           the action that will be applied to each node of the tree that is visited.
	 * @return {@code true} if the ascent of the tree was completed.
	 */

	public static <T extends ITreeNode<T>> boolean visitAscending(T                    startNode,
																  Function<T, Boolean> action)
	{
		return visitAscending(startNode, null, false, action);
	}

	//------------------------------------------------------------------

	/**
	 * Ascends a tree of {@link ITreeNode}s from the specified node to the specified end node, applying the specified
	 * action to each node that is visited.  The end node may optionally be visited.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  startNode
	 *           the node from which the ascent will start.
	 * @param  endNode
	 *           the node at which the ascent will end.  <i>endNode</i> is visited if <i>includeEnd</i> is {@code
	 *           true}.  If <i>endNode</i> is {@code null}, <i>startNode</i> and all its ancestors will be visited.
	 * @param  includeEnd
	 *           if {@code true}, <i>endNode</i> will have <i>action</i> applied to it.
	 * @param  action
	 *           the action that will be applied to each node of the tree that is visited.
	 * @return {@code true} if the ascent of the tree was completed.
	 */

	public static <T extends ITreeNode<T>> boolean visitAscending(T                    startNode,
																  T                    endNode,
																  boolean              includeEnd,
																  Function<T, Boolean> action)
	{
		// Initialise current node
		T node = startNode;

		// Ascend tree
		while (node != null)
		{
			// Visit node
			if ((includeEnd || (node != endNode)) && !action.apply(node))
				return false;

			// Test for end of ascent
			if (node == endNode)
				break;

			// Ascend tree
			node = node.getParent();
		}

		// Indicate ascent completed
		return true;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if and only if the specified {@link ITreeNode} or one of its ancestors satisfies the
	 * specified test.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  startNode
	 *           the node from which the ascent will start.
	 * @param  test
	 *           the test that will be applied to <i>startNode</i> and its ancestors.
	 * @return {@code true} if and only if <i>startNode</i> or one of its ancestors satisfies <i>test</i>.
	 */

	public static <T extends ITreeNode<T>> boolean testAscending(T            startNode,
																 Predicate<T> test)
	{
		return (searchAscending(startNode, test) != null);
	}

	//------------------------------------------------------------------

	/**
	 * Performs a depth-first search of a tree of {@link ITreeNode}s, starting from the specified root node and applying
	 * the specified test to each node that is visited until a node is found that satisfies the test.  If such a node is
	 * found, the search terminates immediately and the matching node is returned.  The root node may optionally be
	 * included in the search.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the node at which the search will start.
	 * @param  preorder
	 *           if {@code true}, each node will be visited <i>before</i> its descendants; otherwise, each node will be
	 *           visited <i>after</i> its descendants.
	 * @param  includeRoot
	 *           if {@code true}, <i>root</i> will be included in the search.
	 * @param  test
	 *           the test that will be applied to each node of the tree that is visited until the test succeeds.
	 * @return the first node that returns {@code true} when <i>test</i> is applied to it, or {@code null} if no node
	 *         satisfies the test.
	 */

	public static <T extends ITreeNode<T>> T searchDepthFirst(T            root,
															  boolean      preorder,
															  boolean      includeRoot,
															  Predicate<T> test)
	{
		// Initialise result
		T result = null;

		// Test root (preorder)
		if (includeRoot && preorder && test.test(root))
			result = root;

		// Test descendants
		else
		{
			for (T child : root.getChildren())
			{
				result = searchDepthFirst(child, preorder, true, test);
				if (result != null)
					break;
			}
		}

		// Test root (postorder)
		if ((result == null) && includeRoot && !preorder && test.test(root))
			result = root;

		// Return result
		return result;
	}

	//------------------------------------------------------------------

	/**
	 * Performs a breadth-first search of a tree of {@link ITreeNode}s, starting from the specified root node and
	 * applying the specified test to each node that is visited until a node is found that satisfies the test.  If such
	 * a node is found, the search terminates immediately and the matching node is returned.  The root node may
	 * optionally be included in the search.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the node at which the search will start.
	 * @param  includeRoot
	 *           if {@code true}, <i>root</i> will be included in the search.
	 * @param  test
	 *           the test that will be applied to each node of the tree that is visited until the test succeeds.
	 * @return the first node that returns {@code true} when <i>test</i> is applied to it, or {@code null} if no node
	 *         satisfies the test.
	 */

	public static <T extends ITreeNode<T>> T searchBreadthFirst(T            root,
																boolean      includeRoot,
																Predicate<T> test)
	{
		@SuppressWarnings("unchecked")
		final	T	ROOT_PLACEHOLDER	= (T)new ITreeNode<T>()
		{
			@Override
			public T getParent()
			{
				return null;
			}

			@Override
			public List<T> getChildren()
			{
				return null;
			}
		};

		// Initialise queue
		Deque<T> queue = new ArrayDeque<>(32);

		// Add root or root placeholder to queue
		queue.addLast(includeRoot ? root : ROOT_PLACEHOLDER);

		// Initialise result
		T result = null;

		// While there are nodes to visit ...
		while (!queue.isEmpty())
		{
			// Get next node from queue
			T node = queue.removeFirst();

			// If node is root placeholder, replace it with root ...
			if (node == ROOT_PLACEHOLDER)
				node = root;

			// ... otherwise, test node
			else if (test.test(node))
			{
				result = node;
				break;
			}

			// Add children of node to queue
			for (T child : node.getChildren())
				queue.addLast(child);
		}

		// Return result
		return result;
	}

	//------------------------------------------------------------------

	/**
	 * Ascends a tree of {@link ITreeNode}s from the specified start node to the root node, applying the specified test
	 * to each node that is visited until a node is found that satisfies the test.  If such a node is found, the search
	 * terminates immediately and the matching node is returned.  The root is included in the search.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  startNode
	 *           the node from which the search will start.
	 * @param  test
	 *           the test that will be applied to each node of the tree that is visited until the test succeeds.
	 * @return the first node that returns {@code true} when <i>test</i> is applied to it, or {@code null} if no node
	 *         satisfies the test.
	 */

	public static <T extends ITreeNode<T>> T searchAscending(T            startNode,
															 Predicate<T> test)
	{
		return searchAscending(startNode, null, false, test);
	}

	//------------------------------------------------------------------

	/**
	 * Ascends a tree of {@link ITreeNode}s from the specified start node to the specified end node, applying the
	 * specified test to each node that is visited until a node is found that satisfies the test.  If such a node is
	 * found, the search terminates immediately and the matching node is returned.  The end node may optionally be
	 * included in the search.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  startNode
	 *           the node from which the search will start.
	 * @param  endNode
	 *           the node at which the search will end.  <i>endNode</i> is included in the search if <i>includeEnd</i>
	 *           is {@code true}.  If <i>endNode</i> is {@code null}, <i>startNode</i> and all its ancestors will be
	 *           included in the search.
	 * @param  includeEnd
	 *           if {@code true}, <i>endNode</i> will have <i>test</i> applied to it.
	 * @param  test
	 *           the test that will be applied to each node of the tree that is visited until the test succeeds.
	 * @return the first node that returns {@code true} when <i>test</i> is applied to it, or {@code null} if no node
	 *         satisfies the test.
	 */

	public static <T extends ITreeNode<T>> T searchAscending(T            startNode,
															 T            endNode,
															 boolean      includeEnd,
															 Predicate<T> test)
	{
		// Initialise result
		T result = null;

		// Perform search
		T node = startNode;
		while (node != null)
		{
			// Apply test to the node
			if (((node != endNode) || includeEnd) && test.test(node))
			{
				result = node;
				break;
			}

			// Test for end of ascent
			if (node == endNode)
				break;

			// Ascend the tree
			node = node.getParent();
		}

		// Return result
		return result;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of the tree of {@link ITreeNode}s whose root is the specified node.
	 *
	 * @param  <T>
	 *           the type of the nodes of the tree.
	 * @param  root
	 *           the node at the root of the tree.
	 * @param  indentIncrement
	 *           the number of spaces by which the indent of a line of text will be incremented for each level of the
	 *           tree below <i>root</i>.
	 * @param  converter
	 *           the function that will convert each node to its string representation.
	 * @return a string representation of the tree whose root is <i>root</i>.
	 */

	public static <T extends ITreeNode<T>> String treeToString(T                   root,
															   int                 indentIncrement,
															   Function<T, String> converter)
	{
		StringBuilder buffer = new StringBuilder(256);
		visitDepthFirst(root, true, true, node ->
		{
			// Get depth of node below root
			int depth = getDepth(root, node);

			// Append linefeed if node is not root
			if (depth > 0)
				buffer.append('\n');

			// Append indent
			int indent = depth * indentIncrement;
			for (int i = 0; i < indent; i++)
				buffer.append(' ');

			// Append string representation of node
			buffer.append(converter.apply(node));

			// Continue to traverse tree
			return true;
		});
		return buffer.toString();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
