<script setup lang="ts">
import { ref, onMounted } from 'vue'
import rssApi from '@/api/anime/rssApi'
import { mdiDelete, mdiPencil, mdiPlus, mdiRead, mdiUpdate } from '@mdi/js'
import { useConfirm, useSnackbar } from 'vuetify-use-dialog'

const unreadRss = ref({
  id: 0,
  title: '所有未读',
  link: '',
  ttl: undefined,
})
const rssSubscriptionList = ref([unreadRss.value])
const addLinkRef = ref('')
const addRssDialogRef = ref(false)
const editRssDialogRef = ref(false)
const nullRss = {
  id: -1,
  title: '',
  link: '',
  ttl: undefined,
  items: [
    {
      id: 0,
      title: '',
      link: '',
      isRead: false,
      pubDate: undefined,
    },
  ],
}
const editRssRef = ref(nullRss)
const currentRssRef = ref(nullRss)

const createConfirm = useConfirm()
const createSnackbar = useSnackbar()

const getRssSubscriptionList = async (page: number, size: number) => {
  rssApi.queryRssSubscription(page, size).then((res) => {
    rssSubscriptionList.value = rssSubscriptionList.value.concat(res.data.data)
  })
}

function getRssSubscriptionById(id: number) {
  return rssSubscriptionList.value.find((rssSubscription) => rssSubscription.id === id)
}

function openAddRssSubscriptionDialog() {
  addLinkRef.value = ''
  addRssDialogRef.value = true
}

function addRssSubscription() {
  rssApi.addRssSubscription(addLinkRef.value).then((res) => {
    addRssDialogRef.value = false
    rssSubscriptionList.value = rssSubscriptionList.value.concat(res.data.data)
  })
}

