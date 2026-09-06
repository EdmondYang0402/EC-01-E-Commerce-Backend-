<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { errorMessage } from '../services/http'
import { useAuthStore } from '../stores/auth'
import { useLocaleStore } from '../stores/locale'
import '../assets/styles/admin.css'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)
const menuOpen = ref(false)

const navigation = computed(() => [
  { label: t('admin.nav.products'), to: '/admin/products', mark: 'P' },
  { label: t('admin.nav.categories'), to: '/admin/categories', mark: 'C' },
  { label: t('admin.nav.orders'), to: '/admin/orders', mark: 'O' },
  { label: t('admin.nav.users'), to: '/admin/users', mark: 'U' },
])
const localeOptions = [
  { value: 'zh-CN', label: '中文' },
  { value: 'en-US', label: 'English' },
  { value: 'ja-JP', label: '日本語' },
]
const breadcrumb = computed(() => {
  const labels = {
    'admin-products': 'admin.products.title', 'admin-product-create': 'admin.productForm.createTitle',
    'admin-product-detail': 'admin.products.detailSku', 'admin-product-edit': 'admin.productForm.editTitle',
    'admin-categories': 'admin.categories.title', 'admin-orders': 'admin.orders.title',
    'admin-order-detail': 'admin.orderDetail.items', 'admin-users': 'admin.users.title',
    'admin-security': 'admin.security.title',
  }
  return labels[route.name] ? t(labels[route.name]) : ''
})

const closeMenu = () => { menuOpen.value = false }
const logout = async () => {
  try {
    await auth.logout()
  } catch (error) {
    ElMessage.warning(errorMessage(error, t('admin.message.sessionCleared')))
  }
  await router.replace('/login')
}
</script>

