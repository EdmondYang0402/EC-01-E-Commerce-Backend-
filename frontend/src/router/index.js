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
        { path: 'products', name: 'admin-products', component: () => import('../views/admin/AdminProductListView.vue') },
        { path: 'products/create', name: 'admin-product-create', component: () => import('../views/admin/AdminProductFormView.vue') },
        { path: 'products/:productId', name: 'admin-product-detail', component: () => import('../views/admin/AdminProductDetailView.vue') },
        { path: 'products/:productId/edit', name: 'admin-product-edit', component: () => import('../views/admin/AdminProductFormView.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('../views/admin/AdminCategoryView.vue') },
        { path: 'orders', name: 'admin-orders', component: () => import('../views/admin/AdminOrderListView.vue') },
        { path: 'orders/:orderNo', name: 'admin-order-detail', component: () => import('../views/admin/AdminOrderDetailView.vue') },
        { path: 'users', name: 'admin-users', component: () => import('../views/admin/AdminUserListView.vue') },
      ],
    },
    {
      path: '/',
      component: ShopLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: HomeView,
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('../views/ProductListView.vue'),
        },
        {
          path: 'products/:id',
          name: 'product-detail',
          component: () => import('../views/ProductDetailView.vue'),
        },
        {
          path: 'login',
          name: 'login',
          component: () => import('../views/LoginView.vue'),
          meta: { guestOnly: true },
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('../views/RegisterView.vue'),
          meta: { guestOnly: true },
        },
        {
          path: 'forbidden',
          name: 'forbidden',
          component: () => import('../views/ForbiddenView.vue'),
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('../views/CartView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('../views/OrderListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'orders/:orderNo',
          name: 'order-detail',
          component: () => import('../views/OrderDetailView.vue'),
          meta: { requiresAuth: true },
        },
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

export default router
