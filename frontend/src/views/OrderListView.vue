<script setup>
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import fallbackImage from '../assets/products/chair.png'
import OrderStatusBadge from '../components/common/OrderStatusBadge.vue'
import PageState from '../components/common/PageState.vue'
import SafeImage from '../components/common/SafeImage.vue'
import { errorMessage } from '../services/http'
import { useLocaleStore } from '../stores/locale'
import { useOrderStore } from '../stores/orders'
import { formatDateTime, formatMoney } from '../utils/formatters'
import { normalizeOrderStatus } from '../utils/orderStatus'

const orders = useOrderStore()
const locale = useLocaleStore()
const { records, total, listLoading, cancellingOrderNo, receivingOrderId } = storeToRefs(orders)
const page = ref(1)
const loadError = ref('')
const size = 10
const t = (key, params) => locale.t(key, params)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

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
  loadError.value = ''
  try {
    await orders.fetchPage({ page: page.value, size })
  } catch (error) {
    loadError.value = errorMessage(error, t('orders.loadFailed'))
  }
}

const copyOrderNo = async (orderNo) => {
  try {
    await navigator.clipboard.writeText(orderNo)
    ElMessage.success(t('orders.copySuccess'))
  } catch {
    ElMessage.error(t('orders.copyFailed'))
  }
}

const receiveOrder = async (order) => {
  try {
    await ElMessageBox.confirm(t('orders.receiveConfirm', { orderNo: order.orderNo }), t('orders.receiveTitle'), {
      confirmButtonText: t('orders.receive'), cancelButtonText: t('orders.keepOrder'), type: 'warning',
    })
  } catch { return }
  try {
    await orders.confirmReceive(order.id)
    ElMessage.success(t('orders.receiveSuccess'))
    await loadOrders()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orders.receiveFailed')))
  }
}

const changePage = async (nextPage) => {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  page.value = nextPage
  await loadOrders()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      t('orders.cancelConfirm', { orderNo: order.orderNo }),
      t('orders.cancelTitle'),
      {
        confirmButtonText: t('orders.confirmCancel'),
        cancelButtonText: t('orders.keepOrder'),
        type: 'warning',
      },
    )
  } catch {
    return
  }

  try {
    await orders.cancelOrder(order.orderNo)
    ElMessage.success(t('orders.cancelSuccess'))
    await loadOrders()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orders.cancelFailed')))
  }
}

onMounted(loadOrders)
</script>

<template>
  <section class="orders page-shell">
    <header class="orders__heading">
      <div><p>{{ t('orders.eyebrow') }}</p><h1>{{ t('orders.title') }}</h1></div>
      <span>{{ t('orders.count', { count: total }) }}</span>
    </header>

    <PageState v-if="listLoading" :title="t('orders.loading')" />
    <PageState v-else-if="loadError" kind="error" :title="loadError" :action-label="t('common.retry')" @action="loadOrders" />
    <PageState v-else-if="!records.length" kind="empty" :title="t('orders.empty')" :action-label="t('orders.browse')" @action="$router.push('/products')" />
    <div v-else class="order-list">
      <article v-for="order in records" :key="order.id" class="order-card">
        <header>
          <div>
            <span>{{ t('orders.orderNo') }}</span>
            <span class="order-number"><RouterLink :to="`/orders/${order.orderNo}`">{{ order.orderNo }}</RouterLink><button type="button" @click="copyOrderNo(order.orderNo)">{{ t('orders.copy') }}</button></span>
          </div>
          <div><span>{{ t('orders.createdAt') }}</span><strong>{{ formatDateTime(order.createTime) }}</strong></div>
          <div><span>{{ t('orders.status') }}</span><OrderStatusBadge :status="order.status" /></div>
          <div><span>{{ t('orders.total') }}</span><strong>{{ formatMoney(order.totalAmount) }}</strong></div>
        </header>
        <div class="order-items">
          <div v-for="item in order.items || []" :key="item.id" class="order-item">
            <SafeImage :src="item.coverUrl" :fallback="fallbackImage" :alt="item.productName" />
            <div><strong>{{ item.productName }}</strong><span>{{ formatSpec(item.skuSpec) }}</span></div>
            <span>× {{ item.quantity }}</span>
            <strong>{{ formatMoney(item.subtotal) }}</strong>
          </div>
        </div>
        <footer class="order-actions">
          <RouterLink class="detail-link" :to="`/orders/${order.orderNo}`">{{ t('orders.viewDetail') }}</RouterLink>
          <button
            v-if="normalizeOrderStatus(order.status) === 0"
            class="cancel-button"
            type="button"
            :disabled="cancellingOrderNo === order.orderNo"
            @click="cancelOrder(order)"
          >
            {{ cancellingOrderNo === order.orderNo ? t('orders.cancelling') : t('orders.cancel') }}
          </button>
          <span v-if="normalizeOrderStatus(order.status) === 1" class="waiting-note">{{ t('orders.waitingShip') }}</span>
          <button v-if="normalizeOrderStatus(order.status) === 2" class="receive-button" type="button" :disabled="receivingOrderId === order.id" @click="receiveOrder(order)">{{ receivingOrderId === order.id ? t('orders.receiving') : t('orders.receive') }}</button>
        </footer>
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
.order-number { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }.order-number button { padding: 0; color: var(--muted); font: inherit; font-size: 9px; background: none; border: 0; cursor: pointer; text-decoration: underline; }
.order-items { display: grid; }
.order-item { display: grid; grid-template-columns: 64px minmax(0, 1fr) 70px 100px; gap: 16px; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--line); font-size: 11px; }
.order-item :deep(img) { width: 64px; height: 58px; object-fit: cover; background: var(--paper); }
.order-item div { display: grid; gap: 5px; }
.order-item div span { color: var(--muted); }
.order-item > strong { text-align: right; }
.order-actions { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-top: 16px; }
.detail-link { color: var(--ink); font-size: 11px; font-weight: 700; }
.cancel-button { padding: 9px 14px; color: var(--red); font: inherit; font-size: 11px; font-weight: 700; background: transparent; border: 1px solid var(--red); cursor: pointer; }
.cancel-button:disabled { cursor: wait; opacity: .55; }
.receive-button { padding: 9px 14px; color: var(--white); font: inherit; font-size: 11px; font-weight: 700; background: var(--ink); border: 1px solid var(--ink); cursor: pointer; }.receive-button:disabled { cursor: wait; opacity: .55; }.waiting-note { margin-left: auto; color: var(--muted); font-size: 11px; }
.state { padding: 80px 0; color: var(--muted); text-align: center; }
.empty a { display: inline-block; margin-top: 12px; padding: 10px 16px; color: white; background: var(--ink); text-decoration: none; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 18px; margin-top: 40px; font-size: 12px; }
.pagination button { padding: 9px 15px; background: var(--white); border: 1px solid var(--line); cursor: pointer; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 760px) { .orders__heading { align-items: flex-start; flex-direction: column; }.order-card > header { grid-template-columns: 1fr 1fr; } .order-item { grid-template-columns: 54px 1fr auto; } .order-item :deep(img) { width: 54px; } .order-item > strong { grid-column: 2 / -1; } }
</style>
