package org.socialbiz.cog.dms;

public class LocalFolderConfig {
    private String displayName;
    private String path;

    public LocalFolderConfig(String displayName, String path){
        this.displayName = displayName;
        this.path = path;
    }
    public String getDisplayName(){
        return displayName;
    }

    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }

    public String getPath(){
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }


}
