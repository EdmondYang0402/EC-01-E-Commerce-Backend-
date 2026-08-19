<script setup>
defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 20 },
})

const paths = {
  search: ['M21 21l-4.35-4.35', 'M11 18a7 7 0 1 1 0-14 7 7 0 0 1 0 14Z'],
  user: ['M20 21a8 8 0 0 0-16 0', 'M12 13a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z'],
  cart: ['M4 5h2l1.5 9h9.8l1.7-6H7', 'M10 19.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0Zm8 0a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0Z'],
  arrow: ['M5 12h14', 'm14 7 5 5-5 5'],
  heart: ['M20.8 4.6a5.4 5.4 0 0 0-7.6 0L12 5.8l-1.2-1.2a5.4 5.4 0 0 0-7.6 7.6l1.2 1.2L12 21l7.6-7.6 1.2-1.2a5.4 5.4 0 0 0 0-7.6Z'],
  plus: ['M12 5v14', 'M5 12h14'],
  menu: ['M4 7h16', 'M4 12h16', 'M4 17h16'],
}
</script>

<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <path v-for="path in paths[name]" :key="path" :d="path" />
  </svg>
</template>
