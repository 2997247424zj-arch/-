<template>
  <AdminLayout>
    <!-- 仪表盘首页内容已移除；访问 /dashboard 时自动重定向到统计页面 -->
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 访问 /dashboard 时自动重定向到统计页，保留页面路由但不渲染原有内容
onMounted(() => {
  router.replace('/dashboard/statistics')
})
</script>

<style scoped>
.dashboard-page {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
  padding-bottom: 3rem;
  color: var(--color-text-primary);
  --dashboard-surface: linear-gradient(140deg, rgba(7, 24, 49, 0.95), rgba(3, 11, 26, 0.92));
  --dashboard-surface-strong: linear-gradient(150deg, rgba(4, 18, 38, 0.98), rgba(2, 8, 20, 0.92));
  --dashboard-border: rgba(79, 181, 255, 0.32);
  --dashboard-border-strong: rgba(79, 181, 255, 0.45);
  --dashboard-shadow: 0 28px 60px rgba(3, 10, 26, 0.6);
}

.page-backdrop {
  position: absolute;
  inset: -40px;
  border-radius: 40px;
  pointer-events: none;
  overflow: hidden;
  z-index: -1;
  background: radial-gradient(circle at 10% 20%, rgba(79, 181, 255, 0.25), transparent 55%),
              radial-gradient(circle at 80% 0%, rgba(236, 72, 153, 0.2), transparent 45%),
              radial-gradient(circle at 55% 90%, rgba(18, 196, 192, 0.22), transparent 50%);
}

.page-orb {
  position: absolute;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  filter: blur(8px);
  opacity: 0.45;
}

.page-orb.orb-one {
  top: -80px;
  left: -40px;
  background: linear-gradient(135deg, rgba(79, 181, 255, 0.75), rgba(14, 165, 233, 0.2));
}

.page-orb.orb-two {
  bottom: -120px;
  right: -20px;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.55), rgba(67, 56, 202, 0.15));
}

.page-grid {
  position: absolute;
  inset: 0;
  display: block;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 140px 140px;
  opacity: 0.35;
  mix-blend-mode: screen;
}

.breadcrumb {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.85rem;
  border-radius: var(--radius-full);
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  box-shadow: 0 8px 20px rgba(3, 10, 26, 0.4);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.75rem;
}

.stat-card {
  background: var(--dashboard-surface);
  border: 1px solid var(--dashboard-border);
  border-radius: 28px;
  padding: 2rem;
  box-shadow: var(--dashboard-shadow);
  position: relative;
  overflow: hidden;
  isolation: isolate;
  backdrop-filter: blur(16px);
  transition: transform var(--transition-base), box-shadow var(--transition-base), border-color var(--transition-base);
}

.stat-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% -10%, rgba(79, 181, 255, 0.2), transparent 55%);
  opacity: 0.7;
  pointer-events: none;
  z-index: -1;
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.01);
  border-color: var(--dashboard-border-strong);
  box-shadow: 0 32px 70px rgba(30, 138, 230, 0.35);
}

.stat-card.flight-stat {
  background:
    linear-gradient(135deg, rgba(30, 138, 230, 0.18), rgba(30, 138, 230, 0)),
    var(--dashboard-surface);
}

.stat-card.order-stat {
  background:
    linear-gradient(135deg, rgba(236, 72, 153, 0.16), rgba(236, 72, 153, 0)),
    var(--dashboard-surface);
}

.stat-card.revenue-stat {
  background:
    linear-gradient(135deg, rgba(245, 185, 66, 0.18), rgba(245, 185, 66, 0)),
    var(--dashboard-surface);
}

.stat-card.user-stat {
  background:
    linear-gradient(135deg, rgba(16, 185, 129, 0.18), rgba(16, 185, 129, 0)),
    var(--dashboard-surface);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin-bottom: 1.25rem;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 0 12px rgba(255, 255, 255, 0.08);
  transition: transform var(--transition-base);
}

.stat-card:hover .stat-icon {
  transform: translateY(-4px);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: clamp(2rem, 4vw, 2.8rem);
  font-weight: 800;
  margin-bottom: 0.75rem;
  background: linear-gradient(135deg, #ffffff 0%, rgba(255, 255, 255, 0.85) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.02em;
}

.stat-change {
  font-size: 0.9rem;
  font-weight: 700;
  padding: 0.45rem 1rem;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  border: 1px solid transparent;
  transition: all var(--transition-base);
}

.stat-change.positive {
  color: var(--color-success);
  background: linear-gradient(135deg, rgba(31, 209, 161, 0.18), rgba(12, 139, 104, 0.08));
  border-color: rgba(31, 209, 161, 0.4);
  box-shadow: 0 8px 18px rgba(31, 209, 161, 0.25);
}

.stat-change.positive::before {
  content: '↑';
  font-size: 0.75rem;
}

.stat-change.negative {
  color: var(--color-error);
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.18), rgba(214, 63, 76, 0.08));
  border-color: rgba(255, 107, 107, 0.4);
  box-shadow: 0 8px 18px rgba(255, 107, 107, 0.25);
}

.stat-change.negative::before {
  content: '↓';
  font-size: 0.75rem;
}

.pending-section {
  margin-bottom: 0;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 1.5rem;
}

.pending-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.75rem;
  margin-top: 1.25rem;
}

.pending-card {
  background: var(--dashboard-surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 2rem;
  box-shadow: var(--dashboard-shadow);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  isolation: isolate;
  transition: transform var(--transition-base), border-color var(--transition-base), box-shadow var(--transition-base);
}

.pending-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 70% 10%, rgba(255, 255, 255, 0.08), transparent 60%);
  pointer-events: none;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.pending-card:hover {
  transform: translateY(-8px);
  border-color: var(--dashboard-border-strong);
  box-shadow: 0 28px 55px rgba(99, 102, 241, 0.25);
}

