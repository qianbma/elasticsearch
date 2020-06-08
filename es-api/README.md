[toc]


# 一、kibana安装
1.下载：
```
https://www.elastic.co/cn/products/kibana
```
2.安装
```
tar -zxvf kibanaxxx.tar.gz
cd kibana_HOME
```
3、修改配置
```
vim config/kibana.yml
```
```
# 将默认配置改成如下：

server.port: 5601
server.host: "0.0.0.0"
elasticsearch.url: "http://127.0.0.1:9201"    //修改成自己集群的端口号
kibana.index: ".kibana"
```
4、运行启动
```
bin/kibana
```
5、访问
```
http://127.0.0.1:5601 
```

# 二、Restful Api
> ## 索引操作

### 1.创建
#### 只创建索引
```
PUT /test1
```
#### 创建索引并插入文档
```
PUT /test1
{
    "name":"狂神",
    "age":"12"
}
```
#### 创建索引和映射
```
PUT /test2
{
  "mappings": {
    "article":{
      "properties": {
        "name": {
          "type":"text",
          "store":true,
          "index":true
        },
        "age":{
          "type":"long",
          "store":true,
          "index":true
        },
        "birthday":{
          "type":"date",
          "store":true,
          "index":true
        }
      }
    }
  }
}
```
#### 获取规则
```
GET test2
```
#### put另一种方法，可用于修改
```
PUT /test2/type3/1
{
  "name":"绝品觉看狂神",
  "age":"1995",
  "nick":"大大大大"
}

```
#### 查看默认配置
```
GET _cat/indices?v

结果
health status index       uuid                   pri rep docs.count docs.deleted store.size pri.store.size
green  open   index_hello RA-jQiJ4RJm_mYDMrG8suQ   5   1         98            0    232.8kb        114.2kb
green  open   blog        4e4_2bDHTgmytJclwJJW2A   5   1          1            0      9.2kb          4.6kb
green  open   test1       tX789LgdQImecn5RUwZlWQ   5   1          1            0        9kb          4.5kb
green  open   hello       j2Sn2xFnRle-5ImCYIAiQg   5   1          0            0      1.5kb           810b
green  open   .kibana     G8MRxRn1RMGO6krfv1m0vA   1   1          1            0      6.4kb          3.2kb
green  open   test2       21yRlPnPQXeVKnI_pXgLJQ   5   1          1            0     10.1kb            5kb
```
### 3.修改
#### 方法一（推荐）
```
POST /test2/type3/1/_update
{
  "doc":{
    "age":"1996"
  }
}
```
#### 方法二 注：不能漏掉其他信息，漏掉则会删除
```
PUT /test2/type3/1
{
  "name":"绝品觉看狂神",
  "age":"1995",
  "nick":"大大大大"
}
```
### 4.删除
DELETE test1

> ## 文档操作

### 1. 插入
```
PUT /test4/user/3
{
  "name":"刘翔 ",
  "age":23,
  "desc":"一顿操作猛如虎，一看工作2500 ",
  "tags":["靓女 ","唱歌 ","渣男2"]
}
```
### 2. 查询
#### 根据id查询
```
GET /test4/user/1
```
#### 根据字段查询
分term查找和match查找。
区别：
> term：不使用分词器解析，使用倒排索引，效率高。
match:使用分词器解析，效率较低。

> 补充：text类型会被分词器解析，keyword不会

##### term精确查找
```
GET /test4/user/_search
{
  "query": {
    "term":{
      "name":"狂"
    }
  }
}
```
##### match先分词再查找(使用分词器解析)
分词：狂，神。
使用term不分词，查找失败(中文智能查一个字符)
```
GET /test4/user/_search
{
  "query": {
    "match":{
      "name":"狂神"
    }
  }
}
```
#### 根据字段查询并限定返回字段
```
GET /test4/user/_search
{
  "query": {
    "match": {
      "name": "刘"
    }
  },
  "_source":["name","age"]
}
```
#### 排序
升序和降序分别为desc,asc
根据年龄降序排序
```
GET /test4/user/_search
{
  "query": {
    "match": {
      "name": "狂"
    }
  },
  "sort":{
    "age":{
      "order":"desc"
    }
  }
}
```
#### 分页
使用form,size
```
GET /test4/user/_search
{
  "query": {
    "match": {
      "name": "狂"
    }
  },
  "from": 0,
  "size": 2
}
```
#### 多条件查询
##### and
使用must表示and
根据name和age查询
```
GET /test4/user/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match":{
            "name":"狂神"
          }
        },
        {
          "match":{
            "age":"21"
          }
        }
      ]
    }
  }
}
```
##### or
使用should表示or
```
GET /test4/user/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match":{
            "name":"狂神"
          }
        },
        {
          "match":{
            "age":"23"
          }
        }
      ]
    }
  }
}
```
##### field为数组类型的“or匹配”
```
GET /test4/user/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match":{
            "tags":"女 男"
          }
        }
      ]
    }
  }
}
```

#### 过滤器
##### 范围过滤
lt,lte,gt,gte
```
GET /test4/user/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match":{
            "name":"狂神"
          }
        },
        {
          "match":{
            "age":"23"
          }
        }
      ],
      "filter": {
        "range": {
          "age": {
            "gte": 10,
            "lte": 23
          }
        }
      }
    }
  }
}
```
#### 高亮
##### 默认高亮
```
GET /test4/user/_search
{
  "query": {
    "match":{
      "name":"狂神"
    }
  },
  "highlight": {
    "fields": {
      "name":{}
    }
  }
}
```
##### 自定义高亮显示
```
GET /test4/user/_search
{
  "query": {
    "match":{
      "name":"狂神"
    }
  },
  "highlight": {
    "pre_tags": "<p class='key' style='color:red'>", 
    "post_tags": "</p>", 
    "fields": {
      "name":{}
    }
  }
}
```
#### 其他
must_not === not

### 3. 更新
post方式，推荐
```
POST /test2/type3/1/_update
{
  "doc":{
    "age":"1996"
  }
}
```
put方式 注：不能漏掉其他信息，漏掉则会删除
```
PUT /test2/type3/1
{
  "name":"绝品觉看狂神",
  "age":"1995",
  "nick":"大大大大"
}
```
# 三、springboot集成es
1.建springboot工程项目
epmtyproject==> new Module ==>spirngboot initializr ==> 
module配置（Description删除，package由com.mqb.esapi改为com.mqb）==> 勾选依赖：nosql的es,springbootweb,devtools,loombook,configuration processor
==> 修改es依赖版本

> 官网查看对应版本,这里是5.6.16 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/5.6/java-rest-high-getting-started-maven.html
```
<properties>
        <elasticsearch.version>5.6.16</elasticsearch.version>
</properties>
```





