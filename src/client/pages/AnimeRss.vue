<script setup>
import { ref, onUpdated, onMounted, onUnmounted } from 'vue'
import rssApi from '@/client/api/rssApi.js'
import { mdiDelete, mdiPlus, mdiRead, mdiUpdate } from '@mdi/js'
import { createConfirm, createSnackbar } from 'vuetify-use-dialog'

const tab = ref('option-1')
const unread = ref({
  id: 0,
  title: '未读',
})
const rssSubscriptionList = ref([unread.value])
const addLink = ref('')
const addRssDialog = ref(false)

function updateHeight() {
  const aEl = document.querySelector('#rss-card-1')
  const bEl = document.querySelector('#rss-card-2')

  const aHeight = aEl.clientHeight
  const bHeight = bEl.clientHeight

  const cHeight = aHeight - bHeight

  document.querySelector('#rss-card-3').style.height = `${cHeight}px`
  console.log(aHeight, bHeight, cHeight)
}

const getRssSubscriptionList = async (page, size) => {
  rssApi.queryRssSubscription(page, size).then((res) => {
    rssSubscriptionList.value = rssSubscriptionList.value.concat(res.data.data)
  })
}

function openAddRssSubscriptionDialog() {
  addLink.value = ''
  addRssDialog.value = true
}

function addRssSubscription() {
  rssApi.addRssSubscription(addLink.value).then((res) => {
    addRssDialog.value = false
    rssSubscriptionList.value = rssSubscriptionList.value.concat(res.data.data)
  })
}

function deleteRssSubscription(rssSubscriptionId) {
  createConfirm({ content: '确定删除当前订阅？' }).then((res) => {
    if (!res) return
    rssApi.deleteRssSubscription(rssSubscriptionId).then(() => {
      // 删除后从列表中移除
      rssSubscriptionList.value = rssSubscriptionList.value.filter(
        (rssSubscription) => rssSubscription.id !== rssSubscriptionId,
      )
      createSnackbar({ text: '删除成功!' })
    })
  })
}

function markAllRssSubscriptionRead() {
  rssSubscriptionList.value.forEach((rssSubscription) => {
    rssApi.markRssSubscriptionRead(rssSubscription.id)
  })
}

function markRssSubscriptionRead(rssSubscriptionId) {
  rssApi.markRssSubscriptionRead(rssSubscriptionId)
}

onUpdated(() => {
  updateHeight()
})

onMounted(() => {
  updateHeight()
  window.addEventListener('resize', updateHeight)
  getRssSubscriptionList(1, 10)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateHeight)
})
</script>

<template>
  <v-card
    id="rss-card-1"
    class="h-screen d-flex"
  >
    <v-dialog
      v-model="addRssDialog"
      width="500"
    >
      <v-card>
        <v-card-text>
          <v-text-field
            v-model="addLink"
            label="订阅地址"
            variant="outlined"
            :rules="[(v) => !!v || '请输入订阅地址']"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            variant="text"
            @click="addRssDialog = false"
          >
            取消
          </v-btn>
          <v-btn
            color="primary"
            variant="text"
            @click="addRssSubscription"
          >
            保存
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-row no-gutters>
      <v-col cols="4">
        <v-card>
          <v-card-item id="rss-card-2">
            <v-card-title>
              <v-toolbar>
                <v-toolbar-title>RSS 订阅</v-toolbar-title>
                <v-spacer />
                <v-btn
                  :icon="mdiPlus"
                  @click="openAddRssSubscriptionDialog"
                >
                  <v-icon :icon="mdiPlus" />
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                    >添加订阅
                  </v-tooltip>
                </v-btn>
                <v-btn :icon="mdiUpdate">
                  <v-icon :icon="mdiUpdate" />
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                    >全部更新
                  </v-tooltip>
                </v-btn>
                <v-btn
                  :icon="mdiRead"
                  @click="markAllRssSubscriptionRead"
                >
                  <v-icon :icon="mdiRead" />
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                    >全部已读
                  </v-tooltip>
                </v-btn>
              </v-toolbar>
            </v-card-title>
          </v-card-item>
          <v-card-text
            id="rss-card-3"
            class="overflow-y-auto"
          >
            <v-row
              dense
              v-for="rssSubscription in rssSubscriptionList"
              :key="rssSubscription.id"
            >
              <v-col>
                <v-card variant="tonal">
                  <v-card-item>
                    <v-card-title style="white-space: normal">
                      {{ rssSubscription.title }}
                    </v-card-title>
                    <v-card-subtitle> 总数：100；未读：10</v-card-subtitle>
                  </v-card-item>
                  <v-card-text v-if="rssSubscription.link">
                    {{ rssSubscription.link }}
                  </v-card-text>
                  <v-card-actions>
                    <v-btn
                      density="compact"
                      :icon="mdiDelete"
                      @click="deleteRssSubscription(rssSubscription.id)"
                    >
                      <v-icon :icon="mdiDelete" />
                      <v-tooltip
                        activator="parent"
                        location="bottom"
                        >删除
                      </v-tooltip>
                    </v-btn>
                    <v-btn
                      density="compact"
                      :icon="mdiUpdate"
                      @click="markRssSubscriptionRead(rssSubscription.id)"
                    >
                      <v-icon :icon="mdiUpdate" />
                      <v-tooltip
                        activator="parent"
                        location="bottom"
                        >更新
                      </v-tooltip>
                    </v-btn>
                    <v-btn
                      density="compact"
                      :icon="mdiRead"
                    >
                      <v-icon :icon="mdiRead" />
                      <v-tooltip
                        activator="parent"
                        location="bottom"
                        >已读
                      </v-tooltip>
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="8">
        <v-sheet>
          <v-window v-model="tab">
            <v-window-item value="option-1">
              <v-card>
                <v-card-text>
                  <p>1</p>
                </v-card-text>
              </v-card>
            </v-window-item>
            <v-window-item value="option-2">
              <v-card>
                <v-card-text> 2</v-card-text>
              </v-card>
            </v-window-item>
            <v-window-item value="option-3">
              <v-card>
                <v-card-text> 3</v-card-text>
              </v-card>
            </v-window-item>
          </v-window>
        </v-sheet>
      </v-col>
    </v-row>
  </v-card>
</template>

<style scoped></style>
