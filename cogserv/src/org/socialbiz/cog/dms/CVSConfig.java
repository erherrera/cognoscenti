package org.socialbiz.cog.dms;

public class CVSConfig {
    private String root;
    private String repository;
    private String sandbox;

    public static String ATT_CVS_ROOT = "cvsRoot";
    public static String ATT_CVS_MODULE ="cvsModule";


    public CVSConfig(String root, String repository, String sandbox){
        this.root = root;
        this.repository = repository;
        this.sandbox = sandbox;
    }
    public String getRoot(){
        return root;
    }

    public void setRoot(String root){
        this.root = root;
    }

    public String getSandbox(){
        return sandbox;
    }

    public void setSandbox(String sandbox){
        this.sandbox = sandbox;
    }

    public String getRepository(){
        return repository;
    }

    public void setRepository(String repository){
        this.repository = repository;
    }


}
