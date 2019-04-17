<#import './layout.ftl' as layout>

<@layout.page className='bgGray Index' pageJs='index' title="宝贝表现">

    <#escape x as x?html>
        <#include "constants.ftl">
        <#if isGraduate!false ><#--是否毕业判断-->
            <div class="parentApp-pathHeader parentApp-pathHeader-left">
                <div class="parentApp-pathHeader-head clearfix">
                    <div class="tp"><img src="${studentImgUrl!''}" alt=""></div>
                    <div class="ft">${studentName!''}</div>
                </div>
            </div>
            <div class="parentApp-messageNull">暂时不支持小学毕业账号</div>
        <#else>
            <#if had_kids!false> <#--是否有孩子判断-->
            <div class="parentApp-pathHeader parentApp-pathHeader-left">
                <div class="parentApp-pathHeader-head clearfix">
                    <div class="tp"><img src="${studentImgUrl!''}" alt=""></div>
                    <div class="ft">${studentName!''}</div>
                </div>
            </div>
            <div class="parentApp-pathHeader parentApp-pathHeader-white">
                <div class="parentApp-pathHeader-info">
                    <div>
                        <div class="hd">使用一起作业天数</div>
                        <div class="ft">${passDaysCount!0}<span>天</span></div>
                    </div>
                    <div>
                        <div class="hd">30天内完成作业</div>
                        <div class="ft">${monthFinishCount!0}<span>次</span></div>
                    </div>
                    <div>
                        <div class="hd">逾期未完成</div>
                        <div class="ft">${monthUnFinishCount!0}<span>次</span></div>
                    </div>
                </div>
            </div>
            <div  class="parentApp-pathHeader-br"></div>
           <div class="parentApp-learnTool">
                <div class="head">学习工具</div>
                <div class="list">
                    <a href="javascript:void(0);" data-operate="point_read" class="icon-1 do_ignore_href doTrack" data-track = "m_kwFidGWy|o_y56fsO8w" >
                        <div class="txt-1">英语课本点读</div>
                        <div class="txt-2">轻松跟读提高口语</div>
                    </a>
                    <a href="javascript:void(0);" class="icon-2 do_ignore_href doTrack"  data-operate="book_listen" data-track = "m_8TJZIRHI|o_VhCC4z5d">
                        <div class="txt-1">英语随身听</div>
                        <div class="txt-2">课本同步纯正原声</div>
                    </a>
                  <a href="javascript:void(0);" class="icon-3 do_ignore_href doTrack" data-operate="text_read" data-track = "m_QUIAp9oN|o_0ghKN7Td">
                        <div class="txt-1">语文课本朗读</div>
                        <div class="txt-2">附带朗读录音功能</div>
                    </a>
                </div>
            </div>
            <#else>
            <div class="parentApp-pathHeader parentApp-pathHeader-left">
                <div class="parentApp-pathHeader-head clearfix">
                    <div class="tp"><img src="" alt=""></div>
                    <div class="ft J-check-kids doTrack" data-track = "m_7Ku7HISV|o_12eVa2bq" data-from="add-child" data-from_type="child_addchild">添加孩子</div>
                </div>
            </div>
            <div class="parentApp-learnTool">
                <div class="head">学习工具</div>
                <div class="list">
                    <a href="javascript:void(0);" class="icon-1 J-check-kids doTrack" data-from="learn-tools" data-track = "m_kwFidGWy|o_y56fsO8w" data-from_type="child_point_read">
                        <div class="txt-1">英语课本点读</div>
                        <div class="txt-2">轻松跟读提高口语</div>
                    </a>
                   <a href="javascript:void(0);" class="icon-2 J-check-kids doTrack" data-track = "m_8TJZIRHI|o_VhCC4z5d" data-from="learn-tools" data-from_type="child_book_listen">
                         <div class="txt-1">英语随身听</div>
                        <div class="txt-2">课本同步纯正原声</div>
                    </a>
                    <a href="javascript:void(0);" class="icon-3 J-check-kids" data-from="learn-tools" data-from_type="child_text_read">
                        <div class="txt-1">语文课本朗读</div>
                        <div class="txt-2">附带朗读录音功能</div>
                    </a>
                </div>
            </div>
            <div class="parentApp-wrap">
                <ul class="parentApp-homeLinkList parentApp-homeLinkList-purpleIcon">
                <#assign homeLinkList = [
                    {
                        "link" :  'javascript:void(0)',
                        "name" : '英语随身听',
                        "ignore_href" : true,
                        "className" : 'walkman',
                        "trackInfo" : "m_8TJZIRHI|o_VhCC4z5d",
                        "memo" : '课本同步纯正原声',
                        "gray"  : false
                    }
                ]
                >
                <#list homeLinkList as linkInfo>
                    <#if linkInfo.gray!true>
                    <li style="border: none;">
                        <a href="${linkInfo.link}" class="doTrack J-check-kids" data-track = "${linkInfo.trackInfo!""}" data-from_type="child_book_listen" data-from="learn-tools">
                            <span class="ico-1 ico-${linkInfo.className}"></span>
                            <span class="ico-2"></span>
                            <div class="hd">${linkInfo.name}</div>
                            <p class="ft">${linkInfo.memo}</p>
                        </a>
                    </li>
                    </#if>
                </#list>
                </ul>
            </div>
            </#if>
        </#if>
    </#escape>

</@layout.page>
