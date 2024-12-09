package com.softwaremarket.autoupgrade;

import com.softwaremarket.autoupgrade.task.ApplicationVersionTask;
import com.softwaremarket.autoupgrade.task.RpmVersionTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        ApplicationVersionTask applicationVersionTask = context.getBean(ApplicationVersionTask.class);
        RpmVersionTask rpmVersionTask = context.getBean(RpmVersionTask.class);
        rpmVersionTask.rpmAutocommit();
        //  applicationVersionTask.premiumAppAutocommit();
       // applicationVersionTask.premiumAppAllOsVersionUpdate();
        System.exit(0);
    }

}
