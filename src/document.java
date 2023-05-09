import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class document {
    public String I;
    public String U;
    public String S;
    public String M;
    public String T;
    public String P;
    public String W;
    public String A;

    public ArrayList<String> filtered = new ArrayList<>();

    List<Map.Entry<String, Integer>> entries;

    Analyzer analyzer;

    public document(String I, String U, String S, String M, String T, String P, String W, String A, Analyzer analyze){
        this.I = I;
        this.U = U;
        this.S = S;
        this.M = M;
        this.T = T;
        this.P = P;
        this.W = W;
        this.A = A;

        if (W!=""){
            try {
//                System.out.println(W);

                this.analyzer = analyze;
                TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(W));
                tokenStream.reset();

                while(tokenStream.incrementToken()) {
                    filtered.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
                }
                tokenStream.close();
            } catch (IOException e) {
            }

            Map<String, Integer> map = new HashMap<>();
            for (String s : filtered){
                map.put(s, map.getOrDefault(s, 0) + 1);
            }

            this.entries = new ArrayList<>(map.entrySet());
            this.entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        }
    }

    public ArrayList topN(int n){

        ArrayList<String> topN = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            topN.add(this.entries.get(i).getKey());
        }

        return topN;
    }
}




