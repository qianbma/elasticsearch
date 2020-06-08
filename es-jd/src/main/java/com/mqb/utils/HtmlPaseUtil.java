package com.mqb.utils;


import com.mqb.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HtmlPaseUtil {

    public static void main(String args[]) throws Exception{
        //    https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword=java";
        Document document = Jsoup.parse(new URL(url),30000);
        List<Content> goodlists = parseJD("心理学");
        System.out.println(goodlists);
    }

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
