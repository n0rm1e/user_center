package com.normie.user_center.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("CorsFilter");
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 设置允许跨域访问的源
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        // 设置允许跨域访问的方法
        httpResponse.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
        // 设置允许跨域访问的头部
        httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        // 设置是否允许发送Cookie等凭证信息
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        //内容类型：如果是post请求必须指定这个属性
        httpResponse.setHeader("Content-Type", "application/json;charset=utf-8");
        // 设置预检请求的有效期，单位为秒
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        chain.doFilter(request, response);
    }
    
    // 其他方法略...
}
