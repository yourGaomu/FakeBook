package com.zhangzc.booknotebiz.Pojo.Milvus;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import lombok.Data;
import org.dromara.milvus.plus.annotation.MilvusCollection;
import org.dromara.milvus.plus.annotation.MilvusField;

import java.util.Arrays;
import java.util.List;

@Data
@MilvusCollection(name = "FakeBookNotes")
public class FakeBookNotes {
    public static final String COLLECTION_NAME = "FakeBookNotes";
    @MilvusField(
            name = "id", // 字段名称
            dataType = DataType.String, // 数据类型为64位整数
            isPrimaryKey = true // 标记为主键
    )
    private String id;
    @MilvusField(
            name = "title_vct",
            dataType = DataType.FloatVector,
            dimension = 1536
    )
    private List<Float> title_vct;
    @MilvusField(
            name = "content_vct",
            dataType = DataType.FloatVector,
            dimension = 1536
    )
    private List<Float> content_vct;
    @MilvusField(
            name = "userId",
            dataType = DataType.String
    )
    private String userId;
    @MilvusField(
            name = "title",
            dataType = DataType.String
    )
    private String title;
    @MilvusField(
            name = "visible",
            dataType = DataType.Int16
    )
    private Integer visible;
    @MilvusField(
            name = "type",
            dataType = DataType.Int16
    )
    private Integer type;
    @MilvusField(
            name = "content",
            dataType = DataType.String
    )
    private String content;
    @MilvusField(name = "imageUrl",
            dataType = DataType.Array
    )
    private List<String> imageUrl;

    public static void initCollection(MilvusClientV2 client) {
        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                .build();
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .isPrimaryKey(true)
                .autoID(false)
                .description("笔记Id")
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("title_vct")
                .dataType(DataType.FloatVector)
                .dimension(1536)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("content_vct")
                .dataType(DataType.FloatVector)
                .dimension(1536)
                .description("分词器")
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("userId")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .description("用户Id")
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("title")
                .dataType(DataType.VarChar)
                .maxLength(100)
                .description("标题")
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("visible")
                .dataType(DataType.Int16)
                .description("是否可见")
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("type")
                .dataType(DataType.Int16)
                .description("文章类型")
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("content")
                .dataType(DataType.VarChar)
                .maxLength(4096)
                .description("文章内容")
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("imageUrl")
                .dataType(DataType.Array)
                .elementType(DataType.VarChar)
                .maxCapacity(10)
                .maxLength(400)
                .description("图片url")
                .build());

        IndexParam indexParamTitle = IndexParam.builder()
                .fieldName("title_vct")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                .build();

        IndexParam indexParamContent = IndexParam.builder()
                .fieldName("content_vct")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                .build();

        IndexParam indexParamId = IndexParam.builder()
                .fieldName("id")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build();

        IndexParam indexParamUserId = IndexParam.builder()
                .fieldName("userId")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build();

        IndexParam indexParamTitleScalar = IndexParam.builder()
                .fieldName("title")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build();

        IndexParam indexParamVisible = IndexParam.builder()
                .fieldName("visible")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build();

        IndexParam indexParamType = IndexParam.builder()
                .fieldName("type")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build();

        IndexParam indexParamContentScalar = IndexParam.builder()
                .fieldName("content")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build();

        IndexParam indexParamImageUrl = IndexParam.builder()
                .fieldName("imageUrl")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build();

        CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                .collectionName(COLLECTION_NAME)
                .collectionSchema(schema)
                .indexParams(Arrays.asList(
                        indexParamTitle,
                        indexParamContent,
                        indexParamId,
                        indexParamUserId,
                        indexParamTitleScalar,
                        indexParamVisible,
                        indexParamType,
                        indexParamContentScalar,
                        indexParamImageUrl
                ))
                .build();

        client.createCollection(createCollectionReq);
    }
}
