package com.mqb.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

/**
 * 查询
 */
public class SearchIndex {

    private TransportClient client;

    @Before
    public void init() throws Exception{
        // 创建一个settings对象
        Settings settings = Settings.builder()
                .put("cluster.name","my-elasticsearch")
                .build();
        // 创建一个transportClient对象
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
    }

    public void search(QueryBuilder queryBuilder)throws Exception{
        SearchResponse searchResponse = client.prepareSearch().setIndices("index_hello")
                .setTypes("article")
                .setQuery(queryBuilder)
                .get();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询结果总记录数"+hits.getTotalHits());
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()){
            SearchHit searchHit = iterator.next();
            // 打印文档对象，以json格式输出
            System.out.println(searchHit.getSourceAsString());
            // 取文档的属性
            Map<String, Object> document = searchHit.getSource();
            System.out.println(document.get("id"));
            System.out.println(document.get("title"));
            System.out.println(document.get("content"));
        }

        client.close();
    }

    /**
     * 高亮显示的域
     * @param queryBuilder
     * @throws Exception
     */
    public void search(QueryBuilder queryBuilder,String highLightField)throws Exception{
        HighlightBuilder builder = new HighlightBuilder();
        // 设置高亮显示域
        builder.field(highLightField);
        builder.preTags("<em>");
        builder.postTags("</em>");
        SearchResponse searchResponse = client.prepareSearch().setIndices("index_hello")
                .setTypes("article")
                .setQuery(queryBuilder)
                .setFrom(0)
                .setSize(4)
                .highlighter(builder)
                .get();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询结果总记录数"+hits.getTotalHits());
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()){
            SearchHit searchHit = iterator.next();
            // 取高亮结果
            System.out.println("**********高亮结果***********");
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField field = highlightFields.get(highLightField);
            Text[] fragments = field.getFragments();
            if(fragments != null){
                String content = fragments[0].toString();
                System.out.println(content);
            }
        }
        client.close();
    }

    /**
     * 设置分页
     * @param queryBuilder
     * @throws Exception
     */
    public void searchByPaper(QueryBuilder queryBuilder)throws Exception{
        SearchResponse searchResponse = client.prepareSearch().setIndices("index_hello")
                .setTypes("article")
                .setQuery(queryBuilder)
                .setFrom(0)
                .setSize(4)
                .get();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询结果总记录数"+hits.getTotalHits());
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()){
            SearchHit searchHit = iterator.next();
            // 打印文档对象，以json格式输出
            System.out.println(searchHit.getSourceAsString());
            // 取文档的属性
            Map<String, Object> document = searchHit.getSource();
            System.out.println(document.get("id"));
            System.out.println(document.get("title"));
            System.out.println(document.get("content"));
        }

        client.close();
    }

    @Test
    public void searchById()throws Exception{
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds("1","2");
        search(queryBuilder);
    }

    @Test
    public void searchByTerm()throws Exception{
        QueryBuilder queryBuilder = QueryBuilders.termQuery("title","伊拉克");
        search(queryBuilder);
    }

    @Test
    public void searchByQueryString() throws Exception{
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("用户体验").defaultField("content");
//        search(queryBuilder);
//        searchByPaper(queryBuilder);
        // 高亮查询
        search(queryBuilder,"content");
    }


}
