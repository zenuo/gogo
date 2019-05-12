package zenuo.gogo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public final class SearchResponse implements IResponse {
    private String key;
    private Integer page;
    private Long amount;
    private Float elapsed;
    private List<Entry> entries;
    private String error;
    @JsonIgnore
    private HttpResponseStatus status;
}
