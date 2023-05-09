import org.apache.lucene.util.BytesRef;

public interface tfidfsim1 {
    float sloppyFreq(int distance);

    float scorePayload(int doc, int start, int end, BytesRef payload);
}
