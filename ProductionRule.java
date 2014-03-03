public class ProductionRule{
		private NonTerminals left;
		private ParserSymbol[] right;


		public ProductionRule(NonTerminals left, ParserSymbol[] right){
			this.left = left;
			this.right = right;
		}

}