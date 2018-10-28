import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Collections;

public class AprioriImproved{
	public double minSupport;
	public ArrayList<ArrayList<String>> transactions;
	public long numberTrans;
	public ArrayList<ArrayList<String>> candidates;
	public int numberitemset;
	public ArrayList<ArrayList<String>> itemset;
	public ArrayList<ArrayList<String>> frequentSet;
	public Hashtable<String, Integer> uniqueItem;
	
	AprioriImproved(String fileName, double minSupport){
		this.minSupport = minSupport;
		this.uniqueItem= new Hashtable<String, Integer>();
		this.transactions = this.dataFromFile(fileName); //read data from file
		this.numberTrans = this.transactions.size();
		this.numberitemset = 0;
		this.itemset = new ArrayList<ArrayList<String>>();
		this.candidates = new ArrayList<ArrayList<String>>();
		this.frequentSet = new ArrayList<ArrayList<String>>();
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
	
	public void getCandidates(){
		this.candidates = new ArrayList<ArrayList<String>>();    //completely change the candidates list and start over
		if(this.numberitemset < 2) {
			this.candidates = findFrequent();
		}
		else{
			String cand =  null;
			for(int i=0; i<this.itemset.size();i++){
				for(int j=i;j< this.itemset.size();j++){
					int difference =0;
					ArrayList<String> subset = new ArrayList<>();
					for(int k=0;k<this.itemset.get(j).size();k++){
						if(!this.itemset.get(i).contains(this.itemset.get(j).get(k))){
							difference++;
							cand = this.itemset.get(j).get(k);
						}
					}
					if(difference == 1){
						subset.addAll(this.itemset.get(i));
						subset.add(cand);
						this.candidates.add(subset);
					}
				}
			}
		}
	}
	
	public ArrayList<ArrayList<String>> findFrequent(){
		ArrayList<ArrayList<String>> frequent = new  ArrayList<ArrayList<String>>();
		for(String s : this.uniqueItem.keySet()){
			ArrayList<String> array = new ArrayList<String>();
			if(this.uniqueItem.get(s) >= this.numberTrans*this.minSupport){
				array.add(s);
				frequent.add(array);
			}
		}
		return frequent;
	}
	
	public void prunning(){   //remove sets with unqualified support
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		if(this.numberitemset > 1){
			for(ArrayList<String> array: this.candidates){
				boolean fr = true;
				for(int j=0;j<array.size();j++){
					ArrayList<String> t = new ArrayList<String>();
					t.addAll(array);
					array.remove(j);
					if(!this.itemset.contains(array)){
						fr = false;
						break;
					}
					array = t;
				}
				if(fr){
					temp.add(array);
				}
			}
			this.candidates = temp;
		}
	
	}
	
	public void aprioriMain(){
		this.numberitemset = 1;
		int number = 0;
		do{
			getCandidates();  //get candidates
			prunning(); // prune the candidates
			this.itemset.clear();
			for(ArrayList<String> a: this.candidates){
				int c = 0;
				for(ArrayList<String> trans: this.transactions){
					boolean contain = true;
					for(String s: a){
						if(!trans.contains(s)){
							contain = false;
							break;
						}
					}
					if(contain){
						c++;
					}
				}
				if(c >= this.numberTrans*this.minSupport && !this.itemset.contains(a)){
					this.itemset.add(a);
				}
			}
			for(int i = 0; i < this.transactions.size(); i++){
				int notThere = 0;
				for(ArrayList<String> a: this.itemset){
					for(String s: a){
						if(!this.transactions.get(i).contains(s)){
							notThere++;
						}
					}
				}
				if(notThere == this.itemset.size()){
					this.transactions.remove(i);
				}
			}
			this.frequentSet.addAll(this.itemset);
			this.numberitemset++;
		}while(this.itemset.size() >0);
	}
	
	public void printFrequentItemSet(){
		int result =0;
		ArrayList<ArrayList<String>> sortedList = new ArrayList<ArrayList<String>>(this.frequentSet);
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
		//System.out.println("There are a total of: " + result+ " frequent itemsets");
	}
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("Please give the name of the file you want to mine frequent set [current on directory: data.txt & test.txt");
		String file = scan.next();
		System.out.println("Enter minimum support in ratio [Example 0.6]");
		double minSupport = scan.nextDouble();
		AprioriImproved aprioriImproved = new AprioriImproved(file, minSupport);
		long start = System.currentTimeMillis();
		aprioriImproved.aprioriMain();
		long end = System.currentTimeMillis();
		aprioriImproved.printFrequentItemSet();
		System.out.println("Run time: " + (end - start)/1000.0);
	}

}