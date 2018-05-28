package yz.gogo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class CompleteResponse {
    private String key;
    private List<String> lint;
}
