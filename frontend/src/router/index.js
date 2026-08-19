import { createRouter, createWebHistory } from 'vue-router'
import ShopLayout from '../layouts/ShopLayout.vue'
import HomeView from '../views/HomeView.vue'
import { AUTH_TOKEN_KEY } from '../services/http'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: (to) => to.hash ? { el: to.hash, behavior: 'smooth' } : { top: 0 },
  routes: [
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
      ],
    },
  ],
})

router.beforeEach((to) => {
  const authenticated = Boolean(localStorage.getItem(AUTH_TOKEN_KEY))
  if (to.meta.requiresAuth && !authenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && authenticated) {
    return { name: 'profile' }
  }
  return true
})

export default router
