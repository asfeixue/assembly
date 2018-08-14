package com.feixue.assembly.http;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HttpClientInvoker {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientInvoker.class);

    private static final String defaultCharset = "utf-8";

    //写日志
    private boolean writeLog = false;

    public HttpClientInvoker() {
    }

    public HttpClientInvoker(boolean writeLog) {
        this.writeLog = writeLog;
    }

    public Protocol.ProtocolResponse doInvoke(Protocol protocol) {
        String method = protocol.getMethod();

        if (StringUtils.isEmpty(method)) {
            throw new IllegalArgumentException("protocol method not set!");
        }

        method = method.toLowerCase();

        switch (method) {
            case "get":
                return doGet(protocol);
            case "post":
                Protocol.ProtocolRequest protocolRequest = protocol.getRequest();
                if (protocolRequest == null) {
                    new IllegalArgumentException("protocol request content not set!");
                }
                return doPost(protocol);
            default:
                throw new IllegalArgumentException("protocol method not support!");
        }
    }

    /**
     * 执行get请求
     *
     * @param protocol
     * @return
     */
    private Protocol.ProtocolResponse doGet(Protocol protocol) {
        Protocol.ProtocolRequest protocolRequest = protocol.getRequest();

        Request request = Request.Get(assembleParam(protocol));
        if (protocolRequest != null && protocolRequest.getHeaderList() != null && !protocolRequest.getHeaderList().isEmpty()) {
            request.setHeaders(protocolRequest.getHeaderList().toArray(new Header[]{}));
        }

        long startTime = System.currentTimeMillis();
        if (writeLog) {
            logger.info("logKey={} for request url={} desc={} body={}.",
                    protocol.getLogKeyFormat("  "),
                    protocol.getUrl(),
                    protocol.getDesc(),
                    JSON.toJSONString(protocolRequest.getBody()));
        }

        Protocol.ProtocolResponse response = invokeRequest(request);

        long endTime = System.currentTimeMillis();
        if (writeLog) {
            logger.info("logKey={} for response url={} desc={} body={} consume time={} ms.",
                    protocol.getLogKeyFormat("  "),
                    protocol.getUrl(),
                    protocol.getDesc(),
                    JSON.toJSONString(response),
                    (endTime - startTime));
        }

        return response;
    }

    /**
     * 执行post请求
     *
     * @param protocol
     * @return
     */
    private Protocol.ProtocolResponse doPost(Protocol protocol) {
        Protocol.ProtocolRequest protocolRequest = protocol.getRequest();

        Request request = Request.Post(assembleParam(protocol));
        if (protocolRequest.getHeaderList() != null && !protocolRequest.getHeaderList().isEmpty()) {
            request.setHeaders(protocolRequest.getHeaderList().toArray(new Header[]{}));
        }

        long startTime = System.currentTimeMillis();
        if (writeLog) {
            logger.info("logKey={} for request url={} desc={} body={}.",
                    protocol.getLogKeyFormat("  "),
                    protocol.getUrl(),
                    protocol.getDesc(),
                    JSON.toJSONString(protocolRequest.getBody()));
        }

        Protocol.ProtocolResponse response;

        if (protocolRequest.getContentType() == null) {
            response = invokeRequest(request);
        } else if (protocolRequest.getContentType().getMimeType().equals(ContentType.APPLICATION_JSON.getMimeType())) {
            response = doJSON(request, protocolRequest);
        } else if (protocolRequest.getContentType().getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
            response = doFORM(request, protocolRequest);
        } else if (protocolRequest.getContentType().getMimeType().equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            response = doMultipartFormData(request, protocolRequest);
        } else {
            throw new IllegalArgumentException("not support " + protocolRequest.getContentType().toString() + " contentType!");
        }

        long endTime = System.currentTimeMillis();
        if (writeLog) {
            logger.info("logKey={} for response url={} desc={} body={} consume time={} ms.",
                    protocol.getLogKeyFormat("  "),
                    protocol.getUrl(),
                    protocol.getDesc(),
                    JSON.toJSONString(response),
                    (endTime - startTime));
        }

        return response;
    }

    private Protocol.ProtocolResponse doMultipartFormData(Request request, Protocol.ProtocolRequest protocolRequest) {
        request.body((HttpEntity) protocolRequest.getBody());

        return invokeRequest(request);
    }

    /**
     * 执行表单
     *
     * @param request
     * @param protocolRequest
     * @return
     */
    private Protocol.ProtocolResponse doFORM(Request request, Protocol.ProtocolRequest protocolRequest) {
        Map<Object, Object> bodyMap = JSON.parseObject(JSON.toJSONString(protocolRequest.getBody()), Map.class);

        List<NameValuePair> pairList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : bodyMap.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());

            BasicNameValuePair pair = new BasicNameValuePair(key, value);
            pairList.add(pair);
        }

        request.bodyForm(pairList, Charset.forName(protocolRequest.getEncode()));

        return invokeRequest(request);
    }

    /**
     * 执行json请求
     *
     * @param request
     * @param protocolRequest
     * @return
     */
    private Protocol.ProtocolResponse doJSON(Request request, Protocol.ProtocolRequest protocolRequest) {
        if (protocolRequest.getBody() != null) {
            ContentType contentType;
            if (StringUtils.isEmpty(protocolRequest.getEncode())) {
                contentType = protocolRequest.getContentType().withCharset(defaultCharset);
            } else {
                contentType = protocolRequest.getContentType().withCharset(protocolRequest.getEncode());
            }
            request.bodyString(JSON.toJSONString(protocolRequest.getBody()), contentType);
        }

        return invokeRequest(request);
    }

    /**
     * 执行请求，封装响应
     *
     * @param request
     * @return
     */
    private Protocol.ProtocolResponse invokeRequest(Request request) {
        Protocol.ProtocolResponse protocolResponse = new Protocol.ProtocolResponse();
        try {
            HttpResponse httpResponse = request.execute().returnResponse();
            protocolResponse.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            protocolResponse.setHeaderList(Arrays.asList(httpResponse.getAllHeaders()));
            protocolResponse.setResponseBody(httpResponse.getEntity());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            protocolResponse.setThrowable(e);
        }

        return protocolResponse;
    }

    /**
     * 组装参数
     *
     * @param protocol
     * @return
     */
    private String assembleParam(Protocol protocol) {
        String url = protocol.getUrl();

        if (protocol.getRequest() == null) {
            return url;
        }

        List<Protocol.ProtocolParam> paramList = protocol.getRequest().getParamList();
        if (paramList == null || paramList.isEmpty()) {
            return url;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(url);
        builder.append("?");
        for (Protocol.ProtocolParam protocolParam : protocol.getRequest().getParamList()) {
            builder.append(protocolParam.getParamKey());
            builder.append("=");
            builder.append(protocolParam.getParamValue());
            builder.append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
