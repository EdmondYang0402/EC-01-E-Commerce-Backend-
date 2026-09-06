<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import fallbackImage from '../assets/products/chair.png'
import OrderStatusBadge from '../components/common/OrderStatusBadge.vue'
import OrderTimeline from '../components/common/OrderTimeline.vue'
import PageState from '../components/common/PageState.vue'
import SafeImage from '../components/common/SafeImage.vue'
import { errorMessage } from '../services/http'
import { paymentApi } from '../services/payments'
import { useLocaleStore } from '../stores/locale'
import { useOrderStore } from '../stores/orders'
import { formatDateTime, formatMoney } from '../utils/formatters'
import { normalizeOrderStatus } from '../utils/orderStatus'

const route = useRoute()
const orders = useOrderStore()
const locale = useLocaleStore()
const { detail, detailLoading, cancellingOrderNo, receivingOrderId } = storeToRefs(orders)
const t = (key, params) => locale.t(key, params)
const paying = ref(false)
const loadError = ref('')

const formatSpec = (value) => {
  if (!value) return t('detail.standard')
  try {
    const spec = JSON.parse(value)
    return Object.entries(spec).map(([key, item]) => `${key}: ${item}`).join(' · ')
  } catch {
    return value
  }
}

const loadDetail = async () => {
  loadError.value = ''
  try {
    await orders.fetchDetail(route.params.orderNo)
  } catch (error) {
    loadError.value = errorMessage(error, t('orderDetail.loadFailed'))
  }
}

const copyOrderNo = async () => {
  try { await navigator.clipboard.writeText(detail.value.orderNo); ElMessage.success(t('orders.copySuccess')) }
  catch { ElMessage.error(t('orders.copyFailed')) }
}

const receiveOrder = async () => {
  try {
    await ElMessageBox.confirm(t('orders.receiveConfirm', { orderNo: detail.value.orderNo }), t('orders.receiveTitle'), {
      confirmButtonText: t('orders.receive'), cancelButtonText: t('orders.keepOrder'), type: 'warning',
    })
  } catch { return }
  try {
    await orders.confirmReceive(detail.value.id)
    ElMessage.success(t('orders.receiveSuccess'))
    await loadDetail()
  } catch (error) { ElMessage.error(errorMessage(error, t('orders.receiveFailed'))) }
}

const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm(
      t('orders.cancelConfirm', { orderNo: detail.value.orderNo }),
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
    await orders.cancelOrder(detail.value.orderNo)
    ElMessage.success(t('orders.cancelSuccess'))
    await loadDetail()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orders.cancelFailed')))
  }
}

