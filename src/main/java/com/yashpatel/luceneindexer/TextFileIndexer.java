/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yashpatel.luceneindexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.ArrayList;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
/**
 *
 * @author Yash Patel
 */
public class TextFileIndexer {
    
   // standard analyzer have stop filter and lowercase filter // simpleanalyzer don't
   private static StandardAnalyzer analyzer = new StandardAnalyzer();

  private IndexWriter writer;
  private ArrayList que = new ArrayList();
    /**
    * Constructor
    * @param indexDir the name of the folder in which the index should be created
    * @throws java.io.IOException when exception creating index.
    */
    TextFileIndexer(String indexDir) throws IOException {
        // the boolean true parameter means to create a new index everytime, 
        // potentially overwriting any existing files there.
        FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());


        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        writer = new IndexWriter(dir, config);
    }
  
    public static void main(String[] args) throws  IOException{
        System.out.println("hello world");
        System.out.println("path to target index folder");

        String indexLocation = null;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String s = br.readLine();

        TextFileIndexer indexer = null;
        try {
          indexLocation = s;
          indexer = new TextFileIndexer(s);
        } catch (Exception ex) {
          System.out.println("Cannot create index..." + ex.getMessage());
          System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================
        while (!s.equalsIgnoreCase("q")) {
          try {
            System.out.println("Enter the full path to add into the index (q=quit): (e.g. /home/ron/mydir or c:/Users/ron/mydir)");
            System.out.println("[Acceptable file types: .xml,.html, .txt]");
            s = br.readLine();
            if (s.equalsIgnoreCase("q")) {
              break;
            }

            //try to add file into the index
            indexer.indexFileOrDirectory(s);
            }catch (Exception e) {
            System.out.println("Error indexing " + s + " : " + e.getMessage());
          }
        }

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created    
        //===================================================
        indexer.closeIndex();

        //=========================================================
        // Now search
        //=========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation).toPath()));
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(5);

        s = "";
        while (!s.equalsIgnoreCase("q")) {
          try {
            System.out.println("Enter the search query (q=quit):");
            s = br.readLine();
            if (s.equalsIgnoreCase("q")) {
              break;
            }
            
            // we have used query parser over here
            
            String FIELD_CONTENTS[] ={"Link","Title","Authors","contents"};
            Query q = new MultiFieldQueryParser(FIELD_CONTENTS,analyzer).parse(s);
             
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            //display results
            System.out.println("Found " + hits.length + " hits.");
            for(int i=0;i<hits.length;++i) {
              int docId = hits[i].doc;
              Document d = searcher.doc(docId);
              System.out.println((i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
               System.out.println(hits[i].shardIndex);
            }
          } catch (Exception e) {
            System.out.println("Error searching " + s + " : " + e.getMessage());
          }
        }

  }
  /**
   * Indexes a file or directory
   * @param fileName the name of a text file or a folder we wish to add to the index
   * @throws java.io.IOException when exception
   */
  public void indexFileOrDirectory(String fileName) throws IOException {
    //===================================================
    //gets the list of files in a folder (if user has submitted
    //the name of a folder) or gets a single file name (is user
    //has submitted only the file name) 
    //===================================================
    
    //add file to the que
    addFiles(new File(fileName));
    
    int originalNumDocs = writer.numDocs();
    for (File f : (ArrayList<File>)que){
      FileReader fr = null;
      try {
        Document doc = new Document();

        //===================================================
        // add contents of file
        //===================================================
        fr = new FileReader(f);
        //doc = ReadCSV.read(doc, f, "|");
        doc.add(new TextField("contents", fr));
        doc.add(new StringField("path", f.getPath(), Field.Store.YES));
        doc.add(new StringField("filename", f.getName(), Field.Store.YES));

        writer.addDocument(doc);
        System.out.println("Added: " + f);
      } catch (Exception e) {
        System.out.println("Could not add: " + f);
      } finally {
        fr.close();
      }
    }
    
    int newNumDocs = writer.numDocs();
    System.out.println("");
    System.out.println("************************");
    System.out.println((newNumDocs - originalNumDocs) + " documents added.");
    System.out.println("************************");

    que.clear();
  }

  private void addFiles(File file) {

    if (!file.exists()) {
      System.out.println(file + " does not exist.");
    }
    if (file.isDirectory()) {
        // if directory add all files
      for (File f : file.listFiles()) {
          // recursive function
          addFiles(f);
      }
    } else {
        
      String filename = file.getName().toLowerCase();
      //===================================================
      // Only index text files
      //===================================================
      if (filename.endsWith(".htm") || filename.endsWith(".html") || 
              filename.endsWith(".xml") || filename.endsWith(".txt")) {
        que.add(file);
      } else {
        System.out.println("Skipped " + filename);
      }
    }
  }

  /**
   * Close the index.
   * @throws java.io.IOException when exception closing
   */
  public void closeIndex() throws IOException {
    writer.close();
  }
}
