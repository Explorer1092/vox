(function($,$17,constantObj){
    "use strict";
    /**
     * checkedGroups : 包含groupId,groupName字段
     * @constructor
     */
    function Carts(){
        var self = this;
        self.checkedGroups = [];
    }
    Carts.prototype = {
        constructor : Carts,
        displayClazzCntOfTabTypes   : ['FALLIBILITY_QUESTION'],
        practiceChildProperty : {
            FALLIBILITY_QUESTION : "questions"
        },
        displayQuestionCntOfTabTypes : [],
        getGroupDuration : function(checkedGroups){
            //以checkedGroups中的班级为准
            var self = this,
                assignGroupList = [],
                specialGroupList = [], //self.displayClazzCntOfTabTypes类型下选择的题中包含的班级
                publicGroupList = [], //checkedGroups 包含的班级
                newClazzNames = [],   //按groupId合并，相同groupId时长累计
                displayClazzCntOfTabTypes = self.displayClazzCntOfTabTypes,
                _practices = constantObj._homeworkContent.practices,
                totalTime = 0, //self.displayClazzCntOfTabTypes之外的所有类型总时长
                moduleSeconds = constantObj._moduleSeconds;

            $.each(displayClazzCntOfTabTypes, function(i,tabType){
                var _contents = _practices[tabType][self.practiceChildProperty[tabType]];
                _contents = $.isArray(_contents) ? _contents : [];
                $.each(_contents,function(i,question){
                    specialGroupList.push({
                        groupId : question.groupId,
                        groupName : question.groupName,
                        seconds : (+question.seconds || 0)
                    });
                });
            });

            for(var key in _practices){
                if(_practices.hasOwnProperty(key) && displayClazzCntOfTabTypes.indexOf(key) == -1){
                    totalTime += moduleSeconds[key];
                }
            }
            checkedGroups = $.isArray(checkedGroups) ? checkedGroups : [];
            for(var c = 0,cLen = checkedGroups.length; c < cLen; c++){
                publicGroupList.push({
                    groupId : checkedGroups[c].groupId,
                    groupName : checkedGroups[c].groupName,
                    seconds   : totalTime
                });
            }

            assignGroupList = assignGroupList.concat(specialGroupList,publicGroupList);
            for(var m = 0,mLen = assignGroupList.length; m < mLen; m++){
                var groupId = assignGroupList[m].groupId,groupIndex = -1;
                for(var k = 0,kLen = newClazzNames.length; k < kLen; k++){
                    if(newClazzNames[k].groupId == groupId){
                        groupIndex = k;
                        break;
                    }
                }
                if(groupIndex == -1){
                    newClazzNames.push($.extend(true,{},assignGroupList[m]));
                }else{
                    newClazzNames[groupIndex].seconds += (+assignGroupList[m].seconds || 0);
                }
            }

            return newClazzNames;
        },
        recalculateCartsTime : function(){
            var self = this,
                checkedGroups = self.checkedGroups,
                $assignTotaltime = $("#assignTotalTime"),
                totalTime = 0,
                minSeconds,maxSeconds,groupIds;

            groupIds = self.getGroupDuration(checkedGroups);
            $.each(groupIds,function(i,group){
                totalTime += (+group.seconds || 0)
            });

            if(groupIds.length == 0){
                $assignTotaltime.html(Math.ceil(totalTime/60));
            }else if(groupIds.length == 1){
                $assignTotaltime.html(Math.ceil(groupIds[0].seconds/60));
            }else{
                groupIds.sort(function(group1,group2){
                    if(group1.seconds > group2.seconds){
                        return 1;
                    }else if(group1.seconds < group2.seconds){
                        return -1;
                    }
                    return 0;
                });
                minSeconds = Math.ceil(groupIds[0].seconds/60);
                maxSeconds = Math.ceil(groupIds[groupIds.length - 1].seconds/60);
                if(minSeconds == maxSeconds){
                    $assignTotaltime.html(minSeconds);
                }else{
                    $assignTotaltime.html(minSeconds + "~" + maxSeconds);
                }
            }
        },
        recalculate : function(tabType,questionCnt){
            //sec : 表示增加或减少的时间数,questionCnt:表示该类型的选入的总题数
            var self = this,
                $count = $(".J_UFOInfo p[type='" + tabType + "'] .count"),
                displayClazzCntOfTabTypes = self.displayClazzCntOfTabTypes;

            self.recalculateCartsTime();

            if(displayClazzCntOfTabTypes.indexOf(tabType) != -1){
                var underTabTypeOfClazz = [];
                var practiceObj = constantObj._homeworkContent.practices[tabType];
                var questions = practiceObj[self.practiceChildProperty[tabType]] || [];
                for(var m = 0,mLen = questions.length; m < mLen; m++){
                    if(underTabTypeOfClazz.indexOf(questions[m].groupId) == -1){
                        underTabTypeOfClazz.push(questions[m].groupId);
                    }
                }
                $count.html(underTabTypeOfClazz.length).attr("data-count",underTabTypeOfClazz.length);
            }else{
                $count.html(questionCnt).attr("data-count",questionCnt);
            }
        },
        resetClazzGroups : function(checkedGroups){
            var self = this,
                seconds = 0,
                groupIds = [],
                displayClazzCntOfTabTypes = self.displayClazzCntOfTabTypes;
            if(!$.isArray(checkedGroups)){
                $17.alert("班级格式不对");
            }
            self.checkedGroups = checkedGroups;
            $.each(checkedGroups,function(i,group){
                groupIds.push(group.groupId);
            });

            $.each(displayClazzCntOfTabTypes, function(i,type){
                var practiceQuestions = [],seconds = 0;
                var practiceObj = constantObj._homeworkContent.practices[type];
                var practiceChildProperty = self.practiceChildProperty[type];
                var practiceQuestionsOrApps = practiceObj[practiceChildProperty] || [];
                $.each(practiceQuestionsOrApps,function(i,question){
                    if(groupIds.indexOf(question.groupId) != -1){
                        practiceQuestions.push(question);
                    }else{
                        seconds += question.seconds;
                    }
                });

                constantObj._homeworkContent.practices[type][practiceChildProperty] = practiceQuestions;

                var reviewQuestions = [];
                $.each(constantObj._reviewQuestions[type],function(i,question){
                    if(groupIds.indexOf(question.groupId) != -1){
                        reviewQuestions.push(question);
                    }
                });
                constantObj._reviewQuestions[type] = reviewQuestions;

                constantObj._moduleSeconds[type] -= seconds;
                self.recalculate(type);
            });

        }
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getCarts : function(){
            return new Carts();
        }
    });
})($,$17,constantObj);
