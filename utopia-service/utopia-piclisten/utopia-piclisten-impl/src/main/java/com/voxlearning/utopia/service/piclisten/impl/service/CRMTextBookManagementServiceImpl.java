package com.voxlearning.utopia.service.piclisten.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.content.api.NewContentLoader;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.piclisten.api.CRMTextBookManagementService;
import com.voxlearning.utopia.service.piclisten.impl.dao.TextBookManagementDao;
import com.voxlearning.utopia.service.piclisten.impl.version.TextBookManagementVersion;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSdkType;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedTextBookManagementList;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/4/5.
 */
@Named
@Service(interfaceClass = CRMTextBookManagementService.class)
@ExposeService(interfaceClass = CRMTextBookManagementService.class)
public class CRMTextBookManagementServiceImpl implements CRMTextBookManagementService {

    @Inject
    private TextBookManagementDao textBookManagementDao;
    @ImportService(interfaceClass = NewContentLoader.class)
    private NewContentLoader newContentLoader;
    @Inject
    private TextBookManagementVersion textBookManagementVersion;

    public VersionedTextBookManagementList loadVersionedTextBookManagementList() {
        return new VersionedTextBookManagementList(textBookManagementVersion.currentVersion(), $loadTextBooks(), $getPublisherBookList());
    }


    @Override
    public TextBookManagement $loadByBookId(String bookId) {
        return textBookManagementDao.load(bookId, RuntimeMode.current());
    }

    @Override
    public List<TextBookManagement> $loadTextBooks() {
        List<TextBookManagement> bookManagements;
        bookManagements = textBookManagementDao.loadAll(RuntimeMode.current());
        List<String> bookIds = bookManagements.stream().map(TextBookManagement::getBookId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoader.loadBooks(bookIds);
        bookManagements.stream().forEach(e -> {
            NewBookProfile newBookProfile = bookProfileMap.get(e.getBookId());
            if (newBookProfile != null) {
                e.setClazzLevel(newBookProfile.getClazzLevel());
                e.setShortPublisherName(newBookProfile.getShortPublisher());
                e.setTermType(newBookProfile.getTermType());
                e.setSubjectId(newBookProfile.getSubjectId());
            }

        });
        return bookManagements;
    }

    @Override
    public List<TextBookMapper> $getPublisherBookList() {
        List<String> bookIds = $loadTextBooks().stream().map(TextBookManagement::getBookId).collect(Collectors.toList());
        List<TextBookMapper> publisherList = new ArrayList<>();
        Map<String, NewBookProfile> data = newContentLoader.loadBooks(bookIds);
        Map<String, List<NewBookProfile>> newBookProfileMapByPublisher = new HashMap<>();
        if (MapUtils.isNotEmpty(data)) {
            newBookProfileMapByPublisher = data.values().stream()
                    .collect(Collectors.groupingBy(NewBookProfile::getShortPublisher));
        }
        if (MapUtils.isNotEmpty(newBookProfileMapByPublisher)) {
            newBookProfileMapByPublisher.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue())).forEach(entry -> {
                NewBookProfile newBookProfile = entry.getValue().get(0);
                if (newBookProfile != null) {
                    String shortPublisher = newBookProfile.getShortPublisher();
                    String publisherName = newBookProfile.getPublisher();
                    Integer publisherRank = newBookProfile.getPublisherRank();
                    Set<TextBookMapper.ClazzAndTerm> set = new HashSet<>();
                    entry.getValue().stream().forEach(e -> {
                        TextBookMapper.ClazzAndTerm clazzAndTerm = new TextBookMapper.ClazzAndTerm();
                        clazzAndTerm.setClazzLevel(e.getClazzLevel());
                        clazzAndTerm.setTermType(e.getTermType());
                        set.add(clazzAndTerm);
                    });
                    TextBookMapper textBookMapper = new TextBookMapper();
                    textBookMapper.setPublisherShortName(shortPublisher);
                    textBookMapper.setPublisherName(publisherName);
                    textBookMapper.setRank(publisherRank);
                    textBookMapper.setClazzAndTerms(set);
                    publisherList.add(textBookMapper);
                }
            });
        }
        return publisherList.stream().sorted(Comparator.comparingInt(TextBookMapper::getRank)).collect(Collectors.toList());
    }

    @Override
    public Boolean removeBook(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return Boolean.FALSE;
        }
        Boolean remove = textBookManagementDao.remove(bookId, RuntimeMode.current());
        if (remove) {
            textBookManagementVersion.increase();
        }
        return remove;

    }

    @Override
    public Boolean removeBookIgnoreEnv(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return Boolean.FALSE;
        }
        Boolean remove = textBookManagementDao.remove(bookId);
        return remove;
    }

    @Override
    public List<TextBookManagement> $loadAllIgnoreEnv() {
        return textBookManagementDao.loadAllIgnoreEnv();
    }

    @Override
    public void initSdkInfo() {
        List<TextBookManagement> textBookManagements = textBookManagementDao.loadAll(Mode.PRODUCTION);
        textBookManagements = textBookManagements.stream().map(t -> {
            String sdkBookId = t.picListenSdkBookId();
            if (StringUtils.isNotBlank(sdkBookId)){
                TextBookManagement.SdkInfo sdkInfo = new TextBookManagement.SdkInfo();
                sdkInfo.setSdkType(TextBookSdkType.waiyan);
                sdkInfo.setSdkBookId(sdkBookId);
                t.getPicListenConfig().setSdkInfo(sdkInfo);
            }else {
                t.getPicListenConfig().setSdkInfo(TextBookManagement.SdkInfo.NONE_SDK);
            }
            return t;
        }).collect(Collectors.toList());
        textBookManagements.forEach( t -> textBookManagementDao.upsert(t, Mode.PRODUCTION));
    }


    @Override
    public TextBookManagement $upsertTextBook(TextBookManagement textBookManager) {
        if (textBookManager == null) {
            return null;
        }

        TextBookManagement upserted = textBookManagementDao.upsert(textBookManager, RuntimeMode.current());
        if (upserted != null) {
            textBookManagementVersion.increase();
            return textBookManager;
        }
        return null;
    }
}
