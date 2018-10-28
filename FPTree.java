import java.util.ArrayList;

public class FPTree {
	public boolean root;
	public ArrayList<FPTree> children;
	public FPTree parent;
	public String id;
	public int count;
	public FPTree next;
	
	public FPTree(String id){
		this.root = false;
		this.children = new ArrayList<FPTree>();
		this.id= id;
	}
}