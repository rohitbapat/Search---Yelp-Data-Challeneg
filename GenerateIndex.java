/*
 * Used Standard Analyzer to for indexing the business id on the training data
 * 
 */

package task1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

public class GenerateIndex {

	
	public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
		ArrayList<HashMap<String, String>> documents = new ArrayList<HashMap<String, String>>();
		String analyzerType = "StandardAnalyzer";		// Set Analyzer type
		
		Analyzer analyzer = new StandardAnalyzer();;
		String indexPath = "E:\\MS\\SEM2\\Search\\Project\\"+ analyzerType;
		
		File f = new File("E:\\MS\\SEM2\\Search\\Project\\combinedDF.csv");
		
//		FileWriter writer2 = new FileWriter("C:\\Users\\harsh\\Desktop\\IUB\\Search\\Project\\test.csv");
		Directory dir = FSDirectory.open(Paths.get(indexPath));
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);
		
		try {
		   FileInputStream inputStream = new FileInputStream(f);
		   Scanner sc = new Scanner(inputStream, "UTF-8");
		   sc.nextLine();
		   while (sc.hasNextLine()) {
			   	String line = sc.nextLine();
			   	
		       	String[] values = line.split("\\|");
//		       	business_id|city|reviews_y|new categories|text
		       	if(values.length>3) {

				   	HashMap<String, String> document = new HashMap<>();
		       		document.put("businessId", values[0]);
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
		System.out.println(" count "+documents.size());
		System.out.println("businessReview complete");
		

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