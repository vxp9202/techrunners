import java.util.*;
import java.io.*;

public class Project {
	
	private static final String stopwordlocation = "stopword/stopwords.txt";
	
	private static final String documentsFolder = "Small_set";
	
	
	//declare
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<DocP>> docLists;
	double[] docLength;
	String folderN;
	String[] stopList;
	
	//constructor
	public Project(String folderName) {
		folderN=folderName;
		File folder = new File(folderName);
		File[] listOffiles = folder.listFiles();
		myDocs = new String[listOffiles.length];
		ArrayList<DocP> docList;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<DocP>>();
		
		for(int i=0;i<listOffiles.length;i++) {
			System.out.println("Files are : " + listOffiles[i].getName());
			myDocs[i] = listOffiles[i].getName();
		}
		
		stopList = parseStopwords();			//add stopwords from data
		Arrays.sort(stopList);
		
		for(int j=1;j<=myDocs.length;j++) {
			String[] tokens = parse(folderName + "/" + myDocs[j-1]);
			for(String token:tokens) {
				if(searchStopword(token) == -1) {
				if(!termList.contains(token)) {
					termList.add(token);
					docList = new ArrayList<DocP>();
					DocP doc = new DocP(j,1);
					docList.add(doc);
					docLists.add(docList);
				}
				else {
					int index = termList.indexOf(token);
					docList = docLists.get(index);
					boolean match=false;
					for(DocP doc:docList) {
						if(doc.docId==j) {
							doc.tw++;
							match=true;
							break;
						}
					}
					if(!match) {
						DocP doc = new DocP(j,1);
						docList.add(doc);
					}
				}
				}
			}

		}
		int N= myDocs.length;
		docLength = new double[N];
		for(int i=0;i<termList.size();i++){
			docList = docLists.get(i);
			int df= docList.size();
			DocP doc;
			for(int j=0;j<docList.size();j++) {
				doc = docList.get(j);
				double tfidf = (1+ Math.log(doc.tw)) * (Math.log(N/df*1.0));
				docLength[doc.docId-1] = Math.pow(tfidf, 2);
				doc.tw = tfidf;
				docList.set(j, doc);
			}
		}
		for(int i=0;i<N;i++) {
			docLength[i] += Math.sqrt(docLength[i]);
		}
	}
	
	public HashMap<Integer, Double> rankSearch(String[] query) {
		HashMap<Integer, Double> docs = new HashMap<>();
		ArrayList<DocP> docList;
		ArrayList<String> wildcardterms = new ArrayList<String>();
		for(int i=0;i<query.length;i++) {
			if(query[i].contains("*")) {
				System.out.println("keyword " + query[i] + "contains wildcard");
				for(String t:termList) {
					if(t.startsWith(query[i].substring(0, query[i].length()-1))) {
						System.out.println("terms are " + t);
						wildcardterms.add(t);
					}
				}
			}
		}
		
		ArrayList<String> reformedquery = new ArrayList<String>();
		for(int i=0;i<query.length;i++) {
			if(query[i].contains("*")) {
				continue;
			}
			else {
				reformedquery.add(query[i]);
			}
		}
		
		for(String wct:wildcardterms) {
			reformedquery.add(wct);
		}
		
		double queryLength =0;
		int termId=0;
		while(termId <reformedquery.size()) {
			int index = termList.indexOf(reformedquery.get(termId));
			if(index <0) {
				termId++;
				continue;
			}
			docList = docLists.get(index);
			double t_w = Math.log(myDocs.length*1.0/docList.size()*1.0);
			queryLength += Math.pow(t_w, 2);
			termId++;	
		}
		queryLength = Math.sqrt(queryLength);					//normalized q length
		
		for(String term:reformedquery) {
			int index = termList.indexOf(term);
			if(index < 0) {
				ArrayList<String> correctterms=editDistance(term);
				for(String t:correctterms) {
					System.out.println("did you mean ? " + t);
				}
				//break;
				continue;							//no match term then ignore 
			}
			else {
				docList = docLists.get(index);
				DocP doc;
				double t_w = Math.log(myDocs.length*1.0/docList.size()*1.0);
				// queryLength += Math.pow(t_w, 2);
				// queryLength = Math.sqrt(queryLength);					//normalized q length
				 
				for(int i=0;i<docList.size();i++) {
					doc= docList.get(i);
					//double score = (t_w * doc.tw);
					double score = (t_w * doc.tw)/(queryLength * docLength[doc.docId-1]); //cosine sim
					if(!docs.containsKey(doc.docId)) {
						docs.put(doc.docId, score);
					}
					else {
						score += docs.get(doc.docId);
						docs.put(doc.docId, score);
					}
				}
			}
		}
		//System.out.println(docs);
		
	
		
		
		List<Map.Entry<Integer, Double>> sortedlists = new LinkedList<Map.Entry<Integer, Double>>(docs.entrySet());
		
		
		  Collections.sort(sortedlists, new Comparator<Map.Entry<Integer, Double>>() { 
			  public int compare(Map.Entry<Integer, Double> a1, Map.Entry<Integer, Double> a2) {
				  return  a2.getValue().compareTo(a1.getValue());
				  } 
			  });
		 
		 HashMap<Integer, Double> sorteddocs = new LinkedHashMap<Integer,Double>();
		for(Map.Entry<Integer, Double> hsm:sortedlists) {
			sorteddocs.put(hsm.getKey(), hsm.getValue());
		}
		  
		//System.out.println( " sorted linked list:" + sortedlists);
		//System.out.println("sorted hashmap:" + sorteddocs);
		return sorteddocs;
	}
	
