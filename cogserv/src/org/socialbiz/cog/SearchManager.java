package org.socialbiz.cog;

import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;


public class SearchManager {

    private static Directory directory = null;
    private static Analyzer analyzer = null;

    private SearchManager() {
        //no instances allowed
    }

    public static synchronized void initializeIndex() throws Exception {
        analyzer = new StandardAnalyzer(Version.LUCENE_42);
        directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzer);
        IndexWriter iWriter = new IndexWriter(directory, config);

        for (NGPageIndex ngpi : NGPageIndex.getAllPageIndex()) {

            if (ngpi.isProject()) {

                NGPage ngp = ngpi.getPage();
                String projectKey = ngp.getKey();
                String projectName = ngp.getFullName();
                String accountName = ngp.getAccount().getFullName();

                for (NoteRecord note : ngp.getAllNotes()) {
                    Document doc = new Document();
                    doc.add(new Field("containerType", "Project", TextField.TYPE_STORED));
                    doc.add(new Field("PAGEKEY", projectKey, TextField.TYPE_STORED));
                    doc.add(new Field("PAGENAME", projectName, TextField.TYPE_STORED));
                    doc.add(new Field("ACCTNAME", accountName, TextField.TYPE_STORED));
                    doc.add(new Field("NOTEID", note.getId(), TextField.TYPE_STORED));
                    doc.add(new Field("NOTESUBJ", note.getSubject(), TextField.TYPE_STORED));
                    doc.add(new Field("LASTMODIFIEDTIME", Long.toString(note.getLastEdited()), TextField.TYPE_STORED));
                    doc.add(new Field("LASTMODIFIEDUSER", note.getLastEditedBy(), TextField.TYPE_STORED));
                    doc.add(new Field("BODY", note.getData(), TextField.TYPE_STORED));
                    iWriter.addDocument(doc);
                }
            }
        }
        iWriter.commit();
        iWriter.close();
    }

    public static synchronized List<SearchResultRecord> performSearch(AuthRequest ar, String queryStr) throws Exception {

        Vector<SearchResultRecord> vec = new Vector<SearchResultRecord>();

        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser(Version.LUCENE_42, "BODY", analyzer);
        Query query = parser.parse(queryStr);
        TopDocs td = isearcher.search(query, null, 1000);
        ScoreDoc[] hits = td.scoreDocs;

        UserProfile up = ar.getUserProfile();
        boolean isLoggedIn = (up!=null);

        for (int i = 0; i < hits.length; i++)
        {
            Document hitDoc = isearcher.doc(hits[i].doc);
            String key = hitDoc.get("PAGEKEY");
            String noteId = hitDoc.get("NOTEID");

            NGPage ngp = NGPageIndex.getProjectByKeyOrFail(key);
            NoteRecord note = ngp.getNoteOrFail(noteId);

            if (note.getVisibility()==SectionDef.PUBLIC_ACCESS) {
                //ok to access public note
            }
            else if (!isLoggedIn) {
                continue;   //don't include this result if not logged in
            }
            else if (ngp.primaryOrSecondaryPermission(up)) {
                //OK no problem, user is a member or admin
            }
            else {
                continue; //no access to non members
            }


            SearchResultRecord sr = new SearchResultRecord();
            sr.setPageName(hitDoc.get("PAGENAME"));
            sr.setPageKey(key);
            sr.setBookName(hitDoc.get("ACCTNAME"));
            sr.setNoteSubject(note.getSubject());
            sr.setPageLink(ar.getResourceURL(ngp, note));
            sr.setTimePeriod("8888");
            sr.setUserLink("bogus user link");
            sr.setLastModifiedTime(DOMFace.safeConvertLong(hitDoc.get("LASTMODIFIEDTIME")));
            sr.setLastModifiedBy(hitDoc.get("LASTMODIFIEDUSER"));
            vec.add(sr);
        }

        ireader.close();
        return vec;
    }

}
