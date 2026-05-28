<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="iconSize" :color="iconColor">
        <component :is="icon" />
      </el-icon>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-description" v-if="description">{{ description }}</p>
    <div class="empty-actions" v-if="$slots.default">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Box, ShoppingCart, Document, User } from '@element-plus/icons-vue'
import { computed } from 'vue'

interface Props {
  type?: 'default' | 'cart' | 'order' | 'user'
  title?: string
  description?: string
  iconSize?: number
  iconColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  title: '暂无数据',
  iconSize: 120,
  iconColor: '#e2e8f0',
})

const icon = computed(() => {
  const iconMap = {
    default: Box,
    cart: ShoppingCart,
    order: Document,
    user: User,
  }
  return iconMap[props.type]
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;

  .empty-icon {
    margin-bottom: 24px;
    opacity: 0.5;
  }

  .empty-title {
    margin: 0 0 12px;
    font-size: 18px;
    font-weight: 600;
    color: #334155;
  }

  .empty-description {
    margin: 0 0 24px;
    font-size: 14px;
    color: #64748b;
    max-width: 400px;
  }

  .empty-actions {
    display: flex;
    gap: 12px;
  }
}
</style>
