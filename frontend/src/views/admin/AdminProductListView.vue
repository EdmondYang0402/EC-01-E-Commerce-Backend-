<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import fallbackImage from '../../assets/products/chair.png'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminPagination from '../../components/admin/AdminPagination.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminProductApi } from '../../services/adminProducts'
import { errorMessage } from '../../services/http'
import { useLocaleStore } from '../../stores/locale'

const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)
const size = 20
const records = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const error = ref('')
const statusBusy = ref(false)
const statusTarget = ref(null)
const filters = reactive({ keyword: '', status: '', categoryId: '' })
const productLabels = computed(() => ({ ON_SHELF: t('admin.common.onShelf'), OFF_SHELF: t('admin.common.offShelf') }))

const statusMessage = computed(() => {
  if (!statusTarget.value) return ''
  const action = statusTarget.value.nextStatus === 'ON_SHELF' ? t('admin.common.putOnShelf') : t('admin.common.takeOffShelf')
  return t('admin.products.statusMessage', { action, name: statusTarget.value.product.name })
})

const formatCurrency = (value) => value == null ? '—' : new Intl.NumberFormat(locale.locale, { style: 'currency', currency: 'CNY' }).format(Number(value))
const formatDate = (value) => value ? new Intl.DateTimeFormat(locale.locale, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'

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
    error.value = errorMessage(requestError, t('admin.products.loadFailed'))
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
    ElMessage.success(t('admin.products.statusUpdated'))
    statusTarget.value = null
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, t('admin.products.statusUpdateFailed')))
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
        <p class="admin-eyebrow">{{ t('admin.products.eyebrow') }}</p>
        <h1>{{ t('admin.products.title') }}</h1>
        <p class="admin-page__subtitle">{{ t('admin.products.subtitle') }}</p>
      </div>
      <RouterLink class="admin-primary-button" to="/admin/products/create">{{ t('admin.products.add') }}</RouterLink>
    </header>

    <form class="admin-filter-card" @submit.prevent="search">
      <label class="admin-field"><span>{{ t('admin.products.keyword') }}</span><input v-model="filters.keyword" maxlength="120" :placeholder="t('admin.products.namePlaceholder')" /></label>
      <label class="admin-field"><span>{{ t('admin.common.status') }}</span><select v-model="filters.status"><option value="">{{ t('admin.common.allStatuses') }}</option><option value="ON_SHELF">{{ t('admin.common.onShelf') }}</option><option value="OFF_SHELF">{{ t('admin.common.offShelf') }}</option></select></label>
      <label class="admin-field"><span>{{ t('admin.products.categoryId') }}</span><input v-model="filters.categoryId" min="1" type="number" :placeholder="t('admin.products.allCategories')" /></label>
      <div class="admin-filter-actions"><button class="admin-primary-button" type="submit">{{ t('admin.common.search') }}</button><button class="admin-secondary-button" type="button" @click="reset">{{ t('admin.common.reset') }}</button></div>
    </form>

    <div v-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">{{ t('admin.common.reload') }}</button></div>
    <div v-if="loading" class="admin-state">{{ t('admin.products.loading') }}</div>
    <div v-else-if="!records.length" class="admin-state">{{ t('admin.products.empty') }}</div>
    <template v-else>
      <div class="admin-table-wrap">
        <table class="admin-table product-table">
          <thead><tr><th>{{ t('admin.products.product') }}</th><th>{{ t('admin.products.category') }}</th><th>SKU</th><th>{{ t('admin.products.minPrice') }}</th><th>{{ t('admin.common.status') }}</th><th>{{ t('admin.common.updatedAt') }}</th><th>{{ t('admin.common.actions') }}</th></tr></thead>
          <tbody>
            <tr v-for="product in records" :key="product.id">
              <td><div class="product-cell"><img :src="product.coverUrl || fallbackImage" :alt="product.name" /><div><strong>{{ product.name }}</strong><span>{{ product.subtitle || t('admin.products.fallbackName', { id: product.id }) }}</span></div></div></td>
              <td><strong>#{{ product.categoryId || '—' }}</strong></td>
              <td>{{ product.skuCount ?? '—' }}</td>
              <td class="admin-money">{{ formatCurrency(product.minPrice) }}</td>
              <td><AdminStatusBadge :status="product.status" :labels="productLabels" /></td>
              <td class="admin-muted">{{ formatDate(product.updateTime) }}</td>
              <td><div class="admin-actions"><RouterLink class="admin-text-button" :to="`/admin/products/${product.id}`">{{ t('admin.products.detailSku') }}</RouterLink><RouterLink class="admin-text-button" :to="`/admin/products/${product.id}/edit`">{{ t('admin.common.edit') }}</RouterLink><button class="admin-text-button" type="button" @click="requestStatusChange(product)">{{ product.status === 'ON_SHELF' ? t('admin.common.takeOffShelf') : t('admin.common.putOnShelf') }}</button></div></td>
            </tr>
          </tbody>
        </table>
      </div>
      <AdminPagination :page="page" :size="size" :total="total" @change="changePage" />
    </template>

    <AdminConfirmDialog
      :open="Boolean(statusTarget)"
      :title="t('admin.products.statusConfirm')"
      :message="statusMessage"
      :confirm-text="statusTarget?.nextStatus === 'ON_SHELF' ? t('admin.products.confirmOnShelf') : t('admin.products.confirmOffShelf')"
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
