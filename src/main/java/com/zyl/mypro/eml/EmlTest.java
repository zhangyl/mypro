package com.zyl.mypro.eml;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.james.mime4j.dom.*;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.field.ContentTypeFieldLenientImpl;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.stream.Field;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;

public class EmlTest {

    public static void main(String[] args) {

        try (InputStream inputStream = new FileInputStream("C:/Users/pi/Desktop/fp/a.eml")) {
            Message message = Message.Builder.of(inputStream).build();
            EmlEntry entry = new EmlEntry();
            entry.setMessage(message);
            entry.setMessageId(message.getMessageId());
            entry.setSubject(message.getSubject());
            entry.setFrom(address2String(message.getFrom()));
            entry.setTo(address2List(message.getTo()));
            entry.setCc(address2List(message.getCc()));
            entry.setBcc(address2List(message.getBcc()));
            TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("GMT"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(timeZone);
            entry.setDateTime(sdf.format(message.getDate()));
            MultipartImpl body = (MultipartImpl) message.getBody();
            List<Entity> bodyParts = body.getBodyParts();
            //邮件附件和内容
            outputContentAndAttachments(bodyParts , entry);
            System.out.println(JSON.toJSONString(entry , true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归处理邮件附件（附件区域附件、内容中的base64图片附件）、邮件内容（纯文本、html富文本）
     * @param bodyParts 邮件内容体
     * @param entry 数据对象
     * @throws IOException 异常处理
     */
    private static void outputContentAndAttachments(List<Entity> bodyParts , EmlEntry entry) throws IOException {
        for (Entity bodyPart : bodyParts) {
            Body bodyContent = bodyPart.getBody();
            String dispositionType = bodyPart.getDispositionType();
            if (ContentDispositionField.DISPOSITION_TYPE_ATTACHMENT.equals(dispositionType)) {
                //正常的附件文件
                BinaryBody binaryBody = (BinaryBody) bodyContent;
                entry.getAttachments().add(MutableTriple.of(bodyPart.getFilename() , binaryBody.size() , binaryBody.getInputStream()));
                continue;
            }
            if (bodyContent instanceof TextBody) {
                //纯文本内容
                TextBody textBody = (TextBody) bodyContent;
                ContentTypeFieldLenientImpl contentType = (ContentTypeFieldLenientImpl) bodyPart.getHeader().getField(HttpHeaders.CONTENT_TYPE);
                String mimeType = contentType.getMimeType();
                //可动态获取内容的编码，按编码转换
                if (MediaType.PLAIN_TEXT_UTF_8.toString().startsWith(mimeType)) {
                    entry.setTextContent(IOUtils.toString(textBody.getReader()));
                }
                if (MediaType.HTML_UTF_8.toString().startsWith(mimeType)) {
                    entry.setHtmlContent(IOUtils.toString(textBody.getReader()));
                }
            } else if (bodyContent instanceof Multipart) {
                MultipartImpl multipart = (MultipartImpl) bodyContent;
                outputContentAndAttachments(multipart.getBodyParts() , entry);
            } else if (bodyContent instanceof BinaryBody) {
                BinaryBody binaryBody = (BinaryBody) bodyContent;
                outputContentInAttachment(bodyPart.getHeader(), binaryBody, entry);
            } else {
                System.err.println("【是否还存在未覆盖到的其它内容类型场景】？");
            }
        }
    }

    /**
     * 处理内容中的图片附件
     *
     * @param header      附件头信息对象
     * @param binaryBody  附件对象
     * @param entry 解析数据对象
     */
    private static void outputContentInAttachment(Header header, BinaryBody binaryBody, EmlEntry entry) throws IOException {
        Field contentIdField = header.getField(FieldName.CONTENT_ID);
        Field typeField = header.getField(FieldName.CONTENT_TYPE);
        if (typeField instanceof ContentTypeField) {
            ContentTypeField contentTypeField = (ContentTypeField) typeField;
            if (contentTypeField.getMediaType().startsWith(MediaType.ANY_IMAGE_TYPE.type())) {
                try (InputStream inputStream = binaryBody.getInputStream()) {
                    String base64 = Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
                    String cid = StringUtils.substringBetween(contentIdField.getBody(), "<", ">");
                    String content = StringUtils.replace(entry.getHtmlContent(),
                            "cid:" + cid, "data:" + contentTypeField.getMimeType() + ";base64," + base64);
                    entry.setHtmlContent(content);
                }
            }
        }
    }

    /**
     * 转换邮件联系人至String
     * @param addressList 邮件联系人
     * @return String数据
     */
    private static String address2String(MailboxList addressList) {
        if (addressList == null) {
            return StringUtils.EMPTY;
        }
        for (Address address : addressList) {
            return address.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 转换邮件联系人至list集合
     * @param addressList 邮件联系人
     * @return list集合
     */
    private static List<Pair<String , String>> address2List(AddressList addressList) {
        List<Pair<String , String>> list = Lists.newArrayList();
        if (addressList == null) {
            return list;
        }
        for (Address address : addressList) {
            Mailbox mailbox = (Mailbox) address;
            list.add(Pair.of(mailbox.getName() , mailbox.getAddress()));
        }
        return list;
    }
}
