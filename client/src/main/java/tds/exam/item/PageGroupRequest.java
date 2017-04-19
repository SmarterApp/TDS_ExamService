package tds.exam.item;

public class PageGroupRequest {
    private int lastPage;
    private int lastPosition;
    private boolean isMsb;

    public PageGroupRequest(final int lastPage, final int lastPosition, final boolean isMsb) {
        this.lastPage = lastPage;
        this.lastPosition = lastPosition;
        this.isMsb = isMsb;
    }

    private PageGroupRequest() {
    }

    public int getLastPage() {
        return lastPage;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public boolean isMsb() {
        return isMsb;
    }
}
