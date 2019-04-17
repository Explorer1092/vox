<#import '../layout.ftl' as layout>

<@layout.page className='Classes bg-fff' pageJs='classes' title="家长签到" specialCss="skin2" specialHead='
   	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
	<meta name="format-detection" content="telephone=no" />
	<meta name="format-detection" content="email=no" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>家长签到</title>
'>
	<#assign isUseNewTitle = true><#--使用新的UI2 title-->
	<script><#--改版的样式，不适用adapt-->
		window.notUseAdapt=true;
	</script>
	<#assign topType = "topTitle" topTitle = '家长签到'>
	<#include "../top.ftl" >
	<#if isGraduate!false><#--是否毕业判断-->
		<div class="null-box">
			<div class="no-account"></div>
			<div class="null-text">暂不支持小学毕业账号</div>
		</div>
	<#else>
			<#assign ajaxBaseUrl = "/parentMobile/rank/__page__.vpage?sid=${sid!''}" trackModule="classrank">
			<#escape x as x?html>
				<#assign tabs = [
				{
				"name" : "家长动态榜",
				"local" : "parent",
				"ajax"  : ajaxBaseUrl?replace("__page__", "dynamic"),
				"track" : "jzaction_open",
				"module" : "parent_state"
				},
				{
				"name" : "学豆奖励榜",
				"local" : "student",
				"ajax"  : ajaxBaseUrl?replace("__page__", "studentrewardrank"),
				"track" : "reward_open",
				"module" : "student_result"
				}
				]>
            <div class="doTabBlock" style="display: none;">
				<div class="l-tab">
					<#list tabs as tab>
                        <span class="do_tab_control doTrack"  ${buildTrackData(trackModule + '|' + tab.track)} data-tab_local="${tab.tab_local!''}" data-tab_cache="1" data-tab_target_el=".tab_content" data-tab_template_el="#${tab.local}" data-tab_ajax_url="${tab.ajax}">${tab.name}</span>
					</#list>
				</div>
            </div>
			</#escape>

			<div class="tab_content">

			</div>

			<#list tabs as tab>
			<script id="${tab.local}" type="text/html">
				<#include "./modules/" + tab.module + ".ftl">
			</script>
			</#list>

			<div style="display:none;" id="studentrewardrank">
				<div class="null-box">
					<div class="parentApp-pathNull">数据更新中</div>
				</div>
			</div>

			<div class="popUp-box" style="display:none;" id="J-get-help-box">
				<div class="popInner" id="J-get-help-box-inner">
					<div class="close" id="J-do-hide-pop"></div>
					<div style="height: 100%;overflow-y: auto;">
						<div class="title">学豆奖励榜</div>
						<div class="content">
							<p> 1、学豆奖励榜来自于老师发放的学豆奖励，以及家长在家长通APP“作业动态”和“家长动态榜”中为学生领取的学豆奖励</p>
							<p> 2、学豆奖励榜按本月获得的学豆总数进行排名，总数相同时依次按老师奖励、家长领取奖励、领取时间及加入班级时间排名</p>
						</div>
						<div class="title">家长动态榜</div>
						<div class="content">
							<P>排名规则：</P>
							<P>1、家长动态榜反应了班级中家长们对孩子学业的关注程度，按本月送花数排名，送花数相同时按送花时间及加入班级时间排名</P>
							<P>2、凡购买过付费产品的用户都享有VIP标识，若购买产品已过期则VIP标识不被点亮</P>
							<P>3、有1位以上不同身份的家长在“家长通APP”－“家长动态榜”中签到，将享有“双倍关注”标识，还可领取家长签到奖励</P>
							<P>家长学豆兑换规则:</P>
							<P>1、每月在“家长通APP”－“家长动态榜”中“签到”可获得奖励学豆：1位家长本月签到奖5学豆，2位不同身份家长本月签到奖20学豆</P>
							<P>2、家长每月可领取上月签到奖励的学豆，当月没有领取的学豆将在下月清零</P>
							<P>3、家长在作业完成后送给老师的鲜花可转换为班级学豆，老师每月根据上月家长送花数可领取班级学豆用于奖励给本班学生</P>
						</div>
                    </div>
				</div>
			</div>

		<div id="no_bind_clazz" class="hide">
        	<div class="null-box">
				<div class="no-list"></div>
				<div class="null-text">未加入班级，请找老师申请加入吧。</div>
            </div>
		</div>

		<script>
			PM.default_user_image = "${publicDefaultUserImg}";
			window.tab_index = "${tab_index!0}";
		</script>
	</#if>

	<style>
		.parentApp-layerBox .layerInner{
			width: 76%;
			left: 47%;
			margin: -25% 0 0 -35%;
			border-radius: 0.875rem;
		}
		.header{
			padding: 2.19rem 0 0;
		}
		.layerMain.content{
			padding: 0.43rem 1rem 0 1.2rem;
		}
		.content>p{
			font-size: 0.7rem;
		}
		.parentApp-layerBox .layerFoot{
            padding: 1rem 1.6rem;
		}
		.footer a{
			color: #fff;
			background: #41bb54;
			padding: 0.6rem 1.6rem;
			border-radius: 1.66rem;
			font-size: 0.79rem;
		}

	</style>
	<div id="popup_modal" class="popup_modal_block" style="z-index: 28;"></div>
	<div  class="parentApp-layerBox popup_block" tabindex="0" style="left: 0px; display: block; top: 0px; z-index: 30;">
		<div class="layerInner">
			<div class="layerHead header">
				<p class="popup_title">公告</p>
			</div>
			<div class="layerMain content">
				<p>亲爱的家长，由于一起作业平台整体产品线升级，"家长签到”已于2017年6月30日下线。</p>
				<p>推荐您升级到最新版家长通，新版中将为您和孩子提供更多的学习奖励。</p>
			</div>
			<div class="layerFoot footer">
				<a href="http://wx.17zuoye.com/download/17parentapp?cid=203005">立即升级</a>
			</div>
		</div>
	</div>
</@layout.page>
