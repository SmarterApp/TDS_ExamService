package tds.score.model;

public class Item {
    private final String stimulusPath;
    private final String itemPath;

    public Item(final String stimulusPath, final String itemPath) {
        this.stimulusPath = stimulusPath;
        this.itemPath = itemPath;
    }

    public String getStimulusPath() {
        return stimulusPath;
    }

    public String getItemPath() {
        return itemPath;
    }
}
