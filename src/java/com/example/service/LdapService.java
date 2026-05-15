package com.example.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.HashMap;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LdapService {

    public static class LdapConfig {
        public final String url;
        public final String userDnPattern;
        public final String baseDn;
        public final String searchFilter;
        public final String attributes;

        public LdapConfig(String url, String userDnPattern, String baseDn, String searchFilter, String attributes) {
            this.url = url;
            this.userDnPattern = userDnPattern;
            this.baseDn = baseDn;
            this.searchFilter = searchFilter;
            this.attributes = attributes;
        }
    }

    public boolean authenticate(String username, String password, LdapConfig config) throws NamingException {
        String userDn = config.userDnPattern.replace("{username}", username);
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(buildEnv(config.url, userDn, password));
            return true;
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (Exception ignore) {}
            }
        }
    }

    public HashMap<String, Object> authenticateAndFetchUserData(String username, String password, LdapConfig config)
            throws NamingException {
        String userDn = config.userDnPattern.replace("{username}", username);
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(buildEnv(config.url, userDn, password));
            try {
                return fetchUserData(ctx, username, config);
            } catch (Exception ignore) {
                return new HashMap<>();
            }
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (Exception ignore) {}
            }
        }
    }

    public HashMap<String, Object> fetchUserData(String username, String password, LdapConfig config)
            throws NamingException {
        String userDn = config.userDnPattern.replace("{username}", username);
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(buildEnv(config.url, userDn, password));
            return fetchUserData(ctx, username, config);
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (Exception ignore) {}
            }
        }
    }

    private HashMap<String, Object> fetchUserData(DirContext ctx, String username, LdapConfig config)
            throws NamingException {
        String filter = config.searchFilter.replace("{username}", username);

        String[] attrs = null;
        if (config.attributes != null && !config.attributes.trim().isEmpty()) {
            attrs = Arrays.stream(config.attributes.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        if (attrs != null && attrs.length > 0) {
            controls.setReturningAttributes(attrs);
        }

        NamingEnumeration<SearchResult> results = ctx.search(config.baseDn, filter, controls);
        if (!results.hasMore()) {
            return new HashMap<>();
        }

        SearchResult sr = results.next();
        Attributes attributes = sr.getAttributes();
        HashMap<String, Object> data = new HashMap<>();
        data.put("distinguishedName", sr.getNameInNamespace());

        if (attributes != null) {
            NamingEnumeration<? extends Attribute> all = attributes.getAll();
            while (all.hasMore()) {
                Attribute attr = all.next();
                if (attr == null) {
                    continue;
                }
                String id = attr.getID();
                if (attr.size() == 1) {
                    Object val = attr.get();
                    data.put(id, val != null ? String.valueOf(val) : "");
                } else {
                    ArrayList<String> values = new ArrayList<>();
                    NamingEnumeration<?> vals = attr.getAll();
                    while (vals.hasMore()) {
                        Object v = vals.next();
                        if (v != null) {
                            values.add(String.valueOf(v));
                        }
                    }
                    data.put(id, values);
                }
            }
        }

        return data;
    }

    private Hashtable<String, String> buildEnv(String url, String userDn, String password) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return env;
    }
}
