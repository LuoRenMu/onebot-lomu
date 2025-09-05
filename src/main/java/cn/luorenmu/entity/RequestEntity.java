package cn.luorenmu.entity;


import java.util.List;

/**
 * @author LoMu
 * Date 2023.11.21 21:12
 */

public class RequestEntity {


    public static class RequestDetailed {
        private String url;
        private String method;
        private List<RequestParam> params;
        private List<RequestParam> body;
        private String bodyJson;
        private List<RequestParam> headers;

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

        public List<RequestParam> getParams() {
            return params;
        }

        public void setParams(List<RequestParam> params) {
            this.params = params;
        }

        public List<RequestParam> getBody() {
            return body;
        }

        public void setBody(List<RequestParam> body) {
            this.body = body;
        }

        public String getBodyJson() {
            return bodyJson;
        }

        public void setBodyJson(String bodyJson) {
            this.bodyJson = bodyJson;
        }

        public List<RequestParam> getHeaders() {
            return headers;
        }

        public void setHeaders(List<RequestParam> headers) {
            this.headers = headers;
        }
    }


    public static class RequestParam {
        private String name;
        private String content;

        public RequestParam(String name, String content) {
            this.name = name;
            this.content = content;
        }

        public RequestParam() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
