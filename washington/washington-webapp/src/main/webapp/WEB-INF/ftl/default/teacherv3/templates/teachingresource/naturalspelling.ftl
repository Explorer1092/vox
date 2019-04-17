<script type="text/html" id="T:NATURAL_SPELLING">
    <div class="phonicsBox" data-comment="自然拼读" v-bind:class="{'pictureBottom':focusCategoryGroupIndex == 1,'spacingBottom':springSwitch && focusCategoryGroupIndex == 0}">
        <div class="picutreBottomCard" style="display: none" v-show="isLoading && !noNetWork">
            <div class="contentLeafSpring" v-bind:style="{height: springSwitch ? '360px' : 0}"></div>
            <div class="phonicsTitleCard">
                <div class="phonicsRecommend" v-bind:class="{'active':focusCategoryGroupIndex == 0}" @click="categoryGroupClick(0)"><i></i><span>推荐自然拼读</span></div>
                <div class="phonicsWhole" v-bind:class="{'active':focusCategoryGroupIndex == 1}" @click="categoryGroupClick(1)"><i></i><span>全部自然拼读</span></div>
            </div>
            <!--推荐自然拼读-->
            <div class="phonicsRecommendCard" style="display: none" v-show="focusCategoryGroupIndex == 0">
                <div class="phonicsCard" v-if="recommendList">
                    <div class="e-lessonsTitle" v-text="recommendList.unitName"></div>
                    <div class="e-lessonsBox" v-if="recommendList.lessons && recommendList.lessons.length>0">
                        <div class="e-lessonsList" v-for="lessons in recommendList.lessons">
                            <div class="el-title" v-text="lessons.lessonName"></div>
                            <div v-if="lessons.categoryGroups && lessons.categoryGroups.length>0" v-for="categoryGroups in lessons.categoryGroups">
                                <div class="el-name" v-if="categoryGroups.sentences && categoryGroups.sentences.length>0">
                                    <p v-for="sentences in categoryGroups.sentences" v-text="sentences"></p>
                                </div>
                                <div class="el-list" v-if="categoryGroups.categories && categoryGroups.categories.length>0">
                                    <ul>
                                        <li v-for="categories in categoryGroups.categories" @click="previewNatural(categories,lessons.lessonId)">
                                            <div class="lessons-text previewText">
                                                <i class="e-icons">
                                                    <img :src='categoryIconPrefixUrl + "e-icons-" + categories.categoryIcon + ".png"'>
                                                </i>
                                                <span class="text" v-text="categories.categoryName"></span>
                                            </div>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tipsCard2" v-if="recommendList == null">
                    <div class="tipsPic noResources"></div>
                    <p class="tipsCon">暂无资源</p>
                </div>
            </div>
            <!--全部自然拼读-->
            <div class="phonicsWholeCard" style="display: none" v-show="focusCategoryGroupIndex == 1">
                <ul class="levelChoice" v-if="levelList.length>0">
                    <span class="levelCon">Level选择：</span>
                    <li v-for="(level,index) in levelList" v-text="level.levelName" v-bind:class="{'active':focusCategoryGroupIndex2 == index}" @click="categoryGroupClick2(index)"></li>
                </ul>
                <div class="phonicsCard" v-if="wholeContentList && wholeContentList.length>0">
                    <div class="e-lessonsTitle" v-text="wholeContentList[pageNum-1].unitName"></div>
                    <div class="e-lessonsBox" v-if="wholeContentList[pageNum-1].lessons && wholeContentList[pageNum-1].lessons.length>0">
                        <div class="e-lessonsList" v-for="lessons in wholeContentList[pageNum-1].lessons">
                            <div class="el-title" v-text="lessons.lessonName"></div>
                            <div v-if="lessons.categoryGroups && lessons.categoryGroups.length>0" v-for="categoryGroups in lessons.categoryGroups">
                                <div class="el-name" v-if="categoryGroups.sentences && categoryGroups.sentences.length>0">
                                    <p v-for="sentences in categoryGroups.sentences" v-text="sentences"></p>
                                </div>
                                <div class="el-list" v-if="categoryGroups.categories && categoryGroups.categories.length>0">
                                    <ul>
                                        <li v-for="categories in categoryGroups.categories" @click="previewNatural(categories,lessons.lessonId)">
                                            <div class="lessons-text previewText">
                                                <i class="e-icons">
                                                    <img :src='categoryIconPrefixUrl + "e-icons-" + categories.categoryIcon + ".png"'>
                                                </i>
                                                <span class="text" v-text="categories.categoryName"></span>
                                            </div>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tipsCard2" v-if="wholeContentList && wholeContentList.length<=0">
                    <div class="tipsPic noResources"></div>
                    <p class="tipsCon">暂无资源</p>
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
        </div>
        <div class="tipsCard" v-if="!isLoading || noNetWork">
            <div class="tipsPic" v-bind:class="{'noNetWork':noNetWork,'isLoading':!isLoading}"></div>
            <p class="tipsCon" v-if="!isLoading" v-text="'加载中...'"></p>
            <p class="tipsCon" v-if="noNetWork" v-text="'当前网络异常,请刷新页面'"></p>
            <div class="refreshBtn" v-if="noNetWork" @click="refreshPage">点击刷新页面</div>
        </div>
    </div>
</script>