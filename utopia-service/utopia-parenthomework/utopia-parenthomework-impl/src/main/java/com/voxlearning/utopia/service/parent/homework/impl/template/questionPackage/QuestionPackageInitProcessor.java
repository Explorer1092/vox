package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePointRef;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.BookQuestionNode;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 初始化教材所有知识点的题
 * @author chongfeng.qi
 * @data 20190126
 */
@Named
@Slf4j
public class QuestionPackageInitProcessor implements HomeworkProcessor {
    @Inject
    private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    LoadingCache<String, BookQuestionNode> cache = CacheBuilder.newBuilder().maximumSize(100).refreshAfterWrite(17, TimeUnit.HOURS).build(
            new CacheLoader<String, BookQuestionNode>() {
           @Override
           public BookQuestionNode load(String bookId) throws Exception {
               return initBookUnit(bookId);
           }
       });

    /**
     * 处理
     *
     * @param hc args
     */
    @Override
    public void process(HomeworkContext hc) {
        long startTime = System.currentTimeMillis();
        HomeworkParam param = hc.getHomeworkParam();
        // 教材id
        String bookId = param.getBookId();
        if (StringUtils.isBlank(bookId)) {
            hc.setMapMessage(MapMessage.errorMessage("教材id为空"));
            return;
        }
        // 教材节点
        BookQuestionNode bookNode = cache.getUnchecked(bookId);
        // 放入Context
        hc.setBookQuestionNode(bookNode);
        LoggerUtils.info("questionPackageInitProcessor.book", bookId, (System.currentTimeMillis() - startTime));
    }

    /**
     * 初始化教材、单元
     *
     * @param bookId
     * @return
     */
    private BookQuestionNode initBookUnit(String bookId){
        // 教材节点
        BookQuestionNode bookNode = HomeWorkCache.load(CacheKey.BOOK_BOX, bookId, "s2");
        if (bookNode != null) {
            return bookNode;
        }
        bookNode = new BookQuestionNode();
        bookNode.setId(bookId);
        bookNode.setBookCatalogType(com.voxlearning.utopia.service.parent.homework.impl.model.BookCatalogType.BOOK);
        //单元
        List<String> bookUnitIds = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT).values().stream().flatMap(Collection::stream).map(NewBookCatalog::getId).collect(Collectors.toList());
        List<BookQuestionNode> nodes = bookUnitIds.stream().map(u->{
            BookQuestionNode node = new BookQuestionNode();
            node.setId(u);
            node.setBookCatalogType(com.voxlearning.utopia.service.parent.homework.impl.model.BookCatalogType.UNIT);
            return node;
        }).collect(Collectors.toList());
        bookNode.setChildNodes(nodes);
        initSection(nodes);
        // 缓存 当前教材的题 720分钟
        HomeWorkCache.set(17 * 24 * 60 * 60, bookNode, CacheKey.BOOK_BOX, bookId, "s2");
        return bookNode;
    }

    /**
     * 初始化课时
     *
     * @param nodes
     * @return
     */
    private void initSection(List<BookQuestionNode> nodes){
        Map<String, List<NewBookCatalog>> newBookCatalogs = newContentLoaderClient.loadChildren(nodes.stream().map(n->n.getId()).collect(Collectors.toList()), BookCatalogType.SECTION);
            nodes.parallelStream().forEach(node-> {
                List<NewBookCatalog> sections = newBookCatalogs.get(node.getId());
                if (CollectionUtils.isEmpty(sections)) {
                    LoggerUtils.info("QuestionPackageInitProcessor.initSection", node);
                    return;
                }
                List<BookQuestionNode> ns = sections.stream().map(s -> {
                BookQuestionNode n = new BookQuestionNode();
                n.setId(s.getId());
                n.setBookCatalogType(com.voxlearning.utopia.service.parent.homework.impl.model.BookCatalogType.SECTION);
                return n;
            }).collect(Collectors.toList());
            node.setChildNodes(ns);
            initKnowledgePoint(ns);
        });
    }

    /**
     * 初始化知识点
     *
     * @param nodes
     * @return
     */
    private void initKnowledgePoint(List<BookQuestionNode> nodes){
        // 获取知识点
        Map<String, NewKnowledgePointRef> newBookCatalogs = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(nodes.stream().map(BookQuestionNode::getId).collect(Collectors.toList()));
        nodes.parallelStream().forEach(node-> {
            NewKnowledgePointRef newKnowledgePointRef = newBookCatalogs.get(node.getId());
            if (newKnowledgePointRef == null) {
                return;
            }
            List<BookQuestionNode> ns = newBookCatalogs.get(node.getId()).getKnowledgePoints().parallelStream().map(s -> {
                BookQuestionNode n = new BookQuestionNode();
                n.setId(s.getId());
                n.setBookCatalogType(com.voxlearning.utopia.service.parent.homework.impl.model.BookCatalogType.KnowledgePoint);
                return n;
            }).collect(Collectors.toList());
            node.setChildNodes(ns);
            initQuestion(ns);
        });
    }

    /**
     * 初始化题
     *
     * @param nodes
     * @return
     */
    private void initQuestion(List<BookQuestionNode> nodes){
        try {
            Map<String, List<NewQuestion>> questionMap = questionLoaderClient.loadQuestionByNewKnowledgePoints0(
                    nodes.stream().map(BookQuestionNode::getId).collect(Collectors.toSet()),
                    QuestionConstants.mentalIncludeContentTypeIds,
                    true,
                    true,
                    true,
                    false,
                    true
            );
            nodes.parallelStream().forEach(node-> {
                List<BookQuestionNode> ns = questionMap.get(node.getId()).stream().filter(q -> !q.isDeletedTrue()).map(s -> {
                    BookQuestionNode n = new BookQuestionNode();
                    n.setId(s.getDocId());
                    n.setSupportForAi(ObjectUtils.get(() -> (int) s.getOthers().get("analysis_support_for_ai"), 0) == 1);
                    return n;
                }).collect(Collectors.toList());
                node.setChildNodes(ns);
            });
        } catch (Exception e) {
            LoggerUtils.info("questionPackageInitProcessor.initQuestion", e.getMessage(), nodes);
        }
    }
}
