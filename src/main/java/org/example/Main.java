package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口
 * Spring Boot 3 会从这里启动嵌入式容器并加载所有 REST API。
 * 
 * @SpringBootApplication 默认会扫描当前包及其子包（org.example.*），
 * 所以 org.example.accounting.controller 下的所有 Controller 都会被扫描到。
 */
@SpringBootApplication(scanBasePackages = "org.example")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
