package com.feixue.assembly.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class ProtocolBuilder {

    private Protocol protocol;

    private static final List<String> supportMethods = Arrays.asList("get", "post");

    private boolean urlEncoderParamValue = false;

    private ProtocolBuilder() {
        protocol = new Protocol();
    }

    public static ProtocolBuilder createBuilder() {
        return new ProtocolBuilder();
    }

    public ProtocolBuilder method(String method) {
        if (StringUtils.isEmpty(method)) {
            throw new IllegalArgumentException("method is empty!");
        } else if (!supportMethods.contains(method.toLowerCase())) {
            throw new IllegalArgumentException("method " + protocol.getMethod() + " not support!");
        } else {
            protocol.setMethod(method.toLowerCase());
        }
        return this;
    }

    public String getMethod() {
        return protocol == null ? null : protocol.getMethod();
    }

    public ProtocolBuilder url(String url) {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url is empty!");
        } else {
            protocol.setUrl(url);
            return this;
        }
    }

    public String getUrl() {
        return protocol == null ? null : protocol.getUrl();
    }

    public ProtocolBuilder pathReplace(String form, String to) {
        if (StringUtils.isEmpty(protocol.getUrl())) {
            throw new IllegalArgumentException("url is not set!");
        }
        if (form == null || to == null) {
            throw new IllegalArgumentException("path replace for form=" + form + ", to=" + to);
        }
        protocol.setUrl(protocol.getUrl().replace(form, to));

        return this;
    }

    public ProtocolBuilder desc(String desc) {
        protocol.setDesc(desc);
        return this;
    }

    /**
     * 设置编码
     *
     * @param encode
     * @return
     */
    public ProtocolBuilder encode(String encode) {
        Protocol.ProtocolRequest request = protocol.getRequest();
        if (request == null) {
            request = new Protocol.ProtocolRequest();
            protocol.setRequest(request);
        }
        request.setEncode(encode);
        return this;
    }

    public String getDesc() {
        return protocol == null ? "" : protocol.getDesc();
    }

    public ProtocolBuilder param(String paramKey, String paramValue) {
        if (StringUtils.isEmpty(paramKey)) {
            throw new IllegalArgumentException("paramKey is error!");
        }
        Protocol.ProtocolRequest protocolRequest = protocol.getRequest();
        if (protocolRequest == null) {
            protocolRequest = new Protocol.ProtocolRequest();
            protocol.setRequest(protocolRequest);
        }

        if (urlEncoderParamValue) {
            try {
                paramValue = URLEncoder.encode(paramValue, protocol.getRequest().getEncode());
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(e);
            }
        }
        Protocol.ProtocolParam protocolParam = new Protocol.ProtocolParam(paramKey, paramValue);
        protocolRequest.addParam(protocolParam);
        return this;
    }

    public ProtocolBuilder paramMap(Map<String, String> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return this;
        }

        Protocol.ProtocolRequest protocolRequest = protocol.getRequest();
        if (protocolRequest == null) {
            protocolRequest = new Protocol.ProtocolRequest();
            protocol.setRequest(protocolRequest);
        }

        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String paramKey = String.valueOf(entry.getKey());
            String paramValue = String.valueOf(entry.getValue());

            param(paramKey, paramValue);
        }
        return this;
    }

    /**
     * 是否开启urlEncoder param value
     *
     * @param isOpen
     * @param defaultEncode
     * @return
     */
    public ProtocolBuilder urlEncoderParamValue(boolean isOpen, String defaultEncode) throws UnsupportedEncodingException {
        this.urlEncoderParamValue = isOpen;
        if (StringUtils.isEmpty(protocol.getRequest().getEncode())) {
            protocol.getRequest().setEncode(defaultEncode);
        }

        refreash();
        return this;
    }

    public ProtocolBuilder header(String headerKey, String headerValue) {
        Protocol.ProtocolRequest protocolRequest = protocol.getRequest();
        if (protocolRequest == null) {
            protocolRequest = new Protocol.ProtocolRequest();
            protocol.setRequest(protocolRequest);
        }

        Header protocolHeader = new BasicHeader(headerKey, headerValue);
        protocolRequest.addHeader(protocolHeader);
        return this;
    }

    public ProtocolBuilder contentType(ContentType contentType) {
        Protocol.ProtocolRequest protocolRequest = protocol.getRequest();
        if (protocolRequest == null) {
            protocolRequest = new Protocol.ProtocolRequest();
            protocol.setRequest(protocolRequest);
        }

        protocolRequest.setContentType(contentType);
        return this;
    }

    public ProtocolBuilder requestBody(Object body) {
        Protocol.ProtocolRequest protocolRequest = protocol.getRequest();
        if (protocolRequest == null) {
            protocolRequest = new Protocol.ProtocolRequest();
            protocol.setRequest(protocolRequest);
        }

        protocolRequest.setBody(body);
        return this;
    }

    /**
     * 设置链接超时时间
     *
     * @param connectTimeout
     * @return
     */
    public ProtocolBuilder connectTimeout(long connectTimeout) {
        protocol.setConnectTimeout(connectTimeout);

        return this;
    }

    /**
     * 设置通讯超时时间
     *
     * @param socketTimeout
     * @return
     */
    public ProtocolBuilder socketTimeout(long socketTimeout) {
        protocol.setSocketTimeout(socketTimeout);

        return this;
    }

    /**
     * 添加日志标记key
     *
     * @param logKey
     * @return
     */
    public ProtocolBuilder addLogKey(Object logKey) {
        if (logKey == null) {
            //null直接忽略
            return this;
        }
        Set<String> logKeySet = protocol.getLogKey();
        if (logKeySet == null) {
            logKeySet = new HashSet<>();
            protocol.setLogKey(logKeySet);
        }
        logKeySet.add(String.valueOf(logKey));
        return this;
    }

    public Protocol build() {
        if (StringUtils.isEmpty(protocol.getUrl())) {
            throw new IllegalArgumentException("protocol url is empty! can't build it!");
        }
        if (protocol.getMethod().equalsIgnoreCase("post")) {
            if (StringUtils.isEmpty(protocol.getRequest().getEncode())) {
                throw new IllegalArgumentException("protocol post method must set encode!");
            }
        }
        Protocol.ProtocolRequest request = protocol.getRequest();
        if (request == null) {
            request = new Protocol.ProtocolRequest();
            protocol.setRequest(request);
        }
        return protocol;
    }

    /**
     * 刷新参数值
     *
     * @throws UnsupportedEncodingException
     */
    private void refreash() throws UnsupportedEncodingException {
        for (Protocol.ProtocolParam protocolParam : protocol.getRequest().getParamList()) {
            String paramValue = protocolParam.getParamValue();
            paramValue = URLEncoder.encode(paramValue, protocol.getRequest().getEncode());
            protocolParam.setParamValue(paramValue);
        }
    }
}
