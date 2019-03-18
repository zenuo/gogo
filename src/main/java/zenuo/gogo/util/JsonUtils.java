package zenuo.gogo.util;

import lombok.extern.slf4j.Slf4j;
import zenuo.gogo.core.Constants;

/**
 * JSON工具类
 */
@Slf4j
public final class JsonUtils {
    private JsonUtils() {

    }

    /**
     * write an object to json
     *
     * @param object the object you want to write
     * @return json string
     */
    public static String toJson(final Object object) {
        try {
            return Constants.MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            log.error("toJson", e);
            return null;
        }
    }
}
