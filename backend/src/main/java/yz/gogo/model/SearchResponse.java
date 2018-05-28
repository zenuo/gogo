package yz.gogo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class SearchResponse {
    private String key;
    private int page;
    private List<Entry> entries;
    private String error;
}
