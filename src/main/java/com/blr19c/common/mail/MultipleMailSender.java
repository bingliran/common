package com.blr19c.common.mail;


import com.blr19c.common.mail.config.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;

/**
 * 多例邮件发送器
 */
public class MultipleMailSender {

    static IndexMailSender primarySender;
    private final CopyOnWriteArrayList<IndexMailSender> senderList = new CopyOnWriteArrayList<>();

    static synchronized void setPrimarySender(IndexMailSender indexMailSender) {
        if (primarySender == null) {
            primarySender = indexMailSender;
            return;
        }
        throw new IllegalArgumentException("Multiple primary servers");
    }

    public void addJavaMailSender(MailProperties mailProperties) {
        List<String> suffixMatching = mailProperties.getSuffixMatching();
        JavaMailSender sender = ComponentNameSender.getSender(mailProperties);
        IndexMailSender indexMailSender = new IndexMailSender(suffixMatching.toArray(new String[0]), sender);
        senderList.add(indexMailSender);
        if (mailProperties.isPrimary())
            setPrimarySender(indexMailSender);
    }

    /**
     * 根据接收人返回指定的JavaMailSender
     */
    public JavaMailSender getJavaMailSender(final String to) {
        Optional<IndexMailSender> first =
                senderList.stream()
                        .filter(i -> i.isSuffix(to))
                        .sorted()
                        .findFirst();
        if (first.isPresent()) {
            return first.get().getJavaMailSender();
        }
        if (primarySender != null)
            return primarySender.getJavaMailSender();
        throw new IllegalArgumentException("There is no mail service that matches " + to);
    }

    static class IndexMailSender implements Comparator<IndexMailSender> {


        final String[] suffixMatching;
        final JavaMailSender javaMailSender;
        final LongAdder usedCount = new LongAdder();

        IndexMailSender(String[] suffixMatching, JavaMailSender javaMailSender) {
            this.suffixMatching = suffixMatching;
            this.javaMailSender = javaMailSender;
        }

        boolean isSuffix(String to) {
            for (String s : suffixMatching) {
                if (to.endsWith(s))
                    return true;
            }
            return false;
        }

        JavaMailSender getJavaMailSender() {
            usedCount.increment();
            return javaMailSender;
        }

        @Override
        public int compare(IndexMailSender o1, IndexMailSender o2) {
            return (int) (o1.usedCount.longValue() - o2.usedCount.longValue());
        }
    }

    static class ComponentNameSender extends JavaMailSenderImpl {
        private String from;

        static JavaMailSender getSender(MailProperties mailProperties) {
            ComponentNameSender componentNameSender = new ComponentNameSender();
            applyProperties(mailProperties, componentNameSender);
            return componentNameSender;
        }

        static void applyProperties(MailProperties properties, ComponentNameSender sender) {
            sender.setHost(properties.getHost());
            if (properties.getPort() != null) {
                sender.setPort(properties.getPort());
            }
            sender.from = properties.getComponentName();
            sender.setUsername(properties.getUsername());
            sender.setPassword(properties.getPassword());
            sender.setProtocol(properties.getProtocol());
            if (properties.getDefaultEncoding() != null) {
                sender.setDefaultEncoding(properties.getDefaultEncoding().name());
            }
            if (!properties.getProperties().isEmpty()) {
                sender.setJavaMailProperties(asProperties(properties.getProperties()));
            }
        }

        static Properties asProperties(Map<String, String> source) {
            Properties properties = new Properties();
            properties.putAll(source);
            return properties;
        }

        @Override
        protected void doSend(@Nonnull MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
            if (from == null) {
                super.doSend(mimeMessages, originalMessages);
                return;
            }
            try {
                for (MimeMessage mimeMessage : mimeMessages) {
                    if (mimeMessage.getFrom() == null)
                        mimeMessage.setFrom(from);
                }
                super.doSend(mimeMessages, originalMessages);
            } catch (MessagingException e) {
                throw new MailParseException(from + "is not an address", e);
            }
        }
    }
}