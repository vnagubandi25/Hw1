import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class queryTriplet {
    public String number;
    public String title;
    public String description;
    public Analyzer analyzer;

    public queryTriplet(String number, String title, String description, Analyzer analyzer) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.analyzer = analyzer;
    }


    public ArrayList getWords(String phrase) throws IOException {
        ArrayList<String> words = new ArrayList<String>();

        // Test the tokenizer

        String testText = phrase;
        TokenStream ts = this.analyzer.tokenStream("context", new StringReader(testText));
        OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
        try {
            ts.reset(); // Resets this stream to the beginning. (Required)
            while (ts.incrementToken()) {
                String word = testText.substring(offsetAtt.startOffset(), offsetAtt.endOffset());
                words.add(word);
            }
            ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ts.close(); // Release resources associated with this stream.
        }
        return words;
    }

    public  ArrayList titleWords() throws IOException {
        return getWords(this.title);
    }

    public  ArrayList descriptionWords() throws IOException {
        return getWords(this.description);
    }

    public ArrayList title_descriptionWords() throws IOException {
        ArrayList<String> twords = getWords(this.title);
        ArrayList<String> dwords = getWords(this.description);
        twords.addAll(dwords);

        return twords;
    }

//    public String getTitle(){
//        return this.title;
//    }
//
//    public String getNumber(){
//        return this.number;
//    }
//
//    public String getDescription(){
//        return this.description;
//    }

    @Override
    public String toString() {
        return "( " + this.number + ", " + this.title+ ", " + this.description + " )";
    }
}
