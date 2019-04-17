<script id="T:LEVEL_CLAZZS_TPL" type="text/html">
    <div id="level_and_clazzs" class="t-homework-form" style="overflow: visible;">
        <dl>
            <dt>班级</dt>
            <dd style="overflow: hidden; *zoom:1; position: relative;">
                <div class="w-border-list t-homeworkClass-list">
                    <ul>
                        <li v-for="(levelObj,index) in levelList" v-on:click="levelClick(levelObj)" v-bind:class="{'active': levelObj.clazzLevel == focusLevel}" class="v-level" v-text="levelObj.name"></li>

                        <li class="pull-down">
                            <p v-bind:class="{'w-checkbox-current' : isAllChecked}" v-on:click="chooseOrCancelAll">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md textWidth">全部</span>
                            </p>
                            <p v-for="(clazzObj,zIndex) in levelClazzList" v-on:click="singleClazzAddOrCancel(clazzObj)" v-bind:class="{'w-checkbox-current':groupIds.indexOf(clazzObj.groupId) != -1}" class="marginL26" style="width:100px;">
                                <span class="w-checkbox"></span>
                                <span class="w-icon-md" v-bind:title="clazzObj.clazzName" v-text="clazzObj.clazzName"></span>
                            </p>
                        </li>

                    </ul>
                </div>
            </dd>
        </dl>
    </div>
</script>