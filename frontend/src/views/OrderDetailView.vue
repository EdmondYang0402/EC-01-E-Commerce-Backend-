<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import fallbackImage from '../assets/products/chair.png'
import { errorMessage } from '../services/http'
import { useLocaleStore } from '../stores/locale'
import { useOrderStore } from '../stores/orders'

const route = useRoute()
const orders = useOrderStore()
const locale = useLocaleStore()
const { detail, detailLoading } = storeToRefs(orders)
const t = (key, params) => locale.t(key, params)

const formatCurrency = (value) => new Intl.NumberFormat(
  locale.locale === 'zh' ? 'zh-CN' : locale.locale,
  { style: 'currency', currency: 'CNY' },
).format(Number(value || 0))

const formatDate = (value) => value
  ? new Intl.DateTimeFormat(locale.locale === 'zh' ? 'zh-CN' : locale.locale, {
      dateStyle: 'medium', timeStyle: 'short',
    }).format(new Date(value))
  : '—'

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
  try {
    await orders.fetchDetail(route.params.orderNo)
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderDetail.loadFailed')))
  }
}

onMounted(loadDetail)
watch(() => route.params.orderNo, loadDetail)
</script>

<template>
  <section class="order-detail page-shell">
    <RouterLink class="back" to="/orders">{{ t('orderDetail.back') }}</RouterLink>
    <p v-if="detailLoading" class="state">{{ t('orderDetail.loading') }}</p>
    <div v-else-if="detail" class="detail-content">
      <header>
        <div><p>{{ t('orderDetail.eyebrow') }}</p><h1>{{ detail.orderNo }}</h1></div>
        <div class="headline-meta">
          <span>{{ t(`order.status.${Number(detail.status)}`) }}</span>
          <strong>{{ formatCurrency(detail.totalAmount) }}</strong>
        </div>
      </header>

      <div class="meta-grid">
        <div><span>{{ t('orders.createdAt') }}</span><strong>{{ formatDate(detail.createTime) }}</strong></div>
        <div><span>{{ t('orderDetail.receiver') }}</span><strong>{{ detail.receiverName }}</strong></div>
        <div><span>{{ t('orderDetail.phone') }}</span><strong>{{ detail.receiverPhone }}</strong></div>
        <div><span>{{ t('orderDetail.address') }}</span><strong>{{ detail.receiverAddress }}</strong></div>
      </div>

      <section class="items">
        <h2>{{ t('orderDetail.items') }}</h2>
        <article v-for="item in detail.items || []" :key="item.id">
          <img :src="item.coverUrl || fallbackImage" :alt="item.productName" />
          <div><strong>{{ item.productName }}</strong><span>{{ formatSpec(item.skuSpec) }}</span></div>
          <span>{{ formatCurrency(item.price) }} × {{ item.quantity }}</span>
          <strong>{{ formatCurrency(item.subtotal) }}</strong>
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
.meta-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1px; margin: 28px 0; background: var(--line); border: 1px solid var(--line); }
.meta-grid div { display: grid; gap: 8px; padding: 18px; background: var(--white); }
.meta-grid span { color: var(--muted); font-size: 9px; text-transform: uppercase; }
.meta-grid strong { font-size: 12px; line-height: 1.5; }
.items { margin-top: 34px; }
.items h2 { margin: 0; padding-bottom: 14px; font-size: 14px; border-bottom: 1px solid var(--ink); }
.items article { display: grid; grid-template-columns: 90px minmax(0, 1fr) 160px 120px; gap: 18px; align-items: center; padding: 18px 0; border-bottom: 1px solid var(--line); font-size: 12px; }
.items img { width: 90px; height: 76px; object-fit: cover; background: var(--paper); }
.items article div { display: grid; gap: 7px; }
.items article div span, .items article > span { color: var(--muted); }
.items article > strong { text-align: right; }
.state { padding: 90px 0; color: var(--muted); text-align: center; }
@media (max-width: 760px) { .meta-grid { grid-template-columns: 1fr 1fr; } .items article { grid-template-columns: 70px 1fr; } .items img { width: 70px; height: 62px; } .items article > strong { text-align: left; } }
</style>
