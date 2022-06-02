package robb.william.httplogmonitor.reader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"remoteHost", "rfc931", "authUser", "date", "request", "status", "bytes"})
public class CommonLogFormat {
    private String remotehost;
    private String rfc931;
    private String authUser;
    private long date;
    private String request;
    private int status;
    private int bytes;

    @JsonIgnore
    private String section;
    @JsonIgnore
    private String verb;

    public String getRemotehost() {
        return remotehost;
    }

    public void setRemotehost(String remotehost) {
        this.remotehost = remotehost;
    }

    public String getRfc931() {
        return rfc931;
    }

    public void setRfc931(String rfc931) {
        this.rfc931 = rfc931;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    @Override
    public String toString() {
        return "CommonLogFormat{" +
                "host='" + remotehost + '\'' +
                ", rfc931='" + rfc931 + '\'' +
                ", authUser='" + authUser + '\'' +
                ", date=" + date +
                ", request='" + request + '\'' +
                ", status=" + status +
                ", bytes=" + bytes +
                ", section='" + section + '\'' +
                ", verb='" + verb + '\'' +
                '}';
    }
}
