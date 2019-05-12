package zenuo.gogo.core.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.IProcessor;
import zenuo.gogo.web.IPageBuilder;

@Component("indexProcessor")
@RequiredArgsConstructor
public final class IndexProcessorImpl implements IProcessor {

    /**
     * 页面构建器
     */
    @NonNull
    private final IPageBuilder indexPageBuilder;

    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request, QueryStringDecoder decoder, ResponseType responseType) {
        if (responseType == ResponseType.API) {
            response(ctx,
                    request,
                    ResponseType.API,
                    "{\"info\":\"Hello, welcome to Gogo API, https://github.com/zenuo/gogo\"}",
                    HttpResponseStatus.OK);
        } else {
            response(ctx,
                    request,
                    ResponseType.PAGE,
                    indexPageBuilder.build(null),
                    HttpResponseStatus.OK
            );
        }
    }
}
