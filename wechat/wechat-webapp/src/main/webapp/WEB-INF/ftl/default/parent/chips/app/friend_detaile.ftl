<#import "../../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="friend_details">
    <@sugar.capsule css=["drawing_update"] />
<style>
	body {
		backgrond: #fbdb77;
	}
    [v-cloak] { display: none }
    img {
    	display: block;
    	width: 100%;
    }
</style>
<div id="friend_energy" class="drawing_update clearfix" v-cloak="">
	<img class="top-bg" src="/public/images/parent/chips/app/drawing_update/bg.png" alt="">
	<img class="logo" src="/public/images/parent/chips/app/drawing_update/logo.png" alt="">
	<img class="bottom-bg" src="/public/images/parent/chips/app/drawing_update/bg.png" alt="">
    <div class="friends-bg02"></div>
    <div class="friends-bg03"></div>
    <div class="friends-wrapper clearfix">
        <div class="friendsPage">
            <div class="f-header">
                <img :src="avatar" alt="">
            </div>
            <div class="f-name">{{uname}}</div>
            <#--没有购买-->
            <div class="f-main clearfix" v-show="!buy">
                <div class="invite-bg"></div>
                <div class="invite-frient">
                    <div>Ta还未购买课程。</div>
                    <div>快来邀请Ta吧！</div>
                </div>
                <div class="invite-btn" @click="inviteFriend">邀请好友</div>
            </div>
            <#--购买过的-->
            <div class="f-main clearfix" v-show="buy">
                <ul class="energyBox clearfix">
                    <li class="energyList">
                        <div class="num">{{totalEnergy}}</div>
                        <div class="energyTitle">总能量</div>
                    </li>
                    <li class="energyList">
                        <div class="num">{{totalCard}}</div>
                        <div class="energyTitle">已获得</div>
                    </li>
                    <li class="energyList">
                        <div class="num">{{totalFinish}}</div>
                        <div class="energyTitle">已升级</div>
                    </li>
                </ul>
                <ul class="energyPicBox clearfix">
                    <li class="picList" v-for="(cardItem,index) in drawingList">
                        <img :src="cardItem.image" alt="">
                        <span class="lock_card" v-show="!cardItem.gain"><i class="icon_lock"></i> 你未获得</span>
                    </li>
                </ul>

            </div>

        </div>

    </div>
</div>

</@layout.page>

<#--</@chipsIndex.page>-->
