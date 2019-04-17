$(function(){

    var venusQuestion = $17.venusQuestion;
    /**
     *
     * 讲练测
     */
    var intelligentTeaching = {
        template : template("T:INTELLIGENT_TEACHING",{}),
        data : function(){
            return {
                intelligentInfo : [],
                questionMap : {},
                questionInfo : [],
                focusCategoryGroupIndex : -1
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
            }
        },
        components: {
            venusQuestion : venusQuestion
        },
        computed: {
            ids : function() {
                var ids = [];
                var vm = this;
                var questionInfo = vm.intelligentInfo[vm.focusCategoryGroupIndex].questions;
                for (var i = 0, iLen = questionInfo.length; i < iLen; i++) {
                    var question = questionInfo[i];
                    ids.push(question.id);
                    var postQuestions = question.postQuestions;
                    for (var m = 0, mLen = postQuestions.length; m < mLen; m++) {
                        ids.push(postQuestions[m].id);
                    }
                }
                return ids;
            }
        },
        watch : {
            unitId : function(newUnitId,oldUnitId){
                if(newUnitId !== oldUnitId){
                    this.intelligentContent();
                    this.focusCategoryGroupIndex = -1;
                }
            }
        },
        methods : {
            // getMessageObject : function(success,info){
            //     return {
            //         success : success,
            //         info    : info || ""
            //     };
            // },
            getMessageObject : function(success,noResources,noNetWork,isLoading){
                return {
                    success : success,
                    noResources : noResources || false,
                    noNetWork : noNetWork || false,
                    isLoading : isLoading || false
                }
            },
            intelligentContent: function () {
                var vm = this;
                // vm.loading = true;
                var noNetWork,noResources;
                var isLoading = true;
                $.get("/teacher/teachingresource/content.vpage", {
                    bookId  : vm.bookId,
                    unitId  : vm.unitId,
                    sectionId : vm.sectionId,
                    type    : vm.type,
                    subject : vm.subject
                }).done(function (res) {
                    var content = res.content || [];
                    vm.intelligentInfo = content;
                    // var info = "";
                    // if(res.success){
                    //     if(content.length > 0){
                    //         // vm.questionInfo = Object.assign([], vm.questionInfo, res.content[vm.focusCategoryGroupIndex].questions);
                    //         vm.categoryGroupClick(0);
                    //         info =  "" ;
                    //     }else{
                    //         info = "暂无内容数据";
                    //     }
                    // }else{
                    //     info =  res.info || "接口请求错误";
                    // }
                    // var result = vm.getMessageObject(res.success && content.length > 0,info);
                    noNetWork = false;
                    if(res.success){
                        isLoading = false;
                        if(content.length > 0){
                            // vm.questionInfo = Object.assign([], vm.questionInfo, res.content[vm.focusCategoryGroupIndex].questions);
                            vm.categoryGroupClick(0);
                        }else{
                            noResources = true;
                        }
                    }else{
                        $17.info("接口请求错误");
                    }
                    var result = vm.getMessageObject(res.success && content.length > 0,noResources,noNetWork,isLoading);
                    $17.info(result);
                    vm.$emit("content-message",result);
                }).fail(function (e) {
                    vm.intelligentInfo = [];
                    $17.error(e.message);
                    // vm.$emit("content-message",vm.getMessageObject(false,"网络错误，请退出页面重试"));
                    noResources = false;
                    isLoading = false;
                    noNetWork = true;
                    vm.$emit("content-message",vm.getMessageObject(false,noResources,noNetWork,isLoading));
                });
            },
            categoryGroupClick: function(index){
                var vm =  this;
                if(vm.focusCategoryGroupIndex === index){
                    return false;
                }
                vm.focusCategoryGroupIndex = index;
                vm.getQuestionMap(vm.ids,function(questionMap){
                    if(questionMap){
                        vm.questionMap = Object.assign({}, vm.questionMap, questionMap);
                        vm.questionInfo = vm.intelligentInfo[vm.focusCategoryGroupIndex].questions;
                    }
                });
                // vm.intelligentContent();
            },
            getQuestionMap : function(ids,callback) {
                callback = typeof callback === "function" ? callback : function(){};
                if (ids.length === 0) return;
                var vm = this,
                    params = JSON.stringify({
                        ids:ids,
                        containsAnswer: false
                    });
                $.get('/exam/flash/load/newquestion/byids.api', {
                    data: params
                }).done(function(data){
                    var info = "";
                    if (data.success) {
                        var questionMap = {},
                            questions = data.result;
                        for (var i = 0,iLen = questions.length; i < iLen; i++) {
                            questionMap[questions[i].id] = questions[i];
                        }
                        // vm.loading = false;
                        callback(questionMap);
                    } else {
                        info =  data.info || "接口请求错误";
                        // vm.loading = true;
                        callback();
                    }
                }).fail(function(){
                    // vm.loading = true;
                    callback();
                });
            },
            previewCourse : function(courseId){
                var vm = this;
                vm.$emit("previewtype",{
                    type : vm.type,
                    params : {
                        courseId : courseId
                    }
                });
            }
        },
        created : function(){
            var vm = this;
            vm.intelligentContent();
            $17.voxLog({
                module: "m_yvkU37oY9J",
                op : "pc_home_one_form_page_load",
                s0 : vm.subject,
                s1 : vm.bookId,
                s2 : vm.unitId,
                s3 : {sectionId:vm.sectionId,type:vm.type}
            });
            $17.info("intelligent_teaching created....");
        },
        mounted : function(){
            $17.info("intelligent_teaching mounted....");
        },
        beforeDestroy : function(){
            $17.info("intelligent_teaching beforeDestroy....");
        },
        destroyed : function () {
            $17.info("intelligent_teaching destroyed....");
        }
    };

    $17.teachingresource = $17.teachingresource || {};
    $17.extend($17.teachingresource, {
        intelligentTeaching   : intelligentTeaching
    });
});