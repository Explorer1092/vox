<#import './layout.ftl' as layout>

    <@layout.page className='HomeworkReport' globalJs=["DateExtend"] title="错题本" pageJs="homeworkReport">

    <#escape x as x?html>

        <#assign tabInfo = [
        {
            "name" : '英语作业',
            "subject" : 'ENGLISH',
            "trackInfo" : "entab_click"
        },
        {
            "name" : '数学作业',
            "subject" : 'MATH',
            "trackInfo" : "mathtab_click"
        },
        {
			"name" : '语文作业',
			"subject" : 'CHINESE',
			"trackInfo" : "mathtab_click"
        }
        ]>
        <#assign topType = "topTitle">
		<#assign topTitle = "错题本">
		<#include "./top.ftl" >

        <div class="doTabBlock">
			<div class="parentApp-topBar doTop parentApp-topBarAndroid">
				<div class="topBox" style="top: 81px;" id="J-do-title-topBox">
					<div class="topTab">
						<#list tabInfo as tab >
							<a
								href = "javascript:;"
								class="doTab doTrack <#if tab_index == 0 >active</#if>"
								data-track = "report|faultnotes_${tab.trackInfo}"
								data-subject = "${tab.subject}"
								data-tab_local = "nullTab"
								>${tab.name}
							</a>
						</#list>
					</div>
				</div>
			</div>
        </div>
        <em style="display: none;" id="J-do-homeworkReport-page"></em>
        <#noescape>${buildAutoTrackTag("report|faultnotes_open", true)}</#noescape>

        <#if result.success>

            <div class="doTabBlock">
                <div id="doFullSecondTab">
                    <div class="parentApp-messageTab">
                    </div>
                </div>

                <div id="tabContent">
                </div>
            </div>

            <#include "homeworkReportTemplate.ftl">

        <#else>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">

            <#noescape>${buildAutoTrackTag("report|faultnotes_error", true)}</#noescape>

            <p class="hide doAutoTrack" data-track="report|fail"></p>
        </#if>

    </#escape>

</@layout.page>

