<template>
  <div class="product-reviews">
    <div class="reviews-header">
      <h3>商品评价 ({{ reviews.length }})</h3>
      <el-button v-if="canReview && userStore.isLoggedIn" type="primary" @click="showAddReview = true">
        写评价
      </el-button>
    </div>

    <el-empty v-if="!loading && reviews.length === 0" description="暂无评价" />

    <div v-loading="loading" class="reviews-list">
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <el-avatar :src="review.userAvatar || defaultAvatar" :size="40" />
          <div class="user-info">
            <div class="username">{{ review.username }}</div>
            <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
          </div>
          <div class="review-time">
            {{ formatTime(review.createTime) }}
          </div>
        </div>

        <div class="review-content">
          {{ review.content }}
        </div>

        <div v-if="review.images" class="review-images">
          <el-image
            v-for="(img, index) in review.images.split(',')"
            :key="index"
            :src="img"
            :preview-src-list="review.images.split(',')"
            :initial-index="index"
            fit="cover"
            class="review-image"
          />
        </div>

        <div class="review-actions">
          <el-button
            :type="review.isLiked ? 'primary' : 'default'"
            size="small"
            @click="handleLike(review)"
          >
            <el-icon><Pointer /></el-icon>
            {{ review.likeCount || 0 }}
          </el-button>

          <el-button
            v-if="canDelete(review)"
            type="danger"
            size="small"
            @click="handleDelete(review.id)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 添加评价对话框 -->
    <el-dialog v-model="showAddReview" title="写评价" width="600px">
      <el-form :model="reviewForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" show-text />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="6"
            placeholder="分享你的使用体验..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddReview = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Pointer } from '@element-plus/icons-vue'
import {
  getProductReviewsAPI,
  addReviewAPI,
  toggleReviewLikeAPI,
  deleteReviewAPI,
} from '@/api/modules/review'
import { useUserStore } from '@/store/user'

const props = defineProps<{
  productId: number
  canReview?: boolean
}>()

const userStore = useUserStore()
const loading = ref(false)
const reviews = ref<any[]>([])
const showAddReview = ref(false)
const submitting = ref(false)
const formRef = ref()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const reviewForm = ref({
  rating: 5,
  content: '',
})

const rules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容至少10个字', trigger: 'blur' },
  ],
}

const loadReviews = async () => {
  loading.value = true
  try {
    const res: any = await getProductReviewsAPI(props.productId)
    if (res.code === 200) {
      reviews.value = res.data || []
    }
  } catch (error) {
    ElMessage.error((error as Error)?.message || '加载评价失败')
    console.error('加载评价失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await formRef.value.validate()

    submitting.value = true
    const res: any = await addReviewAPI({
      productId: props.productId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
    })

    if (res.code === 200) {
      ElMessage.success('评价成功')
      showAddReview.value = false
      reviewForm.value = { rating: 5, content: '' }
      loadReviews()
    } else {
      ElMessage.error(res.msg || '评价失败')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleLike = async (review: any) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const res: any = await toggleReviewLikeAPI(review.id)
    if (res.code === 200) {
      loadReviews()
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handleDelete = async (reviewId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res: any = await deleteReviewAPI(reviewId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadReviews()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const canDelete = (review: any) => {
  return userStore.userInfo && userStore.userInfo.id === review.userId
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString()
  }
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped lang="scss">
.product-reviews {
  margin-top: 24px;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    margin: 0;
    font-size: 20px;
    color: #1e293b;
  }
}

.reviews-list {
  .review-item {
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    margin-bottom: 16px;
    border: 1px solid #e2e8f0;

    .review-header {
      display: flex;
      align-items: center;
      margin-bottom: 12px;

      .user-info {
        flex: 1;
        margin-left: 12px;

        .username {
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 4px;
        }
      }

      .review-time {
        color: #94a3b8;
        font-size: 14px;
      }
    }

    .review-content {
      color: #475569;
      line-height: 1.6;
      margin-bottom: 12px;
    }

    .review-images {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;

      .review-image {
        width: 100px;
        height: 100px;
        border-radius: 4px;
        cursor: pointer;
      }
    }

    .review-actions {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
