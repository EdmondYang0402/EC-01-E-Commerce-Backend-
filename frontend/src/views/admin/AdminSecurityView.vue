<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { errorMessage } from '../../services/http'
import { useAuthStore } from '../../stores/auth'
import { useLocaleStore } from '../../stores/locale'

const auth = useAuthStore()
const locale = useLocaleStore()
const t = (key) => locale.t(key)
const router = useRouter()
const saving = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: '',
})

const submit = async () => {
  if (!form.oldPassword || !form.newPassword || !form.confirmNewPassword) {
    ElMessage.warning(t('admin.security.required'))
    return
  }
  if (form.newPassword !== form.confirmNewPassword) {
    ElMessage.warning(t('admin.security.mismatch'))
    return
  }
  if (form.newPassword === form.oldPassword) {
    ElMessage.warning(t('admin.security.same'))
    return
  }
  if (form.newPassword.length < 8 || form.newPassword.length > 32) {
    ElMessage.warning(t('admin.security.length'))
    return
  }
  if (!/^(?=.*[A-Za-z])(?=.*\d).+$/.test(form.newPassword)) {
    ElMessage.warning(t('admin.security.format'))
    return
  }

  saving.value = true
  try {
    await auth.changeAdminPassword({ ...form })
    Object.assign(form, { oldPassword: '', newPassword: '', confirmNewPassword: '' })
    ElMessage.success(t('admin.security.success'))
    await router.replace('/login')
  } catch (error) {
    ElMessage.error(errorMessage(error, t('admin.security.failed')))
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="admin-page admin-security-page">
    <RouterLink class="admin-back" to="/admin/products">{{ t('admin.security.back') }}</RouterLink>
    <header class="admin-page__header">
      <div>
        <p class="admin-eyebrow">{{ t('admin.security.eyebrow') }}</p>
        <h1>{{ t('admin.security.title') }}</h1>
        <p class="admin-page__subtitle">{{ t('admin.security.subtitle') }}</p>
      </div>
      <span class="security-badge">{{ t('admin.security.currentAdmin') }}</span>
    </header>

    <form class="admin-form security-form" @submit.prevent="submit">
      <label class="admin-field admin-field--full">
        <span>{{ t('admin.security.oldPassword') }}</span>
        <input v-model="form.oldPassword" type="password" autocomplete="current-password" />
      </label>
      <label class="admin-field">
        <span>{{ t('admin.security.newPassword') }}</span>
        <input v-model="form.newPassword" type="password" autocomplete="new-password" />
      </label>
      <label class="admin-field">
        <span>{{ t('admin.security.confirmPassword') }}</span>
        <input v-model="form.confirmNewPassword" type="password" autocomplete="new-password" />
      </label>
      <p class="password-hint">{{ t('admin.security.hint') }}</p>
      <div class="admin-form__actions">
        <button class="admin-primary-button" type="submit" :disabled="saving">
          {{ saving ? t('admin.security.submitting') : t('admin.security.submit') }}
        </button>
        <RouterLink class="admin-secondary-button" to="/admin/products">{{ t('admin.common.cancel') }}</RouterLink>
      </div>
    </form>
  </section>
</template>

<style scoped>
.security-form { max-width: 720px; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.security-badge { padding: 8px 11px; color: #315b42; font-size: 8px; font-weight: 750; letter-spacing: .12em; background: #eaf5ee; border: 1px solid #cce1d3; }
.password-hint { grid-column: 1 / -1; margin: -5px 0 0; color: var(--muted); font-size: 10px; line-height: 1.6; }
@media (max-width: 620px) {
  .security-form { grid-template-columns: 1fr; }
  .password-hint { grid-column: auto; }
}
</style>
