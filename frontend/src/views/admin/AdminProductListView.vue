<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import fallbackImage from '../../assets/products/chair.png'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminPagination from '../../components/admin/AdminPagination.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminProductApi } from '../../services/adminProducts'
import { errorMessage } from '../../services/http'

const size = 20
const records = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const error = ref('')
const statusBusy = ref(false)
const statusTarget = ref(null)
const filters = reactive({ keyword: '', status: '', categoryId: '' })
const productLabels = { ON_SHELF: '已上架', OFF_SHELF: '已下架' }

const statusMessage = computed(() => {
  if (!statusTarget.value) return ''
  const action = statusTarget.value.nextStatus === 'ON_SHELF' ? '上架' : '下架'
  return `确认${action}商品“${statusTarget.value.product.name}”？后端将负责校验所有业务条件。`
})

const formatCurrency = (value) => value == null ? '—' : new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(value))
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const result = await adminProductApi.getPage({
      page: page.value,
      size,
      keyword: filters.keyword.trim() || undefined,
      status: filters.status || undefined,
      categoryId: filters.categoryId || undefined,
    })
    records.value = result?.records || []
    total.value = Number(result?.total || 0)
  } catch (requestError) {
    error.value = errorMessage(requestError, '商品列表加载失败')
  } finally {
    loading.value = false
  }
}

const search = () => { page.value = 1; load() }
const reset = () => {
  Object.assign(filters, { keyword: '', status: '', categoryId: '' })
  page.value = 1
  load()
}
const changePage = (nextPage) => { page.value = nextPage; load() }
const requestStatusChange = (product) => {
  statusTarget.value = { product, nextStatus: product.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF' }
}
const confirmStatusChange = async () => {
  statusBusy.value = true
  try {
    await adminProductApi.updateStatus(statusTarget.value.product.id, statusTarget.value.nextStatus)
    ElMessage.success('商品状态已更新')
    statusTarget.value = null
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, '商品状态更新失败'))
  } finally {
    statusBusy.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <p class="admin-eyebrow">CATALOG MANAGEMENT</p>
        <h1>商品管理</h1>
        <p class="admin-page__subtitle">管理商品基础资料、SKU 与销售状态。价格和库存始终归属于 SKU。</p>
      </div>
      <RouterLink class="admin-primary-button" to="/admin/products/create">＋ 新增商品</RouterLink>
    </header>

    <form class="admin-filter-card" @submit.prevent="search">
      <label class="admin-field"><span>关键词</span><input v-model="filters.keyword" maxlength="120" placeholder="商品名称" /></label>
      <label class="admin-field"><span>状态</span><select v-model="filters.status"><option value="">全部状态</option><option value="ON_SHELF">已上架</option><option value="OFF_SHELF">已下架</option></select></label>
      <label class="admin-field"><span>分类 ID</span><input v-model="filters.categoryId" min="1" type="number" placeholder="全部分类" /></label>
      <div class="admin-filter-actions"><button class="admin-primary-button" type="submit">查询</button><button class="admin-secondary-button" type="button" @click="reset">重置</button></div>
    </form>

    <div v-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">重新加载</button></div>
    <div v-if="loading" class="admin-state">正在加载商品…</div>
    <div v-else-if="!records.length" class="admin-state">暂无符合条件的商品</div>
    <template v-else>
      <div class="admin-table-wrap">
        <table class="admin-table product-table">
          <thead><tr><th>商品</th><th>分类</th><th>SKU</th><th>最低价格</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="product in records" :key="product.id">
              <td><div class="product-cell"><img :src="product.coverUrl || fallbackImage" :alt="product.name" /><div><strong>{{ product.name }}</strong><span>{{ product.subtitle || `商品 #${product.id}` }}</span></div></div></td>
              <td><strong>#{{ product.categoryId || '—' }}</strong></td>
              <td>{{ product.skuCount ?? '—' }}</td>
              <td class="admin-money">{{ formatCurrency(product.minPrice) }}</td>
              <td><AdminStatusBadge :status="product.status" :labels="productLabels" /></td>
              <td class="admin-muted">{{ formatDate(product.updateTime) }}</td>
              <td><div class="admin-actions"><RouterLink class="admin-text-button" :to="`/admin/products/${product.id}`">详情 / SKU</RouterLink><RouterLink class="admin-text-button" :to="`/admin/products/${product.id}/edit`">编辑</RouterLink><button class="admin-text-button" type="button" @click="requestStatusChange(product)">{{ product.status === 'ON_SHELF' ? '下架' : '上架' }}</button></div></td>
            </tr>
          </tbody>
        </table>
      </div>
      <AdminPagination :page="page" :size="size" :total="total" @change="changePage" />
    </template>

    <AdminConfirmDialog
      :open="Boolean(statusTarget)"
      title="商品状态确认"
      :message="statusMessage"
      :confirm-text="statusTarget?.nextStatus === 'ON_SHELF' ? '确认上架' : '确认下架'"
      :danger="statusTarget?.nextStatus === 'OFF_SHELF'"
      :busy="statusBusy"
      @cancel="statusTarget = null"
      @confirm="confirmStatusChange"
    />
  </section>
</template>

<style scoped>
.product-table { min-width: 1040px; }
.product-cell { display: grid; min-width: 270px; grid-template-columns: 62px minmax(0, 1fr); gap: 13px; align-items: center; }
.product-cell img { width: 62px; height: 56px; object-fit: cover; background: var(--paper); }
.product-cell div { display: grid; gap: 5px; }
.product-cell strong { font-size: 12px; }
.product-cell span { max-width: 260px; overflow: hidden; color: var(--muted); font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.admin-error-banner button { margin-left: 8px; }
</style>
