<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminProductApi } from '../../services/adminProducts'
import { adminCategoryApi } from '../../services/categories'
import { errorMessage } from '../../services/http'
import { useLocaleStore } from '../../stores/locale'

const route = useRoute()
const router = useRouter()
const locale = useLocaleStore()
const t = (key, params) => locale.t(key, params)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const categoryOptions = ref([])
const form = reactive({ name: '', subtitle: '', coverUrl: '', description: '', rootCategoryId: '', categoryId: '' })
const editing = computed(() => route.name === 'admin-product-edit')
const productId = computed(() => Number(route.params.productId))
const rootCategories = computed(() => categoryOptions.value.filter((item) =>
  item.parentId == null
  && (item.status === 'ENABLED' || item.id === Number(form.rootCategoryId))))
const childCategories = computed(() => categoryOptions.value.filter((item) =>
  item.parentId === Number(form.rootCategoryId)
  && (item.status === 'ENABLED' || item.id === Number(form.categoryId))))

const fill = (product = {}) => {
  const child = categoryOptions.value.find((item) => item.id === product.categoryId)
  Object.assign(form, {
  name: product.name || '', subtitle: product.subtitle || '', coverUrl: product.coverUrl || '',
  description: product.description || '', rootCategoryId: child?.parentId || '',
  categoryId: product.categoryId || '',
  })
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    categoryOptions.value = await adminCategoryApi.getAll() || []
    fill(editing.value ? await adminProductApi.getDetail(productId.value) : {})
  }
  catch (requestError) { error.value = errorMessage(requestError, t('admin.productForm.loadFailed')) }
  finally { loading.value = false }
}

const changeRoot = () => { form.categoryId = '' }

const submit = async () => {
  if (!form.name.trim()) { ElMessage.warning(t('admin.productForm.nameRequired')); return }
  if (!form.rootCategoryId || !form.categoryId) { ElMessage.warning(t('admin.productForm.categoryRequired')); return }
  saving.value = true
  try {
    const payload = {
      name: form.name.trim(), subtitle: form.subtitle.trim() || null,
      coverUrl: form.coverUrl.trim() || null, description: form.description.trim() || null,
      categoryId: Number(form.categoryId),
    }
    if (editing.value) {
      await adminProductApi.update(productId.value, payload)
      ElMessage.success(t('admin.productForm.updated'))
      await router.push(`/admin/products/${productId.value}`)
    } else {
      const id = await adminProductApi.create(payload)
      ElMessage.success(t('admin.productForm.created'))
      await router.push({ path: '/admin/products', query: { created: id } })
    }
  } catch (requestError) { ElMessage.error(errorMessage(requestError, editing.value ? t('admin.productForm.updateFailed') : t('admin.productForm.createFailed'))) }
  finally { saving.value = false }
}

onMounted(load)
watch(() => route.fullPath, load)
</script>

<template>
  <section class="admin-page">
    <RouterLink class="admin-back" :to="editing ? `/admin/products/${productId}` : '/admin/products'">{{ editing ? t('admin.productForm.backDetail') : t('admin.productForm.backList') }}</RouterLink>
    <header class="admin-page__header">
      <div><p class="admin-eyebrow">{{ t('admin.productForm.eyebrow') }}</p><h1>{{ editing ? t('admin.productForm.editTitle') : t('admin.productForm.createTitle') }}</h1><p class="admin-page__subtitle">{{ t('admin.productForm.subtitle') }}</p></div>
    </header>
    <div v-if="error" class="admin-error-banner">{{ error }}</div>
    <div v-if="loading" class="admin-state">{{ t('admin.productForm.loading') }}</div>
    <form v-else class="admin-form" @submit.prevent="submit">
      <div class="admin-form__grid">
        <label class="admin-field"><span>{{ t('admin.productForm.name') }}</span><input v-model="form.name" maxlength="120" required :placeholder="t('admin.productForm.nameExample')" /></label>
        <label class="admin-field"><span>{{ t('admin.productForm.rootCategory') }}</span><select v-model="form.rootCategoryId" required @change="changeRoot"><option value="" disabled>{{ t('admin.productForm.selectRoot') }}</option><option v-for="root in rootCategories" :key="root.id" :value="root.id">{{ root.name }}</option></select></label>
        <label class="admin-field"><span>{{ t('admin.productForm.childCategory') }}</span><select v-model="form.categoryId" :disabled="!form.rootCategoryId" required><option value="" disabled>{{ t('admin.productForm.selectChild') }}</option><option v-for="child in childCategories" :key="child.id" :value="child.id">{{ child.name }}{{ child.status === 'DISABLED' ? t('admin.productForm.disabledSuffix') : '' }}</option></select></label>
        <label class="admin-field admin-field--full"><span>{{ t('admin.productForm.subtitleField') }}</span><input v-model="form.subtitle" maxlength="255" :placeholder="t('admin.productForm.subtitlePlaceholder')" /></label>
        <label class="admin-field admin-field--full"><span>{{ t('admin.productForm.coverUrl') }}</span><input v-model="form.coverUrl" maxlength="500" type="url" placeholder="https://…" /></label>
        <label class="admin-field admin-field--full"><span>{{ t('admin.productForm.description') }}</span><textarea v-model="form.description" :placeholder="t('admin.productForm.descriptionPlaceholder')" /></label>
      </div>
      <div class="admin-form__actions"><button class="admin-primary-button" type="submit" :disabled="saving">{{ saving ? t('admin.common.saving') : editing ? t('admin.productForm.saveChanges') : t('admin.productForm.create') }}</button><RouterLink class="admin-secondary-button" :to="editing ? `/admin/products/${productId}` : '/admin/products'">{{ t('admin.common.cancel') }}</RouterLink></div>
    </form>
  </section>
</template>
