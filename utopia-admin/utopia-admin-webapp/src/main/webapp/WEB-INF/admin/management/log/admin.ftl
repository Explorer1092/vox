<#include "../index.ftl" />

{% block content %}
<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="span12">
                <div class="well">
                    <legend>日志列表：</legend>
                    <p class="page_container">
                        {% if logs.has_previous %}
                        <a href="?page={{ logs.previous_page_number }}&uid={{ request.GET.uid }}" title="Pre">上一页</a>
                        {% else %}
                        上一页
                        {% endif %}

                        {% if logs.has_next %}
                        <a href="?page={{ logs.next_page_number }}&uid={{ request.GET.uid }}" title="Next">下一页</a>
                        {% else %}
                        下一页
                        {% endif %}

                        <b>
                            {% if logs.number %} 当前第 {{ logs.number }} 页 | {% endif %}
                            {% if logs.paginator.num_pages %}共 {{ logs.paginator.num_pages }} 页{% endif %}
                        </b>
                    </p>

                    <form class="form-horizontal">
                        <input type="text" class="input-small" placeholder="用户ID" name="uid" value="{{ request.GET.p }}">
                        <button type="submit" class="btn">查找</button>
                    </form>

                    <table class="table table-striped table-bordered">
                        <tr>
                            <td></td>
                            <td>用户</td>
                            <td>操作</td>
                            <td>时间</td>
                        </tr>
                        {% for one in logs.object_list %}
                        <tr>
                            <td>{{ forloop.counter }}</td>
                            <td>{{ one.uid }}</td>
                            <td>{{ one.url }}</td>
                            <td>{{ one.created }}</td>
                        </tr>
                        {% endfor %}
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
{% endblock %}
