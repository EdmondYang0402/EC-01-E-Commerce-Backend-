<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminPagination from '../../components/admin/AdminPagination.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminUserApi } from '../../services/adminUsers'
import { errorMessage } from '../../services/http'

const size = 20
const page = ref(1)
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const statusBusy = ref(false)
const statusTarget = ref(null)
const filters = reactive({ keyword: '', status: '' })
const statusLabels = { NORMAL: '正常', DISABLED: '已禁用' }
const confirmMessage = computed(() => {
  if (!statusTarget.value) return ''
  return `确认${statusTarget.value.nextStatus === 'NORMAL' ? '启用' : '禁用'}用户“${statusTarget.value.user.username}”？本操作只修改数据库状态，不会强制清除现有会话。`
})
const initials = (user) => (user.nickname || user.username || '?').trim().slice(0, 1).toUpperCase()
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
const load = async () => {
  loading.value = true; error.value = ''
  try {
    const result = await adminUserApi.getPage({ page: page.value, size, keyword: filters.keyword.trim() || undefined, status: filters.status || undefined })
    records.value = result?.records || []; total.value = Number(result?.total || 0)
  } catch (requestError) { error.value = errorMessage(requestError, '用户列表加载失败') }
  finally { loading.value = false }
}
const search = () => { page.value = 1; load() }
const reset = () => { Object.assign(filters, { keyword: '', status: '' }); page.value = 1; load() }
const changePage = (nextPage) => { page.value = nextPage; load() }
const requestStatus = (user) => { statusTarget.value = { user, nextStatus: user.status === 'NORMAL' ? 'DISABLED' : 'NORMAL' } }
const confirmStatus = async () => {
  statusBusy.value = true
  try {
    await adminUserApi.updateStatus(statusTarget.value.user.id, statusTarget.value.nextStatus)
    ElMessage.success('用户状态已更新')
    statusTarget.value = null
    await load()
  } catch (requestError) { ElMessage.error(errorMessage(requestError, '用户状态更新失败')) }
  finally { statusBusy.value = false }
}
onMounted(load)
</script>

<template>
  <section class="admin-page">
    <header class="admin-page__header"><div><p class="admin-eyebrow">CUSTOMER DIRECTORY</p><h1>用户管理</h1><p class="admin-page__subtitle">查询商城用户并管理可用状态。密码、令牌和会话不会在此页面显示或修改。</p></div><span class="user-count">{{ total }} USERS</span></header>
    <form class="admin-filter-card user-filters" @submit.prevent="search"><label class="admin-field"><span>关键词</span><input v-model="filters.keyword" maxlength="100" placeholder="用户名、昵称或手机号" /></label><label class="admin-field"><span>用户状态</span><select v-model="filters.status"><option value="">全部状态</option><option value="NORMAL">正常</option><option value="DISABLED">已禁用</option></select></label><div class="admin-filter-actions"><button class="admin-primary-button" type="submit">查询</button><button class="admin-secondary-button" type="button" @click="reset">重置</button></div></form>
    <div v-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">重新加载</button></div>
    <div v-if="loading" class="admin-state">正在加载用户…</div>
    <div v-else-if="!records.length" class="admin-state">暂无符合条件的用户</div>
    <template v-else><div class="admin-table-wrap"><table class="admin-table user-table"><thead><tr><th>用户</th><th>联系方式</th><th>状态</th><th>注册时间</th><th>最近更新</th><th>操作</th></tr></thead><tbody><tr v-for="user in records" :key="user.id"><td><div class="user-cell"><img v-if="user.avatarUrl" :src="user.avatarUrl" :alt="user.username" /><span v-else>{{ initials(user) }}</span><div><strong>{{ user.username }}</strong><small>{{ user.nickname || `User #${user.id}` }}</small></div></div></td><td><div class="contact-cell"><span>{{ user.email || '—' }}</span><small>{{ user.phone || '未填写手机号' }}</small></div></td><td><AdminStatusBadge :status="user.status" :labels="statusLabels" /></td><td class="admin-muted">{{ formatDate(user.createTime) }}</td><td class="admin-muted">{{ formatDate(user.updateTime) }}</td><td><button class="admin-text-button" type="button" @click="requestStatus(user)">{{ user.status === 'NORMAL' ? '禁用用户' : '启用用户' }}</button></td></tr></tbody></table></div><AdminPagination :page="page" :size="size" :total="total" @change="changePage" /></template>
    <AdminConfirmDialog :open="Boolean(statusTarget)" title="用户状态确认" :message="confirmMessage" :confirm-text="statusTarget?.nextStatus === 'DISABLED' ? '确认禁用' : '确认启用'" :danger="statusTarget?.nextStatus === 'DISABLED'" :busy="statusBusy" @cancel="statusTarget = null" @confirm="confirmStatus" />
  </section>
</template>

<style scoped>
.user-filters { grid-template-columns: minmax(220px, 1.6fr) minmax(150px, .7fr) auto; }
.user-count { color: var(--muted); font-size: 9px; font-weight: 750; letter-spacing: .12em; }
.user-table { min-width: 980px; }
.user-cell { display: grid; min-width: 210px; grid-template-columns: 42px minmax(0, 1fr); gap: 11px; align-items: center; }
.user-cell > img, .user-cell > span { display: grid; width: 42px; height: 42px; object-fit: cover; place-items: center; color: var(--white); font-size: 13px; font-weight: 750; background: var(--ink); border-radius: 50%; }
.user-cell div, .contact-cell { display: grid; gap: 4px; }
.user-cell small, .contact-cell small { color: var(--muted); font-size: 9px; }
.contact-cell span { font-size: 10px; }
.admin-error-banner button { margin-left: 8px; }
@media (max-width: 900px) { .user-filters { grid-template-columns: 1fr 1fr; } }
@media (max-width: 620px) { .user-filters { grid-template-columns: 1fr; } }
</style>