<template>
  <div class="admin-layout">
    <header class="admin-topbar">
      <div class="admin-topbar__brand">
        <button class="admin-menu-toggle" type="button" :aria-label="t('admin.menu.open')" @click="menuOpen = !menuOpen">☰</button>
        <RouterLink to="/admin/products">EC-01</RouterLink>
        <span>{{ t('admin.layout.internal') }}</span>
      </div>
      <div class="admin-topbar__actions">
        <div class="admin-language-switcher">
          <select :value="locale.locale" :aria-label="t('admin.language')" @change="locale.setLocale($event.target.value)">
            <option v-for="option in localeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>
        <span class="admin-identity">{{ auth.displayName }}</span>
        <RouterLink class="admin-password-link" to="/admin/security">{{ t('admin.action.changePassword') }}</RouterLink>
        <RouterLink class="admin-shop-link" to="/">{{ t('admin.action.storefront') }}</RouterLink>
        <button type="button" @click="logout">{{ t('admin.action.logout') }}</button>
      </div>
    </header>

    <div class="admin-layout__body">
      <div v-if="menuOpen" class="admin-sidebar-backdrop" @click="closeMenu" />
      <aside class="admin-sidebar" :class="{ 'admin-sidebar--open': menuOpen }">
        <div class="admin-sidebar__intro">
          <span>{{ t('admin.layout.adminLabel') }}</span>
          <strong>{{ t('admin.layout.console') }}</strong>
          <p>{{ t('admin.layout.intro') }}</p>
        </div>
        <nav :aria-label="t('admin.layout.console')">
          <RouterLink v-for="item in navigation" :key="item.to" :to="item.to" @click="closeMenu">
            <i>{{ item.mark }}</i>
            <span><strong>{{ item.label }}</strong></span>
          </RouterLink>
        </nav>
        <footer>
          <span>{{ t('admin.layout.phase') }}</span>
          <p>{{ t('admin.layout.mode') }}</p>
        </footer>
      </aside>

      <main class="admin-main">
        <nav class="admin-breadcrumb" aria-label="Breadcrumb"><RouterLink to="/admin">{{ t('admin.layout.console') }}</RouterLink><span>/</span><strong>{{ breadcrumb }}</strong></nav>
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout { min-height: 100vh; background: #f7f5f1; }
.admin-topbar { position: sticky; z-index: 40; top: 0; display: flex; height: 68px; align-items: center; justify-content: space-between; padding: 0 28px; background: var(--white); border-bottom: 1px solid var(--line); }
.admin-topbar__brand, .admin-topbar__actions { display: flex; align-items: center; gap: 14px; }
.admin-topbar__brand a { color: var(--ink); font-size: 25px; font-weight: 750; letter-spacing: -.06em; text-decoration: none; }
.admin-topbar__brand span { padding-left: 14px; color: var(--muted); font-size: 9px; font-weight: 700; letter-spacing: .15em; border-left: 1px solid var(--line); }
.admin-topbar__actions { font-size: 10px; font-weight: 700; text-transform: uppercase; }
.admin-topbar__actions a, .admin-topbar__actions button { padding: 7px 3px; color: var(--ink); background: none; border: 0; text-decoration: none; cursor: pointer; }
.admin-identity { max-width: 160px; overflow: hidden; color: var(--muted); text-overflow: ellipsis; text-transform: none; white-space: nowrap; }
.admin-language-switcher { padding-right: 10px; border-right: 1px solid var(--line); }
.admin-language-switcher select { min-width: 92px; height: 32px; padding: 0 28px 0 10px; color: var(--ink); font: inherit; font-size: 10px; font-weight: 700; background: var(--white); border: 1px solid var(--line); border-radius: 2px; cursor: pointer; text-transform: none; }
.admin-layout__body { display: grid; min-height: calc(100vh - 68px); grid-template-columns: 218px minmax(0, 1fr); }
.admin-sidebar { position: sticky; top: 68px; display: flex; height: calc(100vh - 68px); flex-direction: column; padding: 30px 20px 22px; background: var(--ink); }
.admin-sidebar__intro { padding: 0 7px 28px; color: var(--white); border-bottom: 1px solid rgb(255 255 255 / 16%); }
.admin-sidebar__intro > span { color: #bab7b1; font-size: 8px; font-weight: 700; letter-spacing: .16em; }
.admin-sidebar__intro strong { display: block; margin-top: 8px; font-size: 18px; }
.admin-sidebar__intro p { margin: 8px 0 0; color: #aaa7a2; font-size: 10px; line-height: 1.55; }
.admin-sidebar nav { display: grid; gap: 5px; padding-top: 24px; }
.admin-sidebar nav a { display: flex; align-items: center; gap: 13px; padding: 12px 10px; color: #d3d0ca; text-decoration: none; border: 1px solid transparent; border-radius: 2px; }
.admin-sidebar nav a:hover, .admin-sidebar nav a.router-link-active { color: var(--white); background: rgb(255 255 255 / 8%); border-color: rgb(255 255 255 / 10%); }
.admin-sidebar nav i { display: grid; width: 27px; height: 27px; place-items: center; font-size: 9px; font-style: normal; font-weight: 800; border: 1px solid rgb(255 255 255 / 28%); border-radius: 50%; }
.admin-sidebar nav span { display: grid; gap: 2px; }
.admin-sidebar nav strong { font-size: 12px; }
.admin-sidebar nav small { color: #8f8c87; font-size: 8px; letter-spacing: .08em; text-transform: uppercase; }
.admin-sidebar footer { margin-top: auto; padding: 18px 7px 0; color: #77736f; border-top: 1px solid rgb(255 255 255 / 12%); }
.admin-sidebar footer span { font-size: 8px; font-weight: 700; letter-spacing: .12em; }
.admin-sidebar footer p { margin: 5px 0 0; font-size: 9px; }
.admin-main { min-width: 0; }
.admin-breadcrumb { display: flex; align-items: center; gap: 8px; padding: 18px clamp(20px, 3vw, 38px) 0; color: var(--muted); font-size: 10px; }.admin-breadcrumb a { color: var(--muted); text-decoration: none; }.admin-breadcrumb strong { color: var(--ink); }
.admin-menu-toggle { display: none; width: 34px; height: 34px; background: none; border: 1px solid var(--line); cursor: pointer; }
.admin-sidebar-backdrop { display: none; }
@media (max-width: 820px) {
  .admin-topbar { height: 60px; padding: 0 14px; }
  .admin-layout__body { min-height: calc(100vh - 60px); grid-template-columns: 1fr; }
  .admin-menu-toggle { display: block; }
  .admin-topbar__brand span, .admin-identity { display: none; }
  .admin-sidebar { position: fixed; z-index: 60; top: 60px; bottom: 0; left: 0; width: 218px; height: auto; transform: translateX(-100%); transition: transform 180ms ease; }
  .admin-sidebar--open { transform: translateX(0); }
  .admin-sidebar-backdrop { position: fixed; z-index: 50; display: block; inset: 60px 0 0; background: rgb(21 21 21 / 28%); }
}
@media (max-width: 440px) { .admin-shop-link { display: none; } }
@media (max-width: 620px) { .admin-password-link, .admin-identity { display: none; } .admin-language-switcher { padding-right: 3px; } .admin-language-switcher select { min-width: 78px; padding-left: 7px; } }
</style>
