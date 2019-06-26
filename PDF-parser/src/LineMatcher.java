import java.util.regex.Matcher;

public class LineMatcher {

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getMatchGroup() {
        return group;
    }

    public void setMatchGroup(String matchGroup) {
        this.group = matchGroup;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    private String line;
    private String regexp;
    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    private String group;
    private int frequency;
    private int pageNumber;

    public LineMatcher(String line, String regexp, String group, int frequency, int pageNumber){
        this.line = line;
        this.regexp = regexp;
        this.group = group;
        this.frequency = frequency;
        this.pageNumber = pageNumber;
    }
}
