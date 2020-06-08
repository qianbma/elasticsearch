package com.mqb;


import com.alibaba.fastjson.JSON;
import com.mqb.bean.User;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class EsApiApplicationTests {

    @Resource
    private RestHighLevelClient client;

    // 创建索引
    @Test
    void testCreateIndex() throws Exception{
        CreateIndexRequest request = new CreateIndexRequest("kuang_index1");
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    // 获取索引
    @Test
    void testGetIndex() throws Exception{
        GetIndexRequest request = new GetIndexRequest("kuang_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 删除索引
    @Test
    void testDeleteIndex() throws Exception{
        DeleteIndexRequest request = new DeleteIndexRequest("kuang_index");
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    // 添加文档
    @Test
    void testAddDocument()throws Exception{
        User user = new User("狂神",23);
        IndexRequest request = new IndexRequest("kuang_index");
        request.id("1");
        request.source(JSON.toJSONString(user), XContentType.JSON);
        IndexResponse reponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println(reponse.toString());
        System.out.println(reponse.status());
    }

    // 判断文档是否存在
    @Test
    void testIsExistDocument()throws Exception{
        GetRequest request = new GetRequest("kuang_index","1");
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);

        System.out.println(exists);
    }

    // 获取文档信息
    @Test
    void testGetDocument()throws Exception{
        GetRequest request = new GetRequest("kuang_index","1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        System.out.println(response.getSourceAsString());
        System.out.println(response);
    }

    // 更新文档信息:覆盖更新
    @Test
    void testUpdateDocument()throws Exception{
        UpdateRequest request = new UpdateRequest("kuang_index", "1");
        User user = new User("狂神", 18);
        request.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

        System.out.println(response.status());
    }

    // 删除文档
    @Test
    void testDeleteDocument()throws Exception{
        DeleteRequest request = new DeleteRequest("kuang_index", "1");

        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);

        System.out.println(response.status());
    }

    // 批量操作：插入、更新、删除。注意没有批量查询操作
    @Test
    void testBulkOperate()throws Exception{
        BulkRequest bulkRequest = new BulkRequest();
        List<User> list = new ArrayList<>();
        list.add(new User("狂神1",10));
        list.add(new User("狂神2",11));
        list.add(new User("狂神3",12));
        list.add(new User("狂神4",13));
        list.add(new User("狂神5",14));
        list.add(new User("狂神6",15));
        list.add(new User("狂神7",16));
        for (int i = 0; i < list.size()-4; i++) {
            // 批量插入
            bulkRequest.add(
                    new IndexRequest("kuang_index")
                    .id(""+(i+1))
                    .source(JSON.toJSONString(list.get(i)),XContentType.JSON)
            );

            // 批量更新
            User user = list.get(i);
            user.setAge(user.getAge()+10);
            bulkRequest.add(
                    new UpdateRequest("kuang_index",""+(i+1))
                            .doc(JSON.toJSONString(list.get(i)),XContentType.JSON)
            );

            // 批量删除
            bulkRequest.add(
                    new DeleteRequest("kuang_index",""+(i+1))
            );
        }
        BulkResponse reponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        System.out.println(reponse.hasFailures());
    }

    // 构造查询
    @Test
    void testSearch()throws Exception{
        SearchRequest request = new SearchRequest("kuang_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "狂");
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "狂神");
        searchSourceBuilder.query(termQueryBuilder)
                // 分页
                .from(0)
                .size(2)
                // 排序
                .sort("age",SortOrder.DESC);
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        for(SearchHit hit:response.getHits().getHits()){
            System.out.println(hit.getSourceAsString());
        }
    }

}
