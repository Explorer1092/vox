<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='商品类目管理' page_num=12>
<link href="${requestContext.webAppContextPath}/public/css/reward/skin.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<div class="categorylist-box" id="categorylist_page" v-cloak>
    <fieldset>
        <legend>商品类目管理</legend>
        <div class="categorylist-menu">
            <div class="category_left">
                <ul class="categorylist-ul">
                    <li>
                        <div @click="category_all_button">
                            <span><i class="icon" v-bind:class="{active: category_one_show}"></i>商品分类根目录</span>
                        </div>
                        <ul class="categorylist-main" v-if="category_one_show && category_list && category_list.length">
                            <li v-for="(item,index) in category_list">
                                <div class="name"
                                     v-bind:class="{active: current_index === index && current_category_active}"
                                     @click.stop="one_level_button(0, item, index)"
                                >
                                    <i class="icon"v-bind:class="{active: current_index === index && category_two_show && item.childrenCategory && item.childrenCategory.length}"></i>{{item.name}}
                                </div>
                                <div v-if="current_index === index && category_two_show">
                                    <ul v-if="item.childrenCategory && item.childrenCategory.length">
                                        <li v-for="(list, index) in item.childrenCategory">
                                            <div class="name"
                                                 v-bind:class="{active: second_index === index && two_level_active}"
                                                 @click.stop="two_level_button(item.id, list, index)"
                                            >
                                                <i class="icon" v-bind:class="{active: second_index === index && category_tag_show && list.childrenTrgList && list.childrenTrgList.length}"></i>{{list.name}}
                                            </div>
                                            <div v-if="second_index === index && category_tag_show">
                                                <ul v-if="list.childrenTrgList && list.childrenTrgList.length">
                                                    <li v-for="(tag, index) in list.childrenTrgList">
                                                        <div class="name"
                                                             v-bind:class="{active: tag_index === index && current_tag_active}"
                                                             @click.stop="tag_button(list.id, tag, index)"
                                                        >
                                                            <i class="icon"></i>{{tag.name}}
                                                        </div>
                                                    </li>
                                                </ul>
                                            </div>
                                        </li>
                                    </ul>
                                </div>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="category_right" v-if="category_button_show">
                <p class="right_tip">如该分类下有商品，则该分类不可删除！</p>
                <ul>
                    <li class="add" @click="edit_category('add')" v-if="add_show">增加{{next_button_type}}</li>
                    <li class="edit" @click="edit_category('edit')" v-if="!root_type">编辑{{current_button_type}}</li>
                    <li class="delete" @click="edit_category('delete')" v-if="!root_type">删除{{current_button_type}}</li>
                </ul>
                <div class="category_right_content" v-if="!root_type">
                    <p>名称：{{edit_item.name}}</p>
                    <p v-if="level === 2 || level === 3">
                        用户可见状态：
                        <span>
                            <template v-if="edit_item.visible.indexOf(1) > -1">
                                小学老师可见&nbsp;&nbsp;
                            </template>
                            <template v-if="edit_item.visible.indexOf(2) > -1">
                                小学学生可见&nbsp;&nbsp;
                            </template>
                            <template v-if="edit_item.visible.indexOf(4) > -1">
                                中学老师可见&nbsp;&nbsp;
                            </template>
                            <template v-if="edit_item.visible.indexOf(8) > -1">
                                中学学生可见&nbsp;&nbsp;
                            </template>
                            <template v-if="edit_item.visible.indexOf(1) === -1 && edit_item.visible.indexOf(2) === -1 && edit_item.visible.indexOf(4) === -1 && edit_item.visible.indexOf(8) === -1">
                            都不可见
                        </template>
                        </span>
                    </p>
                    <p v-if="level === 2">是否显示在首页：{{edit_item.display?'是': '否'}}</p>
                    <p v-if="level === 2">分类类型：{{select_list[select_category_id - 1]}}</p>
                    <p v-if="level === 3 && select_category_id === 7">分类类型：{{toby_list[select_toby_id - 1]}}</p>
                    <p>排序值：{{edit_item.displayOrder?edit_item.displayOrder: 0}}</p>
                </div>
            </div>
        </div>
    </fieldset>

    <div class="wrapper_fixed" v-if="edit_pop_show" @click="close_select">
        <div class="wrapper_pop">
            <template v-if="edit_type === 'delete'">
                <h4>删除{{current_button_type}}</h4>
                <p v-if="level === 4">是否确定删除标签：{{edit_item.name}}？</p>
                <p v-else>确定删除{{current_button_type}}：{{edit_item.name}}？</p>
            </template>
            <template v-else>
                <h4><template v-if="edit_type === 'add'" >新增{{next_button_type}}</template><template v-else>编辑{{current_button_type}}</template></h4>
                <div class="edit_main">
                    <div class="edit_select">
                        <span>名称：</span>
                        <input class="add_name" type="text" v-model="add_category_name" placeholder="请输入名称" />
                    </div>

                    <div class="edit_select" v-if="show_see_flag">
                        <div class="edit_div" @click="pteacher_see('1')"><i v-bind:class="{active: pteacher_see_show}"></i>小学老师可见</div>
                        <div class="edit_div" @click="pstudent_see('2')"><i v-bind:class="{active: pstudent_see_show}"></i>小学学生可见</div>
                        <div class="edit_div" @click="jteacher_see('4')"><i v-bind:class="{active: jteacher_see_show}"></i>中学老师可见</div>
                        <div class="edit_div" @click="jstudent_see('8')"><i v-bind:class="{active: jstudent_see_show}"></i>中学学生可见</div>
                    </div>
                    <div class="edit_select" v-if="show_index_flag">
                        <div class="edit_div" @click="show_index"><i v-bind:class="{active: show_index_page}"></i>显示在首页</div>
                    </div>
                    <#--一级分类会有分类类型选项-->
                    <div class="edit_select edit_select2" v-if="show_index_flag">
                        <span>分类类型：</span>
                        <div class="select_fenlei">
                            <div class="label_name" @click.stop="select_category">{{select_text}}<i v-bind:class="{arrow_up: select_category_show,arrow_down: !select_category_show}"></i></div>
                            <div class="tag_list" v-if="select_category_show">
                                <span v-for="(option,index) in select_list" v-bind:class="{active: edit_type === 'edit' && edit_item && edit_item.oneLevelCategoryType === index + 1}" @click.stop="select_category_type(option, index + 1)">{{option}}</span>
                            </div>
                        </div>
                    </div>
                    <div style="clear:both;"></div>
                    <#--托比装扮增加二级分类或者编辑二级分类会有分类类型选项-->
                    <div class="edit_select edit_select2" v-if="show_toby_flag && select_category_id === 7">
                        <span>分类类型：</span>
                        <div class="select_fenlei">
                            <div class="label_name" @click.stop="select_toby">{{toby_text}}<i v-bind:class="{arrow_up: select_toby_show,arrow_down: !select_toby_show}"></i></div>
                            <div class="tag_list" v-if="select_toby_show">
                                <span v-for="(option,index) in toby_list" v-bind:class="{active:edit_item && edit_item.twoLevelCategoryType === index + 1}" @click.stop="select_toby_type(option, index + 1)">{{option}}</span>
                            </div>
                        </div>
                    </div>
                    <div style="clear:both;"></div>
                    <div class="edit_select">
                        <span>排序值：</span>
                        <input class="add_name" type="number" v-model="sort_number" placeholder="排序值"/>
                    </div>
                </div>
            </template>
            <div class="pop_button">
                <div class="left_button" @click="cancel_button">取消</div>
                <div class="delete_button" @click="delete_button" v-if="edit_type === 'delete'">确定</div>
                <div class="right_button" @click="save_button" v-else>保存</div>
            </div>
        </div>
    </div>

    <div class="wrapper_fixed" v-if="tip_show">
        <div class="wrapper_pop tip_pop">
            <p>{{tip_text}}</p>
            <div class="pop_button">
                <div class="left_button" @click="close_button">知道了</div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    new Vue({
        el:'#categorylist_page',
        data:{
            category_list: [], // 列表
            current_index: -1, // 当前页一级分类索引值
            second_index: -1, // 二级分类索引值
            tag_index: -1, // 标签索引值
            category_one_show: false, // 树形 一级分类是否展示
            category_two_show: false, //树形  二级分类模块是否展示
            category_tag_show: false,  // 树形 标签 是否展示
            category_button_show: false,  // 右侧按钮模块是否展示
            root_type: true, // 是否是根目录选中
            current_category_active: false, // 当前类目选中状态
            two_level_active: false, // 二级选中状态
            current_tag_active: false, // 当前标签选中状态
            edit_item: null,// 当前item
            parent_id: 0, //parentid
            current_button_type: '一级分类', // 当前标题文案
            next_button_type: '二级分类', // 下一个标题文案
            add_category_name: '',  // 新增类别名称
            edit_type: '', // 是编辑 或者是 新增 弹窗
            tip_text: '', // 提示内容
            edit_pop_show: false,  // 编辑弹窗是否展示
            delete_pop_show: false,  // 删除弹窗是否展示
            tip_show: false,  // 提示是否展示
            add_show:false, // 增加标签分类 按钮 是否显示
            level:1,  // 当前选中的level值
            show_index_flag: true,  // 是否显示在首页  模块
            show_see_flag: true,
            pteacher_see_show: true, // 老师是否可见  默认true
            pstudent_see_show: true, // 学生是否可见  默认true
            jteacher_see_show: true,
            jstudent_see_show: true,
            show_index_page: true,   //  是否在首页显示状态  默认true
            sort_number: 0,  // 排序值
            visible_number: ['1','2','4','8'],  // 学生或者老师 可见 值
            select_category_show: false, // 分类类型下拉选项  选项是否展示
            select_list: ['实物','流量包','头饰','微课','优惠券','教学资源','托比装扮'], // 一级分类类型选项
            select_text: '实物', //选中的分类类型text
            select_category_id: 1, // 选中的分类类型 id
            select_toby_show: false,  // 分类类型 下拉选项 托比装扮 是否展示
            show_toby_flag: false,// 是否显示托比装扮
            toby_list: ['形象','表情','道具','装饰'], //托比装扮选项
            toby_text: '形象', //托比装扮text
            select_toby_id: 0, //选中的托比装扮id
        },
        methods:{
            close_select: function () {
              var vm = this;
              vm.select_category_show = false;
            },
            select_category: function () {
                var vm = this;
                vm.select_category_show = !vm.select_category_show;
            },
            select_category_type: function (text, id) {
                var vm = this;
                vm.select_text = text;
                vm.select_category_id = id;
                vm.select_category_show = false;
            },
            select_toby: function () {
                var vm = this;
                vm.select_toby_show = !vm.select_toby_show;
            },
            select_toby_type: function (text, id) {
                var vm = this;
                vm.toby_text = text;
                vm.select_toby_id = id;
                vm.select_toby_show = false;
            },
            role_flag: function (see, role_index) {
                var vm = this;
                if (see) {
                    if (vm.visible_number.indexOf(role_index) === -1) {
                        vm.visible_number.push(role_index);
                    }
                } else {
                    var _index = vm.visible_number.indexOf(role_index);
                    vm.visible_number.splice(_index,1);
                }
            },
            pteacher_see: function (role_index) {
                var vm = this;

                vm.pteacher_see_show = !vm.pteacher_see_show;
                vm.role_flag(vm.pteacher_see_show, role_index);
            },
            pstudent_see: function (role_index) {
                var vm = this;

                vm.pstudent_see_show = !vm.pstudent_see_show;
                vm.role_flag(vm.pstudent_see_show, role_index);
            },
            jteacher_see: function (role_index) {
                var vm = this;

                vm.jteacher_see_show = !vm.jteacher_see_show;
                vm.role_flag(vm.jteacher_see_show, role_index);
            },
            jstudent_see: function (role_index) {
                var vm = this;

                vm.jstudent_see_show = !vm.jstudent_see_show;
                vm.role_flag(vm.jstudent_see_show, role_index);
            },
            show_index: function () {
                var vm = this;
                vm.show_index_page = !vm.show_index_page;
            },
            category_all_button: function() {
                var vm = this;

                vm.category_button_show = true;
                vm.current_index = -1;
                vm.second_index = -1;
                vm.tag_index = -1;
                vm.level = 1;
                vm.parent_id = 0;
                vm.edit_item = null;
                vm.next_button_type = '一级分类';
                vm.category_one_show = !vm.category_one_show;
                vm.category_two_show = false;
                vm.category_tag_show = false;
                vm.root_type = true;
                vm.add_show = true;
                vm.show_index_flag = true;
                vm.select_category_id = 1;
                vm.select_text = vm.select_list[0];
                vm.toby_text = vm.toby_list[0];
            },
            one_level_button: function (parent_id, item, index) {
                var vm = this;

                if (vm.current_index !== index) {
                    vm.category_two_show = true;
                } else {
                    vm.category_two_show = !vm.category_two_show;
                }
                vm.edit_item = item;
                vm.parent_id = parent_id;
                vm.current_index = index;
                vm.current_button_type = '一级分类';
                vm.next_button_type = '二级分类';
                vm.level = 2;
                vm.category_button_show = true;
                vm.category_tag_show = false;
                vm.current_category_active = true;
                vm.two_level_active = false;
                vm.current_tag_active = false;
                vm.select_category_id = vm.edit_item.oneLevelCategoryType;
                vm.select_text = vm.select_list[vm.edit_item.oneLevelCategoryType];
                vm.root_type = false;
                vm.add_show = true;
            },
            two_level_button: function (parent_id, item, index) {
                var vm = this;

                if (vm.second_index !== index) {
                    vm.category_tag_show = true;
                } else {
                    vm.category_tag_show = !vm.category_tag_show;
                }
                vm.edit_item = item;
                vm.parent_id = parent_id;
                vm.second_index = index;
                vm.current_button_type = '二级分类';
                vm.next_button_type = '标签';
                vm.category_button_show = true;
                vm.current_category_active = false;
                vm.current_tag_active = false;
                vm.two_level_active = true;
                vm.root_type = false;
                vm.level = 3;
                vm.add_show = true;
                vm.select_toby_id = vm.edit_item.twoLevelCategoryType;
                vm.toby_text = vm.toby_list[vm.edit_item.twoLevelCategoryType];
            },
            tag_button: function (parent_id, item, index) {
                var vm = this;

                vm.edit_item = item;
                vm.parent_id = parent_id;
                vm.tag_index = index;
                vm.current_button_type = '标签';
                vm.category_button_show = true;
                vm.current_category_active = false;
                vm.current_tag_active = true;
                vm.two_level_active = false;
                vm.root_type = false;
                vm.add_show = false;
                vm.level = 4;
            },
            edit_category:function (type) {
                var vm = this;

                vm.edit_type = type;
                vm.edit_pop_show = true;

                if (vm.edit_type === 'edit') {

                    if (vm.level === 2 && vm.edit_item && vm.edit_item.oneLevelCategoryType > 0) {
                        vm.select_text = vm.select_list[vm.edit_item.oneLevelCategoryType - 1];
                        vm.select_category_id = vm.edit_item.oneLevelCategoryType;
                    }

                    if (vm.level === 3 && vm.edit_item && vm.edit_item.twoLevelCategoryType > 0) {
                        vm.toby_text = vm.toby_list[vm.edit_item.twoLevelCategoryType - 1];
                        vm.select_toby_id = vm.edit_item.twoLevelCategoryType;
                    }

                    vm.add_category_name = vm.edit_item.name;

                    vm.pteacher_see_show = false;
                    vm.pstudent_see_show = false;
                    vm.jteacher_see_show = false;
                    vm.jstudent_see_show = false;

                    if (vm.edit_item.visible) {
                        if (vm.edit_item.visible.indexOf('1') > -1) {
                            vm.pteacher_see_show = true;
                        }
                        if (vm.edit_item.visible.indexOf('2') > -1) {
                            vm.pstudent_see_show = true;
                        }
                        if (vm.edit_item.visible.indexOf('4') > -1) {
                            vm.jteacher_see_show = true;
                        }
                        if (vm.edit_item.visible.indexOf('8') > -1) {
                            vm.jstudent_see_show = true;
                        }
                        vm.visible_number = vm.edit_item.visible.split(',');
                    }

                    vm.show_index_page = vm.edit_item.display;
                    vm.sort_number = vm.edit_item.displayOrder;

                    if (vm.level === 2) {
                        vm.show_index_flag = true;
                        vm.show_toby_flag = false;
                        vm.show_see_flag = true;
                    } else if (vm.level === 3){
                        vm.show_index_flag = false;
                        vm.show_toby_flag = true;
                        vm.show_see_flag = true;
                    } else {
                        vm.show_index_flag = false;
                        vm.show_toby_flag = false;
                        vm.show_see_flag = false;
                    }

                } else if(vm.edit_type === 'add') {
                    vm.add_category_name = '';
                    if (vm.level === 1) {
                        vm.show_see_flag = true;
                        vm.show_index_flag = true;
                        vm.show_toby_flag = false;
                    } else if (vm.level === 2) {
                        vm.show_see_flag = true;
                        vm.show_toby_flag = true;
                        vm.show_index_flag = false;
                    } else {
                        vm.show_see_flag = false;
                        vm.show_toby_flag = false;
                        vm.show_index_flag = false;
                    }
                }
            },
            close_button: function () {
               this.tip_show = false;
            },
            cancel_button: function () {
                var vm = this;

                vm.edit_pop_show = false;
                vm.add_category_name = '';
            },
            get_query: function(item){
                var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
                return svalue ? decodeURIComponent(svalue[1]) : '';
            },
            delete_button: function () {
                var vm = this, request_url = '';

                if (vm.category_tag_show) {
                    vm.tip_text = '该分类下有标签，不可以删除！';
                } else {
                    vm.tip_text = '该分类下有子分类，不可以删除！';
                }
                if ( vm.edit_item.child && vm.edit_item.child.length) {
                    vm.edit_pop_show = false;
                    vm.tip_show = true;
                    return;
                }

                if (vm.level === 4){
                    request_url = '/reward/newtag/delete.vpage';
                } else {
                    request_url = '/reward/newcategory/delete.vpage';
                }

                $.ajax({
                    url: request_url,
                    type: 'POST',
                    data:{
                        id : vm.edit_item.id
                    },
                    success:function (res) {
                        vm.edit_pop_show = false;
                        vm.add_category_name = '';
                        if (res.success) {
                            vm.set_init();
                        } else {
                            vm.tip_show = true;
                            vm.tip_text = res.info;
                        }
                    }
                });
            },
            set_init: function () {
                var vm = this;
                vm.category_one_show = true;
                vm.category_button_show = false;
                vm.visible_number = ['1','2','4','8'];
                vm.pteacher_see_show = true;
                vm.pstudent_see_show = true;
                vm.jteacher_see_show = true;
                vm.jstudent_see_show = true;
                vm.show_index_page = true;
                vm.root_type = true;
                vm.level = 1;
                vm.parent_id = 0;
                vm.next_button_type = '一级分类';
                vm.list_init();
            },
            save_button: function () {
                var vm = this,request_url = '';

                if ($.trim(vm.add_category_name) === '') {
                    vm.tip_text = '请输入名称';
                    vm.tip_show = true;
                    return;
                }

                var editData = {};
                if(vm.edit_type === 'edit'){
                    editData = {
                        id: vm.edit_item.id,
                        parentId : vm.parent_id,
                        name: vm.add_category_name,
                        visible: vm.visible_number.sort().join(','),
                        display: vm.show_index_page,
                        displayOrder: vm.sort_number,
                        oneLevelCategoryType: vm.select_category_id,
                        twoLevelCategoryType: vm.select_category_id === 7 && vm.level === 3 ? vm.select_toby_id : 0
                    };
                } else {
                    var par_id = 0;
                    if (vm.edit_item && vm.edit_item.id) {
                        par_id = vm.edit_item.id;
                    }
                    editData = {
                        parentId: par_id,
                        name: vm.add_category_name,
                        visible: vm.visible_number.sort().join(','),
                        display: vm.show_index_page,
                        displayOrder: vm.sort_number,
                        oneLevelCategoryType: vm.select_category_id,
                        twoLevelCategoryType: vm.select_category_id === 7 && vm.level === 3 ? vm.select_toby_id : 0
                    };
                }

                if (vm.edit_type === 'edit'){
                    if (vm.level === 4){
                        editData.parentType = 1;
                        request_url = '/reward/newtag/upsert.vpage';
                    } else {
                        editData.level = (vm.level-1);
                        request_url = '/reward/newcategory/upsert.vpage';
                    }
                } else {
                    if (vm.level === 3){
                        editData.parentType = 1;
                        request_url = '/reward/newtag/upsert.vpage';
                    } else {
                        editData.level = vm.level;
                        request_url = '/reward/newcategory/upsert.vpage';
                    }
                }

                $.ajax({
                    url: request_url,
                    data: editData,
                    type: 'POST',
                    success: function (res) {
                        vm.edit_pop_show = false;
                        vm.add_category_name = '';
                        if (res.success) {
                            vm.set_init();
                        } else{
                            vm.tip_show = true;
                            vm.tip_text = res.info;
                        }

                        vm.sort_number = 0;
                    }
                });
            },
            list_init:function () {
                var vm = this;

                $.ajax({
                    url: "/reward/newcategory/tree.vpage",
                    data:{
                        parentId: 0
                    },
                    success: function (res) {
                        if (res.success) {
                            vm.category_list = res.categoryTree || [];
                        }
                    }
                });
            }
        },
        created:function(){
            var vm = this;

            vm.list_init();
            vm.category_all_button();

        }
    });
</script>
</@layout_default.page>