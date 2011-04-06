package com.occamlab.te;

import java.util.Comparator;

public class ConfigComparator implements Comparator<ConfigEntry> {
    
    private String denull(String s) {
        return s == null ? "" : s;
    }

    public int compare(ConfigEntry o1, ConfigEntry o2) {
        int i = o1.organization.compareTo(o2.organization);
        if (i != 0) return i;
        i = o1.standard.compareTo(o2.standard);
        if (i != 0) return i;
        i = o1.version.compareTo(o2.version);
        if (i != 0) return i;
        if (o1.suite != null && o2.suite != null) {
          return denull(o1.revision).compareTo(denull(o2.revision));
        }
        if (o1.suite == null && o2.suite != null) {
            return -1;
        }
        if (o1.suite != null && o2.suite == null) {
            return 1;
        }
        return 0;
    }

}
