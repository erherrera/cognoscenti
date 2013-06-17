/* UploadRequest.java
 *
 * Support request for file uploader in new gen
 *
 * This is implemented as a hast table of hashtables.  But, the index of the
 * inner hashtable is always an integer.  The inner should be a Vector (or a ArrayList)
 * because that is indexed by number, instead of by a hash object.
 */

package org.socialbiz.cog.util;

import java.util.Enumeration;
import java.util.Hashtable;
import org.socialbiz.cog.exception.NGException;

public class UploadRequest {

    UploadRequest() {
        m_parameters = new Hashtable<String, Hashtable<Integer, String>>();
    }

    protected void putParameter(String name, String value)
            throws Exception {
        if (name == null) {
            throw new NGException("nugen.exception.param.invalid",null);
        }

        if (m_parameters.containsKey(name)) {
            Hashtable<Integer, String> values = m_parameters.get(name);
            values.put(new Integer(values.size()), value);
        } else {
            Hashtable<Integer, String> values = new Hashtable<Integer, String>();
            values.put(new Integer(0), value);
            m_parameters.put(name, values);
        }
    }

    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name does not exist.");
        }
        Hashtable<Integer, String> values = m_parameters.get(name);
        if (values == null) {
            return null;
        }
        else {
            return values.get(new Integer(0));
        }
    }

    public Enumeration<String> getParameterNames() {
        return m_parameters.keys();
    }

    public String[] getParameterValues(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name does not exist.");
        }
        Hashtable<Integer, String> values = m_parameters.get(name);
        if (values == null) {
            return null;
        }
        String strValues[] = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            strValues[i] = values.get(new Integer(i));
        }

        return strValues;
    }

    private Hashtable<String, Hashtable<Integer, String>> m_parameters;
}