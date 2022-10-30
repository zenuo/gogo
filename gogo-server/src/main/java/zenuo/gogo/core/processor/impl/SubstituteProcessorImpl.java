package zenuo.gogo.core.processor.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import zenuo.gogo.core.config.ApplicationConfig;
import zenuo.gogo.core.processor.ISubstituteProcessor;

import javax.inject.Inject;
import java.util.Map;

/**
 * 替换处理器实现
 *
 * @author zenuo
 * @date 2019/05/15
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public final class SubstituteProcessorImpl implements ISubstituteProcessor {

    private final ApplicationConfig applicationConfig;

    @Override
    public String substitute(@NonNull String source) {
        //遍历替换规则
        for (Map.Entry<String, String> rule : applicationConfig.getSubstituteRuleMap().entrySet()) {
            source = source.replaceAll(rule.getKey(), rule.getValue());
        }
        //返回
        return source;
    }
}
