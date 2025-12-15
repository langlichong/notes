package com.example.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Reactor MQTT 流处理 Demo
 * 
 * 核心概念：
 * 1. Sinks.Many: 充当 "数据总线"，将外部的 MQTT 回调转接进入 Reactor 的响应式流世界。
 * 2. Flux Operator: 使用声明式的 API (.filter, .map) 定义业务逻辑。
 * 3. Backpressure: 演示当下游处理慢时，Reactor 如何通过 Buffer 处理积压。
 */
public class MqttReactorDemo {

    // 1. 定义消息模型
    static class SensorData {
        String deviceId;
        double value;
        long timestamp;

        public SensorData(String deviceId, double value) {
            this.deviceId = deviceId;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%s] val=%.2f", deviceId, value);
        }
    }

    // 2. 创建核心数据管道 (The Bus)
    // Multicast: 允许多个订阅者同时监听（例如一个用来告警，一个用来入库）
    // onBackpressureBuffer: 当下游消费不过来时，暂存到队列中，而不是丢弃或报错
    private final Sinks.Many<SensorData> mqttBus = Sinks.many()
            .multicast()
            .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    public static void main(String[] args) throws InterruptedException {
        MqttReactorDemo demo = new MqttReactorDemo();

        // 3. 定义流处理规则 (The Pipeline)
        demo.setupAlertPipeline();

        // 4. 模拟 MQTT 客户端接收消息 (The Source)
        demo.simulateMqttClient();

        // 保持主线程运行以便观察输出
        Thread.sleep(10000);
    }

    /**
     * 定义业务逻辑：过滤 -> 转换 -> 告警
     */
    private void setupAlertPipeline() {
        // 将 Sink 转换为 Flux 流
        Flux<SensorData> dataStream = mqttBus.asFlux();

        dataStream
            // 【过滤逻辑】：只关心温度超过 40 度的读数
            .filter(data -> {
                boolean isHigh = data.value > 40.0;
                if (!isHigh) {
                    // System.out.println("忽略正常数据: " + data); // 调试用
                }
                return isHigh;
            })
            // 【转换逻辑】：简单模拟 enrichment，比如这里可以加标识，或者关联元数据
            .map(data -> "⚠️ [严重告警] 设备 " + data.deviceId + " 温度过高: " + data.value)
            
            // 【并发控制】：模拟告警发送是一个耗时操作 (比如发邮件/调API)
            // publishOn 切换线程，避免阻塞 MQTT 接收线程
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()) 
            .doOnNext(alertMsg -> {
                try {
                    // 模拟发送告警耗时 100ms
                    Thread.sleep(100); 
                } catch (InterruptedException e) {}
                System.err.println(Thread.currentThread().getName() + " 发送处理完成 -> " + alertMsg);
            })
            // 【订阅启动】：必须 subscribe，否则流水线根本不会运行
            .subscribe();
    }

    /**
     * 模拟 MQTT 客户端回调
     */
    private void simulateMqttClient() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Random random = new Random();

        System.out.println(">>> MQTT 客户端开始接收数据...");

        // 每 50ms 收到一条消息 (模拟 20 TPS)
        executor.scheduleAtFixedRate(() -> {
            String deviceId = "Dev-" + random.nextInt(5); // 随机设备 Dev-0 到 Dev-4
            double temp = 20 + random.nextDouble() * 30;  // 20~50 度的随机温度

            SensorData msg = new SensorData(deviceId, temp);

            // 【关键步骤】：在 MQTT 回调线程中，将数据“推”入 Reactor 管道
            // EmitFailureHandler.FAIL_FAST 表示如果推不进去(比如流结束了)就抛异常，
            // 真实场景通常处理 FAIL_ZERO_SUBSCRIBER (如果没有订阅者就不管)
            Sinks.EmitResult result = mqttBus.tryEmitNext(msg);

            if (result.isFailure()) {
                System.out.println("推送失败: " + result);
            }

        }, 0, 50, TimeUnit.MILLISECONDS);
    }
}
