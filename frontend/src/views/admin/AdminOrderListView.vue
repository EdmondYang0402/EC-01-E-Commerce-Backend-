<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminPagination from '../../components/admin/AdminPagination.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminOrderApi } from '../../services/adminOrders'
import { errorMessage } from '../../services/http'
import { useLocaleStore } from '../../stores/locale'
import { formatDateTime, formatMoney } from '../../utils/formatters'

const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)
const size = 20
const page = ref(1)
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const shipTarget = ref(null)
const shipping = ref(false)
const filters = reactive({ orderNo: '', status: '', userId: '' })
const statusLabels = computed(() => ({
  PENDING_PAYMENT: t('admin.orders.pending'), PAID: t('admin.orders.paid'), SHIPPED: t('admin.orders.shipped'), COMPLETED: t('admin.orders.completed'), CANCELLED: t('admin.orders.cancelled'),
  0: t('admin.orders.pending'), 1: t('admin.orders.paid'), 2: t('admin.orders.shipped'), 3: t('admin.orders.completed'), 4: t('admin.orders.cancelled'),
}))
const statuses = computed(() => ['PENDING_PAYMENT', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED'].map((value) => [value, statusLabels.value[value]]))
const isPaid = (status) => status === 'PAID' || Number(status) === 1

const load = async () => {
  loading.value = true
  error.value = ''
  records.value = []
  total.value = 0
  try {
    const result = await adminOrderApi.getPage({ page: page.value, size, orderNo: filters.orderNo.trim() || undefined, status: filters.status || undefined, userId: filters.userId || undefined })
    records.value = result?.records || []
    total.value = Number(result?.total || 0)
  } catch (requestError) { error.value = errorMessage(requestError, t('admin.orders.loadFailed')) }
  finally { loading.value = false }
}
const search = () => { page.value = 1; load() }
const reset = () => { Object.assign(filters, { orderNo: '', status: '', userId: '' }); page.value = 1; load() }
const changePage = (nextPage) => { page.value = nextPage; load() }
const confirmShip = async () => {
  const order = shipTarget.value
  if (!order || !isPaid(order.status)) return
  shipping.value = true
  try {
    await adminOrderApi.shipOrder(order.id)
    ElMessage.success(t('admin.orders.shipSuccess'))
    shipTarget.value = null
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, t('admin.orders.shipFailed')))
  } finally {
    shipping.value = false
  }
}
onMounted(load)
</script>

<template>
  <section class="admin-page">
    <header class="admin-page__header"><div><p class="admin-eyebrow">{{ t('admin.orders.eyebrow') }}</p><h1>{{ t('admin.orders.title') }}</h1><p class="admin-page__subtitle">{{ t('admin.orders.subtitle') }}</p></div><span class="order-count">{{ t('admin.orders.count', { count: total }) }}</span></header>
    <form class="admin-filter-card" @submit.prevent="search">
      <label class="admin-field"><span>{{ t('admin.orders.orderNo') }}</span><input v-model="filters.orderNo" maxlength="64" :placeholder="t('admin.orders.searchOrderNo')" /></label>
      <label class="admin-field"><span>{{ t('admin.orders.orderStatus') }}</span><select v-model="filters.status"><option value="">{{ t('admin.common.allStatuses') }}</option><option v-for="([value, label]) in statuses" :key="value" :value="value">{{ label }}</option></select></label>
      <label class="admin-field"><span>{{ t('admin.orders.userId') }}</span><input v-model="filters.userId" min="1" type="number" :placeholder="t('admin.orders.allUsers')" /></label>
      <div class="admin-filter-actions"><button class="admin-primary-button" type="submit">{{ t('admin.common.search') }}</button><button class="admin-secondary-button" type="button" @click="reset">{{ t('admin.common.reset') }}</button></div>
    </form>
    <div v-if="loading" class="admin-state">{{ t('admin.orders.loading') }}</div>
    <div v-else-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">{{ t('admin.common.reload') }}</button></div>
    <div v-else-if="!records.length" class="admin-state">{{ t('admin.orders.empty') }}</div>
    <template v-else>
      <div class="admin-table-wrap"><table class="admin-table order-table"><thead><tr><th>{{ t('admin.orders.orderNumber') }}</th><th>{{ t('admin.orders.user') }}</th><th>{{ t('admin.orders.amount') }}</th><th>{{ t('admin.common.status') }}</th><th>{{ t('admin.orders.createdAt') }}</th><th>{{ t('admin.common.actions') }}</th></tr></thead><tbody><tr v-for="order in records" :key="order.id"><td><strong>{{ order.orderNo }}</strong><span class="order-id">#{{ order.id }}</span></td><td>{{ t('admin.common.userId', { id: order.userId }) }}</td><td class="admin-money">{{ formatMoney(order.totalAmount) }}</td><td><AdminStatusBadge :status="order.status" :labels="statusLabels" /></td><td class="admin-muted">{{ formatDateTime(order.createTime) }}</td><td><div class="admin-actions"><RouterLink class="admin-text-button" :to="`/admin/orders/${order.orderNo}`">{{ t('admin.common.details') }} →</RouterLink><button v-if="isPaid(order.status)" class="admin-text-button" type="button" @click="shipTarget = order">{{ t('admin.common.ship') }}</button></div></td></tr></tbody></table></div>
      <AdminPagination :page="page" :size="size" :total="total" @change="changePage" />
    </template>
    <AdminConfirmDialog
      :open="Boolean(shipTarget)"
      :title="t('admin.orders.shipTitle')"
      :message="t('admin.orders.shipConfirm', { orderNo: shipTarget?.orderNo || '' })"
      :confirm-text="t('admin.common.ship')"
      :busy="shipping"
      @cancel="shipTarget = null"
      @confirm="confirmShip"
    />
  </section>
</template>

<style scoped>
.order-count { color: var(--muted); font-size: 9px; font-weight: 750; letter-spacing: .12em; }
.order-table { min-width: 850px; }
.order-table td:first-child { min-width: 190px; }
.order-table td:first-child strong, .order-table td:first-child span { display: block; }
.order-id { margin-top: 4px; color: var(--muted); font-size: 8px; }
.admin-error-banner button { margin-left: 8px; }
</style>
