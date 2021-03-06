import java.util.Iterator;

public class ProductionRule implements Iterable<ParserSymbol>{
		private NonTerminalParserSymbol left;
		private ParserSymbol[] right;


		public ProductionRule(NonTerminals left, Enum... rightEnums){
			this.left = new NonTerminalParserSymbol(left);


			this.right = new ParserSymbol[rightEnums.length];

			int i = 0;
			for (Enum e : rightEnums) {
				if (e instanceof State) {
					right[i++] = new Token((State)e);
				} else {
					right[i++] = new NonTerminalParserSymbol((NonTerminals)e);
				}
			}
		}


		NonTerminalParserSymbol left() {
			return left;
		}

		ParserSymbol[] right() {
			return right;
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

		public String toString() {
			String desc = new String(left + " -->");
			for (ParserSymbol smbl : right) {
				desc += " " + smbl;
			}

			return desc;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || this.getClass() != obj.getClass()) return false;

			ProductionRule other = (ProductionRule)obj;

			if (other.right.length != right.length) return false;

			for (int i = 0; i < right.length; i++) {
				if (!other.right[i].equals(right[i])) return false;
			}

			return other.left.equals(left);
		}

		@Override
		public int hashCode() {
			return left.hashCode();
		}
}
