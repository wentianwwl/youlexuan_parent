package com.offcn.page.service;

public interface ItemPageService {
    /**
     * 根据商品id生成商品详细页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);
}
