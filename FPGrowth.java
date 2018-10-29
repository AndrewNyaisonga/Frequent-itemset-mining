import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class FPGrowth{
	public double minSupport;
	public ArrayList<ArrayList<String>> transactions;
	public long numberTrans;
	public Set<ArrayList<String>> frequentPattern;
	public Hashtable<String, Integer> uniqueItem;
	public ArrayList<TableNode> table;
	public FPTree fptree;
	
	FPGrowth(String fileName, double minSupport){
		this.minSupport = minSupport;
		this.uniqueItem= new Hashtable<String, Integer>();
		this.transactions = this.dataFromFile(fileName); //read data from file
		this.numberTrans = this.transactions.size();
		this.frequentPattern = new HashSet<ArrayList<String>>();
		this.table = this.createTable();   //make table
		this.fptree = this.scanSecondTime(this.table,this.transactions); //create fpTree
	}
	
	public ArrayList<ArrayList<String>> dataFromFile(String fileName){
		try{
			ArrayList<ArrayList<String>> t = new ArrayList<ArrayList<String>>();
			Scanner file = new Scanner(new File(fileName));
			while(file.hasNextLine()){
				String line = file.nextLine();
				ArrayList<String> transaction = new ArrayList<>();
				this.numberTrans++;
				StringTokenizer st = new StringTokenizer(line,",");
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					transaction.add(token);
					if(this.uniqueItem.containsKey(token)){
						this.uniqueItem.put(token,this.uniqueItem.get(token)+1);
					}else{
						this.uniqueItem.put(token,1);
					}
				}
				t.add(transaction);
			}
			return t;
		
		}catch(FileNotFoundException e){
			System.out.println("File doesn't exist or couldn't open file");
		}
		return null;
	}
	
	public FPTree scanSecondTime(ArrayList<TableNode> table,ArrayList<ArrayList<String>> subsetOfTransactions){
		FPTree newFPTree = new FPTree("null");
		newFPTree.root = true;
		newFPTree.id = null;
		for(ArrayList<String> transaction: subsetOfTransactions){
			ArrayList<String> orderedtrans = new ArrayList<String>();
			for(TableNode t: table){
				if(transaction.contains(t.id)){
					orderedtrans.add(t.id);
				}
			}
			add(orderedtrans, newFPTree, table,0);
		}
		return newFPTree;
	}
	
	public void add(ArrayList<String> orderedtrans, FPTree fpTree,ArrayList<TableNode> table,int n){
		if(n < orderedtrans.size()){
			String item = orderedtrans.get(n);
			FPTree newTree = null;
			boolean found = false;
			for(FPTree child: fpTree.children){
				if(child.id.equals(item)){
					newTree = child;
					child.count++;
					found = true;
					break;
				}
			}
			if(!found){
				newTree = new FPTree(item);
				newTree.count = 1;
				newTree.parent = fpTree;
				fpTree.children.add(newTree);
				for(TableNode t: table){
					if(t.id.equals(item)){
						FPTree temp = t.nodeLink;
						if(temp == null){
							t.nodeLink = newTree;
						}else{
							while(temp.next != null){
								temp = temp.next;
							}
							temp.next = newTree;
						}
					}
				}
			}
			add(orderedtrans, newTree, table,n+1);
		}
	}
	public void growthOfFP(){
		ArrayList<String> list = new ArrayList<>();
		list.add(null);
		growthHelper(this.fptree,list,this.table);
	}
	public void growthHelper(FPTree fpTree, ArrayList<String> toCheck, ArrayList<TableNode> table){
		if(isonePath(fpTree)){
			ArrayList<String> items = new ArrayList<String>();
			while(fpTree != null){
				if(fpTree.id != null){
					items.add(fpTree.id);
				}
				if(fpTree.children.size() > 0){
					fpTree = fpTree.children.get(0);
				}else{
					fpTree = null;
				}
			}
			ArrayList<ArrayList<String>> combinations = combinations(items, table);
			for(ArrayList<String> combination: combinations){
				combination.addAll(toCheck);
			}
			frequentPattern.addAll(combinations);
		}else{
			for(int i = table.size() - 1; i >=0; i--){
				ArrayList<String> combination = new ArrayList<String>();
				combination.addAll(toCheck);
				combination.remove(null);
				combination.add(table.get(i).id);
				int count = table.get(i).supportCount;
				frequentPattern.add(combination);
				ArrayList<ArrayList<String>> transactions = new ArrayList<ArrayList<String>>();
				FPTree temp = table.get(i).nodeLink;
				HashSet<String> newOneItems = new HashSet<String>();
				while(temp != null){
					FPTree temp2 = temp;				
					ArrayList<String> newItemset = new ArrayList<String>();
					while(temp.id != null){
						if(temp != temp2){
							newItemset.add(temp.id);
							newOneItems.add(temp.id);
						}
						temp = temp.parent;
					}
					for(int j = 0; j < temp2.count; j++){
						transactions.add(newItemset);
					}
					temp = temp2.next;
				}
				ArrayList<TableNode> newtable = createtableSub(transactions, newOneItems);
				FPTree newTree = scanSecondTime(newtable,transactions);
				if(newTree.children.size() > 0){
					growthHelper(newTree, combination, newtable);
				}
			}
		}
	}
	
	public ArrayList<TableNode> createTable(){
		ArrayList<TableNode> table = new ArrayList<TableNode>();
		for(String s: uniqueItem.keySet()){
			if(uniqueItem.get(s) >= numberTrans*minSupport){
				table.add(new TableNode(s, uniqueItem.get(s)));
			}
		}
		return sortTable(table);
	}
	
	public ArrayList<TableNode> createtableSub(ArrayList<ArrayList<String>> transactions, HashSet<String> items){
		ArrayList<TableNode> table = new ArrayList<TableNode>();
		for(String s: items){
			int a = 0;
			for(ArrayList<String> temp: transactions){
				if(temp.contains(s)){
					a++;
				}
			}
			if(a >= numberTrans * minSupport){
				table.add(new TableNode(s, a));
			}
		}
		return sortTable(table);
	}
	public ArrayList<TableNode> sortTable(ArrayList<TableNode> table){
		Collections.sort(table,(t1, t2) -> Integer.compare(t1.supportCount,t2.supportCount));
		//Collections.sort(table, new TableComparator());
		return table;
	}
	public boolean isonePath(FPTree fpTree){
		boolean path = true;
		if(fpTree.children.size() > 1){
			return false;
		}else{
			for(FPTree child: fpTree.children){
				if(path){
					path = isonePath(child);
					continue;
				}else{
					break;
				}
			}
		}
		return path;
	}
	public ArrayList<ArrayList<String>> combinations(ArrayList<String> items, ArrayList<TableNode> table){
		ArrayList<ArrayList<String>> combinations = new ArrayList<ArrayList<String>>();
		if(items.size() != 0){
			String s = items.get(0);
			if(items.size() > 1){
				items.remove(0);
				ArrayList<ArrayList<String>> combinationsSub = combinations(items, table);
				combinations.addAll(combinationsSub);
				for(ArrayList<String> a: combinationsSub){
					for(int i = 0; i < a.size(); i++){
						ArrayList<String> combination = new ArrayList<String>();
						int count = Integer.MAX_VALUE;
						for(int j = 0; j <= i; j++){
							for(TableNode t: table){
								if(t.id.equals(a.get(j)) && count < t.supportCount){
									count = t.supportCount;
								}
							}
							combination.add(a.get(j));
						}
						for(TableNode t: table){
							if(t.id.equals(s) && count < t.supportCount){
								count = t.supportCount;
							}
						}
						if(count >= minSupport*numberTrans){
							combination.add(s);
							combinations.add(combination);
						}
					}
				}
			}
			ArrayList<String> combination = new ArrayList<String>();
			combination.add(s);
			combinations.add(combination);
		}
		return combinations;
	}
	
	public void FPGrowthMain(){
		growthOfFP();
	}
	
	public void printFrequentPattern(){
		int result =0;
		ArrayList<ArrayList<String>> sortedList = new ArrayList<ArrayList<String>>(this.frequentPattern);
		Collections.sort(sortedList,(a1,a2) -> Integer.compare(a1.size(),a2.size()));
		for(ArrayList<String> a: sortedList){
			System.out.print("{");
			for(int i=0;i<a.size();i++){
				System.out.print(a.get(i));
				if(i != a.size()-1) System.out.print(", ");
			}
			System.out.print("}");
			System.out.println();
			result ++;
		}
		System.out.println("There are a total of: " + result+ " frequent itemsets");
	}
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("Please give the name of the file you want to mine frequent set [current on directory: data.txt & test.txt");
		String file = scan.next();
		System.out.println("Enter minimum support in ratio [Example 0.6]");
		double minSupport = scan.nextDouble();
		FPGrowth fpgrowth= new FPGrowth(file, minSupport);
		long start = System.currentTimeMillis();
		fpgrowth.FPGrowthMain();
		long end = System.currentTimeMillis();
		fpgrowth.printFrequentPattern();
		System.out.println("Run time: " + (end - start)/1000.0);
	}

}