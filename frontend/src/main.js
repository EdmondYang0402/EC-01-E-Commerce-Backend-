import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElButton, ElInput } from 'element-plus'
import 'element-plus/es/components/button/style/css'
import 'element-plus/es/components/input/style/css'
import 'element-plus/es/components/message/style/css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useLocaleStore } from './stores/locale'
import './assets/styles/main.css'

const app = createApp(App)
const pinia = createPinia()

app.component('ElButton', ElButton)
app.component('ElInput', ElInput)
app.use(pinia).use(router)

const auth = useAuthStore(pinia)
const locale = useLocaleStore(pinia)
locale.applyLocale()
window.addEventListener('ec01:unauthorized', () => {
  auth.clearSession()
  if (router.currentRoute.value.meta.requiresAuth) {
    router.replace({
      name: 'login',
      query: { redirect: router.currentRoute.value.fullPath },
    })
  }
})

if (auth.isAuthenticated) {
  auth.fetchProfile().catch(() => auth.clearSession())
}

app.mount('#app')
