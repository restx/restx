package restx.specs.mongo;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import restx.specs.RestxSpec;

import static restx.common.MoreStrings.reindent;

/**
* User: xavierhanin
* Date: 3/30/13
* Time: 5:54 PM
*/
public class GivenJongoCollection implements RestxSpec.Given {
    public static final String DB_URI = "GivenCollection.DB_URI";

    private final String collection;
    private final String path;
    private final String data;
    private final ImmutableList<String> sequence;

    public GivenJongoCollection(String collection, String path, String data, ImmutableList<String> sequence) {
        this.collection = collection;
        this.path = path;
        this.data = data;
        this.sequence = sequence;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append("  - collection: ").append(collection).append("\n");
        if (!Strings.isNullOrEmpty(path) && !path.equals("data://")) {
            sb.append("    path: ").append(path).append("\n");
        }
        if (!data.isEmpty()) {
            sb.append("    data: |\n").append(reindent(data.trim(), 8)).append("\n");
        }
        if (!sequence.isEmpty()) {
                sb.append("    sequence: ");
                Joiner.on(", ").appendTo(sb, sequence);
                sb.append("\n");
        }
    }


    public String getCollection() {
        return collection;
    }

    public String getPath() {
        return path;
    }

    public String getData() {
        return data;
    }

    public ImmutableList<String> getSequence() {
        return sequence;
    }

    public GivenJongoCollection addSequenceId(String id) {
        return new GivenJongoCollection(collection, path, data,
                ImmutableList.<String>builder().addAll(sequence).add(id).build());
    }
}
