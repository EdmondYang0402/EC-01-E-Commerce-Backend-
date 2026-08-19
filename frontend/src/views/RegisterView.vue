<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { errorMessage } from '../services/http'
import { useAuthStore } from '../stores/auth'
import { useLocaleStore } from '../stores/locale'

const auth = useAuthStore()
const locale = useLocaleStore()
const t = (key) => locale.t(key)
const router = useRouter()
const submitting = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '' })

const submit = async () => {
  if (!form.username.trim() || !form.password || !form.confirmPassword) {
    ElMessage.warning(t('register.required'))
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning(t('register.mismatch'))
    return
  }
  submitting.value = true
  try {
    await auth.register({
      username: form.username.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
    })
    ElMessage.success(t('register.success'))
    await router.replace('/login')
  } catch (error) {
    ElMessage.error(errorMessage(error, t('register.failed')))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="auth-page page-shell">
    <div class="auth-card">
      <p class="eyebrow">{{ t('register.eyebrow') }}</p>
      <h1>{{ t('register.title') }}</h1>
      <p class="intro">{{ t('register.intro') }}</p>
      <form @submit.prevent="submit">
        <label>{{ t('register.username') }}<el-input v-model="form.username" autocomplete="username" /></label>
        <label>{{ t('register.password') }}<el-input v-model="form.password" type="password" show-password autocomplete="new-password" /></label>
        <label>{{ t('register.confirm') }}<el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" /></label>
        <el-button native-type="submit" :loading="submitting">{{ t('register.submit') }}</el-button>
      </form>
      <p class="switch">{{ t('register.existing') }} <RouterLink to="/login">{{ t('register.signIn') }}</RouterLink></p>
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
