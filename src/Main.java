import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;


import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;



public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        //creates the results directory if it doesn't already exist
        File resultsDirectory = new File("results");
        if (!resultsDirectory.exists()){
            resultsDirectory.mkdir();
        }

        //creates the index directory if it doesn't already exist
        File indexDirectory = new File("results");
        if (!indexDirectory.exists()){
            indexDirectory.mkdir();
        }

        //variable to store documents parsed using the standard analyzer
        ArrayList<document> standardreldocs = new ArrayList<document>();

        //creates an instance of the standard analyzer
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();

        //reads the query document
        long readingQuerytime = System.currentTimeMillis();
        ArrayList<queryTriplet> queries = getQueries(standardAnalyzer);

        long readingQuerytime1 = System.currentTimeMillis();
        System.out.println("Time to read all the queries using the standardAnalyzer queries in milliseconds is " + (readingQuerytime1 - readingQuerytime));

        //creates the standard index stores it in the indexes folder and names it BM25 also populates the standardreldocs
        long indexCreatoranddoctime = System.currentTimeMillis();
        standard_index_creator1("BM25",standardAnalyzer,standardreldocs);
        long indexCreatoranddoctime1 = System.currentTimeMillis();
        System.out.println("Time to create a standardIndex using the standardAnalyzer and to create an arraylist of documents takes in milliseconds is  " + (indexCreatoranddoctime1 - indexCreatoranddoctime));

        //read the standard index that was just created
        Directory standard_index = NIOFSDirectory.open(Paths.get("Indexes\\BM25"));

        //runs the boolean query
        long booleantime = System.currentTimeMillis();
        booleanQuery bquery = new booleanQuery(queries,standard_index,standardAnalyzer);
        bquery.getResultFile("standardBooleanResults");
        long booleantime1 = System.currentTimeMillis();
        System.out.println("Time it takes to do all the booleanQueries using the standard analyzer and create a result file in milliseconds is " + (booleantime1 - booleantime));

        //create the tf index
        long tfindexCreatoranddoctime = System.currentTimeMillis();
        Similarity tfSim = new tfidfsim(true);
        custom_index_creator("tf",tfSim,standardAnalyzer);
        long tfindexCreatoranddoctime1 = System.currentTimeMillis();
        System.out.println("Time to create a standard tf index using the standard analyzer and the tf similarity measure in milliseconds is " + (tfindexCreatoranddoctime1 - tfindexCreatoranddoctime));

        //run the tf query
        long tftime = System.currentTimeMillis();
        Directory tf_index = NIOFSDirectory.open(Paths.get("Indexes\\tf"));
        tfidfQuery tfquery = new tfidfQuery(queries,tf_index,standardAnalyzer,tfSim);
        tfquery.getResultFile("standardtfresults");
        long tftime1 = System.currentTimeMillis();
        System.out.println("Time it takes to do all the tfQueries using the standard analyzer and the tf similarity measure and create a result file in milliseconds is " + (tftime1 - tftime));

        //create the tfidf index
        long tfidfindexCreatoranddoctime = System.currentTimeMillis();
        Similarity tfidfSim = new tfidfsim(false);
        custom_index_creator("tfidf",tfidfSim,standardAnalyzer);
        long tfidfindexCreatoranddoctime1 = System.currentTimeMillis();
        System.out.println("Time to create a standard tfidf index using the standard analyzer and the tfidf similarity measure in milliseconds is " + (tfidfindexCreatoranddoctime1 - tfidfindexCreatoranddoctime));

        //run the tfidf query
        long tfidftime = System.currentTimeMillis();
        Directory tfidf_index = NIOFSDirectory.open(Paths.get("Indexes\\tfidf"));
        tfidfQuery tfidfquery = new tfidfQuery(queries,tfidf_index,standardAnalyzer,tfidfSim);
        tfquery.getResultFile("standardtfidfresults");
        long tfidftime1 = System.currentTimeMillis();
        System.out.println("Time it takes to do all the tfidfQueries using the standard analyzer and the tfidf similarity measure and create a result file in milliseconds is " + (tfidftime1 - tfidftime));

        //creates a map where the string is the queryNumber and the arralist is the documents that are relevant to it
        long relevanceTime = System.currentTimeMillis();
        Map<String,ArrayList<document>> standardrelevance = setRelevant(standardreldocs,standardAnalyzer);
        long relevanceTime1 = System.currentTimeMillis();
        System.out.println("Time it takes to create the mapping of the querynumber to the relevant documents using the standard analyzer in milliseconds is " + (tfidftime1 - tfidftime));

        //runs the psudeorelevance query with the top 5 relevant terms from each document
        long pquerytime = System.currentTimeMillis();
        psdrQuery pquery = new psdrQuery(queries,standard_index,standardAnalyzer,standardrelevance);
        pquery.getResultFile("StandardPqueryResults",5);
        long pquerytime1 = System.currentTimeMillis();
        System.out.println("Time it takes to do the PsudeoRelavance Query using the standard analyzer and create a results file in milliseconds is " + (pquerytime1 - pquerytime));


        //creating a customIndex based on the customAnalyzer
        List<String> stopWords = Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"); //Filters both words
        CharArraySet stopSet = new CharArraySet(stopWords, true);
        Analyzer custom_analyzer = new customAnalyzer(stopSet);

        //will create an array list of queries based on the custom Analyzer
        long customQueriestime = System.currentTimeMillis();
        ArrayList<queryTriplet> customQueries = getQueries(custom_analyzer);
        long customQueriestime1 = System.currentTimeMillis();
        System.out.println("Time it takes read queries with customAnalyzer " + (customQueriestime1 - customQueriestime));

