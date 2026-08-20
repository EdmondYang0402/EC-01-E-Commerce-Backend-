<script setup>
import { watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { errorMessage } from '../../services/http'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import { useLocaleStore } from '../../stores/locale'
import UiIcon from './UiIcon.vue'

const navigation = [
  { key: 'nav.shop', to: '/products' },
  { key: 'nav.categories', to: '/#categories' },
  { key: 'nav.newArrivals', to: '/#new-arrivals' },
  { key: 'nav.collections', to: '/#featured' },
  { key: 'nav.about', to: '/#footer' },
]

const auth = useAuthStore()
const router = useRouter()
const { isAuthenticated } = storeToRefs(auth)
const cart = useCartStore()
const { itemCount } = storeToRefs(cart)
const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)

const logout = async () => {
  try {
    await auth.logout()
    ElMessage.success(t('header.logoutSuccess'))
  } catch (error) {
    ElMessage.warning(errorMessage(error, t('profile.sessionCleared')))
  }
  cart.clear()
  await router.push('/products')
}

watch(isAuthenticated, async (authenticated) => {
  if (!authenticated) {
    cart.clear()
    return
  }
  try {
    await cart.fetchCart()
  } catch {
    // The global 401 handler clears an expired session. Other errors are shown on /cart.
  }
}, { immediate: true })
</script>

<template>
  <header class="site-header">
    <div class="site-header__inner page-shell">
      <RouterLink class="wordmark" to="/" :aria-label="t('header.home')">EC-01</RouterLink>
      <nav class="main-nav" :aria-label="t('header.mainNav')">
        <RouterLink v-for="item in navigation" :key="item.key" :to="item.to">{{ t(item.key) }}</RouterLink>
      </nav>
      <div class="header-actions">
        <button type="button" :aria-label="t('header.search')"><UiIcon name="search" /></button>
        <label class="language-picker">
          <span class="sr-only">{{ t('header.language') }}</span>
          <select :value="locale.locale" :aria-label="t('header.language')" @change="locale.setLocale($event.target.value)">
            <option value="zh">中文</option>
            <option value="en">EN</option>
            <option value="ja">日本語</option>
          </select>
        </label>
        <RouterLink class="account-action" :to="isAuthenticated ? '/profile' : '/login'" :aria-label="t('header.account')">
          <UiIcon name="user" />
        </RouterLink>
        <RouterLink v-if="isAuthenticated" class="text-action" to="/orders">{{ t('nav.orders') }}</RouterLink>
        <RouterLink v-else class="text-action" to="/login">{{ t('header.login') }}</RouterLink>
        <RouterLink class="cart-action" to="/cart" :aria-label="t('header.cart')">
          <UiIcon name="cart" />
          <span v-if="itemCount > 0" class="cart-count">{{ itemCount }}</span>
        </RouterLink>
        <button v-if="isAuthenticated" class="logout-action" type="button" @click="logout">{{ t('header.logout') }}</button>
        <button class="menu-action" type="button" :aria-label="t('header.menu')"><UiIcon name="menu" /></button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header { position: relative; z-index: 20; height: 68px; background: var(--white); border-bottom: 1px solid var(--line); }
.site-header__inner { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; height: 100%; }
.wordmark { width: max-content; color: var(--ink); font-size: 25px; font-weight: 750; letter-spacing: -.06em; text-decoration: none; }
.main-nav { display: flex; align-items: center; gap: clamp(24px, 2.5vw, 44px); }
.main-nav a { position: relative; color: var(--ink); font-size: 13px; font-weight: 550; text-decoration: none; white-space: nowrap; }
.main-nav a::after { position: absolute; right: 0; bottom: -8px; left: 0; height: 1px; background: var(--ink); content: ''; transform: scaleX(0); transform-origin: left; transition: transform 160ms ease; }
.main-nav a:hover::after, .main-nav a:focus-visible::after { transform: scaleX(1); }
.header-actions { display: flex; justify-content: flex-end; align-items: center; gap: 13px; }
.header-actions button { position: relative; display: grid; width: 34px; height: 34px; padding: 0; place-items: center; color: var(--ink); background: transparent; border: 0; cursor: pointer; }
.account-action { display: grid; width: 34px; height: 34px; place-items: center; color: var(--ink); }
.cart-action { position: relative; display: grid; width: 34px; height: 34px; place-items: center; color: var(--ink); text-decoration: none; }
.text-action { color: var(--ink); font-size: 10px; font-weight: 700; text-decoration: none; text-transform: uppercase; }
.header-actions .logout-action { width: auto; padding: 0 4px; font-size: 10px; font-weight: 700; text-transform: uppercase; }
.language-picker select { height: 30px; padding: 0 22px 0 8px; color: var(--ink); font-size: 10px; font-weight: 650; background: var(--white); border: 1px solid var(--line); border-radius: 2px; cursor: pointer; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; clip-path: inset(50%); }
.cart-count { position: absolute; top: -1px; right: -2px; display: grid; min-width: 16px; height: 16px; padding: 0 4px; place-items: center; color: white; font-size: 9px; font-weight: 700; line-height: 1; background: var(--red); border-radius: 99px; }
.menu-action { display: none !important; }

@media (max-width: 900px) {
  .site-header__inner { grid-template-columns: 1fr auto; }
  .main-nav { display: none; }
  .menu-action { display: grid !important; }
}

@media (max-width: 560px) {
  .site-header { height: 60px; }
  .header-actions > button:first-child { display: none; }
  .language-picker select { width: 58px; padding-right: 4px; }
}
</style>
