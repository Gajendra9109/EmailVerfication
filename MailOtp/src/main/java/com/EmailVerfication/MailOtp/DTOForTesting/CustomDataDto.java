package com.EmailVerfication.MailOtp.DTOForTesting;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomDataDto {
    private Header header;
    private List<TData> tData;
    private List<String> additionalInfo;

    // Getters and Setters
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<TData> getTData() {
        return tData;
    }

    public void setTData(List<TData> tData) {
        this.tData = tData;
    }

    public List<String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(List<String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "CustomDataDto{" +
                "header=" + header +
                ", tData=" + tData +
                ", additionalInfo=" + additionalInfo +
                '}';
    }
}