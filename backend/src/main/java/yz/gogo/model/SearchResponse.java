package yz.gogo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class Response {
    private String key;
    private int page;
    private List<Entry> entries;
    private String error;
}
