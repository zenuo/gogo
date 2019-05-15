package zenuo.gogo.exception;

/**
 * 搜索异常
 *
 * @author zenuo
 * @date 2019/05/15
 */
public final class SearchException extends Exception {
    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
