package com.genew.biz.modular.third.alerts.routes;


import com.genew.biz.core.config.MqttClientConfig;
import com.genew.biz.core.config.MqttConfig;
import com.genew.biz.modular.pls.util.MqttClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.apache.camel.support.builder.PredicateBuilder.and;


/**
 * 转换三方 alert的等级 并将转换后的数据推送到 MQTT SERVER (emqx topic = PLS/data/alarm)
 */
@Slf4j
@Component
public class AlertLevelConvertRoute extends RouteBuilder {

    public static final String ALERT_LEVEL_CONVERT_START_ENDPOINT = "direct:alertLevelConvertStart";
    public static final String ALERT_LEVEL_CONVERT_ROUTER_ID = "AlertLevelConvertRouter";

    private String mqttEndpoint;

    @org.springframework.beans.factory.annotation.Value("${pls.third.alerts.convertScriptFile}")
    private String scriptFilePath;

    @Resource
    private MqttClientUtil mqttClientUtil;

    @org.springframework.beans.factory.annotation.Value("classpath:/src/main/alertLevelConvertScript.txt")
    private org.springframework.core.io.Resource scriptResource;

    @PostConstruct
    private void buildMqttEndpoint(){

        final MqttClientConfig mqttConfig = mqttClientUtil.getMqttConfig();

        mqttEndpoint = "paho:".concat(MqttConfig.ALARM_TOPIC)
                .concat("?brokerUrl=").concat(mqttConfig.getHost())
                .concat("&clientId=plsBizAlertCamelRouter")
                .concat("&connectionTimeout=15")
                .concat("&userName=").concat(mqttConfig.getUsername())
                .concat("&password=").concat(mqttConfig.getPassword());

        log.debug("------ paho mqtt endpoint = {}",mqttEndpoint);

    }


    @Override
    public void configure() {

        from(ALERT_LEVEL_CONVERT_START_ENDPOINT)
                .routeId(ALERT_LEVEL_CONVERT_ROUTER_ID)
                .setHeader("script",method("alertLevelConvertRoute", "getScriptContent"))
                .choice()
                    .when(and(header("script").isNotNull(),header("script").isNotEqualTo("")))
                        .process(exchange -> {
                            String scriptContent = exchange.getIn().getHeader("script", String.class);
                            final String body = exchange.getIn().getBody(String.class);
                            final String result = doConvertWithJs(scriptContent, body);
                            exchange.getIn().setBody(result);
                        })
                        .log(">>> Camel Alert Convert Result: ${body}")
                        .to(mqttEndpoint)
                .otherwise().log(LoggingLevel.ERROR,"-----route scirpt header is empty !-----------").stop();

    }

    /**
     * 获取脚本内容，供route运行时调用
     * @return
     */
    public String getScriptContent() throws IOException {

        final ApplicationHome applicationHome = new ApplicationHome(getClass());
        log.debug("--------application home {} -----",applicationHome.getDir().getPath());
        final Path path = Paths.get(scriptFilePath);
        final boolean exists = Files.exists(path);
        if(!exists){
            log.error("告警转换: 脚本文件不存在: {}",scriptFilePath);
            return StringUtils.EMPTY;
        }

        final String scriptContent = Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
        log.debug("alert convert js: {}",scriptContent);

        return scriptContent;
    }


    /**
     * 使用 GraaVM JS 引擎进行数据转换处理
     * @param funcBody 方法体内容
     * @param param 告警数据，一般是 json 数据
     * @return 处理结果
     */
    private String doConvertWithJs(String funcBody, String param) {

        log.info("-- execute js function---");

        final Context ctx = Context.newBuilder("js").option("engine.WarnInterpreterOnly", "false").allowAllAccess(true).build();

        ctx.eval("js", "function convertLevel(param){" + funcBody + " }");
        final Value levelConvertFunction = ctx.getBindings("js").getMember("convertLevel");
        final String res = levelConvertFunction.execute(param).toString();

        ctx.close();

        return res;
    }

}
