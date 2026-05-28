<template>
  <PassengerLayout>
    <div class="page-section" style="padding:20px;">
      <div class="section-title" style="margin-bottom:12px;">
        <span class="title-icon">🖨️</span>
        <span>机票打印</span>
      </div>
      <div style="width:1400px; margin:0 auto; padding: 12px 24px; min-height: 700px;">
        <TicketQuickEntry />
      </div>
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import PassengerLayout from '../components/layout/PassengerLayout.vue'
import TicketQuickEntry from '../components/TicketQuickEntry.vue'
</script>

<style scoped>
.page-section { color: #3556cdff; }
</style>


