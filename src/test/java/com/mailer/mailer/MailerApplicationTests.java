package com.mailer.mailer;

import com.mailer.mailer.api.MailService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MailerApplicationTests {

	@Autowired
	private MailService mailService;

	@Test
	@Ignore
	public void sendMail() throws Exception {
		mailService.sendMail();
	}

	@Test
	@Ignore
	public void deleteSpam() throws Exception {
		mailService.deleteSpam();
	}

	@Test
	@Ignore
	public void deletePromotion() throws Exception {
		mailService.deletePromotions();
	}

	@Test
	@Ignore
	public void deleteSocials() throws Exception {
		mailService.deleteSocials();
	}

	@Test
	@Ignore
	public void getLabels() throws Exception {
		mailService.getLabels();
	}
}