function deleteRssSubscription(rssSubscriptionId: number) {
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

async function markAllRssSubscriptionRead() {
  for (const rssSubscription of rssSubscriptionList.value.filter((v) => v.id !== 0)) {
    await rssApi.markRssSubscriptionRead(rssSubscription.id)
  }
  // 刷新当前订阅
  if (currentRssRef.value.id !== -1) {
    getRssSubscriptionItem(currentRssRef.value.id)
  }
}

async function markRssSubscriptionRead(rssSubscriptionId: number) {
  if (rssSubscriptionId === 0) {
    for (const rssSubscription of rssSubscriptionList.value.filter((v) => v.id !== 0)) {
      await rssApi.markRssSubscriptionRead(rssSubscription.id)
    }
  } else {
    await rssApi.markRssSubscriptionRead(rssSubscriptionId)
  }
  // 刷新当前订阅
  if (currentRssRef.value.id === rssSubscriptionId) {
    getRssSubscriptionItem(rssSubscriptionId)
  }
}

async function updateAllRssSubscriptionsItem() {
  for (const rssSubscription of rssSubscriptionList.value.filter((v) => v.id !== 0)) {
    await rssApi.updateRssSubscriptionItem(rssSubscription.id)
  }
  // 刷新当前订阅
  if (currentRssRef.value.id !== -1) {
    getRssSubscriptionItem(currentRssRef.value.id)
  }
}

async function updateRssSubscriptionItem(rssSubscriptionId: number) {
  if (rssSubscriptionId === 0) {
    for (const rssSubscription of rssSubscriptionList.value.filter((v) => v.id !== 0)) {
      await rssApi.updateRssSubscriptionItem(rssSubscription.id)
    }
  } else {
    await rssApi.updateRssSubscriptionItem(rssSubscriptionId)
  }
  // 刷新当前订阅
  if (currentRssRef.value.id === rssSubscriptionId) {
    getRssSubscriptionItem(rssSubscriptionId)
  }
}

function openEditRssSubscriptionDialog(v: any) {
  editRssRef.value = { ...v }
  editRssDialogRef.value = true
}

function editRssSubscription() {
  const { id, title, ttl } = editRssRef.value
  rssApi.editRssSubscription(id, title, ttl).then((res) => {
    editRssDialogRef.value = false
    rssSubscriptionList.value = rssSubscriptionList.value.map((rssSubscription) => {
      if (rssSubscription.id === id) {
        return res.data.data
      }
      return rssSubscription
    })
  })
}

function getRssSubscriptionItem(rssSubscriptionId: number) {
  console.log(rssSubscriptionId)
  if (rssSubscriptionId !== -1) {
    const currentRss = getRssSubscriptionById(rssSubscriptionId)
    if (!currentRss) {
      currentRssRef.value = nullRss
    } else {
      rssApi.queryRssSubscriptionItem(rssSubscriptionId, 0, 0).then((res) => {
        currentRssRef.value = {
          ...currentRss,
          items: res.data.data,
        }
      })
    }
  } else {
    currentRssRef.value = nullRss
  }
}

onMounted(() => {
  getRssSubscriptionList(0, 0)
})
</script>

<template>
  <v-dialog
    v-model="addRssDialogRef"
    width="500"
  >
    <v-card>
      <v-card-text>
        <v-text-field
          v-model="addLinkRef"
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
          @click="addRssDialogRef = false"
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
    v-model="editRssDialogRef"
    width="500"
  >
    <v-card>
      <v-card-text>
        <v-form>
          <v-text-field
            v-model="editRssRef.title"
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
          @click="editRssDialogRef = false"
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
  <v-card>
    <v-row no-gutters>
      <v-col cols="4">
        <v-card>
          <v-card-title>
            <div class="mr-1">
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
            </div>
          </v-card-title>
          <v-card-text>
            <v-item-group
              selected-class="text-primary"
              @update:model-value="getRssSubscriptionItem"
            >
              <v-virtual-scroll
                :items="rssSubscriptionList"
                style="height: calc(100dvh - 64px - 16px)"
              >
                <template #default="{ item }">
                  <v-row
                    no-gutters
                    class="mb-1 mr-1"
                    :key="item.id"
                  >
                    <v-col>
                      <v-item
                        :value="item.id"
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
                              {{ item.title }}
                            </v-card-title>
                            <v-card-subtitle> 总数：100；未读：10</v-card-subtitle>
                          </v-card-item>
                          <v-card-text v-if="item.link">
                            {{ item.link }}
                          </v-card-text>
                          <v-card-actions>
                            <v-btn
                              density="compact"
                              :icon="mdiPencil"
                              @click.stop="openEditRssSubscriptionDialog(item)"
                              v-if="item.id !== 0"
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
                              @click.stop="deleteRssSubscription(item.id)"
                              v-if="item.id !== 0"
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
                              @click.stop="updateRssSubscriptionItem(item.id)"
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
                              @click.stop="markRssSubscriptionRead(item.id)"
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
                </template>
              </v-virtual-scroll>
            </v-item-group>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="8">
        <v-sheet class="mt-2">
          <v-item-group selected-class="text-primary">
            <v-virtual-scroll
              :items="currentRssRef.items"
              style="height: calc(100dvh - 8px)"
            >
              <template #default="{ item }">
                <v-row
                  no-gutters
                  class="mb-1 mr-1"
                  :key="item.id"
                >
                  <v-col>
                    <v-item
                      :value="item.id"
                      v-slot="{ isSelected, selectedClass, toggle }"
                    >
                      <v-card
                        @click="toggle"
                        variant="tonal"
                      >
                        <v-card-item>
                          <v-card-title
                            :class="[
                              isSelected ? selectedClass : item.isRead ? '' : 'text-secondary',
                            ]"
                            style="white-space: normal"
                          >
                            {{ item.title }}
                          </v-card-title>
                          <v-card-subtitle>{{ item.pubDate }}</v-card-subtitle>
                        </v-card-item>
                        <v-card-text v-if="item.link">
                          {{ item.link }}
                        </v-card-text>
                      </v-card>
                    </v-item>
                  </v-col>
                </v-row>
              </template>
            </v-virtual-scroll>
          </v-item-group>
        </v-sheet>
      </v-col>
    </v-row>
  </v-card>
</template>

<style scoped></style>
