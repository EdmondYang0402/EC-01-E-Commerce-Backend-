<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import fallbackImage from '../../assets/products/chair.png'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminProductApi } from '../../services/adminProducts'
import { errorMessage } from '../../services/http'

const route = useRoute()
const detail = ref(null)
const loading = ref(false)
const error = ref('')
const skuEditorOpen = ref(false)
const skuSaving = ref(false)
const editingSkuId = ref(null)
const confirmTarget = ref(null)
const statusBusy = ref(false)
const skuForm = reactive({ skuCode: '', specJson: '', price: '', stock: '' })
const productLabels = { ON_SHELF: '已上架', OFF_SHELF: '已下架' }
const skuLabels = { ENABLED: '已启用', DISABLED: '已禁用' }
const productId = computed(() => Number(route.params.productId))
const editingSku = computed(() => editingSkuId.value != null)
const confirmMessage = computed(() => {
  if (!confirmTarget.value) return ''
  if (confirmTarget.value.type === 'product') return `确认${confirmTarget.value.nextStatus === 'ON_SHELF' ? '上架' : '下架'}商品“${detail.value?.name}”？`
  return `确认${confirmTarget.value.nextStatus === 'ENABLED' ? '启用' : '禁用'} SKU“${confirmTarget.value.item.skuCode}”？`
})

const formatCurrency = (value) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(value || 0))
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
const formatSpec = (value) => {
  if (!value) return '标准规格'
  try { return Object.entries(JSON.parse(value)).map(([key, item]) => `${key}: ${item}`).join(' · ') }
  catch { return value }
}

const load = async () => {
  loading.value = true
  error.value = ''
  try { detail.value = await adminProductApi.getDetail(productId.value) }
  catch (requestError) { error.value = errorMessage(requestError, '商品详情加载失败'); detail.value = null }
  finally { loading.value = false }
}

const openCreateSku = () => {
  editingSkuId.value = null
  Object.assign(skuForm, { skuCode: '', specJson: '', price: '', stock: '' })
  skuEditorOpen.value = true
}
const openEditSku = (sku) => {
  editingSkuId.value = sku.id
  Object.assign(skuForm, { skuCode: sku.skuCode || '', specJson: sku.specJson || '', price: sku.price, stock: sku.stock })
  skuEditorOpen.value = true
}
const closeSkuEditor = () => { if (!skuSaving.value) skuEditorOpen.value = false }
const saveSku = async () => {
  if ((!editingSku.value && !skuForm.skuCode.trim()) || !skuForm.specJson.trim() || skuForm.price === '' || skuForm.stock === '') {
    ElMessage.warning('请完整填写 SKU 信息')
    return
  }
  if (Number(skuForm.price) < 0 || Number(skuForm.stock) < 0) { ElMessage.warning('价格和库存不能为负数'); return }
  skuSaving.value = true
  try {
    const payload = { specJson: skuForm.specJson.trim(), price: Number(skuForm.price), stock: Number(skuForm.stock) }
    if (editingSku.value) await adminProductApi.updateSku(editingSkuId.value, payload)
    else await adminProductApi.createSku(productId.value, { ...payload, skuCode: skuForm.skuCode.trim() })
    ElMessage.success(editingSku.value ? 'SKU 已更新' : 'SKU 已添加')
    skuEditorOpen.value = false
    await load()
  } catch (requestError) { ElMessage.error(errorMessage(requestError, 'SKU 保存失败')) }
  finally { skuSaving.value = false }
}

