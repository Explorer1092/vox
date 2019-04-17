<script type="text/html" id="T:LEVEL_READINGS">
    <div class="pictureBookBox" data-comment="绘本" v-bind:class="{'pictureBottom':focusCategoryGroupIndex == 1,'spacingBottom':springSwitch && focusCategoryGroupIndex == 0}">
        <div class="picutreBottomCard" style="display: none" v-show="isLoading && !noNetWork">
            <div class="contentLeafSpring" v-bind:style="{height: springSwitch ? '360px' : 0}"></div>
            <div class="phonicsTitleCard">
                <div class="phonicsRecommend" v-bind:class="{'active':focusCategoryGroupIndex == 0}" @click="categoryGroupClick(0)"><i></i><span>推荐绘本</span></div>
                <div class="phonicsWhole" v-bind:class="{'active':focusCategoryGroupIndex == 1}" @click="categoryGroupClick(1)"><i></i><span>全部绘本</span></div>
            </div>
            <!--推荐绘本-->
            <div class="pictureBookBox" style="display: none" v-show="focusCategoryGroupIndex == 0">
                <div class="pictureBook" v-for="(moduleObj,index) in recommendList" v-if="recommendList && recommendList.length>0">
                    <p class="pictureBookTitle"><span v-text="moduleObj.moduleName"></span> <i v-text="moduleObj.description"></i></p>
                    <div class="pictureBookCon">
                        <div class="arrow arrow-l" style="display: none;"
                             v-show="moduleObj.beginPos > 0"
                             v-on:click.stop="arrowLeftClick(moduleObj)"><i class="icon"></i></div>
                        <div class="arrow arrow-r" style="display: none;"
                             v-show="(moduleObj.beginPos + moduleObj.moveCount) < moduleObj.totalCount"
                             v-on:click.stop="arrowRightClick(moduleObj)"><i class="icon"></i></div>
                        <ul v-bind:style="{position:'absolute', transition:'left 2s', left: (30 - moduleObj.beginPos * 250) + 'px', width:(moduleObj.pictureBookList.length * 250 + 'px')}">
                            <!--每个li宽度是250px-->
                            <li class="e-pictureList" v-for="(reading,zIndex) in moduleObj.pictureBookList" v-on:click="previewReading(reading)">
                                <div class="lPic">
                                    <img  v-bind:src="reading.pictureBookImgUrl">
                                </div>
                                <div class="rInfo">
                                    <div class="title" v-text="reading.pictureBookName"></div>
                                    <p class="text" v-text="reading.pictureBookClazzLevelName"></p>
                                    <p class="text" v-text="reading.pictureBookTopics.join('、')"></p>
                                    <p class="text" v-text="reading.pictureBookSeries"></p>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="tipsCard2" v-if="recommendList && recommendList.length<=0">
                    <div class="tipsPic noResources"></div>
                    <p class="tipsCon">暂无资源</p>
                </div>
            </div>
            <!--全部绘本-->
            <div class="pictureWhole" style="display: none" v-show="focusCategoryGroupIndex == 1">
                <div class="h-tab-box" v-if="choiceList.length>0">
                    <div class="t-homework-form t-tab-box">
                        <!-- showAll显示所有-->
                        <dl class="J_filter-clazzLevels theme-box" v-bind:class="{'showAll':isShowAll.indexOf('clazzLevels') != -1}" v-if="choiceList[0].clazzLevelList && choiceList[0].clazzLevelList.length>0">
                            <dt v-text="subject == 'CHINESE' ? '等级：':'年级：'"></dt>
                            <dd>
                                <div class="t-homeworkClass-list">
                                    <div class="pull-down">
                                        <p class="label-check" v-bind:class="{'label-check-current':levelIds===''}" @click="unlimitClick('clazzLevels')"><span class="label">不限</span></p>
                                        <div class="side">
                                            <p class="filter-item" v-for="clazzLevel in choiceList[0].clazzLevelList"
                                               v-bind:class="{'w-radio-current':levelIds===clazzLevel.levelId}"
                                               @click="addOrCancelLevel(clazzLevel.levelId)"
                                            >
                                                <span class="w-radio"></span><span class="w-icon-md" v-text="clazzLevel.levelName"></span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <a href="javascript:void(0)" v-if="isShowIcon.clazzLevels" class="arrow-show-icon" @click="showAll('clazzLevels')"></a>
                            </dd>
                        </dl>
                        <dl class="J_filter-topics theme-box" v-bind:class="{'showAll':isShowAll.indexOf('topic') != -1}" v-if="choiceList[0].topicList && choiceList[0].topicList.length>0">
                            <dt>主题：</dt>
                            <dd>
                                <div class="t-homeworkClass-list">
                                    <div class="pull-down">
                                        <p class="label-check" v-bind:class="{'label-check-current':topicIds.length===0}" @click="unlimitClick('topic')"><span class="label">不限</span></p>
                                        <div class="side">
                                            <p class="filter-item" v-for="topic in choiceList[0].topicList"
                                               v-bind:class="{'w-radio-current':topicIds.indexOf(topic.topicId) != -1}"
                                               @click="addOrCancelTopic(topic.topicId)"
                                            >
                                                <span class="w-checkbox"></span>
                                                <span class="w-icon-md" v-text="topic.topicName"></span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <a href="javascript:void(0)" v-if="isShowIcon.topics" class="arrow-show-icon" @click="showAll('topic')"></a>
                            </dd>
                        </dl>
                        <dl class="J_filter-series theme-box" v-bind:class="{'showAll':isShowAll.indexOf('series') != -1}" v-if="choiceList[0].seriesList && choiceList[0].seriesList.length>0">
                            <dt>系列：<span class="book-new"></span></dt>
                            <dd>
                                <div class="t-homeworkClass-list">
                                    <div class="pull-down">
                                        <p class="label-check" v-bind:class="{'label-check-current':seriesIds.length===0}" @click="unlimitClick('series')"><span class="label">不限</span></p>
                                        <div class="side">
                                            <p class="filter-item" v-for="series in choiceList[0].seriesList"
                                               v-bind:class="{'w-radio-current':seriesIds.indexOf(series.seriesId) != -1}"
                                               @click="addOrCancelSeries(series.seriesId)"
                                            >
                                                <span class="w-checkbox"></span>
                                                <span class="w-icon-md" v-text="series.seriesName"></span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <a href="javascript:void(0)" v-if="isShowIcon.series" class="arrow-show-icon" @click="showAll('series')"></a>
                            </dd>
                        </dl>
                    </div>
                </div>
                <div v-if="pictureBookList && pictureBookList.length>0">
                    <div class="pictureWholeBook">
                        <ul>
                            <li class="e-pictureList" v-for="reading in pictureBookList" v-on:click="previewReading(reading)">
                                <div class="lPic">
                                    <img  v-bind:src="reading.pictureBookImgUrl">
                                </div>
                                <div class="rInfo">
                                    <div class="title" v-text="reading.pictureBookName"></div>
                                    <p class="text" v-text="reading.pictureBookClazzLevelName"></p>
                                    <p class="text" v-text="reading.pictureBookTopics.join('、')"></p>
                                    <p class="text" v-text="reading.pictureBookSeries"></p>
                                </div>
                            </li>
                        </ul>
                    </div>
                    <div class="homework_page_list" v-if="pageCount>0">
                        <a href="javascript:void(0);" v-if="pageCount>1" v-bind:class="{'disable':pageNum<=1,'enable':pageNum>1}" @click="previousPage"><span>上一页</span></a>
                        <span v-if="pageCount>pageShow">
                            <a v-if="pageNum>(pageShow/2)" @click="pageClick(1)"><span>1</span></a>
                            <span v-if="pageNum>(pageShow/2)+1" class="points">...</span>
                            <a v-bind:class="{'this': page === pageNum}" v-for="(page,index) in pageList" @click="pageClick(page)"><span v-text="page"></span></a>
                            <span v-if="pageNum+(pageShow/2)<pageCount" class="points">...</span>
                            <a v-if="pageNum+(pageShow/2)<pageCount"><span v-text="pageCount" @click="pageClick(pageCount)"></span></a>
                        </span>
                        <span v-else-if="pageCount<=pageShow">
                            <a v-bind:class="{'this': (index+1) === pageNum}" v-for="(page,index) in pageCount" @click="pageClick(index+1)"><span v-text="index+1"></span></a>
                        </span>
                        <a href="javascript:void(0);" v-if="pageCount>1" v-bind:class="{'disable':pageNum>=pageCount,'enable':pageNum<pageCount}" @click="nextPage"><span>下一页</span></a>
                    </div>
                </div>
                <div class="tipsCard2" v-if="pictureBookList && pictureBookList.length<=0">
                    <div class="tipsPic noResources"></div>
                    <p class="tipsCon">暂无资源</p>
                </div>
            </div>
        </div>
        <#--<div class="tipsCard" v-if="!isLoading">
            <div class="tipsPic isLoading"></div>
            <p class="tipsCon" v-text="'正在加载…'"></p>
        </div>-->
        <div class="tipsCard" v-if="!isLoading || noNetWork">
            <div class="tipsPic" v-bind:class="{'noNetWork':noNetWork,'isLoading':!isLoading}"></div>
            <p class="tipsCon" v-if="!isLoading" v-text="'加载中...'"></p>
            <p class="tipsCon" v-if="noNetWork" v-text="'当前网络异常,请刷新页面'"></p>
            <div class="refreshBtn" v-if="noNetWork" @click="refreshPage">点击刷新页面</div>
        </div>
    </div>
</script>