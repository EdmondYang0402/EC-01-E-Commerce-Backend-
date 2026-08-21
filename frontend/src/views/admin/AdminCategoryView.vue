<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminConfirmDialog from '../../components/admin/AdminConfirmDialog.vue'
import AdminStatusBadge from '../../components/admin/AdminStatusBadge.vue'
import { adminCategoryApi } from '../../services/categories'
import { errorMessage } from '../../services/http'

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const editorOpen = ref(false)
const editorMode = ref('create')
const statusTarget = ref(null)
const form = reactive({ id: null, parentId: null, name: '', sortOrder: 0, status: 'ENABLED' })

const roots = computed(() => categories.value
  .filter((item) => item.parentId == null)
  .map((root) => ({
    ...root,
    children: categories.value.filter((item) => item.parentId === root.id),
  })))

const load = async () => {
  loading.value = true
  error.value = ''
  try { categories.value = await adminCategoryApi.getAll() || [] }
  catch (requestError) { error.value = errorMessage(requestError, '分类数据加载失败') }
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
  if (!form.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  saving.value = true
  try {
    const base = { name: form.name.trim(), sortOrder: Number(form.sortOrder) }
    if (editorMode.value === 'create') {
      await adminCategoryApi.create({ ...base, parentId: form.parentId, status: form.status })
      ElMessage.success(form.parentId ? '二级分类已创建' : '一级分类已创建')
    } else {
      await adminCategoryApi.update(form.id, base)
      ElMessage.success('分类信息已更新')
    }
    editorOpen.value = false
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, '分类保存失败'))
  } finally { saving.value = false }
}

const confirmStatus = async () => {
  const category = statusTarget.value
  if (!category) return
  saving.value = true
  try {
    const status = category.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    await adminCategoryApi.updateStatus(category.id, status)
    ElMessage.success(status === 'ENABLED' ? '分类已启用' : '分类已禁用')
    statusTarget.value = null
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, '分类状态更新失败'))
  } finally { saving.value = false }
}

onMounted(load)
</script>

<template>
  <section class="admin-page">
    <header class="admin-page__header">
      <div>
        <p class="admin-eyebrow">CATEGORY STRUCTURE</p>
        <h1>分类管理</h1>
        <p class="admin-page__subtitle">维护固定两级分类结构。商品只绑定二级分类，禁用一级分类后用户侧会隐藏整组分类。</p>
      </div>
      <button class="admin-primary-button" type="button" @click="openCreate()">＋ 新增一级分类</button>
    </header>

    <div v-if="error" class="admin-error-banner">{{ error }}</div>
    <div v-if="loading" class="admin-state">正在读取分类结构…</div>
    <div v-else-if="!roots.length" class="admin-state">暂无分类，请先创建一级分类。</div>
    <div v-else class="category-groups">
      <article v-for="root in roots" :key="root.id" class="category-group">
        <header class="category-row category-row--root">
          <div class="category-row__identity"><i>01</i><div><strong>{{ root.name }}</strong><span>#{{ root.id }} · 排序 {{ root.sortOrder }}</span></div></div>
          <AdminStatusBadge :status="root.status" />
          <div class="admin-actions">
            <button class="admin-text-button" type="button" @click="openCreate(root.id)">新增子分类</button>
            <button class="admin-text-button" type="button" @click="openEdit(root)">编辑</button>
            <button class="admin-text-button" type="button" @click="statusTarget = root">{{ root.status === 'ENABLED' ? '禁用' : '启用' }}</button>
          </div>
        </header>
        <div v-if="root.children.length" class="category-children">
          <div v-for="child in root.children" :key="child.id" class="category-row">
            <div class="category-row__identity"><i>02</i><div><strong>{{ child.name }}</strong><span>#{{ child.id }} · 排序 {{ child.sortOrder }}</span></div></div>
            <AdminStatusBadge :status="child.status" />
            <div class="admin-actions">
              <button class="admin-text-button" type="button" @click="openEdit(child)">编辑</button>
              <button class="admin-text-button" type="button" @click="statusTarget = child">{{ child.status === 'ENABLED' ? '禁用' : '启用' }}</button>
            </div>
          </div>
        </div>
        <p v-else class="category-empty">尚未创建二级分类</p>
      </article>
    </div>

    <Teleport to="body">
      <div v-if="editorOpen" class="category-editor-backdrop" @click.self="closeEditor">
        <form class="category-editor" @submit.prevent="save">
          <p>EC-01 ADMIN</p>
          <h2>{{ editorMode === 'create' ? (form.parentId ? '新增二级分类' : '新增一级分类') : '编辑分类' }}</h2>
          <label class="admin-field"><span>分类名称 *</span><input v-model="form.name" maxlength="80" required /></label>
          <label class="admin-field"><span>排序 *</span><input v-model.number="form.sortOrder" min="0" max="9999" required type="number" /></label>
          <label v-if="editorMode === 'create'" class="admin-field"><span>初始状态</span><select v-model="form.status"><option value="ENABLED">启用</option><option value="DISABLED">禁用</option></select></label>
          <footer><button class="admin-secondary-button" type="button" :disabled="saving" @click="closeEditor">取消</button><button class="admin-primary-button" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存分类' }}</button></footer>
        </form>
      </div>
    </Teleport>

    <AdminConfirmDialog
      :open="Boolean(statusTarget)"
      :busy="saving"
      :danger="statusTarget?.status === 'ENABLED'"
      :title="statusTarget?.status === 'ENABLED' ? '禁用分类' : '启用分类'"
      :message="statusTarget?.parentId == null && statusTarget?.status === 'ENABLED' ? '禁用一级分类后，用户侧将同时隐藏其全部子分类。Admin 仍可重新启用。' : `确认${statusTarget?.status === 'ENABLED' ? '禁用' : '启用'}“${statusTarget?.name || ''}”吗？`"
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
