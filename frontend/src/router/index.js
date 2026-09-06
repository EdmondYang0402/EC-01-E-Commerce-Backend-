import { createRouter, createWebHistory } from 'vue-router'
import ShopLayout from '../layouts/ShopLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import HomeView from '../views/HomeView.vue'
import { AUTH_TOKEN_KEY } from '../services/http'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: (to) => to.hash ? { el: to.hash, behavior: 'smooth' } : { top: 0 },
  routes: [
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      redirect: '/admin/products',
      children: [
        { path: 'products', name: 'admin-products', meta: { title: 'EC-01 Admin | 商品管理' }, component: () => import('../views/admin/AdminProductListView.vue') },
        { path: 'products/create', name: 'admin-product-create', meta: { title: 'EC-01 Admin | 新增商品' }, component: () => import('../views/admin/AdminProductFormView.vue') },
        { path: 'products/:productId', name: 'admin-product-detail', meta: { title: 'EC-01 Admin | 商品详情' }, component: () => import('../views/admin/AdminProductDetailView.vue') },
        { path: 'products/:productId/edit', name: 'admin-product-edit', meta: { title: 'EC-01 Admin | 编辑商品' }, component: () => import('../views/admin/AdminProductFormView.vue') },
        { path: 'categories', name: 'admin-categories', meta: { title: 'EC-01 Admin | 分类管理' }, component: () => import('../views/admin/AdminCategoryView.vue') },
        { path: 'orders', name: 'admin-orders', meta: { title: 'EC-01 Admin | 订单管理' }, component: () => import('../views/admin/AdminOrderListView.vue') },
        { path: 'orders/:orderNo', name: 'admin-order-detail', meta: { title: 'EC-01 Admin | 订单详情' }, component: () => import('../views/admin/AdminOrderDetailView.vue') },
        { path: 'users', name: 'admin-users', meta: { title: 'EC-01 Admin | 用户管理' }, component: () => import('../views/admin/AdminUserListView.vue') },
        { path: 'security', name: 'admin-security', meta: { title: 'EC-01 Admin | 账户安全' }, component: () => import('../views/admin/AdminSecurityView.vue') },
      ],
    },
    {
      path: '/',
      component: ShopLayout,
      children: [
        {
          path: '',
          name: 'home',
          meta: { title: 'EC-01 | 首页' },
          component: HomeView,
        },
        {
          path: 'products',
          name: 'products',
          meta: { title: 'EC-01 | 商品' },
          component: () => import('../views/ProductListView.vue'),
        },
        {
          path: 'products/:id',
          name: 'product-detail',
          meta: { title: 'EC-01 | 商品详情' },
          component: () => import('../views/ProductDetailView.vue'),
        },
        {
          path: 'login',
          name: 'login',
          component: () => import('../views/LoginView.vue'),
          meta: { guestOnly: true, title: 'EC-01 | 登录' },
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('../views/RegisterView.vue'),
          meta: { guestOnly: true, title: 'EC-01 | 注册' },
        },
        {
          path: 'forbidden',
          name: 'forbidden',
          meta: { title: 'EC-01 | 无权访问' },
          component: () => import('../views/ForbiddenView.vue'),
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue'),
          meta: { requiresAuth: true, title: 'EC-01 | 账户' },
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('../views/CartView.vue'),
          meta: { requiresAuth: true, title: 'EC-01 | 购物车' },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('../views/OrderListView.vue'),
          meta: { requiresAuth: true, title: 'EC-01 | 我的订单' },
        },
        {
          path: 'orders/:orderNo',
          name: 'order-detail',
          component: () => import('../views/OrderDetailView.vue'),
          meta: { requiresAuth: true, title: 'EC-01 | 订单详情' },
        },
        {
          path: 'payment/result',
          name: 'payment-result',
          component: () => import('../views/PaymentResultView.vue'),
          meta: { requiresAuth: true, title: 'EC-01 | 支付结果' },
        },
        { path: ':pathMatch(.*)*', name: 'not-found', meta: { title: 'EC-01 | 页面不存在' }, component: () => import('../views/NotFoundView.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const authenticated = Boolean(localStorage.getItem(AUTH_TOKEN_KEY))
  if (to.meta.requiresAuth && !authenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && authenticated) {
    return { name: 'profile' }
  }
  if (to.meta.requiresAdmin) {
    const auth = useAuthStore()
    try {
      if (!auth.profile) await auth.fetchProfile()
    } catch {
      auth.clearSession()
      return { name: 'login', query: { redirect: to.fullPath } }
    }
    if (!auth.isAdmin) return { name: 'forbidden' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title || 'EC-01'
})

export default router
