package zenuo.gogo.core.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.IGogoProcessor;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.JsonUtils;
import zenuo.gogo.util.SearchUtils;
import zenuo.gogo.web.ResultPageBuilder;

import java.util.List;

public final class SearchProcessorImpl implements IGogoProcessor {
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request, QueryStringDecoder decoder, ResponseType responseType) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || "".equals(keys.get(0))) {
            response(ctx,
                    request,
                    ResponseType.API,
                    "{\"error\": \"the keyword should not be empty\"}",
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final List<String> pages = decoder.parameters().get("p");
            final SearchResponse response = SearchUtils.response(
                    keys.get(0),
                    pages == null || "".equals(pages.get(0)) ? 1 : Integer.parseInt(pages.get(0)));
            if (responseType == ResponseType.API) {
                response(ctx,
                        request,
                        ResponseType.API,
                        JsonUtils.toJson(response),
                        response.getStatus());
            } else {
                response(ctx,
                        request,
                        ResponseType.PAGE,
                        ResultPageBuilder.build(response),
                        response.getStatus());
            }
        }
    }
}
