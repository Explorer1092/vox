<#import "layout.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["vue"] css=["plugin.venus-pre","teachingresource.wordrecognitionandreadingdetail"]/>
<style type="text/css">
    .wordReadingDetail, .contentLeafSpring{
        transition: height 1s;
        -moz-transition: height 1s; /* Firefox 4 */
        -webkit-transition: height 1s; /* Safari 和 Chrome */
        -o-transition: height 1s; /* Opera */
    }
</style>
<div id="wordContainerBox" class="wordContainerBox" v-cloak>
    <div class="wordContainerCard">
        <div class="wordReadingDetail">
            <div class="contentLeafSpring" v-bind:style="{height: springSwitch ? '360px' : 0}"></div>
            <div class="readingTitle">
                <#--<p class="titleInfo" v-text="lessonName"></p>-->
                <p class="readingCon">共有{{questionList.length}}个生字：跟读、音节笔顺、部首、结构、字义、组词</p>
            </div>
            <div class="readingBox">
                <div class="readingCard">
                    <div class="wordCard">
                        <div class="sz-box">
                            <div class="top">
                                <div class="top_bg"></div>
                            </div>
                            <ul class="bottom">
                                <li class="li-1"></li>
                                <li class="li-2"></li>
                                <li class="li-3"></li>
                                <li class="li-4"></li>
                            </ul>
                            <ul class="sz-content">
                                <li class="c-li1" v-text="wordFocus.wordContentPinyinMark"></li>
                                <li class="c-li2">
                                    <div>
                                        <img :src="wordFocus.chineseWordImgUrl" style="width: 100%;height: 100%;">
                                    </div>
                                </li>
                                <li class="audioPlay" style="cursor: pointer;" :class="{'audioPause': isPlay}" @click="playAudio(wordFocus.chineseWordAudioUrl)"></li>
                            </ul>
                        </div>
                    </div>
                    <audio style="display:none;" id="bgFile" style="width: 100%;"></audio>
                    <div class="readinfInfoCard">
                        <div class="readingInfo">
                            <p class="interpreCon" v-text="wordFocus.chineseWordStrokesOrder"></p>
                        </div>
                        <div class="readingInfo">
                            <p class="interpreCon">共有{{wordFocus.chineseWordStrokes}}画 l {{wordFocus.chineseWordStructure}} l {{wordFocus.chineseWordRadical}}部首</p>
                        </div>
                        <div class="readingInfo">
                            <p class="interpre">释义</p>
                            <p class="interpreCon" v-text="wordFocus.wordExplain ? wordFocus.wordExplain : '无'"></p>
                        </div>
                        <div class="readingInfo">
                            <p class="interpre">组词</p>
                            <p class="interpreCon" v-text="wordFocus.wordWords ? wordFocus.wordWords : '无'"></p>
                        </div>
                    </div>
                </div>
                <div class="wordListCard" v-if="questionList && questionList.length>0">
                    <div class="singleWord arrowRight" style="display: none;" v-show="Math.abs(wordLeft)/sigleWordLength<=(questionList.length-moveNum)" @click="arrowLeftClick"></div>
                    <div class="singleWord arrowLeft" style="display: none;" v-show="Math.abs(wordLeft)/sigleWordLength>=1" @click="arrowRightClick"></div>
                    <div class="wordScroll">
                        <ul class="wordList" v-bind:style="{position:'absolute', transition:'left 2s', left: wordLeft + 'px',width:(questionList.length *102 + 'px')}">
                            <#--每个li宽度102px-->
                            <li class="singleWord" v-for="(questionCon,index) in questionList"  :class="{'active': focusIndex == index}" @click="changeWord(questionCon.id)">
                                <div class="sz-box">
                                    <ul class="bottom">
                                        <li class="li-1"></li>
                                        <li class="li-2"></li>
                                        <li class="li-3"></li>
                                        <li class="li-4"></li>
                                    </ul>
                                    <ul class="sz-content">
                                        <li class="c-li2">
                                            <div v-text="questionCon.content.subContents[0].extras.chineseWordContent"></div>
                                        </li>
                                    </ul>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="turnLeft" @click="wordCardMove('right')"></div>
                <div class="turnRight" @click="wordCardMove('left')"></div>
            </div>
        </div>
        <div class="containerBottom">
            <div class="dropDown" v-on:click="springSwitchClick" v-text="springSwitch ? '收起' : '下拉'"></div>
        </div>
    </div>
</div>
    <@sugar.capsule js=["teachingresource.wordrecognitionandreadingdetail"]/>
</@shell.page>
