import java.util.Iterator;

public class ProductionRule implements Iterator{
		private NonTerminals left;
		private ParserSymbol[] right;
		int curr;


		public ProductionRule(NonTerminals left, ParserSymbol[] right){
			this.left = left;
			this.right = right;
			curr = right.length - 1;
		}


		@Override
		public boolean hasNext() {
			return curr >= 0;
		}


		@Override
		public ParserSymbol next() {
			ParserSymbol val = right[curr];
			curr--;
			return val;
		}


		@Override
		public void remove() {
			curr--;
			
		}
		
		

}