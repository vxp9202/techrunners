package com.javalab.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Matrix {
	
	
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<DocP>> docLists;
	double[] docLength;
	ArrayList<String> reformedquery;
	String folderN="C:\\Users\\Vismay\\eclipse-workspace\\WebApp01\\Small_set";
	
	public Matrix() {
		
		
		 
		String folderName=folderN;
		File folder = new File(folderName);
		File[] listOffiles = folder.listFiles();
		myDocs = new String[listOffiles.length];
		ArrayList<DocP> docList;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<DocP>>();
		
		for(int i=0;i<listOffiles.length;i++) {
			//System.out.println("Files are : " + listOffiles[i].getName());
			myDocs[i] = listOffiles[i].getName();
		}
		
		for(int j=1;j<=myDocs.length;j++) {
			String[] tokens = parse(folderName + "/" + myDocs[j-1]);
			for(String token:tokens) {
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
	}//end of contructor
	
	
	
	private String[] parse(String string) {
		// TODO Auto-generated method stub
		String[] tokens=null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(string));
			String line=null;
			String allLines = new String();
			
			while((line=reader.readLine())!=null) {
				allLines += line.toLowerCase();
			}
			tokens = allLines.split("[ .,/?~`!@#$%\\^\"({)}]+");
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return tokens;	
	}//end of parsemethod
	
	
	public HashMap<Integer, Double> rankSearch(String[] terms) {
		// TODO Auto-generated method stub
		HashMap<Integer, Double> docs = new HashMap<>();
		ArrayList<DocP> docList;
		//////
		ArrayList<String> wildcardterms = new ArrayList<String>();
		for(int i=0;i<terms.length;i++) {
			if(terms[i].contains("*")) {
				System.out.println("keyword " + terms[i] + "contains wildcard");
				for(String t:termList) {
					if(t.startsWith(terms[i].substring(0, terms[i].length()-1))) {
						System.out.println("Are you searching for? " + t);
						wildcardterms.add(t);											//all terms that matches wildcard string
					}
				}
			}
		}
		
		reformedquery = new ArrayList<String>();
		for(int i=0;i<terms.length;i++) {
			if(terms[i].contains("*")) {
				continue;
			}
			else {
				reformedquery.add(terms[i]);
			}
		}
		
		for(String wct:wildcardterms) {
			reformedquery.add(wct);
		}			
		////
		
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
				continue;							//no match term then ignore
				//return null;
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
		System.out.println(docs);
		
	
		
		
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

}
