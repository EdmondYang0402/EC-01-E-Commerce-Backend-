<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminPagination from '../../components/admin/AdminPagination.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminUserApi } from '../../services/adminUsers'
import { errorMessage } from '../../services/http'
import { useLocaleStore } from '../../stores/locale'

const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)
const size = 20
const page = ref(1)
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const statusBusy = ref(false)
const statusTarget = ref(null)
const filters = reactive({ keyword: '', status: '' })
const statusLabels = computed(() => ({ NORMAL: t('admin.users.normal'), DISABLED: t('admin.common.disabled') }))
const confirmMessage = computed(() => {
  if (!statusTarget.value) return ''
  const action = statusTarget.value.nextStatus === 'NORMAL' ? t('admin.common.enable') : t('admin.common.disable')
  return t('admin.users.confirmMessage', { action, name: statusTarget.value.user.username })
})
const initials = (user) => (user.nickname || user.username || '?').trim().slice(0, 1).toUpperCase()
const formatDate = (value) => value ? new Intl.DateTimeFormat(locale.locale, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
const load = async () => {
  loading.value = true; error.value = ''
  try {
    const result = await adminUserApi.getPage({ page: page.value, size, keyword: filters.keyword.trim() || undefined, status: filters.status || undefined })
    records.value = result?.records || []; total.value = Number(result?.total || 0)
  } catch (requestError) { error.value = errorMessage(requestError, t('admin.users.loadFailed')) }
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
    ElMessage.success(t('admin.users.statusUpdated'))
    statusTarget.value = null
    await load()
  } catch (requestError) { ElMessage.error(errorMessage(requestError, t('admin.users.statusUpdateFailed'))) }
  finally { statusBusy.value = false }
}
onMounted(load)
</script>

<template>
  <section class="admin-page">
    <header class="admin-page__header"><div><p class="admin-eyebrow">{{ t('admin.users.eyebrow') }}</p><h1>{{ t('admin.users.title') }}</h1><p class="admin-page__subtitle">{{ t('admin.users.subtitle') }}</p></div><span class="user-count">{{ t('admin.users.count', { count: total }) }}</span></header>
    <form class="admin-filter-card user-filters" @submit.prevent="search"><label class="admin-field"><span>{{ t('admin.users.keyword') }}</span><input v-model="filters.keyword" maxlength="100" :placeholder="t('admin.users.keywordPlaceholder')" /></label><label class="admin-field"><span>{{ t('admin.users.userStatus') }}</span><select v-model="filters.status"><option value="">{{ t('admin.common.allStatuses') }}</option><option value="NORMAL">{{ t('admin.users.normal') }}</option><option value="DISABLED">{{ t('admin.common.disabled') }}</option></select></label><div class="admin-filter-actions"><button class="admin-primary-button" type="submit">{{ t('admin.common.search') }}</button><button class="admin-secondary-button" type="button" @click="reset">{{ t('admin.common.reset') }}</button></div></form>
    <div v-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">{{ t('admin.common.reload') }}</button></div>
    <div v-if="loading" class="admin-state">{{ t('admin.users.loading') }}</div>
    <div v-else-if="!records.length" class="admin-state">{{ t('admin.users.empty') }}</div>
    <template v-else><div class="admin-table-wrap"><table class="admin-table user-table"><thead><tr><th>{{ t('admin.users.user') }}</th><th>{{ t('admin.users.contact') }}</th><th>{{ t('admin.users.role') }}</th><th>{{ t('admin.common.status') }}</th><th>{{ t('admin.users.registeredAt') }}</th><th>{{ t('admin.common.updatedAt') }}</th><th>{{ t('admin.common.actions') }}</th></tr></thead><tbody><tr v-for="user in records" :key="user.id"><td><div class="user-cell"><img v-if="user.avatarUrl" :src="user.avatarUrl" :alt="user.username" /><span v-else>{{ initials(user) }}</span><div><strong>{{ user.username }}</strong><small>{{ user.nickname || t('admin.users.fallbackName', { id: user.id }) }}</small></div></div></td><td><div class="contact-cell"><span>{{ user.email || '—' }}</span><small>{{ user.phone || t('admin.users.notProvidedPhone') }}</small></div></td><td><strong>{{ user.role || 'USER' }}</strong></td><td><AdminStatusBadge :status="user.status" :labels="statusLabels" /></td><td class="admin-muted">{{ formatDate(user.createTime) }}</td><td class="admin-muted">{{ formatDate(user.updateTime) }}</td><td><button class="admin-text-button" type="button" @click="requestStatus(user)">{{ user.status === 'NORMAL' ? t('admin.users.disableUser') : t('admin.users.enableUser') }}</button></td></tr></tbody></table></div><AdminPagination :page="page" :size="size" :total="total" @change="changePage" /></template>
    <AdminConfirmDialog :open="Boolean(statusTarget)" :title="t('admin.users.statusConfirm')" :message="confirmMessage" :confirm-text="statusTarget?.nextStatus === 'DISABLED' ? t('admin.users.confirmDisable') : t('admin.users.confirmEnable')" :danger="statusTarget?.nextStatus === 'DISABLED'" :busy="statusBusy" @cancel="statusTarget = null" @confirm="confirmStatus" />
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
