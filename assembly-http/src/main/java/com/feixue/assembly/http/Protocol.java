package com.feixue.assembly.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 协议实体
 */
public class Protocol implements Serializable {
    private static final long serialVersionUID = 57982722867472989L;

    /**
     * 日志key
     */
    private Set<String> logKey;

    /**
     * 平台key
     */
    private String platformKey;

    /**
     * 协议key
     */
    private String protocolKey;

    /**
     * 协议描述
     */
    private String desc;

    /**
     * url
     */
    private String url;

    /**
     * 具体方法
     */
    private String method;

    /**
     * 链接超时时间
     */
    private Long connectTimeout;

    /**
     * 通讯超时时间
     */
    private Long socketTimeout;

    private ProtocolRequest request;

    public Set<String> getLogKey() {
        return logKey;
    }

    /**
     * 获取关键日志key格式化日志
     *
     * @param partition
     * @return
     */
    public String getLogKeyFormat(String partition) {
        if (logKey == null || logKey.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String key : logKey) {
            builder.append(key);
            builder.append(partition);
        }

        return builder.toString();
    }

    public void setLogKey(Set<String> logKey) {
        this.logKey = logKey;
    }

    public String getPlatformKey() {
        return platformKey;
    }

    public void setPlatformKey(String platformKey) {
        this.platformKey = platformKey;
    }

    public String getProtocolKey() {
        return protocolKey;
    }

    public void setProtocolKey(String protocolKey) {
        this.protocolKey = protocolKey;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ProtocolRequest getRequest() {
        return request;
    }

    public void setRequest(ProtocolRequest request) {
        this.request = request;
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Long socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public static class ProtocolRequest implements Serializable {
        private static final long serialVersionUID = 3711558582411572636L;

        /**
         * 请求header
         */
        private List<Header> headerList;

        /**
         * 请求参数
         */
        private List<ProtocolParam> paramList;

        /**
         * 数据格式
         */
        private ContentType contentType;

        /**
         * 请求编码
         */
        private String encode;

        /**
         * 请求体
         */
        private Object body;

        public List<Header> getHeaderList() {
            return headerList;
        }

        public void setHeaderList(List<Header> headerList) {
            this.headerList = headerList;
        }

        public void addHeader(Header protocolHeader) {
            if (headerList == null) {
                headerList = new ArrayList<>();
            }
            headerList.add(protocolHeader);
        }

        public List<ProtocolParam> getParamList() {
            return paramList;
        }

        public void setParamList(List<ProtocolParam> paramList) {
            this.paramList = paramList;
        }

        public void addParam(ProtocolParam protocolParam) {
            if (paramList == null) {
                paramList = new ArrayList<>();
            }
            paramList.add(protocolParam);
        }

        public ContentType getContentType() {
            return contentType;
        }

        public void setContentType(ContentType contentType) {
            this.contentType = contentType;
        }

        public String getEncode() {
            return encode;
        }

        public void setEncode(String encode) {
            this.encode = encode;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }
    }

    public static class ProtocolHeader implements Serializable {
        private static final long serialVersionUID = -5688468080170523501L;

        private Long id;

        private String protocolKey;

        private String headerKey;

        private String headerValue;

        public ProtocolHeader(String protocolKey, String headerKey, String headerValue) {
            this.protocolKey = protocolKey;
            this.headerKey = headerKey;
            this.headerValue = headerValue;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProtocolKey() {
            return protocolKey;
        }

        public void setProtocolKey(String protocolKey) {
            this.protocolKey = protocolKey;
        }

        public String getHeaderKey() {
            return headerKey;
        }

        public void setHeaderKey(String headerKey) {
            this.headerKey = headerKey;
        }

        public String getHeaderValue() {
            return headerValue;
        }

        public void setHeaderValue(String headerValue) {
            this.headerValue = headerValue;
        }
    }

    public static class ProtocolParam implements Serializable {
        private static final long serialVersionUID = -172970584727211870L;

        private Long id;

        private String protocolKey;

        private String paramKey;

        private String paramValue;

        public ProtocolParam(String paramKey, String paramValue) {
            this.paramKey = paramKey;
            this.paramValue = paramValue;
        }

        public ProtocolParam(String protocolKey, String paramKey, String paramValue) {
            this.protocolKey = protocolKey;
            this.paramKey = paramKey;
            this.paramValue = paramValue;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProtocolKey() {
            return protocolKey;
        }

        public void setProtocolKey(String protocolKey) {
            this.protocolKey = protocolKey;
        }

        public String getParamKey() {
            return paramKey;
        }

        public void setParamKey(String paramKey) {
            this.paramKey = paramKey;
        }

        public String getParamValue() {
            return paramValue;
        }

        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }
    }

    public static class ProtocolResponse implements Serializable {
        private static final long serialVersionUID = 6751261810786987137L;

        private int statusCode;

        private List<Header> headerList;

        private Object responseBody;

        private Throwable throwable;

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public List<Header> getHeaderList() {
            return headerList;
        }

        public void setHeaderList(List<Header> headerList) {
            this.headerList = headerList;
        }

        public String getResponseBodyASString() throws IOException {
            if (responseBody != null) {
                return EntityUtils.toString((HttpEntity) responseBody);
            } else {
                return null;
            }
        }

        public InputStream getResponseBodyASInputSteam() throws IOException {
            if (responseBody != null) {
                return ((HttpEntity) responseBody).getContent();
            } else {
                return null;
            }
        }

        public void setResponseBody(Object responseBody) {
            this.responseBody = responseBody;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }

        public boolean isSuccess() {
            if (throwable != null || statusCode != 200) {
                return false;
            } else {
                return true;
            }
        }

        public String getErrorMsg() throws IOException {
            if (throwable != null) {
                return throwable.getMessage();
            } else if (statusCode != 200) {
                return getResponseBodyASString();
            } else {
                return null;
            }
        }
    }

    public static class ProtocolScript implements Serializable {
        private Long id;

        private String protocolKey;

        private String scriptKey;

        private String scriptValue;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProtocolKey() {
            return protocolKey;
        }

        public void setProtocolKey(String protocolKey) {
            this.protocolKey = protocolKey;
        }

        public String getScriptKey() {
            return scriptKey;
        }

        public void setScriptKey(String scriptKey) {
            this.scriptKey = scriptKey;
        }

        public String getScriptValue() {
            return scriptValue;
        }

        public void setScriptValue(String scriptValue) {
            this.scriptValue = scriptValue;
        }
    }
}
