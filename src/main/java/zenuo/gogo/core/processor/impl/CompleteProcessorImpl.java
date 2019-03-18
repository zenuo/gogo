package zenuo.gogo.core.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.IGogoProcessor;
import zenuo.gogo.model.CompleteResponse;
import zenuo.gogo.util.CompleteUtils;
import zenuo.gogo.util.JsonUtils;

import java.util.List;

public final class CompleteProcessorImpl implements IGogoProcessor {

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
            final CompleteResponse response = CompleteUtils.response(keys.get(0));
            response(ctx,
                    request,
                    ResponseType.API,
                    JsonUtils.toJson(response),
                    response.getStatus());
        }
    }
}
