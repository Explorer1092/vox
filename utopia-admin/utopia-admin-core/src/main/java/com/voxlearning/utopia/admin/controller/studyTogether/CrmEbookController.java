package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.galaxy.service.ebook.api.DPCrmEbookService;
import com.voxlearning.galaxy.service.ebook.api.constant.BookType;
import com.voxlearning.galaxy.service.ebook.api.entity.BookPage;
import com.voxlearning.galaxy.service.ebook.api.entity.Ebook;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/7/10
 */
@Controller
@RequestMapping("opmanager/studyTogether/ebook")
@Slf4j
public class CrmEbookController extends AbstractStudyTogetherController {


    @StorageClientLocation(storage = "17-pmc")
    private StorageClient imgStorageClient;

    @ImportService(interfaceClass = DPCrmEbookService.class)
    private DPCrmEbookService dpCrmEbookService;

    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;

    /**
     * 进入书页内容详情页
     */
    @RequestMapping(value = "/pageDetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String pageDetail(Model model) {
        String pageId = getRequestString("pageId");
        String bookId = getRequestString("bookId");
        Integer bookType = getRequestInt("bookType");
        model.addAttribute("bookType", bookType);
        if (StringUtils.isBlank(pageId)) {
            model.addAttribute("bookId", bookId);
            return "/opmanager/studyTogether/ebookPageDetail";
        }
        BookPage bookPageById = dpCrmEbookService.getBookPageById(pageId);
        model.addAttribute("audioUrl", bookPageById.getAudioUrl());
        model.addAttribute("pageNum", bookPageById.getPageNum());
        model.addAttribute("id", bookPageById.getId());
        model.addAttribute("bookId", bookPageById.getBookId());
        model.addAttribute("contentImg_url", getOssImgUrl(bookPageById.getContentImg()));
        model.addAttribute("contentImg_file", bookPageById.getContentImg());
        model.addAttribute("textContent", bookPageById.getTextContents());
        model.addAttribute("comment", bookPageById.getComment());
        model.addAttribute("pageType", bookPageById.getPageType());
        return "/opmanager/studyTogether/ebookPageDetail";
    }


