package robb.william.httplogmonitor.reader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;


@JsonPropertyOrder({"remoteHost", "rfc931", "authUser", "date", "request", "status", "bytes"})
@Data
public class CommonLogFormat {
    private String remoteHost;
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

}
