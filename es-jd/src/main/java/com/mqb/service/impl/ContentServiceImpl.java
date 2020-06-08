package com.mqb.service.impl;

import com.alibaba.fastjson.JSON;
import com.mqb.pojo.Content;
import com.mqb.service.ContentService;
import com.mqb.utils.HtmlPaseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentServiceImpl implements ContentService {
    @Resource
    RestHighLevelClient client;

    public Boolean parseContent(String keywords) throws Exception {
        List<Content> contents = HtmlPaseUtil.parseJD(keywords);
        BulkRequest bulkRequest = new BulkRequest("jd_goods");
        bulkRequest.timeout("2m");
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(new IndexRequest()
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON)
            );
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }

    /**
     * 根据关键字到es中搜索
     * @param keywords
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> searchPages(String keywords, int pageNo, int pageSize) throws Exception {
        SearchRequest request = new SearchRequest("jd_goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().from(pageNo).size(pageSize);
        searchSourceBuilder.query(QueryBuilders.matchQuery("title", keywords));
//        searchSourceBuilder.query(QueryBuilders.termQuery("title.keyword",keywords));
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        SearchResponse searchResponse = client.search(request.source(searchSourceBuilder), RequestOptions.DEFAULT);
        if (searchResponse.getHits().getHits().length == 0) {
            System.out.println("第一次添加关键字为" + keywords + "的商品");
            parseContent(keywords);
            // 由于es数据可能并未插入完成
            while (searchResponse.getHits().getHits().length == 0) {
                Thread.yield();
                System.out.println("等待一会...再取数据");
                searchResponse = client.search(request.source(searchSourceBuilder), RequestOptions.DEFAULT);
            }
        }
        List<Map<String, Object>> lists = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            lists.add(searchHit.getSourceAsMap());
        }
        return lists;
    }

}
