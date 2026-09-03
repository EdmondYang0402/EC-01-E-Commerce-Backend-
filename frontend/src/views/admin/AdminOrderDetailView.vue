<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import fallbackImage from '../../assets/products/chair.png'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminOrderApi } from '../../services/adminOrders'
import { errorMessage } from '../../services/http'
import { useLocaleStore } from '../../stores/locale'

const route = useRoute()
const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)
const detail = ref(null)
const loading = ref(false)
const error = ref('')
const statusLabels = computed(() => ({
  PENDING_PAYMENT: t('admin.orders.pending'), PAID: t('admin.orders.paid'), SHIPPED: t('admin.orders.shipped'), COMPLETED: t('admin.orders.completed'), CANCELLED: t('admin.orders.cancelled'),
  0: t('admin.orders.pending'), 1: t('admin.orders.paid'), 2: t('admin.orders.shipped'), 3: t('admin.orders.completed'), 4: t('admin.orders.cancelled'),
}))
const formatCurrency = (value) => new Intl.NumberFormat(locale.locale, { style: 'currency', currency: 'CNY' }).format(Number(value || 0))
const formatDate = (value) => value ? new Intl.DateTimeFormat(locale.locale, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
const formatSpec = (value) => {
  if (!value) return t('admin.orderDetail.standardSpec')
  try { return Object.entries(JSON.parse(value)).map(([key, item]) => `${key}: ${item}`).join(' · ') }
  catch { return value }
}
const load = async () => {
  loading.value = true; error.value = ''
  try { detail.value = await adminOrderApi.getDetail(route.params.orderNo) }
  catch (requestError) { error.value = errorMessage(requestError, t('admin.orderDetail.loadFailed')); detail.value = null }
  finally { loading.value = false }
}
onMounted(load)
watch(() => route.params.orderNo, load)
</script>

<template>
  <section class="admin-page">
    <RouterLink class="admin-back" to="/admin/orders">{{ t('admin.orderDetail.back') }}</RouterLink>
    <div v-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">{{ t('admin.common.reload') }}</button></div>
    <div v-if="loading" class="admin-state">{{ t('admin.orderDetail.loading') }}</div>
    <div v-else-if="!detail" class="admin-state">{{ t('admin.orderDetail.unavailable') }}</div>
    <template v-else>
      <header class="order-hero"><div><p class="admin-eyebrow">{{ t('admin.orders.eyebrow') }}</p><h1>{{ detail.orderNo }}</h1><p>{{ t('admin.orderDetail.created', { userId: detail.userId, date: formatDate(detail.createTime) }) }}</p></div><div><AdminStatusBadge :status="detail.status" :labels="statusLabels" /><strong>{{ formatCurrency(detail.totalAmount) }}</strong></div></header>
      <section class="receiver-panel admin-panel"><header><span>{{ t('admin.orderDetail.delivery') }}</span><h2>{{ t('admin.orderDetail.delivery') }}</h2></header><div><article><span>{{ t('admin.orderDetail.receiver') }}</span><strong>{{ detail.receiverName || '—' }}</strong></article><article><span>{{ t('admin.orderDetail.phone') }}</span><strong>{{ detail.receiverPhone || '—' }}</strong></article><article><span>{{ t('admin.orderDetail.address') }}</span><strong>{{ detail.receiverAddress || '—' }}</strong></article></div></section>
      <section class="snapshot-section"><header><div><p class="admin-eyebrow">{{ t('admin.orderDetail.items') }}</p><h2>{{ t('admin.orderDetail.items') }}</h2></div><span>{{ t('admin.orderDetail.itemCount', { count: detail.items?.length || 0 }) }}</span></header><div v-if="!detail.items?.length" class="admin-state">{{ t('admin.orderDetail.noItems') }}</div><div v-else class="snapshot-list"><article v-for="item in detail.items" :key="item.id"><img :src="item.coverUrl || fallbackImage" :alt="item.productName" /><div class="snapshot-copy"><strong>{{ item.productName }}</strong><span>{{ formatSpec(item.skuSpec) }}</span><small>{{ t('admin.common.productId', { id: item.productId }) }} / {{ t('admin.common.skuId', { id: item.skuId }) }}</small></div><span>{{ formatCurrency(item.price) }} × {{ item.quantity }}</span><strong>{{ formatCurrency(item.subtotal) }}</strong></article></div></section>
    </template>
  </section>
</template>

<style scoped>
.admin-error-banner button { margin-left: 8px; }
.order-hero { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; padding-bottom: 26px; border-bottom: 1px solid var(--ink); }
.order-hero h1 { font-size: clamp(28px, 4vw, 50px); word-break: break-all; }
.order-hero p:last-child { margin: 10px 0 0; color: var(--muted); font-size: 10px; }
.order-hero > div:last-child { display: grid; justify-items: end; gap: 12px; }
.order-hero > div:last-child strong { font-size: 25px; letter-spacing: -.03em; }
.receiver-panel { margin-top: 26px; padding: 22px; }
.receiver-panel header { margin-bottom: 16px; }
.receiver-panel header span { color: var(--red); font-size: 8px; font-weight: 750; letter-spacing: .14em; }
.receiver-panel h2 { margin: 5px 0 0; font-size: 21px; }
.receiver-panel > div { display: grid; grid-template-columns: .8fr 1fr 2fr; gap: 1px; background: var(--line); border: 1px solid var(--line); }
.receiver-panel article { display: grid; gap: 7px; padding: 16px; background: var(--white); }
.receiver-panel article span { color: var(--muted); font-size: 8px; text-transform: uppercase; }
.receiver-panel article strong { font-size: 11px; line-height: 1.55; }
.snapshot-section { margin-top: 32px; }
.snapshot-section > header { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 14px; }
.snapshot-section h2 { margin: 0; font-size: 26px; }
.snapshot-section header > span { color: var(--muted); font-size: 9px; font-weight: 700; letter-spacing: .1em; }
.snapshot-list { background: var(--white); border: 1px solid var(--line); }
.snapshot-list article { display: grid; grid-template-columns: 78px minmax(220px, 1fr) 150px 120px; gap: 17px; align-items: center; padding: 15px; font-size: 11px; border-bottom: 1px solid var(--line); }
.snapshot-list article:last-child { border-bottom: 0; }
.snapshot-list img { width: 78px; height: 68px; object-fit: cover; background: var(--paper); }
.snapshot-copy { display: grid; gap: 5px; }
.snapshot-copy span, .snapshot-copy small, .snapshot-list article > span { color: var(--muted); }
.snapshot-list article > strong { text-align: right; }
@media (max-width: 720px) { .receiver-panel > div { grid-template-columns: 1fr; } .snapshot-list article { grid-template-columns: 62px 1fr; } .snapshot-list img { width: 62px; height: 58px; } .snapshot-list article > span, .snapshot-list article > strong { grid-column: 2; text-align: left; } }
@media (max-width: 520px) { .order-hero { align-items: flex-start; flex-direction: column; } .order-hero > div:last-child { justify-items: start; } }
</style>
