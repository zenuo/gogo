package zenuo.gogo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public final class CompleteResponse {
    private String key;
    private List<String> lints;
    private String error;
    @JsonIgnore
    private HttpResponseStatus status;
}
