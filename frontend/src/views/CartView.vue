<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import fallbackImage from '../assets/products/chair.png'
import { errorMessage } from '../services/http'
import { useCartStore } from '../stores/cart'
import { useLocaleStore } from '../stores/locale'
import { useOrderStore } from '../stores/orders'

const cart = useCartStore()
const orders = useOrderStore()
const locale = useLocaleStore()
const router = useRouter()
const { items, itemCount, selectedCount, selectedTotal, loading } = storeToRefs(cart)
const { creating } = storeToRefs(orders)
const pendingId = ref(null)
const checkoutForm = reactive({ receiverName: '', receiverPhone: '', receiverAddress: '' })
const selectedCartItemIds = computed(() => items.value
  .filter((item) => Number(item.selected) === 1)
  .map((item) => item.cartItemId))
const t = (key, params) => locale.t(key, params)

const formatCurrency = (value) => new Intl.NumberFormat(
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

const loadCart = async () => {
  try {
    await cart.fetchCart()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('cart.loadFailed')))
  }
}

const updateItem = async (item, quantity, selected = Number(item.selected)) => {
  if (quantity > Number(item.stock)) {
    ElMessage.warning(t('cart.stockInsufficient'))
    return
  }
  pendingId.value = item.cartItemId
  try {
    await cart.updateItem(item.cartItemId, { quantity, selected })
  } catch (error) {
    ElMessage.error(errorMessage(error, t('cart.updateFailed')))
    await loadCart()
  } finally {
    pendingId.value = null
  }
}

const decrease = async (item) => {
  if (Number(item.quantity) === 1) {
    await removeItem(item)
    return
  }
  await updateItem(item, Number(item.quantity) - 1)
}

const increase = (item) => updateItem(item, Number(item.quantity) + 1)

const toggleSelected = (item, checked) =>
  updateItem(item, Number(item.quantity), checked ? 1 : 0)

const removeItem = async (item) => {
  pendingId.value = item.cartItemId
  try {
    await cart.deleteItem(item.cartItemId)
  } catch (error) {
    ElMessage.error(errorMessage(error, t('cart.deleteFailed')))
    await loadCart()
  } finally {
    pendingId.value = null
  }
}

const checkout = async () => {
  if (!selectedCartItemIds.value.length) {
    ElMessage.warning(t('cart.selectForCheckout'))
    return
  }
  if (!checkoutForm.receiverName.trim()
      || !checkoutForm.receiverPhone.trim()
      || !checkoutForm.receiverAddress.trim()) {
    ElMessage.warning(t('cart.receiverRequired'))
    return
  }

  try {
    const orderNo = await orders.createOrder({
      cartItemIds: selectedCartItemIds.value,
      receiverName: checkoutForm.receiverName.trim(),
      receiverPhone: checkoutForm.receiverPhone.trim(),
      receiverAddress: checkoutForm.receiverAddress.trim(),
    })
    await cart.fetchCart()
    ElMessage.success(t('cart.orderCreated'))
    await router.push({ name: 'order-detail', params: { orderNo } })
  } catch (error) {
    ElMessage.error(errorMessage(error, t('cart.checkoutFailed')))
  }
}

onMounted(loadCart)
</script>

<template>
  <section class="cart page-shell">
    <div class="cart__heading">
      <div>
        <p class="eyebrow">{{ t('cart.eyebrow') }}</p>
        <h1>{{ t('cart.title') }}</h1>
      </div>
      <span>{{ itemCount }}</span>
    </div>

    <p v-if="loading && !items.length" class="state">{{ t('cart.loading') }}</p>
    <div v-else-if="!items.length" class="empty">
      <p>{{ t('cart.empty') }}</p>
      <RouterLink to="/products">{{ t('cart.browse') }}</RouterLink>
    </div>

    <div v-else class="cart__content">
      <div class="cart__items">
        <article v-for="item in items" :key="item.cartItemId" class="cart-item" :class="{ 'is-pending': pendingId === item.cartItemId }">
          <label class="select-item">
            <input
              type="checkbox"
              :checked="Number(item.selected) === 1"
              :disabled="pendingId === item.cartItemId"
              :aria-label="t('cart.select', { name: item.productName })"
              @change="toggleSelected(item, $event.target.checked)"
            />
          </label>

          <RouterLink class="cart-item__image" :to="`/products/${item.productId}`">
            <img :src="item.coverUrl || fallbackImage" :alt="item.productName" />
          </RouterLink>

          <div class="cart-item__info">
            <RouterLink :to="`/products/${item.productId}`"><h2>{{ item.productName }}</h2></RouterLink>
            <p><span>{{ t('cart.spec') }}</span>{{ formatSpec(item.specJson) }}</p>
            <p><span>{{ t('cart.unitPrice') }}</span>{{ formatCurrency(item.price) }}</p>
            <small>{{ t('cart.stock', { count: item.stock }) }}</small>
          </div>

          <div class="quantity">
            <span>{{ t('cart.quantity') }}</span>
            <div>
              <button type="button" :disabled="pendingId === item.cartItemId" :aria-label="t('cart.decrease', { name: item.productName })" @click="decrease(item)">−</button>
              <strong>{{ item.quantity }}</strong>
              <button type="button" :disabled="pendingId === item.cartItemId" :aria-label="t('cart.increase', { name: item.productName })" @click="increase(item)">+</button>
            </div>
          </div>

          <div class="cart-item__subtotal">
            <span>{{ t('cart.subtotal') }}</span>
            <strong>{{ formatCurrency(Number(item.price) * Number(item.quantity)) }}</strong>
            <button type="button" :disabled="pendingId === item.cartItemId" @click="removeItem(item)">{{ t('cart.remove') }}</button>
          </div>
        </article>
      </div>

      <aside class="summary">
        <p>{{ t('cart.selectedItems', { count: selectedCount }) }}</p>
        <div><span>{{ t('cart.total') }}</span><strong>{{ formatCurrency(selectedTotal) }}</strong></div>
        <form class="checkout-form" @submit.prevent="checkout">
          <label>{{ t('cart.receiverName') }}<el-input v-model="checkoutForm.receiverName" maxlength="50" /></label>
          <label>{{ t('cart.receiverPhone') }}<el-input v-model="checkoutForm.receiverPhone" maxlength="30" /></label>
          <label>{{ t('cart.receiverAddress') }}<el-input v-model="checkoutForm.receiverAddress" maxlength="500" type="textarea" :rows="3" /></label>
          <el-button native-type="submit" :loading="creating" :disabled="!selectedCartItemIds.length">
            {{ t('cart.checkout') }}
          </el-button>
        </form>
        <small>{{ t('cart.serverPriceNote') }}</small>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.cart { min-height: 68vh; padding-top: 48px; padding-bottom: 80px; }
