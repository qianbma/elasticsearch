package com.mqb.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

/**
 * 添加mappings/index/document
 */
public class ElasticSearchClient {

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

    @Test
    public void creatIndex()throws Exception{
        // 创建一个settings对象，相当于一个配置信息，主要配置集群的名称
        Settings settings  = Settings.builder()
                .put("cluster.name","my-elasticsearch")
                .build();
        // 创建一个客户端client对象
        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301));
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302));
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        // 使用client对象创建一个索引库
        client.admin().indices().prepareCreate("index_hello_test").get();
        // 关闭client对象
        client.close();
    }

    @Test
    public void setMappings()throws Exception{
        // 创建一个settings对象
        Settings settings = Settings.builder()
                .put("cluster.name","my-elasticsearch")
                .build();
        // 创建一个transportClient对象
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        // 创建一个mappings信息
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("article")
                        .startObject("properties")
                            .startObject("id")
                                .field("type","long")
                                .field("store",true)
                            .endObject()
                            .startObject("title")
                                .field("type","text")
                                .field("store",true)
                                .field("analyzer","ik_smart")
                            .endObject()
                            .startObject("content")
                                .field("type","text")
                                .field("store",true)
                                .field("analyzer","ik_smart")
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject();
        // 使用client把mapping信息设置到索引库中
        client.admin().indices()
                // 设置要映射的索引
                .preparePutMapping("index_hello")
                // 设置要做映射的type
                .setType("article")
                // mapping信息 可以上xcontentBuilder对象，也可以是json格式字符串
                .setSource(builder)
                // 执行操作
                .get();

        // 关闭client
        client.close();

    }

    @Test
    public void addDocument()throws Exception{
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id",1l)
                    .field("title","伊拉克输了")
                    .field("content","伊拉克输了，石油价格降了")
                .endObject();

        client.prepareIndex()
                .setIndex("index_hello")
                .setType("article")
                .setId("1")
                .setSource(builder)
                .get();

        client.close();

    }

    @Test
    public void addDocument2()throws Exception{
        // 创建一个article对象
        // 设置对象的属性
        // 把article对象转换为json格式的字符
        Article article  = new Article();
        article.setId(1l);
        article.setTitle("QQ邮箱");
        article.setContent("QQ邮箱,为亿万用户提供高效稳定便捷的电子邮件服务。你可以在电脑网页、iOS/iPad客户端、及Android客户端上使用它,通过邮件发送3G的超大附件,体验文件中转站、日历、...");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonDocument = objectMapper.writeValueAsString(article);
        System.out.println(jsonDocument);
        client.prepareIndex()
                .setIndex("index_hello")
                .setType("article").setId("3")
                .setSource(jsonDocument, XContentType.JSON)
                .get();
    }

    @Test
    public void addDocument3()throws Exception{
        // 创建一个article对象
        // 设置对象的属性
        // 把article对象转换为json格式的字符
        for (int i = 4; i < 100; i++) {
            Article article  = new Article();
            article.setId(i);
            article.setTitle(i+"_QQ邮箱");
            article.setContent(i+"_QQ邮箱,为亿万用户提供高效稳定便捷的电子邮件服务。你可以在电脑网页、iOS/iPad客户端、及Android客户端上使用它,通过邮件发送3G的超大附件,体验文件中转站、日历、...");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(article);
            System.out.println(jsonDocument);
            client.prepareIndex()
                    .setIndex("index_hello")
                    .setType("article").setId(i+"")
                    .setSource(jsonDocument, XContentType.JSON)
                    .get();
        }

    }
}
