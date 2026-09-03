<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '../services/orders'
import { paymentApi } from '../services/payments'
import { useLocaleStore } from '../stores/locale'

const route = useRoute()
const locale = useLocaleStore()
const t = (key) => locale.t(key)
const orderNo = computed(() => String(route.query.orderNo || ''))
const state = ref('PROCESSING')
const checking = ref(false)
const message = ref('')
let timer
let attempts = 0

const checkStatus = async () => {
  if (!orderNo.value || checking.value) return
  checking.value = true
  try {
    const [order, payment] = await Promise.all([
      orderApi.getOrderDetail(orderNo.value),
      paymentApi.getByOrderNo(orderNo.value),
    ])
    if (Number(order.status) === 1 && Number(payment.status) === 1) {
      state.value = 'SUCCESS'
      return
    }
    if (Number(order.status) === 4 || [2, 3].includes(Number(payment.status))) {
      state.value = 'FAILED'
      return
    }
    state.value = 'PROCESSING'
    attempts += 1
    if (attempts < 15) timer = window.setTimeout(checkStatus, 2000)
  } catch (error) {
    message.value = error.response?.data?.message || error.message
    attempts += 1
    if (attempts < 15) timer = window.setTimeout(checkStatus, 2000)
  } finally {
    checking.value = false
  }
}

const refresh = () => {
  window.clearTimeout(timer)
  attempts = 0
  message.value = ''
  checkStatus()
}

onMounted(checkStatus)
onUnmounted(() => window.clearTimeout(timer))
</script>

<template>
  <section class="payment-result page-shell">
    <p class="eyebrow">ALIPAY SANDBOX</p>
    <div class="status-mark" :class="`is-${state.toLowerCase()}`">{{ state === 'SUCCESS' ? '✓' : state === 'FAILED' ? '×' : '…' }}</div>
    <h1>{{ t(`payment.result.${state.toLowerCase()}`) }}</h1>
    <p>{{ t(`payment.result.${state.toLowerCase()}Hint`) }}</p>
    <small v-if="message">{{ message }}</small>
    <div class="actions">
      <button v-if="state === 'PROCESSING'" type="button" :disabled="checking" @click="refresh">{{ t('payment.result.refresh') }}</button>
      <RouterLink :to="`/orders/${encodeURIComponent(orderNo)}`">{{ t('payment.result.order') }}</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.payment-result { min-height: 68vh; padding-top: 80px; padding-bottom: 100px; text-align: center; }
.eyebrow { color: var(--red); font-size: 10px; font-weight: 750; letter-spacing: .18em; }
.status-mark { display: grid; width: 72px; height: 72px; margin: 28px auto; place-items: center; color: var(--ink); font-size: 30px; border: 1px solid var(--ink); border-radius: 50%; }
.status-mark.is-success { color: var(--white); background: var(--blue); border-color: var(--blue); }
.status-mark.is-failed { color: var(--white); background: var(--red); border-color: var(--red); }
h1 { margin: 0; font-size: clamp(34px, 6vw, 64px); letter-spacing: -.045em; }
.payment-result > p:not(.eyebrow) { max-width: 520px; margin: 18px auto; color: var(--muted); line-height: 1.7; }
small { color: var(--red); }
.actions { display: flex; justify-content: center; gap: 10px; margin-top: 30px; }
.actions button, .actions a { padding: 11px 18px; color: var(--ink); font: inherit; font-size: 11px; font-weight: 700; background: transparent; border: 1px solid var(--ink); text-decoration: none; cursor: pointer; }
.actions button:disabled { cursor: wait; opacity: .55; }
</style>
