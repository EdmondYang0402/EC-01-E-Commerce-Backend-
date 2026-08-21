<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminProductApi } from '../../services/adminProducts'
import { adminCategoryApi } from '../../services/categories'
import { errorMessage } from '../../services/http'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const categoryOptions = ref([])
const form = reactive({ name: '', subtitle: '', coverUrl: '', description: '', rootCategoryId: '', categoryId: '' })
const editing = computed(() => route.name === 'admin-product-edit')
const productId = computed(() => Number(route.params.productId))
const rootCategories = computed(() => categoryOptions.value.filter((item) =>
  item.parentId == null && item.status === 'ENABLED'))
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
  catch (requestError) { error.value = errorMessage(requestError, '商品信息加载失败') }
  finally { loading.value = false }
}

const changeRoot = () => { form.categoryId = '' }

const submit = async () => {
  if (!form.name.trim()) { ElMessage.warning('请输入商品名称'); return }
  if (!form.rootCategoryId || !form.categoryId) { ElMessage.warning('请选择一级分类和二级分类'); return }
  saving.value = true
  try {
    const payload = {
      name: form.name.trim(), subtitle: form.subtitle.trim() || null,
      coverUrl: form.coverUrl.trim() || null, description: form.description.trim() || null,
      categoryId: Number(form.categoryId),
    }
    if (editing.value) {
      await adminProductApi.update(productId.value, payload)
      ElMessage.success('商品基础信息已更新')
      await router.push(`/admin/products/${productId.value}`)
    } else {
      const id = await adminProductApi.create(payload)
      ElMessage.success('商品创建成功，默认保持下架状态')
      await router.push({ path: '/admin/products', query: { created: id } })
    }
  } catch (requestError) { ElMessage.error(errorMessage(requestError, editing.value ? '商品更新失败' : '商品创建失败')) }
  finally { saving.value = false }
}

onMounted(load)
watch(() => route.fullPath, load)
</script>

<template>
  <section class="admin-page">
    <RouterLink class="admin-back" :to="editing ? `/admin/products/${productId}` : '/admin/products'">← {{ editing ? '返回商品详情' : '返回商品列表' }}</RouterLink>
    <header class="admin-page__header">
      <div><p class="admin-eyebrow">PRODUCT EDITOR</p><h1>{{ editing ? '编辑商品' : '新增商品' }}</h1><p class="admin-page__subtitle">只维护 Product 基础资料；价格、库存与规格请在商品详情页管理。</p></div>
    </header>
    <div v-if="error" class="admin-error-banner">{{ error }}</div>
    <div v-if="loading" class="admin-state">正在读取商品信息…</div>
    <form v-else class="admin-form" @submit.prevent="submit">
      <div class="admin-form__grid">
        <label class="admin-field"><span>商品名称 *</span><input v-model="form.name" maxlength="120" required placeholder="例如：弧形休闲椅" /></label>
        <label class="admin-field"><span>一级分类 *</span><select v-model="form.rootCategoryId" required @change="changeRoot"><option value="" disabled>请选择一级分类</option><option v-for="root in rootCategories" :key="root.id" :value="root.id">{{ root.name }}</option></select></label>
        <label class="admin-field"><span>二级分类 *</span><select v-model="form.categoryId" :disabled="!form.rootCategoryId" required><option value="" disabled>请选择二级分类</option><option v-for="child in childCategories" :key="child.id" :value="child.id">{{ child.name }}{{ child.status === 'DISABLED' ? '（已禁用）' : '' }}</option></select></label>
        <label class="admin-field admin-field--full"><span>副标题</span><input v-model="form.subtitle" maxlength="255" placeholder="一句简洁的商品描述" /></label>
        <label class="admin-field admin-field--full"><span>封面图片 URL</span><input v-model="form.coverUrl" maxlength="500" type="url" placeholder="https://…" /></label>
        <label class="admin-field admin-field--full"><span>详细描述</span><textarea v-model="form.description" placeholder="材质、设计理念与使用场景" /></label>
      </div>
      <div class="admin-form__actions"><button class="admin-primary-button" type="submit" :disabled="saving">{{ saving ? '保存中…' : editing ? '保存修改' : '创建商品' }}</button><RouterLink class="admin-secondary-button" :to="editing ? `/admin/products/${productId}` : '/admin/products'">取消</RouterLink></div>
    </form>
  </section>
</template>
