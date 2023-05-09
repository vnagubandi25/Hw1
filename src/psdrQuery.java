import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class psdrQuery {
    public ArrayList<queryTriplet> queries;

    //    public queryTriplet query;
    public Directory index;
    public Analyzer analyzer;

    public Map map;

    public document docs;

    public PrintStream stream;


    public psdrQuery(ArrayList queries, Directory index, Analyzer analyze, Map map) throws FileNotFoundException {
        this.queries = queries;
        this.index = index;
        this.analyzer = analyze;
        this.map = map;
    }

    public void doQuery(queryTriplet query,int topN) throws IOException, ParseException {

        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        ArrayList<String> queryWords = query.title_descriptionWords();
        for (int i = 0; i < queryWords.size(); i++) {
            String element = queryWords.get(i);
            Query q = new QueryParser(".W", this.analyzer).parse(element.toLowerCase());
            queryBuilder.add(q, BooleanClause.Occur.SHOULD);
        }

        if (map.containsKey(query.number)){
            ArrayList<document> reldocs = (ArrayList<document>) this.map.get(query.number);

            for (document d:reldocs){
                if(d.W.isEmpty()){
                    continue;
                }
                ArrayList<String> topNs = d.topN(3);

                for(String element: topNs){
                    Query q = new QueryParser(".W", this.analyzer).parse(element.toLowerCase());
                    queryBuilder.add(q, BooleanClause.Occur.SHOULD);
                }
            }
        }

        BooleanQuery bquery= queryBuilder.build();

        // 3. search
        int hitsPerPage = 50;
        IndexReader reader = DirectoryReader.open(this.index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(bquery, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(query.number + "\t" + "0" + "\t" + d.get(".U") + "\t" + (i + 1)  + "\t" +  hits[i].score + "\t" + "relevance");
        }
//
        // reader can only be closed when there
        // is no need to access the document any more.
        reader.close();
    }


    public void getResultFile(String filename,int topN){
        try {
            String file = "C:\\Users\\venk2\\Desktop\\CSE_272\\hw1_search\\results\\" + filename +".txt";
            File resultFile = new File(file);
            this.stream = new PrintStream(resultFile);
            System.setOut(stream);

            for(queryTriplet q: this.queries){
                doQuery(q,topN);
            }
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        } catch (IOException e) {
        } catch (ParseException e) {
        }

    }

}