const requestProductStatus = () => {
  confirmTarget.value = { type: 'product', nextStatus: detail.value.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF' }
}
const requestSkuStatus = (sku) => {
  confirmTarget.value = { type: 'sku', item: sku, nextStatus: sku.status === 'ENABLED' ? 'DISABLED' : 'ENABLED' }
}
const confirmStatus = async () => {
  statusBusy.value = true
  try {
    if (confirmTarget.value.type === 'product') await adminProductApi.updateStatus(productId.value, confirmTarget.value.nextStatus)
    else await adminProductApi.updateSkuStatus(confirmTarget.value.item.id, confirmTarget.value.nextStatus)
    ElMessage.success('状态已更新')
    confirmTarget.value = null
    await load()
  } catch (requestError) { ElMessage.error(errorMessage(requestError, '状态更新失败')) }
  finally { statusBusy.value = false }
}

onMounted(load)
watch(() => route.params.productId, load)
</script>

<template>
  <section class="admin-page">
    <RouterLink class="admin-back" to="/admin/products">← 返回商品列表</RouterLink>
    <div v-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">重新加载</button></div>
    <div v-if="loading" class="admin-state">正在加载商品详情…</div>
    <div v-else-if="!detail" class="admin-state">无法显示该商品</div>
    <template v-else>
      <header class="detail-hero">
        <img :src="detail.coverUrl || fallbackImage" :alt="detail.name" />
        <div class="detail-hero__copy">
          <p class="admin-eyebrow">PRODUCT #{{ detail.id }}</p>
          <div class="detail-title"><h1>{{ detail.name }}</h1><AdminStatusBadge :status="detail.status" :labels="productLabels" /></div>
          <p class="subtitle">{{ detail.subtitle || '暂无副标题' }}</p>
          <p class="description">{{ detail.description || '暂无商品描述。' }}</p>
          <div class="detail-meta"><span>分类 #{{ detail.categoryId || '—' }}</span><span>创建于 {{ formatDate(detail.createTime) }}</span><span>更新于 {{ formatDate(detail.updateTime) }}</span></div>
          <div class="admin-actions"><RouterLink class="admin-primary-button" :to="`/admin/products/${detail.id}/edit`">编辑基础信息</RouterLink><button class="admin-secondary-button" type="button" @click="requestProductStatus">{{ detail.status === 'ON_SHELF' ? '下架商品' : '上架商品' }}</button></div>
        </div>
      </header>

      <section class="sku-section">
        <header><div><p class="admin-eyebrow">PURCHASABLE VARIANTS</p><h2>SKU 管理</h2><span>共 {{ detail.skus?.length || 0 }} 个规格</span></div><button class="admin-primary-button" type="button" @click="openCreateSku">＋ 新增 SKU</button></header>
        <div v-if="!detail.skus?.length" class="admin-state sku-empty">暂时没有 SKU，请先添加可购买规格。</div>
        <div v-else class="admin-table-wrap">
          <table class="admin-table sku-table">
            <thead><tr><th>SKU 编码</th><th>规格</th><th>价格</th><th>库存</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead>
            <tbody><tr v-for="sku in detail.skus" :key="sku.id"><td><strong>{{ sku.skuCode }}</strong></td><td>{{ formatSpec(sku.specJson) }}</td><td class="admin-money">{{ formatCurrency(sku.price) }}</td><td>{{ sku.stock }}</td><td><AdminStatusBadge :status="sku.status" :labels="skuLabels" /></td><td class="admin-muted">{{ formatDate(sku.updateTime) }}</td><td><div class="admin-actions"><button class="admin-text-button" type="button" @click="openEditSku(sku)">编辑</button><button class="admin-text-button" type="button" @click="requestSkuStatus(sku)">{{ sku.status === 'ENABLED' ? '禁用' : '启用' }}</button></div></td></tr></tbody>
          </table>
        </div>
      </section>
    </template>

    <Teleport to="body">
      <div v-if="skuEditorOpen" class="sku-editor-backdrop" @click.self="closeSkuEditor">
        <section class="sku-editor" role="dialog" aria-modal="true" aria-label="SKU 编辑器">
          <header><div><p class="admin-eyebrow">SKU EDITOR</p><h2>{{ editingSku ? '编辑 SKU' : '新增 SKU' }}</h2></div><button type="button" aria-label="关闭" @click="closeSkuEditor">×</button></header>
          <form @submit.prevent="saveSku">
            <label class="admin-field"><span>SKU 编码 *</span><input v-model="skuForm.skuCode" maxlength="64" :disabled="editingSku" required placeholder="CHAIR-BLACK" /></label>
            <label class="admin-field"><span>规格 JSON *</span><textarea v-model="skuForm.specJson" maxlength="500" required placeholder='{"颜色":"黑色","尺寸":"标准"}' /></label>
            <div class="sku-editor__grid"><label class="admin-field"><span>价格 *</span><input v-model="skuForm.price" min="0" step="0.01" type="number" required /></label><label class="admin-field"><span>库存 *</span><input v-model="skuForm.stock" min="0" step="1" type="number" required /></label></div>
            <p>状态由独立操作管理；编辑基础信息不会修改 SKU 状态。</p>
            <footer><button class="admin-secondary-button" type="button" @click="closeSkuEditor">取消</button><button class="admin-primary-button" type="submit" :disabled="skuSaving">{{ skuSaving ? '保存中…' : '保存 SKU' }}</button></footer>
          </form>
        </section>
      </div>
    </Teleport>

    <AdminConfirmDialog :open="Boolean(confirmTarget)" title="状态操作确认" :message="confirmMessage" :confirm-text="confirmTarget?.nextStatus === 'OFF_SHELF' || confirmTarget?.nextStatus === 'DISABLED' ? '确认停用' : '确认启用'" :danger="confirmTarget?.nextStatus === 'OFF_SHELF' || confirmTarget?.nextStatus === 'DISABLED'" :busy="statusBusy" @cancel="confirmTarget = null" @confirm="confirmStatus" />
  </section>
</template>

<style scoped>
.admin-error-banner button { margin-left: 8px; }
.detail-hero { display: grid; grid-template-columns: minmax(280px, .85fr) minmax(360px, 1.15fr); gap: clamp(30px, 5vw, 72px); padding: clamp(22px, 3vw, 40px); background: var(--white); border: 1px solid var(--line); }
.detail-hero > img { width: 100%; aspect-ratio: 1.15; object-fit: cover; background: var(--paper); }
.detail-hero__copy { align-self: center; }
.detail-title { display: flex; align-items: center; justify-content: space-between; gap: 18px; }
.detail-title h1 { font-size: clamp(34px, 4vw, 56px); }
.subtitle { margin: 12px 0; color: var(--muted); font-size: 14px; }
.description { margin: 24px 0; font-size: 12px; line-height: 1.8; white-space: pre-wrap; }
.detail-meta { display: flex; flex-wrap: wrap; gap: 8px 18px; margin-bottom: 24px; padding-top: 17px; color: var(--muted); font-size: 9px; border-top: 1px solid var(--line); }
.sku-section { margin-top: 32px; }
.sku-section > header { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; margin-bottom: 16px; }
.sku-section h2 { display: inline; margin: 0; font-size: 26px; letter-spacing: -.04em; }
.sku-section header span { margin-left: 12px; color: var(--muted); font-size: 10px; }
.sku-empty { min-height: 180px; }
.sku-table { min-width: 930px; }
.sku-editor-backdrop { position: fixed; z-index: 100; display: flex; justify-content: flex-end; inset: 0; background: rgb(21 21 21 / 32%); }
.sku-editor { width: min(520px, 100%); height: 100%; padding: 30px; overflow-y: auto; background: var(--white); box-shadow: -18px 0 60px rgb(21 21 21 / 12%); }
.sku-editor > header { display: flex; align-items: flex-start; justify-content: space-between; padding-bottom: 20px; border-bottom: 1px solid var(--ink); }
.sku-editor h2 { margin: 0; font-size: 34px; letter-spacing: -.045em; }
.sku-editor header button { width: 34px; height: 34px; font-size: 24px; background: var(--white); border: 1px solid var(--line); cursor: pointer; }
.sku-editor form { display: grid; gap: 18px; padding-top: 24px; }
.sku-editor__grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.sku-editor form > p { margin: 0; color: var(--muted); font-size: 10px; line-height: 1.6; }
.sku-editor footer { display: flex; justify-content: flex-end; gap: 9px; padding-top: 8px; }
@media (max-width: 760px) { .detail-hero { grid-template-columns: 1fr; } .detail-hero > img { max-height: 320px; } }
@media (max-width: 480px) { .detail-title { align-items: flex-start; flex-direction: column; } .sku-editor { padding: 22px; } }
</style>
