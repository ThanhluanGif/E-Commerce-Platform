package com.ecommerce.ecommerceapi.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssSanitizer {
    public static String sanitize(String html) {
        if (html == null) return null;
        // Cho phép các thẻ định dạng văn bản cơ bản và hình ảnh, loại bỏ thẻ script, iframe và các thuộc tính javascript onload, onclick...
        return Jsoup.clean(html, Safelist.basicWithImages());
    }
}
