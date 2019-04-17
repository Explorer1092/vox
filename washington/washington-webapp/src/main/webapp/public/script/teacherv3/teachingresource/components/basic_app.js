$(function(){
    /**
     *
     * 基础练习
     */
    var basicApp = {
        template : template("T:BASIC_APP",{}),
        data : function(){
            return {
                basicInfos : [],
                focusCategoryGroupIndex : 0
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
            }
        },
        watch : {
            unitId : function(newUnitId,oldUnitId){
                $17.info(newUnitId + "<--basicapp新旧单元-->" + oldUnitId);
                if(newUnitId !== oldUnitId){
                    this.basicContent();
                    this.focusCategoryGroupIndex = 0;
                }
            }
        },
        methods : {
            // getMessageObject : function(success,info){
            //     return {
            //         success : success,
            //         info    : info || ""
            //     }
            // },
            getMessageObject : function(success,noResources,noNetWork,isLoading){
                return {
                    success : success,
                    noResources : noResources || false,
                    noNetWork : noNetWork || false,
                    isLoading : isLoading || false
                }
            },
            basicContent: function () {
                var vm = this;
                var noNetWork,noResources;
                var isLoading = true;
                $.get("/teacher/teachingresource/content.vpage", {
                    bookId : vm.bookId,
                    unitId: vm.unitId,
                    type: vm.type,
                    subject : vm.subject
                }).done(function (res) {
                    var content = res.content || [];
                    vm.basicInfos = content;
                    // var info = "";
                    // if(res.success){
                    //     info = content.length > 0 ? "" : "暂无内容数据";
                    // }else{
                    //     info =  res.info || "接口请求错误";
                    // }
                    noNetWork = false;
                    if(res.success){
                        isLoading = false;
                        if(!content.length > 0 ){
                            noResources = true;
                        }
                    }else{
                        $17.info("接口请求错误");
                    }
                    // var result = vm.getMessageObject(res.success && content.length > 0,info);
                    var result = vm.getMessageObject(res.success && content.length > 0,noResources,noNetWork,isLoading);
                    $17.info(result);
                    vm.$emit("content-message",result);
                }).fail(function (e) {
                    vm.basicInfos = [];
                    $17.error(e.message);
                    noResources = false;
                    isLoading = false;
                    noNetWork = true;
                    vm.$emit("content-message",vm.getMessageObject(false,noResources,noNetWork,isLoading));
                    // vm.$emit("content-message",vm.getMessageObject(false,"网络错误，请退出页面重试"));
                });
            },
            categoryGroupClick: function(index){
                var vm =  this;
                var focusCategoryGroupIndex = vm.focusCategoryGroupIndex;
                if(focusCategoryGroupIndex === index){
                    return false;
                }
                vm.focusCategoryGroupIndex = index;
            },
            previewDetail: function(categories,lessonId){
                var vm = this;
                var practices = categories.practices || [];
                if(practices.length <= 0){
                    $17.alert("没有相应类别应用,暂不能预览");
                    return false;
                }
                var questions = categories.practices[0].questions || [];
                if(questions.length <= 0){
                    $17.alert("没有配相应的应试题,暂不能预览");
                    return false;
                }
                var qIds = [];
                for(var t = 0, tLen = questions.length; t < tLen; t++){
                    qIds.push(questions[t].questionId);
                }
                // return vm.domain + "/teacher/teachingresource/previewbasicapp.vpage?" + $.param(paramObj);
                vm.$emit("previewtype",{
                    type : vm.type,
                    params : {
                        qids : qIds.join(","),
                        lessonId : lessonId,
                        practiceId : practices[0].practiceId
                    }
                });
            },
            covertSentences: function(sentences){
                if(!$.isArray(sentences)){
                    return "";
                }
                return sentences.join(" / ");
            }
        },
        created : function(){
            var vm = this;
            vm.basicContent();
            $17.voxLog({
                module: "m_yvkU37oY9J",
                op : "pc_home_one_form_page_load",
                s0 : vm.subject,
                s1 : vm.bookId,
                s2 : vm.unitId,
                s3 : {sectionId:vm.sectionId,type:vm.type}
            });
            $17.info("basic_app created....");
        },
        mounted : function(){
            $17.info("basic_app mounted....");
        },
        beforeDestroy : function(){
            $17.info("basic_app beforeDestroy....");
        },
        destroyed : function () {
            $17.info("basic_app destroyed....");
        }
    };

    $17.teachingresource = $17.teachingresource || {};
    $17.extend($17.teachingresource, {
        basicApp   : basicApp
    });
});