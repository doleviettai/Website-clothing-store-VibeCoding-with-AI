import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// Import Layouts
import AuthLayout from '@/layouts/AuthLayout.vue'
import ClientLayout from '@/layouts/ClientLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

// Import Views
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ForbiddenView from '@/views/error/ForbiddenView.vue'
import NotFoundView from '@/views/error/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ─── Auth Routes (Sử dụng AuthLayout) ─────────────────────────
    {
      path: '/auth',
      component: AuthLayout,
      children: [
        {
          path: '/login',
          name: 'login',
          component: LoginView,
          meta: { guestOnly: true },
        },
        {
          path: '/register',
          name: 'register',
          component: RegisterView,
          meta: { guestOnly: true },
        },
      ],
    },

    // ─── Client Routes (Sử dụng ClientLayout) ──────────────────────
    {
      path: '/',
      component: ClientLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/client/HomeView.vue'),
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('@/views/client/ProductListView.vue'),
        },
        {
          path: 'products/:id',
          name: 'product-detail',
          component: () => import('@/views/client/ProductDetailView.vue'),
        },
        {
          path: 'product/:id',
          name: 'product-detail-alt',
          component: () => import('@/views/client/ProductDetailView.vue'),
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('@/views/client/CartView.vue'),
        },
        {
          path: 'checkout',
          name: 'checkout',
          component: () => import('@/views/client/CheckoutView.vue'),
        },
        {
          path: 'favorites',
          name: 'favorites',
          component: () => import('@/views/client/FavoriteView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('@/views/client/OrderHistoryView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'payment-result',
          name: 'payment-result',
          component: () => import('@/views/client/PaymentResultView.vue'),
        },
        {
          path: 'zalopay-pay',
          name: 'zalopay-mock-gateway',
          component: () => import('@/views/client/ZaloPayMockGatewayView.vue'),
        },
        {
          path: 'momo-pay',
          name: 'momo-mock-gateway',
          component: () => import('@/views/client/MoMoMockGatewayView.vue'),
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/client/ProfileView.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },

    // ─── Admin Routes (Sử dụng AdminLayout) ───────────────────────
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiredRole: 'ADMIN' },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard',
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/DashboardView.vue'),
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/CategoryView.vue'),
        },
        {
          path: 'brands',
          name: 'admin-brands',
          component: () => import('@/views/admin/BrandView.vue'),
        },
        {
          path: 'banners',
          name: 'admin-banners',
          component: () => import('@/views/admin/BannerView.vue'),
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('@/views/admin/ProductView.vue'),
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('@/views/admin/OrderView.vue'),
        },
        {
          path: 'reviews',
          name: 'admin-reviews',
          component: () => import('@/views/admin/ReviewView.vue'),
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/UserView.vue'),
        },
        {
          path: 'payments',
          name: 'admin-payments',
          component: () => import('@/views/admin/PaymentView.vue'),
        },
      ],
    },

    // ─── Error Routes ──────────────────────────────────────────────
    {
      path: '/403',
      name: 'forbidden',
      component: ForbiddenView,
    },

    // Catch-all (404 Not Found)
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

// ─── Navigation Guards ──────────────────────────────────────────────
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  if (authStore.accessToken && !authStore.user) {
    await authStore.fetchCurrentUser()
  }

  // Route chỉ dành cho khách (Chưa đăng nhập, VD: /login, /register)
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    if (authStore.isAdmin) {
      return next('/admin/dashboard')
    }
    return next('/')
  }

  // Route yêu cầu đăng nhập
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }

  // Route yêu cầu phân quyền Role (VD: ADMIN)
  if (to.meta.requiredRole) {
    if (!authStore.user?.roles?.includes(to.meta.requiredRole)) {
      return next({ name: 'forbidden' })
    }
  }

  next()
})

export default router
