/*
 * We use the output optimized by Task1_Project.py as a input query to compute TFIDF
 * and get the categries as the output. 
 * 
 */

package task1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;



public class EasySearch {

	
	public static void main(String[] args) throws IOException, ParseException {
		
		int count=0;
		File f = new File("E:\\MS\\SEM2\\Search\\Project\\LDATop100.csv");
		FileWriter fstream = new FileWriter(new File("E:\\MS\\SEM2\\Search\\Project\\TFIDFFile_LDA100.txt"));
		BufferedWriter out = new BufferedWriter(fstream);
		StringBuilder sb = new StringBuilder();
		try {
			   FileInputStream inputStream = new FileInputStream(f);
			   
			   Scanner sc = new Scanner(inputStream, "UTF-8");
			   sc.nextLine();
			   while (sc.hasNextLine()) {
				   count++;
				   	String line = sc.nextLine();
				   	System.out.println(count+"  "+line);
			       	String[] values = line.split("\\|");
			       	
			       	String bIDList ="";
			       	if(values.length>=2) {
			       		Analyzer analyzer = new StandardAnalyzer();
			       		String queryString = values[2];
			       		System.out.println("query: "+queryString);
			    		EasySearch easySearch = new EasySearch();
			    		
			    		IndexReader reader = DirectoryReader.open(
			    				FSDirectory.open(Paths.get("E:\\MS\\SEM2\\Search\\Project\\StandardAnalyzer")));
			    		IndexSearcher searcher = new IndexSearcher(reader);
			    		
			    		QueryParser parser = new QueryParser("reviews", analyzer);
			    		HashMap<String, Double> map = new HashMap<>();
			    		Query query = parser.parse(queryString);
			    		Set<Term> queryTerms = new LinkedHashSet<Term>();
			    		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
			    		
			    		for (Term t : queryTerms) {
			    			map = easySearch.getTDFScoreForReviews(t.text(), map);
			    		}
			    		
			    		HashMap<String, Double> sortedMap = easySearch.sortByValue(map);
			    		
			    		int rank = 1;
						for (Map.Entry<String, Double> e : sortedMap.entrySet()) {
							out.write(count + " Q0 " + e.getKey() + " " + rank++ + " " + e.getValue() + " short-query");
							out.newLine();
							
						}	
			       	}
			       	
			   }
			   out.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
		
	 
	
	
	public HashMap<String, Double> getTDFScoreForReviews(String queryString, HashMap<String, Double> map) throws IOException, ParseException {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("E:\\MS\\SEM2\\Search\\Project\\StandardAnalyzer")));
		IndexSearcher searcher = new IndexSearcher(reader);

		
		
		int kt =reader.docFreq(new Term("reviews", queryString));
		int N  = reader.maxDoc();
		Double idf = 0.0;
		if(kt !=0)
			idf = Math.log(1+(N/kt));
		
		
		/**
		 * Get document length and term frequency
		 */

		ClassicSimilarity dSimi = new ClassicSimilarity();
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		
		for (int i = 0; i < leafContexts.size(); i++) {
			// Get document length
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			
			PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
					"reviews", new BytesRef(queryString));
			
			int doc;
			if (de != null) {
				while (((doc = de.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS)) {
					String docName = searcher.doc(de.docID() + startDocNo).get("businessId");
					float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
							.getNormValues("reviews").get(de.docID()));
					float docLeng = 1 / (normDocLeng * normDocLeng);
					Double tf = (double) (de.freq()/docLeng);
					
					map.put(docName, map.getOrDefault(docName, 0.0)+tf*idf);
				}
			}
		}
		return map;
		
	}
	
	
	
	// Referred geeksforgeeks for comparing values in hashtable
	public HashMap<String, Double> sortByValue(HashMap<String, Double> map) 
    {
        List<Map.Entry<String, Double> > list = 
               new LinkedList<Map.Entry<String, Double> >(map.entrySet()); 
  
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() { 
            @Override
			public int compare(Map.Entry<String, Double> o1,  
                               Map.Entry<String, Double> o2) 
            { 
            	if(o2.getValue()!=o1.getValue())
            		return (o2.getValue()).compareTo(o1.getValue());
            	else
            		return (o2.getKey()).compareTo(o1.getKey());
            } 
        }); 
          
        
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>(); 
        for (Map.Entry<String, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 
	
}
