<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { postForm, postJson } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import type { CreateSongResponse, UploadAudioResponse } from '@/types/catalog'
import type { ApiResponse } from '@/types/common'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({
  title: '',
  artist: '',
  genre: 'Indie',
  moodTag: '',
  durationText: '03:00',
  description: '',
  audioUrl: '',
  coverUrl: '',
  tags: '',
})

const submitting = ref(false)
const uploadPending = ref(false)
const actionMessage = ref('')
const uploadedAudio = ref<UploadAudioResponse | null>(null)

const genreOptions = ['Indie', 'Electronic', 'Lo-fi', 'City Pop']

async function uploadLocalAudio(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) {
    return
  }

  uploadPending.value = true
  actionMessage.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await postForm<UploadAudioResponse>('/api/media/audio/upload', formData, authStore.token)

    uploadedAudio.value = response
    form.audioUrl = response.audioUrl
    actionMessage.value = '本地音频上传成功，已自动填入音频地址。'
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : '本地音频上传失败。'
  } finally {
    uploadPending.value = false
    target.value = ''
  }
}

async function submitSong() {
  if (!authStore.profile) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  submitting.value = true
  actionMessage.value = ''

  try {
    const response = await postJson<ApiResponse<CreateSongResponse>>('/api/songs', {
      userId: authStore.profile.id,
      title: form.title,
      artist: form.artist,
      genre: form.genre,
      moodTag: form.moodTag,
      durationText: form.durationText,
      description: form.description,
      audioUrl: form.audioUrl,
      coverUrl: form.coverUrl,
      tags: form.tags
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean),
    }, authStore.token)

    actionMessage.value = `歌曲已创建：${response.data.title}`
    form.title = ''
    form.artist = ''
    form.moodTag = ''
    form.durationText = '03:00'
    form.description = ''
    form.audioUrl = ''
    form.coverUrl = ''
    form.tags = ''
    uploadedAudio.value = null
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : '歌曲创建失败。'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-shell stack">
    <section class="section-card hero-block motion-rise">
      <div>
        <span class="eyebrow">Creator Studio</span>
        <h1 class="section-title">提交外部音频 URL，或先上传本地音频再建歌。</h1>
        <p class="section-copy">
          现在创作者可以直接把可公开播放的外部音频地址写入平台，也可以先上传本地文件，再把返回地址作为歌曲音频源。
        </p>
      </div>
    </section>

    <section class="grid-two">
      <article class="section-card section-block">
        <span class="eyebrow">Upload Local Audio</span>
        <h2 class="section-title">本地文件上传</h2>
        <p class="section-copy">上传成功后，会自动把 `/media/audio/...` 地址填入歌曲表单。</p>

        <label class="upload-box">
          <span>{{ uploadPending ? '上传中...' : '选择本地音频文件' }}</span>
          <input type="file" accept=".mp3,.wav,.ogg,.m4a,.flac,audio/*" @change="uploadLocalAudio" />
        </label>

        <div v-if="uploadedAudio" class="upload-result">
          <p>已上传：{{ uploadedAudio.fileName }}</p>
          <p>音频地址：{{ uploadedAudio.audioUrl }}</p>
        </div>
      </article>

      <article class="section-card section-block">
        <span class="eyebrow">External Audio URL</span>
        <h2 class="section-title">外部音频直链</h2>
        <p class="section-copy">
          直接填写 `http://` 或 `https://` 音频地址即可，平台会优先走外部直链播放和下载。
        </p>
      </article>
    </section>

    <section class="section-card section-block">
      <span class="eyebrow">Create Song</span>
      <h2 class="section-title">创建歌曲</h2>

      <form class="song-form" @submit.prevent="submitSong">
        <label class="field">
          <span>歌曲标题</span>
          <input v-model="form.title" type="text" required />
        </label>

        <label class="field">
          <span>歌手名</span>
          <input v-model="form.artist" type="text" required />
        </label>

        <label class="field">
          <span>曲风</span>
          <select v-model="form.genre">
            <option v-for="genre in genreOptions" :key="genre" :value="genre">{{ genre }}</option>
          </select>
        </label>

        <label class="field">
          <span>情绪标签</span>
          <input v-model="form.moodTag" type="text" placeholder="例如：夜行感 / 学习循环" />
        </label>

        <label class="field">
          <span>时长</span>
          <input v-model="form.durationText" type="text" placeholder="03:30" required />
        </label>

        <label class="field field-wide">
          <span>音频地址</span>
          <input
            v-model="form.audioUrl"
            type="text"
            placeholder="填写外部 URL，或使用本地上传后自动填充的 /media/audio/... 地址"
            required
          />
        </label>

        <label class="field field-wide">
          <span>封面地址</span>
          <input v-model="form.coverUrl" type="text" placeholder="可选" />
        </label>

        <label class="field field-wide">
          <span>标签</span>
          <input v-model="form.tags" type="text" placeholder="用英文逗号分隔，例如：夜跑,电子,通勤" />
        </label>

        <label class="field field-wide">
          <span>简介</span>
          <textarea v-model="form.description" rows="4" placeholder="介绍这首歌适合的场景或特点"></textarea>
        </label>

        <button type="submit" class="primary-button submit-button" :disabled="submitting">
          {{ submitting ? '创建中...' : '创建歌曲' }}
        </button>
      </form>

      <p v-if="actionMessage" class="status-text">{{ actionMessage }}</p>
    </section>
  </div>
</template>

<style scoped>
.hero-block,
.section-block {
  padding: 26px;
}

.upload-box {
  margin-top: 18px;
  padding: 18px;
  border: 1px dashed var(--color-border-strong);
  border-radius: 20px;
  display: grid;
  gap: 10px;
  color: var(--color-heading);
}

.upload-box input {
  font: inherit;
}

.upload-result,
.status-text,
.section-copy {
  color: var(--color-muted);
}

.song-form {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.field {
  display: grid;
  gap: 8px;
  color: var(--color-heading);
  font-weight: 600;
}

.field input,
.field select,
.field textarea {
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.88);
  color: var(--color-text);
  font: inherit;
}

.field textarea {
  resize: vertical;
}

.field-wide {
  grid-column: 1 / -1;
}

.submit-button {
  width: fit-content;
}

@media (max-width: 900px) {
  .song-form {
    grid-template-columns: 1fr;
  }

  .field-wide {
    grid-column: auto;
  }
}
</style>
