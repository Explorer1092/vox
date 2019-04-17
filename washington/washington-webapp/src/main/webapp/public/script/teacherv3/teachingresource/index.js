$(function () {
    //菜单模块
    /***
     *
     * menuList结构,最多二级结构：
     *  [{
     *     id : "",
     *     name : "",
     *     childrens:[{
     *        id : name,
     *        name : ""
     *     }]
     *  }]
     */
    var menuPanel = {
        template : template("T:UNIT_LIST",{}),
        data : function(){
            return {
                oneLevelId : "",  //一级ID
                twoLevelId : ""   //二级ID
            };
        },
        watch : {
            menuList : function(newValue,oldValue){
                var vm = this;
                var menuList = newValue;
                var oldOneLevelId = this.oneLevelId;
                var oldTwoLevelId = this.twoLevelId;
                var newItem,newSubItem;  //新的一级菜单和二级菜单对象
                if(menuList.length > 0){
                    var tempMenuList = menuList.filter(function(menuObj){
                        return !!menuObj.selected;
                    });
                    newItem = tempMenuList.length > 0 ? tempMenuList[0] : menuList[0];
                    if(newItem.childrens.length > 0){
                        var tempSubItem = newItem.childrens.filter(function(subObj){
                            return !!subObj.selected;
                        });
                        newSubItem = tempSubItem.length > 0 ? tempSubItem[0] : newItem.childrens[0];
                    }
                }
                $17.info("watch-->" + (newItem && newItem.id) + "<<>>" + (newSubItem && newSubItem.id));
                this.oneLevelId = newItem ? newItem.id : "";
                this.twoLevelId = newSubItem ? newSubItem.id : "";

                //menuList变化后，oneLevelId或twoLevelId变化时，触发相应事件
                if(newItem && oldOneLevelId !== newItem.id){
                    vm.$emit("onelevel-click",newItem)
                }
                if(newSubItem && oldTwoLevelId !== newSubItem.id){
                    vm.$emit("twolevel-click",newSubItem);
                }
            }
        },
        props : {
            menuList : {
                type : Array,
                default : function(){ return [];}
            }
        },
        methods : {
            oneLevelClick : function(item,index){
                var vm = this;
                vm.oneLevelId = item.id;
                vm.$emit("onelevel-click",item);
            },
            twoLevelClick : function(item,index){
                var vm = this;
                vm.twoLevelId = item.id;
                vm.$emit("twolevel-click",item);
            }
        },
        beforeCreate : function(){
            $17.info("menuList beforeCreate...");
        },
        created : function(){
            $17.info("menuList created...");
        },
        mounted : function(){
            $17.info("menuList mounted...");
        }
    };

    //学科选择
    var subjectChoiceList = {
        template: template("T:CHOICE_SUBJECT_BOX", {}),
        data: function () {
            return {};
        },
        props: {
            showPanel: {
                type: Boolean,
                default: false
            },
            subject: {
                type: String,
                default: ""
            },
            subjectList: {
                type: Array,
                default: function () {
                    return [];
                }
            }
        },
        methods: {
            changeSubject: function (index) {
                this.$emit("change-subject", this.subjectList[index]);
            },
            closeSubjectBox: function () {
                this.$emit("close-subject-box");
            }
        },
        created: function () {

        },
        mounted: function () {

        }
    };

    //课本列表选择
    var bookList = {
        template: template("t:CHANGE_BOOK_POPUP", {}),
        data: function () {
            return {
                termList: [{key: 1, name: "上册"}, {key: 2, name: "下册"}],
                levelList: [{level: 1, levelName: "一年级"}, {level: 2, levelName: "二年级"}, {
                    level: 3,
                    levelName: "三年级"
                }, {level: 4, levelName: "四年级"}, {level: 5, levelName: "五年级"}, {level: 6, levelName: "六年级"}],
                bookList : [],
                focusBookId : "",
                focusLevel : this.level,
                focusTerm : this.term,
                termLevelBookMap : {}  //存放以学期+年级为键，课本列表为值的缓存
            }
        },
        props: {
            showPanel: {
                type: Boolean,
                default: false
            },
            level: {
                type: Number,
                default: 1
            },
            term: {
                type: Number,
                default: 1
            },
            subject : {
                type : String,
                default : ""
            }
        },
        watch : {
            subject : function(newValue,oldValue){
                (newValue !== oldValue) && this.fetchBookList();
            },
            focusLevel : function(newValue,oldValue){
                (newValue !== oldValue) && this.fetchBookList();
            },
            focusTerm : function(newValue,oldValue){
                (newValue !== oldValue) && this.fetchBookList();
            }
        },
        methods: {
            fetchBookList: function () {
                var vm = this;
                //todo 增加缓存
                $.get("/teacher/new/homework/sortbook.vpage", {
                    level: vm.focusLevel,
                    term: vm.focusTerm,
                    subject: vm.subject
                }).done(function (res) {
                    if (res.success) {
                        vm.bookList = res.rows;
                    } else {
                        $17.error(res.info || "接口请求错误");
                    }
                }).fail(function () {
                    $17.error("网络错误，请退出页面重试");
                });
            },
            closeBookBox: function () {
                this.$emit("close-book-box");
            },
            levelClick : function(levelObj,index){
                this.focusLevel = levelObj.level;
            },
            termClick : function(termObj,index){
                this.focusTerm = termObj.key;
            },
            confirmSelect : function(){
                var vm = this;
                var bookId = vm.focusBookId;
                //保存选择课本信息
                $.post("/teacher/teachingresource/changebook.vpage",{
                    bookId : bookId,
                    subject: vm.subject
                }).done(function(res){
                    if(res.success){
                        //课本保存成功后，发送确认事件
                        vm.$emit("book-confirm",bookId);
                    }else{
                        $17.error(res.info || "保存课本接口错误");
                    }
                }).fail(function(){
                    $17.error("网络错误，请关闭重试");
                });
            },
            selectBook : function(book,index){
                var vm = this;
                this.focusBookId = book.id;
                this.confirmSelect();
                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_textbook_switch_click",
                    s0 : vm.subject,
                    s1 : book.id
                });
            }
        },
        created: function () {
            $17.info("booklist created....");
            this.fetchBookList();
        },
        mounted: function () {
            $17.info("booklist mounted....");
        }
    };

    //作业类型模块
    var tabTypeList = {
        template : template("T:HOMEWORK_TYPE_LIST",{}),
        data : function(){
            return {
                focusTypeObj : {}
            };
        },
        watch : {
            typeList : function(newTypeList,oldTypeList){
                var vm = this;
                $17.info("---------typeList watch start---------");
                if(newTypeList.length > 0){
                    var type = vm.focusTypeObj.type;
                    var filterTypeList = newTypeList.filter(function(obj,index){
                          return obj.type === type;
                    });
                    if(filterTypeList.length === 0){
                        vm.focusTypeObj = Object.assign({},vm.focusTypeObj,newTypeList[0]);
                    }
                    vm.$emit("tab-click",Object.assign({},vm.focusTypeObj));
                }else{
                    vm.focusTypeObj = {};
                }
                $17.info("---------typeList watch end---------");
            }
        },
        props : {
            typeList : {
                type : Array,
                default : function(){return [];}
            }
        },
        methods : {
            tabClick : function(item){
                var vm = this;
                vm.focusTypeObj = Object.assign({},vm.focusTypeObj,item);
                vm.$emit("tab-click",item);
            }
        },
        created : function(){

        }
    };

    //消息提示模块
    var tipArea = {
        template : template("T:INFO_TIP",{}),
        data : function(){
            return {};
        },
        props : {
            info : {
                type : String,
                default : ""
            }
        }
    };



    var basicApp = $17.teachingresource.basicApp;
    var levelReadings = $17.teachingresource.levelReadings;
    var intelligentTeaching = $17.teachingresource.intelligentTeaching;
    var classCourseWare = $17.teachingresource.classCourseWare;
    var wordRecognitionAndReading = $17.teachingresource.wordRecognitionAndReading;
    var keyPoints = $17.teachingresource.keyPoints;
    var cuoTiBao = $17.teachingresource.cuoTiBao;
    var wordTeachAndPractice = $17.teachingresource.wordTeachAndPractice;
    var naturalSpelling = $17.teachingresource.naturalSpelling;
    var unknownType = $17.teachingresource.unknownType;
    //类型与组件的映射
    var objectConfigTypeH5TagMap = {
        BASIC_APP           : "basic-app",
        LEVEL_READINGS      : "level-readings",
        INTELLIGENT_TEACHING      : "intelligent-teaching",
        CLASS_COURSE_WARE : "class-course-ware",
        WORD_RECOGNITION_AND_READING : "word-recognition-and-reading",
        KEY_POINTS : "key-points",
        CUO_TI_BAO : "cuo-ti-bao",
        WORD_TEACH_AND_PRACTICE : "word-teach-and-practice",
        NATURAL_SPELLING : "natural-spelling",
        UNKNOWN_TYPE        : "unknown-type"
    };

    var constantObj = window.constantObj || {};
    var $uper = window.$uper || {};
    // {subject: "MATH",subjectName: "数学"}
    var subjectList = constantObj.subjectList;
    var ua = navigator.userAgent.toLowerCase();
    new Vue({
        el: "#teachingresource",
        data: {
            userId : $uper.userId || "",
            objectConfigTypeH5TagMap : objectConfigTypeH5TagMap,
            subject: $17.getQuery("subject") || constantObj.subject || "",
            subjectList: subjectList || [],
            showSubjectPanel: false,  //选择学科面板是否显示
            book: {},  //选中的课本
            bookList: [],
            bookListPanel: false,  //课本切换面板是否显示
            typeList : [],  //作业类型列表
            currentType : {},
            envOption : {
                domain : constantObj.env === "test" ? "//www.test.17zuoye.net/" : constantObj.domain,
                imgDomain : constantObj.imgDomain,
                env : constantObj.env,
                categoryIconPrefixUrl : constantObj.categoryIconPrefixUrl
            },
            oneLevelRmi : false,  //当一级菜单改变时触发远程接口调用
            oneLevelObj : {},
            twoLevelRmi : false,   //当二级菜单改变时鉵发远程接口调用
            twoLevelObj : {},
            messageObj : {},  //作业形式及内容部分消息结果提示区域
            ua : ua || "",
            targetPreviewUrl : "", //新窗口打开时的URL
            springSwitch : false, //弹起|回复
            previewWordIdList : [] //要预览的字id
        },
        computed: {
            isDateDu : function(){
                //戴特标识
                return this.ua.indexOf("datedu") !== -1;
            },
            isDateduTeach: function isDateduTeach() {
                //戴特大屏
                return this.ua.indexOf("datedu_teach") !== -1;
            },
            isDateduppt : function(){
                //戴特备课
                return this.ua.indexOf("datedu_ppt") !== -1;
            },
            isEmptySubject : function(){
                return this.subjectList.length === 0;
            },
            subjectName: function () {
                var vm = this;
                var subjectObj = vm.subjectList.find(function (obj) {
                    return obj.subject === vm.subject;
                });
                subjectObj = subjectObj || {};
                return subjectObj.subjectName || "选择学科";
            },
            showShadowLayer: function () {
                var vm = this;
                return vm.showSubjectPanel || vm.bookListPanel;
            },
            menuList : function(){
                var vm = this;
                var book = vm.book || {},moduleList = book.moduleList || [],unitList = book.unitList || [];
                var menuList;
                //获取localStorage中有默认单元
                var defaultBookUnit;
                try{
                    defaultBookUnit = window.localStorage.getItem(vm.userId + "_" + vm.subject);
                    defaultBookUnit = JSON.parse(defaultBookUnit);
                }catch (e) {
                    defaultBookUnit = null;
                }
                if(moduleList.length > 0){
                    vm.oneLevelRmi = false;
                    vm.twoLevelRmi = true;

                    menuList = moduleList.map(function(moduleObj,index){
                        var selectUnitFlag = false;
                        var units = moduleObj.units.map(function(unit){
                            var selectUnit = (defaultBookUnit && defaultBookUnit.bookId === book.bookId && unit.unitId === defaultBookUnit.unitId);
                            selectUnitFlag = selectUnitFlag || selectUnit;
                            return {
                                id : unit.unitId,
                                name : unit.cname,
                                selected : selectUnit
                            };
                        });
                        return {
                            id : 'vu_' + index,
                            name : moduleObj.moduleName,
                            selected : selectUnitFlag,
                            childrens : units
                        }
                    });
                }else{
                    menuList = unitList.map(function(unit,index){
                        var childrens = [];
                        if($.isArray(unit.sections) && unit.sections.length > 0){
                            vm.oneLevelRmi = false;
                            vm.twoLevelRmi = true;
                            childrens = unit.sections.map(function(section){
                                return {
                                    id : section.sectionId,
                                    name : section.cname,
                                    selected : (defaultBookUnit && defaultBookUnit.bookId === book.bookId && unit.unitId === defaultBookUnit.unitId && section.sectionId === defaultBookUnit.sectionId)
                                };
                            });
                        }else{
                            vm.oneLevelRmi = true;
                            vm.twoLevelRmi = false;
                        }
                        return {
                            id : unit.unitId,
                            name : unit.cname,
                            selected : (defaultBookUnit && childrens.length === 0 && defaultBookUnit.bookId === book.bookId && unit.unitId === defaultBookUnit.unitId),
                            childrens : childrens
                        }
                    });
                }
                $17.info("主体计算属性menuList:");
                $17.info(menuList);
                $17.info("主体计算属性menuList------end---");
                return menuList;
            }
        },
        components: {
            subjectChoiceList: subjectChoiceList,
            bookList : bookList,
            menuPanel : menuPanel,
            basicApp : basicApp,
            levelReadings : levelReadings,
            intelligentTeaching : intelligentTeaching,
            classCourseWare : classCourseWare,
            wordRecognitionAndReading : wordRecognitionAndReading,
            keyPoints : keyPoints,
            cuoTiBao : cuoTiBao,
            naturalSpelling : naturalSpelling,
            wordTeachAndPractice : wordTeachAndPractice,
            unknownType : unknownType,
            tabTypeList : tabTypeList,
            tipArea : tipArea
        },
        methods: {
            getComponentTag : function(objectiveConfigType){
                var typeH5TagMap = this.objectConfigTypeH5TagMap;
                return typeH5TagMap[objectiveConfigType] || typeH5TagMap["UNKNOWN_TYPE"];
            },
            subjectClick: function () {
                var vm = this;
                if(vm.subjectList.length <= 1){
                    return false;
                }
                vm.showSubjectPanel = !vm.showSubjectPanel;
            },
            changeSubject: function (item) {
                var vm = this;
                vm.subjectClick();
                vm.subject = item.subject;
                vm.fetchBookUnit();
            },
            fetchBookUnit: function () {
                var vm = this;
                $.get("/teacher/teachingresource/book.vpage", {
                    subject: vm.subject
                }).done(function (res) {
                    if (res.success) {
                        vm.book = res.book;
                        $17.voxLog({
                            module: "m_yvkU37oY9J",
                            op : "pc_home_default_textbook_load",
                            s0 : vm.subject,
                            s1 : vm.book.bookId
                        });
                    } else {
                        //恢复默认值
                        vm.book = {};
                        //弹出选教材弹窗
                        vm.bookNameClick();
                        vm._setTypeList([]);
                        vm.contentLoadingCb({
                            success : false,
                            info    : res.info || "获取课本失败"
                        });
                    }
                }).fail(function (e) {
                    $17.error("fetch book fail....");
                    //恢复默认值
                    vm.book = {};
                    vm.bookNameClick();
                    vm._setTypeList([]);
                    vm.contentLoadingCb({
                        success : false,
                        info    : "课本获取网络错误，请刷新重试"
                    });
                });
            },
            bookNameClick: function () {
                this.bookListPanel = !this.bookListPanel;
            },
            bookConfirmCb : function(){
                this.bookNameClick();
                this.fetchBookUnit();
            },
            oneLevelClickCb : function(item){
                var vm = this;
                //左侧菜单一级点击回调
                $17.info(item);
                $17.info("一级菜单：" + this.oneLevelRmi);
                this.oneLevelObj = $.extend(true,{},item);
                if(this.oneLevelRmi){
                    try{
                        window.localStorage.setItem(vm.userId + "_" + vm.subject,JSON.stringify({
                            bookId : vm.book.bookId,
                            unitId : item.id
                        }));
                    }catch (e) {
                        // ignore
                    }
                    this.fetchTypeList(this.book.bookId,item.id);
                }
            },
            fetchUnitOrSection : function(){
                var oneLevelId = this.oneLevelObj.id || "";
                var item = this.twoLevelObj;
                var prefixOneLevelId = oneLevelId.slice(0,3);
                var unitId,sectionId;
                if(this.twoLevelRmi && prefixOneLevelId === "vu_"){
                    //是虚拟单元
                    unitId = item.id;
                    sectionId = "";
                }else{
                    unitId = oneLevelId;
                    sectionId = item.id;
                }
                return {
                    sectionId : sectionId,
                    unitId : unitId
                };
            },
            twoLevelClickCb : function(item){
                var vm = this;
                //左侧菜单二级点击回调
                $17.info(item);
                this.twoLevelObj = Object.assign({},this.twoLevelObj,item);
                var unitObj = this.fetchUnitOrSection();
                try{
                    window.localStorage.setItem(vm.userId + "_" + vm.subject,JSON.stringify({
                        bookId : vm.book.bookId,
                        unitId : unitObj.unitId,
                        sectionId : unitObj.sectionId
                    }));
                }catch (e) {
                    // ignore
                }
                this.fetchTypeList(this.book.bookId,unitObj.unitId,unitObj.sectionId);
            },
            _setTypeList : function(typeList){
                var vm = this;
                vm.typeList = $.isArray(typeList) ? typeList : [];
            },
            fetchTypeList : function(bookId,unitId,sections){
                var vm = this;
                sections = $.isArray(sections) ? sections.join(",") : (sections || "");
                $.get("/teacher/teachingresource/types.vpage",{
                    bookId : bookId,
                    unitId : unitId,
                    sections : sections,
                    subject : vm.subject
                }).done(function(res){
                    if(!res.success){
                        $17.error("fetchTypeList接口错误");
                    }
                    vm._setTypeList(res.typeList);
                    //发消息
                    if($.isArray(res.typeList) && res.typeList.length === 0){
                        vm.contentLoadingCb({
                            success : false,
                            info    : res.success ? "作业类型为空" : (res.info || "作业形式接口获取失败")
                        });
                    }
                    var sectionId = vm.fetchUnitOrSection().sectionId || "";
                    $17.voxLog({
                        module: "m_yvkU37oY9J",
                        op : "pc_home_form_load",
                        s0 : vm.subject,
                        s1 : vm.book.bookId,
                        s2 : vm.book.unitList[0].unitId,
                        s3 : {sectionId:sectionId,typeList:JSON.stringify(vm.typeList)}
                    });
                }).fail(function(e){
                    $17.error("网络错误");
                    vm._setTypeList([]);
                    vm.contentLoadingCb({
                        success : false,
                        info    : "网络错误,请刷新重试"
                    });
                });
            },
            tabClickCb : function(item){
                //作业类型点击回调 item:{type: "BASIC_APP", typeName: "基础练习"}
                var vm = this;
                //各个作业类型所需参数都在此处添加
                $17.info("tabClickCb.....");
                //重置消息状态
                (!vm.messageObj || !vm.messageObj.success) && vm.contentLoadingCb({
                    success : true,
                    noResources : false,
                    noNetWork : false,
                    isLoading : false
                });
                var unitObj = this.fetchUnitOrSection();
                vm.currentType = Object.assign({},vm.currentType,item,{
                    bookId : vm.book.bookId,
                    unitId : unitObj.unitId,
                    sectionId : unitObj.sectionId,
                    subject : vm.subject,
                    qids : vm.previewWordIdList,
                    isDateppt : vm.isDateduppt,
                    clazzLevel : vm.book.clazzLevel,
                    termType : vm.book.termType,
                    springSwitch : vm.springSwitch
                },vm.envOption);

                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_form_click",
                    s0 : vm.subject
                });
            },
            contentLoadingCb : function(result){
                //作业类型加载状态消息回执
                this.messageObj = Object.assign({},this.messageObj,result);
                $17.info(this.messageObj);
            },
            closePage : function(){
                window.opener = null;
                window.open("", "_self");
                window.close();
            },
            _previewOpen : function(url,type){
                var vm = this;
                type = (typeof type === "string" ? type : String(type));
                if(vm.isDateDu){
                    $17.daite.callCplus('mirco.cotroler', JSON.stringify({'url': url,flash: String(type === "BASIC_APP"),restype: type}), 'openResource');
                }
                if(!vm.isDateduTeach){
                    vm.targetPreviewUrl = url;
                    setTimeout(function(){
                        vm.$refs.targetTag.click();
                    },200);
                }
            },
            previewReadingCb : function(reading){
                var vm = this,envOption = vm.envOption;
                var sectionId = vm.fetchUnitOrSection().sectionId || "";
                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_one_form_preview_page_click",
                    s0 : vm.subject,
                    s1 : vm.book.bookId,
                    s2 : vm.book.unitList[0].unitId,
                    s3 : {sectionId:sectionId,type:vm.currentType.type},
                    s4 : reading.pictureBookId
                });
                var url = envOption.domain + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html?" + $.param({
                    pictureBookIds : reading.pictureBookId,
                    from : "preview"
                });
                vm._previewOpen(url);
            },
            previewTypeCb : function(result){
                var vm = this,envOption = vm.envOption;
                var sectionId = vm.fetchUnitOrSection().sectionId || "";
                var courseId = result.params["courseId"] || "";
                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_one_form_preview_page_click",
                    s0 : vm.subject,
                    s1 : vm.book.bookId,
                    s2 : vm.book.unitList[0].unitId,
                    s3 : {sectionId:sectionId,type:vm.currentType.type},
                    s4 : courseId
                });
                var url = envOption.domain + "/teacher/teachingresource/preview.vpage?url=";
                var resultUrl;
                switch (result.type){
                    case "BASIC_APP":
                        resultUrl = envOption.domain + "/flash/loader/newselfstudywiththirdparty.vpage?" + $.param(result.params);
                        break;
                    case "INTELLIGENT_TEACHING":
                    case "CHINESECHARACTERCULTURE":
                        //预览课程
                        resultUrl = envOption.domain + "/teacher/new/homework/previewteachingcourse.vpage?" + $.param({
                            courseId : result.params["courseId"]
                        });
                        break;
                    case "CLASS_COURSE_WARE":
                        var coursewareUrl = result.params["coursewareUrl"];
                        //预览课件
                        var otherParam = '';
                        if (coursewareUrl.indexOf('v.17xueba.com') > -1) {
                            otherParam = 'i=16939&ssl=1&n=5';
                        } else {
                            otherParam = 'i=16940&ssl=1&n=5';
                        }
                        resultUrl = 'https://ow365.cn/?' + otherParam + '&furl=' + window.encodeURIComponent(coursewareUrl.replace('http://', 'https://'));
                        break;
                    case "IMAGETEXTRHYME":
                        resultUrl = envOption.domain + "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-speak-training/read.vhtml?" + $.param({
                            __p__ : encodeURIComponent(JSON.stringify({
                                doModuleUrl : result.params.doModuleUrl,
                                chapterId : result.params.chapterId
                            }))
                        });
                        break;
                    case "NATURAL_SPELLING":
                    case "ALL_NATURAL_SPELLING":
                        resultUrl = envOption.domain + "/resources/apps/hwh5/funkyspell/V1_0_0/index.vhtml?" + $.param({
                            __p__ : encodeURIComponent(result.params.urlParams)
                        });
                        break;
                    default:
                        break;
                }
                if(!resultUrl){
                    $17.error("preview url error");
                    return false;
                }
                url += encodeURIComponent(resultUrl) + "&type="+result.type;
                vm._previewOpen(url,result.type);
            },
            previewWordCb : function(obj){
                var vm = this,envOption = vm.envOption;
                var sectionId = vm.fetchUnitOrSection().sectionId || "";
                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_one_form_preview_page_click",
                    s0 : vm.subject,
                    s1 : vm.book.bookId,
                    s2 : vm.book.unitList[0].unitId,
                    s3 : {sectionId:sectionId,type:vm.currentType.type},
                    s4 : obj.params.id
                });
                var url = envOption.domain + "/teacher/teachingresource/wordrecognitionandreadingdetail.vpage?" + $.param({
                    // lessonName : lessonName,
                    id : obj.params.id,
                    ids : obj.params.ids.join(',')
                });
                vm._previewOpen(url);
            },
            previewVideoCb : function(obj){
                var vm = this,envOption = vm.envOption;
                var sectionId = vm.fetchUnitOrSection().sectionId || "";
                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_one_form_preview_page_click",
                    s0 : vm.subject,
                    s1 : vm.book.bookId,
                    s2 : vm.book.unitList[0].unitId,
                    s3 : {sectionId:sectionId,type:vm.currentType.type},
                    s4 : obj.id
                });
                var url = envOption.domain + "/teacher/teachingresource/previewvideo.vpage?" + $.param({
                    videoUrl : obj.videoUrl,
                    videoConverUrl : obj.coverUrl
                });
                vm._previewOpen(url);
            },
            previewWordIdCb : function(arr){
                var vm = this;
                vm.previewWordIdList = arr;
            },
            previewWord : function(id,ids){
                var vm = this,envOption = vm.envOption;
                if(!vm.isDateduppt || vm.previewWordIdList.length<=0) return false;
                var sectionId = vm.fetchUnitOrSection().sectionId || "";
                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_one_form_preview_page_click",
                    s0 : vm.subject,
                    s1 : vm.book.bookId,
                    s2 : vm.book.unitList[0].unitId,
                    s3 : {sectionId:sectionId,type:vm.currentType.type},
                    s4 : id
                });
                var url = envOption.domain + "/teacher/teachingresource/wordrecognitionandreadingdetail.vpage?" + $.param({
                    id : id,
                    ids : ids.join(',')
                });
                vm._previewOpen(url);
                setTimeout(function(){
                    vm.clearWord();
                },500);
            },
            clearWord : function(){
                var vm = this;
                vm.previewWordIdList.splice(0,vm.previewWordIdList.length);
            },
            springSwitchClick : function(){
                var vm = this;
                vm.springSwitch = !vm.springSwitch;
                vm.currentType.springSwitch = vm.springSwitch;
                $17.voxLog({
                    module: "m_yvkU37oY9J",
                    op : "pc_home_down_click"
                });
            }
        },
        created: function () {
            $17.info("created....");
            var vm = this;
            if(subjectList.length > 0){
                vm.fetchBookUnit();
            }else{
                vm.contentLoadingCb({
                    success : false,
                    info : "没有学科信息"
                });
            }
            $17.voxLog({
                module: "m_yvkU37oY9J",
                op : "pc_home_load"
            });
        },
        mounted: function () {
            $17.info("mounted....");
        }
    });
});