package zenuo.gogo.core.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.IProcessor;
import zenuo.gogo.web.IPageBuilder;
import zenuo.gogo.web.IndexPageBuilder;

public final class IndexProcessorImpl implements IProcessor {

    /**
     * 页面构建器
     */
    private static final IPageBuilder PAGE_BUILDER = new IndexPageBuilder();

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
                    PAGE_BUILDER.build(null),
                    HttpResponseStatus.OK
            );
        }
    }
}
