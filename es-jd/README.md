# 三、springboot集成es
1.建springboot工程项目
epmtyproject==> new Module ==>spirngboot initializr ==> 
module配置（Description删除，package由com.mqb.esapi改为com.mqb
2.添加依赖
```
<dependencies>
    <!--网页解析-->
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.10.2</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.62</version>
    </dependency>
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>7.2.0</version>
    </dependency>
    <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
        <version>7.2.0</version>
    </dependency>
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-client</artifactId>
        <version>7.2.0</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.junit.vintage</groupId>
                <artifactId>junit-vintage-engine</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```
3.application.properties配置文件
```
server.port=9090
spring.thymeleaf.cache=false

elasticsearch.host.first=127.0.0.1
elasticsearch.port.first=9201
elasticsearch.host.second=127.0.0.1
elasticsearch.port.second=9202
elasticsearch.host.third=127.0.0.1
elasticsearch.port.third=9203
elasticsearch.protocol=http
```
4.添加es客户端配置类，用于建立和es集群的连接
```
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfig {
    @Value("${elasticsearch.host.first}")
    private String ES_IP_O1;
    @Value("${elasticsearch.host.second}")
    private String ES_IP_O2;
    @Value("${elasticsearch.host.third}")
    private String ES_IP_O3;
    @Value("${elasticsearch.port.first}")
    private int ES_PORT_01;
    @Value("${elasticsearch.port.second}")
    private int ES_PORT_02;
    @Value("${elasticsearch.port.third}")
    private int ES_PORT_03;
    @Value("${elasticsearch.protocol}")
    private String PORTOCOL;

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(ES_IP_O1, ES_PORT_01, PORTOCOL),
                        new HttpHost(ES_IP_O2, ES_PORT_02, PORTOCOL),
                        new HttpHost(ES_IP_O3, ES_PORT_03, PORTOCOL)));
        return client;
    }
}
```
5.添加工具类
> 工具类的作用:传入搜索关键字，根据关键字搜索京东商品，根据索索到的网页进行解析(jsoup)，并解析的商品存入elasticsearch中

```
import com.mqb.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HtmlPaseUtil {

    /**
     * 如果中文不行，在url后拼接&enc=utf-8
     * @param keywords
     * @return
     * @throws Exception
     */
    public static List<Content> parseJD(String keywords)throws Exception{
        //    https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword="+keywords;
        Document document = Jsoup.parse(new URL(url),30000);
        Element element = document.getElementById("J_goodsList");
//        System.out.println(element.html());
        Elements elements = element.getElementsByTag("li");
        List<Content> goodLists = new ArrayList<>();
        for (Element el : elements){
//            String img = el.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            String img = el.getElementsByTag("img").eq(0).attr("src");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text().split(" ")[0];
            String shop = el.getElementsByClass("p-shop").eq(0).text();
            goodLists.add(new Content(title,img,price,shop));
        }
        return goodLists;
    }

}
```
6.pojo商品实体类
属性包括：`商品名` `图片` `价格` `店铺`
```
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private String title;
    private String img;
    private String price;
    private String shop;
}
```

7.controller
```
import com.mqb.service.ContentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class ContentController {

    @Resource
    private ContentService contentService;

    @GetMapping("/parse/{keywords}")
    public String parse(@PathVariable("keywords") String keywords) throws Exception {
        return String.valueOf(contentService.parseContent(keywords));
    }

    @GetMapping("/search")
    public String search(@RequestParam("keywords") String keywords, WebRequest webRequest) throws Exception {


        List<Map<String, Object>> goodList = contentService.searchPages(keywords, 0, 10);
        webRequest.setAttribute("goodsList", goodList, RequestAttributes.SCOPE_REQUEST);
        return "index";
    }
}
```
8.service
```
import java.util.List;
import java.util.Map;

public interface ContentService {
    Boolean parseContent(String keywords) throws Exception;

    List<Map<String, Object>> searchPages(String keywords, int pageNo, int pageSize) throws Exception;

}
```
```
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
```
```
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
```
9.结果展示
输入：
> http://localhost:9090

打开网页如下：

输入框搜索关键字java:
结果如下







