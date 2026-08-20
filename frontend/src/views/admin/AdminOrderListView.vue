<script setup>
import { onMounted, reactive, ref } from 'vue'
import AdminPagination from '../../components/admin/AdminPagination.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminOrderApi } from '../../services/adminOrders'
import { errorMessage } from '../../services/http'

const size = 20
const page = ref(1)
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const filters = reactive({ orderNo: '', status: '', userId: '' })
const statusLabels = { PENDING_PAYMENT: '待支付', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
const statuses = Object.entries(statusLabels)

const formatCurrency = (value) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(value || 0))
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const result = await adminOrderApi.getPage({ page: page.value, size, orderNo: filters.orderNo.trim() || undefined, status: filters.status || undefined, userId: filters.userId || undefined })
    records.value = result?.records || []
    total.value = Number(result?.total || 0)
  } catch (requestError) { error.value = errorMessage(requestError, '订单列表加载失败') }
  finally { loading.value = false }
}
const search = () => { page.value = 1; load() }
const reset = () => { Object.assign(filters, { orderNo: '', status: '', userId: '' }); page.value = 1; load() }
const changePage = (nextPage) => { page.value = nextPage; load() }
onMounted(load)
</script>

<template>
  <section class="admin-page">
    <header class="admin-page__header"><div><p class="admin-eyebrow">ORDER ARCHIVE</p><h1>订单管理</h1><p class="admin-page__subtitle">仅查询订单与购买快照，不提供发货、退款或状态修改操作。</p></div><span class="order-count">{{ total }} ORDERS</span></header>
    <form class="admin-filter-card" @submit.prevent="search">
      <label class="admin-field"><span>订单号</span><input v-model="filters.orderNo" maxlength="64" placeholder="搜索订单编号" /></label>
      <label class="admin-field"><span>订单状态</span><select v-model="filters.status"><option value="">全部状态</option><option v-for="([value, label]) in statuses" :key="value" :value="value">{{ label }}</option></select></label>
      <label class="admin-field"><span>用户 ID</span><input v-model="filters.userId" min="1" type="number" placeholder="全部用户" /></label>
      <div class="admin-filter-actions"><button class="admin-primary-button" type="submit">查询</button><button class="admin-secondary-button" type="button" @click="reset">重置</button></div>
    </form>
    <div v-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">重新加载</button></div>
    <div v-if="loading" class="admin-state">正在加载订单…</div>
    <div v-else-if="!records.length" class="admin-state">暂无符合条件的订单</div>
    <template v-else>
      <div class="admin-table-wrap"><table class="admin-table order-table"><thead><tr><th>订单编号</th><th>用户</th><th>金额</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead><tbody><tr v-for="order in records" :key="order.id"><td><strong>{{ order.orderNo }}</strong><span class="order-id">#{{ order.id }}</span></td><td>User #{{ order.userId }}</td><td class="admin-money">{{ formatCurrency(order.totalAmount) }}</td><td><AdminStatusBadge :status="order.status" :labels="statusLabels" /></td><td class="admin-muted">{{ formatDate(order.createTime) }}</td><td><RouterLink class="admin-text-button" :to="`/admin/orders/${order.orderNo}`">查看详情 →</RouterLink></td></tr></tbody></table></div>
      <AdminPagination :page="page" :size="size" :total="total" @change="changePage" />
    </template>
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
