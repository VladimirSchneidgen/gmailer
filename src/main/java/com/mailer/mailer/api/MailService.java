package com.mailer.mailer.api;

import com.google.api.services.gmail.model.Label;

import java.util.List;

public interface MailService {

    void sendMail() throws Exception;
    void deleteSpam() throws Exception;
    void deleteSocials() throws Exception;
    void deletePromotions() throws Exception;
    List<Label> getLabels() throws Exception;
}
