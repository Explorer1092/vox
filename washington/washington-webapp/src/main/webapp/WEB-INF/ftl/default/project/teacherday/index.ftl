<#import "../../layout/webview.layout.ftl" as layout>
<@layout.page title="教师节送祝福"
pageCssFile={"teaDay":["public/skin/project/teacherday/css/teaDay"]}
pageJsFile={
    "teaDay" : "/public/script/project/teaDay",
    "jqcookie" : "/public/script/project/jquery.cookie"
}
pageJs=["jqcookie","teaDay","weui","voxLogs"]>
<div class="teaDay-banner">
    <div class="bg bg01"></div>
    <div class="bg bg02"></div>
</div>
<#if !isTerminal>
<div class="teaDay-header">
    <div class="td-tips">小伙伴们，教师节到来了，拍一张你的笑脸照片作为教师节祝福送给老师，感谢老师和你一起成长的时光，还有机会得到老师送的学豆呦！</div>
    <div class="td-avatar">
        <ul>
            <#list teacher_bless_result_list as teacher>
                <li class="js-teacher" data-bless="${teacher.bless_count}" data-tid="${teacher.teacher_id }"><img src="${teacher.teacher_avatar}"><p class="name">${teacher.teacher_name}</p></li>
            </#list>
        </ul>
    </div>
</div>
<div class="tab-main">
    <#list teacher_bless_result_list as teacher>
        <#if teacher.bless_count == 0>
            <div class="js-1"><#--0祝福-->
                <div class="td-rules">
                    <div class="r-btn js-rule"><a href="javascript:void(0)" class="w-btn">活动<br>规则</a></div>
                    <div class="text">快来参与活动，成为全班第一个送出<span class="txt-yellow">笑脸祝福</span>的学生吧！</div>
                </div>
                <div class="teaDay-btn">
                    <div class="innerBox" style="position: fixed; bottom: 0; left: 0; z-index: 3; background-color: #d94d64">
                        <div class="btnBox">
                            <a href="javascript:void(0);" class="w-btn js-send">送祝福</a>
                        </div>
                    </div>
                </div>
            </div>
        <#elseif teacher.my_bless??>
            <div class="js-3"><#--我送过了祝福-->
                <div class="td-cartoonBox"><i class="cartoonIcon"></i></div>
                <div class="td-rules">
                    <div class="r-btn js-rule"><a href="javascript:void(0)" class="w-btn">活动<br>规则</a></div>
                    <div class="text">${teacher.teacher_name}老师已经收到了<span class="txt-yellow">${teacher.bless_count}</span>个笑脸祝福，<span class="txt-yellow">${teacher.flower_count}</span>朵鲜花活动，还有<span class="txt-yellow">${restDays!0}</span>天结束！</div>
                </div>
                <div class="teaDay-section">
                    <div class="td-titleTag"><span class="textTag">我的祝福</span></div>
                    <div class="td-stuList">
                        <ul class="list-2">
                            <li class="js-student" style="width:100%;">
                                <div class="img">
                                    <img width="100%" src="${teacher.my_bless.img_url}@200w_1o">
                                </div>
                                <p class="name js-my-bless" data-count="${teacher.my_bless.flower_count!}" data-url="${teacher.my_bless.img_url!}">${teacher.my_bless.student_name}</p>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="teaDay-section">
                    <div class="td-titleTag"><span class="textTag">其他同学</span></div>
                    <div class="td-stuList">
                        <ul>
                            <#list teacher.bless_list as student>
                                <li class="js-student">
                                    <div class="img">
                                        <img width="100%" src="${student.img_url!}@160w_1o" />
                                    </div>
                                    <p class="name" data-url="${student.img_url!}">${student.student_name!}</p>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="teaDay-btn">
                    <div class="innerBox fixFooter">
                        <div class="btnBox">
                            <a href="javascript:void(0)" class="w-btn js-invite">邀请爸爸妈妈送鲜花</a>
                        </div>
                    </div>
                </div>
            </div>
        <#else>
            <div class="js-2"><#--有人送了我没送-->
                <div class="td-cartoonBox"><i class="cartoonIcon"></i></div>
                <div class="td-rules">
                    <div class="r-btn js-rule"><a href="javascript:void(0)" class="w-btn">活动<br>规则</a></div>
                    <div class="text">${teacher.teacher_name}老师已经收到了<span class="txt-yellow">${teacher.bless_count}</span>个笑脸祝福，<span class="txt-yellow">${teacher.flower_count}</span>朵鲜花活动，还有<span class="txt-yellow">${restDays!}</span>天结束！</div>
                </div>
                <div class="teaDay-btn">
                    <div class="innerBox">
                        <div class="btnBox">
                            <a href="javascript:void(0);" class="w-btn js-send">送祝福</a>
                        </div>
                    </div>
                </div>
                <div class="teaDay-section">
                    <div class="td-titleTag"><span class="textTag">其他同学</span></div>
                    <div class="td-stuList">
                        <ul>
                            <#list teacher.bless_list as student>
                                <li class="js-student">
                                    <div class="img">
                                        <img width="100%" src="${student.img_url!}@200w_1o" />
                                    </div>
                                    <p class="name" data-url="${student.img_url!}">${student.student_name!}</p>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </#if>
    </#list>
</div>

<div id="popup-wrapper"></div>
<script type="text/html" id="T:我的祝福">
    <div class="popUp-box">
        <div class="inner">
            <div class="top"></div>
            <div class="close"></div>
            <div class="head">
                <h1>
                    <%=sname%>送出的祝福
                    <%if(showParent){%>
                    家长送出<span><%=flowerCount%></span>朵鲜花
                    <%}%>
                </h1>
            </div>
            <div class="image">
                <img width="100%" src="<%=imgUrl%>@640w_1o" />
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="T:活动规则">
    <div class="popUp-box">
        <div class="inner">
            <div class="top"></div>
            <div class="close"></div>
            <div class="head">
                <h2>活动规则</h2>
            </div>
            <div class="container">
                <div class="title">活动时间：9月1日-9月14日 </div>
                <div class="tag">
                    <span>活动说明</span>
                </div>
                <div class="content">
                    <p>教师节到来了，拍一张你的笑脸，上传送给老师，有机会得到老师送的学豆呦！</p>
                </div>
                <div class="tag">
                    <span>参与方式</span>
                </div>
                <div class="content">
                    <p>学生拍一张自己的笑脸作为送给老师的教师节祝福，家长给孩子祝福的老师送鲜花，老师收到鲜花可兑换为班级学豆，奖励学生。</p>
                </div>
                <div class="tag">
                    <span>详细规则</span>
                </div>
                <div class="content">
                    <p>1. 一名学生可以给多名老师送笑脸祝福，且每名老师只能接受该学生的一次祝福；</p>
                    <p>2. 家长（限爸爸、妈妈）若有一个孩子，则每日可送鲜花数为2朵 ，若两个孩子，则每日可送鲜花数为4朵，以此类推(当日有效)；</p>
                    <p>3. 家长邀请同班家长（限爸爸、妈妈）注册并登录“家长通”，每成功邀请一人，可得6朵鲜花；</p>
                    <p>4. 活动期间，每个孩子做阿分题英语、阿分题数学、走遍美国学英语各2题，则一次性给父母各奖励6朵送花机会。</p>
                </div>
            </div>
        </div>
    </div>
</script>
<#else>
<div style="line-height:4rem;color:#fff;text-align: center;">
    毕业班学生不能参加此活动！
</div>
</#if>
</@layout.page>