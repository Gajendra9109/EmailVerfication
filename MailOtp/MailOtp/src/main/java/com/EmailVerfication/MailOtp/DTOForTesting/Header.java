package com.EmailVerfication.MailOtp.DTOForTesting;
class Header {
    private String id;
    private String ref;
    private String issueDate;
    private String issueTime;
    private int totalRecord;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(String issueTime) {
        this.issueTime = issueTime;
    }

    public int getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
    }

    @Override
    public String toString() {
        return "Header{" +
                "id='" + id + '\'' +
                ", ref='" + ref + '\'' +
                ", issueDate='" + issueDate + '\'' +
                ", issueTime='" + issueTime + '\'' +
                ", totalRecord=" + totalRecord +
                '}';
    }
}