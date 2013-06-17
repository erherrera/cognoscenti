package org.socialbiz.cog;

public class SearchResultRecord
{
    private String bookName = "";
    private String pageName = "";
    private String pageKey = "";
    private String pageLink = "";
    private long lastModifiedTime = System.currentTimeMillis();
    private String lastModifiedBy = "";
    private String NO_DATA = "";
    private String userLink = "";
    private String timePeriod = "";

    //Not sure below two properties should be in this class or need to create another class but for time being added here
    private String noteSubject = "";
    private String noteLink = "";

    public String getNoteLink() {
        return noteLink;
    }
    public void setNoteLink(String noteLink) {
        this.noteLink = noteLink;
    }

    public String getNoteSubject() {
        return noteSubject;
    }
    public void setNoteSubject(String noteSubject) {
        this.noteSubject = noteSubject;
    }
    public String getBookName() {
        return bookName;
    }
    public void setBookName(String value) {
        if (value == null || value.length() == 0) value = NO_DATA;
        bookName = value;
    }

    public String getPageKey() {
        return pageKey;
    }
    public void setPageKey(String value) {
        if (value == null || value.length() == 0) value = NO_DATA;
        pageKey = value;
    }


    public String getPageName() {
        return pageName;
    }
    public void setPageName(String value) {
        if (value == null || value.length() == 0) value = NO_DATA;
        pageName = value;
    }

    public String getPageLink() {
        return pageLink;
    }
    public void setPageLink(String value) {
        if (value == null || value.length() == 0) value = NO_DATA;
        pageLink = value;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }
    public void setLastModifiedTime(long value) {
        lastModifiedTime = value;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
    public void setLastModifiedBy(String value) {
        if (value == null || value.length() == 0) value = NO_DATA;
        lastModifiedBy = value;
    }

    public String getUserLink(){
        return userLink;
    }

    public void setUserLink(String uLink){
        userLink = uLink;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Page Name = ").append(getPageName()).append("\n");
        sb.append("Page Key = ").append(getPageKey()).append("\n");
        sb.append("Book Name = ").append(getBookName()).append("\n");
        sb.append("Page Link = ").append(getPageLink()).append("\n");
        sb.append("Last By").append(getLastModifiedBy()).append("\n");
        sb.append("Last Modified").append(String.valueOf(getLastModifiedTime())).append("\n");
        sb.append("User Link = ").append(getUserLink()).append("\n");
        sb.append("Time Period = ").append(getTimePeriod()).append("\n");
        return sb.toString();
    }
    
    public String getTimePeriod() {
        return timePeriod;
    }
    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }
}