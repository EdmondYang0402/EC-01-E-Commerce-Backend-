<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminCategoryApi } from '../../services/categories'
import { errorMessage } from '../../services/http'
import { useLocaleStore } from '../../stores/locale'

const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)
const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const editorOpen = ref(false)
const editorMode = ref('create')
const statusTarget = ref(null)
const form = reactive({ id: null, parentId: null, name: '', sortOrder: 0, status: 'ENABLED' })
const statusLabels = computed(() => ({ ENABLED: t('admin.common.enabled'), DISABLED: t('admin.common.disabled') }))

const roots = computed(() => categories.value
  .filter((item) => item.parentId == null)
  .map((root) => ({
    ...root,
    children: categories.value.filter((item) => item.parentId === root.id),
  })))

const load = async () => {
  loading.value = true
  error.value = ''
  categories.value = []
  try { categories.value = await adminCategoryApi.getAll() || [] }
  catch (requestError) { error.value = errorMessage(requestError, t('admin.categories.loadFailed')) }
  finally { loading.value = false }
}

const openCreate = (parentId = null) => {
  Object.assign(form, { id: null, parentId, name: '', sortOrder: 0, status: 'ENABLED' })
  editorMode.value = 'create'
  editorOpen.value = true
}

const openEdit = (category) => {
  Object.assign(form, {
    id: category.id,
    parentId: category.parentId,
    name: category.name,
    sortOrder: category.sortOrder,
    status: category.status,
  })
  editorMode.value = 'edit'
  editorOpen.value = true
}

const closeEditor = () => {
  if (!saving.value) editorOpen.value = false
}

const save = async () => {
  if (!form.name.trim()) { ElMessage.warning(t('admin.categories.nameRequired')); return }
  saving.value = true
  try {
    const base = { name: form.name.trim(), sortOrder: Number(form.sortOrder) }
    if (editorMode.value === 'create') {
      await adminCategoryApi.create({ ...base, parentId: form.parentId, status: form.status })
      ElMessage.success(form.parentId ? t('admin.categories.childCreated') : t('admin.categories.rootCreated'))
    } else {
      await adminCategoryApi.update(form.id, base)
      ElMessage.success(t('admin.categories.updated'))
    }
    editorOpen.value = false
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, t('admin.categories.saveFailed')))
  } finally { saving.value = false }
}

