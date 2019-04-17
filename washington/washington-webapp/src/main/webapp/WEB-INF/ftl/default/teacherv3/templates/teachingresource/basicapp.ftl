<script type="text/html" id="T:BASIC_APP">
    <div class="basicExercises" v-if="basicInfos.length>0" data-comment="基础练习">
        <div class="h-topicPackage">
            <div class="topicBox">
                <ul>
                    <li v-for="(basic,index) in basicInfos"
                        v-bind:class="{'active':index == focusCategoryGroupIndex}"
                        v-on:click="categoryGroupClick(index)">
                            <p v-text="basic.groupName"></p>
                    </li>
                </ul>
            </div>
            <div class="line"></div>
        </div>
        <div class="e-lessonsBox">
            <div class="e-lessonsList" v-if="basicInfos[focusCategoryGroupIndex] && basicInfos[focusCategoryGroupIndex].lessons" v-for="(lessons,index) in basicInfos[focusCategoryGroupIndex].lessons">
                <div class="el-title" v-text="lessons.lessonName"></div>
                <div class="el-name" v-text="covertSentences(lessons.sentences)"></div>
                <div class="el-list">
                    <ul>
                        <li v-for="categories in lessons.categories">
                            <a href="javascript:void(0);" v-on:click="previewDetail(categories,lessons.lessonId)" class="preview" style="position: absolute;width: 100%;height: 100%;top:0;left:0;"></a>
                            <div class="lessons-text" >
                                <i class="e-icons">
                                    <img v-bind:src="imgDomain+'/public/skin/teacherv3/images/homework/english-icon/e-icons-'+categories.categoryIcon+'.png'">
                                </i>
                                <span class="text" v-text="categories.categoryName"></span>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</script>