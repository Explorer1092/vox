<#if (currentTeacherDetail.rootRegionCode)?? && p2pRefs[(currentTeacherDetail.rootRegionCode)?string]?exists && (currentTeacherDetail.schoolAmbassador)!false>
    <style>
        /*customService-box*/
        .customService-box{ margin: -30px 0 -10px;}
        .customService-box .icon-s{ background: url(<@app.link href="public/skin/teacherv3/images/tooneservice/service.png"/>) no-repeat 300px 300px; width: 36px; height: 36px; display: inline-block;}
        .customService-box .icon-s-card{ background-position: 0 0;}
        .customService-box .icon-s-phone{ background-position: 0 -40px;}
        .customService-box .icon-s-qq{background-position: 0 -81px;}
        .customService-box .icon-s-mobile{background-position: 0 -121px;}
        .customService-box .cs-title{ text-align: center; color: #383a4a; font-size: 20px; padding: 20px 0;}
        .customService-box .cus-con{ text-align: center; }
        .customService-box .cus-con .actor{ display: inline-block; width: 146px; text-align: center;}
        .customService-box .cus-con .actor i{}
        .customService-box .cus-con .actor strong{ font-size: 14px; font-weight: normal; color: #383a4a; display: block; padding-top: 8px;}
        .customService-box .cus-con .actor span{ display: block; line-height: 150%;}
        .customService-person-btn{ position: fixed; _position: absolute; right: 10px; top: 100%; margin-top: -200px; width: 53px;}
        .customService-person-btn .cp-inner{ _position: absolute; _top: expression(documentElement.scrollTop); left: 0;}
        .customService-person-btn a{ background: url(<@app.link href="public/skin/teacherv3/images/tooneservice/servicePerson.png"/>) no-repeat 0 0; width: 53px; height: 155px;display: inline-block;}
        .customService-person-btn a:hover{ background-position: 0 -158px;}
    </style>
    <div style="" class="customService-person-btn">
        <div class="cp-inner">
            <a id="oneToOneServiceOpenPopupBtn" href="javascript:void (0);"></a>
        </div>
    </div>
    <script type="text/html" id="T:oneToOneServicePopup">
        <div class="customService-box">
            <div class="cs-title">
                <#if p2pRefs[(currentTeacherDetail.rootRegionCode)?string].realName == "黄楚云">
                    <p style="margin-bottom: 10px;"><img src="<@app.link href="public/skin/teacherv3/images/tooneservice/proson-hcy.png"/>"/></p>
                </#if>
                <#if p2pRefs[(currentTeacherDetail.rootRegionCode)?string].realName == "李倩">
                    <p style="margin-bottom: 10px;"><img src="<@app.link href="public/skin/teacherv3/images/tooneservice/proson-lq.png"/>"/></p>
                </#if>
                <#if p2pRefs[(currentTeacherDetail.rootRegionCode)?string].realName == "张帅">
                    <p style="margin-bottom: 10px;"><img src="<@app.link href="public/skin/teacherv3/images/tooneservice/proson-zs.png"/>"/></p>
                </#if>
                <#if p2pRefs[(currentTeacherDetail.rootRegionCode)?string].realName == "廖志超">
                    <p style="margin-bottom: 10px;"><img src="<@app.link href="public/skin/teacherv3/images/tooneservice/proson-lzc.png"/>"/></p>
                </#if>
                ${p2pRefs[(currentTeacherDetail.rootRegionCode)?string].realName}
            </div>
            <div class="cus-con">
                <#--<span class="actor">-->
                    <#--<i class="icon-s icon-s-phone"></i>-->
                    <#--<strong>电话(免长途费)<span>${p2pRefs[(currentTeacherDetail.rootRegionCode)?string].phone}</span></strong>-->
                <#--</span>-->
                <span class="actor">
                    <i class="icon-s icon-s-qq"></i>
                    <strong>QQ<span>${p2pRefs[(currentTeacherDetail.rootRegionCode)?string].qq}</span></strong>
                </span>
                <span class="actor">
                    <i class="icon-s icon-s-mobile"></i>
                    <strong>移动电话<span>${p2pRefs[(currentTeacherDetail.rootRegionCode)?string].mobile}</span></strong>
                </span>
            </div>
        </div>
    </script>
    <script type="text/javascript">
        $(function(){
            $("#oneToOneServiceOpenPopupBtn").on("click", function(){
                $17.tongji("老师端-一对一客服专员-Click")
                $.prompt(template("T:oneToOneServicePopup", {}), {
                    title: "您的一对一客服专员",
                    buttons: { "确定": true },
                    position: {width: 510}
                });
            });
        });
    </script>
</#if>