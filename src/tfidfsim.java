import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class tfidfsim extends TFIDFSimilarity implements tfidfsim1 {

    public Boolean if_tf;

    public tfidfsim(Boolean if_tf){
        this.if_tf = if_tf;
    }

    @Override
    public float tf(float freq) {
        // Implement the term frequency part of the TF-IDF formula here
        return (float) Math.sqrt(freq);
    }

    @Override
    public float idf(long docFreq, long numDocs) {
        if (if_tf){
            return 1;
        }
        // Implement the inverse document frequency part of the TF-IDF formula here
        return (float) Math.log((numDocs + 1) / (double) (docFreq + 1)) + 1.0f;
    }

    @Override
    public float lengthNorm(int i) {
        return 1;
    }

    @Override
    public float sloppyFreq(int distance) {
        return 1.0f / (distance + 1);
    }

    @Override
    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        return 1;
    }
}