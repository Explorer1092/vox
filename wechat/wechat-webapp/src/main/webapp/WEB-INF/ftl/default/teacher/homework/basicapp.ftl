<div class="exercise-box" data-bind="visible : $data.selectedHomeworkType() == 'BASIC_APP' " style="display: none;">
    <@sugar.capsule css=['swiper3'] />

    <!-- ko foreach : {data : $root.basicAppDetail, as : '_basic'} -->
    <div class="ex-column">
        <div class="ex-header">
            <div class="ex-fLeft"><span data-bind="text: _basic.lessonName">--</span></div>
            <div class="ex-fRight"><span class="ex-view" data-bind="click : $root.viewImgBtn">预览</span></div>
        </div>
        <p class="ex-info" data-bind="text: $root.basicAppCovertSentences(_basic.sentences())">--</p>
        <ul class="ex-side">
            <!-- ko foreach : {data : _basic.categories, as : '_category'} -->
            <li data-bind="css: {'active': _category.checked}, click : _category.checked() ? $root.removeCategory.bind($data,$parent) : $root.addCategory.bind($data,$parent)">
                <div class="ex-content">
                    <img src="" data-bind="attr : {'src': '/public/images/teacher/homework/english/basicappicon/e-icons-'+_category.categoryIcon()+'.png'}" alt="">
                    <!--ko if: _category.teacherAssignTimes() > 0-->
                    <i class="ex-tag">已出<span data-bind="text: _category.teacherAssignTimes">--</span>次</i>
                    <!--/ko-->
                </div>
                <div class="ex-titleBar" data-bind="text: _category.categoryName">--</div>
            </li>

            <!--/ko-->
        </ul>
    </div>
    <!--/ko-->

    <div data-bind="visible: $root.basicAppDetail().length == 0" style="text-align: center; display: none;">温馨提示：暂无应用，请选择其他题目类型</div>
</div>