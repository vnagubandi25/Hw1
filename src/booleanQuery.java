import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class booleanQuery {

    public  ArrayList<queryTriplet> queries;
    public Directory index;
    public Analyzer analyzer;
    public PrintStream stream;

    public booleanQuery(ArrayList queries, Directory index, Analyzer analyzer) {
        this.queries = queries;
        this.index = index;
        this.analyzer = analyzer;
    }

    public void doQuery(queryTriplet query) throws IOException, ParseException {

        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        ArrayList<String> queryWords = null;

        queryWords = query.title_descriptionWords();

        for (int i = 0; i < queryWords.size(); i++) {
            String element = queryWords.get(i);
            Query q = null;
            q = new QueryParser(".W", this.analyzer).parse(element.toLowerCase());
            queryBuilder.add(q, BooleanClause.Occur.SHOULD);
        }

        BooleanQuery bquery= queryBuilder.build();

        // 3. search
         int hitsPerPage = 50;
        IndexReader reader = null;
        reader = DirectoryReader.open(this.index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = null;
        docs = searcher.search(bquery, hitsPerPage);

        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = null;
            try {
                d = searcher.doc(docId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(query.number + "\t" + "0" + "\t" + d.get(".U") + "\t" + (i + 1)  + "\t" +  hits[i].score + "\t" + "Boolean");
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
            System.out.println(e);
        } catch (ParseException e) {
            System.out.println(e);
        }

    }

}
