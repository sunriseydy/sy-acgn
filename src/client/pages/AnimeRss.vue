<script setup>
import { ref, onMounted } from 'vue'
import rssApi from '@/client/api/rssApi.js'
import { mdiDelete, mdiPencil, mdiPlus, mdiRead, mdiUpdate } from '@mdi/js'
import { useConfirm, useSnackbar } from 'vuetify-use-dialog'

const unreadRss = ref({
  id: 0,
  title: '所有未读',
})
const rssSubscriptionList = ref([unreadRss.value])
const addLink = ref('')
const addRssDialog = ref(false)
const editRssDialog = ref(false)
const editRss = ref({})
const nullRss = {
  id: null,
  title: null,
  link: null,
  items: [],
}
const currentRss = ref(nullRss)

const createConfirm = useConfirm()
const createSnackbar = useSnackbar()

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
  createConfirm({
    title: '确定删除当前订阅？',
    confirmationText: '确定',
    cancellationText: '取消',
    dialogProps: {
      width: 500,
    },
  }).then((res) => {
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
  rssSubscriptionList.value
    .filter((rssSubscription) => rssSubscription.id !== 0)
    .forEach((rssSubscription) => {
      rssApi.markRssSubscriptionRead(rssSubscription.id)
    })
  // ydy todo 刷新
}

function markRssSubscriptionRead(rssSubscriptionId) {
  if (rssSubscriptionId === 0) {
    markAllRssSubscriptionRead()
  } else {
    rssApi.markRssSubscriptionRead(rssSubscriptionId)
    // ydy todo 刷新
  }
}

function updateAllRssSubscriptionsItem() {
  rssSubscriptionList.value
    .filter((rssSubscription) => rssSubscription.id !== 0)
    .forEach((rssSubscription) => {
      rssApi.updateRssSubscriptionItem(rssSubscription.id)
    })
  // ydy todo 刷新
}

function updateRssSubscriptionItem(rssSubscriptionId) {
  if (rssSubscriptionId === 0) {
    updateAllRssSubscriptionsItem()
  } else {
    rssApi.updateRssSubscriptionItem(rssSubscriptionId)
    // ydy todo 刷新
  }
}

function openEditRssSubscriptionDialog(v) {
  editRss.value = { ...v }
  editRssDialog.value = true
}

function editRssSubscription() {
  const { id, title, ttl } = editRss.value
  rssApi.editRssSubscription(id, title, ttl).then((res) => {
    editRssDialog.value = false
    rssSubscriptionList.value = rssSubscriptionList.value.map((rssSubscription) => {
      if (rssSubscription.id === id) {
        return res.data.data
      }
      return rssSubscription
    })
  })
}

function onCurrentRsschange(rssSubscription) {
  console.log(rssSubscription)
  if (rssSubscription) {
    currentRss.value = rssSubscription
  } else {
    currentRss.value = nullRss
  }
}

onMounted(() => {
  getRssSubscriptionList(0, 0)
})
</script>

<template>
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
  <v-dialog
    v-model="editRssDialog"
    width="500"
  >
    <v-card>
      <v-card-text>
        <v-form>
          <v-text-field
            v-model="editRss.title"
            label="订阅标题"
            variant="outlined"
            :rules="[(v) => !!v || '请输入订阅标题']"
          />
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="primary"
          variant="text"
          @click="editRssDialog = false"
        >
          取消
        </v-btn>
        <v-btn
          color="primary"
          variant="text"
          @click="editRssSubscription"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <v-card
    id="rss-card-1"
    class="h-screen d-flex"
    v-resize="updateHeight"
  >
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
                <v-btn
                  :icon="mdiUpdate"
                  @click="updateAllRssSubscriptionsItem"
                >
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
            <v-item-group
              selected-class="text-primary"
              @update:model-value="onCurrentRsschange"
            >
              <v-row
                dense
                v-for="rssSubscription in rssSubscriptionList"
                :key="rssSubscription.id"
              >
                <v-col>
                  <v-item
                    :value="rssSubscription"
                    v-slot="{ selectedClass, toggle }"
                  >
                    <v-card
                      @click="toggle"
                      variant="tonal"
                    >
                      <v-card-item>
                        <v-card-title
                          :class="selectedClass"
                          style="white-space: normal"
                        >
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
                          :icon="mdiPencil"
                          @click="openEditRssSubscriptionDialog(rssSubscription)"
                          v-if="rssSubscription.id !== 0"
                        >
                          <v-icon :icon="mdiPencil" />
                          <v-tooltip
                            activator="parent"
                            location="bottom"
                            >编辑
                          </v-tooltip>
                        </v-btn>
                        <v-btn
                          density="compact"
                          :icon="mdiDelete"
                          @click="deleteRssSubscription(rssSubscription.id)"
                          v-if="rssSubscription.id !== 0"
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
                          @click="updateRssSubscriptionItem(rssSubscription.id)"
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
                          @click="markRssSubscriptionRead(rssSubscription.id)"
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
                  </v-item>
                </v-col>
              </v-row>
            </v-item-group>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="8">
        <v-card>
          <v-card-text> 111</v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-card>
</template>

<style scoped></style>
