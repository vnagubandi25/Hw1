import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import java.io.*;
import java.util.ArrayList;

public class tfidfQuery {

    public  ArrayList<queryTriplet> queries;

    public Directory index;
    public Analyzer analyzer;

    public PrintStream stream;

    public Similarity sim;

    public tfidfQuery(ArrayList queries, Directory index, Analyzer analyzer, Similarity similarity) throws FileNotFoundException {
        this.queries= queries;
        this.index = index;
        this.analyzer = analyzer;
        this.sim = similarity;
    }


    private void doQuery(queryTriplet query1) throws IOException, ParseException {

//        IndexReader reader = DirectoryReader.open(this.index);
//        IndexSearcher searcher = new IndexSearcher(reader);
//        searcher.setSimilarity(this.sim);

//      Execute a query using the custom Similarity
        Query query =  new QueryParser(".W", this.analyzer).parse((query1.title + " " + query1.description ).toLowerCase().replace("/"," ")); // Create a Query that represents your search criteria

        int hitsPerPage = 50;
        IndexReader reader = DirectoryReader.open(this.index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(this.sim);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(query1.number + "\t" + "0" + "\t" + d.get(".U") + "\t" + (i + 1)  + "\t" +  hits[i].score + "\t" + "TFIDF");
        }
        reader.close();
    }

    public void getResultFile(String filename){
        try {
            String file = "C:\\Users\\venk2\\Desktop\\CSE_272\\hw1_search\\results\\" + filename +".txt";
            File resultFile = new File(file);
            this.stream = new PrintStream(resultFile);
            System.setOut(stream);

            for(queryTriplet q: this.queries){
                doQuery(q);
            }
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        } catch (IOException e) {
        } catch (ParseException e) {
        }

    }

}