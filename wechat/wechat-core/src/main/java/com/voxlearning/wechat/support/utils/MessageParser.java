/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.support.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.lang.mapper.xml.XmlObjectMapper;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.context.ReplyMessageContext;
import com.voxlearning.wechat.support.Article;
import com.voxlearning.wechat.support.Articles;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Xin Xin
 * @since 10/16/15
 */
@Slf4j
public class MessageParser {
    private static final Map<String, Method> setMethods = new HashMap<>();  //保存一下MessageContext里的method
    private static final Map<String, Method> getMethods = new HashMap<>();  //保存一下MessageContext里的method
    private static final String DELIMIT = "_";

    static {
        Class clz = MessageContext.class;
        List<String> methodNames = new ArrayList<>();

        Field[] fields = clz.getDeclaredFields();
        for (Field f : fields) {
            methodNames.add(f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));
        }

        Method[] ms = clz.getMethods();
        for (Method m : ms) {
            if (m.getName().startsWith("set") && m.getParameterTypes().length == 1 && methodNames.contains(m.getName().substring(3))) {
                setMethods.put(m.getName().substring(3), m);
            } else if (m.getName().startsWith("get") && m.getParameterTypes().length == 0 && methodNames.contains(m.getName().substring(3))) {
                getMethods.put(m.getName().substring(3), m);
            }
        }

        //ReplyMessageContext
        for (Field f : ReplyMessageContext.class.getDeclaredFields()) {
            methodNames.add(f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));
        }

        for (Method m : ReplyMessageContext.class.getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0 && methodNames.contains(m.getName().substring(3))) {
                getMethods.put(m.getName().substring(3), m);
            }
        }
    }

    /**
     * 将消息文本解析成MessageContext
     */
    public static MessageContext parse(String msg) throws DocumentException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
        Objects.requireNonNull(msg, "msg must not be null.");

        Class clazz = MessageContext.class;
        Object obj = clazz.newInstance();

        XmlMapper objectMapper = XmlObjectMapper.newObjectMapper();
        Map<String, Object> msgMap = objectMapper.readValue(msg, Map.class);
        for (Map.Entry entry : msgMap.entrySet()) {
            try {
                invoke(obj, entry, null);
            } catch (UtopiaRuntimeException ex) {
                log.warn(ex.getMessage() + "," + msg);
            }
        }
        return (MessageContext) obj;
    }

    private static void invoke(Object context, Map.Entry entry, Map.Entry parentEntry) throws InvocationTargetException, IllegalAccessException {
        if (entry.getValue() instanceof Map) {
            for (Map.Entry e : ((Map<String, Object>) entry.getValue()).entrySet()) {
                invoke(context, e, entry);
            }
        } else {
            //如果parent不为null，则set方法名组成为 setParentName_ElementName
            Method method = setMethods.get((null == parentEntry ? "" : parentEntry.getKey() + DELIMIT) + entry.getKey());
            if (null == method) {
                log.warn("New field found:" + (null == parentEntry ? "" : parentEntry.getKey() + DELIMIT) + entry.getKey());
                return;
//                throw new UtopiaRuntimeException("New field found:"+(null == parentEntry ? "" : parentEntry.getKey() + DELIMIT) + entry.getKey());
            }

            Object value = valueParse(entry.getValue().toString(), method.getParameterTypes()[0]);
            if (null == value) {
                //说明不是基本类型
                setMethods.get((null == parentEntry ? "" : parentEntry.getKey() + DELIMIT) + entry.getKey()).invoke(context, MessageType.of(entry.getValue().toString()));
            } else {
                setMethods.get((null == parentEntry ? "" : parentEntry.getKey() + DELIMIT) + entry.getKey()).invoke(context, value);
            }
        }
    }

    private static Object valueParse(String value, Class<?> clz) {
        switch (clz.getName()) {
            case "java.lang.String":
                return value;
            case "java.lang.Long":
                return Long.valueOf(value);
            case "java.lang.Double":
                return Double.valueOf(value);
            case "java.lang.Integer":
                return Integer.valueOf(value);
            case "java.math.BigDecimal":
                return Integer.valueOf(value);
        }
        return null;
    }

    /**
     * 将消息对象解析成XML文本
     */
    public static String parse(MessageContext context) throws InvocationTargetException, IllegalAccessException {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("xml");

        for (String key : getMethods.keySet()) {
            Method method = getMethods.get(key);
            if (null != method.invoke(context)) {
                build(root, key, method.invoke(context));
            }
        }

        return doc.asXML();
    }

    private static void build(Element root, String path, Object value) {
        Objects.requireNonNull(root, "root must not be null.");
        Objects.requireNonNull(path, "path must not be null.");
        Objects.requireNonNull(value, "value must not be null.");

        String[] nodes = path.split(DELIMIT);
        Element currentNode = root;
        for (String node : nodes) {
            Element n = currentNode.element(node);
            if (null == n) {
                n = currentNode.addElement(node);
            }
            currentNode = n;
        }
        if (value instanceof MessageType) {
            currentNode.setText(((MessageType) value).getType());
        } else if (value instanceof Articles) {
            //回复的图文消息，突然出来一个小写的<item>，先单独处理
            List<Article> articles = ((Articles) value).getArticles();
            for (Article article : articles) {
                appendArticle(currentNode, article);
            }
        } else {
            currentNode.setText(value.toString());
        }
    }

    private static void appendArticle(Element node, Article article) {
        Element item = node.addElement("item");

        Element title = item.addElement("Title");
        title.setText(article.getTitle());

        Element description = item.addElement("Description");
        description.setText(article.getDescription());

        Element picUrl = item.addElement("PicUrl");
        picUrl.setText(article.getPicUrl());

        Element url = item.addElement("Url");
        url.setText(article.getUrl());
    }

    public static List<String> analyze(String content) {
        IKAnalyzer ikAnalyzer = new IKAnalyzer(true);
        TokenStream tokenStream = ikAnalyzer.tokenStream("", new StringReader(content));
        CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
        List<String> keywords = new ArrayList<>();
        try {
            while (tokenStream.incrementToken()) {
                keywords.add(term.toString());
            }
        } catch (IOException e) {
            log.error("IKAnalyzer get tokens error,msg:{}", content, e);
        }
        return keywords;
    }
}
