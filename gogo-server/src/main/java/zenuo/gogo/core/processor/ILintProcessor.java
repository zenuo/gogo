package zenuo.gogo.core.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public interface ILintProcessor extends IProcessor {

    List<String> lint(String key);

    @Builder
    @AllArgsConstructor
    @Getter
    final class LintResponse {
        private String key;
        private List<String> lints;
        private String error;
    }
}
