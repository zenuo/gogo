package zenuo.gogo.core.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.IIndexProcessor;
import zenuo.gogo.web.IIndexPageBuilder;
import zenuo.gogo.web.IPageBuilder;

import java.util.ServiceLoader;

public final class IndexProcessorImpl implements IIndexProcessor {

    /**
     * 页面构建器
     */
    private final IPageBuilder indexPageBuilder = ServiceLoader.load(IIndexPageBuilder.class).iterator().next();

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
