package com.murto.goods.client;

import com.murto.goods.dto.ShopDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;


// 请求数据类
@Component
public class ShopClient {

    private final RestClient restClient;

    public ShopClient(
            @Value("${services.shop.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public ShopDTO getShop(Long shopId) {
        return restClient.get()
                .uri("/shop/info?shopId={shopId}", shopId)
                .retrieve()
                .body(ShopDTO.class);
    }
}
