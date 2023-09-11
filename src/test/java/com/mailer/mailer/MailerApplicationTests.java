package com.mailer.mailer;

import com.mailer.mailer.api.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MailerApplicationTests {

	@Autowired
	MailService mailService;

	@Test
	public void sendMail() throws Exception {
		mailService.sendMail();
	}
}
