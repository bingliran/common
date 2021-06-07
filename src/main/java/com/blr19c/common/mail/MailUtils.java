package com.blr19c.common.mail;

import com.blr19c.common.mail.model.Attachment;
import com.blr19c.common.mail.model.Inline;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 邮件发送服务
 *
 * @author blr
 */
public class MailUtils {
    private static MultipleMailSender multipleMailSender;

    public static void setMultipleMailSender(MultipleMailSender multipleMailSender) {
        MailUtils.multipleMailSender = multipleMailSender;
    }

    /**
     * 邮件发送
     *
     * @param subject 标题
     * @param text    内容
     * @param to      接收人
     */
    public static void send(String subject, String text, String... to) {
        String[][] toGroup = groupTo(to);
        for (String[] ts : toGroup) {
            doSend(subject, text, ts, null, null);
        }
    }

    /**
     * 邮件发送
     *
     * @param subject 标题
     * @param text    内容
     * @param to      接收人
     * @param inlines html内嵌图片
     */
    public static void send(String subject, String text, Inline[] inlines, String... to) {
        String[][] toGroup = groupTo(to);
        for (String[] ts : toGroup) {
            doSend(subject, text, ts, inlines, null);
        }
    }

    /**
     * 邮件发送
     *
     * @param subject     标题
     * @param text        内容
     * @param to          接收人
     * @param attachments 附件
     */
    public static void send(String subject, String text, Attachment[] attachments, String... to) {
        String[][] toGroup = groupTo(to);
        for (String[] ts : toGroup) {
            doSend(subject, text, ts, null, attachments);
        }
    }

    /**
     * 邮件发送
     *
     * @param subject     标题
     * @param text        内容
     * @param to          接收人
     * @param inlines     html内嵌图片
     * @param attachments 附件
     */
    public static void send(String subject, String text, Inline[] inlines, Attachment[] attachments, String... to) {
        String[][] toGroup = groupTo(to);
        for (String[] ts : toGroup) {
            doSend(subject, text, ts, inlines, attachments);
        }
    }

    /**
     * 邮件发送
     *
     * @param subject     标题
     * @param text        内容
     * @param to          接收人
     * @param inlines     html内嵌图片
     * @param attachments 附件
     */
    private static void doSend(String subject, String text, String[] to, Inline[] inlines, Attachment[] attachments) {
        if (to.length == 0)
            throw new IllegalArgumentException("请设置接收人");
        try {
            JavaMailSender mailSender = multipleMailSender.getJavaMailSender(to[0]);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(text, true);
            if (inlines != null)
                for (Inline inline : inlines) {
                    messageHelper.addInline(inline.getContentId(), getAttachmentDataSource(inline, messageHelper));
                }
            if (attachments != null)
                for (Attachment attachment : attachments) {
                    messageHelper.addAttachment(attachment.getFileName(), getAttachmentDataSource(attachment, messageHelper));
                }
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 附件转为 DataSource
     */
    private static DataSource getAttachmentDataSource(Attachment attachment, MimeMessageHelper helper) {
        if (attachment.getDataSource() != null)
            return attachment.getDataSource();
        if (attachment.getFile() != null) {
            FileDataSource dataSource = new FileDataSource(attachment.getFile());
            dataSource.setFileTypeMap(helper.getFileTypeMap());
            return dataSource;
        }
        if (attachment.getInputStream() != null) {
            return new DataSource() {
                @Override
                public InputStream getInputStream() {
                    return attachment.getInputStream();
                }

                @Override
                public OutputStream getOutputStream() {
                    throw new UnsupportedOperationException("只读资源");
                }

                @Override
                public String getContentType() {
                    return attachment.getContentType();
                }

                @Override
                public String getName() {
                    return attachment.getFileName();
                }
            };
        }
        throw new IllegalArgumentException("附件缺失文件");
    }


    private static String[][] groupTo(String... toList) {
        if (toList.length == 1)
            return new String[][]{new String[]{toList[0]}};
        return Arrays.stream(toList)
                .collect(Collectors.groupingBy(s -> "@" + s.split("@")[1]))
                .values()
                .stream()
                .map(l -> l.toArray(new String[0]))
                .toArray(String[][]::new);
    }
}
