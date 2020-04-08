/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yashpatel.luceneindexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 *
 * @author Yash Patel
 */
public class ReadCSV {
    public static Document  read(Document doc, File file,String seperator) throws FileNotFoundException, IOException{
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
    String line;
    while ((line = br.readLine()) != null) {
       // process the line.
        // assuming the line is CSV
        String vals[] = line.split("\\"+seperator);
         doc.add(new TextField("Link", vals[0].toLowerCase(),Field.Store.YES));
         doc.add(new StringField("Topic", vals[1].toLowerCase(),Field.Store.YES));
         doc.add(new TextField("Title", vals[2].toLowerCase(),Field.Store.YES));
         doc.add(new TextField("Authors", vals[3].toLowerCase(),Field.Store.YES));
        }
     }//try ends
    
    return doc;
    }
}
