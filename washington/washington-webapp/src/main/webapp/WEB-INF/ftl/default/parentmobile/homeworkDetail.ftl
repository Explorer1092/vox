<#import './layout.ftl' as layout>

<@layout.page className='HomeworkDetail' title="作业详情" pageJs="homework">

    <#assign ht = (ht!"")?trim legalHt = ht?has_content>
    <#assign pidShow = (pid%2)!0>
    <#if result.success >
		<#macro dateFormat sconds = 0>
		${(sconds/60)?int!''}分${(sconds%60)?int!''}秒
		</#macro>
        <#assign topType = "topTitle">
        <#assign topTitle = "作业详情">
        <#include "./top.ftl" >
        <#assign detail = result.detail >

		<#if !(detail.teacherName??) && !(detail.comment??) && ((detail.wrongCount!0) lte 0)>
			<#assign tipType = "nomessage" tipText = "暂无错题">
			<#include "./tip.ftl">
		<#else>
			<div class="homework_English_box">
				<#if (showPayCourse!false) && pidShow gt 0>
					<div style="margin-top: 20px;">
						<a href="/parentMobile/activity/paycourse.vpage?shopId=36&sid=${sid}" data-track="APP_homework_report|lesson_click" class="doTrack">
							<img src="<@app.link href="/public/skin/parentMobile/images/activity/paycourse/me_banner.png"/>" alt="" width="100%">
						</a>
					</div>
				</#if>

				<div class="report-fraction-box" style="margin-top: 30px;">
					<div class="rf-left">
						<h3 class="k-title">总得分</h3>
						<div class="k-fraction">
						${detail.score!0}
						</div>
					</div>
					<div class="rf-right">
						<table class="report-table">
							<tbody>
							<tr class="title"> <td>班级最高分</td> <td>个人用时</td> </tr>
							<tr> <td>${detail.maxScore!0}</td> <td><@dateFormat (detail.duration)!0></@dateFormat></td> </tr>
							<tr class="title"> <td>班级平均分</td> <td>平均用时</td> </tr>
							<tr> <td>${detail.averageScore!0}</td> <td><@dateFormat (detail.averageDuration)!0></@dateFormat></td> </tr>
							</tbody>
						</table>
					</div>
				</div>

				<#assign commentTeacherName = detail.teacherName!"" comment = detail.comment!"">
				<#if comment != "" && commentTeacherName != "">
					<div class="report-box">
						<h2 class="title-back-5">老师评语</h2>
						<div class="write" style="height: auto;">
							<div class="comment">
								<div class="cont"> ${comment}</div>
								<div class="foot">${commentTeacherName}</div>
							</div>
						</div>
					</div>
				</#if>

				<#assign isEnglish = (ht!"")?upper_case?index_of("ENGLISH") gt -1>
				<#if isEnglish && (detail.audios)?? && detail.audios?size gt 0>
					<div id="playersList">
						<h3 class="font_28 text_blue"><i class="icon_blue icon_blue_02"></i>孩子朗读</h3>
						<div data-widget="table" class="w_table">
							<table>
								<thead>
									<tr>
										<td>题型</td>
										<td>录音</td>
									</tr>
								</thead>
								<tbody>
									<#list result.detail.audios as audio>
										<tr>
											<td>${audio.practiceType}</td>
											<td>
												<i class="icon-c icon-n-play doPlayAudio doTrack" data-track="hwdetail|en_play" data-audio_src="${audio.audio}"></i>
											</td>
										</tr>
									</#list>
								</tbody>
							</table>
						</div>
					</div>
				</#if>

				<#assign prize = detail.prize!0>
				<#if prize gt 0>
					<div class="report-box">
						<h2 class="title-back-6">老师奖励</h2>
						<div class="write write-teacherAward">
							<div><p>获得了老师设置的<span>${prize}</span>学豆奖励</p></div>
						<#--<div>
								<a href="/parentMobile/homework/giveBean.vpage?sid=${sid}" style="background-color: #b9d55e;">给班级贡献学豆</a>
							</div>
							-->
						</div>
					</div>
				</#if>



				<#if (detail.wrongCount!0) gt 0>
					<div class="report-box">
						<h2 class="title-back-5">本次错题</h2>
						<div class="write">
							<div><p><span>${detail.wrongCount}</span>道错题</p></div>
							<div>
                                <#assign trackData = buildTrackData("hwdetail|" + (isEnglish?string('en','math')) + "_viewfault_click")>
                                <a href="/parentMobile/homework/wrongQuestionDetail.vpage?sid=${sid}&homeworkType=${ht!''}&homeworkId=${hid!''}" ${trackData} class="doTrack" style="background-color: #359bff;">查看</a>
							</div>
						</div>
					</div>
				</#if>

				<#if (showPayCourse!false) && pidShow lte 0>
                    <div>
                        <div class="report-box">
                            <h2 class="title-back-7">名师讲堂</h2>
                            <div class="write">
                                <div><p><span>3</span>小时搞定小升初数学测评道错题</p></div>
                                <a href="/parentMobile/activity/paycourse.vpage?shopId=36&sid=${sid}" style="background-color: #359bff;float: right;" data-track="APP_homework_report|lesson_btn_click" class="doTrack">查看</a>
							</div>
                        </div>
                    </div>
				</#if>

				<div id="banner-220202"></div>
				<@sugar.capsule js=['voxSpread']/>

			</div>
		</#if>
    <#else>
        <#assign info = result.info errorCode = result.errorCode>
        <#include "errorTemple/errorBlock.ftl">

        <#if legalHt>
            <#assign trackType = (ht == "ENGLISH")?string("en", "math") >
            <a href="javascript:;" class="hide doAutoTrack" data-track = "hwdetail|hwdetail_${trackType}_fail"></a>
        </#if>
    </#if>
	<#if (showPayCourse!false) && pidShow gt 0>
		<p class="doAutoTrack hide" data-track="APP_homework_report|lesson_show"></p>
	</#if>
	<#if (showPayCourse!false) && pidShow lte 0>
		<p class="doAutoTrack hide" data-track="APP_homework_report|lesson_btn_show"></p>
	</#if>
</@layout.page>
