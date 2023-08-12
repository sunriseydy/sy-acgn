<script setup>
import { ref, onUpdated, onMounted, onUnmounted } from 'vue'

const tab = ref('option-1')
ref([
  { id: 1, title: '1' },
  { id: 2, title: '2' },
])
const count = ref(12)
function increment() {
  count.value++
}

function sub() {
  count.value--
}
ref(800)
function updateHeight() {
  const aEl = document.querySelector('#rss-card-1')
  const bEl = document.querySelector('#rss-card-2')

  const aHeight = aEl.clientHeight
  const bHeight = bEl.clientHeight

  const cHeight = aHeight - bHeight

  document.querySelector('#rss-card-3').style.height = `${cHeight}px`
  console.log(aHeight, bHeight, cHeight)
}

onUpdated(() => {
  updateHeight()
})

onMounted(() => {
  updateHeight()
  window.addEventListener('resize', updateHeight)
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
    <v-row no-gutters>
      <v-col cols="3">
        <v-card>
          <v-card-item id="rss-card-2">
            <v-card-title>
              <v-toolbar>
                <v-toolbar-title>RSS 订阅</v-toolbar-title>
                <v-spacer />
                <v-toolbar-items>
                  <v-btn
                    @click="increment"
                    text="添加"
                  />
                  <v-btn
                    @click="sub"
                    text="删除"
                  />
                  <v-btn text="更新" />
                  <v-btn text="已读" />
                </v-toolbar-items>
              </v-toolbar>
            </v-card-title>
          </v-card-item>
          <v-card-text
            id="rss-card-3"
            class="overflow-y-auto"
          >
            <v-row
              dense
              v-for="rssSubscription in count"
              :key="rssSubscription"
            >
              <v-col>
                <v-card variant="tonal">
                  <v-card-item>
                    <v-card-title style="white-space: normal">
                      {{ String(rssSubscription).repeat(100) }}
                    </v-card-title>
                    <v-card-subtitle> 总数：100；未读：10 </v-card-subtitle>
                  </v-card-item>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="9">
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
                <v-card-text> 2 </v-card-text>
              </v-card>
            </v-window-item>
            <v-window-item value="option-3">
              <v-card>
                <v-card-text> 3 </v-card-text>
              </v-card>
            </v-window-item>
          </v-window>
        </v-sheet>
      </v-col>
    </v-row>
  </v-card>
</template>

<style scoped></style>