//        variable to store documents parsed using the custom analyzer
        ArrayList<document> customreldocs = new ArrayList<document>();

        //create a custom index and call it custom
        long customindextime = System.currentTimeMillis();
        custom_index_creator1("Custom", new BM25Similarity(), custom_analyzer, customreldocs);
        long customindextime1 = System.currentTimeMillis();
        System.out.println("Time it takes to create a customIndex" + (customindextime1  - customindextime));

        Directory customindex = NIOFSDirectory.open(Paths.get("Indexes\\Custom"));

        //creates a map where the string is the queryNumber and the arralist is the documents that are relevant to it
        long customrelevanceset = System.currentTimeMillis();
        Map<String,ArrayList<document>> customrelevance = setRelevant(customreldocs, custom_analyzer);
        long customrelevanceset1 = System.currentTimeMillis();
        System.out.println("Time it takes to create set customRelevance" + (customrelevanceset1  - customrelevanceset));

        //running my custom Algorithm
        long cquerytime = System.currentTimeMillis();
        MyOwnQuery customq = new MyOwnQuery(customQueries,customindex,custom_analyzer,customrelevance,1.0f,1.0f,1.0f );
        customq.getResultFile("CustomResults",5);
        long cquerytime1 = System.currentTimeMillis();
        System.out.println("Time it takes to do customQuery and write a results file" + (cquerytime1  - cquerytime));



    }

    public static void setStandardStream(){
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }

    public static void addDoc(IndexWriter w, String i, String u, String s, String m, String t, String p, String w1, String a ) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(".I", i, Field.Store.YES));
        doc.add(new StringField(".U", u, Field.Store.YES));
        doc.add(new StringField(".S", s, Field.Store.YES));
        doc.add(new StringField(".M", m, Field.Store.YES));
        doc.add(new TextField(".T", t, Field.Store.YES));
        doc.add(new StringField(".P", p, Field.Store.YES));
        doc.add(new TextField(".W", w1, Field.Store.YES));
        doc.add(new StringField(".A", a, Field.Store.YES));

        w.addDocument(doc);
