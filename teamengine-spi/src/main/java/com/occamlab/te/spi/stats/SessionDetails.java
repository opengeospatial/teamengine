package com.occamlab.te.spi.stats;

import java.util.List;

/**
 * This POJO class is used to hold the user details which is retrieved from the
 * session.xml file and used for further operations.
 * 
 * @author Keshav
 *
 */
public class SessionDetails {

    String id;
    String etsName;
    String date;
    int status;
    List<String> failedTestList;

    public SessionDetails(String id, String etsName, String date, int status, List<String> failedTestList) {
        this.id = id;
        this.etsName = etsName;
        this.date = date;
        this.status = status;
        this.failedTestList = failedTestList;
    }

    public SessionDetails() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEtsName() {
        return etsName;
    }

    public void setEtsName(String etsName) {
        this.etsName = etsName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getFailedTestList() {
        return failedTestList;
    }

    public void setFailedTestList(List<String> failedTestList) {
        this.failedTestList = failedTestList;
    }

}
