package com.mqb.controller;

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
