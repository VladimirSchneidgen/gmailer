package com.mailer.mailer.cron;

import com.mailer.mailer.api.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CronRunner {

    private final MailService mailService;

    @Scheduled(cron = "0 * * ? * *")
    public void run() throws Exception {
        mailService.sendMail();
    }
}
