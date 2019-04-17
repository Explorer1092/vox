/* global define : true, PM : true, $:true, Vue :true */
/**
 *  @date 2015/12/15
 *  @auto liluwei
 *  @description 该模块主要圣诞节活动
 */

require(["ajax","jqPopup"], function( promise){
    'use strict';

    var SID;

    var urlPre = "/parentMobile/activity/",
        getStudents = function(sid){
            promise(
                urlPre+"/getChristmasInfo.vpage",
                {
                    studentId : sid
                }
            )
            .done(function(res){
                SID = sid;

                sendFlower.cipc = res.cipc;
                sendFlower.cic = res.cic;
                sendFlower.sfpc = res.sfpc;
                sendFlower.canSend = res.canSend;
                sendFlower.isBindClazz = res.isBindClazz;
                kids.students = res.students;
                kids.ssc = res.ssc;
                kids.sic = res.sic;
            });
        };

    var kids = new Vue(
        {
            el : '#kids',
            data : {
                kids : [ ],
                sic : 0,
                ssc : 0,
                students : []
            },
            methods : {
                kidChange : function(event){
                    getStudents(event.target.value);
                }
            }
        }
    );

    var sendFlower = new Vue(
        {
            el : "#sendFlower",
            data:{
                cipc : 0,
                cic : 0,
                sfpc : 0,
                canSend : false,
                isBindClazz : false
            },
            methods:{
                doSendFlower : function(){
                    var vm = this;

                    if(!vm.isBindClazz){
                        $.alert("暂无可送花的老师");
                        return ;
                    }

                    promise(
                        urlPre + "christmasSendFlower.vpage",
                        {
                            studentId : SID
                        },
                        "POST"
                    )
                    .done(function(res){
                        if(res.success){
                            // vm.sfpc++;
                            vm.canSend = false;
                            return ;
                        }

                        $.alert(res.info);

                    });
                },
                doSendIntegral : function(){
                    location.href = "/parentMobile/homework/giveBean.vpage?isActivity=true&sid=" + SID;
                }
            }
        }
    );

    promise(
        urlPre + "getStudentList.vpage"
    )
    .done(function(res){
        if(!res.success){
            return ;
        }

        var students = res.students;
        // TODO  vue how to trigger v:on event
        SID = students[0].id;
        getStudents(SID);

        kids.kids = students;

    });

});