const confirmStatus = async () => {
  const category = statusTarget.value
  if (!category) return
  saving.value = true
  try {
    const status = category.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    await adminCategoryApi.updateStatus(category.id, status)
    ElMessage.success(status === 'ENABLED' ? t('admin.categories.enabled') : t('admin.categories.disabled'))
    statusTarget.value = null
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, t('admin.categories.statusFailed')))
  } finally { saving.value = false }
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <p class="admin-eyebrow">{{ t('admin.categories.eyebrow') }}</p>
        <h1>{{ t('admin.categories.title') }}</h1>
        <p class="admin-page__subtitle">{{ t('admin.categories.subtitle') }}</p>
      </div>
      <button class="admin-primary-button" type="button" @click="openCreate()">{{ t('admin.categories.addRoot') }}</button>
    </header>

    <div v-if="loading" class="admin-state">{{ t('admin.categories.loading') }}</div>
    <div v-else-if="error" class="admin-error-banner">{{ error }} <button class="admin-text-button" type="button" @click="load">{{ t('admin.common.reload') }}</button></div>
    <div v-else-if="!roots.length" class="admin-state">{{ t('admin.categories.empty') }}</div>
    <div v-else class="category-groups">
      <article v-for="root in roots" :key="root.id" class="category-group">
        <header class="category-row category-row--root">
          <div class="category-row__identity"><i>01</i><div><strong>{{ root.name }}</strong><span>#{{ root.id }} · {{ t('admin.categories.sort', { sort: root.sortOrder }) }}</span></div></div>
          <AdminStatusBadge :status="root.status" :labels="statusLabels" />
          <div class="admin-actions">
            <button class="admin-text-button" type="button" @click="openCreate(root.id)">{{ t('admin.categories.addChild') }}</button>
            <button class="admin-text-button" type="button" @click="openEdit(root)">{{ t('admin.common.edit') }}</button>
            <button class="admin-text-button" type="button" @click="statusTarget = root">{{ root.status === 'ENABLED' ? t('admin.common.disable') : t('admin.common.enable') }}</button>
          </div>
        </header>
        <div v-if="root.children.length" class="category-children">
          <div v-for="child in root.children" :key="child.id" class="category-row">
            <div class="category-row__identity"><i>02</i><div><strong>{{ child.name }}</strong><span>#{{ child.id }} · {{ t('admin.categories.sort', { sort: child.sortOrder }) }}</span></div></div>
            <AdminStatusBadge :status="child.status" :labels="statusLabels" />
            <div class="admin-actions">
              <button class="admin-text-button" type="button" @click="openEdit(child)">{{ t('admin.common.edit') }}</button>
              <button class="admin-text-button" type="button" @click="statusTarget = child">{{ child.status === 'ENABLED' ? t('admin.common.disable') : t('admin.common.enable') }}</button>
            </div>
          </div>
        </div>
        <p v-else class="category-empty">{{ t('admin.categories.noChildren') }}</p>
      </article>
    </div>

    <Teleport to="body">
      <div v-if="editorOpen" class="category-editor-backdrop" @click.self="closeEditor">
        <form class="category-editor" @submit.prevent="save">
          <p>{{ t('admin.layout.adminLabel') }}</p>
          <h2>{{ editorMode === 'create' ? (form.parentId ? t('admin.categories.createChild') : t('admin.categories.createRoot')) : t('admin.categories.edit') }}</h2>
          <label class="admin-field"><span>{{ t('admin.categories.name') }}</span><input v-model="form.name" maxlength="80" required /></label>
          <label class="admin-field"><span>{{ t('admin.categories.sortField') }}</span><input v-model.number="form.sortOrder" min="0" max="9999" required type="number" /></label>
          <label v-if="editorMode === 'create'" class="admin-field"><span>{{ t('admin.categories.initialStatus') }}</span><select v-model="form.status"><option value="ENABLED">{{ t('admin.common.enable') }}</option><option value="DISABLED">{{ t('admin.common.disable') }}</option></select></label>
          <footer><button class="admin-secondary-button" type="button" :disabled="saving" @click="closeEditor">{{ t('admin.common.cancel') }}</button><button class="admin-primary-button" type="submit" :disabled="saving">{{ saving ? t('admin.common.saving') : t('admin.categories.save') }}</button></footer>
        </form>
      </div>
    </Teleport>

    <AdminConfirmDialog
      :open="Boolean(statusTarget)"
      :busy="saving"
      :danger="statusTarget?.status === 'ENABLED'"
      :title="statusTarget?.status === 'ENABLED' ? t('admin.categories.disableTitle') : t('admin.categories.enableTitle')"
      :message="statusTarget?.parentId == null && statusTarget?.status === 'ENABLED' ? t('admin.categories.disableRootWarning') : t('admin.categories.statusMessage', { action: statusTarget?.status === 'ENABLED' ? t('admin.common.disable') : t('admin.common.enable'), name: statusTarget?.name || '' })"
      @cancel="statusTarget = null"
      @confirm="confirmStatus"
    />
  </section>
</template>

<style scoped>
.category-groups { display: grid; gap: 16px; }
.category-group { background: var(--white); border: 1px solid var(--line); }
.category-row { display: grid; grid-template-columns: minmax(240px, 1fr) 100px auto; gap: 18px; align-items: center; min-height: 66px; padding: 12px 18px 12px 58px; border-top: 1px solid var(--line); }
.category-row--root { min-height: 78px; padding-left: 18px; background: #faf9f7; border-top: 0; }
.category-row__identity { display: flex; align-items: center; gap: 13px; }
.category-row__identity i { display: grid; width: 29px; height: 29px; place-items: center; color: var(--muted); font-size: 8px; font-style: normal; border: 1px solid var(--line); border-radius: 50%; }
.category-row__identity strong, .category-row__identity span { display: block; }
.category-row__identity strong { font-size: 13px; }
.category-row__identity span { margin-top: 4px; color: var(--muted); font-size: 9px; }
.category-empty { margin: 0; padding: 18px 58px; color: var(--muted); font-size: 10px; border-top: 1px solid var(--line); }
.category-editor-backdrop { position: fixed; z-index: 100; display: grid; inset: 0; padding: 20px; place-items: center; background: rgb(21 21 21 / 38%); }
.category-editor { display: grid; width: min(460px, 100%); gap: 18px; padding: 30px; background: var(--white); border: 1px solid var(--ink); }
.category-editor > p { margin: 0; color: var(--red); font-size: 9px; font-weight: 750; letter-spacing: .15em; }
.category-editor h2 { margin: -9px 0 5px; font-size: 28px; letter-spacing: -.04em; }
.category-editor footer { display: flex; justify-content: flex-end; gap: 9px; padding-top: 5px; }
@media (max-width: 700px) { .category-row { grid-template-columns: 1fr; padding-left: 18px; } }
</style>
