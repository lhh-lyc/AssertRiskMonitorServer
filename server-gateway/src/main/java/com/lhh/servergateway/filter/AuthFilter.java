package com.lhh.servergateway.filter;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.Constants;
import com.lhh.serverbase.common.constant.TokenConstants;
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

@Component("authFilter")
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Resource
    private PassJavaJwtProperties jwtProperties;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    private static final String AUTH_TOKEN_URL = "/auth/login";
    private static final String AUTH = "Authorization";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "username";
    public static final String FROM_SOURCE = "from-source";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Auth start");
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders header = request.getHeaders();
        String token = header.getFirst(AUTH);

        // ???????????????????????? token ???????????????????????????????????? token ?????????????????? token ??????
        String requestUrl = request.getURI().getPath();
        log.info(requestUrl);
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

        // ???Token?????????????????????Token????????????
        boolean isJwtNotValid = jwtTokenUtil.isTokenExpired(token);
        if(isJwtNotValid){
            return unauthorizedResponse(exchange, response, ResponseCodeEnum.TOKEN_INVALID);
        }
        // ?????? token ????????? userId ????????????
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUserNameFromToken(token);
        if (StringUtils.isEmpty(userId)) {
            return unauthorizedResponse(exchange, response, ResponseCodeEnum.TOKEN_CHECK_INFO_FAILED);
        }

        // ???????????????????????????
        ServerHttpRequest.Builder mutate = request.mutate();
        addHeader(mutate, USER_ID, userId);
        addHeader(mutate, USER_NAME, username);
        // ??????????????????????????????
        removeHeader(mutate, FROM_SOURCE);

        // TODO ??????????????????????????????header????????????????????????
        mutate.header("user-name", username);
        ServerHttpRequest buildReuqest = mutate.build();

        //todo ????????????????????????????????????????????????response???header???
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("user-name",username);
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
     * ????????????
     *
     * @param str ??????
     * @return ??????????????????
     */
    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * ????????????token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(jwtProperties.getHeader());
        // ??????????????????????????????????????????????????????
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX))
        {
            token = token.replaceFirst(TokenConstants.PREFIX, StringUtils.EMPTY);
        }
        return token;
    }

    /**
     * ??? JWT ???????????????????????????????????????
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, ServerHttpResponse serverHttpResponse, ResponseCodeEnum responseCodeEnum) {
        log.error("[??????????????????]????????????:{}", exchange.getRequest().getPath());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        ResponseResult responseResult = ResponseResult.error(responseCodeEnum.getCode(), responseCodeEnum.getMessage());
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory()
                .wrap(JSON.toJSONStringWithDateFormat(responseResult, JSON.DEFFAULT_DATE_FORMAT)
                        .getBytes(StandardCharsets.UTF_8));
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }

}

