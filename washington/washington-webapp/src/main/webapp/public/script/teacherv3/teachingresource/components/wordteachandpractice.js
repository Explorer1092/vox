$(function(){
    /**
     *
     * 字词讲练
     */
    var venusQuestion = $17.venusQuestion;
    var wordTeachAndPractice = {
        template : template("T:WORD_TEACH_AND_PRACTICE",{}),
        data : function(){
            return {
                wordContentList : [],
                wordsPracticeMap : {},
                isActive : false,
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
                var wordQuestions= vm.wordsPracticeMap['wordExerciseMap'] && (vm.wordsPracticeMap['wordExerciseMap'] !== {}) && vm.wordsPracticeMap['wordExerciseMap']['questions'];
                var questionInfo =  wordQuestions && wordQuestions.length>0 ? vm.wordsPracticeMap['wordExerciseMap']['questions'] : [];
                for (var i = 0, iLen = questionInfo.length; i < iLen; i++) {
                    var question = questionInfo[i];
                    ids.push(question.id);
                }
                return ids;
            }
        },
        watch : {
            sectionId : function(newsectionId,oldsectionId){
                if(newsectionId !== oldsectionId){
                    this.wordContent();
                    this.focusCategoryGroupIndex = -1;
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
            wordContent : function(){
                var vm = this;
                var noNetWork,noResources;
                var isLoading = true;
                $.get("/teacher/teachingresource/content.vpage",{
                    bookId  : vm.bookId,
                    unitId  : vm.unitId,
                    sectionId : vm.sectionId,
                    type    : vm.type,
                    subject : vm.subject
                }).done(function(res){
                    var content = res.content || [];
                    vm.wordContentList = content;
                    vm.wordsPracticeMap = (vm.wordContentList.length>0 && vm.wordContentList[0].stoneData && vm.wordContentList[0].stoneData.length>0 && vm.wordContentList[0].stoneData[0]["wordsPractice"]) ? vm.wordContentList[0].stoneData[0]["wordsPractice"] : {};
                    if(vm.wordsPracticeMap['imageTextMap']){
                        vm.categoryGroupClick(1);
                    }else{
                        vm.categoryGroupClick(2);
                    }
                    // var info = "";
                    // if(res.success){
                    //     info = content.length > 0 ? "" : "暂无内容数据";
                    // }else{
                    //     info =  res.info || "接口请求错误";
                    // }
                    // var result = vm.getMessageObject(res.success && content.length > 0,info);
                    noNetWork = false;
                    if(res.success){
                        isLoading = false;
                        if(!content.length > 0 ){
                            noResources = true;
                        }
                    }else{
                        $17.info("接口请求错误");
                    }
                    var result = vm.getMessageObject(res.success && content.length > 0,noResources,noNetWork,isLoading);
                    $17.info(result);
                    vm.$emit("content-message",result);
                }).fail(function(e){
                    vm.wordContentList = [];
                    // vm.$emit("content-message",vm.getMessageObject(false,e.message));
                    noResources = false;
                    isLoading = false;
                    noNetWork = true;
                    vm.$emit("content-message",vm.getMessageObject(false,noResources,noNetWork,isLoading));
                });
            },
            categoryGroupClick: function(index){
                var vm =  this;
                var focusCategoryGroupIndex = vm.focusCategoryGroupIndex;
                if(focusCategoryGroupIndex === index){
                    return false;
                }
                vm.focusCategoryGroupIndex = index;
                if(vm.focusCategoryGroupIndex === 0){
                    vm.getQuestionMap(vm.ids,function(questionMap){
                        if(questionMap){
                            vm.questionMap = Object.assign({}, vm.questionMap, questionMap);
                            vm.questionInfo = vm.wordsPracticeMap['wordExerciseMap']['questions'];
                        }
                    });
                }
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
                }).fail(function(e){
                    // vm.loading = true;
                    callback();
                });
            },
            previewCourse : function(type,courseId){
                var vm = this;
                vm.$emit("previewtype",{
                    type : type,
                    params : {
                        courseId : courseId
                    }
                });
            },
            previewImageText : function(type,doModuleUrl,chapterId){
                var vm = this;
                vm.$emit("previewtype",{
                    type : type,
                    params : {
                        doModuleUrl : doModuleUrl,
                        chapterId : chapterId
                    }
                });
            }
        },
        created : function(){
            var vm = this;
            vm.wordContent();
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
        wordTeachAndPractice   : wordTeachAndPractice
    });
});