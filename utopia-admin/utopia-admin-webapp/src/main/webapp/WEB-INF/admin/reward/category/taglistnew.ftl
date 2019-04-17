<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='分类集合管理' page_num=12>
<link href="${requestContext.webAppContextPath}/public/css/reward/skin.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<div class="categorylist-box" id="taglist_page" v-cloak>
    <fieldset>
        <legend>分类集合管理</legend>
        <div class="categorylist-menu">
            <div class="category_left">
                <ul class="categorylist-ul">
                    <li>
                        <div @click="category_all_button">
                            <span><i class="icon" v-bind:class="{active: category_one_show}"></i>专区根目录</span>
                        </div>
                        <ul class="categorylist-main" v-if="category_one_show && tag_list && tag_list.length">
                            <li v-for="(item,index) in tag_list">
                                <div class="name"
                                     v-bind:class="{active: current_index === index && current_category_active}"
                                     @click.stop="one_level_button(0, item, index)"
                                >
                                    <i class="icon"v-bind:class="{active: current_index === index && category_two_show && item.childrenTrgList && item.childrenTrgList.length}"></i>{{item.name}}
                                </div>
                                <div v-if="current_index === index && category_two_show">
                                    <ul v-if="item.childrenTrgList && item.childrenTrgList.length">
                                        <li v-for="(list, index) in item.childrenTrgList">
                                            <div class="name"
                                                 v-bind:class="{active: second_index === index && two_level_active}"
                                                 @click.stop="two_level_button(item.id, list, index)"
                                            >
                                                <i class="icon"></i>{{list.name}}
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
                    <p v-if="level === 2">
                        用户可见状态：
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
                    </p>
                    <p v-if="level === 2">是否显示在首页：{{edit_item.display?'是': '否'}}</p>
                    <p>排序值：{{edit_item.displayOrder?edit_item.displayOrder: 0}}</p>
                </div>
            </div>
        </div>
    </fieldset>

    <div class="wrapper_fixed" v-if="edit_pop_show">
    <div class="wrapper_pop">
            <template v-if="edit_type === 'delete'">
                <h4>删除{{current_button_type}}</h4>
                <p v-if="level === 3">是否确定删除标签：{{edit_item.name}}？</p>
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
        el:'#taglist_page',
        data:{
            tag_list: [],// 列表
            current_index: -1,// 当前页一级分类索引值
            second_index: -1,// 二级分类索引值
            category_one_show: false,// 树形 一级分类是否展示
            category_two_show: false,//树形  二级分类模块是否展示
            category_button_show: false,// 右侧按钮模块是否展示
            root_type: true,// 是否是根目录选中
            current_category_active: false,// 当前类目选中状态
            two_level_active: false,// 二级选中状态
            edit_item: null,// 当前item
            parent_id: 0,//parentid
            current_button_type: '分类',// 当前标题文案
            next_button_type: '标签',// 下一个标题文案
            add_category_name: '', // 新增类别名称
            edit_type: '',// 是编辑 或者是 新增 弹窗
            tip_text: '',// 提示内容
            edit_pop_show: false,// 编辑弹窗是否展示
            delete_pop_show: false, // 删除弹窗是否展示
            tip_show: false,// 提示是否展示
            add_show:false,// 增加标签分类 按钮 是否显示
            level:1, // 当前选中的level值
            show_index_flag: true,  // 是否显示在首页  模块
            show_see_flag: true,
            pteacher_see_show: true, // 老师是否可见  默认true
            pstudent_see_show: true, // 学生是否可见  默认true
            jteacher_see_show: true,
            jstudent_see_show: true,
            show_index_page: true,   //  是否在首页显示状态  默认true
            sort_number: 0,  // 排序值
            visible_number: ['1','2','4','8'],  // 学生或者老师 可见 值
        },
        methods:{
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
                vm.next_button_type = '分类';
                vm.category_one_show = !vm.category_one_show;
                vm.category_two_show = false;
                vm.root_type = true;
                vm.add_show = true;
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
                vm.current_button_type = '分类';
                vm.next_button_type = '标签';
                vm.level = 2;
                vm.category_button_show = true;
                vm.current_category_active = true;
                vm.two_level_active = false;
                vm.current_tag_active = false;
                vm.root_type = false;
                vm.add_show = true;
                vm.show_index_flag = true;
            },
            two_level_button: function (parent_id, item, index) {
                var vm = this;

                vm.edit_item = item;
                vm.parent_id = parent_id;
                vm.second_index = index;
                vm.current_button_type = '标签';
                vm.category_button_show = true;
                vm.current_category_active = false;
                vm.two_level_active = true;
                vm.root_type = false;
                vm.level = 3;
                vm.add_show = false;
            },
            edit_category:function (type) {
                var vm = this;

                vm.edit_type = type;
                vm.edit_pop_show = true;
                if (vm.edit_type === 'edit') {
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
                        vm.show_see_flag = true;
                    } else {
                        vm.show_index_flag = false;
                        vm.show_see_flag = false;
                    }

                } else if(vm.edit_type === 'add') {
                    vm.add_category_name = '';
                    if (vm.level === 1) {
                        vm.show_see_flag = true;
                        vm.show_index_flag = true;
                    } else {
                        vm.show_see_flag = false;
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
            delete_button: function () {
                var vm = this, request_url = '';

                if ( vm.edit_item.child && vm.edit_item.child.length) {
                    vm.tip_text = '该分类下有标签，不可以删除！';
                    vm.edit_pop_show = false;
                    vm.tip_show = true;
                    return;
                }

                if (vm.level === 3){
                    request_url = '/reward/newtag/delete.vpage';
                } else {
                    request_url = '/reward/newset/delete.vpage';
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
                vm.root_type = true;
                vm.level = 1;
                vm.parent_id = 0;
                vm.next_button_type = '分类';
                vm.visible_number = ['1','2','4','8'];
                vm.pteacher_see_show = true;
                vm.pstudent_see_show = true;
                vm.jteacher_see_show = true;
                vm.jstudent_see_show = true;
                vm.show_index_page = true;
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

                if (vm.edit_type === 'edit'){
                    if (vm.level === 3){
                        editData = {
                            id: vm.edit_item.id,
                            parentId : vm.parent_id,
                            name: vm.add_category_name,
                            parentType: 2,
                            visible: vm.visible_number.sort().join(','),
                            display: vm.show_index_page,
                            displayOrder: vm.sort_number
                        };
                        request_url = '/reward/newtag/upsert.vpage';
                    } else {
                        editData = {
                            id: vm.edit_item.id,
                            name: vm.add_category_name,
                            visible: vm.visible_number.sort().join(','),
                            display: vm.show_index_page,
                            displayOrder: vm.sort_number
                        };
                        request_url = '/reward/newset/upsert.vpage';
                    }
                } else {
                    if (vm.level === 2){
                        var par_id = 0;
                        if (vm.edit_item && vm.edit_item.id) {
                            par_id = vm.edit_item.id;
                        }
                        editData = {
                            parentId: par_id,
                            name: vm.add_category_name,
                            parentType: 2,
                            visible: vm.visible_number.sort().join(','),
                            display: vm.show_index_page,
                            displayOrder: vm.sort_number
                        };
                        request_url = '/reward/newtag/upsert.vpage';
                    } else {
                        editData = {
                            name: vm.add_category_name,
                            visible: vm.visible_number.sort().join(','),
                            display: vm.show_index_page,
                            displayOrder: vm.sort_number
                        };
                        request_url = '/reward/newset/upsert.vpage';
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
                    url: "/reward/newset/tree.vpage",
                    data:{
                        parentId: 0
                    },
                    success: function (res) {
                        if (res.success) {
                            vm.tag_list = res.setTree || [];
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