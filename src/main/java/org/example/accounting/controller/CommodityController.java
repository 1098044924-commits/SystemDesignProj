package org.example.accounting.controller;

import java.util.List;
import org.example.accounting.domain.Commodity;
import org.example.accounting.domain.CommodityType;
import org.example.accounting.dto.CommodityDtos.CommodityResponse;
import org.example.accounting.dto.CommodityDtos.CreateCommodityRequest;
import org.example.accounting.exception.BusinessException;
import org.example.accounting.repository.CommodityRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 货币/商品管理 REST 控制器。
 */
@RestController
@RequestMapping("/api/commodities")
public class CommodityController {

    private final CommodityRepository commodityRepository;

    public CommodityController(CommodityRepository commodityRepository) {
        this.commodityRepository = commodityRepository;
    }

    /**
     * 查询所有货币/商品列表。
     */
    @GetMapping
    public List<CommodityResponse> list() {
        return commodityRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 创建货币/商品。
     */
    @PostMapping
    public CommodityResponse create(@RequestBody CreateCommodityRequest request) {
        // 参数校验
        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
            throw new BusinessException("符号不能为空");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException("名称不能为空");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new BusinessException("类型不能为空");
        }
        if (request.getFraction() == null) {
            throw new BusinessException("小数位数不能为空");
        }

        // 检查是否已存在
        if (commodityRepository.findBySymbol(request.getSymbol()).isPresent()) {
            throw new BusinessException("符号已存在: " + request.getSymbol());
        }

        // 解析类型
        CommodityType type;
        try {
            type = CommodityType.valueOf(request.getType());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("非法类型: " + request.getType());
        }

        Commodity commodity = Commodity.builder()
                .symbol(request.getSymbol())
                .name(request.getName())
                .type(type)
                .fraction(request.getFraction())
                .build();

        Commodity saved = commodityRepository.save(commodity);
        return toResponse(saved);
    }

    private CommodityResponse toResponse(Commodity commodity) {
        CommodityResponse response = new CommodityResponse();
        response.setId(commodity.getId());
        response.setSymbol(commodity.getSymbol());
        response.setName(commodity.getName());
        response.setType(commodity.getType() != null ? commodity.getType().name() : null);
        response.setFraction(commodity.getFraction());
        return response;
    }
}











