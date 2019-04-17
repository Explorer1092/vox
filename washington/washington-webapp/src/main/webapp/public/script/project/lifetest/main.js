var Main = function () {
    this.curIndex = 0,
        this.totalScore = 0,
        this.allTopic = [1, 2, 3, 4, 5],
        /*this.sortTopic = [5,3,8,7,6],*/
        this.sortTopic = [1,2,3,4,5],
        this.allTopicNum = 5,
        this.totalObj = []
};
var nextFlag = true;
Main.prototype = {
    init: function () {
        $("#J_SalIndex").addClass("web-box"), this.eventHandle()
    },
    eventHandle: function () {
        var t = this;
        $("#J_Btn1").on("tap", function () {
            $("#J_SalIndex").hide(), t.goNextTopic()
        }), $(".btns .btn").on("tap", function () {
            t.doSelect(this)
        }), $(".J_NextBtn").on("tap", function () {
            t.goNext(this)
        }), $("#J_OverBtn").on("tap", function () {
            t.goNextTopic()
        }), $("#J_ShareTipBtn").on("tap", function () {
            $("#J_ShareTip").show()
        }), $("#J_ShareTip").on("tap", function () {
            $(this).hide()
        })
    },
    setTopic: function () {
        /*var t = this.allTopic.length,
         i = Math.floor(Math.random() * t) + 1;
         i = i === t ? t - 1 : i;
         var o = this.allTopic[i];
         return this.allTopic.splice(i, 1), o*/
        //this.curIndex++;
        return this.sortTopic[this.curIndex];
    },
    doSelect: function (t) {
        $(t).parent(".btns").find(".btn").removeClass("active"), $(t).addClass("active")
        $(t).attr('flag',1);
    },
    goNext: function (t) {
        nextFlag = true;
        if(!nextFlag) return false;
        nextFlag = false;
        // $('.btn').removeClass('active');
        //var i = $(t).prev(".btns").find(".active");
        var i = [];
        var eleBtns = $(t).prev(".btns").find('.btn.active');
        // $(eleBtns).each(function(index,item){
        //     if($(item).attr('flag')){
        //         i = $(item);
        //     }
        // });
        // if (i.length <= 0) return alert_v("请先选择答案"), !1;
        if(eleBtns.length <=0) return alert_v("请先选择答案"), !1;
        // var o = parseInt(i.attr("data-score")),
        var o = parseInt(eleBtns.attr("data-score")),
            // e = i.index(),
            n = {
                topic: this.curTopic,
                // option: e,
                score: o
            };
        this.totalObj.push(n),
            this.totalScore += o,
            $("#J_OverLay").css("display", "-webkit-box"),
            $(".over-con").hide(),
            $("#J_SubOverItem" + this.curTopic).show();
    },
    goNextTopic: function () {
        nextFlag = true;
        if (this.curTopic = this.setTopic(), 3 === this.curTopic) {
            var t = 0;
            setInterval(function () {
                $(".subject3 .sec-pic04").removeClass("a0 a1 a2").addClass("a" + t), t = 2 > t ? t + 1 : 0
            }, 100)
        }
        $('.btn').removeClass('active');
        var that = this;
        setTimeout(function(){
            $("#J_OverLay,.sal-box").hide(), that.curIndex < that.allTopicNum ? (that.curIndex++, $("#J_SalBox" + that.curTopic).css("display", "-webkit-box")) : that.goOver()
        },100);

    },
    goOver: function () {
        var t = this;
        $("#J_paperInspection").css("display", "-webkit-box"), setTimeout(function () {
            t.goResult()
        }, 2e3)
    },
    goResult: function () {
        $("#J_paperInspection").hide(), $("#J_Result").show();
        var t = this.totalScore;
        var num = t/10;

        setNumber(num);
        if(t>=0 && t<=10){
            $("#J_Result_01").show();
        }else if(t>10 && t<=30){
            $("#J_Result_02").show();
        }else{
            $("#J_Result_03").show();
        }
        // t >= 0 && 40 > t ? $("#J_Result_01").show() : t >= 40 && 60 > t ? $("#J_Result_02").show() : t >= 60 && 80 > t ? $("#J_Result_03").show() : t >= 80 && 90 > t ? $("#J_Result_05").show() : t >= 90 && 100 >= t && $("#J_Result_07").show();
        var i = JSON.stringify(this.totalObj);
        // $.getJSON("http://h5.flyfinger.com/qualcomm/sign/submitUserInfo?userName=" + i + "&mark=nice")
    }
};
var cdnLocation = "";
var hosts = location.host;
if (hosts.indexOf("test.17zuoye")>-1){
    cdnLocation = '//cdn-cnc.test.17zuoye.net/';
}else if(hosts.indexOf("staging.17zuoye")>-1){
    cdnLocation = '//cdn-cnc.staging.17zuoye.net/';
}else if(hosts.indexOf("17zuoye.com")>-1){
    cdnLocation = '//cdn-cnc.17zuoye.cn/';
}
var main = (new Main).init();
function setNumber(n){
    if(!n){
        $('.last-number-img img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-item1-head-0.png');
    }else if(n===1){
        $('.last-number-img img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-item1-head-1.png');
    }else if(n===2){
        $('.last-number-img img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-item2-head-2.png');
    }else if(n===3){
        $('.last-number-img img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-item2-head-3.png');
    }else if(n===4){
        $('.last-number-img img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-item3-head-4.png');
        $('#share-btn').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-btn1_308e1fd.png');
    }else if(n===5){
        $('.last-number-img img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-item3-head-5.png');
        $('#share-btn').attr('src',cdnLocation + '/public/skin/project/lifetest/images/result-btn1_308e1fd.png');
    }

    if(n>3){
        $('#share-img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/share2.png');
    }else{
        $('#share-img').attr('src',cdnLocation + '/public/skin/project/lifetest/images/share1.png');
    }
}


// 背景音乐
(function(){
    var flag = 1;
    var musicObj = document.getElementById('J_BgAudio');
    $('.music-outer').click(function(){
        if(flag){
            flag = 0;
            $('#music-on').css({top:-100});
            $('#music-off').css({top:0});
            musicObj.pause();
        }else{
            flag =1;
            $('#music-off').css({top:-100});
            $('#music-on').css({top:0});
            musicObj.play();
        }


    });
})();

(function(){
    var flag = 1;
    setInterval(function(){
        if(flag){
            flag = 0;
            $('#replace-img').css({"background-image":"url(" + cdnLocation + "/public/skin/project/lifetest/images/subject5-02_365ff0f.png)"})
        }else{
            flag = 1;
            $('#replace-img').css({"background-image":"url(" + cdnLocation + "/public/skin/project/lifetest/images/subject5-02_365ff0f-a.png)"})
        }
    },800);

})();
