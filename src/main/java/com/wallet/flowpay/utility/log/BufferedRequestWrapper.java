package com.wallet.flowpay.utility.log;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
@Slf4j
public class BufferedRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    public BufferedRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        try (InputStream is = request.getInputStream()) {
            this.body = is.readAllBytes();
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(this.body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    public String getRequestBody() {
        return new String(this.body);
    }

    public Map<String, String> getHeaderMap(){
        Map<String,String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            headers.put(headerName, getHeader(headerName));
        }
        return headers;
    }
}