.cart__heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 28px; padding-bottom: 22px; border-bottom: 1px solid var(--ink); }
.eyebrow { margin: 0 0 8px; color: var(--red); font-size: 10px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
h1 { margin: 0; font-size: clamp(40px, 5vw, 68px); letter-spacing: -.055em; }
.cart__heading > span { font-size: 13px; font-weight: 700; }
.cart__content { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 38px; }
.cart__items { border-top: 1px solid var(--line); }
.cart-item { display: grid; grid-template-columns: 24px 132px minmax(220px, 1fr) 150px 140px; gap: 18px; align-items: center; padding: 22px 0; border-bottom: 1px solid var(--line); transition: opacity 140ms ease; }
.cart-item.is-pending { opacity: .55; }
.select-item input { width: 16px; height: 16px; accent-color: var(--ink); }
.cart-item__image { display: block; height: 112px; overflow: hidden; background: var(--paper); }
.cart-item__image img { width: 100%; height: 100%; object-fit: cover; }
.cart-item__info a { color: inherit; text-decoration: none; }
.cart-item__info h2 { margin: 0 0 12px; font-size: 15px; }
.cart-item__info p { display: flex; gap: 10px; margin: 5px 0; font-size: 11px; }
.cart-item__info p span, .quantity > span, .cart-item__subtotal > span { color: var(--muted); }
.cart-item__info small { display: block; margin-top: 9px; color: var(--blue); font-size: 10px; }
.quantity { display: grid; justify-items: center; gap: 10px; font-size: 10px; }
.quantity > div { display: grid; grid-template-columns: 34px 40px 34px; height: 34px; border: 1px solid var(--line); }
.quantity button { background: white; border: 0; cursor: pointer; }
.quantity button:disabled { cursor: not-allowed; opacity: .35; }
.quantity strong { display: grid; place-items: center; border-right: 1px solid var(--line); border-left: 1px solid var(--line); }
.cart-item__subtotal { display: grid; justify-items: end; gap: 9px; font-size: 10px; }
.cart-item__subtotal strong { font-size: 14px; }
.cart-item__subtotal button { padding: 0; color: var(--red); font-size: 10px; background: transparent; border: 0; cursor: pointer; text-decoration: underline; }
.summary { position: sticky; top: 24px; align-self: start; padding: 24px; background: var(--paper); border-top: 4px solid var(--ink); }
.summary p { margin: 0 0 26px; color: var(--muted); font-size: 11px; }
.summary > div { display: flex; justify-content: space-between; padding: 18px 0; border-top: 1px solid var(--line); font-size: 12px; }
.summary > div strong { font-size: 18px; }
.summary .el-button { width: 100%; height: 42px; margin-top: 8px; }
.summary small { display: block; margin-top: 10px; color: var(--muted); font-size: 9px; text-align: center; }
.checkout-form { display: grid; gap: 12px; padding-top: 16px; border-top: 1px solid var(--line); }
.checkout-form label { display: grid; gap: 6px; color: var(--muted); font-size: 10px; }
.state, .empty { padding: 90px 0; color: var(--muted); text-align: center; }
.empty a { display: inline-block; margin-top: 12px; padding: 10px 16px; color: white; background: var(--ink); text-decoration: none; }
@media (max-width: 1050px) { .cart__content { grid-template-columns: 1fr; } .summary { position: static; } }
@media (max-width: 780px) { .cart-item { grid-template-columns: 22px 90px 1fr; } .cart-item__image { height: 90px; } .quantity { grid-column: 2; } .cart-item__subtotal { grid-column: 3; } }
</style>
