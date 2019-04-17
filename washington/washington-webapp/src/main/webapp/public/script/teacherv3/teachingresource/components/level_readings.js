$(function(){
    /**
     *
     * 阅读绘本
     */
    var levelReadings = {
        template : template("T:LEVEL_READINGS",{}),
        data : function(){
            return {
                isLoading: false,
                noNetWork : false,
                isShowIcon :{
                    clazzLevels : false,
                    topics : false,
                    series : false
                },
                themeHeight : 38,
                recommendList : [],
                choiceList : [],
                pictureBookList : [],
                levelIds : "",
                topicIds : [],
                seriesIds : [],
                focusCategoryGroupIndex : 0,
                isShowAll : [],
                pageList : [],
                pageNum : 1,
                pageSize : 18,
                pageCount : 0,
                pageShow : 10 //显示几个数字的页码
            };
        },
        props : {
            domain : {
                type : String,
                default : ""
            },
            imgDomain : {
                type : String,
                default : ""
            },
            env : {
                type : String,
                default : ""
            },
            bookId : {
                type : String,
                default : ""
            },
            unitId : {
                type : String,
                default : ""
            },
            sectionId : {
                type : String,
                default : ""
            },
            type : {
                type : String,
                default : ""
            },
            subject : {
                type : String,
                default : ""
            },
            springSwitch : {
                type : Boolean,
                default : ""
            }
        },
        watch : {
            unitId : function(newUnitId,oldUnitId){
                if(this.subject === "ENGLISH" && (newUnitId !== oldUnitId)){
                    this.pageNum = 1;
                    this.recommendContent();
                    this.wholeContent();
                }
            },
            sectionId : function(newsectionId,oldsectionId){
                if(this.subject === "CHINESE" && (newsectionId !== oldsectionId)){
                    this.pageNum = 1;
                    this.recommendContent();
                    this.wholeContent();
                }
            }
        },
        methods : {
            recommendContent : function(){
                var vm = this;
                var params = {
                    bookId  : vm.bookId,
                    unitId  : vm.unitId,
                    sectionId : vm.sectionId,
                    type    : vm.type,
                    subject : vm.subject
                }
                $.get("/teacher/teachingresource/content.vpage",params).done(function(res){
                    var content = res.content || [];
                    var choiceList;
                    if(res.success){
                        choiceList = content.filter(function(obj,index){
                            var recommendModuleFlag = (obj.module === "all");
                            return recommendModuleFlag;
                        });
                        content = content.filter(function(obj,index){
                            var recommendModuleFlag = (obj.module !== "all");
                            if(recommendModuleFlag){
                                obj["beginPos"] = 0;  //起始位置
                                obj["moveCount"] = 3;
                                obj["totalCount"] = obj.pictureBookList.length;
                            }
                            return recommendModuleFlag;
                        });
                    }else{
                        content = [];
                    }
                    vm.isLoading = true;
                    vm.choiceList = choiceList;
                    vm.recommendList = content;
                    if(vm.recommendList.length<=0){
                        vm.focusCategoryGroupIndex = 1;
                        vm.wholeContent();
                        vm.noResources = true;
                    }else{
                        vm.focusCategoryGroupIndex = 0;
                        vm.noResources = false;
                    }
                    // vm.$emit("content-message",vm.getMessageObject(res.success && content.length > 0,(content.length === 0 ? "暂无内容数据" : res.info)));
                }).fail(function(e){
                    vm.recommendList = [];
                    // vm.$emit("content-message",vm.getMessageObject(false,e.message));
                    vm.noNetWork = true;
                });
            },
            wholeContent : function(){
                var vm = this;
                var noNetWork;
                var params = {
                    bookId  : vm.bookId,
                    unitId  : vm.unitId,
                    sectionId : vm.sectionId,
                    type    : "ALL_LEVEL_READINGS",
                    subject : vm.subject,
                    pageNum : vm.pageNum,
                    pageSize : vm.pageSize,
                    levelReadingsClazzLevel : vm.levelIds,
                    topicIds : vm.topicIds.join(','),
                    seriesIds : vm.seriesIds.join(',')
                }
                $.get("/teacher/teachingresource/content.vpage",params).done(function(res){
                    var content = res.content.pictureBookList || [];
                    vm.pageCount = res.content.pageCount;
                    vm.pageList = vm.showPage();
                    vm.pictureBookList = content;
                    vm.isLoading = true;

                    vm.isShowFun();
                    $(window).resize(function () {
                        vm.isShowIcon ={
                            clazzLevels : false,
                            topics : false,
                            series : false
                        };
                        vm.isShowFun();
                    });
                    // vm.$emit("content-message",vm.getMessageObject(res.success && content.length > 0,(content.length === 0 ? "暂无内容数据" : res.info)));
                }).fail(function(e){
                    vm.pictureBookList = [];
                    // vm.$emit("content-message",vm.getMessageObject(false,e.message));
                    vm.noNetWork = true;
                });
            },
            categoryGroupClick: function(index){
                var vm =  this;
                if(vm.focusCategoryGroupIndex === index){
                    return false;
                }
                vm.focusCategoryGroupIndex = index;
                if(vm.focusCategoryGroupIndex === 1){
                    vm.wholeContent();
                }
                // vm.focusCategoryGroupIndex === 0 ?this.recommendContent():this.wholeContent();
            },
            isShowFun : function(){
                var vm = this;
                var clazzLevelsHeight = $(".J_filter-clazzLevels dd").height();
                var topicsHeight = $(".J_filter-topics dd").height();
                var seriesHeight = $(".J_filter-series dd").height();
                var themeHeight = vm.themeHeight
                if(clazzLevelsHeight>themeHeight) vm.isShowIcon.clazzLevels = true;
                if(topicsHeight>themeHeight) vm.isShowIcon.topics = true;
                if(seriesHeight>themeHeight) vm.isShowIcon.series = true;
            },
            showAll : function(type){
                var vm = this;
                var arr = vm.isShowAll;
                if (arr.indexOf(type) > -1) {
                    arr.splice(arr.indexOf(type), 1);
                } else {
                    arr.push(type);
                }
                vm.isShowAll = arr;
            },
            addOrCancelLevel : function(id){
                var vm = this;
                vm.levelIds = id;
                vm.pageNum = 1;
                vm.wholeContent();
            },
            addOrCancelTopic : function(id){
                var vm = this;
                var arr = vm.topicIds;
                if (arr.indexOf(id) > -1) {
                    arr.splice(arr.indexOf(id), 1);
                } else {
                    arr.push(id);
                }
                vm.topicIds = arr;
                vm.pageNum = 1;
                vm.wholeContent();
            },
            addOrCancelSeries : function(id){
                var vm = this;
                var arr = vm.seriesIds;
                if (arr.indexOf(id) > -1) {
                    arr.splice(arr.indexOf(id), 1);
                } else {
                    arr.push(id);
                }
                vm.seriesIds = arr;
                vm.pageNum = 1;
                vm.wholeContent();
            },
            unlimitClick : function(type){
                var vm = this;
                switch (type){
                    case "clazzLevels":
                        vm.levelIds = "";
                        break;
                    case "topic":
                        vm.topicIds = [];
                        break;
                    case "series":
                        vm.seriesIds = [];
                        break;
                    default:
                        break;
                }
                vm.wholeContent();
            },
            showPage : function(){
                var vm = this;
                var newPage = [];
                var pageNum= vm.pageNum;
                var pageCount = vm.pageCount;
                var pageShow = vm.pageShow;
                if(pageNum>=(pageCount-(pageShow/2))){
                    for(var i=pageCount-(pageShow-2);i<=pageCount;i++){
                        newPage.push(i);
                    }
                }else if(pageNum<(pageCount-((pageShow/2)-1))&&pageNum>=(pageCount-(pageShow/2))){
                    for(var i=pageNum-((pageShow/2)-1);i<pageNum+((pageShow/2)-1);i++){
                        newPage.push(i);
                    }
                }else if(pageNum<=(pageShow/2)){
                    for(var i=1;i<pageShow;i++){
                        newPage.push(i);
                    }
                }else{
                    for(var i=pageNum-((pageShow/2)-1);i<pageNum+((pageShow/2)-1);i++){
                        newPage.push(i);
                    }
                }
                return newPage;
            },
            pageClick : function(pageNo){
                var vm = this;
                vm.pageNum = pageNo;
                vm.wholeContent();
            },
            previousPage : function(){
                var vm = this;
                if(vm.pageNum<=1) return;
                vm.pageNum-=1;
                vm.wholeContent();
            },
            nextPage : function(){
                var vm = this;
                if(vm.pageNum>=vm.pageCount) return;
                vm.pageNum+=1;
                vm.wholeContent();
            },
            arrowLeftClick : function(moduleObj){
                var oldBeginPos = moduleObj.beginPos;
                if(oldBeginPos <= 0){
                    return false;
                }
                var beginPos = (oldBeginPos - moduleObj.moveCount);
                moduleObj.beginPos = (beginPos < 0 ? 0 : beginPos);
            },
            arrowRightClick : function(moduleObj){
                var oldBeginPos = moduleObj.beginPos;
                var beginPos = (oldBeginPos + moduleObj.moveCount);
                if(beginPos >= moduleObj.totalCount){
                    return false;
                }
                moduleObj.beginPos = beginPos;
            },
            previewReading : function(reading){
                this.$emit("preview-reading",{
                    pictureBookId : reading.pictureBookId
                });
            },
            refreshPage : function(){
                this.recommendContent();
                this.wholeContent();
            }
        },
        created : function(){
            // this.categoryGroupClick(0);
            var vm = this;
            vm.recommendContent();
            $17.voxLog({
                module: "m_yvkU37oY9J",
                op : "pc_home_one_form_page_load",
                s0 : vm.subject,
                s1 : vm.bookId,
                s2 : vm.unitId,
                s3 : {sectionId:vm.sectionId,type:vm.type}
            });
            $17.info("levelReadings created....");
        },
        mounted : function(){
            $17.info("levelReadings mounted....");
        },
        beforeDestroy : function(){
            $17.info("levelReadings beforeDestroy....");
        },
        destroyed : function () {
            $17.info("levelReadings destroyed....");
        }
    };

    $17.teachingresource = $17.teachingresource || {};
    $17.extend($17.teachingresource, {
        levelReadings   : levelReadings
    });
});