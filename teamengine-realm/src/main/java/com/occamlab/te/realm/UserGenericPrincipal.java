package com.occamlab.te.realm;

import java.security.Principal;
import java.util.HashMap;
import java.util.logging.Logger;

public class UserGenericPrincipal {

  private static final Logger logger = Logger
      .getLogger(UserGenericPrincipal.class.getPackage().getName());

  private HashMap<String, Principal> principals = new HashMap<String, Principal>();

  private static volatile UserGenericPrincipal userPrincipal = null;

  public static UserGenericPrincipal getInstance() {

    if (null == userPrincipal) {
      synchronized (UserGenericPrincipal.class) {
        // check again, because the thread might have been preempted
        // just after the outer if was processed but before the
        // synchronized statement was executed
        if (userPrincipal == null) {
          userPrincipal = new UserGenericPrincipal();
        }
      }
    }
    return userPrincipal;
  }
  
  public Principal removePrincipal(String username) {

    synchronized (principals) {
      return (Principal) principals.remove(username);
    }

  }

  public HashMap<String, Principal> getPrincipals() {
    return principals;
  }
  
}
