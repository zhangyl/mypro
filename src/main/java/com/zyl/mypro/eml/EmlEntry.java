package com.zyl.mypro.eml;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.james.mime4j.dom.Message;

import java.io.InputStream;
import java.util.List;

@Data
public class EmlEntry {

    /**
     * 原始message对象
     */
    @JSONField(serialize = false)
    private Message message;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 纯文本邮件内容
     */
    private String textContent;

    /**
     * 富文本邮件内容
     */
    private String htmlContent;

    /**
     * 邮件附件
     */
    private List<MutableTriple<String , Long , InputStream>> attachments = Lists.newArrayList();

    /**
     * 发件人
     */
    private String from;

    /**
     * 收件人
     */
    private List<Pair<String , String>> to;

    /**
     * 抄送人
     */
    private List<Pair<String , String>> cc;

    /**
     * 密送人
     */
    private List<Pair<String , String>> bcc;

    /**
     * 邮件时间
     */
    private String dateTime;

}
