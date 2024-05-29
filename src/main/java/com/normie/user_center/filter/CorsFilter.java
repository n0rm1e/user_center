package com.normie.user_center.filter;

import com.google.gson.Gson;
import com.normie.user_center.model.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;

import static com.normie.user_center.constant.UserConstant.USER_LOGIN_STATE;
@ConfigurationProperties(prefix = "server")
@Data
@Component
public class CorsFilter implements Filter {
    private String url;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("CorsFilter");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 检查Cookie进行自动登录
        checkCookie(httpRequest, httpResponse);
        // 跨域处理
        handleCors(httpRequest, httpResponse);

        chain.doFilter(request, response);
    }
    private void checkCookie(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            if (httpRequest.getSession().getAttribute(USER_LOGIN_STATE).equals("logout")){
                System.out.println("user is not trying to autologin");
                return;
            }
        } catch (Exception e) {
            System.out.println("user is trying to login without cookie");
        }
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginToken".equals(cookie.getName())) {
                    // 解析Cookie的值，通常是JWT或其他类型的令牌
                    String token = cookie.getValue();
                    // 解码Base64字符串
                    byte[] decodedBytes = Base64.getDecoder().decode(token);
                    // 使用JSON库将其转换回对象
                    String decodedString = new String(decodedBytes);
                    // 验证并解析令牌
                    User user = new Gson().fromJson(decodedString,User.class);
                    if (user != null) {
                        // 用户验证成功，创建或更新session
                        HttpSession session = httpRequest.getSession(true);
                        session.setAttribute(USER_LOGIN_STATE, user);
                        System.out.println("user autologin success");
                    }
                    System.out.println(token);
                }
            }
        }
    }

    private void handleCors(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        System.out.println("当前允许跨域的源：" + url);
        // 设置允许跨域访问的源
        httpResponse.setHeader("Access-Control-Allow-Origin", url);
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
    }

}