<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { errorMessage } from '../services/http'
import { useAuthStore } from '../stores/auth'
import { useLocaleStore } from '../stores/locale'

const auth = useAuthStore()
const locale = useLocaleStore()
const t = (key) => locale.t(key)
const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const form = reactive({ username: '', password: '' })

const submit = async () => {
  if (!form.username.trim() || !form.password) {
    ElMessage.warning(t('login.required'))
    return
  }
  submitting.value = true
  try {
    await auth.login({ username: form.username.trim(), password: form.password })
    ElMessage.success(t('login.success'))
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/products'
    await router.replace(redirect)
  } catch (error) {
    ElMessage.error(errorMessage(error, t('login.failed')))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="auth-page page-shell">
    <div class="auth-card">
      <p class="eyebrow">{{ t('login.eyebrow') }}</p>
      <h1>{{ t('login.title') }}</h1>
      <p class="intro">{{ t('login.intro') }}</p>
      <form @submit.prevent="submit">
        <label>{{ t('login.username') }}<el-input v-model="form.username" autocomplete="username" /></label>
        <label>{{ t('login.password') }}<el-input v-model="form.password" type="password" show-password autocomplete="current-password" /></label>
        <el-button native-type="submit" :loading="submitting">{{ t('login.submit') }}</el-button>
      </form>
      <p class="switch">{{ t('login.new') }} <RouterLink to="/register">{{ t('login.create') }}</RouterLink></p>
    </div>
  </section>
</template>

<style scoped>
.auth-page { display: grid; min-height: 68vh; padding: 64px 0 82px; place-items: center; }
.auth-card { width: min(440px, calc(100vw - 32px)); padding: 42px; background: var(--white); border: 1px solid var(--line); }
.eyebrow { margin: 0 0 8px; color: var(--red); font-size: 10px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
h1 { margin: 0; font-size: 42px; letter-spacing: -.05em; }
.intro { margin: 12px 0 28px; color: var(--muted); font-size: 12px; line-height: 1.6; }
form { display: grid; gap: 18px; }
label { display: grid; gap: 7px; font-size: 11px; font-weight: 650; }
form .el-button { width: 100%; height: 42px; margin-top: 4px; color: white; background: var(--ink); border-color: var(--ink); }
.switch { margin: 22px 0 0; color: var(--muted); font-size: 11px; text-align: center; }
.switch a { color: var(--ink); font-weight: 700; }
</style>
