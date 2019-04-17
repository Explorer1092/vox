<#import '../layout.ftl' as layout>
<@layout.page className='HomeworkReportGiveBean' title="孩子个人信息" pageJs="second" specialCss="skin2" extraJs=extraJs![] specialHead='
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <meta name="format-detection" content="telephone=no" />
    <meta name="format-detection" content="email=no" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>孩子个人信息</title>
'>
    <script><#--改版的样式，不适用adapt-->
        window.notUseAdapt=true;
    </script>
    <#include "../constants.ftl">
    <#escape x as x?html>
        <#noescape> ${buildAutoTrackTag("childinfo|childinfo_open", true)} </#noescape>
        <#if result.success>
            <#assign studentInfo = result.studentInfo  teacherInfos= result.teacherInfo![]>
                <div class="personalInfor-box">
                    <div class="pur-image pi-top">
                        <div class="img"><img src="${studentInfo.studentImg!publicDefaultUserImg}" alt=""></div>
                        <div class="name">${studentInfo.studentName!""}</div>
                    </div>
                    <div class="pi-list list-info">
                        <ul>
                            <li>
                                <div class="box">
                                    <b>学号：</b><span>${sid}</span>
                                </div>
                            </li>
                            <#if isGraduate!false ><#else><#--判断用户是否毕业-->
                                <li><div class="box"><b>已使用一起作业：</b><span>${studentInfo.useDayCount!0}天</span></div></li>
                            </#if>

                            <#if studentInfo.canChangeClazzLevel!false><#--判断用户是否可以编辑学校和班级-->
                                <li class="pi-arrow J-doClick doTrack" data-operate="switch-school" ${buildTrackData("m_1dib82tl|o_KhigDMOe")}>
                                    <div class="box">
                                        <b>学校：</b><span><#if (studentInfo.schoolName!"")== "">选择学校<#else>${studentInfo.schoolName}</#if></span>
                                    </div>
                                </li>
                                <li class="pi-arrow J-doClick doTrack" data-operate="switch-grade" ${buildTrackData("m_1dib82tl|o_x4gKFGQT")}>
                                    <div class="box">
                                        <b>年级：</b><span><#if (studentInfo.clazzName!"")== "">选择年级<#else>${studentInfo.clazzName}</#if></span>
                                    </div>
                                </li>
                            <#else>
                                <#if (studentInfo.schoolName!"") == ""&&(studentInfo.clazzName!"")== "">
                                    <li><div class="box"><b>班级：</b><span>暂无班级</span></div></li>
                                <#else>
                                    <li class="fck"><div class="box"><b>班级：</b><span>${studentInfo.schoolName!""}  ${studentInfo.clazzName!""}</span></div></li>
                                </#if>
                            </#if>

                            <#if isNewVer!false><#--兼容老版本-->
                                <li class="pi-arrow J-doClick doTrack" data-operate="switch-gender" ${buildTrackData("m_1dib82tl|o_ppj2bqg9")}>
                                    <div class="box">
                                        <b>性别：</b><span><#if (studentInfo.studentGender!"")== "">未选择<#else>${studentInfo.studentGender}</#if></span>
                                    </div>
                                </li>
                                <li class="pi-arrow J-doClick doTrack" ${buildTrackData("m_1dib82tl|o_JnR47Ru3")} data-operate="switch-age">
                                    <div class="box">
                                        <b>年龄：</b><span>${studentInfo.studentAge!"年/月/日"}<#if (studentInfo.studentAge!"")!="">岁</#if></span>
                                    </div>
                                </li>
                            </#if>
                        </ul>
                    </div>

                    <#if isGraduate!false ><#else><#--判断用户是否毕业-->
                        <#if studentInfo.hadClazz!false ><#--判断用户是否有班级-->
                            <#if teacherInfos?size gt 0>
                                <div class="pi-title">任课老师</div>
                                <div class="pi-list">
                                    <#assign subjectInfo = {
                                        "MATH" :{
                                            "className" : ""
                                        },
                                        "ENGLISH" :{
                                            "className" : "subEnglish"
                                        },
                                        "CHINESE" : {
                                            "className" : "subChinese"
                                        }
                                    }>
                                    <ul>
                                        <#list teacherInfos as teacherInfo>
                                            <#assign subjectInfoCache = subjectInfo[(teacherInfo.subject!'xxx')?upper_case]!{}>
                                            <li><i class="icon-subject ${subjectInfoCache.className!''}"></i><span>${teacherInfo.teacherName!""}老师</span></li>
                                        </#list>
                                    </ul>
                                </div>
                            </#if>
                        <#else>
                            <div class="pi-title">任课老师</div>
                            <div class="pi-main">
                                <p class="pi-info">暂未加入班级。登录一起作业学生App，输入老师号码加入班级，可免费接收和完成作业练习</p>
                                <a href="javascript:void(0);" class="btn-add J-doClick doTrack" data-operate="join-clazz" ${buildTrackData("m_1dib82tl|o_o9Kjds82")}>加入班级</a>
                                <a href="javascript:void(0);" class="getNumber J-doClick doTrack" data-operate="get-help-show" ${buildTrackData("m_1dib82tl|o_ZBtF9wOD")}>如何获得老师号码</a>
                            </div>

                            <div class="popUp-box" style="display: none;" id="J-get-help-box">
                                <div class="popInner" style="display: none;height: 20%;" id="J-get-help-box-inner">
                                    <div class="close J-doClick" data-operate="get-help-close"></div>
                                    <div class="title">如何获得老师号码？</div>
                                    <div class="content">
                                        <p>· 老师分享到微信、QQ或发给孩子的号码</p>
                                        <p>· 向老师询问是否有一起作业老师账号</p>
                                        <p>通过老师号码加入一起作业班级，可免费接收老师布置的作业和通知，随时查看学业表现</p>
                                    </div>
                                </div>
                            </div>
                        </#if>

                        <#if result.productList?exists>
                            <#assign productListes = result.productList![]>
                            <#if productListes?size == 0 >
                                <div class="pi-title">暂未开通课外学习产品</div>
                            <#else>
                                <div class="pi-title">已开通趣味学习产品</div>
                                <div class="pi-list">
                                    <ul>
                                        <#list productListes as product>
                                        <li>
                                            <a href="/parentMobile/ucenter/shoppinginfo.vpage?sid=${sid}&productType=${product.appKey!""}" class="right doTrack" ${buildTrackData("childinfo|interest_click")}>
                                                ${(product.isExpire!true)?string("已过期，去续费", "还剩" + (product.dayToExpire!0) + "天" )}
                                            </a>
                                            <div class="left">
                                                <#if product.productName?exists&&product.productName?length gt 0 >
                                                    <#if product.productName?index_of(" ") gt 0 >
                                                        <span>${product.productName?substring(0,product.productName?index_of(" "))}</span>
                                                    <#else>
                                                        <span>${product.productName}</span>
                                                    </#if>
                                                </#if>
                                                <#if product.usePlatformDesc?exists&&product.usePlatformDesc?length gt 0 >
                                                    <span class="icon-pc">${product.usePlatformDesc}</span>
                                                </#if>
                                            </div>
                                        </li>
                                        </#list>
                                    </ul>
                                </div>
                            </#if>
                        </#if>
                </div>
             </#if>
        <#else>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "../errorTemple/errorBlock.ftl">
        </#if>
    </#escape>

</@layout.page>
