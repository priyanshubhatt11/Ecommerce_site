package com.priyanshu.ecommerce.service;

import com.priyanshu.ecommerce.entity.ProductOrder;
import com.priyanshu.ecommerce.entity.ProductOrderRequest;

public interface ProductOrderService {
	
	public ProductOrder saveProductOrder(Long id, ProductOrderRequest productOrderRequest);
}
