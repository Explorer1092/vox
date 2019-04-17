$(function(){
    /**
     *
     * 生字认读
     */
    var wordRecognitionAndReading = {
        template : template("T:WORD_RECOGNITION_AND_READING",{}),
        data : function(){
            return {
                wordContentList : [],
                wordPackagesList : [],
                questionList : [],
                selectQuestionIds : this.qids,
                audioIndex : -1,
                isDateduppt : this.isDateppt
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
            isDateppt : {
                type : String,
                default : ""
            },
            qids : {
                type : Array,
                default : []
            }
        },
        computed: {
            ids : function() {
                var ids = [];
                var vm = this;
                var questionInfo = vm.wordPackagesList[0]  ? vm.wordPackagesList[0].questions : [];
                for (var i = 0, iLen = questionInfo.length; i < iLen; i++) {
                    var question = questionInfo[i];
                    ids.push(question.id);
                }
                return ids;
            }
        },
        watch : {
            sectionId : function(newsectionId,oldsectionId){
                (newsectionId !== oldsectionId) && this.wordContent();
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
                    if(vm.wordContentList.length>0 && vm.wordContentList[0].packages.length>0){
                        vm.wordPackagesList = vm.wordContentList[0].packages;
                    }
                    vm.getQuestionMap(vm.ids,function(questionMap){
                        if(questionMap){
                            vm.questionMap = Object.assign({}, vm.questionMap, questionMap);
                            vm.questionInfo = vm.wordPackagesList[0].questions;
                        }
                    });
                    vm.audioIndex = -1;
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
                        vm.questionList = questions;
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
            playAudio : function(index,audioUrl){
                var videoElem = document.getElementById("bgFile");
                var vm = this;
                if(vm.audioIndex === index){
                    vm.audioIndex = -1;
                    videoElem.pause();
                }else {
                    vm.audioIndex = index;
                    videoElem.src = audioUrl;
                    videoElem.play();
                    videoElem.onended = function() {
                        vm.audioIndex = -1;
                    };
                }
            },
            addOrRemove: function(id) {
                var vm = this;
                var arr = vm.selectQuestionIds;
                if (arr.indexOf(id) > -1) {
                    arr.splice(arr.indexOf(id), 1);
                } else {
                    arr.push(id);
                }
                vm.selectQuestionIds = arr;
                vm.$emit("preview-word-id",arr);
            },
            lookDetail : function(id){
                var vm = this;
                if(vm.isDateduppt) return false;
                vm.$emit("preview-word",{
                    type : vm.type,
                    params : {
                        // lessonName : lessonName,
                        id : id,
                        ids : vm.ids
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
        wordRecognitionAndReading   : wordRecognitionAndReading
    });
});