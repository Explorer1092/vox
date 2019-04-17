package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.piclisten.support.PiclistenBookImgUtils;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookShelf;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-10-30 8:12 PM
 **/
@Controller
@RequestMapping(value = "/parentMobile/piclisten/")
@Slf4j
public class MobileParentPiclistenController extends AbstractMobileParentSelfStudyController {


    @RequestMapping(value = "/publisher/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage publisherList() {
        User parent = currentParent();
        if (parent == null){
            return noLoginResult;
        }
        List<TextBookMapper> publisherList = textBookManagementLoaderClient.getPublisherList();
        List<Map<String, Object>> publisherMapList = publisherList.stream().map(t -> {
            Map<String, Object> map = new HashMap<>(2);
            map.put("publisher_id", t.getPublisherShortName());
            map.put("publisher_name", t.getPublisherName());
            return map;
        }).collect(Collectors.toList());

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> allMap = new HashMap<>();
        allMap.put("publisher_id", "全部");
        allMap.put("publisher_name", "全部教材");
        list.add(allMap);
        list.addAll(publisherMapList);
        return MapMessage.successMessage().add("publisher_list", list);

    }


    @RequestMapping(value = "/book/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookList() {
        User parent = currentParent();
        if (parent == null){
            return noLoginResult;
        }
        String sys = getRequestString("sys");
        long studentId = currentRequestStudentId();
        BookListQuery query = buildBookListQuery();

        List<PicListenBookShelf> picListenBookShelves = parentSelfStudyService.loadParentPicListenBookShelf(parent.getId());

        Map<String, PicListenBookPayInfo> picListenBookPayInfoMap = picListenCommonService.userBuyBookPicListenLastDayMap(parent, false);

        //第一次进入，什么条件都没传，找默认
        if (query == null){
            query = defaultQuery(picListenBookPayInfoMap, studentId);
        }
        if (query == null){
            return MapMessage.errorMessage("初始化查询失败了！");
        }

        return query(query, picListenBookShelves, picListenBookPayInfoMap, sys, parent);

    }

    private MapMessage query(BookListQuery query,
                             List<PicListenBookShelf> picListenBookShelves,
                             Map<String, PicListenBookPayInfo> picListenBookPayInfoMap,
                             String sys,
                             User parent) {

        List<TextBookManagement> textBookList = textBookManagementLoaderClient.getTextBookManagementByClazzLevel(query.getClazzLevel());
        if (!"全部".equals(query.getShortPublisherName())){

        }
        textBookList = textBookList.stream().filter(t -> {
            if (!"全部".equals(query.getShortPublisherName())) {
                if( !query.getShortPublisherName().equals(t.getShortPublisherName())){
                    return false;
                }
            }
            if (t.getTermType() != query.getTerm().getKey()){
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        Set<String> bookIdSet = textBookList.stream().map(TextBookManagement::getBookId).collect(Collectors.toSet());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdSet);
        Map<String, List<TextBookManagement>> publisherBookListMap = textBookList.stream().filter(t -> bookProfileMap.get(t.getBookId()) != null)
                .collect(Collectors.groupingBy(t -> bookProfileMap.get(t.getBookId()).getPublisher()));
        Map<String, Integer> publisherRankMap = textBookManagementLoaderClient.getPublisherList().stream().collect(Collectors.toMap(TextBookMapper::getPublisherName, t -> {
            if (t == null) {
                return 99999;
            }
            return SafeConverter.toInt(t.getRank(), 99999);
        }));

        Set<String> shelfBookIdSet = picListenBookShelves.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toSet());

        Boolean auth = picListenCommonService.userIsAuthForPicListen(parent);
        List<Map<String, Object>> puhlihserMapList = new ArrayList<>(publisherBookListMap.size());
        publisherBookListMap.forEach((publisherName, bookManagerList) -> {
            if (CollectionUtils.isEmpty(bookManagerList)){
                return;
            }
            Map<String, Object> publisherMap = new HashMap<>();
            publisherMap.put("publisher_id", bookManagerList.get(0).getShortPublisherName());
            publisherMap.put("publisher_name", publisherName);
            List<Map<String, Object>> bookMapList = new ArrayList<>(bookManagerList.size());
            for (TextBookManagement bookManagement : bookManagerList) {
                NewBookProfile newBookProfile = bookProfileMap.get(bookManagement.getBookId());
                if (newBookProfile == null){
                    continue;
                }
                Subject subject = bookManagement.fetchBookSubject();
                Map<String, Object> bookMap = new HashMap<>(10);
                bookMap.put("book_id", bookManagement.getBookId());
                bookMap.put("subject", subject.name());
                bookMap.put("subject_name", subject.getValue());
                bookMap.put("clazz_level_name", ClazzLevel.parse(bookManagement.getClazzLevel()).getDescription());
                bookMap.put("clazz_level", bookManagement.getClazzLevel());
                bookMap.put("book_list_name", bookManagement.getBookListName());
                bookMap.put("term", Term.of(bookManagement.getTermType()).getValue());
                bookMap.put("cover_url", PiclistenBookImgUtils.getCompressBookImg(newBookProfile.getImgUrl()));
                String lable = fuckingLabel(bookManagement, shelfBookIdSet, picListenBookPayInfoMap, sys, auth);
                bookMap.put("label", lable );
                bookMapList.add(bookMap);
            }
            publisherMap.put("book_list", bookMapList);
            puhlihserMapList.add(publisherMap);
        });
        puhlihserMapList.sort((o1, o2) -> {
            String shortPublisher1 = SafeConverter.toString(o1.get("publisher_name"));
            Integer order1 = SafeConverter.toInt(publisherRankMap.get(shortPublisher1), 99999);

            String shortPublisher2 = SafeConverter.toString(o2.get("publisher_name"));
            Integer order2 = SafeConverter.toInt(publisherRankMap.get(shortPublisher2), 99999);

            return Integer.compare(order1, order2);
        });
        return MapMessage.successMessage().add("clazz_level", query.getClazzLevel())
                .add("term", query.getTerm().getKey())
                .add("publisher_id", query.getShortPublisherName())
                .add("pub_book_list", puhlihserMapList);
    }
    private String fuckingLabel(TextBookManagement bookManagement,
                                Set<String> shelfBookIdSet,
                                Map<String, PicListenBookPayInfo> picListenBookPayInfoMap,
                                String sys,
                                boolean isAuth){
        String learning = "正在学";
        String added = "已添加";

        boolean onShelf = shelfBookIdSet.contains(bookManagement.getBookId());
        Boolean hasPiclisten = textBookManagementLoaderClient.picListenShow(bookManagement.getBookId(), sys, isAuth);
        Boolean picListenBookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookManagement.getBookId());
        boolean payed = picListenBookPayInfoMap.containsKey(bookManagement.getBookId());
        if (onShelf){
            if (hasPiclisten){
                if (picListenBookNeedPay){
                    if (payed){
                        return learning;
                    }else {
                        return added;
                    }
                }else {
                    return learning;
                }
            }else {
                return added;
            }
        }else {
            if (hasPiclisten && picListenBookNeedPay && payed){
                return learning;
            }
        }
        return "";
    }

