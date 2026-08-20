<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import fallbackImage from '../assets/products/chair.png'
import { errorMessage } from '../services/http'
import { useProductStore } from '../stores/products'
import { useLocaleStore } from '../stores/locale'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const products = useProductStore()
const locale = useLocaleStore()
const auth = useAuthStore()
const cart = useCartStore()
const { detail, detailLoading } = storeToRefs(products)
const t = (key, params) => locale.t(key, params)
const selectedSkuId = ref(null)
const quantity = ref(1)
const adding = ref(false)
const selectedSku = computed(() => detail.value?.skus?.find((sku) => sku.id === selectedSkuId.value) || null)
const imageUrl = computed(() => detail.value?.coverUrl || fallbackImage)

const formatPrice = (value) => new Intl.NumberFormat(
  locale.locale === 'zh' ? 'zh-CN' : locale.locale,
  { style: 'currency', currency: 'CNY' },
).format(Number(value || 0))

const formatSpec = (specJson) => {
  if (!specJson) return t('detail.standard')
  try {
    const spec = JSON.parse(specJson)
    return Object.entries(spec).map(([key, value]) => `${key}: ${value}`).join(' · ')
  } catch {
    return specJson
  }
}

const loadProduct = async () => {
  try {
    const product = await products.fetchDetail(route.params.id)
    selectedSkuId.value = null
  } catch (error) {
    ElMessage.error(errorMessage(error, t('message.detailFailed')))
  }
}

const changeQuantity = (nextQuantity) => {
  if (!selectedSku.value) return
  if (nextQuantity < 1) return
  if (nextQuantity > Number(selectedSku.value.stock)) {
    ElMessage.warning(t('cart.stockInsufficient'))
    return
  }
  quantity.value = nextQuantity
}

