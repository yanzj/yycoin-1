package com.china.center.oa.publics.manager.impl;

/**
 * Created by user on 2016/4/8.
 */
import com.sun.mail.imap.IMAPMessage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;
import java.io.*;
import java.security.Security;
import java.util.Enumeration;
import java.util.Properties;


public class FetchingEmail {

    private static final String IMAP = "imap";

    public static void main(String[] args) throws Exception{
//        receiveQQEmail("imap.163.com","kingofhawks@163.com", "grace1121");
        receiveQQEmail("imap.qq.com","kingofhawks@qq.com", "grace_1121");
    }

    public static void receiveQQEmail(String host, String username, String password) throws Exception {
        String port = "993";

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties props = System.getProperties();
        props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.port", port);
        props.setProperty("mail.smtp.starttls.enable", "true");

        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", port);
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.auth.plain.disable", "true");
        props.setProperty("mail.imap.auth.login.disable", "true");
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(true);
        Store store = session.getStore(IMAP);
        Folder inbox = null;

        try {
            store.connect(host, username, password);
            inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_ONLY);
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
//            Message[] messages = inbox.getMessages();
            //only receive unread mails
            Message messages[] = inbox.search(new FlagTerm(new Flags(
                    Flags.Flag.SEEN), false));
            inbox.fetch(messages, profile);
            System.out.println("***mail count***" + messages.length);
            System.out.println("***unread mail count***" + inbox.getUnreadMessageCount());

            IMAPMessage msg;
            for (Message message : messages) {
                msg = (IMAPMessage) message;
                Flags flags = message.getFlags();
                if (flags.contains(Flags.Flag.SEEN)){
                    System.out.println("这是一封已读邮件");
                    continue;
                }
                else {
                    System.out.println("未读邮件");
                }
                String from = decodeText(msg.getFrom()[0].toString());
                InternetAddress ia = new InternetAddress(from);
                System.out.println("FROM:" + ia.getPersonal() + '(' + ia.getAddress() + ')');
                System.out.println("TITLE:" + msg.getSubject());
                System.out.println("SIZE:" + msg.getSize());
                System.out.println("DATE:" + msg.getSentDate());
                Enumeration headers = msg.getAllHeaders();
                System.out.println("----------------------allHeaders-----------------------------");
                while (headers.hasMoreElements()) {
                    Header header = (Header) headers.nextElement();
                    System.out.println(header.getName() + " ======= " + header.getValue());
                }
                parseMultipart((Multipart) msg.getContent());
            }

        } catch(Exception e){
           e.printStackTrace();
        } finally {
            try {
                if (inbox != null) {
                    inbox.close(false);
                }
            } catch (Exception ignored) {
            }
            try {
                store.close();
            } catch (Exception ignored) {
            }
        }
    }

    static String decodeText(String text) throws UnsupportedEncodingException {
        if (text == null)
            return null;
        if (text.startsWith("=?GB") || text.startsWith("=?gb"))
            text = MimeUtility.decodeText(text);
        else
            text = new String(text.getBytes("ISO8859_1"));
        return text;
    }


    /**
     * �Ը����ʼ��Ľ���
     *
     * @param multipart
     * @throws MessagingException
     * @throws IOException
     */
    public static void parseMultipart(Multipart multipart) throws MessagingException, IOException {
        int count = multipart.getCount();
        System.out.println("***parseMultipart count =  " + count);
        for (int idx = 0; idx < count; idx++) {
            BodyPart bodyPart = multipart.getBodyPart(idx);
            System.out.println(bodyPart.getContentType());
            System.out.println("***fileName***"+bodyPart.getFileName());
            if (bodyPart.isMimeType("text/plain")) {
                System.out.println("plain................." + bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                System.out.println("html..................." + bodyPart.getContent());
            } else if (bodyPart.isMimeType("multipart/*")) {
                Multipart mpart = (Multipart) bodyPart.getContent();
                parseMultipart(mpart);
            } else if (bodyPart.isMimeType("application/octet-stream")) {
                String disposition = bodyPart.getDisposition();
                System.out.println("***disposition***"+disposition);
                if (BodyPart.ATTACHMENT.equalsIgnoreCase(disposition)) {
                    String fileName = bodyPart.getFileName();
                    System.out.println("****fileName***"+fileName);
                    InputStream is = bodyPart.getInputStream();
                    copy(is, new FileOutputStream("D:\\" + fileName));
                }
            }
        }
    }

    /**
     * �ļ����������û����и������ص�ʱ�򣬿��԰Ѹ�����InputStream�����û���������
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[1024];
        int len;
        while ((len = is.read(bytes)) != -1) {
            os.write(bytes, 0, len);
        }
        if (os != null)
            os.close();
        is.close();
    }
}