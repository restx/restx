package restx.json;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TxBuffer {
    /**
     * Let's limit maximum segment length to something sensible
     * like 256k
     */
    final static int MAX_SEGMENT_LEN = 0x40000;

    private List<char[]> segments;
    private char[] segment;
    private int pos;
    private int segmentsSize = 0;


    public TxBuffer() {
    }

    public void resetWithBuffer(char[] buffer) {
        this.segment=buffer;
        segments = null;
        pos = 0;
        segmentsSize = 0;
    }

    public void append(char[] buffer, int start, int len) {
        char[] curr = segment;
        int max = curr.length - pos;

        if (max >= len) {
            System.arraycopy(buffer, start, curr, pos, len);
            pos += len;
            return;
        }

        // No room for all, need to copy part(s):
        if (max > 0) {
            System.arraycopy(buffer, start, curr, pos, max);
            start += max;
            len -= max;
        }
        /* And then allocate new segment; we are guaranteed to now
         * have enough room in segment.
         */
        do {
            expand(len);
            int amount = Math.min(segment.length, len);
            System.arraycopy(buffer, start, segment, 0, amount);
            pos += amount;
            start += amount;
            len -= amount;
        } while (len > 0);
    }

    private void expand(int minNewSegmentSize) {
        // First, let's move current segment to segment list:
        if (segments == null) {
            segments = new ArrayList<char[]>();
        }
        char[] curr = segment;
        segments.add(curr);
        segmentsSize += curr.length;
        int oldLen = curr.length;
        // Let's grow segments by 50% minimum
        int sizeAddition = oldLen >> 1;
        if (sizeAddition < minNewSegmentSize) {
            sizeAddition = minNewSegmentSize;
        }
        curr = new char[Math.min(MAX_SEGMENT_LEN, oldLen + sizeAddition)];
        pos = 0;
        segment = curr;
    }

    public String asString() {
        int segLen = segmentsSize;
        int currLen = pos;

        if (segLen == 0) {
            // only one segment
            return (currLen == 0) ? "" : new String(segment, 0, currLen);
        } else { // no, need to combine
            StringBuilder sb = new StringBuilder(segLen + currLen);
            // First stored segments
            if (segments != null) {
                for (int i = 0, len = segments.size(); i < len; ++i) {
                    char[] curr = segments.get(i);
                    sb.append(curr, 0, curr.length);
                }
            }
            // And finally, current segment:
            sb.append(segment, 0, pos);
            return sb.toString();
        }
    }
}
