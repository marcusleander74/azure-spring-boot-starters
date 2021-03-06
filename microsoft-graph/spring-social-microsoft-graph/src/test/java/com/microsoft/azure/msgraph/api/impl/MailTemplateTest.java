/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.msgraph.api.impl;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.microsoft.azure.msgraph.api.BodyType;
import com.microsoft.azure.msgraph.api.EmailAddress;
import com.microsoft.azure.msgraph.api.Importance;
import com.microsoft.azure.msgraph.api.InferenceClassificationType;
import com.microsoft.azure.msgraph.api.ItemBody;
import com.microsoft.azure.msgraph.api.Message;
import com.microsoft.azure.msgraph.api.Messages;
import com.microsoft.azure.msgraph.api.Recipient;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class MailTemplateTest extends AbstractMicrosoftApiTest {

    @Test
    public void listMessagesSuccessfully() throws ParseException {
        mockServer
                .expect(requestTo(microsoft.getGraphAPI("me/messages")))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer access token"))
                .andRespond(withSuccess(jsonResource("messages"), MediaType.APPLICATION_JSON));

        final Messages messages = microsoft.mailOperations().listMessages();
        verifyMessages(messages);
    }

    @Test
    public void listMessagesWithMailBoxSuccessfully() throws ParseException {
        final String folderID = "Inbox";

        mockServer
                .expect(requestTo(microsoft.getGraphAPI("me/mailFolders/" + folderID + "/messages")))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer access token"))
                .andRespond(withSuccess(jsonResource("messages"), MediaType.APPLICATION_JSON));

        final Messages messages = microsoft.mailOperations().listMessages(folderID);
        verifyMessages(messages);
    }

    @Test
    public void listMessagesWithParamsSuccessfully() throws ParseException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        final List<String> values = new ArrayList<>();
        values.add("10");
        params.put("$skip", values);

        mockServer
                .expect(requestTo(microsoft.getGraphAPI("me/messages?%24skip=10")))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer access token"))
                .andRespond(withSuccess(jsonResource("messages"), MediaType.APPLICATION_JSON));

        final Messages messages = microsoft.mailOperations().listMessages(params);
        verifyMessages(messages);
    }

    @Test
    public void listMessagesWithMailBoxAndParamsSuccessfully() throws ParseException {
        final String folderID = "Inbox";

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        final List<String> values = new ArrayList<>();
        values.add("10");
        params.put("$skip", values);

        mockServer
                .expect(requestTo(microsoft.getGraphAPI("me/mailFolders/Inbox/messages?%24skip=10")))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer access token"))
                .andRespond(withSuccess(jsonResource("messages"), MediaType.APPLICATION_JSON));

        final Messages messages = microsoft.mailOperations().listMessages(folderID, params);
        verifyMessages(messages);
    }

    @Test
    public void listMessagesWithNextLinkSuccessfully() throws ParseException {
        final String nextLink = "https://dummy.com/test";

        mockServer
                .expect(requestTo(nextLink))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer access token"))
                .andRespond(withSuccess(jsonResource("messages"), MediaType.APPLICATION_JSON));

        final Messages messages = microsoft.mailOperations().listMessagesWithNextLink(nextLink);
        verifyMessages(messages);
    }

    @Test
    public void sendMailSuccessfully() throws ParseException {
        final String content = "{\"saveToSentItems\":true,\"message\":{\"subject\":\"Meet for lunch?\"," +
                "\"body\":{\"contentType\":\"text\",\"content\":\"The new cafeteria is open.\"},\"" +
                "toRecipients\":[{\"emailAddress\":{\"name\":null,\"address\":\"" +
                "fannyd@contoso.onmicrosoft.com\"}}]}}";
        mockServer.expect(requestTo(microsoft.getGraphAPI("me/sendMail")))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer access token"))
                .andExpect(content().string(content))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        final Message message = new Message();
        message.setSubject("Meet for lunch?");

        final ItemBody body = new ItemBody();
        body.setContentType(BodyType.text);
        body.setContent("The new cafeteria is open.");
        message.setBody(body);

        final List<Recipient> recipients = new ArrayList<>();
        final Recipient recipient = new Recipient();
        final EmailAddress emailAddress = new EmailAddress();
        emailAddress.setAddress("fannyd@contoso.onmicrosoft.com");
        recipient.setEmailAddress(emailAddress);
        recipients.add(recipient);
        message.setToRecipients(recipients);

        microsoft.mailOperations().sendMail(message, true);
    }

    private void verifyMessages(Messages messages) throws ParseException {
        assertThat(messages.getValue().size()).isEqualTo(2);

        final Message message = messages.getValue().get(0);
        assertThat(message.getCreatedDateTime().getTime()).
                isEqualTo(new StdDateFormat().parse("2017-07-31T16:20:47Z"));
        assertThat(message.getLastModifiedDateTime().getTime()).
                isEqualTo(new StdDateFormat().parse("2017-08-08T19:40:47Z"));
        assertThat(message.getChangeKey()).isEqualTo("CQAAABYAAABuW0RzxJhUT4ez5BWQCxI9AABh8vwG");
        assertThat(message.getCategories()).isEmpty();
        assertThat(message.getReceivedDateTime().getTime()).
                isEqualTo(new StdDateFormat().parse("2017-07-31T16:20:47Z"));
        assertThat(message.getSentDateTime().getTime()).
                isEqualTo(new StdDateFormat().parse("2017-07-31T11:17:11Z"));
        assertThat(message.getHasAttachments()).isFalse();
        assertThat(message.getInternetMessageId()).
                isEqualTo("<CY1PR21MB0037008DE4635704988C7178A6B20@CY1PR21MB0037.namprd21.prod.outlook.com>");
        assertThat(message.getSubject()).isEqualTo("Weekly digest: Office 365 changes");
        assertThat(message.getBodyPreview()).
                isEqualTo("You have 3 new Office 365 Message center announcements from last week\r\n\r\n\r\n" +
                        "Organization: MICROSOFT API SANDBOX\r\n        Is this digest useful to you?   Yes" +
                        "     No      Tell us more...\r\nOffice 365 announcements from last week" +
                        "         Edit Message cente");
        assertThat(message.getImportance()).isEqualTo(Importance.normal);
        assertThat(message.getParentFolderId()).
                isEqualTo("AQMkADRmZWM1ODE4LWQ4YWItNDlkYS1iZTY4LWVhZWEzYjRlODgAMzkALgAAAwuZXteXVEJLqYcO_ELlrI" +
                        "4BAG5bRHPEmFRPh7PkFZALEj0AAAIBDAAAAA==");
        assertThat(message.getConversationId()).isEqualTo("AAQkADRmZWM1ODE4LWQ4YWItNDlkYS1iZTY4LWVhZWEzYjRlODgz" +
                "OQAQANhHWVZXoHtImHvs-m5jEbU=");
        assertThat(message.getIsDeliveryReceiptRequested()).isNull();
        assertThat(message.getIsReadReceiptRequested()).isFalse();
        assertThat(message.getIsRead()).isFalse();
        assertThat(message.getIsDraft()).isFalse();
        assertThat(message.getWebLink()).isEqualTo("https://outlook.office365.com/owa/?ItemID=AAMkADRmZWM1O" +
                "DE4LWQ4YWItNDlkYS1iZTY4LWVhZWEzYjRlODgzOQBGAAAAAAALmV7Xl1RCS6mHDvhC5ayOBwBuW0RzxJhUT4ez" +
                "5BWQCxI9AAAAAAEMAABuW0RzxJhUT4ez5BWQCxI9AABdGgEMAAA%3D&exvsurl=1&viewmodel=ReadMessageItem");
        assertThat(message.getInferenceClassification()).isEqualTo(InferenceClassificationType.focused);

        final ItemBody itemBody = message.getBody();
        assertThat(itemBody.getContentType()).isEqualTo(BodyType.html);
        assertThat(itemBody.getContent()).isEqualTo("<html>\r\n<head>\r\n<meta http-equiv=\"Content-Type\" " +
                "content=\"text/html; charset=utf-8\">\r\n</html>\r\n");

        assertThat(message.getSender().getEmailAddress().getAddress()).isEqualTo("o365mc6@microsoft.com");
        assertThat(message.getSender().getEmailAddress().getName()).isEqualTo("Office365 Message Center");

        assertThat(message.getFrom().getEmailAddress().getAddress()).isEqualTo("o365mc6@microsoft.com");
        assertThat(message.getFrom().getEmailAddress().getName()).isEqualTo("Office365 Message Center");

        assertThat(message.getToRecipients().get(0).getEmailAddress().getAddress()).
                isEqualTo("annew@CIE493742.onmicrosoft.com");
        assertThat(message.getToRecipients().get(0).getEmailAddress().getName()).isEqualTo("Anne Weiler");
        assertThat(message.getToRecipients().get(1).getEmailAddress().getAddress()).
                isEqualTo("kepatel@microsoft.com");
        assertThat(message.getToRecipients().get(1).getEmailAddress().getName()).isEqualTo("Keyur Patel");

        assertThat(message.getCcRecipients()).size().isEqualTo(0);
        assertThat(message.getBccRecipients()).size().isEqualTo(0);
        assertThat(message.getReplyTo()).size().isEqualTo(0);

        assertThat(message.getId()).isEqualTo("AAMkADRmZWM1ODE4LWQ4YWItNDlkYS1iZTY4LWVhZWEzYjRlODgzOQBG" +
                "AAAAAAALmV7Xl1RCS6mHDvhC5ayOBwBuW0RzxJhUT4ez5BWQCxI9AAAAAAEMAABuW0RzxJhUT4ez5BWQCxI" +
                "9AABdGgEMAAA=");
    }
}
