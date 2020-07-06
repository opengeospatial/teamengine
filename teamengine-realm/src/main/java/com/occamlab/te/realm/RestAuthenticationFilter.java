package com.occamlab.te.realm;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestAuthenticationFilter implements Filter {
    private static final String AUTHENTICATION_HEADER = "Authorization";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filter) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String authCredentials = httpServletRequest.getHeader(AUTHENTICATION_HEADER);
            
            if (null != authCredentials) {
                final String encodedUserPassword = authCredentials.replaceFirst("Basic" + " ", "");
                String usernameAndPassword = null;
                
                try {
                    byte[] decodedBytes = Base64.getDecoder()
                            .decode(encodedUserPassword);
                    usernameAndPassword = new String(decodedBytes, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                final String username = tokenizer.nextToken();
                final String password = tokenizer.nextToken();
                String root = System.getProperty("TE_BASE") + "/users";
                
                PBKDF2Realm pbkdf2Realm = new PBKDF2Realm();
                pbkdf2Realm.setRoot(root);
                Principal principal = pbkdf2Realm.authenticate(username, password);
                
                if (null != principal) {
                    filter.doFilter(request, response);
                } else {
                    if (response instanceof HttpServletResponse) {
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"Insert credentials\"");
                        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            } else {
                if (response instanceof HttpServletResponse) {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"Insert credentials\"");
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
