import java.util.Comparator;

public class TableNode {
	public String id;
	public int supportCount;
	public FPTree nodeLink;
	
	public TableNode(String id, int supportCount){
		this.id = id;
		this.supportCount = supportCount;
	}
	
	@Override
	public String toString() {
		return "TableNode [id=" + id + ", supportCount=" + supportCount + "]";
	}
}
