package com.genew.biz.modular.third.alerts.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 主动获取三方告警数据
 */
@Slf4j
@Component
public class AlertsFetcherRoute extends RouteBuilder {


    @Value("${pls.third.alerts.pollIntervalInSeconds}")
    private String fetchIntervalInSeconds;

    @Value("${pls.third.alerts.endpoints}")
    private String[] endpoints;

    @Override
    public void configure() throws Exception {

        from("timer:thirdAlertsTimer?period=".concat(fetchIntervalInSeconds).concat("s&delay=20s"))
                .routeId("FetchThirdPartyAlertsRouter")
                .log(LoggingLevel.DEBUG, "-----获取三方的alerts数据--------")
                .setBody(constant(endpoints))
                .split(body(),",")
                .choice()
                    .when(body().isNull()).stop()
                .otherwise()
                    .doTry()
                        .log(LoggingLevel.INFO,"---请求三方alerts： ".concat("${body}"))
                        .toD("${body}")
                        .choice()
                            .when(body().isNotNull())
                            .to(AlertLevelConvertRoute.ALERT_LEVEL_CONVERT_START_ENDPOINT)
                            .log(LoggingLevel.INFO,"-----发送获取到的alerts数据到route: ".concat(AlertLevelConvertRoute.ALERT_LEVEL_CONVERT_START_ENDPOINT).concat("-----------"))
                        .otherwise().log(LoggingLevel.WARN,"三方 alerts endpoint 为空")
                    .endDoTry()
                    .doCatch(Exception.class).log(LoggingLevel.ERROR,"获取alerts失败: ${exception.message}");


    }
}
