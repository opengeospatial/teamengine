package com.occamlab.te.realm;

import java.io.IOException;
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

import org.apache.catalina.realm.GenericPrincipal;

public class RestAuthenticationFilter implements Filter {
    private static final String AUTHENTICATION_HEADER = "Authorization";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filter) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String authCredentials = httpServletRequest.getHeader(AUTHENTICATION_HEADER);
            
            if (null != authCredentials && authCredentials != "" && authCredentials.startsWith("Basic ")) {
                final String encodedUserPassword = authCredentials.replaceFirst("Basic" + " ", "");
                String usernameAndPassword = null;
                
                try {
                    byte[] decodedBytes = Base64.getDecoder()
                            .decode(encodedUserPassword);
                    usernameAndPassword = new String(decodedBytes, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                String username = "";
                String password =  "";
                
                if (null != usernameAndPassword && usernameAndPassword.length() > 1) {
                    final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                    
                    if (tokenizer.countTokens() == 2) {
                        username = tokenizer.nextToken();
                        password = tokenizer.nextToken();
                    } else {
                        unauthorizedException(response);
                    }
                } else {
                    unauthorizedException(response);
                }
                String root = System.getProperty("TE_BASE") + "/users";
                
                PBKDF2Realm pbkdf2Realm = new PBKDF2Realm();
                GenericPrincipal principal = (GenericPrincipal) pbkdf2Realm.authenticate(username, password);
                
                if (null != principal) {
                    if (httpServletRequest.getRequestURI().contains("stats")) {
                        String[] roles = principal.getRoles();
                        for (String role : roles) {
                            if (role.equalsIgnoreCase("admin")) {
                                filter.doFilter(request, response);
                            }
                        }
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    } else {
                        filter.doFilter(request, response);
                    }
                } else {
                    unauthorizedException(response);
                }
            } else {
                unauthorizedException(response);
            }
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
    
    private void unauthorizedException(ServletResponse response) {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"Insert credentials\"");
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    private void serverError(ServletResponse response) {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
