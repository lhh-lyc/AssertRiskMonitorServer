package com.lhh.servergateway.filter;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.TokenConstants;
import com.lhh.serverbase.common.constant.Constant;
import com.lhh.serverbase.utils.MD5;
import com.lhh.servergateway.jwt.common.ResponseCodeEnum;
import com.lhh.servergateway.jwt.common.ResponseResult;
import com.lhh.servergateway.jwt.config.PassJavaJwtProperties;
import com.lhh.servergateway.jwt.utils.PassJavaJwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component("authFilter")
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Resource
    private PassJavaJwtProperties jwtProperties;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    private static final String AUTH_TOKEN_URL = "/admin/auth/login";
    private static final String AUTH = "Authorization";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "username";
    public static final String ENC_USER_ID = "encUserId";
    public static final String FROM_SOURCE = "from-source";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders header = request.getHeaders();
        String token = header.getFirst(AUTH);

        // 跳过对登录请求的 token 检查。因为登录请求是没有 token 的，是来申请 token 的。
        String requestUrl = request.getURI().getPath();
        if(AUTH_TOKEN_URL.equals(requestUrl)) {
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        if (StringUtils.isBlank(token)) {
            return unauthorizedResponse(exchange, response, ResponseCodeEnum.TOKEN_MISSION);
        }
        if (StringUtils.isEmpty(token)) {
            return unauthorizedResponse(exchange, response, ResponseCodeEnum.TOKEN_MISSION);
        }

        // 对Token解签名，并验证Token是否过期
        boolean isJwtNotValid = jwtTokenUtil.isTokenExpired(token);
        if(isJwtNotValid){
            return unauthorizedResponse(exchange, response, ResponseCodeEnum.TOKEN_INVALID);
        }
        // 验证 token 里面的 userId 是否为空
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUserNameFromToken(token);
        if (StringUtils.isEmpty(userId)) {
            return unauthorizedResponse(exchange, response, ResponseCodeEnum.TOKEN_CHECK_INFO_FAILED);
        }

        String encUserId = header.getFirst(ENC_USER_ID);
        ResponseCodeEnum e = jwtTokenUtil.isSelf(userId, encUserId);
        if (e != null) {
            return unauthorizedResponse(exchange, response, e);
        }

        /*boolean isRefreshTokenNotExisted = jwtTokenUtil.isRefreshTokenNotExistCache(token);
        String accessToken = Const.STR_EMPTY;
        if(isRefreshTokenNotExisted){
            accessToken = jwtTokenUtil.refreshTokenAndGenerateToken(userId, username);
        }*/

        // 设置用户信息到请求
        ServerHttpRequest.Builder mutate = request.mutate();
        addHeader(mutate, USER_ID, userId);
        addHeader(mutate, USER_NAME, username);
        // 内部请求来源参数清除
        removeHeader(mutate, FROM_SOURCE);

        // TODO 将用户信息存放在请求header中传递给下游业务
        mutate.header("user-name", username);
        ServerHttpRequest buildReuqest = mutate.build();

        //todo 如果响应中需要放数据，也可以放在response的header中
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("user-name",username);

        String refreshToken = header.getFirst(REFRESH_TOKEN);
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            Map<String, Object> tokenMap = jwtTokenUtil.generateTokenAndRefreshToken(userId, username);
            response.getHeaders().add(ACCESS_TOKEN, MapUtil.getStr(tokenMap, ACCESS_TOKEN));
            response.getHeaders().add(REFRESH_TOKEN, MapUtil.getStr(tokenMap, REFRESH_TOKEN));
            response.getHeaders().add("Access-Control-Expose-Headers", ACCESS_TOKEN);
            response.getHeaders().add("Access-Control-Expose-Headers", REFRESH_TOKEN);
        }
        return chain.filter(exchange.mutate()
                .request(buildReuqest)
                .response(response)
                .build());
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name) {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }

    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, Constant.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(jwtProperties.getHeader());
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX))
        {
            token = token.replaceFirst(TokenConstants.PREFIX, StringUtils.EMPTY);
        }
        return token;
    }

    /**
     * 将 JWT 鉴权失败的消息响应给客户端
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, ServerHttpResponse serverHttpResponse, ResponseCodeEnum responseCodeEnum) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        ResponseResult responseResult = ResponseResult.error(responseCodeEnum.getCode(), responseCodeEnum.getMessage());
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory()
                .wrap(JSON.toJSONStringWithDateFormat(responseResult, JSON.DEFFAULT_DATE_FORMAT)
                        .getBytes(StandardCharsets.UTF_8));
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }

}