    /**
     * 书页列表
     */
    @RequestMapping(value = "/pageList.vpage", method = RequestMethod.GET)
    public String pageList(Model model) {
        String bookId = getRequestString("selectBookId");
        String searchBookId = getRequestString("searchBookId");
        if (StringUtils.isNotBlank(searchBookId)) {
            bookId = searchBookId;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        List<Ebook> ebookIdAndTitle = dpCrmEbookService.getEbookIdAndTitle();
        model.addAttribute("bookMap", ebookIdAndTitle);
        if (StringUtils.isBlank(bookId)) {
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPage", 1);
            return "/opmanager/studyTogether/ebookPageList";
        }
        List<BookPage> bookPageList = dpCrmEbookService.getBookPageByBookId(bookId);
        String finalBookId = bookId;
        Integer bookType = ebookIdAndTitle.stream().filter(e -> StringUtils.equals(finalBookId, e.getId())).map(Ebook::getBookType).findFirst().orElse(0);
        model.addAttribute("bookId", bookId);
        model.addAttribute("bookType", bookType);
        if (CollectionUtils.isEmpty(bookPageList)) {
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPage", 1);
            return "/opmanager/studyTogether/ebookPageList";
        }
        bookPageList = bookPageList.stream().sorted(Comparator.comparingInt(BookPage::getPageNum)).collect(Collectors.toList());
        Page<BookPage> bookPages = PageableUtils.listToPage(bookPageList, pageRequest);
        model.addAttribute("content", bookPages.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", bookPages.getTotalPages());
        model.addAttribute("hasPrev", bookPages.hasPrevious());
        model.addAttribute("hasNext", bookPages.hasNext());

        return "/opmanager/studyTogether/ebookPageList";
    }


    /**
     * 书籍列表
     */
    @RequestMapping(value = "/bookList.vpage", method = RequestMethod.GET)
    public String bookList(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String bookId = getRequestString("searchBookId");
        if (StringUtils.isNotBlank(bookId)) {
            Ebook ebookById = dpCrmEbookService.getEbookById(bookId);
            if (ebookById == null) {
                model.addAttribute("currentPage", pageNum);
                model.addAttribute("totalPage", 1);
                return "/opmanager/studyTogether/ebookList";
            } else {
                Page<Ebook> books = PageableUtils.listToPage(Collections.singletonList(ebookById), pageRequest);
                model.addAttribute("currentPage", pageNum);
                model.addAttribute("totalPage", 1);
                model.addAttribute("content", books.getContent());
                return "/opmanager/studyTogether/ebookList";
            }
        }
        Page<Ebook> ebookByPage = dpCrmEbookService.getEbookByPage(pageRequest);
        if (CollectionUtils.isEmpty(ebookByPage.getContent())) {
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPage", 1);
            return "/opmanager/studyTogether/ebookList";
        }
        model.addAttribute("content", ebookByPage.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", ebookByPage.getTotalPages());
        model.addAttribute("hasPrev", ebookByPage.hasPrevious());
        model.addAttribute("hasNext", ebookByPage.hasNext());
        model.addAttribute("bookId", bookId);
        return "/opmanager/studyTogether/ebookList";
    }


    /**
     * 进入书籍详情页
     */
    @RequestMapping(value = "/bookDetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String bookDetail(Model model) {
        String bookId = getRequestString("bookId");
        List<String> lessonIds = getAllLessonId();
        model.addAttribute("lessonIds", lessonIds);
        model.addAttribute("bookTypes", BookType.getBookTypes());
        if (StringUtils.isBlank(bookId)) {
            return "/opmanager/studyTogether/ebookDetail";
        }
        Ebook ebook = dpCrmEbookService.getEbookById(bookId);
        model.addAttribute("id", ebook.getId());
        model.addAttribute("lessonId", ebook.getLessonId());
        model.addAttribute("title", ebook.getTitle());
        model.addAttribute("coverImg_url", getOssImgUrl(ebook.getCoverImg()));
        model.addAttribute("coverImg_file", ebook.getCoverImg());
        model.addAttribute("bookIdList", ebook.getBookPageList());
        model.addAttribute("selectBookType", ebook.getBookType());
        model.addAttribute("comment", ebook.getComment());
        return "/opmanager/studyTogether/ebookDetail";
    }

    /**
     * 检查bookId
     */
    @RequestMapping(value = "/checkBookId.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkBookId() {
        String bookId = getRequestString("bookId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("bookId不能为空");
        }
        Ebook ebookById = dpCrmEbookService.getEbookById(bookId);
        if (ebookById == null) {
            return MapMessage.errorMessage("bookId错误");
        }
        return MapMessage.successMessage();
    }

    /**
     * 保存页面详细信息
     */
    @RequestMapping(value = "/saveBookPageDetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveBookPageDetail() {
        String bookId = getRequestString("bookId");
        String pageId = getRequestString("pageId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("bookId不能为空");
        }
        String contentImg = getRequestString("contentImg");
        String contentAudio = getRequestString("contentAudio");
        String textContentArray = getRequestString("textContentArray");
        Integer pageNum = getRequestInt("pageNum");
        Integer pageType = getRequestInt("pageType");
        String comment = getRequestString("comment");
        List<BookPage.PageTextContent> pageTextContents = JsonUtils.fromJsonToList(textContentArray, BookPage.PageTextContent.class);
        if (CollectionUtils.isEmpty(pageTextContents) && (pageType == 1 || pageType == 3)) {
            return MapMessage.errorMessage("书页文本内容错误");
        }
        Ebook ebookById = dpCrmEbookService.getEbookById(bookId);
        if (ebookById == null) {
            return MapMessage.errorMessage("未找到相应的书籍");
        }
        BookPage bookPage = new BookPage();
        //换书用
        String oldBookId = "";
        //换页码、删页码用
        Integer oldPageNum = -1;
        if (StringUtils.isNotBlank(pageId)) {
            bookPage = dpCrmEbookService.getBookPageById(pageId);
            oldBookId = bookPage.getBookId();
            oldPageNum = SafeConverter.toInt(bookPage.getPageNum());
        }
        bookPage.setPageType(pageType);
        bookPage.setPageNum(pageNum);
        bookPage.setBookId(bookId);
        if (StringUtils.isNotBlank(contentImg)) {
            bookPage.setContentImg(contentImg);
        }
        if (StringUtils.isNotBlank(contentAudio)) {
            bookPage.setAudioUrl(contentAudio);
        }
        if (CollectionUtils.isNotEmpty(pageTextContents)) {
            bookPage.setTextContents(pageTextContents);
        }
        if (StringUtils.isNotBlank(comment)) {
            bookPage.setComment(comment);
        }
        bookPage = dpCrmEbookService.saveBookPage(bookPage);
        if (bookPage == null) {
            return MapMessage.errorMessage("保存失败");
        }
        //新建
        if (oldPageNum == -1 && bookPage.getPageNum() != 0) {
            List<Ebook.PageInfo> bookPageIdList = CollectionUtils.isNotEmpty(ebookById.getBookPageList()) ? ebookById.getBookPageList() : new ArrayList<>();
            Ebook.PageInfo pageInfo = new Ebook.PageInfo();
            pageInfo.setPageId(bookPage.getId());
            pageInfo.setUpdateDate(bookPage.getUpdateDate());
            bookPageIdList = checkPageAdd(bookPageIdList, pageInfo, bookPage.getPageNum());
            if (CollectionUtils.isEmpty(bookPageIdList)) {
                bookPage.setPageNum(0);
                dpCrmEbookService.saveBookPage(bookPage);
                return MapMessage.errorMessage().add("page_error", "页码错误，请按顺序添加页码，已将页码设置成未设置状态");
            }
            changePageNum(bookPageIdList, ebookById.getId());
            return MapMessage.successMessage();
        }
        //改页码、删页码-不换书
        List<Ebook.PageInfo> checkList = new ArrayList<>();
        if (oldPageNum != -1 && !oldPageNum.equals(pageNum) && StringUtils.equals(oldBookId, bookPage.getBookId())) {
            List<Ebook.PageInfo> bookPageIdList = ebookById.getBookPageList();
            Ebook.PageInfo pageInfo = new Ebook.PageInfo();
            pageInfo.setPageId(bookPage.getId());
            if (CollectionUtils.isNotEmpty(bookPageIdList)) {
                bookPageIdList.remove(pageInfo);
//                ebookById.setBookPageList(bookPageIdList);
            }
            if (bookPage.getPageNum() != 0) {
                bookPageIdList = CollectionUtils.isNotEmpty(ebookById.getBookPageList()) ? ebookById.getBookPageList() : new ArrayList<>();
                pageInfo.setUpdateDate(bookPage.getUpdateDate());
                checkList = checkPageAdd(bookPageIdList, pageInfo, bookPage.getPageNum());
                if (CollectionUtils.isEmpty(checkList)) {
                    bookPage.setPageNum(0);
                    dpCrmEbookService.saveBookPage(bookPage);
                    changePageNum(bookPageIdList, ebookById.getId());
                    return MapMessage.errorMessage().add("page_error", "页码错误，请按顺序添加页码，已将页码设置成未设置状态");
                }
            }
            changePageNum(checkList, ebookById.getId());
            return MapMessage.successMessage();
        }
        //换书
        if (oldPageNum != -1 && !StringUtils.equals(oldBookId, bookPage.getBookId())) {


            Ebook.PageInfo pageInfo = new Ebook.PageInfo();
            pageInfo.setPageId(bookPage.getId());
            Ebook oldBook = dpCrmEbookService.getEbookById(oldBookId);
            List<Ebook.PageInfo> bookPageIdList = oldBook.getBookPageList();
            bookPageIdList.remove(pageInfo);
            changePageNum(bookPageIdList, oldBookId);
            //不删页码
            if (bookPage.getPageNum() != 0) {
                pageInfo.setUpdateDate(bookPage.getUpdateDate());
                bookPageIdList = ebookById.getBookPageList();
                checkList = checkPageAdd(bookPageIdList, pageInfo, bookPage.getPageNum());
                if (CollectionUtils.isEmpty(checkList)) {
                    bookPage.setPageNum(0);
                    dpCrmEbookService.saveBookPage(bookPage);
                    changePageNum(bookPageIdList, ebookById.getId());
                    return MapMessage.errorMessage().add("page_error", "页码错误，请按顺序添加页码，已将页码设置成未设置状态");
                }
                changePageNum(checkList, ebookById.getId());
            }
            return MapMessage.successMessage();
        }
        changePageUTById(ebookById, bookPage.getId(), bookPage.getUpdateDate());
        return MapMessage.successMessage();
    }


    /**
     * 保存书籍详细信息
     */
    @RequestMapping(value = "/saveBookDetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveBookDetail() {
        String bookId = getRequestString("bookId");
        String coverImg = getRequestString("coverImg");
        if (StringUtils.isBlank(coverImg)) {
            return MapMessage.errorMessage("封面不能为空");
        }
        String title = getRequestString("title");
        String lessonId = getRequestString("lessonId");
        String pageIdArray = getRequestString("pageIdArray");
        Integer bookType = getRequestInt("bookType");
        String comment = getRequestString("comment");
        List<String> pageIds = JsonUtils.fromJsonToList(pageIdArray, String.class);
        Ebook book = new Ebook();
        List<Ebook.PageInfo> oldPageIds = new ArrayList<>();
        if (StringUtils.isNotBlank(bookId)) {
            book = dpCrmEbookService.getEbookById(bookId);
            oldPageIds = book.getBookPageList();
        }
        book.setTitle(title);
        book.setLessonId(lessonId);
        book.setCoverImg(coverImg);
        book.setBookType(bookType);
        book.setComment(comment);
        //检查id是否正确并赋值
        if (CollectionUtils.isNotEmpty(pageIds)) {
            List<BookPage> pages = dpCrmEbookService.getBookPageIdAndPageNumByIds(pageIds);
            if (CollectionUtils.isEmpty(pages)) {
                return MapMessage.errorMessage("页面id错误，请检查");
            }
            Map<String, BookPage> pageMap = pages.stream().collect(Collectors.toMap(BookPage::getId, Function.identity()));
            List<Ebook.PageInfo> pageInfos = new ArrayList<>();
            for (String pageId : pageIds) {
                if (!pageMap.keySet().contains(pageId)) {
                    return MapMessage.errorMessage("页面id错误，错误id为" + pageId);
                }
                Ebook.PageInfo pageInfo = new Ebook.PageInfo();
                pageInfo.setPageId(pageId);
                pageInfo.setUpdateDate(pageMap.get(pageId).getUpdateDate());
                pageInfos.add(pageInfo);
            }
            book.setBookPageList(pageInfos);
        }
        Ebook ebook = dpCrmEbookService.saveEbook(book);
//        //把删除的拿出来，做下删除
        if (CollectionUtils.isNotEmpty(ebook.getBookPageList())) {
            if (CollectionUtils.isNotEmpty(oldPageIds)) {
                List<Ebook.PageInfo> removeList = oldPageIds.stream().filter(e -> !ebook.getBookPageList().contains(e)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(removeList)) {
                    removeList.forEach(e -> {
                        BookPage bookPage = new BookPage();
                        bookPage.setId(e.getPageId());
                        bookPage.setPageNum(0);
                        dpCrmEbookService.saveBookPage(bookPage);
                    });
                }
            }
            //保存页码改动，这里保存主要是为了对齐updateTime
            changePageNum(ebook.getBookPageList(), ebook.getId());
        }
        return MapMessage.successMessage();
    }


    /**
     * 上传图片
     */
    @RequestMapping(value = "/uploadImg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImg(MultipartFile inputFile) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = "ebookImg/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "ebookImg/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String realName = imgStorageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("imgName", realName).add("imgUrl", fileUrl);
    }

    /**
     * 根据id取书籍
     */
    @RequestMapping(value = "/get_ebook_by_id.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getEbookById() {
        String bookId = getRequestString("bookId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("error");
        }
        Ebook ebookById = dpCrmEbookService.getEbookById(bookId);
        if (ebookById == null) {
            return MapMessage.errorMessage("error");
        }
        return MapMessage.successMessage().add("ebookType", ebookById.getBookType());
    }

    private String getOssImgUrl(String relativeUrl) {
        if (StringUtils.isBlank(relativeUrl)) {
            return "";
        }
        return ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host") + relativeUrl;
    }


    private void changePageNum(List<Ebook.PageInfo> list, String bookId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Integer pageNum = 1;
        List<Ebook.PageInfo> newPagelist = new ArrayList<>();
        for (Ebook.PageInfo page : list) {
            BookPage bookPage = new BookPage();
            bookPage.setId(page.getPageId());
            bookPage.setPageNum(pageNum);
            BookPage upsertPage = dpCrmEbookService.saveBookPage(bookPage);
            page.setUpdateDate(upsertPage.getUpdateDate());
            newPagelist.add(page);
            pageNum++;
        }
        Ebook ebook = new Ebook();
        ebook.setId(bookId);
        ebook.setBookPageList(newPagelist);
        dpCrmEbookService.saveEbook(ebook);
    }

    //在修改页面信息时更新ut
    private void changePageUTById(Ebook book, String pageId, Date updateTime) {
        if (book == null || StringUtils.isBlank(pageId)) {
            return;
        }
        List<Ebook.PageInfo> bookPageList = book.getBookPageList();
        Boolean isUpdate = false;
        if (CollectionUtils.isNotEmpty(bookPageList)) {
            for (Ebook.PageInfo pageInfo : bookPageList) {
                if (StringUtils.equals(pageId, pageInfo.getPageId())) {
                    pageInfo.setUpdateDate(updateTime);
                    isUpdate = true;
                }
            }
            if (isUpdate) {
                book.setBookPageList(bookPageList);
                dpCrmEbookService.saveEbook(book);
            }
        }
    }

    private List<Ebook.PageInfo> checkPageAdd(List<Ebook.PageInfo> bookPageIdList, Ebook.PageInfo pageInfo, Integer pageNum) {
        if (bookPageIdList == null || pageInfo == null || pageNum == 0) {
            return Collections.emptyList();
        }
        if (pageNum == bookPageIdList.size() + 1) {
            bookPageIdList.add(pageInfo);
            return bookPageIdList;
        }
        if (pageNum <= bookPageIdList.size()) {
            bookPageIdList.add(pageNum - 1, pageInfo);
            return bookPageIdList;
        }
        return Collections.emptyList();
    }
}
