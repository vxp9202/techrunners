package com.javalab.sample;

public class DocP {
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
