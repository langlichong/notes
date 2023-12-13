package com.genew.biz.modular.third.alerts.controller;

import com.genew.biz.modular.third.alerts.routes.AlertLevelConvertRoute;
import com.genew.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Api(value = "告警数据处理")
@RestController
@RequestMapping("/third")
public class AlertsReceiverController {

    @Resource
    private ProducerTemplate producerTemplate;


    @ApiOperation(value = "接收三方告警数据",notes = "接收原始的三方告警数据()，并将数据转发到camel路由中")
    @PostMapping("alerts")
    public CommonResult<Object> receiveAlertsFromThirdParty(@RequestBody String alerts) throws IOException {

        log.info("-----receiveAlertsFromThirdParty 接收告警数据----");
        producerTemplate.sendBody(AlertLevelConvertRoute.ALERT_LEVEL_CONVERT_START_ENDPOINT,alerts);
        return CommonResult.ok("alerts send to camel convert route. ");
    }


}