	public ArrayList<String> editDistance(String term){
		//add correct terms to lexicon from data
		String[] lexicon= {"analyze,analysis","analyzing","addition","adding","mobile","platforms","game"};
		ArrayList<String> correctterms = new ArrayList<String>();
		
		for(int i=0;i<lexicon.length;i++) {
			String lex = lexicon[i];
			 int distance= ed(term,lex);
			if(distance <=2) {
				correctterms.add(lex);
			}
			
		}
		return correctterms;
	}
	
	public int ed(String t, String l) {
		if(t.length()==0) {
			return l.length();
		}
		else if(l.length() ==0) {
			return t.length();
		}
		else {
			int addD = ed(t,l.substring(0,l.length()-1)) + 1;
			int removeD = ed(t.substring(0,t.length()-1),l) +1;
			int changeD = ed(t.substring(0,t.length()-1),l.substring(0,l.length()-1)) +
					((t.charAt(t.length()-1))== (l.charAt(l.length()-1))? 0:1);
			return Math.min(Math.min(addD, removeD), changeD);
		}
	}
	
	public String[] parse(String fileName) {
		String[] tokens=null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line=null;
			String allLines = new String();
			
			while((line=reader.readLine())!=null) {
				allLines += line.toLowerCase();
			}
			tokens = allLines.split("[ .;:,/?~`&!@#$%\\^\"({)}’�]+");
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return tokens;
	}
	
	public String[] parseStopwords() {
		List<String> allines = new ArrayList<String>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(stopwordlocation));
			String line =null;
			while((line=reader.readLine())!=null) {
				allines.add(line);
			}

		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return allines.toArray(new String[allines.size()]);
	}
	
	public int searchStopword(String key) {
		int lo=0;
		int hi= stopList.length-1;
		
		while(lo<=hi) {
			int mid =lo + (hi-lo)/2;
			int result = key.compareTo(stopList[mid]);
			if(result <0) {
				hi= mid-1;
			}
			else if(result >0) {
				lo=mid+1;
			}
			else {
				return mid;				//match
			}
		}
		return -1;					//no match
	}
	
	public String toString() {
		String matrixString = new String();
		ArrayList<DocP> docList;
		for(int i=0;i<termList.size();i++) {
			matrixString += String.format("%-12s", termList.get(i));
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++) {
				matrixString += docList.get(j) + "\t";
			}
			matrixString += "\n";
		}
		return matrixString;
	}
	
	public void printResults(HashMap<Integer, Double> result)
	{
		if(result.isEmpty())
		{
			System.out.println("No docs found for the given query");
			return;
		}
		System.out.println("Results:");
		//List<String> snippets = new ArrayList<String>();
		for(Integer key: result.keySet()) {
			System.out.println("Document:" + key);
			String line=null;
			
			String path = this.folderN + "/" + this.myDocs[key.intValue()-1];
			try {
				BufferedReader reader = new BufferedReader(new FileReader(path));
					line =reader.readLine();
					//snippets.add(line);
					System.out.println(line);
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Project p = new Project(documentsFolder);
		System.out.println(p);
		

		String[] query= {"analyzing", "addition"};
		HashMap<Integer, Double> result = p.rankSearch(query);	
		p.printResults(result);
		System.out.println();
		
		
		String[] query1= {"analyzing", "pic*"};
		HashMap<Integer, Double> result1 = p.rankSearch(query1);	
		p.printResults(result1);
		System.out.println();
		
		
		String[] query2= {"mobil", "gam"};
		HashMap<Integer, Double> result2 = p.rankSearch(query2);	
		p.printResults(result2);
		
		
		/*System.out.println("Results:");
		//List<String> snippets = new ArrayList<String>();
		for(Integer key: result.keySet()) {
			System.out.println("Document:" + key);
			String line=null;
			
			String path = p.folderN + "/" + p.myDocs[key.intValue()-1];
			try {
				BufferedReader reader = new BufferedReader(new FileReader(path));
					line =reader.readLine();
					//snippets.add(line);
					System.out.println(line);
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		String[] finalans = snippets.toArray(new String[snippets.size()]);
		for(String s:finalans) {
			System.out.println(s);
			System.out.println();
		}*/
		
		
	}

}

class DocP{
	int docId;
	double tw;
	
	public DocP(int did, double weight) {
		docId = did;
		tw = weight;
	}
	public String toString() {
		String docIdString = docId + ":" + tw;
		return docIdString;
	}
}
