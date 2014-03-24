import java.util.ArrayList;

/**
 * A TreeTransformer provides a method for transforming a tree.  Combined with matching
 * conditions, it provides a way to do tree rewrite rules.
 * 
 * Note: the matching conditions were originally part of this interface, but I later found
 * that interfaces can't have constructors, which made it tough to have it contain extra
 * variables.
 */
public interface TreeTransformer {
	/**
	 * This takes a matched subtree and transforms it, returning the transformed subtree.
	 * Note: @left and @right may be empty, but they must not be null.  @subSymbolTree may be null
	 */
	TreeNode transform(ParserSymbol parentSymbol,
		ArrayList<TreeNode> left,
		TreeNode subSymbolTree,
		ArrayList<TreeNode> right);
}
