<script setup>
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import fallbackImage from '../assets/products/chair.png'
import { errorMessage } from '../services/http'
import { useLocaleStore } from '../stores/locale'
import { useOrderStore } from '../stores/orders'

const orders = useOrderStore()
const locale = useLocaleStore()
const { records, total, listLoading } = storeToRefs(orders)
const page = ref(1)
const size = 10
const t = (key, params) => locale.t(key, params)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

const formatCurrency = (value) => new Intl.NumberFormat(
  locale.locale === 'zh' ? 'zh-CN' : locale.locale,
  { style: 'currency', currency: 'CNY' },
).format(Number(value || 0))

const formatDate = (value) => value
  ? new Intl.DateTimeFormat(locale.locale === 'zh' ? 'zh-CN' : locale.locale, {
      dateStyle: 'medium', timeStyle: 'short',
    }).format(new Date(value))
  : '—'

const statusText = (status) => t(`order.status.${Number(status)}`)

const formatSpec = (value) => {
  if (!value) return t('detail.standard')
  try {
    const spec = JSON.parse(value)
    return Object.entries(spec).map(([key, item]) => `${key}: ${item}`).join(' · ')
  } catch {
    return value
  }
}

const loadOrders = async () => {
  try {
    await orders.fetchPage({ page: page.value, size })
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orders.loadFailed')))
  }
}

const changePage = async (nextPage) => {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  page.value = nextPage
  await loadOrders()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(loadOrders)
</script>

<template>
  <section class="orders page-shell">
    <header class="orders__heading">
      <div><p>{{ t('orders.eyebrow') }}</p><h1>{{ t('orders.title') }}</h1></div>
      <span>{{ t('orders.count', { count: total }) }}</span>
    </header>

    <p v-if="listLoading && !records.length" class="state">{{ t('orders.loading') }}</p>
    <div v-else-if="!records.length" class="state empty">
      <p>{{ t('orders.empty') }}</p>
      <RouterLink to="/products">{{ t('orders.browse') }}</RouterLink>
    </div>
    <div v-else class="order-list">
      <article v-for="order in records" :key="order.id" class="order-card">
        <header>
          <div>
            <span>{{ t('orders.orderNo') }}</span>
            <RouterLink :to="`/orders/${order.orderNo}`">{{ order.orderNo }}</RouterLink>
          </div>
          <div><span>{{ t('orders.createdAt') }}</span><strong>{{ formatDate(order.createTime) }}</strong></div>
          <div><span>{{ t('orders.status') }}</span><strong>{{ statusText(order.status) }}</strong></div>
          <div><span>{{ t('orders.total') }}</span><strong>{{ formatCurrency(order.totalAmount) }}</strong></div>
        </header>
        <div class="order-items">
          <div v-for="item in order.items || []" :key="item.id" class="order-item">
            <img :src="item.coverUrl || fallbackImage" :alt="item.productName" />
            <div><strong>{{ item.productName }}</strong><span>{{ formatSpec(item.skuSpec) }}</span></div>
            <span>× {{ item.quantity }}</span>
            <strong>{{ formatCurrency(item.subtotal) }}</strong>
          </div>
        </div>
        <RouterLink class="detail-link" :to="`/orders/${order.orderNo}`">{{ t('orders.viewDetail') }}</RouterLink>
      </article>
    </div>

    <nav v-if="totalPages > 1" class="pagination" :aria-label="t('orders.pages')">
      <button type="button" :disabled="page === 1" @click="changePage(page - 1)">{{ t('catalog.previous') }}</button>
      <span>{{ t('catalog.pageOf', { page, total: totalPages }) }}</span>
      <button type="button" :disabled="page === totalPages" @click="changePage(page + 1)">{{ t('catalog.next') }}</button>
    </nav>
  </section>
</template>

<style scoped>
.orders { min-height: 68vh; padding-top: 48px; padding-bottom: 80px; }
.orders__heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 28px; padding-bottom: 22px; border-bottom: 1px solid var(--ink); }
.orders__heading p { margin: 0 0 8px; color: var(--red); font-size: 10px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
h1 { margin: 0; font-size: clamp(40px, 5vw, 68px); letter-spacing: -.055em; }
.orders__heading > span { color: var(--muted); font-size: 12px; }
.order-list { display: grid; gap: 22px; }
.order-card { padding: 22px; background: var(--white); border: 1px solid var(--line); }
.order-card > header { display: grid; grid-template-columns: 1.4fr 1.1fr .7fr .8fr; gap: 18px; padding-bottom: 17px; border-bottom: 1px solid var(--line); }
.order-card header div { display: grid; gap: 5px; }
.order-card header span { color: var(--muted); font-size: 9px; text-transform: uppercase; }
.order-card header strong, .order-card header a { color: var(--ink); font-size: 12px; font-weight: 700; text-decoration: none; }
.order-items { display: grid; }
.order-item { display: grid; grid-template-columns: 64px minmax(0, 1fr) 70px 100px; gap: 16px; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--line); font-size: 11px; }
.order-item img { width: 64px; height: 58px; object-fit: cover; background: var(--paper); }
.order-item div { display: grid; gap: 5px; }
.order-item div span { color: var(--muted); }
.order-item > strong { text-align: right; }
.detail-link { display: inline-block; margin-top: 16px; color: var(--ink); font-size: 11px; font-weight: 700; }
.state { padding: 80px 0; color: var(--muted); text-align: center; }
.empty a { display: inline-block; margin-top: 12px; padding: 10px 16px; color: white; background: var(--ink); text-decoration: none; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 18px; margin-top: 40px; font-size: 12px; }
.pagination button { padding: 9px 15px; background: var(--white); border: 1px solid var(--line); cursor: pointer; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 760px) { .order-card > header { grid-template-columns: 1fr 1fr; } .order-item { grid-template-columns: 54px 1fr auto; } .order-item img { width: 54px; } .order-item > strong { grid-column: 2 / -1; } }
</style>
