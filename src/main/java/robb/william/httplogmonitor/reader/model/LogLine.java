package robb.william.httplogmonitor.reader.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "remoteHost","rfc931","authUser","date","request","status","bytes" })
public class LogLine {
    String remoteHost;
    String rfc931;
    String authUser;
    int date;
    String request;
    int status;
    int bytes;

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
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

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
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

    @Override
    public String toString() {
        return "LogLine{" +
                "remoteHost='" + remoteHost + '\'' +
                ", rfc931='" + rfc931 + '\'' +
                ", authUser='" + authUser + '\'' +
                ", date=" + date +
                ", request='" + request + '\'' +
                ", status=" + status +
                ", bytes=" + bytes +
                '}';
    }
}