const payOrder = async () => {
  const paymentWindow = window.open('', '_blank')
  paying.value = true
  try {
    const payment = await paymentApi.create(detail.value.orderNo)
    if (!paymentWindow) {
      ElMessage.warning(t('payment.popupBlocked'))
      return
    }
    paymentWindow.document.open()
    paymentWindow.document.write(payment.paymentForm)
    paymentWindow.document.close()
  } catch (error) {
    paymentWindow?.close()
    ElMessage.error(errorMessage(error, t('payment.createFailed')))
  } finally {
    paying.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.orderNo, loadDetail)
</script>

<template>
  <section class="order-detail page-shell">
    <RouterLink class="back" to="/orders">{{ t('orderDetail.back') }}</RouterLink>
    <PageState v-if="detailLoading" :title="t('orderDetail.loading')" />
    <PageState v-else-if="loadError" kind="error" :title="loadError" :action-label="t('common.retry')" @action="loadDetail" />
    <div v-else-if="detail" class="detail-content">
      <header>
        <div><p>{{ t('orderDetail.eyebrow') }}</p><h1>{{ detail.orderNo }}</h1><button class="copy-button" type="button" @click="copyOrderNo">{{ t('orders.copy') }}</button></div>
        <div class="headline-meta">
          <OrderStatusBadge :status="detail.status" />
          <strong>{{ formatMoney(detail.totalAmount) }}</strong>
          <button
            v-if="normalizeOrderStatus(detail.status) === 0"
            class="pay-button"
            type="button"
            :disabled="paying"
            @click="payOrder"
          >
            {{ paying ? t('payment.opening') : t('payment.payNow') }}
          </button>
          <button
            v-if="normalizeOrderStatus(detail.status) === 0"
            type="button"
            :disabled="cancellingOrderNo === detail.orderNo"
            @click="cancelOrder"
          >
            {{ cancellingOrderNo === detail.orderNo ? t('orders.cancelling') : t('orders.cancel') }}
          </button>
          <span v-if="normalizeOrderStatus(detail.status) === 1" class="waiting-note">{{ t('orders.waitingShip') }}</span>
          <button v-if="normalizeOrderStatus(detail.status) === 2" class="receive-button" type="button" :disabled="receivingOrderId === detail.id" @click="receiveOrder">{{ receivingOrderId === detail.id ? t('orders.receiving') : t('orders.receive') }}</button>
        </div>
      </header>

      <OrderTimeline :status="detail.status" />

      <div class="meta-grid">
        <div><span>{{ t('orders.createdAt') }}</span><strong>{{ formatDateTime(detail.createTime) }}</strong></div>
        <div><span>{{ t('orderDetail.receiver') }}</span><strong>{{ detail.receiverName }}</strong></div>
        <div><span>{{ t('orderDetail.phone') }}</span><strong>{{ detail.receiverPhone }}</strong></div>
        <div><span>{{ t('orderDetail.address') }}</span><strong>{{ detail.receiverAddress }}</strong></div>
      </div>

      <section class="items">
        <h2>{{ t('orderDetail.items') }}</h2>
        <article v-for="item in detail.items || []" :key="item.id">
          <SafeImage :src="item.coverUrl" :fallback="fallbackImage" :alt="item.productName" />
          <div><strong>{{ item.productName }}</strong><span>{{ formatSpec(item.skuSpec) }}</span></div>
          <span>{{ formatMoney(item.price) }} × {{ item.quantity }}</span>
          <strong>{{ formatMoney(item.subtotal) }}</strong>
        </article>
      </section>
    </div>
  </section>
</template>

<style scoped>
.order-detail { min-height: 68vh; padding-top: 34px; padding-bottom: 80px; }
.back { display: inline-block; margin-bottom: 24px; color: var(--muted); font-size: 12px; text-decoration: none; }
.detail-content > header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; padding-bottom: 24px; border-bottom: 1px solid var(--ink); }
.detail-content header p { margin: 0 0 8px; color: var(--red); font-size: 10px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
h1 { margin: 0; font-size: clamp(30px, 5vw, 58px); letter-spacing: -.045em; }
.headline-meta { display: grid; justify-items: end; gap: 8px; }
.headline-meta span { padding: 5px 9px; color: white; font-size: 10px; background: var(--blue); }
.headline-meta strong { font-size: 22px; }
.headline-meta button { padding: 9px 14px; color: var(--red); font: inherit; font-size: 11px; font-weight: 700; background: transparent; border: 1px solid var(--red); cursor: pointer; }
.headline-meta .pay-button { color: var(--white); background: var(--ink); border-color: var(--ink); }
.headline-meta button:disabled { cursor: wait; opacity: .55; }
.copy-button { margin-top: 8px; padding: 0; color: var(--muted); font: inherit; font-size: 10px; background: none; border: 0; cursor: pointer; text-decoration: underline; }.waiting-note { color: var(--muted); font-size: 11px; }.headline-meta .receive-button { color: var(--white); background: var(--ink); border-color: var(--ink); }
.meta-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1px; margin: 28px 0; background: var(--line); border: 1px solid var(--line); }
.meta-grid div { display: grid; gap: 8px; padding: 18px; background: var(--white); }
.meta-grid span { color: var(--muted); font-size: 9px; text-transform: uppercase; }
.meta-grid strong { font-size: 12px; line-height: 1.5; }
.items { margin-top: 34px; }
.items h2 { margin: 0; padding-bottom: 14px; font-size: 14px; border-bottom: 1px solid var(--ink); }
.items article { display: grid; grid-template-columns: 90px minmax(0, 1fr) 160px 120px; gap: 18px; align-items: center; padding: 18px 0; border-bottom: 1px solid var(--line); font-size: 12px; }
.items :deep(img) { width: 90px; height: 76px; object-fit: cover; background: var(--paper); }
.items article div { display: grid; gap: 7px; }
.items article div span, .items article > span { color: var(--muted); }
.items article > strong { text-align: right; }
.state { padding: 90px 0; color: var(--muted); text-align: center; }
@media (max-width: 760px) { .detail-content > header { align-items: flex-start; flex-direction: column; }.headline-meta { justify-items: start; }.meta-grid { grid-template-columns: 1fr 1fr; } .items article { grid-template-columns: 70px 1fr; } .items :deep(img) { width: 70px; height: 62px; } .items article > strong { text-align: left; } }
</style>
