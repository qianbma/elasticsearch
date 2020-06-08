package com.mqb.service;

import java.util.List;
import java.util.Map;

public interface ContentService {
    Boolean parseContent(String keywords) throws Exception;

    List<Map<String, Object>> searchPages(String keywords, int pageNo, int pageSize) throws Exception;

}