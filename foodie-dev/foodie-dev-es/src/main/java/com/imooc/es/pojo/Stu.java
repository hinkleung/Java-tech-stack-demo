package com.imooc.es.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "stu", shards = 2, replicas = 0)
public class Stu {

    @Id
    @Field(store = true, type = FieldType.Long)
    private Long stuId;

    @Field(store = true, type = FieldType.Text)
    private String name;

    @Field(store = true, type = FieldType.Integer)
    private Integer age;

    @Field(store = true, type = FieldType.Float)
    private Float money;

    @Field(store = true, type = FieldType.Keyword)
    private String sign;

    @Field(store = true, type = FieldType.Text)
    private String description;

}
