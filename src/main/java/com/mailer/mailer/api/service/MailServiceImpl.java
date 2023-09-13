package com.mailer.mailer.api.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.mailer.mailer.api.MailService;
import com.mailer.mailer.properties.CredentialProperties;
import com.mailer.mailer.properties.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;

import static com.google.api.services.gmail.GmailScopes.*;
import static javax.mail.Message.RecipientType.TO;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final MailProperties mailProperties;
    private final CredentialProperties credentialProperties;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = new FileInputStream(String.valueOf(credentialProperties.getPath()));

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Set.of(
                        GMAIL_SEND, GMAIL_COMPOSE, GMAIL_LABELS, GMAIL_MODIFY, MAIL_GOOGLE_COM))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        // returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Override
    public void sendMail() throws Exception {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName("mailer")
                .build();

        // Create the email content
        final String bodyText = """
                test body
                """;

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(mailProperties.getFrom()));
        email.addRecipient(TO, new InternetAddress(mailProperties.getTo()));
        email.setSubject(mailProperties.getSubject());
        email.setText(bodyText);

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            service.users().messages().send("me", message).execute();
            LOG.info("Mail sent successfully");
        } catch (GoogleJsonResponseException e) {
            LOG.error("Mail failed to sent, message: {}", e.getDetails());
        }
    }

    @Override
    public List<Label> getLabels() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName("mailer")
                .build();

        return service.users().labels().list("me").execute().getLabels();
    }

    @Override
    public void deleteSpam() throws Exception {
        if (delete("SPAM")) {
            LOG.info("Spam mails were deleted");
        } else {
            LOG.info("There is nothing to delete from spam");
        }
    }

    @Override
    public void deleteSocials() throws Exception {
        if (delete("CATEGORY_SOCIAL")) {
            LOG.info("Social mails were deleted");
        } else {
            LOG.info("There is nothing to delete from socials");
        }
    }

    @Override
    public void deletePromotions() throws Exception {
        if (delete("CATEGORY_PROMOTIONS")) {
            LOG.info("Promotion mails were deleted");
        } else {
            LOG.info("There is nothing to delete from promotions");
        }
    }

    public boolean delete(String label) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName("mailer")
                .build();

        ListMessagesResponse messages = service.users().messages().list("me")
                .setLabelIds(List.of(label))
                .setIncludeSpamTrash(true)
                .setMaxResults(500L)
                .execute();

        if (messages.getMessages() != null) {
            // arraylist holding message IDs
            final var idList = new ArrayList<String>();
            messages.getMessages().forEach(message -> idList.add(message.getId()));

            final var batchDeleteRequest = new BatchDeleteMessagesRequest();
            batchDeleteRequest.setIds(idList);

            // batch delete all social mails
            service.users().messages().batchDelete("me", batchDeleteRequest).execute();

            messages = service.users().messages().list("me")
                    .setLabelIds(List.of(label))
                    .setIncludeSpamTrash(true)
                    .setMaxResults(500L)
                    .execute();

            if (messages.size() > 0) {
                deleteSpam();
            }
            return true;
        }
        return false;
    }
}
