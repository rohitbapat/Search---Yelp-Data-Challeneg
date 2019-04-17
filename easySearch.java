package task1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.apache.lucene.util.packed.PackedLongValues.Iterator;



public class easySearch {

	
	public static void main(String[] args) throws IOException, ParseException {
		
		String queryString_full = "Paneer or Sushi";
		Boolean flag_not = false;
		Boolean flag_or = false;
		Boolean flag_and = false;
		if(queryString_full.contains("not")) {
			flag_not = true;					
		}
		if(queryString_full.contains("or")) {
			flag_or=true;
		}
		if(queryString_full.contains("and")) {
			flag_and=true;
		}
		
		
		easySearch easySearch = new easySearch();
		if(flag_not) {
			String queryStringFirst = queryString_full.substring(0, queryString_full.indexOf("not")).trim();
			String queryStringSecond = queryString_full.substring(queryString_full.indexOf("not")+3, queryString_full.length()).trim();
			HashMap<String, Double> map = new HashMap<>();
			HashMap<String, Double> sortedMap = new HashMap<>();
			if(!queryStringFirst.equals("")) {
				sortedMap = searchQuery(easySearch,map,queryStringFirst,sortedMap,false);
	//			sortedMap = searchQuery(easySearch,map,queryStringSecond,sortedMap);
				int rank = 1;
				System.out.println("Based on Tips and reviews -------------------------");
				for (Map.Entry<String, Double> e : sortedMap.entrySet()) {
	//				System.out.println("here?");
					System.out.println(e.getKey() + " " + rank++ + " " + e.getValue());
					if(rank == 20)
						break;
				}
			}
			else {
				sortedMap = searchQuery(easySearch,map,queryStringSecond,sortedMap,true);
				int rank = 1;
				System.out.println("Based on Tips and reviews -------------------------");
				for (Map.Entry<String, Double> e : sortedMap.entrySet()) {
					System.out.println(e.getKey() + " " + rank++ + " " + e.getValue());
					if(rank == 20)
						break;
				}
			}
		}
		else if(flag_or) {
			HashMap<String, Double> map = new HashMap<>();
			HashMap<String, Double> sortedMap = new HashMap<>();
			sortedMap = searchQuery(easySearch,map,queryString_full,sortedMap,false);
			int rank = 1;
			System.out.println("Based on Tips and reviews -------------------------");
			for (Map.Entry<String, Double> e : sortedMap.entrySet()) {
				System.out.println(e.getKey() + " " + rank++ + " " + e.getValue());
				if(rank == 20)
					break;
			}
		}
	}
	 
	public static HashMap<String, Double> searchQuery(easySearch easySearch, HashMap<String, Double> map, String queryString,HashMap<String, Double> sortedMap, boolean flag_desc) throws IOException, ParseException {
		IndexReader reader = DirectoryReader.open(
				FSDirectory.open(Paths.get("E:\\MS\\SEM2\\Search\\Project\\indexStandardAnalyzer")));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
//		System.out.println("Based on reviews -------------------------");
		QueryParser parser = new QueryParser("reviews", analyzer);
	
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		
		// Get score for each term
		/*for (Term t : queryTerms) {
			System.out.println("For text: "+ t.text());
			map = easySearch.getTDFScoreForReviews(t.text(), new HashMap<>());
			
			HashMap<String, Double> sortedMap = easySearch.sortByValue(map);

			int rank = 1;
			for (Map.Entry<String, Double> e : sortedMap.entrySet()) {
				System.out.println(e.getKey() + " " + rank++ + " " + e.getValue());
				if(rank == 20)
					break;
			}
		}*/

		// For complete query
		
		for (Term t : queryTerms) {
			map = easySearch.getTDFScoreForReviews(t.text(), map);
		}
		
//		HashMap<String, Double> sortedMap = easySearch.sortByValue(map);
//
//		int rank = 1;
//		for (Map.Entry<String, Double> e : sortedMap.entrySet()) {
//			System.out.println(e.getKey() + " " + rank++ + " " + e.getValue());
//			if(rank == 20)
//				break;
//		}
		
		
		System.out.println("****** Calculating please wait *******");
		parser = new QueryParser("tips", analyzer);
		
//		map = new HashMap<>();
		query = parser.parse(queryString);
		queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		
		
		for (Term t : queryTerms) {
			map = easySearch.getTDFScoreForTips(t.text(), map);
		}
		
		sortedMap = easySearch.sortByValue(map);
		if(flag_desc) {
			sortedMap = easySearch.descSortByValue(map);
		}
		return sortedMap;
	}
	
	public HashMap<String, Double> getTDFScoreForReviews(String queryString, HashMap<String, Double> map) throws IOException, ParseException {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("E:\\MS\\SEM2\\Search\\Project\\indexStandardAnalyzer")));
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
				while (((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS)) {
					String docName = searcher.doc(de.docID() + startDocNo).get("categories");
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
	
	
	public HashMap<String, Double> getTDFScoreForTips(String queryString, HashMap<String, Double> map) throws IOException, ParseException {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("E:\\MS\\SEM2\\Search\\Project\\indexStandardAnalyzer")));
		IndexSearcher searcher = new IndexSearcher(reader);

		
		
		int kt =reader.docFreq(new Term("tips", queryString));
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
					"tips", new BytesRef(queryString));
			
			int doc;
			if (de != null) {
				while (((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS)) {
					String docName = searcher.doc(de.docID() + startDocNo).get("categories");
					float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
							.getNormValues("tips").get(de.docID()));
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
	
//	static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
	public HashMap<String, Double> descSortByValue(HashMap<String, Double> map) 
    {
        List<Map.Entry<String, Double> > list = 
                new LinkedList<Map.Entry<String, Double> >(map.entrySet()); 
   
         Collections.sort(list, new Comparator<Map.Entry<String, Double> >() { 
             public int compare(Map.Entry<String, Double> o1,  
                                Map.Entry<String, Double> o2) 
             { 
             	if(o1.getValue()!=o2.getValue())
             		return (o1.getValue()).compareTo(o2.getValue());
             	else
             		return (o1.getKey()).compareTo(o2.getKey());
             } 
         }); 
           
         
         HashMap<String, Double> temp = new LinkedHashMap<String, Double>(); 
         for (Map.Entry<String, Double> aa : list) { 
             temp.put(aa.getKey(), aa.getValue()); 
         } 
         return temp; 
     }
	
}
