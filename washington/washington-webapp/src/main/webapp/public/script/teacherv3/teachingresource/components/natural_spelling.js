$(function(){
    /**
     *
     * 自然拼读
     */
    var naturalSpelling = {
        template : template("T:NATURAL_SPELLING",{}),
        data : function(){
            return {
                isLoading: false,
                noNetWork : false,
                levelList : [],
                recommendList : {},//推荐自然拼读
                wholeContentList : [],//全部自然拼读
                focusCategoryGroupIndex : 0,
                focusCategoryGroupIndex2 : 0,
                naturalSpellingLevel : 0,
                pageList : [],
                pageNum : 1,
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
            categoryIconPrefixUrl : {
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
                if(newUnitId !== oldUnitId){
                    this.pageNum = 1;
                    this.focusCategoryGroupIndex2 = 0;
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
                };
                $.get("/teacher/teachingresource/content.vpage",params).done(function(res){
                    var content = res.content || {};
                    if(res.success){
                        if(res.content == null){
                            vm.recommendList = null;
                            vm.focusCategoryGroupIndex = 1;
                        }else{
                            vm.recommendList = content;
                            vm.focusCategoryGroupIndex = 0;
                        }
                    }else{
                        content = {};
                    }
                    vm.isLoading = true;
                    // vm.$emit("content-message",vm.getMessageObject(res.success && content.length > 0,(content.length === 0 ? "暂无内容数据" : res.info)));
                }).fail(function(e){
                    vm.recommendList = [];
                    // vm.$emit("content-message",vm.getMessageObject(false,e.message));
                    vm.noNetWork = true;
                });
            },
            levelContent : function(){
                var vm = this;
                var params = {
                    bookId  : vm.bookId
                }
                $.get("/teacher/teachingresource/naturalspellinglevels.vpage",params).done(function(res){
                    var content = res.levels || [];
                    var info = "";
                    if(res.success){
                        info =  "" ;
                        vm.naturalSpellingLevel= res.levels[vm.focusCategoryGroupIndex2].level;
                        vm.wholeContent();
                    }else{
                        info =  res.info || "接口请求错误";
                    }
                    vm.levelList = content;
                    vm.isLoading = true;
                    // vm.$emit("content-message",vm.getMessageObject(res.success && content.length > 0,(content.length === 0 ? "暂无内容数据" : res.info)));
                }).fail(function(e){
                    vm.levelList = [];
                    // vm.$emit("content-message",vm.getMessageObject(false,e.message));
                    vm.noNetWork = true;
                });
            },
            wholeContent : function(){
                var vm = this;
                var params = {
                    bookId  : vm.bookId,
                    unitId  : vm.unitId,
                    type    : "ALL_NATURAL_SPELLING",
                    subject : vm.subject,
                    naturalSpellingLevel : vm.naturalSpellingLevel
                }
                $.get("/teacher/teachingresource/content.vpage",params).done(function(res){
                    var content = res.content.content || [];
                    vm.pageCount = res.content.content.length;
                    vm.pageList = vm.showPage();
                    vm.wholeContentList = content;
                    vm.isLoading = true;
                    // vm.$emit("content-message",vm.getMessageObject(res.success && content.length > 0,(content.length === 0 ? "暂无内容数据" : res.info)));
                }).fail(function(e){
                    vm.wholeContentList = [];
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
            },
            categoryGroupClick2: function(index){
                var vm =  this;
                if(vm.focusCategoryGroupIndex2 === index){
                    return false;
                }
                vm.focusCategoryGroupIndex2 = index;
                vm.naturalSpellingLevel= vm.levelList[vm.focusCategoryGroupIndex2].level;
                vm.pageNum = 1;
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
                vm.pageList = vm.showPage();
            },
            previousPage : function(){
                var vm = this;
                if(vm.pageNum<=1) return;
                vm.pageNum-=1;
                vm.pageList = vm.showPage();
            },
            nextPage : function(){
                var vm = this;
                if(vm.pageNum>=vm.pageCount) return;
                vm.pageNum+=1;
                vm.pageList = vm.showPage();
            },
            previewNatural : function(categories,lessonId){
                var vm = this;
                var practices = categories.practices || [];
                if(practices.length <= 0){
                    $17.alert("没有相应类别应用,暂不能预览");
                    return false;
                }
                var questions = practices[0].questions || [];
                if(questions.length <= 0){
                    $17.alert("没有配相应的应试题,暂不能预览");
                    return false;
                }
                var qIds = [];
                for(var t = 0, tLen = questions.length; t < tLen; t++){
                    qIds.push(questions[t].questionId);
                }
                vm.$emit("previewtype",{
                    type : vm.type,
                    params : {
                        urlParams: JSON.stringify({
                            env : vm.env,
                            img_domain:vm.imgDomain,
                            hw_practice_url:"/flash/loader/newselfstudymobile.vpage?bookId=" + vm.bookId + "&qids="+qIds.join(",")+"&lessonId="+lessonId+"&practiceId="+practices[0].practiceId,
                            client_name:"pc",
                            client_type:"pc",
                            from : "preview"
                        })  //ppt或pptx课件的绝对地址
                    }
                });
            },
            refreshPage : function(){
                this.levelContent();
                this.recommendContent();
                this.wholeContent();
            }
        },
        created : function(){
            var vm = this;
            vm.levelContent();
            vm.recommendContent();
            vm.wholeContent();
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
        naturalSpelling   : naturalSpelling
    });
});