.pending-card:hover::after {
  opacity: 1;
}

.pending-card.urgent {
  border-left: 4px solid var(--color-error);
  background:
    linear-gradient(140deg, rgba(255, 107, 107, 0.18), rgba(255, 107, 107, 0)),
    var(--dashboard-surface-strong);
}

.pending-card.warning {
  border-left: 4px solid var(--color-warning);
  background:
    linear-gradient(140deg, rgba(245, 185, 66, 0.18), rgba(245, 185, 66, 0)),
    var(--dashboard-surface-strong);
}

.pending-card.info {
  border-left: 4px solid var(--color-info);
  background:
    linear-gradient(140deg, rgba(58, 201, 255, 0.2), rgba(58, 201, 255, 0)),
    var(--dashboard-surface-strong);
}

.pending-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.85rem;
}

.pending-header h3 {
  font-size: 1.05rem;
  color: var(--color-text-primary);
}

.pending-count {
  font-size: 1.4rem;
  font-weight: 800;
  padding: 0.45rem 1.1rem;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(79, 181, 255, 0.25), rgba(30, 138, 230, 0.15));
  border: 1px solid rgba(79, 181, 255, 0.45);
  box-shadow: 0 10px 25px rgba(79, 181, 255, 0.25);
}

.pending-card p {
  color: var(--color-text-secondary);
  font-size: 0.92rem;
}

/* 快捷操作面板样式 */
.quick-actions-section {
  margin-bottom: 2rem;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-top: 1.25rem;
}

.quick-action-card {
  background: var(--dashboard-surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: var(--dashboard-shadow);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  isolation: isolate;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quick-action-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 0%, rgba(79, 181, 255, 0.1), transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.quick-action-card:hover {
  transform: translateY(-6px) scale(1.02);
  border-color: var(--dashboard-border-strong);
  box-shadow: 0 24px 48px rgba(79, 181, 255, 0.3);
}

.quick-action-card:hover::before {
  opacity: 1;
}

.quick-action-card:hover .action-arrow {
  transform: translateX(4px);
  opacity: 1;
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: inset 0 0 12px rgba(255, 255, 255, 0.05);
  transition: transform 0.3s ease;
}

.quick-action-card:hover .action-icon {
  transform: scale(1.1) rotate(5deg);
}

.action-content {
  flex: 1;
  min-width: 0;
}

.action-content h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 0.35rem 0;
  line-height: 1.3;
}

.action-content p {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.action-arrow {
  font-size: 1.2rem;
  color: rgba(79, 181, 255, 0.8);
  opacity: 0.6;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-action-card {
    padding: 1.25rem;
  }
  
  .action-icon {
    width: 48px;
    height: 48px;
    font-size: 1.5rem;
  }
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.75rem;
  margin-top: 2rem;
}

.chart-card {
  background: var(--dashboard-surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 28px;
  padding: 2rem;
  box-shadow: var(--dashboard-shadow);
  transition: transform var(--transition-base), border-color var(--transition-base), box-shadow var(--transition-base);
}

.chart-card:hover {
  transform: translateY(-6px);
  border-color: var(--dashboard-border-strong);
  box-shadow: 0 30px 60px rgba(79, 181, 255, 0.28);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.chart-header h3 {
  font-size: 1.1rem;
  color: var(--color-text-primary);
}

.period-select {
  background: rgba(3, 12, 26, 0.6);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-light);
  border-radius: 999px;
  padding: 0.35rem 1rem;
  font-size: 0.9rem;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.05);
}

.period-select option {
  background: #071a33;
  color: var(--color-text-primary);
}

.chart-placeholder {
  height: 280px;
  border-radius: 20px;
  border: 1px dashed rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.chart-placeholder.chart-placeholder-row {
  justify-content: flex-start;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  height: 100%;
}

.chart-bar {
  flex: 1;
  background: var(--gradient-primary);
  border-radius: 12px 12px 0 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 0.5rem;
  box-shadow: 0 12px 24px rgba(30, 138, 230, 0.25);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.chart-bar:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 32px rgba(30, 138, 230, 0.35);
}

.bar-value {
  color: var(--color-white);
  font-size: 0.85rem;
  font-weight: 600;
}

.pie-chart {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.donut {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.donut-ring {
  stroke: rgba(15, 23, 42, 0.9);
}

.donut-segment {
  stroke-linecap: round;
  transition: stroke-dasharray var(--transition-base), stroke-dashoffset var(--transition-base);
}

.donut-center {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.donut-total {
  font-size: 1.6rem;
  font-weight: 700;
  color: #e5f2ff;
}

.donut-label {
  font-size: 0.85rem;
  color: rgba(148, 163, 184, 0.9);
}

.chart-header-with-legend {
  gap: 1.5rem;
}

.chart-subtitle {
  margin: 0.35rem 0 0;
  font-size: 0.85rem;
  color: rgba(148, 163, 184, 0.9);
}

.chart-legend {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: rgba(226, 232, 240, 0.92);
}

.chart-legend li {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-label {
  min-width: 70px;
}

.legend-value {
  font-weight: 600;
}

.legend-percent {
  color: rgba(148, 163, 184, 0.9);
}

@media (max-width: 768px) {
  .dashboard-page {
    gap: 1.75rem;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .chart-placeholder {
    height: 220px;
  }
}
</style>