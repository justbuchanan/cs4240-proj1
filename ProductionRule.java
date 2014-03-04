import java.util.Iterator;

public class ProductionRule implements Iterable<ParserSymbol>{
		private NonTerminals left;
		private ParserSymbol[] right;
		int curr;


		public ProductionRule(NonTerminals left, ParserSymbol[] right){
			this.left = left;
			this.right = right;
			curr = right.length - 1;
		}


		NonTerminals left() {
			return left;
		}


		private class ProductionRuleIterator implements Iterator<ParserSymbol> {
			private int curr;
			private ProductionRule rule;

			ProductionRuleIterator(ProductionRule rule) {
				this.rule = rule;
				curr = 0;
			}

			@Override
			public boolean hasNext() {
				return curr < rule.right.length;
			}

			@Override
			public ParserSymbol next() {
				if (!hasNext()) {
					throw new IndexOutOfBoundsException();
				}

				ParserSymbol val = rule.right[curr];
				curr++;
				return val;
			}

			@Override
			public void remove() {
				throw new RuntimeException("remove() not supported");
			}
		}

		public Iterator<ParserSymbol> iterator() {
			return new ProductionRuleIterator(this);
		}

}
