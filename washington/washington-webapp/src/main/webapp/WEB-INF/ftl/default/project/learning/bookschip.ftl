<#if books?? && books?has_content>
    <#list books as book>
    <dl data-book_id="${book.id!}" style="cursor: pointer;">
        <dt>
                <span class="w-build-image w-build-image-${book.color!''}">
                    <strong class="wb-title">${book.viewContent!''}</strong>
                <#--奇葩的new图标 英语 语文 用latestVersion， 数学 用versions -->
                    <#if (book.latestVersion)!false || (book.versions == '1')!false>
                        <span class="wb-new"></span>
                    </#if>
                </span>
        </dt>
        <dd>
            <p>${book.cname!}</p>
        </dd>
    </dl>
    </#list>

<script type="text/javascript">
    $(function(){
        $("#l_books_list_box dl").on('click', function(){
            var bookId = $(this).data('book_id');
            var subjectType = '${(subjectType)!'ENGLISH'}';
            setTimeout(function(){location.href = '?bookId='+bookId+"&subject="+subjectType},200);
        });
    });
</script>
</#if>