//        System.out.println(doc);
    }

    public static void docAdder(IndexWriter w, Analyzer analyzer, ArrayList documents) {
        try {
            File file = new File("C:\\Users\\venk2\\Desktop\\CSE_272\\hw1_search\\src\\ohsumed.88-91");
            Scanner sc = new Scanner(file);

            Pattern ipattern = Pattern.compile("(.I )([0-9]{5})");
            Pattern upattern = Pattern.compile("^.U$");
            Pattern spattern = Pattern.compile("^.S$");
            Pattern mpattern = Pattern.compile("^.M$");
            Pattern tpattern = Pattern.compile("^.T$");
            Pattern ppattern = Pattern.compile("^.P$");
            Pattern wpattern = Pattern.compile("^.W$");
            Pattern apattern = Pattern.compile("^.A$");

            boolean gotSkipped = false;
            boolean notDone = true;
            int currentLn = 0;

            ArrayList<String> lines = new ArrayList<String>();

            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }

            int numLns = lines.size();
            int docsadded = 0;

            while(notDone){

                if (lines.get(currentLn).isEmpty()){
                    notDone = false;
                }

                String data = lines.get(currentLn);

                String I = "";
                String U = "";
                String S = "";
                String M = "";
                String T = "";
                String P = "";
                String W = "";
                String A = "";

                Matcher imatcher = ipattern.matcher(data);

                if (imatcher.find()){
                    I = imatcher.group(2);
                    currentLn = currentLn +1;
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher umatcher = upattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));

                        docsadded = docsadded +1;
                        currentLn = currentLn-1;
                        continue;
                    } else if (umatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        U = (data1);

                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher smatcher = spattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));
                        currentLn = currentLn-1;
                        continue;
                    } else if (smatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        S = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher mmatcher = mpattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));
                        currentLn = currentLn-1;
                        continue;
                    } else if (mmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        M = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher tmatcher = tpattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));
                        currentLn = currentLn-1;
                        continue;
                    } else if (tmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        T = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher pmatcher = ppattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));
                        currentLn = currentLn-1;
                        continue;
                    } else if (pmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        P = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher wmatcher = wpattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));
                        currentLn = currentLn-1;
                        continue;
                    } else if (wmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        W = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher amatcher = apattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));
                        currentLn = currentLn-1;
                        continue;
                    } else if (amatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        A = (data1);
                        addDoc(w,I,U,S,M,T,P,W,A);
                        documents.add(new document(I,U,S,M,T,P,W,A,analyzer));
                    }
                }
                currentLn = currentLn + 1;

            }

        }
        catch (FileNotFoundException ex) {
            int xx = 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void docAdder(IndexWriter w, Analyzer analyzer) {
        try {
            File file = new File("C:\\Users\\venk2\\Desktop\\CSE_272\\hw1_search\\src\\ohsumed.88-91");
            Scanner sc = new Scanner(file);

            Pattern ipattern = Pattern.compile("(.I )([0-9]{5})");
            Pattern upattern = Pattern.compile("^.U$");
            Pattern spattern = Pattern.compile("^.S$");
            Pattern mpattern = Pattern.compile("^.M$");
            Pattern tpattern = Pattern.compile("^.T$");
            Pattern ppattern = Pattern.compile("^.P$");
            Pattern wpattern = Pattern.compile("^.W$");
            Pattern apattern = Pattern.compile("^.A$");

            boolean gotSkipped = false;
            boolean notDone = true;
            int currentLn = 0;

            ArrayList<String> lines = new ArrayList<String>();

            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }

            int numLns = lines.size();
            int docsadded = 0;

            while(notDone){

                if (lines.get(currentLn).isEmpty()){
                    notDone = false;
                }

                String data = lines.get(currentLn);

                String I = "";
                String U = "";
                String S = "";
                String M = "";
                String T = "";
                String P = "";
                String W = "";
                String A = "";

                Matcher imatcher = ipattern.matcher(data);

                if (imatcher.find()){
                    I = imatcher.group(2);
                    currentLn = currentLn +1;
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher umatcher = upattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);

                        docsadded = docsadded +1;
                        currentLn = currentLn-1;
                        continue;
                    } else if (umatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        U = (data1);

                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher smatcher = spattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        currentLn = currentLn-1;
                        continue;
                    } else if (smatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        S = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher mmatcher = mpattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        currentLn = currentLn-1;
                        continue;
                    } else if (mmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        M = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher tmatcher = tpattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        currentLn = currentLn-1;
                        continue;
                    } else if (tmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        T = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher pmatcher = ppattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        currentLn = currentLn-1;
                        continue;
                    } else if (pmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        P = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher wmatcher = wpattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        currentLn = currentLn-1;
                        continue;
                    } else if (wmatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        W = (data1);
                        currentLn = currentLn + 1;
                    }
                }

                if (numLns > currentLn){
                    data = lines.get(currentLn);
                    Matcher amatcher = apattern.matcher(data);
                    boolean unordered = ipattern.matcher(data).find();

                    if (unordered){
                        addDoc(w,I,U,S,M,T,P,W,A);
                        currentLn = currentLn-1;
                        continue;
                    } else if (amatcher.find()) {
                        currentLn = currentLn + 1;
                        String data1 = lines.get(currentLn);
                        A = (data1);
                        addDoc(w,I,U,S,M,T,P,W,A);
                    }
                }
                currentLn = currentLn + 1;

            }

        }
        catch (FileNotFoundException ex) {
            int xx = 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, ArrayList<document>> setRelevant(ArrayList<document> docs, Analyzer analyze){

        Map<String, ArrayList<document>> map = new HashMap<>();

        try {
            File file = new File("C:\\Users\\venk2\\Desktop\\CSE_272\\hw1_search\\src\\qrels.ohsu.88-91");
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String str = sc.nextLine();
                if (str.isEmpty()){
                    break;
                }
                String[] arrOfStr = str.split("\t", 3);

                if (arrOfStr[2].equals("2")){

                    document tempdoc = new document("","","","","","","","",analyze);

                    for (document d: docs){
                        if (d.U.equals(arrOfStr[1])){
                            tempdoc = d;
                        }
                    }

                    if (map.containsKey(arrOfStr[0])) {
                        ArrayList<document> temp_reldocs = map.get(arrOfStr[0]);
                        temp_reldocs.add(tempdoc);
                        map.put(arrOfStr[0],temp_reldocs);
                    } else {
                        map.put(arrOfStr[0], new ArrayList<document>(Arrays.asList(tempdoc)));
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    public static ArrayList getQueries(Analyzer analyzer){

        ArrayList<queryTriplet> queries = new ArrayList<queryTriplet>();
        try
        {
            File file = new File("C:\\Users\\venk2\\Desktop\\CSE_272\\hw1_search\\src\\query.ohsu");
            Scanner sc = new Scanner(file);
            String number = null;
            String title = null;
            String description = null;

            while (sc.hasNextLine()) {
                String data = sc.nextLine();

                if (data.contains("<num> Number: ")) {
                    number = data.replace("<num> Number: ","");
                }
                if (data.contains("<title> ")) {
                    title = data.replace("<title> ","");
                }

                Pattern pattern = Pattern.compile("[A-Za-z]");
                Matcher matcher = pattern.matcher(data);
                boolean matchFound = matcher.find();

                if (!data.contains("<") && matchFound ) {
                    description = data;
                }

                if (!(number==null) && !(title==null) && !(description==null)){
                    queryTriplet query = new queryTriplet(number,title,description,analyzer);
                    queries.add(query);
                    number = null;
                    title = null;
                    description=null;
                }

            }
        }
        catch (FileNotFoundException ex) {
//            System.out.println("Is there an error");
        }

        return queries;
    }

    public static void standard_index_creator(String savename, Analyzer analyzer) throws IOException {

        Directory index = new NIOFSDirectory(Paths.get("Indexes/" +savename));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);
        docAdder(w,analyzer);
        w.close();

    }

    //Creates a standard index using the BM25 similarity measure
    public static void standard_index_creator1(String savename, Analyzer analyzer, ArrayList documents) throws IOException {

        Directory index = new NIOFSDirectory(Paths.get("Indexes/" +savename));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);
        docAdder(w,analyzer,documents);
        w.close();

    }

    //Creates a standard index using any similarity measure you want
    public static void custom_index_creator1(String savename,Similarity similarity, Analyzer analyzer, ArrayList documents) throws IOException {
        Directory index = new NIOFSDirectory(Paths.get("Indexes/"+savename));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(similarity);
        IndexWriter w = new IndexWriter(index, config);
        docAdder(w,analyzer,documents);
        w.close();
    }

    public static void custom_index_creator(String savename,Similarity similarity, Analyzer analyzer) throws IOException {
        Directory index = new NIOFSDirectory(Paths.get("Indexes/"+savename));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(similarity);
        IndexWriter w = new IndexWriter(index, config);
        docAdder(w,analyzer);
        w.close();
    }

}