    private BookListQuery defaultQuery(Map<String, PicListenBookPayInfo> picListenBookPayInfoMap, long studentId){
        //如果用户购买过教材
        if (MapUtils.isNotEmpty(picListenBookPayInfoMap)){
            //随便取一本已购教材，用这个教材的 出版社，年级，学期做筛选条件
            Map.Entry<String, PicListenBookPayInfo> payInfoEntry = picListenBookPayInfoMap.entrySet().stream().findAny().orElse(null);
            if (payInfoEntry == null){
                return null;
            }
            return initQueryByBook(payInfoEntry.getKey());
        }else {
            //用户没购买过教材
            BookListQuery query = new BookListQuery();
            Term currentTerm = SchoolYear.newInstance().currentTerm();
            query.setTerm(currentTerm);
            //如果去得到用户的年级，取不到就是3年级
            if (studentId != 0L){
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                if (studentDetail != null){
                    Integer levelAsInteger = studentDetail.getClazzLevelAsInteger();
                    if (levelAsInteger != null && levelAsInteger > 0 && levelAsInteger <= 6){
                        Boolean isAuth = picListenCommonService.userIsAuthForPicListen(currentUser());
                        List<String> bookIds = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, getRequestString("sys"), isAuth, null, Subject.ENGLISH);
                        Map<String, TextBookManagement> textBookByIds = textBookManagementLoaderClient.getTextBookByIds(bookIds);
                        if (MapUtils.isNotEmpty(textBookByIds)){
                            TextBookManagement bookManagement = textBookByIds.values().stream().filter(t -> t.getTermType().equals(currentTerm.getKey())).findFirst().orElse(null);
                            if (bookManagement != null){
                                query.setShortPublisherName(bookManagement.getShortPublisherName());
                                query.setClazzLevel(bookManagement.getClazzLevel());
                                return query;
                            }
                        }
                    }
                }
            }
            query.setClazzLevel(3);
            query.setShortPublisherName("全部");
            return query;
        }
    }

    private BookListQuery initQueryByBook(String bookId){
        TextBookManagement textBook = textBookManagementLoaderClient.getTextBook(bookId);
        if (textBook == null){
            return null;
        }
        return new BookListQuery(textBook.getClazzLevel(), textBook.getTermType(), textBook.getShortPublisherName());
    }

    private BookListQuery buildBookListQuery(){
        int clazzLevel = getRequestInt("clazz_level");
        int term = getRequestInt("term");
        String shortPubliserName = getRequestString("publisher_id");
        if (clazzLevel == 0  && term == 0 && StringUtils.isBlank(shortPubliserName)){
            return null;
        }
        return new BookListQuery(clazzLevel, term, shortPubliserName);
    }

    @Data
    private class BookListQuery{
        private Integer clazzLevel;
        private Term term;
        private String shortPublisherName;

        public BookListQuery(int clazzLevel, int term, String shortPubliserName) {
            if (clazzLevel > 0 && clazzLevel <= 6){
                this.clazzLevel = clazzLevel;
            }
            if (term != 0 ){
                this.term = Term.of(term);
            }
            if (StringUtils.isNotBlank(shortPubliserName)){
                this.shortPublisherName = shortPubliserName;
            }
        }

        public BookListQuery() {

        }
    }

}
