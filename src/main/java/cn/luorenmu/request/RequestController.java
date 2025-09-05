package cn.luorenmu.request;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.luorenmu.entity.RequestEntity;
import com.alibaba.fastjson2.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LoMu
 * Date 2024.05.19 15:11
 */

public class RequestController {
    private final RequestEntity.RequestDetailed requestDetailed;
    private final HttpRequest httpRequest;

    public RequestController(RequestEntity.RequestDetailed requestDetailed) {
        this.requestDetailed = requestDetailed;
        httpRequest = HttpRequest.of(buildParamsUrl());
        addData();
    }

    public RequestController(String url) {
        this.requestDetailed = new RequestEntity.RequestDetailed();
        this.requestDetailed.setUrl(url);
        this.requestDetailed.setMethod("get");
        this.httpRequest = HttpRequest.of(buildParamsUrl());
        addData();
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    private String buildParamsUrl() {
        if (requestDetailed.getParams() != null && !requestDetailed.getParams().isEmpty()) {
            String params = requestDetailed.getParams().stream().map(i -> i.getName() + "=" + i.getContent()).reduce((a, b) -> a + "&" + b).get();
            requestDetailed.setUrl(requestDetailed.getUrl() + "?" + params);
        }
        return requestDetailed.getUrl();
    }


    public void setParams(String name, String value) {
        List<RequestEntity.RequestParam> requestParams = requestDetailed.getParams();
        requestParams.add(new RequestEntity.RequestParam(name, value));
    }


    @Override
    public String toString() {
        return "RequestController{" +
                "requestDetailed=" + requestDetailed +
                '}';
    }

    public void setBody(String name, String value) {
        List<RequestEntity.RequestParam> body = requestDetailed.getBody();
        body.add(new RequestEntity.RequestParam(name, value));
    }


    private void addData() {
        if (requestDetailed.getParams() != null) {
            httpRequest.setUrl(paramToUrl(requestDetailed.getUrl(), requestDetailed.getParams()));
        } else {
            httpRequest.setUrl(paramToUrl(requestDetailed.getUrl(), null));
        }
        if (requestDetailed.getHeaders() != null) {
            httpRequest.addHeaders(toHeaders(requestDetailed.getHeaders()));
        }
        if (requestDetailed.getBody() != null) {
            httpRequest.body(requestParamToJsonStr(requestDetailed.getBody()));
        } else if (requestDetailed.getBodyJson() != null) {
            httpRequest.body(requestDetailed.getBodyJson());
        }
        if (requestDetailed.getHeaders() != null && requestDetailed.getHeaders().stream().anyMatch(i -> i.getName().equals("User-Agent"))) {
            httpRequest.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0");
        }
    }

    private String requestParamToJsonStr(List<RequestEntity.RequestParam> requestBody) {
        Map<String, String> map = new HashMap<>();
        for (RequestEntity.RequestParam body : requestBody) {
            map.put(body.getName(), body.getContent());
        }
        return JSON.toJSONString(map);
    }

    private Map<String, String> toHeaders(List<RequestEntity.RequestParam> requestHeaders) {
        Map<String, String> map = new HashMap<>();
        for (RequestEntity.RequestParam header : requestHeaders) {
            map.put(header.getName(), header.getContent());
        }
        return map;
    }

    private String paramToUrl(String url, List<RequestEntity.RequestParam> requestParams) {
        if (requestParams == null || requestParams.isEmpty()) {
            return url;
        }

        StringBuilder newUrl = new StringBuilder(url);
        newUrl.append("?");
        for (RequestEntity.RequestParam param : requestParams) {
            newUrl.append(param.getName());
            newUrl.append("=");
            String content = param.getContent();
            newUrl.append(content);
            newUrl.append("&");
        }
        return newUrl.toString();
    }


    public HttpResponse request() {
        HttpRequest request = switch (requestDetailed.getMethod().toLowerCase()) {
            case "post" -> httpRequest.method(Method.POST);
            case "delete" -> httpRequest.method(Method.DELETE);
            case "put" -> httpRequest.method(Method.PUT);
            case "head" -> httpRequest.method(Method.HEAD);
            case "options" -> httpRequest.method(Method.OPTIONS);
            case "trace" -> httpRequest.method(Method.TRACE);
            default -> httpRequest.method(Method.GET);
        };
        return request.execute();
    }
}
