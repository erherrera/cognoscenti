package org.socialbiz.cog;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class NGFilter implements Filter {

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        try{
            //always set the encoding to UTF-8 in filter, as early as possible to avoid well known J2EE bug
            request.setCharacterEncoding("UTF-8");
            chain.doFilter(request, response);
        }finally{
            NGPageIndex.clearLocksHeldByThisThread();
        }

    }

    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

}
