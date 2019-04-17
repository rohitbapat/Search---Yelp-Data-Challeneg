package task1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

public class GenerateIndex {

	
	public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
		ArrayList<HashMap<String, String>> documents = new ArrayList<HashMap<String, String>>();
		String analyzerType = "StandardAnalyzer";		// Set Analyzer type
		
		Analyzer analyzer = new StandardAnalyzer();;
		String indexPath = "C:\\Users\\harsh\\Desktop\\IUB\\Search\\Project\\index"+ analyzerType;
		
		File f = new File("C:\\Users\\harsh\\Desktop\\IUB\\Search\\Project\\Combined_Data.csv");
		Directory dir = FSDirectory.open(Paths.get(indexPath));
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);
		
		
		try {
		   FileInputStream inputStream = new FileInputStream(f);
		   Scanner sc = new Scanner(inputStream, "UTF-8");
		  
		   while (sc.hasNextLine()) {
			   	String line = sc.nextLine();
			   	
		       	String[] values = line.split("\\|");
		       	if(values.length>4) {

				   	HashMap<String, String> document = new HashMap<>();
		       		document.put("businessId", values[1]);
		       		document.put("categories", values[3].replace("[", "").replace("]", "").replace(",", " "));
		       		document.put("reviews", values[2]);
		       		document.put("tips", values[4]);
		       		indexDoc(writer, document);
		       	}
		   }
		   writer.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("count "+documents.size());
		System.out.println("businessReview complete");
		
		/*
		String analyzerType = "StandardAnalyzer";		// Set Analyzer type
		
		Analyzer analyzer = null;
		String indexPath = "C:\\Users\\harsh\\Desktop\\IUB\\Search\\project\\"+ analyzerType;
		switch (analyzerType) {

			case "StandardAnalyzer":
				analyzer = new StandardAnalyzer();
				break;
	
			case "KeywordAnalyzer":
				analyzer = new KeywordAnalyzer();
				break;
	
			case "SimpleAnalyzer":
				analyzer = new SimpleAnalyzer();
				break;
	
			case "StopAnalyzer":
				analyzer = new StopAnalyzer();
				break;

		}		
		
		createIndex(analyzer, indexPath, documents);	
*/
	}
	
	public static void createIndex(Analyzer analyzer, String indexPath, ArrayList<HashMap<String, String>> documents) {
		try {
			
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			iwc.setOpenMode(OpenMode.CREATE);

			IndexWriter writer = new IndexWriter(dir, iwc);

			for (HashMap<String, String> doc1 : documents) {
				indexDoc(writer, doc1);
			}

			writer.close();
			System.out.println("Done ...");
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
		
	}
	
	
	/** Indexes a single document 
	 * @throws IOException */
	public static void indexDoc(IndexWriter writer, HashMap<String, String> document) throws IOException {
		// make a new, empty document
		org.apache.lucene.document.Document lDoc = new org.apache.lucene.document.Document();
		System.out.println(document.get("businessId"));
		lDoc.add(new StringField("businessId", document.get("businessId"),Field.Store.YES));
		lDoc.add(new TextField("categories", document.get("categories"), Field.Store.YES));
		lDoc.add(new TextField("reviews", document.get("reviews"), Field.Store.YES));
		lDoc.add(new TextField("tips", document.get("tips"), Field.Store.YES));
		writer.addDocument(lDoc);
	}
}
