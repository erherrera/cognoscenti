package org.socialbiz.cog.util;

import java.io.PrintWriter;
import java.io.Writer;

public class UnclosableWriter extends PrintWriter {
    public Writer wrapped;

    public UnclosableWriter(Writer w) {
        super(w);
        wrapped = w;
    }

    public void close() {
        throw new RuntimeException("something tried to close the writer here");
    }
}