const addToCart = async () => {
  if (!selectedSku.value) {
    ElMessage.warning(t('detail.selectSku'))
    return
  }
  if (!auth.isAuthenticated) {
    ElMessage.warning(t('detail.loginRequired'))
    await router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (quantity.value <= 0) {
    ElMessage.warning(t('detail.invalidQuantity'))
    return
  }
  if (quantity.value > Number(selectedSku.value.stock)) {
    ElMessage.warning(t('cart.stockInsufficient'))
    return
  }

  adding.value = true
  try {
    await cart.addItem({ skuId: selectedSku.value.id, quantity: quantity.value })
    ElMessage.success(t('detail.addSuccess'))
  } catch (error) {
    ElMessage.error(errorMessage(error, t('cart.addFailed')))
  } finally {
    adding.value = false
  }
}

onMounted(loadProduct)
watch(() => route.params.id, loadProduct)
watch(selectedSkuId, () => { quantity.value = 1 })
</script>

<template>
  <section class="detail page-shell">
    <RouterLink class="back" to="/products">{{ t('detail.back') }}</RouterLink>
    <p v-if="detailLoading" class="state">{{ t('detail.loading') }}</p>
    <div v-else-if="detail" class="detail__grid">
      <div class="detail__image"><img :src="imageUrl" :alt="detail.name" /></div>
      <div class="detail__content">
        <p class="eyebrow">{{ t('detail.product', { id: detail.id }) }}</p>
        <h1>{{ detail.name }}</h1>
        <p class="subtitle">{{ detail.subtitle }}</p>
        <p class="description">{{ detail.description }}</p>

        <div class="sku-section">
          <h2>{{ t('detail.choose') }}</h2>
          <p v-if="!detail.skus?.length" class="empty">{{ t('detail.noSku') }}</p>
          <button
            v-for="sku in detail.skus"
            :key="sku.id"
            class="sku"
            :class="{ 'is-selected': selectedSkuId === sku.id }"
            type="button"
            :disabled="sku.stock <= 0"
            @click="selectedSkuId = sku.id"
          >
            <span>{{ formatSpec(sku.specJson) }}</span>
            <strong>{{ formatPrice(sku.price) }}</strong>
            <small>{{ sku.stock > 0 ? t('detail.inStock', { count: sku.stock }) : t('detail.outOfStock') }}</small>
          </button>
        </div>

        <div v-if="selectedSku" class="selection">
          <div>
            <span>{{ t('detail.selected') }}</span>
            <strong>{{ selectedSku.skuCode }} · {{ formatPrice(selectedSku.price) }}</strong>
          </div>
          <div class="purchase">
            <span>{{ t('detail.quantity') }}</span>
            <div class="purchase__quantity">
              <button type="button" :aria-label="t('detail.decrease')" :disabled="quantity <= 1" @click="changeQuantity(quantity - 1)">−</button>
              <strong>{{ quantity }}</strong>
              <button type="button" :aria-label="t('detail.increase')" @click="changeQuantity(quantity + 1)">+</button>
            </div>
            <el-button :loading="adding" :disabled="selectedSku.stock <= 0" @click="addToCart">
              {{ adding ? t('detail.adding') : t('detail.addToCart') }}
            </el-button>
          </div>
        </div>
        <el-button v-else class="unselected-add" @click="addToCart">{{ t('detail.addToCart') }}</el-button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.detail { min-height: 68vh; padding-top: 34px; padding-bottom: 72px; }
.back { display: inline-block; margin-bottom: 24px; color: var(--muted); font-size: 12px; text-decoration: none; }
.detail__grid { display: grid; grid-template-columns: minmax(0, 1.2fr) minmax(360px, .8fr); gap: clamp(36px, 6vw, 90px); }
.detail__image { min-height: 520px; overflow: hidden; background: var(--paper); }
.detail__image img { width: 100%; height: 100%; object-fit: cover; }
.detail__content { padding-top: 20px; }
.eyebrow { margin: 0 0 13px; color: var(--red); font-size: 10px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
h1 { margin: 0; font-size: clamp(38px, 4vw, 64px); letter-spacing: -.055em; line-height: .98; }
.subtitle { margin: 15px 0 0; color: var(--muted); font-size: 14px; }
.description { margin: 28px 0; color: #3f3d3a; font-size: 13px; line-height: 1.75; }
.sku-section { padding-top: 22px; border-top: 1px solid var(--line); }
.sku-section h2 { margin: 0 0 13px; font-size: 13px; }
.sku { display: grid; width: 100%; grid-template-columns: 1fr auto; gap: 5px 20px; margin-bottom: 8px; padding: 14px; text-align: left; background: white; border: 1px solid var(--line); cursor: pointer; }
.sku.is-selected { border-color: var(--ink); box-shadow: 0 0 0 1px var(--ink); }
.sku:disabled { cursor: not-allowed; opacity: .45; }
.sku span { font-size: 12px; }
.sku strong { grid-row: span 2; align-self: center; }
.sku small { color: var(--muted); }
.selection { display: grid; gap: 20px; margin-top: 24px; padding: 18px 0; border-top: 1px solid var(--line); border-bottom: 1px solid var(--line); font-size: 12px; }
.selection > div:first-child { display: flex; justify-content: space-between; }
.selection span, .empty, .state { color: var(--muted); }
.purchase { display: grid; grid-template-columns: auto 108px 1fr; align-items: center; gap: 14px; }
.purchase__quantity { display: grid; grid-template-columns: 34px 40px 34px; height: 36px; border: 1px solid var(--line); }
.purchase__quantity button { background: white; border: 0; cursor: pointer; }
.purchase__quantity button:disabled { cursor: not-allowed; opacity: .35; }
.purchase__quantity strong { display: grid; place-items: center; border-right: 1px solid var(--line); border-left: 1px solid var(--line); }
.purchase .el-button { height: 38px; color: white; background: var(--ink); border-color: var(--ink); }
.unselected-add.el-button { width: 100%; height: 42px; margin-top: 22px; color: white; background: var(--ink); border-color: var(--ink); }
.state { padding: 80px 0; text-align: center; }
@media (max-width: 840px) { .detail__grid { grid-template-columns: 1fr; } .detail__image { min-height: 380px; } }
</style>
