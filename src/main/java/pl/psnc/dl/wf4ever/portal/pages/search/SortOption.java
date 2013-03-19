package pl.psnc.dl.wf4ever.portal.pages.search;

import org.purl.wf4ever.rosrs.client.search.SearchServer;
import org.purl.wf4ever.rosrs.client.search.SearchServer.SortOrder;
import org.purl.wf4ever.rosrs.client.search.dataclasses.solr.FacetEntry;

public class SortOption {

    private final FacetEntry facetEntry;

    private final SearchServer.SortOrder sortOrder;


    public SortOption(FacetEntry facetEntry, SortOrder sortOrder) {
        this.facetEntry = facetEntry;
        this.sortOrder = sortOrder;
    }


    public FacetEntry getFacetEntry() {
        return facetEntry;
    }


    public SearchServer.SortOrder getSortOrder() {
        return sortOrder;
    }


    /**
     * The key for the drop down list.
     * 
     * @return the facet name and the sort order
     */
    public String getKey() {
        return facetEntry.getFieldName() + "_" + sortOrder.toString();
    }


    /**
     * The name on the drop down list option.
     * 
     * @return the facet name and the sort order
     */
    public String getValue() {
        return facetEntry.getName() + (sortOrder == SortOrder.ASC ? " ascending" : " descending");
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getKey() == null) ? 0 : getKey().hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SortOption other = (SortOption) obj;
        if (getKey() == null) {
            if (other.getKey() != null) {
                return false;
            }
        } else if (!getKey().equals(other.getKey())) {
            return false;
        }
        return true;
    }
}
