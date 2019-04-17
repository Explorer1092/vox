$(function(){
    /**
     * /teacher/teachingresource/wordrecognitionandreadingdetail.vpage
     * 生字认读详情
     */
    var id = $17.getQuery("id");
    var ids = $17.getQuery("ids");
    new Vue({
        el : "#wordContainerBox",
        data : {
            lessonName : "",
            questionMap : {},
            wordFocus: {},
            questionList : [],
            isPlay : false,
            focusIndex : -1,
            wordLeft : 0,
            moveNum : 7,
            sigleWordLength : 102, //单位px
            springSwitch : false
        },
        methods : {
            getData : function(){
                var vm = this;
                vm.paramsList = {
                    id : id,
                    ids : ids.split(',')
                };
                // vm.lessonName = vm.paramsList.lessonName;
                vm.changeWord(vm.paramsList.id);
                vm.wordLeft = -(vm.focusIndex * vm.sigleWordLength);
            },
            getQuestionMap : function(ids,id,callback) {
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
            playAudio : function(audioUrl){
                var videoElem = document.getElementById("bgFile");
                var vm = this;
                vm.isPlay = true;
                if(vm.isPlay){
                    videoElem.src = audioUrl;
                    videoElem.play();
                    videoElem.onended = function() {
                        vm.isPlay = false;
                    };
                }
            },
            changeWord : function(id){
                var vm = this;
                for (var j = 0, iLen = vm.paramsList.ids.length; j < iLen; j++) {
                    if(vm.paramsList.ids[j] === id){
                        vm.focusIndex = j;
                    }
                }
                vm.getQuestionMap(vm.paramsList.ids,vm.paramsList.ids[vm.focusIndex],function(questionMap){
                    if(questionMap){
                        vm.questionMap = Object.assign({}, vm.questionMap, questionMap);
                        vm.wordFocus = vm.questionMap[vm.paramsList.ids[vm.focusIndex]]["content"]["subContents"][0]["extras"];
                    }
                });
                return  vm.focusIndex;
            },
            wordCardMove : function(arrow){
                var vm = this;
                var focusIndex = vm.focusIndex;
                var wordCardLen = vm.questionList.length;
                var nextfocusIndex;
                if(wordCardLen <= 1){
                    return false;
                }
                switch (arrow) {
                    case "left":
                        //向左滑动
                        nextfocusIndex = (((focusIndex + 1) >= wordCardLen) ? 0 : (focusIndex + 1));
                        break;
                    case "right":
                        //向右滑动
                        nextfocusIndex = (((focusIndex - 1) < 0) ? (wordCardLen - 1) : (focusIndex - 1));
                        break;
                    default:
                        nextfocusIndex = 0;
                }
                vm.focusIndex = nextfocusIndex;
                vm.getQuestionMap(vm.paramsList.ids,vm.paramsList.ids[vm.focusIndex],function(questionMap){
                    if(questionMap){
                        vm.questionMap = Object.assign({}, vm.questionMap, questionMap);
                        vm.wordFocus = vm.questionMap[vm.paramsList.ids[vm.focusIndex]]["content"]["subContents"][0]["extras"];
                    }
                });
            },
            arrowLeftClick : function(){
                var vm = this;
                var newWordLeft = Math.abs(vm.wordLeft);
                var sigleWordLength = vm.sigleWordLength;
                var moveNum = vm.moveNum;
                var questionList = vm.questionList.length;
                vm.wordLeft = newWordLeft/sigleWordLength>=questionList - moveNum ? -(questionList-moveNum)*sigleWordLength : vm.wordLeft-moveNum*sigleWordLength;
            },
            arrowRightClick : function(){
                var vm = this;
                var newWordLeft = Math.abs(vm.wordLeft);
                var sigleWordLength = vm.sigleWordLength;
                var moveNum = vm.moveNum;
                vm.wordLeft = newWordLeft/sigleWordLength<=moveNum ? 0 : vm.wordLeft+moveNum*sigleWordLength;
            },
            springSwitchClick : function(){
                this.springSwitch = !this.springSwitch
            }
        },
        created: function () {
            var vm = this;
            vm.getData();
            $17.info("preview created....");
        },
        mounted: function () {
            $17.info("preview mounted....");
        }
    });
});