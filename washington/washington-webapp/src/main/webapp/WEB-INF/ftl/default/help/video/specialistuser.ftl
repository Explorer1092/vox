<#import "../servicemodule.ftl" as com>
<@com.page title="专家点评">
<!--//start-->
<div class="section">
    <h2 class="blue"><i class="d_icon d_icon_6"></i>视频</h2>
    <div class="article specialistBox">
        <div class="nav_side">
            <ul>
                <li class="active" uniKey="all"><a href="#uniKey=all">全 部</a><span class="arrow arrowRight">◆<span class="inArrow">◆</span></span></li>
                <li uniKey="special"><a href="#uniKey=special">专家点评</a><span class="arrow arrowRight">◆<span class="inArrow">◆</span></span></li>
                <li uniKey="user"><a href="#uniKey=user">用户心声</a><span class="arrow arrowRight">◆<span class="inArrow">◆</span></span></li>
            </ul>
        </div>

        <div class="article_side">
            <ul class="breadcrumb_vox">
                <li><a href="/project/about/index.vpage">了解更多</a> <span class="divider">/</span></li>
                <li class="active" id="address">全 部</li>
            </ul>

            <#--//video list start-->
            <ul class="video_list"></ul>
            <#--video list end//-->

            <#--//video start-->
            <div class="videoDetail" style="display: none;">
                <div class="videoStart"></div>
                <div class="videoTitle"></div>
            </div>
            <#--video end//-->
        </div>

        <div class="clear"></div>
    </div>
</div>
<!--end//-->
<script type="text/javascript">
    var videoList = $(".video_list");
    var videoDetail = $(".videoDetail");
    var jsonArray =  {
        "user": [
            { "img":"userlist/1.jpg?1.0.2" , "url":"userlist/1.flv" , "time":"20130904" , "title":"教师评价 饶老师", "detail":"北京市昌平区南邵中心小学饶老师介绍使用一起作业网的感受" },
            { "img":"userlist/2.jpg?1.0.2" , "url":"userlist/2.flv" , "time":"20130904" , "title":"学生评价 南邵中心小学", "detail":"北京市昌平区南邵中心小学学生使用一起作业网后的感受" },
            { "img":"userlist/3.jpg?1.0.2" , "url":"userlist/3.flv" , "time":"20130904" , "title":"学校领导评价 张主任", "detail":"北京市昌平区南邵中心小学张主任介绍使用一起作业网的感受" },
            { "img":"userlist/4.jpg?1.0.2" , "url":"userlist/4.flv" , "time":"20130904" , "title":"教研组长演讲 孙慧", "detail":"北京东城区黑芝麻胡同小学英语教研组长孙慧在年会上发表了如何利用一起作业网学习培养学生的英语学习素养" },
            { "img":"userlist/5.jpg?1.0.2" , "url":"userlist/5.flv" , "time":"20130904" , "title":"教研员演讲 李静", "detail":"北京市海淀区教师进修学校小学英语教研员李静在年会上发表了基于信息技术的网络作业研究演讲" },
            { "img":"userlist/6.jpg?1.0.2" , "url":"userlist/6.flv" , "time":"20130904" , "title":"教师演讲 梁丹", "detail":"辽宁省沈阳市实验学校教师梁丹在年会上发表了浅谈一起作业对英语教学的促进为主题的演讲" },
            { "img":"userlist/7.jpg?1.0.2" , "url":"userlist/7.flv" , "time":"20130904" , "title":"教研组长评价 孙慧", "detail":"北京东城区黑芝麻胡同小学英语教研组长孙慧介绍使用一起作业网的感受" },
            { "img":"userlist/8.jpg?1.0.2" , "url":"userlist/8.flv" , "time":"20130904" , "title":"教研员评价 李静", "detail":"北京市海淀区教师进修学校小学英语教研员李静介绍一起作业网对学生的帮助" },
            { "img":"userlist/9.jpg?1.0.2" , "url":"userlist/9.flv" , "time":"20130904" , "title":"教师评价 梁丹", "detail":"辽宁省沈阳市实验学校教师梁丹讲述使用一起作业网的感受" },
            { "img":"userlist/10.jpg?1.0.2" , "url":"userlist/10.flv" , "time":"20130904" , "title":"学生评价", "detail":"让学生一起快乐学习" }
        ],
        "special": [
            { "img":"speciallist/1_v.jpg" , "url":"speciallist/1_v.flv" , "time":"20130904","type":"special" , "title":"全国小学英语教研员工作论坛", "detail":"2013年7月，全国小学英语教研员工作论坛在京隆重举行，共邀请到来自全国18个省的200余位教研员参与" },
            { "img":"speciallist/2.jpg?1.0.2" , "url":"speciallist/2.flv" , "time":"20130904","type":"special" , "title":"教育专家点评 徐小平", "detail":"全国著名英语教育专家徐小平解读一起作业网" },
            { "img":"speciallist/3.jpg?1.0.2" , "url":"speciallist/3.flv" , "time":"20130904","type":"special" , "title":"教育专家点评 龚亚夫", "detail":"中国教育学会外语专业委员会理事长龚亚夫发表本次参会感言" }
        ]
    }

    function videoType(array){
        for(var i = 0, len = array.length; i < len; i++){
             $(".video_list").append("<li><span idx="+ array[i].type +" str="+ i +"><img width='193' src='http://cdn.17zuoye.com/static/project/video/"+ array[i].img +"'></span><p>" + array[i].title + "</p></li>")
        }
    }

    function videoShow(item, array){
        $("li[uniKey="+item+"]").radioClass("active");
        $("#address").html( $("li[uniKey="+item+"] a").text() );
        switch ( item ){
            case "all":
                videoType(array.special);
                videoType(array.user);
                break;
            case "special":
                videoType(array.special);
                break;
            case "user":
                videoType(array.user);
                break;
            default :
                videoType(array.special);
                videoType(array.user);
        }
    }

    function videoStart (i, array){
        videoDetail.find(".videoStart").html('<embed width="600" height="386" type="application/x-shockwave-flash"' +
                'src="http://cdn.17zuoye.com/public/skin/project/about/images/flvplayer.swf"' +
                'style="undefined" id="single" name="single" quality="high" allowfullscreen="true"' +
                'flashvars="file=http://cdn.17zuoye.com/static/project/video/'+ array[i].url +'&amp;' +
                'image=http://cdn.17zuoye.com/static/project/video/'+ array[i].img +
                '&amp;width=600&amp;height=386">');
        videoDetail.find(".videoTitle").html(array[i].detail);
    }

    function getHashQuery(item){
        var svalue = location.hash.match(new RegExp('[\#\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    $(function(){
        videoShow( getHashQuery("uniKey") ,jsonArray );

        $(".nav_side").find("li").on('click', function(){
            var $this = $(this);

            $(".videoStart").html("");
            $this.radioClass("active");
            videoList.html("").show();
            videoDetail.hide();
            videoShow( $this.attr("uniKey"), jsonArray);
        });

        videoList.on("click", "li span", function(){
            var $thsiAtrr = $(this);
            videoList.hide();
            videoDetail.show();
            if( $thsiAtrr.attr("idx") == "special" ){
                videoStart( $thsiAtrr.attr("str"), jsonArray.special)
            }else{
                videoStart( $thsiAtrr.attr("str"), jsonArray.user)
            }
        });
    });
</script>
</@com.page>