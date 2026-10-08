package com.mycompany.bachhoaxanhonline.module.cart;

import com.mycompany.bachhoaxanhonline.common.ApiResponse;
import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.module.product.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartService {

    private final CartRepository cartRepository = new CartRepository();
    private final ProductRepository productRepository = new ProductRepository();

    public static class CartException extends RuntimeException {
        private final int statusCode;
        public CartException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
        public int getStatusCode() { return statusCode; }
    }

    public ApiResponse<CartResponse.CartDetailData> getCart(String customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId);
        if (cart == null) {
            throw new CartException(404, "Không tìm thấy giỏ hàng của khách hàng trong hệ thống");
        }

        CartResponse.CartDetailData data = new CartResponse.CartDetailData();
        data.setCartId(cart.getMaGioHang());
        data.setTotalAmount(cart.getTongTien());

        List<CartResponse.LineItemData> itemDataList = new ArrayList<>();
        int totalItems = 0;

        for (LineItem item : cart.getLineItems()) {
            if (item.getMaDonHang() != null) continue; // Skip items already ordered

            CartResponse.LineItemData itemData = new CartResponse.LineItemData();
            itemData.setLineItemId(item.getLineItemId());
            itemData.setProductId(item.getMaSanPham());
            itemData.setQuantity(item.getSoLuong());
            itemData.setItemTotal(item.getThanhTien());

            Product product = item.getProduct();
            if (product != null) {
                itemData.setProductName(product.getTenSanPham());
                itemData.setImageUrl(product.getHinhAnh());
                itemData.setUnit(product.getDonViTinh());
                itemData.setPrice(product.getGiaBan());
                itemData.setAvailableStock(product.getSoLuong());
            }

            itemDataList.add(itemData);
            totalItems += item.getSoLuong();
        }

        data.setItems(itemDataList);
        data.setTotalItems(totalItems);

        return new ApiResponse<>(200, "Lấy thông tin giỏ hàng thành công", data);
    }

    public ApiResponse<CartResponse.AddItemData> addItemToCart(String customerId, CartRequest.AddItemRequest request) {
        if (request.getProductId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new CartException(400, "Thiếu thông tin bắt buộc hoặc số lượng <= 0");
        }

        Cart cart = cartRepository.findByCustomerId(customerId);
        if (cart == null) {
            throw new CartException(404, "Không tìm thấy giỏ hàng của khách hàng trong hệ thống");
        }

        Optional<Product> productOpt = productRepository.findActiveProductById(request.getProductId());
        if (!productOpt.isPresent()) {
            throw new CartException(404, "Sản phẩm không tồn tại hoặc đã bị xóa");
        }

        Product product = productOpt.get();
        if (product.getSoLuong() < request.getQuantity()) {
            throw new CartException(409, "Số lượng yêu cầu vượt quá số lượng hàng tồn kho");
        }

        LineItem existingItem = null;
        for (LineItem item : cart.getLineItems()) {
            if (item.getMaDonHang() == null && item.getMaSanPham().equals(request.getProductId())) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            if (product.getSoLuong() < existingItem.getSoLuong() + request.getQuantity()) {
                throw new CartException(409, "Số lượng cộng dồn vượt quá tồn kho");
            }
            existingItem.setSoLuong(existingItem.getSoLuong() + request.getQuantity());
            existingItem.setThanhTien(product.getGiaBan().multiply(new BigDecimal(existingItem.getSoLuong())));
        } else {
            LineItem newItem = new LineItem();
            newItem.setCart(cart);
            newItem.setMaSanPham(product.getMaSanPham());
            newItem.setSoLuong(request.getQuantity());
            newItem.setThanhTien(product.getGiaBan().multiply(new BigDecimal(request.getQuantity())));
            cart.getLineItems().add(newItem);
            existingItem = newItem;
        }

        recalculateCartTotal(cart);
        cartRepository.update(cart);

        CartResponse.AddItemData data = new CartResponse.AddItemData();
        data.setCartId(cart.getMaGioHang());
        data.setLineItemId(existingItem.getLineItemId());
        data.setProductId(existingItem.getMaSanPham());
        data.setQuantity(existingItem.getSoLuong());
        data.setItemTotal(existingItem.getThanhTien());
        data.setTotalCartAmount(cart.getTongTien());

        return new ApiResponse<>(200, "Thêm sản phẩm vào giỏ hàng thành công", data);
    }

    public ApiResponse<CartResponse.UpdateItemData> updateItemQuantity(String customerId, Long lineItemId, CartRequest.UpdateItemRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new CartException(400, "Số lượng phải lớn hơn 0");
        }

        Cart cart = cartRepository.findByCustomerId(customerId);
        if (cart == null) {
            throw new CartException(404, "Không tìm thấy giỏ hàng");
        }

        LineItem targetItem = null;
        for (LineItem item : cart.getLineItems()) {
            if (item.getLineItemId().equals(lineItemId) && item.getMaDonHang() == null) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            throw new CartException(404, "Không tìm thấy dòng sản phẩm trong giỏ");
        }

        Optional<Product> productOpt = productRepository.findActiveProductById(targetItem.getMaSanPham());
        if (!productOpt.isPresent()) {
            throw new CartException(404, "Sản phẩm không tồn tại");
        }

        Product product = productOpt.get();
        if (product.getSoLuong() < request.getQuantity()) {
            throw new CartException(409, "Số lượng cập nhật vượt quá số lượng hàng tồn trong kho");
        }

        targetItem.setSoLuong(request.getQuantity());
        targetItem.setThanhTien(product.getGiaBan().multiply(new BigDecimal(request.getQuantity())));

        recalculateCartTotal(cart);
        cartRepository.update(cart);

        CartResponse.UpdateItemData data = new CartResponse.UpdateItemData();
        data.setLineItemId(targetItem.getLineItemId());
        data.setProductId(targetItem.getMaSanPham());
        data.setQuantity(targetItem.getSoLuong());
        data.setItemTotal(targetItem.getThanhTien());
        data.setTotalCartAmount(cart.getTongTien());

        return new ApiResponse<>(200, "Cập nhật số lượng thành công", data);
    }

    public ApiResponse<CartResponse.CartDetailData> removeItem(String customerId, Long lineItemId) {
        Cart cart = cartRepository.findByCustomerId(customerId);
        if (cart == null) {
            throw new CartException(404, "Không tìm thấy giỏ hàng");
        }

        boolean removed = cart.getLineItems().removeIf(item -> item.getLineItemId().equals(lineItemId) && item.getMaDonHang() == null);
        if (!removed) {
            throw new CartException(404, "Không tìm thấy sản phẩm trong giỏ hàng");
        }

        recalculateCartTotal(cart);
        cartRepository.update(cart);

        CartResponse.CartDetailData data = new CartResponse.CartDetailData();
        data.setTotalAmount(cart.getTongTien());
        return new ApiResponse<>(200, "Xóa sản phẩm khỏi giỏ hàng thành công", data);
    }

    public ApiResponse<CartResponse.CartDetailData> clearCart(String customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId);
        if (cart == null) {
            throw new CartException(404, "Không tìm thấy giỏ hàng");
        }

        cart.getLineItems().removeIf(item -> item.getMaDonHang() == null);
        cart.setTongTien(BigDecimal.ZERO);
        cartRepository.update(cart);

        CartResponse.CartDetailData data = new CartResponse.CartDetailData();
        data.setTotalAmount(BigDecimal.ZERO);
        data.setTotalItems(0);
        return new ApiResponse<>(200, "Đã làm trống giỏ hàng thành công", data);
    }

    private void recalculateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : cart.getLineItems()) {
            if (item.getMaDonHang() == null) { // Only count items not yet ordered
                total = total.add(item.getThanhTien());
            }
        }
        cart.setTongTien(total);
    }
}
