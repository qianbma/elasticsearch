# elasticsearch学习
# 1. 安装es：linux和mac
#### 1.1下载：
官网：https://www.elastic.co/downloads/elasticsearch
找到对应版本下载
#### 1.2解压
#### 1.3启动
进入bin目录，执行elasticsearch脚本
#### 1.4验证
访问localhost:9200

# 2.安装head插件
#### 2.1下载head插件https://github.com/mobz/elasticsearch-head
#### 2.2没有node先安装node https://www.cnblogs.com/wang-yaz/p/10168862.html
#### 2.3npm install          //这个不能少
#### 2.4npm install -g grunt -cli
#### 2.5grunt server  //进入head目录，启动
![ ](https://github.com/xingdaomqb/es-first/blob/master/image/image.png)
![ ](https://github.com/xingdaomqb/es-first/blob/master/image/2.png)
#### 2.6连接
##### 2.6.1 config/elasticsearch.yml新增以下两行
```
http.cors.enabled: true
http.cors.allow-origin: "*"
```
#### 重启es
点击head的web页面的连接按钮，可以看到连接成功
![ ](https://github.com/xingdaomqb/es-first/blob/master/image/3.png)
# 3.使用
#### 3.1 索引
3.1.1 页面操作
3.1.2 postman操作：http://127.0.0.1:9200/blog put方法

#### 3.2 mappings
3.2.1 新建mappings
http://127.0.0.1:9200/blog1 put方法  body raw使用json如下
```
{
	"mappings":{
		"article":{            
			"properties":{    //表
				"id":{    //字段
					"type":"long",
					"store":true
				},
				"title":{
					"type":"text",
					"store":true,
					"index":true,
					"analyzer":"standard"
				},
				"content":{
					"type":"text",
					"store":true,
					"index":true,
					"analyzer":"standard"
				}
			}
		}
	}
}
```
3.2.2 修改mappings
http://127.0.0.1:9200/blog/hello?_mappings post方法
```
{
	"hello":{
		"properties":{
			"id":{
				"type":"long",
				"store":true
			},
			"title":{
				"type":"text",
				"store":true,
				"index":true,
				"analyzer":"standard"
			},
			"content":{
				"type":"text",
				"store":true,
				"index":true,
				"analyzer":"standard"
			}
		}
	}
}
```
3.2.3 删除mappings
http://127.0.0.1:9200/blog delete方法

#### 3.3 文档
3.3.1 新建文档
localhost:9200/blog1/article
http://127.0.0.1:9200/blog/hello/1 post方法，body如下
```
{
	"id":1,
	"title":"新添加的文档1",
	"content":"新添加的文档的内容"
}
```
http://127.0.0.1:9200/blog/hello  post方法 不设置url的id,后台会生成一个随机串

以下是head插件过滤功能
![ ](https://github.com/xingdaomqb/es-first/blob/master/image/4.png)
3.3.2删除文档
 http://127.0.0.1:9200/blog/hello/1  delete方法
3.3.3修改文档(本质先删除后添加)
http://127.0.0.1:9200/blog/hello/1  post方法
3.3.4查询文档
3.3.4.1 根据id查询
```
http://127.0.0.1:9200/blog/hello/1  get方法 查询id为1的文档
```
3.3.4.2 指定字段查询
```
http://127.0.0.1:9200/blog/hello/_search  get方法
```
```
{
	"query":{
		"term":{
			"content":"修"
		}
	}
}
```
注：使用term只能查询一个中文字符
查询多个中文字符(原理是会将查询的字符串分词)使用query_string，如下
http://127.0.0.1:9200/blog/hello/_search  get方法 body如下：
```
{
	"query":{
		"query_string":{
			"default_field":"title",
			"query":"修改"
		}
	}
}
```
查看查询字符串如何分词
```
http://127.0.0.1:9200/_analyze?analyzer=standard&text=天龙八部
```
 使用标准分词器会被分为：”天“ ”龙“ ”八“ ”部“
**补充：ik分词插件**
1. 下载
https://github.com/medcl/elasticsearch-analysis-ik/releases/tag/v5.6.8
2. 解压到elasticsearch文件夹的plugins目录中，改名为ik-analyzer
3. 重启es
4. ik分词两种算法：ik_smart ik_max_word
5. 接下来分别使用ik_smart和ik_max_word分词器访问
`<http://127.0.0.1:9200/_analyze?analyzer=ik_smart&text=我是黑马程序员>`
分词效果：”我“ ”是“ ”黑马“ ”程序员“
`<http://127.0.0.1:9200/_analyze?analyzer=ik_max_word&text=我是黑马程序员>`
分词效果：”我“ ”是“ ”黑马“ ”程序员“ ”程序“ ”员“

# 4 es集群搭建：
#### 4.1 复制多份es文件，并删除其中data目录
#### 4.2 进入config目录，修改elasticsearch.yml,新增如下配置(不同节点修改node.name、network.host、http.port、transport.tcp.port)
```
##集群名，同一集群中集群名相同
cluster.name: my-elasticsearch
##节点名
node.name: node-1
##ip和端口
network.host: 127.0.0.1
http.port: 9201
##集群间通信端口号，同一机器上必须不一样
transport.tcp.port: 9301
##设置集群自动发现机器ip集合
discovery.zen.ping.unicast.hosts: ["127.0.0.1:9301","127.0.0.1:9302","127.0.0.1:9303"]
```
#### 4.3 分别启动各个es节点
#### 4.4 head插件web页面验证，随便访问127.0.0.1:9201，可得到如下界面
![](https://github.com/xingdaomqb/es-first/blob/master/image/5.png)
#### 4.5 验证集群
4.5.1. 新建index
![](https://github.com/xingdaomqb/es-first/blob/master/image/6.png)
4.5.2. http://127.0.0.1:9201/blog put方法
```
{
	"mappings":{
		"article":{            
			"properties":{    //表
				"id":{    //字段
					"type":"long",
					"store":true
				},
				"title":{
					"type":"text",
					"store":true,
					"index":true,
					"analyzer":"ik_smart"
				},
				"content":{
					"type":"text",
					"store":true,
					"index":true,
					"analyzer":"ik_smart"
				}
			}
		}
	}
}
```
添加type
http://127.0.0.1:9201/blog/hello post方法
```
{
	"id":1,
	"title":"宇宙大爆炸",
	"content":"新华社报道，不久将发生宇宙大爆炸"
}
```
# 5 使用java代码操作es
#### 5.1 新建项目: 使用idea新建EmptyProject ==> new Module ==> 选中maven，直接next ==> 填写项目坐标 
#### 5.2 pom.xml文件添加依赖
```
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>5.6.8</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>transport</artifactId>
    <version>5.6.8</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-to-slf4j</artifactId>
    <version>2.9.1</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.24</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.21</version>
</dependency>
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.12</version>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
</dependency>
```
#### 5.3 创建索引库
```
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
    client.admin().indices().prepareCreate("index_hello").get();
    // 关闭client对象
    client.close();
}
```
结果:
![](https://github.com/xingdaomqb/es-first/blob/master/image/7.png)
#### 5.4 设置mappings
```
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
```
#### 5.5 添加文档
```
// 创建一个settings对象
Settings settings = Settings.builder()
        .put("cluster.name","my-elasticsearch")
        .build();
// 创建一个transportClient对象
client = new PreBuiltTransportClient(settings)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        
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
```
补充：可以不用XcontentBuilder。直接写一个pojo对象，转成json就行
添加json依赖
```
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.9.6</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <version>2.9.0</version>
</dependency>
```
Article对象
```
public class Article {
    private Long id;
    private String title;
    private String content;
    // getter/setter...
}
```
```
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
```
#### 5.6 搜索
分类：id查询，term查询，query_string查询
5.6.1 根据id搜索
```
@Test
public void searchById(){
    QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds("1","2");
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
```
部分代码可以封装一下，如下：
```
@Test
public void searchById()throws Exception{
    QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds("1","2");
    search(queryBuilder);
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
```
后续也是基于该封装方法
5.6.2 根据Term查询
```
@Test
public void searchByTerm()throws Exception{
    QueryBuilder queryBuilder = QueryBuilders.termQuery("title","伊拉克");
    search(queryBuilder);
}
```
5.6.3 根据QueryString查询
```
@Test
public void searchByQueryString() throws Exception{
    QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("用户体验").defaultField("content");
    search(queryBuilder);
}
```
#### 5.7 分页处理
设置from和size,如下
```
SearchResponse searchResponse = client.prepareSearch().setIndices("index_hello")
        .setTypes("article")
        .setQuery(queryBuilder)
        .setFrom(0)
        .setSize(4)
        .get();
```
#### 5.8 查询结果高亮显示(原理，高亮地方添加<em>xxx</em>标签)
1)设置高亮显示的字段
2)设置高亮显示的前缀
3)设置高亮显示的后缀
4)在client执行查询之前设置高亮显示信息
5)遍历结果列表时可以从结果中取出高亮显示结果
```
@Test
public void searchByQueryString() throws Exception{
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("用户体验").defaultField("content");
        // 高亮查询
        search(queryBuilder,"content");
    }
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
```
结果：
```
**********高亮结果***********
7_QQ邮箱,为亿万<em>用户</em>提供高效稳定便捷的电子邮件服务。你可以在电脑网页、iOS/iPad客户端、及Android客户端上使用它,通过邮件发送3G的超大附件,<em>体验</em>文件中转站、日历、...
```
# 6. springdata 使用
