<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { errorMessage } from '../services/http'
import { useAuthStore } from '../stores/auth'
import '../assets/styles/admin.css'

const router = useRouter()
const auth = useAuthStore()
const menuOpen = ref(false)

const navigation = [
  { label: '商品管理', caption: 'Products', to: '/admin/products', mark: 'P' },
  { label: '分类管理', caption: 'Categories', to: '/admin/categories', mark: 'C' },
  { label: '订单管理', caption: 'Orders', to: '/admin/orders', mark: 'O' },
  { label: '用户管理', caption: 'Users', to: '/admin/users', mark: 'U' },
]

const closeMenu = () => { menuOpen.value = false }
const logout = async () => {
  try {
    await auth.logout()
  } catch (error) {
    ElMessage.warning(errorMessage(error, '会话已在本地清除'))
  }
  await router.replace('/login')
}
</script>

<template>
  <div class="admin-layout">
    <header class="admin-topbar">
      <div class="admin-topbar__brand">
        <button class="admin-menu-toggle" type="button" aria-label="打开后台菜单" @click="menuOpen = !menuOpen">☰</button>
        <RouterLink to="/admin/products">EC-01</RouterLink>
        <span>INTERNAL</span>
      </div>
      <div class="admin-topbar__actions">
        <span class="admin-identity">{{ auth.displayName }}</span>
        <RouterLink to="/">返回商城</RouterLink>
        <button type="button" @click="logout">退出登录</button>
      </div>
    </header>

    <div class="admin-layout__body">
      <div v-if="menuOpen" class="admin-sidebar-backdrop" @click="closeMenu" />
      <aside class="admin-sidebar" :class="{ 'admin-sidebar--open': menuOpen }">
        <div class="admin-sidebar__intro">
          <span>ADMIN CONSOLE</span>
          <strong>管理工作台</strong>
          <p>保持商品、订单与用户信息清晰可控。</p>
        </div>
        <nav aria-label="后台管理导航">
          <RouterLink v-for="item in navigation" :key="item.to" :to="item.to" @click="closeMenu">
            <i>{{ item.mark }}</i>
            <span><strong>{{ item.label }}</strong><small>{{ item.caption }}</small></span>
          </RouterLink>
        </nav>
        <footer>
          <span>EC-01 / PHASE 2</span>
          <p>Internal management mode</p>
        </footer>
      </aside>

      <main class="admin-main"><RouterView /></main>
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
@media (max-width: 440px) { .admin-topbar__actions a { display: none; } }
</style>
