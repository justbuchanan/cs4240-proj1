
public class Scope {
	private String funcName;
	private int level;
	
	public Scope(String funcName, int level){
		this.funcName = funcName;
		this.level = level;
	}
	
	public String getFuncName(){
		return funcName;
	}
	
	public int getLevel(){
		return level;
	}
	
	public boolean equals(Scope scp){
		return funcName.equals(scp.getFuncName()) && 
				level == scp.getLevel();
	}
}
