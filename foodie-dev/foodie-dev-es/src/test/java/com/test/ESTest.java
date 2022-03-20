package com.test;

import com.imooc.Application;
import com.imooc.es.pojo.Stu;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ESTest {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 不建议使用 ElasticsearchTemplate 对索引进行管理（创建索引，更新映射，删除索引）
     * 索引就像是数据库或者数据库中的表，我们平时是不会是通过java代码频繁的去创建修改删除数据库或者表的
     * 我们只会针对数据做CRUD的操作
     * 在es中也是同理，我们尽量使用 ElasticsearchTemplate 对文档数据做CRUD的操作
     * 1. 属性（FieldType）类型不灵活
     * 2. 主分片与副本分片数无法设置
     */

    @Test
    public void createIndexStu() {
        elasticsearchRestTemplate.indexOps(Stu.class).create();

        Document mapping = elasticsearchRestTemplate.indexOps(Stu.class).createMapping();
        elasticsearchRestTemplate.indexOps(Stu.class).putMapping(mapping);

//        Stu stu = new Stu();
//        stu.setStuId(1005L);
//        stu.setName("iron man");
//        stu.setAge(54);
//        elasticsearchRestTemplate.save(stu);

    }

    @Test
    public void updateMapping() {

        Document mapping = elasticsearchRestTemplate.indexOps(Stu.class).createMapping();
        elasticsearchRestTemplate.indexOps(Stu.class).putMapping(mapping);
        Stu stu = new Stu();
        stu.setStuId(1006L);
        stu.setName("spider man");
        stu.setAge(55);
        stu.setMoney(122.8f);
        stu.setSign("I am spider man");
        stu.setDescription("I have a spider army");
        elasticsearchRestTemplate.save(stu);
    }

    @Test
    public void deleteIndexStu() {
        elasticsearchRestTemplate.indexOps(Stu.class).delete();
    }

//    ------------------------- 我是分割线 --------------------------------

    @Test
    public void updateStuDoc() {

        Map<String, Object> sourceMap = new HashMap<>();
//        sourceMap.put("sign", "I am not super man");
        sourceMap.put("money", 99.8f);
//        sourceMap.put("age", 33);


        UpdateQuery updateQuery = UpdateQuery.builder("1006")
                .withDocument(Document.from(sourceMap)).build();

        elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of("stu"));
    }


    @Test
    public void getStuDoc() {

        Stu stu = elasticsearchRestTemplate.get("1006", Stu.class);
        System.out.println(stu);
    }

    @Test
    public void deleteStuDoc() {
        Stu stu = new Stu();
        stu.setStuId(1007L);
        stu.setName("spider man");
        stu.setAge(55);
        stu.setMoney(122.8f);
        stu.setSign("I am spider man");
        stu.setDescription("I have a spider army");
        elasticsearchRestTemplate.save(stu);

//        stu.setStuId(1008L);
//        stu.setName("iron man");
//        elasticsearchRestTemplate.save(stu);
//
//        stu.setStuId(1009L);
//        stu.setName("xiaosi man");
//        elasticsearchRestTemplate.save(stu);
//
//        stu.setStuId(1010L);
//        stu.setDescription("慕课网");
//        elasticsearchRestTemplate.save(stu);
//
//        stu.setStuId(1011L);
//        stu.setSign("笑死");
//        elasticsearchRestTemplate.save(stu);

        String delete = elasticsearchRestTemplate.delete("1007", Stu.class);
        System.out.println(delete);
    }


//    ------------------------- 我是分割线 --------------------------------

    @Test
    public void searchStuDoc() {

        Pageable pageable = PageRequest.of(0, 2);

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("description", "慕课"))
                .withPageable(pageable)
                .build();
        SearchHits<Stu> result = elasticsearchRestTemplate.search(query, Stu.class);
        System.out.println("检索后的总分页数目为：" + result.getTotalHits());
        List<Stu> stuList = result.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
        for (Stu s : stuList) {
            System.out.println(s);
        }

    }

    @Test
    public void highlightStuDoc() {

        String preTag = "<font color='red'>";
        String postTag = "</font>";

        Pageable pageable = PageRequest.of(0, 10);

        SortBuilder sortBuilder = new FieldSortBuilder("money")
                .order(SortOrder.DESC);
        SortBuilder sortBuilderAge = new FieldSortBuilder("age")
                .order(SortOrder.ASC);

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("description", "spider"))
                .withHighlightFields(new HighlightBuilder.Field("description")
                                    .preTags(preTag)
                                    .postTags(postTag))
                .withSort(sortBuilder)
                .withSort(sortBuilderAge)
                .withPageable(pageable)
                .build();
        SearchHits<Stu> result = elasticsearchRestTemplate.search(query, Stu.class);
        System.out.println("检索后的总分页数目为：" + result.getTotalHits());
        List<Stu> stuList = result.getSearchHits().stream().map(item->{
            Stu content = item.getContent();
            Map<String, List<String>> highlightFields = item.getHighlightFields();
            content.setDescription(highlightFields.get("description").get(0));
            return content;
        }).collect(Collectors.toList());
        for (Stu s : stuList) {
            System.out.println(s);
        }

    }
}
