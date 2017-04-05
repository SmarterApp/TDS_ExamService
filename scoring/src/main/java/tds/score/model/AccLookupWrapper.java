package tds.score.model;

import org.apache.commons.lang.builder.HashCodeBuilder;

import tds.itemrenderer.data.AccLookup;

public class AccLookupWrapper {
    private AccLookup accLookup;

    public AccLookupWrapper(AccLookup accLookup) {
        this.accLookup = accLookup;
    }

    public AccLookup getValue() {
        return this.accLookup;
    }

    @Override
    public int hashCode() {
        if (this.accLookup == null)
            return 0;

        return new HashCodeBuilder(13, 41)
            .appendSuper(this.accLookup.getTypes().hashCode())
            .append(this.accLookup.getPosition())
            .append(this.accLookup.getId())
            .toHashCode();
    }
}
