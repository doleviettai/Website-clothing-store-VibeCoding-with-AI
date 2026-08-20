<script setup>
import { ref, computed } from 'vue'

const cartItems = ref([
  {
    id: 101,
    productName: 'Áo Khoác Biker Piqué',
    variant: 'Đen / M',
    image: '/img/shopping-cart/cart-1.jpg',
    price: 1550000,
    quantity: 1,
  },
  {
    id: 102,
    productName: 'Áo Polo Nam Thêu Logo',
    variant: 'Trắng / L',
    image: '/img/shopping-cart/cart-2.jpg',
    price: 490000,
    quantity: 2,
  },
])

const updateQuantity = (item, change) => {
  if (item.quantity + change >= 1) {
    item.quantity += change
  }
}

const removeItem = (id) => {
  cartItems.value = cartItems.value.filter(i => i.id !== id)
}

const subtotal = computed(() => {
  return cartItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0)
})

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="cart-page">
    <!-- Breadcrumb Begin -->
    <section class="breadcrumb-option">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <div class="breadcrumb__text">
              <h4>Giỏ hàng</h4>
              <div class="breadcrumb__links">
                <RouterLink to="/">Trang chủ</RouterLink>
                <RouterLink to="/products">Cửa hàng</RouterLink>
                <span>Giỏ hàng</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <!-- Breadcrumb End -->

    <!-- Shopping Cart Section Begin -->
    <section class="shopping-cart spad">
      <div class="container">
        <div class="row" v-if="cartItems.length > 0">
          <div class="col-lg-8">
            <div class="shopping__cart__table">
              <table>
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Số lượng</th>
                    <th>Tổng tiền</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in cartItems" :key="item.id">
                    <td class="product__cart__item">
                      <div class="product__cart__item__pic">
                        <img :src="item.image" :alt="item.productName">
                      </div>
                      <div class="product__cart__item__text">
                        <h6>{{ item.productName }}</h6>
                        <small class="text-muted">Biến thể: {{ item.variant }}</small>
                        <h5>{{ formatPrice(item.price) }}</h5>
                      </div>
                    </td>
                    <td class="quantity__item">
                      <div class="quantity-control d-flex align-items-center">
                        <button class="btn btn-sm btn-outline-secondary" @click="updateQuantity(item, -1)">-</button>
                        <span class="mx-3 font-weight-bold">{{ item.quantity }}</span>
                        <button class="btn btn-sm btn-outline-secondary" @click="updateQuantity(item, 1)">+</button>
                      </div>
                    </td>
                    <td class="cart__price">{{ formatPrice(item.price * item.quantity) }}</td>
                    <td class="cart__close">
                      <i class="fa fa-close" @click="removeItem(item.id)" style="cursor: pointer;"></i>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="row">
              <div class="col-lg-6 col-md-6 col-sm-6">
                <div class="continue__btn">
                  <RouterLink to="/products" class="btn btn-outline-dark">Tiếp tục mua hàng</RouterLink>
                </div>
              </div>
            </div>
          </div>

          <div class="col-lg-4">
            <div class="cart__discount">
              <h6>Mã giảm giá</h6>
              <form @submit.prevent>
                <input type="text" placeholder="Nhập mã ưu đãi">
                <button type="submit">Áp dụng</button>
              </form>
            </div>
            <div class="cart__total">
              <h6>Tổng đơn hàng</h6>
              <ul>
                <li>Tạm tính <span>{{ formatPrice(subtotal) }}</span></li>
                <li>Phí vận chuyển <span>Miễn phí</span></li>
                <li>Tổng cộng <span>{{ formatPrice(subtotal) }}</span></li>
              </ul>
              <RouterLink to="/checkout" class="primary-btn btn-block text-center">Tiến hành thanh toán</RouterLink>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-5">
          <img src="/img/icon/cart.png" alt="Cart Empty" style="width: 64px; opacity: 0.5;" class="mb-3">
          <h4>Giỏ hàng của bạn đang trống</h4>
          <p class="text-muted mb-4">Hãy khám phá thêm hàng ngàn sản phẩm thời trang hấp dẫn tại Male Fashion.</p>
          <RouterLink to="/products" class="primary-btn">Khám phá cửa hàng</RouterLink>
        </div>
      </div>
    </section>
    <!-- Shopping Cart Section End -->
  </div>
</template>

<style scoped>
.breadcrumb-option {
  background: #f3f2ee;
  padding: 40px 0;
}

.shopping__cart__table table tbody tr td.cart__close i:hover {
  color: #e53637;
}
</style>
