package restx.json.testing.twitter;

import java.util.List;

// a simple entity to be mapped to json, most of the code borrowed from jackson jsm-json-benchmark,
// which is Apache Licensed

public class TwitterSearch {
    protected List<TwitterEntry> results;

    public long since_id, max_id;
    public int page;
    public int results_per_page;
    public String query;
    public String refresh_url;
    public String next_page;
    public double completed_in;
    
    public TwitterSearch() { }

    public List<TwitterEntry> getResults() {
        return results;
    }

    public void setResults(List<TwitterEntry> r) { results = r; }
    
    public int size() { return getResults().size(); }

    // // // Setters for whoever needs them
    
    public void setSince_id(long l) { since_id = l; }
    public void setMax_id(long l) { max_id = l; }

    public void setPage(int i) { page = i; }
    public void setResults_per_page(int i) { results_per_page = i; }
    
    public void setQuery(String s) { query = s; }
    public void setRefresh_url(String s) { refresh_url = s; }
    public void setNext_page(String s) { next_page = s; }

    public void setCompleted_in(double d) { completed_in = d; }

    // // // ditto for getters

    public long getSince_id() { return since_id; }
    public long getMax_id() { return max_id; }

    public int getPage() { return page; }
    public int getResults_per_page() { return results_per_page; }

    public String getQuery() { return query; }
    public String getRefresh_url() { return refresh_url; }
    public String getNext_page() { return next_page; }

    public double getCompleted_in() { return completed_in; }
    
    /*
    /**********************************************************************
    /* Std methods
    /**********************************************************************
     */

    @Override
    public String toString() {
        return "TwitterSearch{" +
                "results=" + results +
                ", since_id=" + since_id +
                ", max_id=" + max_id +
                ", page=" + page +
                ", results_per_page=" + results_per_page +
                ", query='" + query + '\'' +
                ", refresh_url='" + refresh_url + '\'' +
                ", next_page='" + next_page + '\'' +
                ", completed_in=" + completed_in +
                '}';
    }
}

